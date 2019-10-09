/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 10/04
 */
package hw2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import hw1.*;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

	private ArrayList<Tuple> tuples;
	private TupleDesc td;

	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		//your code here
        this.tuples = l;
        this.td = td;
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		//your code here
		ArrayList<Tuple> list = new ArrayList<>();
		for(Tuple tp: tuples) {
			Field curr = tp.getField(field);
			if(curr.compare(op, operand))
				list.add(tp);
		}
		return new Relation(list, td);
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) {
		//your code here

		// Create new Schema
		// index of the input fields
		int index = 0;
		int num = td.numFields();
		Type[] types = new Type[num];
		String[] newFields = new String[num];
		for(int i = 0; i < num; i++) {
			// Index bound check & field name null check
			if(index < fields.size() && i == fields.get(index) && names.get(index).length() != 0) {
				// Change
				newFields[i] = names.get(index);
				index++;
			}
			else {
				// remain unchanged
				newFields[i] = td.getFieldName(i);
			}
			types[i] = td.getType(i);
		}

		// Check field name duplicates
		Set<String> set = new HashSet<>();
		for(String name: newFields) {
			if(!set.add(name)) {
				// Contains Duplicates, throw an exception
				try {
					String t = td.getFieldName(num);
				} catch (NoSuchElementException e) {
					throw new IllegalArgumentException("Duplicates!");
				}
			}
		}

		TupleDesc newTD = new TupleDesc(types, newFields);

		// Create the temporary tuples
		ArrayList<Tuple> list = new ArrayList<>();
		for(Tuple tp: tuples) {
			Tuple newTP = new Tuple(newTD);
			for(int i = 0; i < newTP.getDesc().numFields(); i++) {
				newTP.setField(i, tp.getField(i));
			}
			list.add(newTP);
		}

		return new Relation(list, newTD);
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 */
	public Relation project(ArrayList<Integer> fields) {
		//your code here
		// Create new Schema
		ArrayList<Tuple> list = new ArrayList<>();
		int num = fields.size();
		Type[] types = new Type[num];
		String[] newFields = new String[num];
		int j = 0;
		for(int i: fields) {
			try {
				types[j] = td.getType(i);
				newFields[j] = td.getFieldName(i);
				j++;
			} catch (NoSuchElementException e) {
				throw new IllegalArgumentException();
			}
		}
		TupleDesc newTD = new TupleDesc(types, newFields);
		if(num == 0) {
			return new Relation(new ArrayList<>(), newTD);
		}
		for(Tuple t: tuples) {
			Tuple newTP = new Tuple(newTD);
			int k = 0;
			for(int i: fields) {
				newTP.setField(k, t.getField(i));
				k++;
			}
			list.add(newTP);
		}
		return new Relation(list, newTD);
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		//your code here
		// Create new Schema
		ArrayList<Tuple> list = new ArrayList<>();
		int num = td.numFields() + other.getDesc().numFields();
		Type[] types = new Type[num];
		String[] newFields = new String[num];
		for(int i = 0; i < td.numFields(); i++) {
			types[i] = td.getType(i);
			newFields[i] = td.getFieldName(i);
		}
		for(int i = td.numFields(); i < num; i++) {
			types[i] = other.getDesc().getType(i - td.numFields());
			newFields[i] = other.getDesc().getFieldName(i - td.numFields());
		}
		TupleDesc newTD = new TupleDesc(types, newFields);

		// For join empty set
		if(field1 == -100 && field2 == -100)
			return new Relation(new ArrayList<>(), newTD);

		// Find targeted tuples-pairs
		for(Tuple x: tuples) {
			for(Tuple y: other.getTuples()) {
				if(x.getField(field1).compare(RelationalOperator.EQ, y.getField(field2))) {
					Tuple newTP = new Tuple(newTD);
					for(int j = 0; j < td.numFields(); j++) {
						newTP.setField(j, x.getField(j));
					}
					for(int j = td.numFields(); j < num; j++) {
						newTP.setField(j, y.getField(j - td.numFields()));
					}
					list.add(newTP);
				}
			}
		}
		return new Relation(list, newTD);
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		//your code here
		Aggregator ag = new Aggregator(op, groupBy, getDesc());
		for(Tuple t: getTuples()) {
			ag.merge(t);
		}
		return new Relation(ag.getResults(), ag.getResults().get(0).getDesc());
	}
	
	public TupleDesc getDesc() {
		//your code here
		return this.td;
	}
	
	public ArrayList<Tuple> getTuples() {
		//your code here
		return this.tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		//your code here
		StringBuilder sb = new StringBuilder();
		sb.append(td.toString());
		sb.append("\n");
		for(Tuple tp: tuples) {
			sb.append(tp.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
