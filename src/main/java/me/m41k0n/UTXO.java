package me.m41k0n;

public record UTXO(String txId, int outputIndex, String owner, double amount) {}