/*
 * Copyright (c) 2002, 2015, Oracle and/or its affiliates. All rights reserved.
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

package com.mysql.cj.jdbc.integration.jboss;

import java.sql.SQLException;

import org.jboss.resource.adapter.jdbc.vendor.MySQLExceptionSorter;

/**
 * Exception sorter used for JBoss to make recovery of downed/stale connections work more consistently.
 */
public final class ExtendedMysqlExceptionSorter extends MySQLExceptionSorter {

    static final long serialVersionUID = -2454582336945931069L;

    @Override
    public boolean isExceptionFatal(SQLException ex) {
        String sqlState = ex.getSQLState();

        if (sqlState != null && sqlState.startsWith("08")) {
            return true;
        }

        return super.isExceptionFatal(ex);
    }

}
