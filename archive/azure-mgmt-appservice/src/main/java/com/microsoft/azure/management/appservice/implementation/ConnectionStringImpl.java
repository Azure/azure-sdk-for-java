/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.ConnStringValueTypePair;
import com.microsoft.azure.management.appservice.ConnectionString;
import com.microsoft.azure.management.appservice.ConnectionStringType;

/**
 * An immutable client-side representation of a connection string on a web app.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
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
