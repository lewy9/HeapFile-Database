package hw3;

import hw1.Field;

public interface Node {
	
	
	public int getDegree();
	public boolean isLeafNode();
	void split(int degree);
	boolean isFull();
	Field getMax();
	InnerNode getParent();
	boolean canDirectRemove();
	void merge(Field f);
	void setParent(InnerNode parent);
	boolean canBorrowFromSibling();
}
