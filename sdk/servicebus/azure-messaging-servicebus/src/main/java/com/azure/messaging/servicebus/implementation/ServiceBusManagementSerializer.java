// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.servicebus.implementation.models.ServiceBusManagementError;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serializes and deserializes data plane responses from Service Bus.
 */
public class ServiceBusManagementSerializer implements SerializerAdapter {
    private static final String MINIMUM_DATETIME_FORMATTED = ">0001-01-01T00:00:00Z</";
    private static final Pattern MINIMUM_DATETIME_PATTERN = Pattern.compile("\\>0001\\-01\\-01T00:00:00\\<\\/",
        Pattern.MULTILINE);

    private final JacksonAdapter jacksonAdapter = new JacksonAdapter();
    private final ClientLogger logger = new ClientLogger(ServiceBusManagementSerializer.class);

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        return jacksonAdapter.serialize(object, encoding);
    }

    @Override
    public String serializeRaw(Object object) {
        return jacksonAdapter.serializeRaw(object);
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return jacksonAdapter.serializeList(list, format);
    }

    public <T> T deserialize(String value, Type type) throws IOException {
        final Matcher matcher = MINIMUM_DATETIME_PATTERN.matcher(value);
        final String serializedString;

        // We have to replace matches because service returns a format that is not parsable from OffsetDateTime when
        // entities are created.
        if (matcher.find(0)) {
            logger.verbose("Found instances of '{}' to replace. Value: {}", MINIMUM_DATETIME_PATTERN.pattern(), value);
            serializedString = matcher.replaceAll(matchResult -> MINIMUM_DATETIME_FORMATTED);
        } else {
            serializedString = value;
        }

        return jacksonAdapter.deserialize(serializedString, type, SerializerEncoding.XML);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (encoding != SerializerEncoding.XML) {
            return jacksonAdapter.deserialize(value, type, encoding);
        }

        if (ServiceBusManagementError.class == type) {
            final ServiceBusManagementError error = deserialize(value, type);
            return (T) error;
        }

        return (T) value;
    }

    @Override
    public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
        return jacksonAdapter.deserialize(headers, type);
    }
}
