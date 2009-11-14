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

package org.apache.hadoop.hbase.jdbc;

import java.sql.RowId;

public class JdbcRowIdImpl implements RowId {

    private final String keyval;

    public JdbcRowIdImpl(final String keyval) {
        this.keyval = keyval;
    }

    private String getKeyval() {
        return this.keyval;
    }

    public byte[] getBytes() {
        return this.getKeyval().getBytes();
    }

    public String toString() {
        return this.getKeyval();
    }

    public boolean equals(final Object o) {
        return (o instanceof String) && this.getKeyval().equals(o);
    }

    public int hashCode() {
        return getKeyval().hashCode();
    }
}