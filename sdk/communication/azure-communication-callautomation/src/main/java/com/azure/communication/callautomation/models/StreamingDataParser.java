// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.json.JsonReader;

/** Streaming data parser class */
public interface StreamingDataParser<T extends StreamingData> {

    /**
     * Parse method for streaming data 
     * @param jsonReader data from the socket
     * @return returns the subtypes of streamingdata
     * @throws IOException throws the exception if parsing fails
    */
    T parse(JsonReader jsonReader) throws IOException;
}
