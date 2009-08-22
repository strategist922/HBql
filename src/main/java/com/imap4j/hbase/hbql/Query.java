package com.imap4j.hbase.hbql;

import com.google.common.collect.Lists;
import com.imap4j.hbase.antlr.QueryArgs;
import com.imap4j.hbase.antlr.config.HBqlRule;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.RowResult;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 20, 2009
 * Time: 9:26:38 PM
 */
public class Query<T extends Persistable> {

    final String query;
    final QueryListener<T> listener;

    public Query(final String query, final QueryListener<T> listener) {
        this.query = query;
        this.listener = listener;
    }

    public void execute() throws IOException, PersistException {

        final QueryArgs qa = (QueryArgs)HBqlRule.QUERY.parse(this.query);

        final ClassSchema classSchema = ClassSchema.getClassSchema(qa.getTableName());

        final HTable table = new HTable(new HBaseConfiguration(), classSchema.getTableName());

        final List<String> colList = Lists.newArrayList();

        try {
            if (qa.getColumnList() == null) {
                final List<String> fieldList = ClassSchema.getClassSchema(qa.getTableName()).getFieldList();
                for (final String fieldName : fieldList) {
                    final FieldAttrib attrib = classSchema.getFieldAttribMapByField().get(fieldName);
                    colList.add(attrib.getQualifiedName());
                }
            }
            else {
                for (final String attribName : qa.getColumnList()) {
                    final FieldAttrib attrib = classSchema.getFieldAttribMapByField().get(attribName);
                    colList.add(attrib.getQualifiedName());
                }
            }

            final String[] cols = colList.toArray(new String[colList.size()]);
            final Scanner scanner = table.getScanner(cols);

            for (RowResult result : scanner) {

                final T newobj = (T)classSchema.getClazz().newInstance();
                final FieldAttrib keyattrib = classSchema.getKeyFieldAttrib();
                final byte[] keybytes = result.getRow();
                final Object keyval = keyattrib.getValueFromBytes(newobj, keybytes);
                classSchema.getKeyFieldAttrib().getField().set(newobj, keyval);

                for (byte[] colbytes : result.keySet()) {

                    final String column = new String(colbytes);
                    final FieldAttrib attrib = classSchema.getFieldAttribMapByColumn().get(column);

                    final byte[] valbytes = result.get(colbytes).getValue();
                    final Object val = attrib.getValueFromBytes(newobj, valbytes);
                    attrib.getField().set(newobj, val);
                }

                this.listener.onEachRow(newobj);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            throw new PersistException("Error in execute()");
        }

    }
}
