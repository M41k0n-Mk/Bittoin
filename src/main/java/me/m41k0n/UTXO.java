package me.m41k0n;

import java.io.Serializable;

public record UTXO(String txId, int outputIndex, String owner, double amount) implements Serializable {}