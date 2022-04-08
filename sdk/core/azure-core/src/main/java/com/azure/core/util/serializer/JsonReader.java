// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.io.Closeable;

/**
 * Reads a JSON encoded value as a stream of tokens.
 */
public interface JsonReader extends Closeable {
    JsonToken currentToken();
    JsonToken nextToken();
    boolean getBooleanValue();
    double getDoubleValue();
    int getIntValue();
    long getLongValue();
    String getStringValue();
}
