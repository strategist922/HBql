package com.imap4j.hbase.hbql.expr.node;

import com.imap4j.hbase.hbase.HPersistException;
import com.imap4j.hbase.hbql.expr.EvalContext;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 26, 2009
 * Time: 10:18:22 AM
 */
public interface DateValue extends ValueExpr {

    Long getValue(final EvalContext context) throws HPersistException;
}