package org.apache.hadoop.hbase.hbql.query.expr.value.var;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.node.NumberValue;
import org.apache.hadoop.hbase.hbql.query.schema.FieldType;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class IntegerAttribRef extends GenericAttribRef<NumberValue> implements NumberValue {

    public IntegerAttribRef(final String attribName) {
        super(attribName, FieldType.IntegerType);
    }

    @Override
    public Integer getValue(final Object object) throws HPersistException {
        return (Integer)this.getVariableAttrib().getCurrentValue(object);
    }

}