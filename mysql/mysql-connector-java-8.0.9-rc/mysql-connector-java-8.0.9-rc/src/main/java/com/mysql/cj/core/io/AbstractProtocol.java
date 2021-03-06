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

package com.mysql.cj.core.io;

import java.util.LinkedList;

import com.mysql.cj.api.Session;
import com.mysql.cj.api.authentication.AuthenticationProvider;
import com.mysql.cj.api.conf.PropertySet;
import com.mysql.cj.api.exceptions.ExceptionInterceptor;
import com.mysql.cj.api.io.PacketReceivedTimeHolder;
import com.mysql.cj.api.io.PacketSentTimeHolder;
import com.mysql.cj.api.io.Protocol;
import com.mysql.cj.api.io.SocketConnection;
import com.mysql.cj.api.log.Log;

public abstract class AbstractProtocol implements Protocol {

    protected Session session;
    protected SocketConnection socketConnection;

    protected PropertySet propertySet;

    /** The logger we're going to use */
    protected transient Log log;

    protected ExceptionInterceptor exceptionInterceptor;

    protected AuthenticationProvider authProvider;

    // Default until packet sender created
    private PacketSentTimeHolder packetSentTimeHolder = new PacketSentTimeHolder() {
        public long getLastPacketSentTime() {
            return 0;
        }
    };
    private PacketReceivedTimeHolder packetReceivedTimeHolder = new PacketReceivedTimeHolder() {
        public long getLastPacketReceivedTime() {
            return 0;
        }
    };

    protected LinkedList<StringBuilder> packetDebugRingBuffer = null;

    public SocketConnection getSocketConnection() {
        return this.socketConnection;
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return this.authProvider;
    }

    public ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }

    public PacketSentTimeHolder getPacketSentTimeHolder() {
        return this.packetSentTimeHolder;
    }

    public void setPacketSentTimeHolder(PacketSentTimeHolder packetSentTimeHolder) {
        this.packetSentTimeHolder = packetSentTimeHolder;
    }

    public PacketReceivedTimeHolder getPacketReceivedTimeHolder() {
        return this.packetReceivedTimeHolder;
    }

    public void setPacketReceivedTimeHolder(PacketReceivedTimeHolder packetReceivedTimeHolder) {
        this.packetReceivedTimeHolder = packetReceivedTimeHolder;
    }

    public PropertySet getPropertySet() {
        return this.propertySet;
    }

    public void setPropertySet(PropertySet propertySet) {
        this.propertySet = propertySet;
    }

}
