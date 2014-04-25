package simpledb;

import java.util.Map;
import java.util.*;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op what;

    private Map<Object, Integer> aggregatesCount;

    private boolean firstTuple = true;

    private String gbfield_name = null;
    private String afield_name = null;

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;

        if (!what.toString().equals("count")) {
                throw new IllegalArgumentException("Invalid Operator! Only count supported");
        }
        aggregatesCount = new HashMap<Object, Integer>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        
        Object groupByField = null;

        if (gbfield != NO_GROUPING) {
                if (gbfieldtype == Type.INT_TYPE) {
                        groupByField = new Integer(((IntField)tup.getField(gbfield)).getValue());
                } else if (gbfieldtype == Type.STRING_TYPE) {
                        groupByField = ((StringField)tup.getField(gbfield)).getValue();
                }
        }

        if (firstTuple) {
                if (gbfield != NO_GROUPING) {
                        gbfieldtype = tup.getField(gbfield).getType();
                        gbfield_name = tup.getTupleDesc().getFieldName(gbfield);
                }
                afield_name = tup.getTupleDesc().getFieldName(afield);
                firstTuple = false;
        }


        switch (what) {
                case COUNT: {
                        Integer aggregate_count = aggregatesCount.get(groupByField);
                        aggregatesCount.put(groupByField, (aggregate_count != null ? aggregate_count + 1 : 1));
                        break;
                }
        }
            
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        //throw new UnsupportedOperationException("please implement me for lab2");
        
        List<Tuple> tupleList = new ArrayList<Tuple>();
        int fafield = 1;
        String aggregate_name = what + " (" + afield_name + ")";

        TupleDesc tupleDesc = null;
        if (gbfield != NO_GROUPING) {
                Type[] typeArray = {gbfieldtype, Type.INT_TYPE };
                String[] fieldArray = {gbfield_name, aggregate_name};
                tupleDesc = new TupleDesc(typeArray, fieldArray);
        } else {
                Type[] typeArray = {Type.INT_TYPE};
                String[] fieldArray = {aggregate_name};

                tupleDesc = new TupleDesc(typeArray, fieldArray); 
                fafield = 0;
        }
        for (Map.Entry<Object, Integer> entry : aggregatesCount.entrySet()) {
                Tuple tuple = new Tuple(tupleDesc);

                int aggVal = 0;
                switch (what) {
                        case COUNT: {
                                aggVal = entry.getValue();
                                break;
                        }
                }

                if (gbfield != NO_GROUPING) {
                        if (gbfieldtype == Type.INT_TYPE) {
                                tuple.setField(0, new IntField((Integer) entry.getKey()));
                        } else if (gbfieldtype == Type.STRING_TYPE) {
                                tuple.setField(0, new StringField((String) entry.getKey(), Type.STRING_TYPE.getLen()));
                        }
                }
                tuple.setField(fafield, new IntField(aggVal));
                tupleList.add(tuple);
        }
        return new TupleIterator(tupleDesc, tupleList);
    }
    
}
