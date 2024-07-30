// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.implementation.models;

import com.azure.json.JsonProvider;
import com.azure.json.contract.models.JsonStringContractTests;
import com.azure.json.implementation.DefaultJsonProvider;

public class DefaultJsonStringContractTests extends JsonStringContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
