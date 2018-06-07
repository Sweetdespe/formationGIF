/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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

package com.mysql.cj.mysqla.authentication;

import java.util.List;

import com.mysql.cj.api.io.Protocol;
import com.mysql.cj.api.mysqla.authentication.AuthenticationPlugin;
import com.mysql.cj.api.mysqla.io.NativeProtocol.IntegerDataType;
import com.mysql.cj.api.mysqla.io.PacketPayload;
import com.mysql.cj.core.util.StringUtils;
import com.mysql.cj.mysqla.io.Buffer;

/**
 * MySQL Clear Password Authentication Plugin
 */
public class MysqlClearPasswordPlugin implements AuthenticationPlugin {

    private Protocol protocol;
    private String password = null;

    @Override
    public void init(Protocol prot) {
        this.protocol = prot;
    }

    public void destroy() {
        this.password = null;
    }

    public String getProtocolPluginName() {
        return "mysql_clear_password";
    }

    public boolean requiresConfidentiality() {
        return true;
    }

    public boolean isReusable() {
        return true;
    }

    public void setAuthenticationParameters(String user, String password) {
        this.password = password;
    }

    public boolean nextAuthenticationStep(PacketPayload fromServer, List<PacketPayload> toServer) {
        toServer.clear();

        String encoding = this.protocol.versionMeetsMinimum(5, 7, 6) ? this.protocol.getPasswordCharacterEncoding() : "UTF-8";
        PacketPayload bresp = new Buffer(StringUtils.getBytes(this.password != null ? this.password : "", encoding));

        bresp.setPosition(bresp.getPayloadLength());
        bresp.writeInteger(IntegerDataType.INT1, 0);
        bresp.setPosition(0);

        toServer.add(bresp);
        return true;
    }

}