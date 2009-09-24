package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class StringBetweenStmt extends GenericBetweenStmt {

    public StringBetweenStmt(final ValueExpr expr, final boolean not, final ValueExpr lower, final ValueExpr upper) {
        super(not, expr, lower, upper);
    }

    @Override
    public Boolean getValue(final Object object) throws HPersistException {

        final String strval = (String)this.getExpr().getValue(object);
        final boolean retval = strval.compareTo((String)this.getLower().getValue(object)) >= 0
                               && strval.compareTo((String)this.getUpper().getValue(object)) <= 0;

        return (this.isNot()) ? !retval : retval;
    }
}