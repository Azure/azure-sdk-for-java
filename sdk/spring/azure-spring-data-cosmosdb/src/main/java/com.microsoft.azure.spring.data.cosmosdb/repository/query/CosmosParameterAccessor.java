// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import org.springframework.data.repository.query.ParameterAccessor;

public interface CosmosParameterAccessor extends ParameterAccessor {
    Object[] getValues();
}
