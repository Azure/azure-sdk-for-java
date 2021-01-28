// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.sastokens;

import java.util.Locale;

import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ACCOUNT_CREATE_DATABASES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ACCOUNT_DELETE_DATABASES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ACCOUNT_LIST_DATABASES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ACCOUNT_READ_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ACCOUNT_READ_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ACCOUNT_WRITE_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINERS_READ_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINERS_WRITE_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_DELETE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_OFFER_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_REPLACE_OFFER_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_REPLACE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_CREATE_CONTAINERS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_DELETE_CONTAINERS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_DELETE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_LIST_CONTAINERS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_READ_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_READ_OFFER_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_READ_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_REPLACE_OFFER_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_DATABASE_WRITE_ALL_ACCESS_VALUE;

/**
 * Represents permission scope settings applicable to control plane related operations.
 */
public enum ControlPlanePermissionScope {
    // REQUIRED: This enum must be kept in sync with the ControlPlanePermissionScope enum in backend services.

    /**
     * Cosmos account read scope.
     */
    SCOPE_ACCOUNT_READ("AccountRead", SCOPE_ACCOUNT_READ_VALUE),
    SCOPE_ACCOUNT_LIST_DATABASES("AccountListDatabases", SCOPE_ACCOUNT_LIST_DATABASES_VALUE),

    /**
     * Cosmos database read scope.
     */
    SCOPE_DATABASE_READ("DatabaseRead", SCOPE_DATABASE_READ_VALUE),
    SCOPE_DATABASE_READ_OFFER("DatabaseReadOffer", SCOPE_DATABASE_READ_OFFER_VALUE),
    SCOPE_DATABASE_LIST_CONTAINERS("DatabaseListContainers", SCOPE_DATABASE_LIST_CONTAINERS_VALUE),

    /**
     * Cosmos Container read scope.
     */
    SCOPE_CONTAINER_READ("ContainerRead", SCOPE_CONTAINER_READ_VALUE),
    SCOPE_CONTAINER_READ_OFFER("ContainerReadOffer", SCOPE_CONTAINER_READ_OFFER_VALUE),

    /**
     * Composite read scopes.
     */
    SCOPE_ACCOUNT_READ_ALL_ACCESS("AccountReadAllAccess", SCOPE_ACCOUNT_READ_ALL_ACCESS_VALUE),
    SCOPE_DATABASE_READ_ALL_ACCESS("DatabaseReadAllAccess", SCOPE_DATABASE_READ_ALL_ACCESS_VALUE),
    SCOPE_CONTAINER_READ_ALL_ACCESS("ContainersReadAllAccess", SCOPE_CONTAINERS_READ_ALL_ACCESS_VALUE),

    /**
     * Cosmos account write scope.
     */
    SCOPE_ACCOUNT_CREATE_DATABASES("AccountCreateDatabases", SCOPE_ACCOUNT_CREATE_DATABASES_VALUE),
    SCOPE_ACCOUNT_DELETE_DATABASES("AccountDeleteDatabases", SCOPE_ACCOUNT_DELETE_DATABASES_VALUE),

    /**
     * Cosmos database write scope.
     */
    SCOPE_DATABASE_DELETE("DatabaseDelete", SCOPE_DATABASE_DELETE_VALUE),
    SCOPE_DATABASE_REPLACE_OFFER("DatabaseReplaceOffer", SCOPE_DATABASE_REPLACE_OFFER_VALUE),
    SCOPE_DATABASE_CREATE_CONTAINERS("DatabaseCreateContainers", SCOPE_DATABASE_CREATE_CONTAINERS_VALUE),
    SCOPE_DATABASE_DELETE_CONTAINERS("DatabaseDeleteContainers", SCOPE_DATABASE_DELETE_CONTAINERS_VALUE),

    /**
     * Cosmos Container write scope.
     */
    SCOPE_CONTAINER_REPLACE("ContainerReplace", SCOPE_CONTAINER_REPLACE_VALUE),
    SCOPE_CONTAINER_DELETE("ContainerDelete", SCOPE_CONTAINER_DELETE_VALUE),
    SCOPE_CONTAINER_REPLACE_OFFER("ContainerReplaceOffer", SCOPE_CONTAINER_REPLACE_OFFER_VALUE),

    /**
     * Composite write scopes.
     */
    SCOPE_ACCOUNT_WRITE_ALL_ACCESS("AccountFullAllAccess", SCOPE_ACCOUNT_WRITE_ALL_ACCESS_VALUE),
    SCOPE_DATABASE_WRITE_ALL_ACCESS("DatabaseWriteAllAccess", SCOPE_DATABASE_WRITE_ALL_ACCESS_VALUE),
    SCOPE_CONTAINER_WRITE_ALL_ACCESS("ContainersWriteAllAccess", SCOPE_CONTAINERS_WRITE_ALL_ACCESS_VALUE),

    NONE("None", (short) 0x0);


    private final short value;
    private final String stringValue;
    private final String toLowerStringValue;

    ControlPlanePermissionScope(String stringValue, short scopeBitMask) {
        this.stringValue = stringValue;
        this.toLowerStringValue = stringValue.toLowerCase(Locale.ROOT);
        this.value = scopeBitMask;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }

    public String toLowerCase() {
        return this.toLowerStringValue;
    }

    public short value() {
        return this.value;
    }
}
