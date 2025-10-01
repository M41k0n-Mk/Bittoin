package me.m41k0n;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {

    private final List<Block> chain = new ArrayList<>();
    private final int difficulty;

    public Blockchain(int difficulty) {
        this.difficulty = difficulty;
        chain.add(new Block(0, "Genesis Block", "0", difficulty));
    }

    public Block getLatestBlock() {
        return chain.getLast();
    }

    public void addBlock(String data) {
        Block previous = getLatestBlock();
        Block newBlock = new Block(previous.getIndex() + 1, data, previous.getHash(), difficulty);
        chain.add(newBlock);
    }

    public boolean isValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);
            if (!current.getPreviousHash().equals(previous.getHash())) return false;
            String expectedHash = Block.calculateHash(
                    current.getIndex(), current.getTimestamp(), current.getData(), current.getPreviousHash(), current.getNonce()
            );
            if (!current.getHash().equals(expectedHash)) return false;
        }
        return true;
    }

    public List<Block> getBlocks() {
        return List.copyOf(chain);
    }
}