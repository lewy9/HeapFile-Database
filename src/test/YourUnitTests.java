// Weichen Zhu, Hongchuan Shi
package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import hw1.*;
import org.junit.Before;
import org.junit.Test;

public class YourUnitTests {
	
	private HeapFile hf;
	private TupleDesc td;
	private Catalog c;
	private HeapPage hp;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		hp = hf.readPage(0);
	}

	@Test
	public void testTupleDescGetSize() {
		// Include some Strings
		Type[] t1 = {Type.STRING, Type.STRING, Type.STRING};
		Type[] t2 = {Type.INT, Type.STRING, Type.INT};
		String[] n1 = {"", "", ""};
		String[] n2 = {"", "", ""};
		TupleDesc td1 = new TupleDesc(t1, n1);
		TupleDesc td2 = new TupleDesc(t2, n2);

		assertTrue(td1.getSize() == 387);
		assertTrue(td2.getSize() == 137);
	}

	@Test
	public void testTupleDescToString() {
		Type[] t1 = {Type.STRING, Type.STRING, Type.STRING};
		Type[] t2 = {Type.INT, Type.STRING, Type.INT};
		String[] n1 = {"10", "20", "30"};
		String[] n2 = {"15", "25", "35"};
		TupleDesc td1 = new TupleDesc(t1, n1);
		TupleDesc td2 = new TupleDesc(t2, n2);

		String str1 = td1.toString();
		String str2 = td2.toString();
		assertTrue(str1.equals("STRING(10), STRING(20), STRING(30)"));
		assertTrue(str2.equals("INT(15), STRING(25), INT(35)"));
	}

	@Test
	public void testGetAllTuples() {
		// Original data has 1 tuple
		assertTrue(hf.getAllTuples().size() == 1);
	}

	@Test
	public void testGetNumPagesWhenPagesIncrease() {
		// MAX_Page_Size = 4096
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, (byte)2, (byte)18}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 0x68;
		s[2] = 0x69;
		t.setField(1, new StringField(s));

		try {
			int i = 0;
			while(i < 40) {
				hf.addTuple(t);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to add tuples");
		}
		assertTrue(hf.getNumPages() == 2);
	}

}
