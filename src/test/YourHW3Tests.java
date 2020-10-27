package test;

import static org.junit.Assert.*;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw3.*;
import org.junit.Test;

import java.util.ArrayList;

public class YourHW3Tests {

	@Test
	public void testInsert() {
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));

		Node root = bt.getRoot();
		assertTrue(!root.isLeafNode());

		InnerNode in = (InnerNode)root;

		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();

		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(9)));

		//grab left and right children from root
		Node l = c.get(0);
		Node r = c.get(1);
		assertTrue(l.isLeafNode());
		assertTrue(r.isLeafNode());

		LeafNode ll = (LeafNode)l;
		LeafNode lr = (LeafNode)r;

		ArrayList<Entry> ell = ll.getEntries();

		assertTrue(ell.get(0).getField().equals(new IntField(4)));
		assertTrue(ell.get(1).getField().equals(new IntField(9)));

		ArrayList<Entry> elm = lr.getEntries();

		assertTrue(elm.get(0).getField().equals(new IntField(12)));

	}


	@Test
	public void testSearch() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));

		//these values should exist
		assertTrue(bt.search(new IntField(12)) != null);
		assertTrue(bt.search(new IntField(9)) != null);
		assertTrue(bt.search(new IntField(4)) != null);

		//these values should not exist
		assertTrue(bt.search(new IntField(8)) == null);
		assertTrue(bt.search(new IntField(11)) == null);
		assertTrue(bt.search(new IntField(5)) == null);

	}

	@Test
	public void testDelete() {
		//Create a tree, then delete some values

		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		bt.delete(new Entry(new IntField(7), 0));
		bt.delete(new Entry(new IntField(3), 0));
		bt.delete(new Entry(new IntField(4), 0));
		bt.delete(new Entry(new IntField(10), 0));
		bt.delete(new Entry(new IntField(2), 0));
		bt.delete(new Entry(new IntField(6), 0));

		//verify root properties
		Node root = bt.getRoot();

		assertTrue(!root.isLeafNode());

		InnerNode in = (InnerNode) root;

		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();

		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(1)));

		//grab left, middle and right children from root
		Node l = c.get(0);
		Node m = c.get(1);
		Node r = c.get(2);

		assertTrue(l.isLeafNode());
		assertTrue(r.isLeafNode());

		LeafNode ll = (LeafNode) l;
		LeafNode ml = (LeafNode) m;
		LeafNode rl = (LeafNode) r;

		//check values in left node
		ArrayList<Entry> ell = ll.getEntries();
		assertTrue(ell.get(0).getField().equals(new IntField(1)));

		//check values in middle node
		ArrayList<Entry> eml = ml.getEntries();
		assertTrue(eml.get(0).getField().equals(new IntField(9)));

		//check values in right node
		ArrayList<Entry> erl = rl.getEntries();
		assertTrue(erl.get(0).getField().equals(new IntField(12)));
	}

}
