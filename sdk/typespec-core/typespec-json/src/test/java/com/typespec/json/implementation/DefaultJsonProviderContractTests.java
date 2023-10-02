// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.implementation;

import com.typespec.json.JsonProvider;
import com.typespec.json.contract.JsonProviderContractTests;

/**
 * Tests {@link DefaultJsonProvider} against the contract required by {@link JsonProvider}.
 */
public class DefaultJsonProviderContractTests extends JsonProviderContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
