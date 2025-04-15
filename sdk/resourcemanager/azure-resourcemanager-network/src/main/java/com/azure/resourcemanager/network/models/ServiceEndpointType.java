// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for ServiceEndpointType. */
public final class ServiceEndpointType extends ExpandableStringEnum<ServiceEndpointType> {
    /** Static value Microsoft.Storage for ServiceEndpointType. */
    public static final ServiceEndpointType MICROSOFT_STORAGE = fromString("Microsoft.Storage");

    /** Static value Microsoft.Sql for ServiceEndpointType. */
    public static final ServiceEndpointType MICROSOFT_SQL = fromString("Microsoft.Sql");

    /** Static value Microsoft.AzureCosmosDB for ServiceEndpointType. */
    public static final ServiceEndpointType MICROSOFT_AZURECOSMOSDB = fromString("Microsoft.AzureCosmosDB");

    /** Static value Microsoft.Web for ServiceEndpointType. */
    public static final ServiceEndpointType MICROSOFT_WEB = fromString("Microsoft.Web");

    /**
     * Creates a new instance of ServiceEndpointType value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ServiceEndpointType() {
    }

    /**
     * Creates or finds a ServiceEndpointType from its string representation.
     *
     * @param name a name to look for
     * @return the corresponding ServiceEndpointType
     */
    public static ServiceEndpointType fromString(String name) {
        return fromString(name, ServiceEndpointType.class);
    }

    /**
     * Gets known ServiceEndpointType values.
     *
     * @return known ServiceEndpointType values
     */
    public static Collection<ServiceEndpointType> values() {
        return values(ServiceEndpointType.class);
    }
}
