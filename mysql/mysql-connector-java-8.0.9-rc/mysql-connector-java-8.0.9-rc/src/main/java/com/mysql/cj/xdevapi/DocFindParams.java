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

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import com.mysql.cj.api.xdevapi.Expression;
import com.mysql.cj.x.protobuf.MysqlxCrud.Collection;
import com.mysql.cj.x.protobuf.MysqlxCrud.Projection;

public class DocFindParams extends FindParams {
    public DocFindParams(String schemaName, String collectionName) {
        super(schemaName, collectionName, false);
    }

    public DocFindParams(String schemaName, String collectionName, String criteriaString) {
        super(schemaName, collectionName, criteriaString, false);
    }

    private DocFindParams(Collection coll, boolean isRelational) {
        super(coll, isRelational);
    }

    public void setFields(Expression docProjection) {
        this.fields = Collections.singletonList(Projection.newBuilder().setSource(new ExprParser(docProjection.getExpressionString(), false).parse()).build());
    }

    @Override
    public void setFields(String... projection) {
        this.fields = new ExprParser(Arrays.stream(projection).collect(Collectors.joining(", ")), false).parseDocumentProjection();
    }

    @Override
    public FindParams clone() {
        FindParams newFindParams = new DocFindParams(this.collection, this.isRelational);
        newFindParams.setLimit(this.limit);
        newFindParams.setOffset(this.offset);
        if (this.orderExpr != null) {
            newFindParams.setOrder(this.orderExpr);
        }
        if (this.criteriaStr != null) {
            newFindParams.setCriteria(this.criteriaStr);
            if (this.args != null) {
                // newFindParams.args should already exist after setCriteria() call
                for (int i = 0; i < this.args.length; i++) {
                    newFindParams.args[i] = this.args[i];
                }
            }
        }
        if (this.groupBy != null) {
            newFindParams.setGrouping(this.groupBy);
        }
        if (this.having != null) {
            newFindParams.setGroupingCriteria(this.having);
        }
        if (this.lock != null) {
            newFindParams.setLock(this.lock);
        }
        return newFindParams;
    }
}
