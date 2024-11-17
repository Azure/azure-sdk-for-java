// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.json.implementation.models;

import io.clientcore.core.json.JsonProvider;
import io.clientcore.core.json.contract.models.JsonStringContractTests;
import io.clientcore.core.json.implementation.DefaultJsonProvider;

public class DefaultJsonStringContractTests extends JsonStringContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
