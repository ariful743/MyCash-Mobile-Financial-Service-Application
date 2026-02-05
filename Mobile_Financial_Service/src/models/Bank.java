package models;

public class Bank extends User {
    public Bank(String name, String phoneNumber, String pin, double balance) {
        super(name, phoneNumber, pin, balance, UserRole.BANK);
    }
}
