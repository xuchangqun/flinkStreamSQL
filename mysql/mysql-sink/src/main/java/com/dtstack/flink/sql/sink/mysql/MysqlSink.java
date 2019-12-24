/*
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


package com.dtstack.flink.sql.sink.mysql;


import com.dtstack.flink.sql.sink.IStreamSinkGener;
import com.dtstack.flink.sql.sink.rdb.RdbSink;
import com.dtstack.flink.sql.sink.rdb.format.RetractJDBCOutputFormat;
import com.dtstack.flink.sql.util.DtStringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Date: 2017/2/27
 * Company: www.dtstack.com
 *
 * @author xuchao
 */

public class MysqlSink extends RdbSink implements IStreamSinkGener<RdbSink> {

    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    public MysqlSink() {
    }

    @Override
    public RetractJDBCOutputFormat getOutputFormat() {
        return new RetractJDBCOutputFormat();
    }

    @Override
    public void buildSql(String scheam, String tableName, List<String> fields) {
        buildReplaceUpsertStatement(tableName, fields);
        buildDuplicateUpsertStatement(tableName, fields);
    }

    @Override
    public String buildUpdateSql(String schema, String tableName, List<String> fieldNames, Map<String, List<String>> realIndexes, List<String> fullField) {
        return null;
    }

    private void buildReplaceUpsertStatement(String tableName, List<String> fields) {
        this.sql = getUpsertIntoStatement("REPLACE ", tableName, fields);
    }

    public void buildDuplicateUpsertStatement(String tableName, List<String> fields) {
        String updateClause = fields.stream().map(f -> quoteIdentifier(f) + "=IFNULL(VALUES(" + quoteIdentifier(f) + "),"+ quoteIdentifier(f) + ")")
                .collect(Collectors.joining(", "));

        this.sql = getUpsertIntoStatement("INSERT",tableName, fields) +
                " ON DUPLICATE KEY UPDATE " + updateClause;
    }

    public String getUpsertIntoStatement(String operator, String tableName, List<String> fields) {
        String columns = fields.stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));

        String placeholders = fields.stream()
                .map(f -> "?")
                .collect(Collectors.joining(", "));

        return operator + " INTO " + quoteIdentifier(tableName) +
                "(" + columns + ")" + " VALUES (" + placeholders + ")";
    }

    @Override
    public String getDriverName() {
        return MYSQL_DRIVER;
    }

}
