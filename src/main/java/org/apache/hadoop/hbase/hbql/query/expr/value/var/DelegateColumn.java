package org.apache.hadoop.hbase.hbql.query.expr.value.var;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprContext;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.schema.VariableAttrib;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 29, 2009
 * Time: 1:13:05 PM
 */
public class DelegateColumn extends GenericColumn<GenericValue> {

    private GenericColumn typedColumn = null;
    private String attribName;

    public DelegateColumn(final String attribName) {
        super(null, null);
        this.attribName = attribName;
    }

    private GenericColumn getTypedColumn() {
        return this.typedColumn;
    }

    private void setTypedColumn(final GenericColumn typedColumn) {
        this.typedColumn = typedColumn;
    }

    @Override
    public String getVariableName() {
        return this.attribName;
    }

    @Override
    public Object getValue(final Object object) throws HBqlException {
        return this.getTypedColumn().getValue(object);
    }

    @Override
    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowsCollections) throws TypeException {
        return this.getTypedColumn().validateTypes(parentExpr, allowsCollections);
    }

    @Override
    public void setContext(ExprContext context) throws HBqlException {

        final VariableAttrib attrib = context.getSchema().getVariableAttribByVariableName(this.getVariableName());

        if (attrib == null)
            throw new HBqlException("Invalid variable: " + this.getVariableName());

        switch (attrib.getFieldType()) {

            case KeyType:
            case StringType:
                this.setTypedColumn(new StringColumn(attrib));
                break;

            case LongType:
                this.setTypedColumn(new LongColumn(attrib));
                break;

            case IntegerType:
                this.setTypedColumn(new IntegerColumn(attrib));
                break;

            case DateType:
                this.setTypedColumn(new DateColumn(attrib));
                break;

            case BooleanType:
                this.setTypedColumn(new BooleanColumn(attrib));
                break;

            default:
                throw new HBqlException("Invalid type: " + attrib.getFieldType().name());
        }

        this.getTypedColumn().setContext(context);
    }
}