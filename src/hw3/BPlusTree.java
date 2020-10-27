/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 10/30
 */

package hw3;

import hw1.Field;
import hw1.RelationalOperator;

import java.util.ArrayList;

public class BPlusTree {

    private Node root;
    private int pInner;
    private int pLeaf;
    
    public BPlusTree(int pInner, int pLeaf) {
    	//your code here
        this.pInner = pInner;
        this.pLeaf = pLeaf;
    }
    
    public LeafNode search(Field f) {
    	//your code here
        Node curr = root;
    	LeafNode res = searchHelper(curr, f);
    	if(res == null || !res.contains(f))
    	    return null;
    	return res;
    }

    private LeafNode searchHelper(Node curr, Field f) {
        if(curr == null)
            return null;
        if(curr.isLeafNode())
            return (LeafNode) curr;
        else {
            InnerNode Inode = (InnerNode)curr;
            ArrayList<Field> keys = Inode.getKeys();
            ArrayList<Node> children = Inode.getChildren();
            for(int i = 0; i < keys.size(); i++) {
                if(f.compare(RelationalOperator.LTE, keys.get(i)))
                    return searchHelper(children.get(i), f);
                else if(f.compare(RelationalOperator.GT, keys.get(keys.size() - 1)))
                    return searchHelper(children.get(children.size() - 1), f);
            }
        }
        return null;
    }
    
    public void insert(Entry e) {
    	//your code here
        if(root == null) {
            root = new LeafNode(pLeaf);
            ((LeafNode)root).add(e);
            return;
        }
        Node curr = root;
        LeafNode leaf = searchHelper(curr, e.getField());
        if(leaf != null && leaf.contains(e.getField()))
            // No duplicates allowed.
            return;
        if(leaf == null)
            leaf = new LeafNode(pLeaf);
        if(!leaf.isFull()) {
            leaf.add(e);
        }
        else {
            leaf.add(e);
            splitHelper(leaf, leaf.getParent());
        }
    }

    private void splitHelper(Node node, InnerNode parent) {
        if(!node.isFull())
            return;
        if(parent == null) {
            // current node is root
            node.split(pInner);
            root = node.getParent();
            return;
        }
        node.split(pInner);
        if(node.getParent().getChildren().size() > pInner) {
            splitHelper(node.getParent(), node.getParent().getParent());
        }
    }
    
    public void delete(Entry e) {
    	//your code here
        if(root == null)
            return;
        Node curr = root;
        LeafNode leaf = searchHelper(curr, e.getField());
        if(leaf == null) {
            return;
        }
        if(leaf.canDirectRemove()) {
            leaf.delete(e);
        }
        else {
            // Check if siblings has an available node
            if(leaf.canBorrowFromSibling()) {
                leaf.delete(e);
                // update parent
                leaf.getParent().setKeys();
            }
            else if(leaf.getParent() == null) {
                leaf.delete(e);
                if(leaf.getEntries().size() == 0)
                    root = null;
            }
            else {
                // Merge node with sibling
                mergeHelper(leaf, e);
            }
        }
    }

    private void mergeHelper(Node node, Entry e) {
        if(node.canDirectRemove()) {
            return;
        }
        if(node.canBorrowFromSibling()) {
            node.getParent().setKeys();
            mergeHelper(node.getParent(), e);
        }
        else {
            InnerNode parent = node.getParent();
            node.merge(e.getField());
            if(parent == null) {
                // curr node is root
                if(!node.isLeafNode() && ((InnerNode)node).getChildren().size() <= 1) {
                    // if the root node has only one child, then update its child as the new root;
                    root = ((InnerNode)node).getChildren().get(0);
                }
            }
            else if(!parent.isValid()) {
                mergeHelper(parent, e);
            }
        }
    }
    
    public Node getRoot() {
    	//your code here
    	return root;
    }

}
