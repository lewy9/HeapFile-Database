package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw4.BufferPool;
import hw4.Permissions;

public class YourHW4Tests {

	private Catalog c;
	private BufferPool bp;
	private HeapFile hf;
	private TupleDesc td;
	private int tid;
	private int tid2;
	
	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		Database.reset();
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		c.loadSchema("testfiles/test2.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		
		Database.resetBufferPool(BufferPool.DEFAULT_PAGES);

		bp = Database.getBufferPool();
		
		
		tid = c.getTableId("test");
		tid2 = c.getTableId("test2");
	}
	
	@Test
	public void testReleasePage() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		bp.releasePage(0, tid, 0);
		bp.getPage(1, tid, 0, Permissions.READ_WRITE);
		try {
			bp.getPage(0, tid, 0, Permissions.READ_WRITE);
		}
		catch (Exception e) {
			return;
		}
		assertTrue(true);
	}

	@Test
	public void testMultipleReads() throws Exception {
		bp.getPage(0, tid, 0, Permissions.READ_ONLY);
		bp.getPage(1, tid, 0, Permissions.READ_ONLY);
		bp.getPage(2, tid, 0, Permissions.READ_ONLY);
		assertFalse(!bp.holdsLock(0, tid, 0) && !bp.holdsLock(1, tid, 0) && !bp.holdsLock(2, tid, 0));
	}

	@Test
	public void testEvict() {
		for(int i = 0; i <= 50; i++) {
			try {
				bp.getPage(0, tid2, i, Permissions.READ_WRITE);
			} catch (Exception e) {
				fail("A dirty page should be evicted.");
			}
		}
		assertTrue(true);
	}
}
