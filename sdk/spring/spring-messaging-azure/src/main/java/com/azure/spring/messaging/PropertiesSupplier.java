// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging;

/**
 * An interface to provide properties by providing key.
 */
@FunctionalInterface
public interface PropertiesSupplier<K, V> {

    V getProperties(K key);

}
