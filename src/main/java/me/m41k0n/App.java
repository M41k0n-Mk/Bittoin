package me.m41k0n;

import java.util.List;

public class App {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain(4);

        Wallet alice = new Wallet();
        Wallet bob = new Wallet();

        Transaction t1 = Transaction.createSigned(alice, bob.getAddress(), 10.0);

        Transaction t2 = Transaction.createSigned(bob, alice.getAddress(), 4.5);

        blockchain.addBlock(List.of(t1, t2));

        System.out.println("Blockchain:");
        for (Block block : blockchain.getBlocks()) {
            System.out.println("Index: " + block.getIndex());
            System.out.println("Transactions: " + block.getTransactions());
            System.out.println("Prev Hash: " + block.getPreviousHash());
            System.out.println("Hash: " + block.getHash());
            System.out.println("Nonce: " + block.getNonce());
            System.out.println("-----------------------------");
        }

        System.out.println("Is blockchain valid? " + blockchain.isValid());
    }
}