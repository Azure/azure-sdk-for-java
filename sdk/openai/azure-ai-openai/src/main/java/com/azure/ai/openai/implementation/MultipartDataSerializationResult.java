// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.util.BinaryData;

/**
 * This class is used as a stand-in representation of marshalled data to be used in an HTTP multipart request.
 */
public class MultipartDataSerializationResult {

    /**
     * Represents the length of the content of this request. The value is to be used for the "Content-Length" header
     * of the HTTP request
     */
    private final long dataLength;

    /**
     * The multipart form data of the request.
     */
    private final BinaryData data;

    /**
     * Constructor bundling both data and its length
     * @param data the multipart form data of the request
     * @param contentLength the length of the multipart form data of the request
     */
    public MultipartDataSerializationResult(BinaryData data, long contentLength) {
        this.dataLength = contentLength;
        this.data = data;
    }

    /**
     *
     * @return the result of marshaling a multipart HTTP request
     */
    public BinaryData getData() {
        return data;
    }

    /**
     *
     * @return the length of a multipart HTTP request data
     */
    public long getDataLength() {
        return dataLength;
    }

}
