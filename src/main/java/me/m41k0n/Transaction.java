package me.m41k0n;

public record Transaction(String from, String to, double amount) {

    @Override
    public String toString() {
        if (from == null || from.equals("GENESIS")) {
            return "COINBASE -> " + to + ": " + amount;
        }
        return from + " -> " + to + ": " + amount;
    }
}