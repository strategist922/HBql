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

package org.apache.hadoop.hbase.hbql.schema.property;

import org.apache.expreval.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.io.hfile.Compression;

public class CompressionTypeProperty extends FamilyProperty {

    public CompressionTypeProperty(final GenericValue arg0) {
        super(Type.COMPRESSIONTYPE, arg0);
    }

    public Compression.Algorithm getValue() throws HBqlException {
        final String val = (String)this.evaluateConstant(0, false, null);

        try {
            return Compression.Algorithm.valueOf(val);
        }
        catch (Exception e) {
            throw new HBqlException("Invalid compression type: " + val);
        }
    }

    public String asString() {
        return "INDEX INTERVAL " + this.getGenericValue(0).asString();
    }
}