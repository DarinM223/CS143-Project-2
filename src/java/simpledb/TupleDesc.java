package simpledb;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    List<TDItem> tditems;

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
        return tditems.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        tditems = new ArrayList<TDItem>();
        // some code goes here
        for (int i = 0; i < typeAr.length; i++) {
                if (fieldAr[i] == null)
                        tditems.add(new TDItem(typeAr[i], new String()));
                else 
                        tditems.add(new TDItem(typeAr[i], fieldAr[i]));
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        tditems = new ArrayList<TDItem>();
        // some code goes here
        for (int i = 0; i < typeAr.length; i++) {
                tditems.add(new TDItem(typeAr[i], new String()));
        }
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return tditems.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        try {
                return tditems.get(i).fieldName; //this could be null, as long as i is valid
        } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
        }
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        try {
                return tditems.get(i).fieldType;
        } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
        }
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        for (int i = 0; i < tditems.size(); i++) {
                try {
                        if (tditems.get(i).fieldName.equals(name)) {
                                return i;
                        }
                } catch (IndexOutOfBoundsException e) {
                        throw new NoSuchElementException();
                }
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        int bytes = 0;
        for (int i = 0; i < tditems.size(); i++) {
                bytes += tditems.get(i).fieldType.getLen();
        }
        return bytes;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        ArrayList<Type> tdTypes = new ArrayList<Type>();
        ArrayList<String> tdFields = new ArrayList<String>();
        //add the fields from both TupleDescs to the ArrayLists
        for (int i = 0; i < td1.numFields(); i++) {
                tdTypes.add(td1.getFieldType(i));
                tdFields.add(td1.getFieldName(i));
        }
        for (int i = 0; i < td2.numFields(); i++) {
                tdTypes.add(td2.getFieldType(i));
                tdFields.add(td2.getFieldName(i));
        }
        //convert from ArrayList to array
        Type[] tdTypeArr = tdTypes.toArray(new Type[tdTypes.size()]); 
        String[] tdFieldArr = tdFields.toArray(new String[tdFields.size()]);
        //create a new TupleDesc from the arrays
        TupleDesc newDesc = new TupleDesc(tdTypeArr, tdFieldArr);
        return newDesc;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        // some code goes here
        if (o instanceof TupleDesc) {
                TupleDesc compObj = (TupleDesc)o;
                //return false if sizes aren't the same
                if (this.getSize() != compObj.getSize()) return false;
                //apparently it doesn't matter what the field names are for the comparison
                for (int i = 0; i < this.numFields(); i++) {
                        if (!this.getFieldType(i).equals(compObj.getFieldType(i))) {
                                return false;
                        }                   
                }
                return true;
        } else {
                return false;
        }
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        String desc = new String();
        for (int i = 0; i < tditems.size(); i++) {
                desc += tditems.get(i).toString();
                desc += ",";
        }
        return desc;
    }
}
