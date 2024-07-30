// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.json.implementation;

import io.clientcore.core.json.JsonProvider;
import io.clientcore.core.json.contract.JsonProviderContractTests;

/**
 * Tests {@link DefaultJsonProvider} against the contract required by {@link JsonProvider}.
 */
public class DefaultJsonProviderContractTests extends JsonProviderContractTests {
    @Override
    protected JsonProvider getJsonProvider() {
        return new DefaultJsonProvider();
    }
}
