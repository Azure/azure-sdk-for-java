// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

interface SerializerDefaultMappings {

    boolean deserializeAsBoolean(byte[] bytes);

    Double deserializeAsDouble(byte[] bytes);

    String deserializeAsString(byte[] bytes);

    byte[] serializeBoolean(boolean val);

    byte[] serializeDouble(double val);

    byte[] serializeString(String val);
}
