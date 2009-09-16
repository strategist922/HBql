package com.imap4j.hbase.antlr.args;

import com.imap4j.hbase.hbase.HPersistException;
import com.imap4j.hbase.hbql.expr.node.NumberValue;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Sep 4, 2009
 * Time: 10:26:18 AM
 */
public class VersionArgs {

    private int value = -1;

    public VersionArgs(final NumberValue val) {
        try {
            if (val != null)
                this.value = val.getCurrentValue(null).intValue();
        }
        catch (HPersistException e) {
            e.printStackTrace();
        }
    }

    public boolean isValid() {
        return this.getValue() != -1;
    }

    public int getValue() {
        return this.value;
    }
}