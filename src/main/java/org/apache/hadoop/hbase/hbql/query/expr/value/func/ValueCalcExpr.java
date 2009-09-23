package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.node.DateValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.NumberValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.StringValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class ValueCalcExpr extends GenericCalcExpr<ValueExpr> {

    private GenericCalcExpr typedExpr = null;

    public ValueCalcExpr(final ValueExpr expr1, final Operator op, final ValueExpr expr2) {
        super(expr1, op, expr2);
    }

    @Override
    public Class<? extends ValueExpr> validateType() throws HPersistException {

        final Class<? extends ValueExpr> type1 = this.getExpr1().validateType();
        final Class<? extends ValueExpr> type2 = this.getExpr2().validateType();

        if (!type1.equals(type2))
            throw new HPersistException("Type mismatch in ValueCalcExpr");

        if (type1.equals(DateValue.class))
            typedExpr = new DateCalcExpr((DateValue)this.getExpr1(), this.getOp(), (DateValue)this.getExpr2());
        else if (type1.equals(StringValue.class))
            typedExpr = new StringCalcExpr((StringValue)this.getExpr1(), this.getOp(), (StringValue)this.getExpr2());
        else if (type1.equals(NumberValue.class))
            typedExpr = new NumberCalcExpr((NumberValue)this.getExpr1(), this.getOp(), (NumberValue)this.getExpr2());
        else
            throw new HPersistException("Invalid type in ValueCalcExpr: " + type1.getClass());

        return type1;
    }

    @Override
    public ValueExpr getOptimizedValue() throws HPersistException {
        return this.typedExpr.getOptimizedValue();
    }

    @Override
    public Object getValue(final Object object) throws HPersistException {
        return this.typedExpr.getValue(object);
    }
}