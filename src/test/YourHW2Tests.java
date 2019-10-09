package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import hw1.*;
import hw2.Relation;
import org.junit.Before;
import org.junit.Test;

public class YourHW2Tests {

	private HeapFile testhf;
	private TupleDesc testtd;
	private HeapFile ahf;
	private TupleDesc atd;
	private Catalog c;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File("testfiles/A.dat.bak").toPath(), new File("testfiles/A.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		testtd = c.getTupleDesc(tableId);
		testhf = c.getDbFile(tableId);

		c = Database.getCatalog();
		c.loadSchema("testfiles/A.txt");
		
		tableId = c.getTableId("A");
		atd = c.getTupleDesc(tableId);
		ahf = c.getDbFile(tableId);
	}
	
	@Test
	public void testGetTupDesc() {
		// Test Relation.getDesc() when the returned Relation contains no tuples
		Relation rl = new Relation(ahf.getAllTuples(), atd);
		rl = rl.select(0, RelationalOperator.EQ, new IntField(100));

		assertTrue("It is an empty set", rl.getTuples().size() == 0);
		assertTrue("The empty set should still contain the proper TupleDesc" ,rl.getDesc().equals(atd));
	}

	@Test
	public void testProjectionWhenInputIsEmpty() {
		// Test Relation.project() when the input field is empty
		Relation rl = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> fields = new ArrayList<>();
		rl = rl.project(fields);

		assertTrue("The size of TupleDesc should not be zero", rl.getDesc().getSize() == 0);
		assertTrue("The number of tuples should be zero", rl.getTuples().size() == 0);
		assertTrue("Projection should retain the column names", rl.getDesc().numFields() == 0);
	}

	@Test
	public void testNoMatchJoin() {
		// Test then return relation when there is no match
		Relation tr = new Relation(testhf.getAllTuples(), testtd);
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		try {
			tr = tr.join(ar, 1, 0);
			fail("It should have no Match");
		} catch (Exception e) {
			// Success
		}
	}
}
