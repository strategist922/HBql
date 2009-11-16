/*
 * Copyright (c) 2009.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.hbql.impl;

import org.apache.expreval.util.Lists;
import org.apache.expreval.util.Sets;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HResultSet;
import org.apache.hadoop.hbase.hbql.client.QueryListener;
import org.apache.hadoop.hbase.hbql.schema.AnnotationMapping;
import org.apache.hadoop.hbase.hbql.schema.ColumnAttrib;
import org.apache.hadoop.hbase.hbql.schema.Mapping;
import org.apache.hadoop.hbase.hbql.statement.SelectStatement;
import org.apache.hadoop.hbase.hbql.statement.args.WithArgs;
import org.apache.hadoop.hbase.hbql.statement.select.RowRequest;

import java.util.List;
import java.util.Set;

public class Query<T> {

    private final HConnectionImpl connection;
    private final SelectStatement selectStatement;
    private List<QueryListener<T>> listeners = null;

    private Query(final HConnectionImpl connection,
                  final SelectStatement selectStatement,
                  final Mapping mapping) throws HBqlException {
        this.connection = connection;
        this.selectStatement = selectStatement;

        if (mapping != null)
            this.getSelectStatement().setMapping(mapping);

        this.getSelectStatement().validate(this.getHConnection());
    }

    public static <T> Query<T> newQuery(final HConnectionImpl connection,
                                        final SelectStatement selectStatement) throws HBqlException {
        return new Query<T>(connection, selectStatement, null);
    }

    public static <T> Query<T> newQuery(final HConnectionImpl connection,
                                        final SelectStatement selectStatement,
                                        final Class clazz) throws HBqlException {
        Mapping mapping = null;
        if (clazz != null) {
            mapping = AnnotationMapping.getAnnotationMapping(clazz);

            if (mapping == null)
                throw new HBqlException("Unknown class " + clazz.getName());
        }
        return new Query<T>(connection, selectStatement, mapping);
    }


    public synchronized void addListener(final QueryListener<T> listener) {
        if (this.getListeners() == null)
            this.listeners = Lists.newArrayList();

        this.getListeners().add(listener);
    }

    public HConnectionImpl getHConnection() {
        return this.connection;
    }

    public SelectStatement getSelectStatement() {
        return this.selectStatement;
    }

    public List<RowRequest> getRowRequestList() throws HBqlException {

        final WithArgs withArgs = this.getSelectStatement().getWithArgs();

        // Get list of all columns that are used in select list and expr tree
        final Set<ColumnAttrib> allAttribs = Sets.newHashSet();
        allAttribs.addAll(this.getSelectStatement().getSelectAttribList());
        allAttribs.addAll(withArgs.getAllColumnsUsedInExprs());

        return withArgs.getRowRequestList(allAttribs);
    }

    public List<QueryListener<T>> getListeners() {
        return this.listeners;
    }

    public void clearListeners() {
        if (this.getListeners() != null)
            this.getListeners().clear();
    }

    public HResultSet<T> getResults() throws HBqlException {
        return new HResultSetImpl<T>(this);
    }
}