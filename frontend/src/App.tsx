import { useEffect, useMemo, useState } from "react";
import type { ReactElement } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { api } from "./api";
import { Bar, Doughnut, Line } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, BarElement, CategoryScale, Legend, LinearScale, LineElement, PointElement, Tooltip } from "chart.js";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

ChartJS.register(ArcElement, BarElement, CategoryScale, LinearScale, LineElement, PointElement, Tooltip, Legend);

type Tx = { id: number; amount: number; type: "INCOME" | "EXPENSE"; category: string; description: string; date: string };

function AuthPage({ mode }: { mode: "login" | "signup" }) {
  const nav = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const submit = async () => {
    const url = mode === "login" ? "/auth/login" : "/auth/register";
    const { data } = await api.post(url, form);
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", data.name);
    nav("/dashboard");
  };
  return (
    <div className="auth-wrap">
      <div className="glass">
        <h1>SmartSpend AI</h1>
        <p>{mode === "login" ? "Welcome back" : "Create account"}</p>
        {mode === "signup" && <input placeholder="Name" onChange={(e) => setForm({ ...form, name: e.target.value })} />}
        <input placeholder="Email" onChange={(e) => setForm({ ...form, email: e.target.value })} />
        <input placeholder="Password" type="password" onChange={(e) => setForm({ ...form, password: e.target.value })} />
        <button onClick={submit}>{mode === "login" ? "Login" : "Sign Up"}</button>
        <button className="link" onClick={() => nav(mode === "login" ? "/signup" : "/login")}>
          {mode === "login" ? "Need an account?" : "Already have an account?"}
        </button>
      </div>
    </div>
  );
}

function Dashboard() {
  const nav = useNavigate();
  const [tx, setTx] = useState<Tx[]>([]);
  const [summary, setSummary] = useState<any>(null);
  const [insight, setInsight] = useState<{ insight: string; financialHealthScore: number } | null>(null);
  const [form, setForm] = useState({ amount: "", type: "EXPENSE", category: "Food", description: "", date: "" });
  const [filters, setFilters] = useState({ month: "", year: "", category: "", type: "" });

  const load = async () => {
    const params: any = {};
    Object.entries(filters).forEach(([k, v]) => v && (params[k] = v));
    const [txRes, sumRes] = await Promise.all([api.get("/transactions", { params }), api.get("/transactions/summary")]);
    setTx(txRes.data);
    setSummary(sumRes.data);
  };

  useEffect(() => {
    load();
  }, [filters.month, filters.year, filters.category, filters.type]);

  const addTx = async () => {
    await api.post("/transactions", { ...form, amount: Number(form.amount) });
    setForm({ amount: "", type: "EXPENSE", category: "Food", description: "", date: "" });
    load();
  };

  const generateInsight = async () => {
    if (!summary) return;
    const payload = { income: summary.totalIncome, expenses: Object.entries(summary.expensesByCategory || {}).map(([category, amount]) => ({ category, amount })) };
    const { data } = await api.post("/insights", payload);
    setInsight(data);
  };

  const downloadPdf = () => {
    const doc = new jsPDF();
    doc.text("SmartSpend Monthly Report", 14, 12);
    autoTable(doc, {
      head: [["Date", "Type", "Category", "Amount", "Description"]],
      body: tx.map((t) => [t.date, t.type, t.category, t.amount, t.description || "-"]),
    });
    doc.save("smartspend-report.pdf");
  };

  const pieData = useMemo(
    () => ({
      labels: Object.keys(summary?.expensesByCategory || {}),
      datasets: [{ data: Object.values(summary?.expensesByCategory || {}), backgroundColor: ["#60a5fa", "#f472b6", "#facc15", "#34d399", "#a78bfa"] }],
    }),
    [summary]
  );

  const trendLabels = (summary?.monthlyTrend || []).map((m: any) => m.month);
  return (
    <div className="dash">
      <header>
        <h2>SmartSpend AI Dashboard</h2>
        <div>
          <button onClick={downloadPdf}>Export PDF</button>
          <button onClick={() => { localStorage.clear(); nav("/login"); }}>Logout</button>
        </div>
      </header>
      <div className="cards">
        <div className="card">Income: ₹{summary?.totalIncome || 0}</div>
        <div className="card">Expense: ₹{summary?.totalExpense || 0}</div>
        <div className="card">Balance: ₹{summary?.balance || 0}</div>
        <div className="card">Health Score: {insight?.financialHealthScore ?? "--"}/100</div>
      </div>
      <div className="grid">
        <div className="panel">
          <h3>Add Transaction</h3>
          <input placeholder="Amount" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
          <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}><option>EXPENSE</option><option>INCOME</option></select>
          <input placeholder="Category" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
          <input placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          <input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
          <button onClick={addTx}>Save</button>
        </div>
        <div className="panel"><h3>Expense Categories</h3><Doughnut data={pieData} /></div>
        <div className="panel"><h3>Monthly Trend</h3><Bar data={{ labels: trendLabels, datasets: [{ label: "Expense", data: (summary?.monthlyTrend || []).map((m: any) => m.expense), backgroundColor: "#f87171" }] }} /></div>
        <div className="panel"><h3>Income vs Expense</h3><Line data={{ labels: trendLabels, datasets: [{ label: "Income", data: (summary?.monthlyTrend || []).map((m: any) => m.income), borderColor: "#4ade80" }, { label: "Expense", data: (summary?.monthlyTrend || []).map((m: any) => m.expense), borderColor: "#fb7185" }] }} /></div>
      </div>
      <div className="panel">
        <h3>Filters</h3>
        <div className="row">
          <input placeholder="Month (1-12)" onChange={(e) => setFilters({ ...filters, month: e.target.value })} />
          <input placeholder="Year" onChange={(e) => setFilters({ ...filters, year: e.target.value })} />
          <input placeholder="Category" onChange={(e) => setFilters({ ...filters, category: e.target.value })} />
          <select onChange={(e) => setFilters({ ...filters, type: e.target.value })}><option value="">All</option><option>INCOME</option><option>EXPENSE</option></select>
          <button onClick={generateInsight}>Generate AI Insight</button>
        </div>
        {insight && <p className="insight">{insight.insight}</p>}
      </div>
      <div className="panel">
        <h3>Recent Transactions</h3>
        <table>
          <thead><tr><th>Date</th><th>Type</th><th>Category</th><th>Amount</th><th>Description</th><th>Action</th></tr></thead>
          <tbody>
            {tx.map((t) => <tr key={t.id}><td>{t.date}</td><td>{t.type}</td><td>{t.category}</td><td>₹{t.amount}</td><td>{t.description}</td><td><button onClick={async () => { await api.delete(`/transactions/${t.id}`); load(); }}>Delete</button></td></tr>)}
          </tbody>
        </table>
      </div>
    </div>
  );
}

const Protected = ({ children }: { children: ReactElement }) => (localStorage.getItem("token") ? children : <Navigate to="/login" />);

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="/login" element={<AuthPage mode="login" />} />
      <Route path="/signup" element={<AuthPage mode="signup" />} />
      <Route path="/dashboard" element={<Protected><Dashboard /></Protected>} />
    </Routes>
  );
}
