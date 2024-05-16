// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

/**
 * Transformer for the CTL workload too allow using the same representation to be used
 * in the application layer and the storage layer e.g. JsonNode
 */
public class IdentityDocumentTransformer<T> implements DocumentTransformer<T, T> {
    @Override
    public T serialize(T object) {
        return object;
    }

    @Override
    public T deserialize(T serializedObject) {
        return serializedObject;
    }
}
