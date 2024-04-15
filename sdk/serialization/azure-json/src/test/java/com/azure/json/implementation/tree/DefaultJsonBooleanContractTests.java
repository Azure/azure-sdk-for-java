// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.implementation.tree;

import com.azure.json.JsonProvider;
import com.azure.json.contract.tree.JsonBooleanContractTests;
import com.azure.json.implementation.DefaultJsonProvider;

public class DefaultJsonBooleanContractTests extends JsonBooleanContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
