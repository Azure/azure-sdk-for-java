// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.json.implementation.models;

import io.clientcore.core.serialization.json.JsonProvider;
import io.clientcore.core.serialization.json.implementation.DefaultJsonProvider;
import io.clientcore.core.serialization.json.contract.models.JsonStringContractTests;

public class DefaultJsonStringContractTests extends JsonStringContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
