package models;

public class Customer extends User {
    public Customer(String name, String phoneNumber, String pin, double balance) {
        super(name, phoneNumber, pin, balance, UserRole.CUSTOMER);
    }
}