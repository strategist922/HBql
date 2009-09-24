package org.apache.hadoop.hbase.hbql.query.expr.value.var;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.ExprVariable;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;
import org.apache.hadoop.hbase.hbql.query.schema.FieldType;
import org.apache.hadoop.hbase.hbql.query.schema.Schema;
import org.apache.hadoop.hbase.hbql.query.schema.VariableAttrib;
import org.apache.hadoop.hbase.hbql.query.util.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 31, 2009
 * Time: 12:30:57 PM
 */
public abstract class GenericAttribRef<T extends ValueExpr> implements ValueExpr {

    private final ExprVariable exprVar;
    private ExprTree context = null;

    protected GenericAttribRef(final String attribName, final FieldType fieldType) {
        this.exprVar = new ExprVariable(attribName, fieldType);
    }

    protected ExprVariable getExprVar() {
        return this.exprVar;
    }

    protected String getName() {
        return this.getExprVar().getName();
    }

    public List<ExprVariable> getExprVariables() {
        return Lists.newArrayList(this.getExprVar());
    }

    public T getOptimizedValue() throws HPersistException {
        return (T)this;
    }

    public boolean isAConstant() {
        return false;
    }

    public void setContext(final ExprTree context) {
        this.context = context;
    }

    protected ExprTree getContext() {
        return this.context;
    }

    protected Schema getSchema() {
        return this.getContext().getSchema();
    }

    protected VariableAttrib getVariableAttrib() throws HPersistException {
        final VariableAttrib attrib = this.getSchema().getVariableAttribByVariableName(this.getExprVar().getName());
        if (attrib == null)
            throw new HPersistException("Invalid variable name: " + this.getExprVar().getName());
        return attrib;
    }

    public Class<? extends ValueExpr> validateType() throws HPersistException {
        return this.getExprVar().getFieldType().getExprType();
    }

}