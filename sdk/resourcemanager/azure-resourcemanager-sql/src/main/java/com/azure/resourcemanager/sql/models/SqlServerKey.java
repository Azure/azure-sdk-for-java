// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.fluent.models.ServerKeyInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL Server Key. */
@Fluent
public interface SqlServerKey
    extends HasId, HasInnerModel<ServerKeyInner>, HasName, HasResourceGroup, Indexable, Refreshable<SqlServerKey> {
    /**
     * Gets the name of the SQL Server to which this DNS alias belongs.
     *
     * @return name of the SQL Server to which this DNS alias belongs
     */
    String sqlServerName();

    /**
     * Gets the parent SQL server ID.
     *
     * @return the parent SQL server ID
     */
    String parentId();

    /**
     * Gets the kind of encryption protector.
     *
     * @return the kind of encryption protector; this is metadata used for the Azure Portal experience
     */
    String kind();

    /**
     * Gets the resource location.
     *
     * @return the resource location
     */
    Region region();

    /**
     * Gets the server key type.
     *
     * @return the server key type
     */
    ServerKeyType serverKeyType();

    /**
     * Gets the URI of the server key.
     *
     * @return the URI of the server key
     */
    String uri();

    /**
     * Gets the thumbprint of the server key.
     *
     * @return the thumbprint of the server key
     */
    String thumbprint();

    /**
     * Gets the server key creation date.
     *
     * @return the server key creation date
     */
    OffsetDateTime creationDate();

    /** Deletes the SQL Server Key. */
    void delete();

    /**
     * Deletes the SQL Server Key asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();
}
