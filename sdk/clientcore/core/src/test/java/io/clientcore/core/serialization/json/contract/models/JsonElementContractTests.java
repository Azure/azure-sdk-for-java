// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.json.contract.models;

import io.clientcore.core.serialization.json.JsonProvider;
import io.clientcore.core.serialization.json.models.JsonElement;

/**
 * Tests the contract of {@link JsonElement}.
 * <p>
 * All implementations of {@link JsonProvider} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 */
public abstract class JsonElementContractTests {
    /**
     * Creates an instance {@link JsonProvider} that will be used by a test.
     *
     * @return The {@link JsonProvider} that a test will use.
     */
    protected abstract JsonProvider getJsonProvider();
}
