// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;

public class CosmosParameter extends Parameter {

    public CosmosParameter(MethodParameter parameter) {
        super(parameter);
    }

    @Override
    public boolean isSpecialParameter() {
        return super.isSpecialParameter();
    }
}
