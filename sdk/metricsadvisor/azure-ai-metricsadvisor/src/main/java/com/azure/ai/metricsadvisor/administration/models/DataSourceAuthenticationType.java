// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for DataSourceAuthenticationType. */
public final class DataSourceAuthenticationType extends ExpandableStringEnum<DataSourceAuthenticationType> {
    /** Static value Basic for DataSourceAuthenticationType. */
    public static final DataSourceAuthenticationType BASIC = fromString("Basic");

    /** Static value ManagedIdentity for DataSourceAuthenticationType. */
    public static final DataSourceAuthenticationType MANAGED_IDENTITY = fromString("ManagedIdentity");

    /** Static value AzureSQLConnectionString for DataSourceAuthenticationType. */
    public static final DataSourceAuthenticationType AZURE_SQL_CONNECTION_STRING
        = fromString("AzureSQLConnectionString");

    /** Static value DataLakeGen2SharedKey for DataSourceAuthenticationType. */
    public static final DataSourceAuthenticationType DATA_LAKE_GEN2_SHARED_KEY = fromString("DataLakeGen2SharedKey");

    /** Static value ServicePrincipal for DataSourceAuthenticationType. */
    public static final DataSourceAuthenticationType SERVICE_PRINCIPAL = fromString("ServicePrincipal");

    /** Static value ServicePrincipalInKV for DataSourceAuthenticationType. */
    public static final DataSourceAuthenticationType SERVICE_PRINCIPAL_IN_KV = fromString("ServicePrincipalInKV");

    /**
     * Creates or finds a AuthenticationTypeEnum from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AuthenticationTypeEnum.
     */
    @JsonCreator
    public static DataSourceAuthenticationType fromString(String name) {
        return fromString(name, DataSourceAuthenticationType.class);
    }

    /** @return known AuthenticationTypeEnum values. */
    public static Collection<DataSourceAuthenticationType> values() {
        return values(DataSourceAuthenticationType.class);
    }
}
