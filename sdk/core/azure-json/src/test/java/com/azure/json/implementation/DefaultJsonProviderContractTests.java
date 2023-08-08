// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.implementation;

import com.azure.json.JsonProvider;
import com.azure.json.contract.JsonProviderContractTests;

/**
 * Tests {@link DefaultJsonProvider} against the contract required by {@link JsonProvider}.
 */
public class DefaultJsonProviderContractTests extends JsonProviderContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
