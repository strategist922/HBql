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

package org.apache.hadoop.hbase.hbql.statement;

import org.apache.hadoop.hbase.hbql.client.ExecutionResults;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;
import org.apache.hadoop.hbase.hbql.mapping.ColumnAttrib;
import org.apache.hadoop.hbase.hbql.mapping.HBaseTableMapping;

public class DescribeMappingStatement extends StatementContext implements ConnectionStatement {

    public DescribeMappingStatement(final StatementPredicate predicate, final String mappingName) {
        super(predicate, mappingName);
    }

    public ExecutionResults execute(final HConnectionImpl connection) throws HBqlException {

        this.validateMappingName(connection);

        final HBaseTableMapping tableMapping = this.getHBaseTableMapping();

        if (tableMapping == null)
            return new ExecutionResults("Unknown mapping: " + this.getMappingName());

        final ExecutionResults retval = new ExecutionResults();

        retval.out.println("Mapping name: " + this.getMappingName());
        retval.out.println("Table name: " + tableMapping.getTableName());
        retval.out.println("Columns:");

        for (final String familyName : tableMapping.getFamilySet()) {
            for (final ColumnAttrib column : tableMapping.getColumnAttribListByFamilyName(familyName))
                retval.out.println("\t" + column.asString());
        }

        retval.out.flush();
        return retval;
    }
}