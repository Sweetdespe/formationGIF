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

import java.util.concurrent.CompletableFuture;

import com.mysql.cj.api.xdevapi.DocResult;
import com.mysql.cj.api.xdevapi.Expression;
import com.mysql.cj.api.xdevapi.FindStatement;
import com.mysql.cj.x.core.MysqlxSession;
import com.mysql.cj.x.protobuf.MysqlxCrud.Find.RowLock;

public class FindStatementImpl extends FilterableStatement<FindStatement, DocResult> implements FindStatement {
    private MysqlxSession mysqlxSession;
    private DocFindParams findParams;

    /* package private */ FindStatementImpl(MysqlxSession mysqlxSession, String schema, String collection, String criteria) {
        super(new DocFindParams(schema, collection));
        this.findParams = (DocFindParams) this.filterParams;
        this.mysqlxSession = mysqlxSession;
        if (criteria != null && criteria.length() > 0) {
            this.findParams.setCriteria(criteria);
        }
    }

    public DocResultImpl execute() {
        return this.mysqlxSession.findDocs(this.findParams);
    }

    public CompletableFuture<DocResult> executeAsync() {
        return this.mysqlxSession.asyncFindDocs(this.findParams);
    }

    public <R> CompletableFuture<R> executeAsync(R id, Reducer<DbDoc, R> reducer) {
        return this.mysqlxSession.asyncFindDocsReduce(this.findParams, id, reducer);
    }

    @Override
    public FindStatement fields(String... projection) {
        this.findParams.setFields(projection);
        return this;
    }

    public FindStatement fields(Expression docProjection) {
        this.findParams.setFields(docProjection);
        return this;
    }

    @Override
    public FindStatement groupBy(String... groupBy) {
        this.findParams.setGrouping(groupBy);
        return this;
    }

    public FindStatement having(String having) {
        this.findParams.setGroupingCriteria(having);
        return this;
    }

    @Override
    public FindStatement lockShared() {
        this.findParams.setLock(RowLock.SHARED_LOCK);
        return this;
    }

    @Override
    public FindStatement lockExclusive() {
        this.findParams.setLock(RowLock.EXCLUSIVE_LOCK);
        return this;
    }
}
