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

package com.mysql.cj.core.conf;

import com.mysql.cj.api.conf.RuntimeProperty;
import com.mysql.cj.api.exceptions.ExceptionInterceptor;
import com.mysql.cj.core.exceptions.ExceptionFactory;
import com.mysql.cj.core.exceptions.WrongArgumentException;

public class LongPropertyDefinition extends AbstractPropertyDefinition<Long> {

    private static final long serialVersionUID = -5264490959206230852L;

    public LongPropertyDefinition(String name, String alias, long defaultValue, boolean isRuntimeModifiable, String description, String sinceVersion,
            String category, int orderInCategory) {
        super(name, alias, Long.valueOf(defaultValue), isRuntimeModifiable, description, sinceVersion, category, orderInCategory);
    }

    public LongPropertyDefinition(String name, String alias, long defaultValue, boolean isRuntimeModifiable, String description, String sinceVersion,
            String category, int orderInCategory, long lowerBound, long upperBound) {
        super(name, alias, Long.valueOf(defaultValue), isRuntimeModifiable, description, sinceVersion, category, orderInCategory, (int) lowerBound,
                (int) upperBound);
    }

    @Override
    public Long parseObject(String value, ExceptionInterceptor exceptionInterceptor) {
        try {
            // Parse decimals, too
            return Double.valueOf(value).longValue();

        } catch (NumberFormatException nfe) {
            throw ExceptionFactory.createException(WrongArgumentException.class, "The connection property '" + getName()
                    + "' only accepts long integer values. The value '" + value + "' can not be converted to a long integer.", exceptionInterceptor);
        }
    }

    @Override
    public boolean isRangeBased() {
        return getUpperBound() != getLowerBound();
    }

    /**
     * Creates instance of ReadableLongProperty or ModifiableLongProperty depending on isRuntimeModifiable() result.
     * 
     * @return
     */
    @Override
    public RuntimeProperty<Long> createRuntimeProperty() {
        return isRuntimeModifiable() ? new ModifiableLongProperty(this) : new ReadableLongProperty(this);
    }

}
