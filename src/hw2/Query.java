/*
 * Student 1 name: Weichen Zhu
 * Student 2 name: Hongchuan Shi
 * Date: 2019 10/04
 */
package hw2;

import java.util.ArrayList;
import java.util.List;

import hw1.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.*;

public class Query {

	private String q;
	
	public Query(String q) {
		this.q = q;
	}
	
	public Relation execute()  {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
		
		
		//your code here

		// FROM clause
		String tableName = sb.getFromItem().toString();
		Catalog c = Database.getCatalog();
		HeapFile table = c.getDbFile(c.getTableId(tableName));
		ArrayList<Tuple> tuples = table.getAllTuples();
		TupleDesc td = table.getTupleDesc();
		Relation relation = new Relation(tuples, td);

		// is SELECT ALL?
		boolean isAll = false;

		// is Rename?
		boolean isRename = false;
		ArrayList<Integer> renameFields = new ArrayList<>();
		ArrayList<String> newNames = new ArrayList<>();

		// is JOIN?
		boolean isJoin = false;
		// Format:{{tableName, field1Name, field2Name}}
		List<String[]> joinList = new ArrayList<>();
		// is Empty set?
		boolean isEmpty = false;

		// project list
		ArrayList<Integer> projects = new ArrayList<>();

		// contains WHERE?
		boolean isWhere = false;

		// is GroupBy ?
		boolean isGroupBy = false;

		// is Aggregate?
		boolean isAggregate = false;
		AggregateOperator aggregateOperator = null;

		// WHERE clause
		WhereExpressionVisitor wv = new WhereExpressionVisitor();
		Expression where = sb.getWhere();
		if(where != null) {
			isWhere = true;
			where.accept(wv);
		}
		RelationalOperator op = wv.getOp();
		String fieldName = wv.getLeft();
		Field operand = wv.getRight();

		// JOIN clause
		List<Join> joins = sb.getJoins();
		if(joins != null) {
			isJoin = true;
			String curr = tableName;
			for(Join join: joins) {
				String[] strs = new String[3];
				String otherName = join.getRightItem().toString();
				strs[0] = otherName;
				String ons = join.getOnExpression().toString();
				String[] both = ons.split("=");
				String combine1 = both[0].trim();
				String combine2 = both[1].trim();
				String t1 = combine1.split("\\.")[0];
				String c1 = combine1.split("\\.")[1];
				String t2 = combine2.split("\\.")[0];
				String c2 = combine2.split("\\.")[1];
				if(t1.equals(curr)) {
					strs[1] = c1;
					strs[2] = c2;
					curr = t2;
				}
				else if(t2.equals(curr)) {
					strs[1] = c2;
					strs[2] = c1;
					curr = t1;
				}
				else {
					// Empty set
					isEmpty = true;
					strs[1] = c1;
					strs[2] = c2;
				}
				joinList.add(strs);
			}
		}

		// GroupBy clause
		isGroupBy = sb.getGroupByColumnReferences() != null;

		if(isJoin) {
			for(String[] s: joinList) {
				int otherID = c.getTableId(s[0]);
				TupleDesc otherTD = c.getTupleDesc(otherID);
				Relation other = new Relation(c.getDbFile(otherID).getAllTuples(), otherTD);
				if(isEmpty)
					relation = relation.join(other, -100, -100);
				else
				relation = relation.join(other, relation.getDesc().nameToId(s[1]), otherTD.nameToId(s[2]));
			}
		}

		// SELECT clause
		List<SelectItem> selects = sb.getSelectItems();
		for(SelectItem item: selects) {
			ColumnVisitor cv = new ColumnVisitor();
			if(item instanceof AllColumns) {
				cv.visit((AllColumns) item);
				isAll = true;
			}
			else if(item instanceof SelectExpressionItem) {
				item.accept(cv);
				SelectExpressionItem expression = (SelectExpressionItem) item;
				Alias alias = expression.getAlias();
				String column = cv.getColumn();
				if(alias != null) {
					isRename = true;
					renameFields.add(td.nameToId(column));
					newNames.add(alias.getName());
					projects.add(td.nameToId(column));
				}
				else {
					td = relation.getDesc();
					if(!cv.isAggregate())
						projects.add(td.nameToId(column));
					else {
						isAggregate = true;
						aggregateOperator = cv.getOp();
						if(!column.equals("*"))
							projects.add(td.nameToId(column));
						else {
							for(int i = 0; i < td.numFields(); i++)
								projects.add(i);
						}
					}
				}
			}
		}

		if(isWhere) {
			relation = relation.select(td.nameToId(fieldName), op, operand);
		}

		if(isAll)
			return relation;

		if(isRename) {
			relation = relation.rename(renameFields, newNames);
		}

		relation = relation.project(projects);

		if(isAggregate) {
			relation = relation.aggregate(aggregateOperator, isGroupBy);
		}

		return relation;
	}
}
