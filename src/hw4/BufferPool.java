/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 11/16
 */
package hw4;

import java.io.*;
import java.util.*;

import hw1.*;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool which check that the transaction has the appropriate
 * locks to read/write the page.
 */
public class BufferPool {

    // [HeapPage: [Permission Type: Set of Transaction ID]]
    private LinkedHashMap<HeapPage, Map<Permissions, Set<Integer>>> pool;

    // [Transaction ID: [Permission Type: Set of HeapPage]]
    private HashMap<Integer, Map<Permissions, Set<HeapPage>>> tidToPages;

    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;

    private int numPages;

    /**q
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        // your code here
        this.numPages = numPages;
        pool = new LinkedHashMap<>();
        tidToPages = new HashMap<>();
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param tableId the ID of the table with the requested page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public HeapPage getPage(int tid, int tableId, int pid, Permissions perm)
        throws Exception {
        // your code here
        HeapFile hf = Database.getCatalog().getDbFile(tableId);
        HeapPage hp = getHeapPage(tableId, pid);
        if(hp == null) {
            // not represented
            if(pool.size() == numPages) {
                // Buffer pool is full
                evictPage();
            }
            // Initialization for the adding
            hp = hf.readPage(pid);
            pool.putIfAbsent(hp, new HashMap<>());
            Map<Permissions, Set<Integer>> hp_locks = pool.get(hp);
            hp_locks.putIfAbsent(Permissions.READ_ONLY, new HashSet<>());
            hp_locks.putIfAbsent(Permissions.READ_WRITE, new HashSet<>());
            tidToPages.putIfAbsent(tid, new HashMap<>());
            Map<Permissions, Set<HeapPage>> tid_locks = tidToPages.get(tid);
            tid_locks.putIfAbsent(Permissions.READ_ONLY, new HashSet<>());
            tid_locks.putIfAbsent(Permissions.READ_WRITE, new HashSet<>());
        }
        acquireLocks(tid, hp, perm);
        return hp;
    }

    // acquire the specific lock
    private void acquireLocks(int tid, HeapPage hp, Permissions perm) throws InterruptedException, IOException {
        if(!canReadOrWrite(tid, hp, perm)) {
            // Check Dead Lock
            Thread.sleep(500);
            if (!canReadOrWrite(tid, hp, perm)) {
                transactionComplete(tid, false);
                return;
            }
        }
        Map<Permissions, Set<Integer>> hp_locks = pool.get(hp);
        hp_locks.get(perm).add(tid);
        tidToPages.putIfAbsent(tid, new HashMap<>());
        Map<Permissions, Set<HeapPage>> tid_locks = tidToPages.get(tid);
        tid_locks.putIfAbsent(Permissions.READ_ONLY, new HashSet<>());
        tid_locks.putIfAbsent(Permissions.READ_WRITE, new HashSet<>());
        tid_locks.get(perm).add(hp);
        if(Permissions.READ_ONLY.equals(perm)) {
            hp_locks.get(Permissions.READ_WRITE).remove(tid);
            tid_locks.get(Permissions.READ_WRITE).remove(hp);
        }
        else if(Permissions.READ_WRITE.equals(perm)) {
            hp_locks.get(Permissions.READ_ONLY).remove(tid);
            tid_locks.get(Permissions.READ_ONLY).remove(hp);
        }
    }

    // Return true if the transaction is valid to have permission on a specific page
    private boolean canReadOrWrite(int tid, HeapPage hp, Permissions perm) {
        if(!pool.containsKey(hp))
            return false;
        Set<Integer> readLocks = pool.get(hp).get(Permissions.READ_ONLY);
        Set<Integer> writeLocks = pool.get(hp).get(Permissions.READ_WRITE);
        // Check if the requests are from the same transaction
        if((readLocks != null && readLocks.contains(tid)) ||
                (writeLocks != null && writeLocks.contains(tid))) {
            return true;
        }
        if(Permissions.READ_ONLY.equals(perm)) {
            // Check whether it can have read locks
            return writeLocks == null || writeLocks.size() == 0;
        }
        // check whether it can have write locks
        return (readLocks == null || readLocks.size() == 0) && (writeLocks == null || writeLocks.size() == 0);
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param tableId the ID of the table containing the page to unlock
     * @param pid the ID of the page to unlock
     */
    public void releasePage(int tid, int tableId, int pid) {
        // your code here
        HeapPage hp = getHeapPage(tableId, pid);
        Map<Permissions, Set<HeapPage>> tid_locks = tidToPages.get(tid);
        if(tid_locks.get(Permissions.READ_ONLY).contains(hp)) {
            tid_locks.get(Permissions.READ_ONLY).remove(hp);
            pool.get(hp).get(Permissions.READ_ONLY).remove(tid);
        }
        if(tid_locks.get(Permissions.READ_WRITE).contains(hp)) {
            tid_locks.get(Permissions.READ_WRITE).remove(hp);
            pool.get(hp).get(Permissions.READ_WRITE).remove(tid);
        }
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public boolean holdsLock(int tid, int tableId, int pid) {
        // your code here
        HeapPage hp = getHeapPage(tableId, pid);
        Map<Permissions, Set<Integer>> hp_locks = pool.get(hp);
        if(hp_locks == null)
            return false;
        return hp_locks.get(Permissions.READ_ONLY).contains(tid) ||
                hp_locks.get(Permissions.READ_WRITE).contains(tid);
    }

    // Retrieve heap page from the buffer pool
    private HeapPage getHeapPage(int tableId, int pid) {
        for(HeapPage hp: pool.keySet()) {
            if(hp.getId() == pid && hp.getTableId() == tableId)
                return hp;
        }
        return null;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction. If the transaction wishes to commit, write
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(int tid, boolean commit)
        throws IOException {
        // your code here
        if(commit) {
            // Write
            Set<HeapPage> set = tidToPages.get(tid).get(Permissions.READ_WRITE);
            for(HeapPage hp: set) {
                if(hp.isDirty())
                    flushPage(hp.getTableId(), hp.getId());
            }
        }
        // release all the locks
        Set<HeapPage> reads = tidToPages.get(tid).get(Permissions.READ_ONLY);
        Set<HeapPage> writes = tidToPages.get(tid).get(Permissions.READ_WRITE);
        for(HeapPage hp: reads) {
            pool.get(hp).get(Permissions.READ_ONLY).remove(tid);
        }
        for(HeapPage hp: writes) {
            pool.get(hp).get(Permissions.READ_WRITE).remove(tid);
        }
        reads.clear();
        writes.clear();
    }

    /**
     * Add a tuple to the specified table behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to. May block if the lock cannot 
     * be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public void insertTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
        HeapFile hf = Database.getCatalog().getDbFile(tableId);
        HeapPage hp = hf.getFirstAvailablePage();
        hp = getHeapPage(tableId, hp.getId());
        // Check write lock
        Set<HeapPage> writeLocks = tidToPages.get(tid).get(Permissions.READ_WRITE);
        if(hp == null || !writeLocks.contains(hp)) {
            // Not Permitted
            throw new Exception("You don't have access");
        }
        hp.addTuple(t);
        hp.setDirty(true);
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from. May block if
     * the lock cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty.
     *
     * @param tid the transaction adding the tuple.
     * @param tableId the ID of the table that contains the tuple to be deleted
     * @param t the tuple to add
     */
    public void deleteTuple(int tid, int tableId, Tuple t)
        throws Exception {
        // your code here
        HeapPage hp = getHeapPage(tableId, t.getPid());
        // Check write lock
        Set<HeapPage> writeLocks = tidToPages.get(tid).get(Permissions.READ_WRITE);
        if(hp == null || !writeLocks.contains(hp)) {
            // Not Permitted
            throw new Exception("You don't have access");
        }
        hp.deleteTuple(t);
        hp.setDirty(true);
    }

    private synchronized void flushPage(int tableId, int pid) throws IOException {
        // your code here
        HeapFile hf = Database.getCatalog().getDbFile(tableId);
        for(HeapPage hp: pool.keySet()) {
            if(hp.getTableId() == tableId && hp.getId() == pid) {
                hf.writePage(hp);
                hp.setDirty(false);
                break;
            }
        }
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized void evictPage() throws Exception {
        // your code here
        HeapPage target = null;
        for(HeapPage hp: pool.keySet()) {
            if(!hp.isDirty()) {
                target = hp;
                break;
            }
        }
        if(target == null)
            throw new Exception("Full");
        Set<Integer> reads = pool.get(target).get(Permissions.READ_ONLY);
        Set<Integer> writes = pool.get(target).get(Permissions.READ_WRITE);
        for(int i: reads) {
            tidToPages.get(i).get(Permissions.READ_ONLY).remove(target);
        }
        for(int i: writes) {
            tidToPages.get(i).get(Permissions.READ_WRITE).remove(target);
        }
        reads.clear();
        writes.clear();
    }

}
