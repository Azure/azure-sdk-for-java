// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.ConnStringValueTypePair;
import com.azure.management.appservice.ConnectionString;
import com.azure.management.appservice.ConnectionStringType;

/** An immutable client-side representation of a connection string on a web app. */
public class ConnectionStringImpl implements ConnectionString {
    private String name;
    private ConnStringValueTypePair valueTypePair;
    private boolean sticky;

    ConnectionStringImpl(String name, ConnStringValueTypePair valueTypePair, boolean sticky) {
        this.name = name;
        this.valueTypePair = valueTypePair;
        this.sticky = sticky;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return valueTypePair.value();
    }

    @Override
    public ConnectionStringType type() {
        return valueTypePair.type();
    }

    @Override
    public boolean sticky() {
        return sticky;
    }
}
