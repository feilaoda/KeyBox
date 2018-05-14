package com.bitcode.agent;

import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BlockChain implements Serializable {

//    private List<Block> blocks = new LinkedList<>();

    private Object lock = new Object();

    private int chainHeight;

    private Block chainHead;

    private String lastBlockHash;

    RocksDatabase db;

    protected final ReentrantLock locking = new ReentrantLock();

    class BlockElement {
        Block block;
        int index;

        public BlockElement(Block block, int index) {
            this.block = block;
            this.index = index;
        }

        public Block getBlock() {
            return block;
        }

        public void setBlock(Block block) {
            this.block = block;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    public BlockChain(String name, Block root) {

        chainHeight = 0;
        chainHead  = root;
        lastBlockHash = root.getHash();
        db = RocksDatabase.getInstance(name+".db");
        add(root);
    }

    public class BlockChainIterator {

        private String currentBlockHash;

        private BlockChainIterator(String currentBlockHash) {
            this.currentBlockHash = currentBlockHash;
        }

        /**
         * 是否有下一个区块
         *
         * @return
         */
        public boolean hashNext() {
            if (StringUtils.isEmpty(currentBlockHash)) {
                return false;
            }
            Block lastBlock = db.getBlock(currentBlockHash);
            if (lastBlock == null) {
                return false;
            }
            // 创世区块直接放行
            if (lastBlock.getPreviousHash().length() == 0) {
                return true;
            }
            return db.getBlock(lastBlock.getPreviousHash()) != null;
        }


        /**
         * 返回区块
         *
         * @return
         */
        public Block next() {
            Block currentBlock = db.getBlock(currentBlockHash);
            if (currentBlock != null) {
                this.currentBlockHash = currentBlock.getPreviousHash();
                return currentBlock;
            }
            return null;
        }
    }

    public BlockChainIterator getBlockChainIterator() {
        return new BlockChainIterator(lastBlockHash);
    }

    public boolean addWithLock(Block block) {
        locking.lock();
        try {
            return add(block, false);
        } finally {
            locking.unlock();
        }
    }

    public void add(Block block) {
            db.putLastBlockHash(block.getHash());
            db.putBlock(block);
            this.lastBlockHash = block.getHash();
    }


    public boolean add(Block block, boolean force) {
//        synchronized (lock) {
//            blocks.add(block);
//        }
//        add(Collections.singletonList(block));



        int height = chainHeight;
        int blockHeight = block.getIndex();

        if(!force) {
            if (blockHeight < chainHeight) {
                return false;
            }

            if (block.equals(chainHead)) {
                return false;
            }
        }

        int willHeight;

        BlockElement element = getBlockInCurrentScope(block.getPreviousHash());

        Block prevBlock = null;

        if (element != null) {
            prevBlock = element.getBlock();
        }

        if (prevBlock != null) {
            willHeight = prevBlock.getIndex() + 1;
        } else {
            willHeight = -1;
        }

        if (prevBlock == null) {
            //未能找到，先放一边
        } else {
            Block headBlock = chainHead;
            if (prevBlock.equals(headBlock)) {
                setChainHead(block);
//                blocks.add(block);
            } else {
                int idx = element.getIndex();
//                blocks.subList(idx, blocks.size()).clear();
                setChainHead(block);
//                blocks.add(block);
            }
        }


        return true;

    }

    private void setChainHead(Block newBlock) {
        chainHead = newBlock;
        chainHeight = newBlock.getIndex();
    }

    private BlockElement getBlockInCurrentScope(String hash) {
        int index = 0;
        for (Block old : getBlocks()) {
            if (old.hash.equals(hash)) {
                return new BlockElement(old, index);
            }
            index++;
        }
        return null;
    }


    public void addBlocks(List<Block> blockList) {
        locking.lock();
        try {
            if (blockList.size() <= 0) return;
            for (Block block : blockList) {
                add(block);
            }
        } finally {
            locking.unlock();
        }
    }

    public void addx(List<Block> blockList) {
        synchronized (lock) {
            if (blockList.size() <= 0) return;
            Block willLatestBlock = blockList.get(blockList.size() - 1);
            for (Block block : blockList) {
                int find = 0;
                List<Block> blocks = getBlocks();
                for (Block old : blocks) {
//                    if (old.getHash().equals(block.getHash())) {
                    if (old.getIndex() == block.getIndex()) {
                        if (old.getHash().equals(block.getHash()) && old.getPreviousHash().equals(block.getPreviousHash())) {
                            find = 1;
                        } else {
                            find = 2;
                        }
                        break;
                    }

                }
                if (find == 1) {
                    continue;
                } else if (find == 2) {
                    //相同高度，但hash不一致，
                    System.out.println("======!!!!!===== block height not unique" + block.getCreator() + ", index=" + block.getIndex() + " , " + block.getHash());
                    break;
                } else {
                    if (blocks.size() > 0) {
                        Block latest = blocks.get(blocks.size() - 1);
                        if (latest.getIndex() + 1 != block.getIndex()) {
                            System.out.println("!!!!! " + latest.getCreator() + " latest height != block.index" + latest.getIndex() + " != " + block.getIndex());
                        }
                        if (!latest.getHash().equals(block.getPreviousHash())) {
                            System.out.println("!!!!===== latest hash != block previous hash, " + latest.getHash() + " != " + block.getPreviousHash());

                        }

                        if (latest.getIndex() + 1 != block.getIndex() || !latest.getHash().equals(block.getPreviousHash())) {
                            //wrong
                            if (latest.getIndex() + 1 < willLatestBlock.getIndex()) {
                                //这一个latest需要去掉，或重新校验本地block TODO
                            }
                        }
                    }
                    blocks.add(block);
                }
            }
        }
    }

    public int findIndex(int from) {
        List<Block> dbBlocks = getBlocks();
        for (int i = 0; i < dbBlocks.size(); i++) {
            if (dbBlocks.get(i).getIndex() == from) {
                return i;
            }
        }
        return -1;
    }

    public Block findBlock(int from) {
        List<Block> dbBlocks = getBlocks();
        for (int i = 0; i < dbBlocks.size(); i++) {
            Block block = dbBlocks.get(i);
            if (block.getIndex() == from) {
                return block;
            }
        }
        return null;
    }

    public List<Block> split(int from) {
        synchronized (lock) {
            int latest = getLatestBlock().getIndex();
            int fromIndex = findIndex(from);
            if (fromIndex < 0) {
                return new LinkedList<>();
            }
            List<Block> myblocks = new LinkedList<>();
            List<Block> dbBlocks = getBlocks();
            for (int i = fromIndex; i < dbBlocks.size(); i++) {
                myblocks.add(new Block(dbBlocks.get(i)));
            }

            return myblocks;
        }
    }

    public boolean isEmpty() {
        return db.isEmpty();
    }

    public Block getLatestBlock() {
        return db.getBlock(lastBlockHash);
    }

    public int size() {
        return db.getBlocks().size();
    }

    public List<Block> getBlocks() {

        return db.getBlocks();
    }

}
