// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.util.BinaryData;

public class MultipartDataSerializationResult {

    private final long dataLength;
    private final BinaryData data;

    public MultipartDataSerializationResult(BinaryData data, long contentLength) {
        this.dataLength = contentLength;
        this.data = data;
    }

    public BinaryData getData() {
        return data;
    }

    public long getDataLength() {
        return dataLength;
    }

}
