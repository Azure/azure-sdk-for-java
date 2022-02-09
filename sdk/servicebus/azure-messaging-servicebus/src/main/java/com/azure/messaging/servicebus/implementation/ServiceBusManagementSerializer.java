// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.servicebus.implementation.models.CreateQueueBody;
import com.azure.messaging.servicebus.implementation.models.CreateRuleBody;
import com.azure.messaging.servicebus.implementation.models.CreateSubscriptionBody;

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
    private static final Pattern MINIMUM_DATETIME_PATTERN = Pattern.compile(">0001-01-01T00:00:00</",
        Pattern.MULTILINE);
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile(
        "xmlns:(?<namespace>\\w+)=\"http://schemas\\.microsoft\\.com/netservices/2010/10/servicebus/connect\"",
        Pattern.MULTILINE);
    private static final Pattern FILTER_ACTION_PATTERN = Pattern.compile("<(Filter|Action) type=",
        Pattern.MULTILINE);
    private static final Pattern FILTER_VALUE_PATTERN = Pattern.compile("<(Value)",
        Pattern.MULTILINE);
    private static final String RULE_VALUE_ATTRIBUTE_XML = "<$1 xmlns:d6p1=\"http://www.w3.org/2001/XMLSchema\" ns0:type=\"d6p1:string\"";
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    private final ClientLogger logger = new ClientLogger(ServiceBusManagementSerializer.class);

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        final String contents = SERIALIZER_ADAPTER.serialize(object, encoding);

        final Class<?> clazz = object.getClass();
        if (!CreateQueueBody.class.equals(clazz) && !CreateRuleBody.class.equals(clazz)
            && !CreateSubscriptionBody.class.equals(clazz)) {
            return contents;
        }

        // This hack exists because the service requires a global namespace for the XML rather than allowing
        // each XML element to be prefaced with an explicit namespace. For example:
        // xmlns="foo" works because "foo" is assigned the global namespace.
        // xmlns:ns0="foo", and then prefixing all elements with ns0:AuthorizationRule will break.
        final Matcher namespaceMatcher = NAMESPACE_PATTERN.matcher(contents);
        if (!namespaceMatcher.find()) {
            logger.warning("Could not find {} in {}", NAMESPACE_PATTERN.pattern(), contents);
            return contents;
        }

        final String namespace = namespaceMatcher.group("namespace");
        String replaced = contents
            .replaceAll(namespace + ":", "")
            .replace("xmlns:" + namespace + "=", "xmlns=");

        if (!CreateRuleBody.class.equals(clazz)) {
            return replaced;
        }

        // This hack is here because value of custom property within RuleFilter should have a namespace like xmlns:d6p1="http://www.w3.org/2001/XMLSchema" ns0:type="d6p1:string".
        if (CreateRuleBody.class.equals(clazz)) {
            final Matcher filterValue = FILTER_VALUE_PATTERN.matcher(replaced);
            if (filterValue.find()) {
                replaced = filterValue.replaceAll(RULE_VALUE_ATTRIBUTE_XML);
            } else {
                logger.warning("Could not find filter name pattern '{}' in {}.", FILTER_VALUE_PATTERN.pattern(),
                    contents);
            }
        }

        // This hack is here because RuleFilter and RuleAction type="Foo" should have a namespace like n0:type="Foo".
        final Matcher filterType = FILTER_ACTION_PATTERN.matcher(replaced);
        if (filterType.find()) {
            return filterType.replaceAll("<$1 xmlns:ns0=\"http://www.w3.org/2001/XMLSchema-instance\" ns0:type=");
        } else {
            logger.warning("Could not find filter name pattern '{}' in {}.", FILTER_ACTION_PATTERN.pattern(),
                contents);
            return replaced;
        }
    }

    @Override
    public String serializeRaw(Object object) {
        return SERIALIZER_ADAPTER.serializeRaw(object);
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return SERIALIZER_ADAPTER.serializeList(list, format);
    }

    public <T> T deserialize(String value, Type type) throws IOException {
        final Matcher matcher = MINIMUM_DATETIME_PATTERN.matcher(value);
        final String serializedString;

        // We have to replace matches because service returns a format that is not parsable from OffsetDateTime when
        // entities are created.
        if (matcher.find(0)) {
            logger.verbose("Found instances of '{}' to replace. Value: {}", MINIMUM_DATETIME_PATTERN.pattern(), value);
            serializedString = matcher.replaceAll(MINIMUM_DATETIME_FORMATTED);
        } else {
            serializedString = value;
        }

        return SERIALIZER_ADAPTER.deserialize(serializedString, type, SerializerEncoding.XML);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (encoding != SerializerEncoding.XML) {
            return SERIALIZER_ADAPTER.deserialize(value, type, encoding);
        }

        if (Object.class == type) {
            return (T) value;
        } else {
            return (T) deserialize(value, type);
        }
    }

    @Override
    public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
        return SERIALIZER_ADAPTER.deserialize(headers, type);
    }
}
