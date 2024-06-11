// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.implementation.models;

import com.azure.json.JsonProvider;
import com.azure.json.contract.models.JsonNullContractTests;
import com.azure.json.implementation.DefaultJsonProvider;

public class DefaultJsonNullContractTests extends JsonNullContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
