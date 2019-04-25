// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.management;

import java.time.Instant;

/**
 * Represents the metadata related to a service bus namespace.
 */
public class NamespaceInfo {
    private String name;
    private NamespaceType namespaceType;
    private Instant createdAt;
    private Instant modifiedAt;
    private NamespaceSku namespaceSku;
    private int messagingUnits;
    private String alias;

    /**
     * @return Gets the name of namespace.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name - Name of the namespace.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Gets the type of entities present in the namespace.
     */
    public NamespaceType getNamespaceType() {
        return namespaceType;
    }

    /**
     * @param namespaceType - Sets the namespace type.
     */
    public void setNamespaceType(NamespaceType namespaceType) {
        this.namespaceType = namespaceType;
    }

    /**
     * @return - Gets the instant at which the namespace was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt - Sets the instant at which the namespace was created.
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return - Gets the instant at which the namespace was modified.
     */
    public Instant getModifiedAt() {
        return modifiedAt;
    }

    /**
     * @param modifiedAt - Sets the instant at which the namespace was modified.
     */
    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    /**
     * @return - Gets the SKU/tier of the namespace. Not valid only for {@link NamespaceType#Unknown}
     */
    public NamespaceSku getNamespaceSku() {
        return namespaceSku;
    }

    /**
     * @param namespaceSku - SKU of the namespace
     */
    public void setNamespaceSku(NamespaceSku namespaceSku) {
        this.namespaceSku = namespaceSku;
    }

    /**
     * @return - Gets the number of messaging units allocated for namespace. Valid only for {@link NamespaceSku#Premium}
     */
    public int getMessagingUnits() {
        return messagingUnits;
    }

    /**
     * @param messagingUnits - Number of messaging units allocated for namespace.
     */
    public void setMessagingUnits(int messagingUnits) {
        this.messagingUnits = messagingUnits;
    }

    /**
     * @return - Gets the alias set for the namespace (if any).
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias - alias for the namespace.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
}
