package hw3;

import java.util.ArrayList;

import hw1.Field;

public class InnerNode implements Node {

	private ArrayList<Field> keys;
	private ArrayList<Node> children;
	private int degree;

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
	
	public ArrayList<Node> getChildren() {
		//your code here
		return children;
	}

	public int getDegree() {
		//your code here
		return degree;
	}
	
	public boolean isLeafNode() {
		return false;
	}

}