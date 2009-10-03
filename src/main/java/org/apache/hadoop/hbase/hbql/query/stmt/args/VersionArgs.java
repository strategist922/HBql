package org.apache.hadoop.hbase.hbql.query.stmt.args;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 4, 2009
 * Time: 10:26:18 AM
 */
public class VersionArgs extends SelectArgs {

    public VersionArgs(final GenericValue val) {
        super(SelectArgs.Type.VERSION, val);
    }

    public int getValue() throws HBqlException {
        return ((Number)this.getGenericValue(0).getValue(null)).intValue();
    }

    public String asString() {
        return "VERSIONS " + this.getGenericValue(0).asString();
    }

}