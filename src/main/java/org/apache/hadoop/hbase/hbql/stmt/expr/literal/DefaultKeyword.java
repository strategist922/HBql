package org.apache.hadoop.hbase.hbql.stmt.expr.literal;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.ResultMissingColumnException;
import org.apache.hadoop.hbase.hbql.stmt.expr.ExpressionContext;
import org.apache.hadoop.hbase.hbql.stmt.expr.node.GenericValue;

public class DefaultKeyword implements GenericValue {

    public void setExprContext(final ExpressionContext context) throws HBqlException {

    }

    public Object getValue(final Object object) throws HBqlException, ResultMissingColumnException {
        return null;
    }

    public GenericValue getOptimizedValue() throws HBqlException {
        return null;
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr, final boolean allowsCollections) throws HBqlException {
        return DefaultKeyword.class;
    }

    public boolean isAConstant() {
        return true;
    }

    public boolean isDefaultKeyword() {
        return true;
    }

    public boolean hasAColumnReference() {
        return false;
    }

    public String asString() {
        return "DEFAULT";
    }

    public void reset() {

    }
}
