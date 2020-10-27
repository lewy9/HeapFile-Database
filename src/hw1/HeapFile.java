/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 09/23
 */
package hw1;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	public static final int PAGE_SIZE = 4096;
	private File file;
	private TupleDesc td;
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param type type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		this.file = f;
		this.td = type;
	}
	
	public File getFile() {
		return file;
	}
	
	public TupleDesc getTupleDesc() {
		return td;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 */
	public HeapPage readPage(int id) {
		File file = getFile();
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "rw");
			byte[] data = new byte[PAGE_SIZE];
			// Set current pointer
			raf.seek(PAGE_SIZE * id);
			raf.read(data);
			int tableId = getId();
			raf.close();
			return new HeapPage(id, data, tableId);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		//  AKA table id
		return getFile().hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) {
		int pageId = p.getId();
		byte[] data = p.getPageData();
		File file = getFile();
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "rw");
			// Set current pointer
			raf.seek(PAGE_SIZE * pageId);
			raf.write(data);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 */
	public HeapPage addTuple(Tuple t) {
		// Get the number of pages
		int num = getNumPages();

		// has available slot
		for(int i = 0; i < num; i++) {
			HeapPage hp = readPage(i);
			if(hp.hasAvailableSlot()) {
				try {
					hp.addTuple(t);
					writePage(hp);
					return hp;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// No available slot, create a new page
		try {
			HeapPage hp = new HeapPage(num, new byte[PAGE_SIZE], getId());
			hp.addTuple(t);
			writePage(hp);
			return hp;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public HeapPage getFirstAvailablePage() {
		// Get the number of pages
		int num = getNumPages();

		// has available slot
		for(int i = 0; i < num; i++) {
			HeapPage hp = readPage(i);
			if(hp.hasAvailableSlot()) {
				return hp;
			}
		}
		return null;
	}
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 */
	public void deleteTuple(Tuple t){
		int slotId = t.getId();
		int pageId = t.getPid();
		HeapPage hp = readPage(pageId);
		try {
			hp.deleteTuple(t);
			writePage(hp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		ArrayList<Tuple> list = new ArrayList<>();
		// Get the number of pages
		int num = getNumPages();

		for(int i = 0; i < num; i++) {
			HeapPage hp = readPage(i);
			Iterator<Tuple> itr = hp.iterator();
			while(itr.hasNext()) {
				list.add(itr.next());
			}
		}
		return list;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		RandomAccessFile raf;
		int num;
		try {
			raf = new RandomAccessFile(getFile(), "rw");
			num = raf.length() % PAGE_SIZE == 0 ? (int) (raf.length() / PAGE_SIZE) : (int) (raf.length() / PAGE_SIZE + 1);
			raf.close();
			return num;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return -100;
	}
}
