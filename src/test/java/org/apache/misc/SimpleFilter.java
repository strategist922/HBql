/*
 * Copyright (c) 2011.  The Apache Software Foundation
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

package org.apache.misc;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleFilter {

    static byte[] family = Bytes.toBytes("f1");
    static byte[] val1   = Bytes.toBytes("val1");
    static byte[] val2   = Bytes.toBytes("val2");

    public static void main(String[] args) throws HBqlException, IOException {

        final SingleColumnValueFilter val1Filter11 = new SingleColumnValueFilter(family,
                                                                                 val1,
                                                                                 CompareFilter.CompareOp.EQUAL,
                                                                                 Bytes.toBytes("11"));

        final SingleColumnValueFilter val2Filter10 = new SingleColumnValueFilter(family,
                                                                                 val2,
                                                                                 CompareFilter.CompareOp.EQUAL,
                                                                                 Bytes.toBytes(10));

        doQuery("Results for val2 = 10 || va1l = '11'", val2Filter10, val1Filter11);
    }

    static void doQuery(String msg, Filter... filters) throws IOException {

        HTable table = new HTable(HBaseConfiguration.create(), "table20");

        Scan scan = new Scan();
        scan.addColumn(family, val1);
        scan.addColumn(family, val2);

        List<Filter> filterList = new ArrayList<Filter>();
        for (Filter f : filters)
            filterList.add(f);

        scan.setFilter(new FilterList(FilterList.Operator.MUST_PASS_ONE, filterList));

        for (Result result : table.getScanner(scan))
            System.out.println(Bytes.toString(result.getRow()) + " - "
                               + Bytes.toString(result.getValue(family, val1)) + " - "
                               + Bytes.toInt(result.getValue(family, val2)));
    }
}