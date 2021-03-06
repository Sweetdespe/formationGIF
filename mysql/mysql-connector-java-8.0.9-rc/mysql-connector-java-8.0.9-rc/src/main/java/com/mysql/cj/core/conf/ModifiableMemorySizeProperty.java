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

import java.io.Serializable;

import com.mysql.cj.api.conf.ModifiableProperty;
import com.mysql.cj.api.conf.PropertyDefinition;
import com.mysql.cj.api.exceptions.ExceptionInterceptor;
import com.mysql.cj.core.exceptions.ExceptionFactory;
import com.mysql.cj.core.exceptions.WrongArgumentException;

public class ModifiableMemorySizeProperty extends ReadableMemorySizeProperty implements ModifiableProperty<Integer>, Serializable {

    private static final long serialVersionUID = -8018059699460539279L;

    private String initialValueAsString;

    protected ModifiableMemorySizeProperty(PropertyDefinition<Integer> propertyDefinition) {
        super(propertyDefinition);
    }

    @Override
    public void setFromString(String value, ExceptionInterceptor exceptionInterceptor) {
        setValue(((MemorySizePropertyDefinition) getPropertyDefinition()).parseObject(value, exceptionInterceptor), value, exceptionInterceptor);
        this.valueAsString = value;
    }

    @Override
    protected void initializeFrom(String extractedValue, ExceptionInterceptor exceptionInterceptor) {
        super.initializeFrom(extractedValue, exceptionInterceptor);
        this.initialValue = this.value;
        this.initialValueAsString = this.valueAsString;
    }

    @Override
    public void setValue(Integer value) {
        setValue(value, null, null);
    }

    @Override
    public void setValue(Integer value, ExceptionInterceptor exceptionInterceptor) {
        setValue(value, null, exceptionInterceptor);
    }

    void setValue(int intValue, String valueAsString, ExceptionInterceptor exceptionInterceptor) {
        if (getPropertyDefinition().isRangeBased()) {
            if ((intValue < getPropertyDefinition().getLowerBound()) || (intValue > getPropertyDefinition().getUpperBound())) {
                throw ExceptionFactory.createException(WrongArgumentException.class,
                        "The connection property '" + getPropertyDefinition().getName() + "' only accepts integer values in the range of "
                                + getPropertyDefinition().getLowerBound() + " - " + getPropertyDefinition().getUpperBound() + ", the value '"
                                + (valueAsString == null ? intValue : valueAsString) + "' exceeds this range.",
                        exceptionInterceptor);
            }
        }

        this.value = Integer.valueOf(intValue);
        this.valueAsString = valueAsString == null ? String.valueOf(intValue) : valueAsString;
        this.wasExplicitlySet = true;
        invokeListeners();
    }

    @Override
    public void resetValue() {
        this.value = this.initialValue;
        this.valueAsString = this.initialValueAsString;
        invokeListeners();
    }

}
