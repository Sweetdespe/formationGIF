/*
 * Copyright (c) 2016, 2017, Oracle and/or its affiliates. All rights reserved.
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

import java.util.UUID;

public class DocumentID {

    /**
     * An X DevAPI Document ID is a string of up to 32 characters in length,
     * and has a format based on the RFC 4122 specification (version 1, variant 1),
     * with a modification to satisfy the requirement for a stable ID prefix. When an X DevAPI Document ID
     * is generated by Connector/J, it is a hexadecimal representation of a 16-byte UUID value in lowercase
     * hexadecimal digits, without dashes.
     * <p>
     * The original UUID specification has the following format, as described in
     * <a href="http://dev.mysql.com/doc/refman/5.7/en/miscellaneous-functions.html#function_uuid">UUID()</a>
     * <p>
     * aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee
     * <p>
     * where aaaaaaaa is the lower part of the timestamp and eeeeeeeeeeee is the MAC
     * address of the host. An X DevAPI Document ID has an inverted component order and no dashes, so the format becomes:
     * <p>
     * eeeeeeeeeeeeddddccccbbbbaaaaaaaa
     * <p>
     * Example:
     * <p>
     * RFC 4122 UUID: 5c99cdfe-48cb-11e6-94f3-4a383b7fcc8
     * <p>
     * X DevAPI Document ID: 4a383b7fcc894f311e648cb5c99cdfe
     *
     * @return X DevAPI Document ID
     */
    public static String generate() {
        UUID uuid = UUID.randomUUID();
        return (getDigits(uuid.getLeastSignificantBits(), 12) + //
                getDigits(uuid.getLeastSignificantBits() >> 48, 4) + //
                getDigits(uuid.getMostSignificantBits(), 4) + //
                getDigits(uuid.getMostSignificantBits() >> 16, 4) + //
                getDigits(uuid.getMostSignificantBits() >> 32, 8));
    }

    private static String getDigits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

}
