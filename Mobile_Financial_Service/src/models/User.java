package models;

public abstract class User {
    protected String name;
    protected String phoneNumber;
    protected String pin;
    protected double balance;
    protected UserRole role;

    public User(String name, String phoneNumber, String pin, double balance, UserRole role) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.balance = balance;
        this.role = role;
    }

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public double getBalance() {
        return balance;
    }

    // Updates local object state (DB update happens in DAO)
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public UserRole getRole() {
        return role;
    }
}