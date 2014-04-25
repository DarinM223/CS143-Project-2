package simpledb;
import java.util.*;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op what;

    private Map<Object, Integer> aggregates;
    private Map<Object, Integer> aggregatesCount;
    private boolean firstTuple = true;

    private String gbfield_name = null;
    private String afield_name = null;

    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;

        aggregates = new HashMap<Object, Integer>();
        aggregatesCount = new HashMap<Object, Integer>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Object groupByField = null;
        if (gbfield != NO_GROUPING) {
                if (gbfieldtype == Type.INT_TYPE) {
                        groupByField = new Integer(((IntField)tup.getField(gbfield)).getValue());
                } else if (gbfieldtype == Type.STRING_TYPE) {
                        groupByField = ((StringField) tup.getField(gbfield)).getValue();
                }

                if (firstTuple) {
                        if (gbfield != NO_GROUPING) {
                                gbfieldtype = tup.getField(gbfield).getType();
                                gbfield_name = tup.getTupleDesc().getFieldName(gbfield);
                        }
                        afield_name = tup.getTupleDesc().getFieldName(afield);
                        firstTuple = false;
                }

                int val = ((IntField) tup.getField(afield)).getValue();
                Integer aggregate_value = aggregates.get(groupByField);
                switch (what) {
                        case MIN: {
                                          if ((aggregate_value != null && val < aggregate_value) || aggregate_value == null) {
                                                  aggregates.put(groupByField, val);
                                          }
                                          break;
                        }
                        case MAX: {
                                          if ((aggregate_value != null && val > aggregate_value) || aggregate_value == null) {
                                                  aggregates.put(groupByField, val);
                                          }
                                          break;
                        }
                        case SUM: {
                                          aggregates.put(groupByField, (aggregate_value == null ? val : (val + aggregate_value)));
                                          break;
                        }
                        case AVG: {
                                          Integer aggregate_count = aggregatesCount.get(groupByField);
                                          //if the aggregate value and count are not null, add it
                                          if (aggregate_value != null && aggregate_count != null) {
                                                  aggregates.put(groupByField, aggregate_value + val);
                                                  aggregatesCount.put(groupByField, aggregate_count + 1);
                                          } else if (aggregate_value == null && aggregate_count == null) { //if they are both null add default  
                                                  aggregates.put(groupByField, val);
                                                  aggregatesCount.put(groupByField, 1);
                                          } else { //otherwise they are out of sync
                                                  throw new RuntimeException("Aggregate value and count are out of sync");
                                          }
                                          break;
                        }
                        case COUNT: {
                                          Integer aggregate_count = aggregatesCount.get(groupByField);
                                          Integer cnt = (aggregate_count != null ? aggregate_count + 1 : 1);
                                          aggregates.put(groupByField, cnt);
                                          aggregatesCount.put(groupByField, cnt);
                                          break;
                        }
                                
                }

        }


    }

    /**
     * Create a DbIterator over group aggregate results.
     * 
     * @return a DbIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        //throw new
        //UnsupportedOperationException("please implement me for lab2");
        List<Tuple> tupleList = new ArrayList<Tuple>();
        int fafield = 1;

        String aggregate_name = what + " (" + afield_name + ")";
        TupleDesc tupleDesc = null;
        if (gbfield != NO_GROUPING) {
                Type[] typeArray = {gbfieldtype, Type.INT_TYPE };
                String[] fieldArray = {gbfield_name, aggregate_name};
                tupleDesc = new TupleDesc(typeArray, fieldArray);
        } else {
                fafield = 0;
                Type[] typeArray = {Type.INT_TYPE};
                String[] fieldArray = {aggregate_name};
                tupleDesc = new TupleDesc(typeArray, fieldArray);
        }

        for (Map.Entry<Object, Integer> entry : aggregates.entrySet()) {
                Tuple tuple = new Tuple(tupleDesc);
                int aggVal = 0;
                switch (what) {
                        case AVG: {
                                         Integer count = aggregatesCount.get(entry.getKey());
                                         if (count == null) { //for some reason count is null
                                                 System.out.println("WHAT THE FUUUU222");
                                         }
                                         aggVal = entry.getValue() / count; //need to round?
                                         break;
                        }
                        case COUNT: {
                                         aggVal = aggregatesCount.get(entry.getKey());
                                         break;
                        }
                        default: {
                                         aggVal = entry.getValue();
                                         break;
                        }
                }
                if (gbfield != NO_GROUPING) {
                        if (gbfieldtype == Type.INT_TYPE) {
                                tuple.setField(0, new IntField((Integer) entry.getKey()));
                        } else if (gbfieldtype == Type.STRING_TYPE) {
                                tuple.setField(0, new StringField((String)entry.getKey(), Type.STRING_TYPE.getLen()));
                        }
                }
                tuple.setField(fafield, new IntField(aggVal));
                tupleList.add(tuple);
        }
        return new TupleIterator(tupleDesc, tupleList);
    }
}