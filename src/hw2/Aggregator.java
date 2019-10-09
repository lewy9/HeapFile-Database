/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 10/04
 */
package hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import hw1.*;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * @author Doug Shook
 *
 */
public class Aggregator {

	private AggregateOperator o;
	private boolean groupBy;
	private TupleDesc td;

	// for single-column
	private ArrayList<Tuple> list;
	private int sum;
	private int count;

	// for double-column
	private HashMap<Field, ArrayList<Tuple>> map;
	private HashMap<Field, ArrayList<Integer>> counter;  // ArrayList format: {count, sum}

	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
		//your code here
		this.o = o;
		this.groupBy = groupBy;
		this.td = td;
		this.list = new ArrayList<>();
		this.sum = 0;
		this.count = 0;
		this.map = new HashMap<>();
		this.counter = new HashMap<>();
	}

	/**
	 * Merges the given tuple into the current aggregation
	 * @param t the tuple to be aggregated
	 */
	public void merge(Tuple t) {
		//your code here
		// Single Column
		if(!groupBy) {
			switch (o) {
				case MAX:
					if(list.size() == 0) {
						list.add(t);
					}
					else {
						if(list.get(0).getField(0).compare(RelationalOperator.LT, t.getField(0))) {
							// Keep the larger tuple
							list.set(0, t);
						}
					}
					break;

				case MIN:
					if(list.size() == 0) {
						list.add(t);
					}
					else {
						if(list.get(0).getField(0).compare(RelationalOperator.GT, t.getField(0))) {
							// Keep the smaller tuple
							list.set(0, t);
						}
					}
					break;

				case AVG:
					count++;
					if(td.getType(0).equals(Type.INT)) {
						sum += ((IntField) t.getField(0)).getValue();
					}
					break;

				case COUNT:
					count++;
					break;

				case SUM:
					if(td.getType(0).equals(Type.INT)) {
						sum += ((IntField) t.getField(0)).getValue();
					}
					break;
			}
		}
		else {
			// double columns
			switch (o) {
				case MAX:
					if(map.containsKey(t.getField(0))) {
						ArrayList<Tuple> temp = map.get(t.getField(0));
						if(map.get(t.getField(0)).get(0).getField(1).compare(RelationalOperator.LT, t.getField(1))) {
							temp.set(0, t);
							map.put(t.getField(0), temp);
						}
					}
					else {
						ArrayList<Tuple> temp = new ArrayList<>();
						temp.add(t);
						map.put(t.getField(0), temp);
					}
					break;

				case MIN:
					if(map.containsKey(t.getField(0))) {
						ArrayList<Tuple> temp = map.get(t.getField(0));
						if(map.get(t.getField(0)).get(0).getField(1).compare(RelationalOperator.GT, t.getField(1))) {
							temp.set(0, t);
							map.put(t.getField(0), temp);
						}
					}
					else {
						ArrayList<Tuple> temp = new ArrayList<>();
						temp.add(t);
						map.put(t.getField(0), temp);
					}
					break;

				case AVG:
					if(td.getType(1).equals(Type.INT)) {
						if(counter.containsKey(t.getField(0))) {
							ArrayList<Integer> temp = counter.get(t.getField(0));
							temp.set(0, temp.get(0) + 1);
							temp.set(1, temp.get(1) + ((IntField)t.getField(1)).getValue());
						}
						else {
							ArrayList<Integer> temp = new ArrayList<>();
							temp.add(1);
							temp.add(((IntField)t.getField(1)).getValue());
							counter.put(t.getField(0), temp);
						}
					}
					break;

				case COUNT:
					if(counter.containsKey(t.getField(0))) {
						ArrayList<Integer> temp = counter.get(t.getField(0));
						temp.set(0, temp.get(0) + 1);
					}
					else {
						ArrayList<Integer> temp = new ArrayList<>();
						temp.add(1);
						counter.put(t.getField(0), temp);
					}
					break;

				case SUM:
					if(counter.containsKey(t.getField(0))) {
						ArrayList<Integer> temp = counter.get(t.getField(0));
						temp.set(1, temp.get(1) + ((IntField)t.getField(1)).getValue());
					}
					else {
						ArrayList<Integer> temp = new ArrayList<>();
						temp.add(0);
						temp.add(((IntField)t.getField(1)).getValue());
						counter.put(t.getField(0), temp);
					}
					break;
			}
		}
	}
	
	/**
	 * Returns the result of the aggregation
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		//your code here
		ArrayList<Tuple> res = new ArrayList<>();
		if(!groupBy) {
			switch (o) {
				case MAX:
					if(list.size() != 0) {
						Tuple target = list.get(0);
						Type[] type = {target.getDesc().getType(0)};
						String[] name = {"MAX"};
						TupleDesc currTD = new TupleDesc(type, name);
						Tuple t = new Tuple(currTD);
						t.setField(0, target.getField(0));
						res.add(t);
					}
					break;

				case MIN:
					if(list.size() != 0) {
						Tuple target = list.get(0);
						Type[] type = {target.getDesc().getType(0)};
						String[] name = {"MIN"};
						TupleDesc currTD = new TupleDesc(type, name);
						Tuple t = new Tuple(currTD);
						t.setField(0, target.getField(0));
						res.add(t);
					}
					break;

				case COUNT:
					if(count >= 0) {
						Type[] type = {Type.INT};
						String[] name = {"COUNT"};
						TupleDesc currTD = new TupleDesc(type, name);
						Tuple t = new Tuple(currTD);
						t.setField(0, new IntField(count));
						res.add(t);
					}
					break;

				case SUM:
					if(sum >= 0) {
						Type[] type = {Type.INT};
						String[] name = {"SUM"};
						TupleDesc currTD = new TupleDesc(type, name);
						Tuple t = new Tuple(currTD);
						t.setField(0, new IntField(sum));
						res.add(t);
					}
					break;

				case AVG:
					if(count != 0) {
						Type[] type = {Type.INT};
						String[] name = {"AVG"};
						TupleDesc currTD = new TupleDesc(type, name);
						Tuple t = new Tuple(currTD);
						t.setField(0, new IntField(sum / count));
						res.add(t);
					}
					break;
			}
		}
		else {
			switch (o) {
				case MAX:
					for(Map.Entry<Field, ArrayList<Tuple>> entry: map.entrySet()) {
						Tuple target = entry.getValue().get(0);
						Type[] types = {target.getDesc().getType(0), target.getDesc().getType(1)};
						String[] names = {target.getDesc().getFieldName(0), "MAX"};
						TupleDesc currTD = new TupleDesc(types, names);
						Tuple t = new Tuple(currTD);
						t.setField(0, entry.getKey());
						t.setField(1, target.getField(1));
						res.add(t);
					}
					break;

				case MIN:
					for(Map.Entry<Field, ArrayList<Tuple>> entry: map.entrySet()) {
						Tuple target = entry.getValue().get(0);
						Type[] types = {target.getDesc().getType(0), target.getDesc().getType(1)};
						String[] names = {target.getDesc().getFieldName(0), "MIN"};
						TupleDesc currTD = new TupleDesc(types, names);
						Tuple t = new Tuple(currTD);
						t.setField(0, entry.getKey());
						t.setField(1, target.getField(1));
						res.add(t);
					}
					break;

				case COUNT:
					for(Map.Entry<Field, ArrayList<Integer>> entry: counter.entrySet()) {
						int target = entry.getValue().get(0);
						Type[] types = {td.getType(0), Type.INT};
						String[] names = {td.getFieldName(0), "COUNT"};
						TupleDesc currTD = new TupleDesc(types, names);
						Tuple t = new Tuple(currTD);
						t.setField(0, entry.getKey());
						t.setField(1, new IntField(target));
						res.add(t);
					}
					break;

				case SUM:
					for(Map.Entry<Field, ArrayList<Integer>> entry: counter.entrySet()) {
						int target = entry.getValue().get(1);
						Type[] types = {td.getType(0), Type.INT};
						String[] names = {td.getFieldName(0), "SUM"};
						TupleDesc currTD = new TupleDesc(types, names);
						Tuple t = new Tuple(currTD);
						t.setField(0, entry.getKey());
						t.setField(1, new IntField(target));
						res.add(t);
					}
					break;

				case AVG:
					for(Map.Entry<Field, ArrayList<Integer>> entry: counter.entrySet()) {
						int count = entry.getValue().get(0);
						int sum = entry.getValue().get(1);
						Type[] types = {td.getType(0), Type.INT};
						String[] names = {td.getFieldName(0), "AVG"};
						TupleDesc currTD = new TupleDesc(types, names);
						Tuple t = new Tuple(currTD);
						t.setField(0, entry.getKey());
						t.setField(1, new IntField(sum / count));
						res.add(t);
					}
					break;
			}
		}
		return res;
	}

}
