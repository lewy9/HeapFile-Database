/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 10/30
 */
package hw3;

import hw1.Field;
import hw1.RelationalOperator;

import java.util.ArrayList;
import java.util.Iterator;

public class LeafNode implements Node {

	private int degree;
	private ArrayList<Entry> entries;
	private InnerNode parent;

	public LeafNode(int degree) {
		//your code here
		this.degree = degree;
		this.entries = new ArrayList<>();
	}
	
	public ArrayList<Entry> getEntries() {
		//your code here
		return entries;
	}

	public int getDegree() {
		//your code here
		return degree;
	}
	
	public boolean isLeafNode() {
		return true;
	}

	public boolean contains(Field f) {
		for(Entry entry: entries) {
			if(entry.getField().equals(f))
				return true;
		}
		return false;
	}

	public boolean isFull() {
		return entries.size() >= degree;
	}

	// Add only when it has available room
	public void add(Entry e) {
		int i = 0;
		while(i < entries.size() && entries.get(i).getField().compare(RelationalOperator.LT, e.getField()))
			i++;
		entries.add(i, e);
	}

	// Get max value for current node
	public Field getMax() {
		return entries.get(entries.size() - 1).getField();
	}

	public InnerNode getParent() {
		return this.parent;
	}

	public void setParent(InnerNode parent) {
		this.parent = parent;
	}

	public void split(int innerDegree) {
		LeafNode newNode = new LeafNode(degree);
		ArrayList<Entry> curr = new ArrayList<>();
		int size = entries.size();
		int start = size % 2 == 0 ? size / 2 : size / 2 + 1;
		for(int i = 0; i < size; i++) {
			if(i >= start)
				newNode.getEntries().add(entries.get(i));
			else
				curr.add(entries.get(i));
		}
		entries = curr;
		if(parent == null) {
			parent = new InnerNode(innerDegree);
			parent.add(this);
		}
		newNode.setParent(parent);
		parent.add(newNode);
	}

	// Return whether it needs to update parent
	public void delete(Entry e) {
		boolean update = false;
		if(parent != null && parent.containsKey(e.getField())) {
			// Whether it Requires to update parent keys
			update = true;
		}
		Iterator<Entry> itr = entries.iterator();
		while(itr.hasNext()) {
			if(itr.next().getField().equals(e.getField())) {
				itr.remove();
				break;
			}
		}
		if(update && parent != null) {
			// Need to update keys
			parent.setKeys();
		}
	}

	public boolean canDirectRemove() {
		int n = degree;
		if(n % 2 == 1) n += 1;
		return entries.size() > n / 2;
	}

	// Return true if the current node successfully borrows node from siblings
	public boolean canBorrowFromSibling() {
		// check left sibling
		LeafNode left = getSibling(0);
		if(left != null && left.canDirectRemove()) {
			ArrayList<Entry> list = left.getEntries();
			Entry e = list.get(list.size() - 1);
			this.add(e);
			list.remove(list.size() - 1);
			return true;
		}
		// Check right sibling
		LeafNode right = getSibling(1);
		if(right != null && right.canDirectRemove()) {
			ArrayList<Entry> list = right.getEntries();
			Entry e = list.get(0);
			this.add(e);
			list.remove(0);
			return true;
		}
		return false;
	}

	// Input param: position : {0: left}; {1: right}
	public LeafNode getSibling(int position) {
		if(parent == null)
			return null;

		int i = parent.getChildren().indexOf(this);
		if(i > 0 && position == 0)
			return (LeafNode) (parent.getChildren().get(i - 1));
		else if(i < parent.getChildren().size() - 1 && position == 1)
			return (LeafNode)(parent.getChildren().get(i + 1));

		return null;
	}

	public void merge(Field f) {
		if(parent == null)
			return;

		// Try merging left
		LeafNode left = getSibling(0);
		if(!mergeHelper(left, f)) {
			// Try merging right
			LeafNode right = getSibling(1);
			mergeHelper(right, f);
		}
	}

	// Return true if success
	private boolean mergeHelper(LeafNode node, Field f) {
		if(node != null) {
			Iterator<Entry> itr = entries.iterator();
			while(itr.hasNext()) {
				if(itr.next().getField().equals(f)) {
					itr.remove();
					break;
				}
			}
			for(Entry e: entries) {
				node.add(e);
			}
			parent.getChildren().remove(this);
			parent.setKeys();
			setParent(null);
			return true;
		}
		return false;
	}

	// For test purpose
	public ArrayList<Field> toField() {
		ArrayList<Field> list = new ArrayList<>();
		for(Entry e: entries) {
			list.add(e.getField());
		}
		return list;
	}

}