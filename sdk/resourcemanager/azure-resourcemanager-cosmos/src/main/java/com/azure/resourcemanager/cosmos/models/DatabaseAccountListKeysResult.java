// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cosmos.fluent.models.DatabaseAccountListKeysResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An immutable client-side representation of an Azure Cosmos DB DatabaseAccountListKeysResult. */
@Fluent
public interface DatabaseAccountListKeysResult extends HasInnerModel<DatabaseAccountListKeysResultInner> {
    /** @return Base 64 encoded value of the primary read-write key. */
    String primaryMasterKey();

    /** @return Base 64 encoded value of the secondary read-write key. */
    String secondaryMasterKey();

    /** @return Base 64 encoded value of the primary read-only key. */
    String primaryReadonlyMasterKey();

    /** @return Base 64 encoded value of the secondary read-only key. */
    String secondaryReadonlyMasterKey();
}
