package org.apache.hadoop.hbase.hbql.query.expr.value.literal;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class BooleanLiteral extends GenericLiteral<BooleanValue> implements BooleanValue {

    private final Boolean value;

    public BooleanLiteral(final String text) {
        this.value = text.equalsIgnoreCase("true");
    }

    public BooleanLiteral(final boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getValue(final Object object) {
        return this.value;
    }

    @Override
    public Class<? extends ValueExpr> validateType() throws HPersistException {
        return BooleanValue.class;
    }
}