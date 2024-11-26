// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.json.implementation.models;

import io.clientcore.core.json.JsonProvider;
import io.clientcore.core.json.contract.models.JsonArrayContractTests;
import io.clientcore.core.json.implementation.DefaultJsonProvider;

public class DefaultJsonArrayContractTests extends JsonArrayContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
