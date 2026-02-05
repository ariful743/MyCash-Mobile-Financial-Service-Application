package models;

public class Agent extends User {
    public Agent(String name, String phoneNumber, String pin, double balance) {
        super(name, phoneNumber, pin, balance, UserRole.AGENT);
    }
}