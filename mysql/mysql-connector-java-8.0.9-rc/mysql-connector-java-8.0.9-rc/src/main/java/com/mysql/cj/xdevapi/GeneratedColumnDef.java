/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

import com.mysql.cj.api.xdevapi.Type;
import com.mysql.cj.api.xdevapi.ColumnDefinition.GeneratedColumnDefinition;
import com.mysql.cj.core.util.StringUtils;

public final class GeneratedColumnDef extends AbstractColumnDef<GeneratedColumnDefinition> implements GeneratedColumnDefinition {

    private String expr;
    private boolean isStored = false;

    public GeneratedColumnDef(String columnName, Type columnType, String expression) {
        this.name = columnName;
        this.type = columnType;
        this.expr = expression;
    }

    public GeneratedColumnDef(String columnName, Type columnType, int length, String expression) {
        this.name = columnName;
        this.type = columnType;
        this.length = length;
        this.expr = expression;
    }

    @Override
    GeneratedColumnDefinition self() {
        return this;
    }

    @Override
    public GeneratedColumnDefinition stored() {
        this.isStored = true;
        return self();
    }

    /**
     * column_definition:
     * data_type [GENERATED ALWAYS] AS (expression)
     * [VIRTUAL | STORED] [UNIQUE [KEY]] [COMMENT comment]
     * [NOT NULL | NULL] [[PRIMARY] KEY]
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.name);
        sb.append(" ").append(getMysqlType());

        sb.append(" AS (").append(this.expr).append(")");
        if (this.isStored) {
            sb.append(" STORED");
        }
        if (this.uniqueIndex) {
            sb.append(" UNIQUE KEY");
        }
        if (this.comment != null && !this.comment.isEmpty()) {
            sb.append(" COMMENT ").append(StringUtils.quoteIdentifier(this.comment, "'", true));
        }
        if (this.notNull != null) {
            sb.append(this.notNull ? " NOT NULL" : " NULL");
        }
        if (this.primaryKey) {
            sb.append(" PRIMARY KEY");
        }

        return sb.toString();
    }
}
