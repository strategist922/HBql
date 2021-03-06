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

package org.apache.hadoop.hbase.hbql.statement.args;

import org.apache.expreval.expr.ExpressionProperty;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;

import java.io.Serializable;

public class ColumnWidth extends ExpressionProperty implements Serializable {

    private final boolean widthSpecified;
    private int width = -1;

    public ColumnWidth() {
        this(null);
    }

    public ColumnWidth(final GenericValue val) {
        super(SelectStatementArgs.ArgType.WIDTH, val);
        this.widthSpecified = val != null;
    }

    public boolean isWidthSpecified() {
        return this.widthSpecified;
    }

    public int getWidth() {
        return this.width;
    }

    public String asString() {
        return (this.isWidthSpecified() ? "WIDTH " + this.getGenericValue(0).asString() : "");
    }

    public void validate() throws HBqlException {
        if (this.isWidthSpecified()) {
            this.width = ((Number)this.evaluateConstant(0, false)).intValue();
            if (this.getWidth() <= 0)
                throw new HBqlException("Invalid column width: " + this.getWidth());
        }
    }
}