# SmartSpend AI - Intelligent Expense Tracker

Full-stack personal finance application with JWT authentication, interactive dashboard, PDF export, and AI-powered insights using Groq API.

## Tech Stack
- Frontend: React + TypeScript + Vite + React Router + Chart.js + jsPDF
- Backend: Spring Boot + Spring Security + JWT + JPA
- Database: MySQL

## Project Structure
- `frontend` - React application with auth pages and protected dashboard
- `backend` - Spring Boot REST API with JWT auth and transaction services

## Features
- User signup/login with JWT authentication
- Protected routes and APIs
- Add/view/update/delete transactions
- Filters by month, year, category, and transaction type
- Dashboard cards: income, expense, balance, health score
- Charts: pie, bar, line
- AI insights using Groq API
- PDF report export for transactions

## Backend Setup
1. Install Java 17 and Maven.
2. Create MySQL database (or let app create it):
   - Database: `smartspend`
3. Update `backend/src/main/resources/application.properties`:
   - `spring.datasource.username`
   - `spring.datasource.password`
   - `app.groq.api-key` (optional but recommended)
4. Run backend:
   - `cd backend`
   - `mvn spring-boot:run`

Backend runs on `http://localhost:8080`.

## Frontend Setup
1. Run commands:
   - `cd frontend`
   - `npm install`
   - `npm run dev`

Frontend runs on `http://localhost:5173`.

## Main Routes
- `/login` - login page
- `/signup` - registration page
- `/dashboard` - protected SmartSpend AI dashboard

## Interview Highlight
SmartSpend AI acts as an intelligent financial assistant, not just a ledger. It combines secure JWT-based full-stack architecture, data analytics, and AI-generated personalized guidance from user transaction patterns.
