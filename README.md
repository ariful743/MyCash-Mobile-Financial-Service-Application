# MyCash - Mobile Financial Service (MFS) Application

## 📱 Project Overview
**MyCash** is a desktop-based Mobile Financial Service (MFS) application built with **Java** and **Swing (GUI)**. It simulates a digital wallet experience similar to services like bKash or Nagad, allowing users to perform financial transactions securely and efficiently.

The project demonstrates core **Object-Oriented Programming (OOP)** principles such as **Inheritance**, **Encapsulation**, **Polymorphism**, and **Abstraction**, integrated with a **MySQL** database for reliable data persistence.

---

## ✨ Key Features

myCash serves three distinct user roles, each with specialized capabilities:

### 1. 👤 **Customer**
Regular users who use the service for personal finance.
- **Send Money**: Transfer funds to other customers instantly.
- **Cash Out**: Withdraw money via an Agent (requires Agent approval).
- **Cash In**: Deposit money via an Agent (requires Agent approval).
- **Transaction Statement**: View a history of all personal transactions.
- **Real-time Balance**: View current account balance on the dashboard.

### 2. 🏪 **Agent**
Business partners who facilitate cash conversions.
- **Approve Requests**: Accept or Reject Cash Out and Cash In requests from customers.
- **Add Money**: Request system float (virtual money) from the Bank.
- **Commission**: Earn commissions on transactions (e.g., Cash Cash In/Out).
- **Statement**: Track business transactions.

### 3. 🏦 **Bank (Admin)**
The central authority managing the system's liquidity.
- **Approve Add Money**: Authorize requests from Agents to add funds to their accounts.
- **System Monitoring**: View all registered Customers and Agents.
- **Global Statement**: Access a complete log of all transactions within the system.

---

## 🛠️ Technology Stack
- **Language**: Java (JDK 8+)
- **GUI Framework**: Java Swing (CardLayout, Custom Components)
- **Database**: MySQL (JDBC)
- **Design Pattern**: MVC (Model-View-Controller) Architecture
  - **Models**: `User`, `Customer`, `Agent`, `Bank`
  - **DAO**: Data Access Objects for SQL operations
  - **Services**: Business logic for transaction validation
  - **UI**: Swing-based views (`AuthView`, `DashboardView`)

---

## 🚀 Setup & Installation

### Prerequisites
1.  **Java Development Kit (JDK)** installed.
2.  **MySQL Server** installed and running.
3.  **MySQL JDBC Driver** (included in `lib/` folder).

### Database Setup
1.  Open your MySQL Workbench or Command Line.
2.  Create the database:
    ```sql
    CREATE DATABASE mycash_db;
    ```
3.  The application will automatically create the necessary tables (`users`, `transactions`, `transaction_requests`) on the first run.

### Running the Application
You can run the application directly using the provided batch scripts (Windows):

1.  **Compile the Code**:
    Double-click `compile.bat` to compile all Java files.
    
2.  **Run the App**:
    Double-click `run.bat` to launch the application.

*Alternatively, via Command Line:*
```bash
# Compile
javac -d bin -sourcepath src -cp "lib/*" src/app/SwingMain.java

# Run
java -cp "bin;lib/mysql-connector-java-8.0.20.jar" app.SwingMain
```

---

## 📖 Usage Guide

### 1. Registration
- Launch the app and select **"Register"**.
- Enter your **Name**, **Phone Number**, **PIN**, and select your **Account Type** (Customer or Agent).
- *Note: The "Bank" account is a special system account usually pre-seeded or created manually in the DB.*

### 2. Logging In
- Enter your **Phone Number** and **PIN**.
- The dashboard will automatically adapt based on your role (Customer/Agent/Bank).

### 3. Performing Transactions
- **Icons**: The interface uses intuitive Emoji icons (💸, 🏧, 💰) for actions.
- **Flow**: 
    - Click an action (e.g., "Send Money").
    - Enter the recipient's phone number and amount.
    - Confirm.
- **Approvals**: 
    - Cash Out/In requests go to the "Pension Requests" tab of the specified Agent.
    - The Agent must login to "Approve" or "Reject" them.


## 📂 Project Structure
```
src/
├── app/            # Main entry point (SwingMain.java)
├── dao/            # Database Access Objects (handle SQL)
├── models/         # Data classes (User, Customer, Agent)
├── services/       # Business logic (Transaction processing)
└── ui/             # GUI Screens (Dashboard, Login, components)
```

