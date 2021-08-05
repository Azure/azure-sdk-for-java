// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuationDeserializer;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternalDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Locale;

public class ChangeFeedStateDeserializer extends StdDeserializer<ChangeFeedState> {

    private static final long serialVersionUID = 1L;

    public ChangeFeedStateDeserializer() {
        this(null);
    }

    public ChangeFeedStateDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public ChangeFeedState deserialize(final JsonParser parser,
                                       final DeserializationContext context)
        throws IOException {

        final JsonNode rootNode = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        JsonNode versionNode = rootNode.get(Constants.Properties.CHANGE_FEED_STATE_VERSION);
        if (versionNode == null || !versionNode.isInt()) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Required property '%s' missing or does not have a valid value.",
                    Constants.Properties.CHANGE_FEED_STATE_VERSION));
        }
        int version = versionNode.intValue();
        if (version != ChangeFeedStateVersions.V1) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Version '%d' not supported.",
                    version));
        }

        JsonNode ridNode = rootNode.get(Constants.Properties.CHANGE_FEED_STATE_RESOURCE_ID);
        if (ridNode == null || !ridNode.isTextual() || Strings.isNullOrWhiteSpace(ridNode.textValue())) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Required property '%s' missing or does not have a valid value.",
                    Constants.Properties.CHANGE_FEED_STATE_RESOURCE_ID));
        }

        JsonNode modeNode = rootNode.get(Constants.Properties.CHANGE_FEED_STATE_MODE);
        if (modeNode == null || !modeNode.isTextual() || Strings.isNullOrWhiteSpace(modeNode.textValue())) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Required property '%s' missing or does not have a valid value.",
                    Constants.Properties.CHANGE_FEED_STATE_MODE));
        }
        ChangeFeedMode mode;

        try {
            mode = ChangeFeedMode.valueOf(modeNode.textValue().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException argException) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Value '%s' for required property '%s' does not have a valid value.",
                    modeNode.textValue(),
                    Constants.Properties.CHANGE_FEED_STATE_MODE),
                argException);
        }

        JsonNode startFromNode = rootNode.get(Constants.Properties.CHANGE_FEED_STATE_START_FROM);
        if (startFromNode == null || !startFromNode.isObject()) {
            throw JsonMappingException.from(
                parser,
                String.format(
                    "Required property '%s' missing or does not have a valid value.",
                    Constants.Properties.CHANGE_FEED_STATE_START_FROM));
        }

        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternalDeserializer
            .deserializeStartFromInternal(startFromNode, mapper, parser);

        FeedRangeInternal feedRange;
        FeedRangeContinuation continuation = null;
        JsonNode continuationNode =
            rootNode.get(Constants.Properties.CHANGE_FEED_STATE_CONTINUATION);
        if (continuationNode != null) {
            continuation = FeedRangeContinuationDeserializer
                .deserializeFeedRangeContinuation(continuationNode, mapper, parser);
            feedRange = continuation.getFeedRange();
        } else {
            feedRange = FeedRangeInternalDeserializer
                .deserializeFeedRange(rootNode, mapper, parser);
        }

        return new ChangeFeedStateV1(
            ridNode.textValue(),
            feedRange,
            mode,
            startFromSettings,
            continuation);
    }
}