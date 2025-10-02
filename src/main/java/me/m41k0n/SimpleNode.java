package me.m41k0n;

import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleNode {

    private final Blockchain blockchain;
    private final int port;
    private final List<InetSocketAddress> peers = new ArrayList<>();

    public SimpleNode(Blockchain blockchain, int port) {
        this.blockchain = blockchain;
        this.port = port;
    }

    public void addPeer(String host, int peerPort) {
        peers.add(new InetSocketAddress(host, peerPort));
    }

    public void start() {
        new Thread(this::listen).start();
    }

    private void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Node listening on port " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (Exception e) {
                    System.out.println("Erro ao aceitar conexão: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            socket.setSoTimeout(8000);
            System.out.println("Recebendo conexão de " + socket.getInetAddress() + ":" + socket.getPort());
            Object commandObj = in.readObject();
            System.out.println("Comando recebido: " + commandObj);

            if ("GET_BLOCKCHAIN".equals(commandObj)) {
                out.writeObject(blockchain.getBlocks());
                out.flush();
            } else if ("NEW_BLOCK".equals(commandObj)) {
                Block newBlock = (Block) in.readObject();
                System.out.println("Novo bloco recebido do peer.");
                tryAddBlock(newBlock);
            }
        } catch (Exception e) {
            System.out.println("Erro ao lidar com cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private void tryAddBlock(Block block) {
        List<Block> localChain = blockchain.getBlocks();
        if (block.getIndex() == localChain.size()) {
            boolean ok = blockchain.addBlock(block.getTransactions());
            if (ok) System.out.println("Added new block from peer!");
        } else if (block.getIndex() > localChain.size()) {
            List<Block> peerChain = requestChainFromPeer();
            if (peerChain != null && peerChain.size() > localChain.size()) {
                blockchain.replaceChain(peerChain);
                System.out.println("Replaced local chain with peer's longer chain!");
            }
        }
    }

    private List<Block> requestChainFromPeer() {
        for (InetSocketAddress peer : peers) {
            int attempts = 0;
            while (attempts < 3) {
                try (Socket socket = new Socket(peer.getHostName(), peer.getPort());
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                    out.writeObject("GET_BLOCKCHAIN");
                    out.flush();
                    Object response = in.readObject();
                    if (response instanceof List) {
                        System.out.println("Blockchain recebida do peer " + peer);
                        return (List<Block>) response;
                    }
                } catch (Exception e) {
                    attempts++;
                    System.out.println("Erro ao pedir blockchain ao peer " + peer + " tentativa " + attempts);
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }
        }
        return null;
    }

    public void broadcastBlock(Block block) {
        for (InetSocketAddress peer : peers) {
            int attempts = 0;
            boolean sent = false;
            while (attempts < 3 && !sent) {
                try (Socket socket = new Socket(peer.getHostName(), peer.getPort());
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    out.writeObject("NEW_BLOCK");
                    out.writeObject(block);
                    out.flush();
                    sent = true;
                    System.out.println("Bloco enviado ao peer " + peer);
                } catch (Exception e) {
                    attempts++;
                    System.out.println("Erro ao enviar bloco ao peer " + peer + " tentativa " + attempts + ": " + e.getMessage());
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }
            if (!sent) {
                System.out.println("Não foi possível enviar bloco ao peer " + peer + " após 3 tentativas.");
            }
        }
    }
}