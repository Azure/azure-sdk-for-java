// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.json.implementation;

import io.clientcore.json.JsonProvider;
import io.clientcore.json.contract.JsonProviderContractTests;

/**
 * Tests {@link DefaultJsonProvider} against the contract required by {@link JsonProvider}.
 */
public class DefaultJsonProviderContractTests extends JsonProviderContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
