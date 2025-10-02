package me.m41k0n;

import java.util.List;

public class App {

    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain(4);

        Transaction t1 = new Transaction("Alice", "Bob", 10.0);
        Transaction t2 = new Transaction("Bob", "Carol", 5.5);

        blockchain.addBlock(List.of(t1, t2));

        Transaction t3 = new Transaction("Carol", "Dave", 2.0);
        blockchain.addBlock(List.of(t3));

        System.out.println("Blockchain:");
        for (Block block : blockchain.getBlocks()) {
            System.out.println("Index: " + block.getIndex());
            System.out.println("Timestamp: " + block.getTimestamp());
            System.out.println("Transactions: " + block.getTransactions());
            System.out.println("Prev Hash: " + block.getPreviousHash());
            System.out.println("Hash: " + block.getHash());
            System.out.println("Nonce: " + block.getNonce());
            System.out.println("-----------------------------");
        }

        System.out.println("Is blockchain valid? " + blockchain.isValid());
    }
}