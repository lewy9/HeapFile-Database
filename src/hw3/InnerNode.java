/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 10/30
 */
package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;

public class InnerNode implements Node {

	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	private int degree;
	private InnerNode parent;

	public InnerNode(int degree) {
		//your code here
		this.degree = degree;
		this.keys = new ArrayList<>();
		this.children = new ArrayList<>();
	}
	
	public ArrayList<Field> getKeys() {
		//your code here
		return keys;
	}

	public boolean containsKey(Field f) {
		for(Field key: keys) {
			if(f.equals(key))
				return true;
		}
		return false;
	}
	
	public ArrayList<Node> getChildren() {
		//your code here
		return children;
	}

	public void add(Node child) {
		// Compute the index in the ArrayList to add child
		int i = 0;
		while(i < children.size() && children.get(i).getMax().compare(RelationalOperator.LT, child.getMax()))
			i++;
		children.add(i, child);

		// Refresh InnerNode keys
		setKeys();
	}

	public void setKeys() {
		// Add each child's largest value except the last child;
		keys.clear();
		for(int j = 0; j < children.size() - 1; j++) {
			keys.add(children.get(j).getMax());
		}
	}

	public int getDegree() {
		//your code here
		return degree;
	}

	// Check Whether InnerNode is full
	public boolean isFull() {
		return children.size() >= degree;
	}
	
	public boolean isLeafNode() {
		return false;
	}

	public InnerNode getParent() {
		return this.parent;
	}

	public void setParent(InnerNode parent) {
		this.parent = parent;
	}

	// check whether it can be removed or borrowed
	public boolean canDirectRemove() {
		int n = degree;
		if(n % 2 == 1) n += 1;
		return children.size() > n / 2;
	}

	// Check whether it follows degree property
	public boolean isValid() {
		int n = degree;
		if(n % 2 == 1) n += 1;
		return children.size() >= n / 2;
	}

	public void split(int innerDegree) {
		InnerNode newNode = new InnerNode(innerDegree);
		ArrayList<Field> currKeys = new ArrayList<>();
		ArrayList<Node> currNodes = new ArrayList<>();
		int size = keys.size();
		int start = size % 2 == 0 ? size / 2 : size / 2 + 1;
		for(int i = 0; i < size; i++) {
			if(i >= start) {
				newNode.getKeys().add(keys.get(i));
			}
			else {
				currKeys.add(keys.get(i));
			}
		}
		int size2 = children.size();
		int start2 = size2 % 2 == 0 ? size2 / 2 : size2 / 2 + 1;
		for(int i = 0; i < size2; i++) {
			if(i >= start2) {
				children.get(i).setParent(newNode);
				newNode.getChildren().add(children.get(i));
			}
			else {
				currNodes.add(children.get(i));
			}
		}
		keys = currKeys;
		children = currNodes;
		if(parent == null) {
			parent = new InnerNode(innerDegree);
			parent.add(this);
		}
		newNode.setParent(parent);
		parent.add(newNode);
		// Update current keys since some children were removed
		setKeys();
	}

	// Get max value for current node
	public Field getMax() {
		return keys.get(keys.size() - 1);
	}

	public void merge(Field f) {
		if(parent == null)
			return;

		// Try merging left
		InnerNode left = getSibling(0);
		InnerNode right = getSibling(1);
		if(!mergeHelper(left)) {
			// Try merging right
			mergeHelper(right);
		}
	}

	// Return true if success
	private boolean mergeHelper(InnerNode node) {
		if(node != null) {
			for(Node n: children)
				node.add(n);
			parent.getChildren().remove(this);
			parent.setKeys();
			setParent(null);
			return true;
		}
		return false;
	}

	// Input param: position : {0: left}; {1: right}
	public InnerNode getSibling(int position) {
		if(parent == null)
			return null;
		int i = parent.getChildren().indexOf(this);
		if(i > 0 && position == 0)
			return (InnerNode) (parent.getChildren().get(i - 1));
		else if(i < parent.getChildren().size() - 1 && position == 1)
			return (InnerNode) (parent.getChildren().get(i + 1));

		return null;
	}

	// Return true if the current node successfully borrows node from siblings
	public boolean canBorrowFromSibling() {
		// check left sibling
		InnerNode left = getSibling(0);
		if(left != null && left.canDirectRemove()) {
			ArrayList<Node> list = left.getChildren();
			Node n = list.get(list.size() - 1);
			this.add(n);
			list.remove(list.size() - 1);
			return true;
		}
		// check right sibling
		InnerNode right = getSibling(1);
		if(right != null && right.canDirectRemove()) {
			ArrayList<Node> list = right.getChildren();
			Node n = list.get(0);
			this.add(n);
			list.remove(0);
			return true;
		}
		return false;
	}
}