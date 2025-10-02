package me.m41k0n;

import java.util.List;

public class MultiNodeDemo {

    public static void main(String[] args) {
        Wallet satoshi = new Wallet();
        Wallet alice = new Wallet();
        Wallet bob = new Wallet();
        Wallet carol = new Wallet();

        // Each node has its own blockchain (with the same genesis)
        Blockchain chainA = new Blockchain(4, satoshi.getAddress());
        Blockchain chainB = new Blockchain(4, satoshi.getAddress());
        Blockchain chainC = new Blockchain(4, satoshi.getAddress());
        Blockchain chainD = new Blockchain(4, satoshi.getAddress());

        // Create nodes and connect to each other
        SimpleNode nodeA = new SimpleNode(chainA, 5000);
        SimpleNode nodeB = new SimpleNode(chainB, 5001);
        SimpleNode nodeC = new SimpleNode(chainC, 5002);
        SimpleNode nodeD = new SimpleNode(chainD, 5003);

        nodeA.addPeer("localhost", 5001); nodeA.addPeer("localhost", 5002); nodeA.addPeer("localhost", 5003);
        nodeB.addPeer("localhost", 5000); nodeB.addPeer("localhost", 5002); nodeB.addPeer("localhost", 5003);
        nodeC.addPeer("localhost", 5000); nodeC.addPeer("localhost", 5001); nodeC.addPeer("localhost", 5003);
        nodeD.addPeer("localhost", 5000); nodeD.addPeer("localhost", 5001); nodeD.addPeer("localhost", 5002);

        // Start nodes in parallel
        new Thread(nodeA::start).start();
        new Thread(nodeB::start).start();
        new Thread(nodeC::start).start();
        new Thread(nodeD::start).start();

        // Important: Wait a bit for all servers to start
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        Transaction tA1 = Transaction.createSigned(satoshi, alice.getAddress(), 20.0);
        chainA.addBlock(List.of(tA1));
        nodeA.broadcastBlock(chainA.getLatestBlock());

        Transaction tB1 = Transaction.createSigned(alice, bob.getAddress(), 5.0);
        chainB.addBlock(List.of(tB1));
        nodeB.broadcastBlock(chainB.getLatestBlock());

        Transaction tC1 = Transaction.createSigned(bob, carol.getAddress(), 2.0);
        chainC.addBlock(List.of(tC1));
        nodeC.broadcastBlock(chainC.getLatestBlock());

        Transaction tD1 = Transaction.createSigned(carol, alice.getAddress(), 1.0);
        chainD.addBlock(List.of(tD1));
        nodeD.broadcastBlock(chainD.getLatestBlock());

        // Let the network synchronize a bit
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        System.out.println("Node A chain: " + chainA.getBlocks());
        System.out.println("Node B chain: " + chainB.getBlocks());
        System.out.println("Node C chain: " + chainC.getBlocks());
        System.out.println("Node D chain: " + chainD.getBlocks());
    }
}