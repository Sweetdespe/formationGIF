/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License, version 2.0, as published by the
 * Free Software Foundation.
 *
 * This program is also distributed with certain software (including but not
 * limited to OpenSSL) that is licensed under separate terms, as designated in a
 * particular file or component or in included license documentation. The
 * authors of MySQL hereby grant you an additional permission to link the
 * program and your derivative works with the separately licensed software that
 * they have included with MySQL.
 *
 * Without limiting anything contained in the foregoing, this file, which is
 * part of MySQL Connector/J, is also subject to the Universal FOSS Exception,
 * version 1.0, a copy of which can be found at
 * http://oss.oracle.com/licenses/universal-foss-exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License, version 2.0,
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.mysql.cj.xdevapi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mysql.cj.api.xdevapi.InsertResult;
import com.mysql.cj.api.xdevapi.InsertStatement;
import com.mysql.cj.x.core.MysqlxSession;
import com.mysql.cj.x.core.StatementExecuteOk;

public class InsertStatementImpl implements InsertStatement {
    private MysqlxSession mysqlxSession;
    private String schemaName;
    private String tableName;
    private InsertParams insertParams = new InsertParams();

    /* package private */ InsertStatementImpl(MysqlxSession mysqlxSession, String schema, String table, String[] fields) {
        this.mysqlxSession = mysqlxSession;
        this.schemaName = schema;
        this.tableName = table;
        this.insertParams.setProjection(fields);
    }

    /* package private */ InsertStatementImpl(MysqlxSession mysqlxSession, String schema, String table, Map<String, Object> fieldsAndValues) {
        this.mysqlxSession = mysqlxSession;
        this.schemaName = schema;
        this.tableName = table;
        this.insertParams.setFieldsAndValues(fieldsAndValues);
    }

    public InsertResult execute() {
        StatementExecuteOk ok = this.mysqlxSession.insertRows(this.schemaName, this.tableName, this.insertParams);
        return new InsertResultImpl(ok);
    }

    public CompletableFuture<InsertResult> executeAsync() {
        CompletableFuture<StatementExecuteOk> okF = this.mysqlxSession.asyncInsertRows(this.schemaName, this.tableName, this.insertParams);
        return okF.thenApply(ok -> new InsertResultImpl(ok));
    }

    public InsertStatement values(List<Object> row) {
        this.insertParams.addRow(row);
        return this;
    }
}
