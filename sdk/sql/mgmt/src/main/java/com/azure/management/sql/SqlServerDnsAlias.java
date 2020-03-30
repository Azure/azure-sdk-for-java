/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.sql.models.ServerDnsAliasInner;
import reactor.core.publisher.Mono;

/**
 * An immutable client-side representation of an Azure SQL Server DNS alias.
 */
@Fluent
public interface SqlServerDnsAlias
    extends
        HasId,
        HasInner<ServerDnsAliasInner>,
        HasName,
        HasResourceGroup,
        Indexable,
        Refreshable<SqlServerDnsAlias> {
    /**
     * @return name of the SQL Server to which this DNS alias belongs
     */
    String sqlServerName();

    /**
     * @return the fully qualified DNS record for alias
     */
    String azureDnsRecord();

    /**
     * @return the parent SQL server ID
     */
    String parentId();

    /**
     * Deletes the DNS alias.
     */
    void delete();

    /**
     * Deletes the DNS alias asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

}
