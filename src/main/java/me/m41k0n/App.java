package me.m41k0n;

import java.util.List;

public class App {

    public static void main(String[] args) {
        Wallet satoshi = new Wallet();
        Wallet alice = new Wallet();
        Wallet bob = new Wallet();
        Wallet carol = new Wallet();

        Blockchain blockchain = new Blockchain(4, satoshi.getAddress());

        printBalances(blockchain, satoshi, alice, bob, carol);
        printSeparator();

        // Agora sempre informa o minerador ao adicionar um bloco!
        processTransaction(blockchain, "Attempt: Alice pays Bob 10.0 BTC", Transaction.createSigned(alice, bob.getAddress(), 10.0), alice, bob, carol, satoshi, alice);
        processTransaction(blockchain, "Satoshi pays Alice 25.0 BTC", Transaction.createSigned(satoshi, alice.getAddress(), 25.0), alice, bob, carol, satoshi, satoshi);
        processTransaction(blockchain, "Alice pays Bob 10.0 BTC", Transaction.createSigned(alice, bob.getAddress(), 10.0), alice, bob, carol, satoshi, alice);
        processTransaction(blockchain, "Attempt: Bob pays Carol 15.0 BTC", Transaction.createSigned(bob, carol.getAddress(), 15.0), alice, bob, carol, satoshi, bob);
        processTransaction(blockchain, "Bob pays Carol 7.0 BTC", Transaction.createSigned(bob, carol.getAddress(), 7.0), alice, bob, carol, satoshi, bob);
        processTransaction(blockchain, "Alice pays Carol 5.0 BTC", Transaction.createSigned(alice, carol.getAddress(), 5.0), alice, bob, carol, satoshi, alice);

        System.out.println("Full blockchain:");
        for (Block block : blockchain.getBlocks()) {
            System.out.println("Index: " + block.getIndex());
            System.out.println("Timestamp: " + block.getTimestamp());
            System.out.println("Transactions: " + block.getTransactions());
            System.out.println("Prev Hash: " + block.getPreviousHash());
            System.out.println("Hash: " + block.getHash());
            System.out.println("Nonce: " + block.getNonce());
            System.out.println("-----------------------------");
        }
    }

    private static void processTransaction(
            Blockchain blockchain,
            String description,
            Transaction transaction,
            Wallet alice,
            Wallet bob,
            Wallet carol,
            Wallet satoshi,
            Wallet miner
    ) {
        boolean added = blockchain.addBlock(List.of(transaction), miner.getAddress());
        System.out.println(description);
        System.out.println("Transaction valid? " + added);
        printBalances(blockchain, satoshi, alice, bob, carol);
        printLatestBlock(blockchain);
        printSeparator();
    }

    private static void printBalances(Blockchain blockchain, Wallet satoshi, Wallet alice, Wallet bob, Wallet carol) {
        System.out.println("Satoshi balance: " + blockchain.getBalance(satoshi.getAddress()));
        System.out.println("Alice balance: " + blockchain.getBalance(alice.getAddress()));
        System.out.println("Bob balance: " + blockchain.getBalance(bob.getAddress()));
        System.out.println("Carol balance: " + blockchain.getBalance(carol.getAddress()));
    }

    private static void printLatestBlock(Blockchain blockchain) {
        Block block = blockchain.getLatestBlock();
        System.out.println("Block added:");
        System.out.println("Index: " + block.getIndex());
        System.out.println("Transactions: " + block.getTransactions());
        System.out.println("Hash: " + block.getHash());
    }

    private static void printSeparator() {
        System.out.println("--------------------------------------------------");
    }
}