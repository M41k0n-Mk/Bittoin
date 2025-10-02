package me.m41k0n;

import lombok.Getter;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@Getter
public class Block {

    private final int index;
    private final Instant timestamp;
    private final List<Transaction> transactions;
    private final String previousHash;
    private final long nonce;
    private final String hash;

    public Block(int index, List<Transaction> transactions, String previousHash, int difficulty) {
        this.index = index;
        this.timestamp = Instant.now();
        this.transactions = List.copyOf(transactions);
        this.previousHash = previousHash;

        var result = mineBlock(difficulty);
        this.nonce = result.nonce();
        this.hash = result.hash();
    }

    private record MiningResult(long nonce, String hash) {}

    private MiningResult mineBlock(int difficulty) {
        String prefix = "0".repeat(difficulty);
        long nonce = 0;
        String hash;
        do {
            hash = calculateHash(index, timestamp, transactions, previousHash, nonce);
            nonce++;
        } while (!hash.startsWith(prefix));
        return new MiningResult(nonce-1, hash);
    }

    public static String calculateHash(int index, Instant timestamp, List<Transaction> transactions, String previousHash, long nonce) {
        try {
            String toHash = index + timestamp.toString() + transactions.toString() + previousHash + nonce;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(toHash.getBytes());
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        return String.format("%064x", new java.math.BigInteger(1, bytes));
    }
}