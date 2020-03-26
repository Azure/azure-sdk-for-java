/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.management.resources.fluentcore.model.Appliable;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.Updatable;
import com.azure.management.sql.models.ServerKeyInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * An immutable client-side representation of an Azure SQL Server Key.
 */
@Fluent
public interface SqlServerKey
    extends
        HasId,
        HasInner<ServerKeyInner>,
        HasName,
        HasResourceGroup,
        Indexable,
        Refreshable<SqlServerKey>,
        Updatable<SqlServerKey.Update> {
    /**
     * @return name of the SQL Server to which this DNS alias belongs
     */
    String sqlServerName();

    /**
     * @return the parent SQL server ID
     */
    String parentId();

    /**
     * @return  the kind of encryption protector; this is metadata used for the Azure Portal experience
     */
    String kind();

    /**
     * @return  the resource location
     */
    Region region();

    /**
     * @return the server key type
     */
    ServerKeyType serverKeyType();

    /**
     * @return the URI of the server key
     */
    String uri();

    /**
     * @return  the thumbprint of the server key
     */
    String thumbprint();

    /**
     * @return the server key creation date
     */
    OffsetDateTime creationDate();

    /**
     * Deletes the SQL Server Key.
     */
    void delete();

    /**
     * Deletes the SQL Server Key asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

    /**
     * The template for a SQL Server Key update operation, containing all the settings that can be modified.
     */
    interface Update extends
        SqlServerKey.UpdateStages.WithThumbprint,
        SqlServerKey.UpdateStages.WithCreationDate,
        Appliable<SqlServerKey> {
    }

    /**
     * Grouping of all the SQL Server Key update stages.
     */
    interface UpdateStages {
        /**
         * The SQL Server Key definition to set the thumbprint.
         */
        interface WithThumbprint {
            /**
             * Sets the thumbprint of the server key.
             *
             * @param thumbprint the thumbprint of the server key
             * @return The next stage of the definition.
             */
            SqlServerKey.Update withThumbprint(String thumbprint);
        }

        /**
         * The SQL Server Key definition to set the server key creation date.
         */
        interface WithCreationDate {
            /**
             * Sets the server key creation date.
             *
             * @param creationDate the server key creation date
             * @return The next stage of the definition.
             */
            SqlServerKey.Update withCreationDate(OffsetDateTime creationDate);
        }
    }
}
