// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.HashMap;
import java.util.Map;

/** The Streaming Data factory class for the parsers. */
class StreamingDataFactory {
    private static final Map<String, StreamingDataParser<? extends StreamingData>> PARSERS = new HashMap<>();
 
    static {
        PARSERS.put("audioData", new AudioData.Parser());
        PARSERS.put("transcriptionData", new TranscriptionData.Parser());
        PARSERS.put("transcriptionMetadata", new TranscriptionData.Parser());
        PARSERS.put("Metadata", new TranscriptionData.Parser());
    }

    /**
     *  get the parser based on the streaming data kind type
     * @param <T> subtype of streaming data
     * @param type type of streaming data
     * @return the parser
     * @throws  IllegalArgumentException if the input is invalid
    */
    @SuppressWarnings("unchecked")
    static <T extends StreamingData> StreamingDataParser<T> getParser(String type) {
        StreamingDataParser<?> parser = PARSERS.get(type);
        if (parser == null) {
            throw new IllegalArgumentException("No parser registered for type: " + type);
        }

        return (StreamingDataParser<T>) parser;
    }
}
