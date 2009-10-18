package org.apache.hadoop.hbase.hbql.query.impl.hbase;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HConnection;
import org.apache.hadoop.hbase.hbql.client.HQuery;
import org.apache.hadoop.hbase.hbql.client.HQueryListener;
import org.apache.hadoop.hbase.hbql.client.HResults;
import org.apache.hadoop.hbase.hbql.client.ResultMissingColumnException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.schema.HBaseSchema;
import org.apache.hadoop.hbase.hbql.query.stmt.args.QueryArgs;
import org.apache.hadoop.hbase.hbql.query.stmt.args.WhereArgs;
import org.apache.hadoop.hbase.hbql.query.stmt.select.RowRequest;
import org.apache.hadoop.hbase.hbql.query.util.Lists;
import org.apache.hadoop.hbase.hbql.query.util.ResultsIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ResultsImpl<T> implements HResults<T> {

    private final List<ResultScanner> scannerList = Lists.newArrayList();
    private final QueryImpl hquery;

    ResultsImpl(final HQuery hquery) {
        this.hquery = (QueryImpl)hquery;
    }

    private HConnection getConnection() {
        return this.getHQuery().getConnection();
    }

    private QueryImpl getHQuery() {
        return this.hquery;
    }

    private List<ResultScanner> getScannerList() {
        return this.scannerList;
    }

    private QueryArgs getQueryArgs() {
        return this.getHQuery().getQueryArgs();
    }

    private WhereArgs getWhereArgs() {
        return this.getQueryArgs().getWhereArgs();
    }

    private List<HQueryListener<T>> getListeners() {
        return this.getHQuery().getListeners();
    }

    private List<RowRequest> getRowRequestList() throws HBqlException, IOException {
        return this.getHQuery().getRowRequestList();
    }

    public void close() {
        for (final ResultScanner scanner : this.getScannerList())
            closeCurrentScanner(scanner, false);

        this.scannerList.clear();
    }

    private void closeCurrentScanner(final ResultScanner scanner, final boolean removeFromList) {
        if (scanner == null)
            return;

        try {
            scanner.close();
        }
        catch (Exception e) {
            // Do nothing
        }

        if (removeFromList)
            getScannerList().remove(scanner);
    }

    public Iterator<T> iterator() {

        try {
            return new ResultsIterator<T>() {

                final HTable table = getConnection().getHTable(getQueryArgs().getSchema().getTableName());
                final ExprTree clientExprTree = getWhereArgs().getClientExprTree();
                final Iterator<RowRequest> rowRequestIter = getRowRequestList().iterator();

                int maxVersions = 0;
                ResultScanner currentResultScanner = null;
                Iterator<Result> resultIter = null;
                long recordCount = 0;

                // Prime the iterator with the first value
                T nextObject = fetchNextObject();

                private ExprTree getClientExprTree() {
                    return this.clientExprTree;
                }

                private Iterator<RowRequest> getRowRequestIter() {
                    return this.rowRequestIter;
                }

                private ResultScanner getCurrentResultScanner() {
                    return this.currentResultScanner;
                }

                private Iterator<Result> getResultIter() {
                    return this.resultIter;
                }

                protected T getNextObject() {
                    return this.nextObject;
                }

                private HTable getTable() {
                    return this.table;
                }

                private Iterator<Result> getNextResultScanner() throws IOException {
                    if (this.getRowRequestIter().hasNext()) {

                        final RowRequest rowRequest = this.getRowRequestIter().next();
                        this.maxVersions = rowRequest.getMaxVersions();

                        // First close previous ResultScanner before reassigning
                        closeCurrentScanner(this.getCurrentResultScanner(), true);

                        currentResultScanner = rowRequest.getResultScanner(this.getTable());

                        getScannerList().add(this.getCurrentResultScanner());

                        return this.getCurrentResultScanner().iterator();
                    }
                    else {
                        return null;
                    }
                }

                protected T fetchNextObject() throws HBqlException, IOException {

                    T val = doFetch();

                    if (val != null)
                        return val;

                    // Try one more time
                    val = doFetch();
                    if (val == null)
                        closeCurrentScanner(this.getCurrentResultScanner(), true);
                    return val;
                }

                @SuppressWarnings("unchecked")
                private T doFetch() throws HBqlException, IOException {

                    if (this.getResultIter() == null)
                        resultIter = getNextResultScanner();

                    if (this.getResultIter() != null) {

                        while (this.getResultIter().hasNext()) {

                            final Result result = this.getResultIter().next();

                            try {
                                if (getClientExprTree() == null || getClientExprTree().evaluate(result)) {

                                    this.recordCount++;

                                    final HBaseSchema schema = getQueryArgs().getSchema();
                                    final T val = (T)schema.newObject(getQueryArgs().getSelectAttribList(),
                                                                      getQueryArgs().getSelectElementList(),
                                                                      this.maxVersions,
                                                                      result);

                                    if (getListeners() != null)
                                        for (final HQueryListener<T> listener : getListeners())
                                            listener.onEachRow(val);

                                    return val;
                                }
                            }
                            catch (ResultMissingColumnException e) {
                                // Just skip and do nothing
                            }
                        }
                    }

                    // Reset to get next scanner
                    resultIter = null;
                    return null;
                }

                protected void setNextObject(final T nextObject, final boolean fromExceptionCatch) {

                    this.nextObject = nextObject;

                    if (nextObject == null && !fromExceptionCatch && getListeners() != null) {
                        for (final HQueryListener<T> listener : getListeners())
                            listener.onQueryComplete();
                    }
                }
            };
        }
        catch (HBqlException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return new Iterator<T>() {

            public boolean hasNext() {
                return false;
            }

            public T next() {
                return null;
            }

            public void remove() {

            }
        };
    }
}
