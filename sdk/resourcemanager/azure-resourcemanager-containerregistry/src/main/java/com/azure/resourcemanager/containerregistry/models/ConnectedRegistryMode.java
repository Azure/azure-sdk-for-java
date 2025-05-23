// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The mode of the connected registry resource that indicates the permissions of the registry.
 */
public final class ConnectedRegistryMode extends ExpandableStringEnum<ConnectedRegistryMode> {
    /**
     * Static value ReadWrite for ConnectedRegistryMode.
     */
    public static final ConnectedRegistryMode READ_WRITE = fromString("ReadWrite");

    /**
     * Static value ReadOnly for ConnectedRegistryMode.
     */
    public static final ConnectedRegistryMode READ_ONLY = fromString("ReadOnly");

    /**
     * Static value Registry for ConnectedRegistryMode.
     */
    public static final ConnectedRegistryMode REGISTRY = fromString("Registry");

    /**
     * Static value Mirror for ConnectedRegistryMode.
     */
    public static final ConnectedRegistryMode MIRROR = fromString("Mirror");

    /**
     * Creates a new instance of ConnectedRegistryMode value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ConnectedRegistryMode() {
    }

    /**
     * Creates or finds a ConnectedRegistryMode from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding ConnectedRegistryMode.
     */
    public static ConnectedRegistryMode fromString(String name) {
        return fromString(name, ConnectedRegistryMode.class);
    }

    /**
     * Gets known ConnectedRegistryMode values.
     * 
     * @return known ConnectedRegistryMode values.
     */
    public static Collection<ConnectedRegistryMode> values() {
        return values(ConnectedRegistryMode.class);
    }
}
