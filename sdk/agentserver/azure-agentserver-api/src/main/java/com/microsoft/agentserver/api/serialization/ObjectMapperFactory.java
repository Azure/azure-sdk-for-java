// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.serialization;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.openai.core.JsonNull;
import com.openai.core.KnownValue;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreatedEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Factory for a pre-configured {@link ObjectMapper} suitable for serializing
 * OpenAI SDK model types to JSON.
 * <p>
 * The mapper is configured to:
 * <ul>
 *   <li>Exclude null values from serialization</li>
 *   <li>Skip {@link JsonNull} sentinel fields emitted by the OpenAI SDK</li>
 *   <li>Serialize {@code createdAt} timestamps as integers via mixin</li>
 *   <li>Hide the internal {@code valid}/{@code isValid} validation properties</li>
 * </ul>
 * <p>
 * This class is framework-agnostic. For JAX-RS integration, use the
 * {@code ObjectMapperProvider} from the {@code java-agent-server-api-jaxrs} module.
 */
public final class ObjectMapperFactory {

    private static final ObjectMapper INSTANCE = createMapper();

    private ObjectMapperFactory() {
        // Static utility class
    }

    /**
     * Returns the shared, pre-configured {@link ObjectMapper} instance.
     *
     * @return the configured ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return INSTANCE;
    }

    private static ObjectMapper createMapper() {
        return JsonMapper.builder()
            .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.USE_DEFAULTS))
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .addModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module())
            .addMixIn(Response.class, ResponseMixin.class)
            .addMixIn(ResponseCreatedEvent.class, ResponseStreamEventMixin.class)
            .addMixIn(KnownValue.class, ResponseStreamEventMixin.class)
            // IgnoreValidMixin only carries @JsonIgnore for valid/isValid/getValid —
            // safe on Object.class because @JsonIgnore on non-existent methods is a no-op.
            // The @JsonFilter is intentionally NOT on this mixin to avoid applying the
            // JsonNull property filter to every type in the JVM (non-SDK types never
            // contain JsonNull sentinel values and don't need the filter overhead).
            .addMixIn(Object.class, IgnoreValidMixin.class)
            .filterProvider(new SimpleFilterProvider()
                .addFilter("ignoreJsonNull", new JsonNullPropertyFilter())
                .setFailOnUnknownId(false))
            .build();
    }

    @JsonFilter("ignoreJsonNull")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public interface ResponseStreamEventMixin {
    }

    /**
     * Mixin for {@link Response} that serializes timestamp fields as whole numbers
     * (Unix epoch seconds) instead of decimals.
     */
    @JsonFilter("ignoreJsonNull")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public interface ResponseMixin {
        @JsonSerialize(using = DoubleAsIntegerSerializer.class)
        double createdAt();

        @JsonSerialize(using = OptionalDoubleAsIntegerSerializer.class)
        Optional<Double> completedAt();
    }

    /**
     * Mixin applied to {@code Object.class} to suppress the OpenAI SDK's internal
     * {@code valid}/{@code isValid}/{@code getValid} validation properties from
     * JSON serialization.
     * <p>
     * This mixin intentionally does <em>not</em> carry {@code @JsonFilter} — the
     * {@link JsonNullPropertyFilter} is only needed for OpenAI SDK model types
     * (which get it via {@link ResponseMixin} or {@link ResponseStreamEventMixin}).
     * Applying the filter globally to {@code Object.class} would add unnecessary
     * overhead to every non-SDK type serialized through this mapper.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public interface IgnoreValidMixin {
        @JsonIgnore
        boolean valid();

        @JsonIgnore
        boolean isValid();

        @JsonIgnore
        boolean getValid();
    }

    /**
     * Serializer that writes {@link Double} values as plain integers (no decimal point).
     * Applied via mixin to specific fields like {@code createdAt} (Unix timestamps).
     */
    private static class DoubleAsIntegerSerializer extends JsonSerializer<Double> {
        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeNumber(BigDecimal.valueOf(value).toBigInteger().toString());
        }
    }

    /**
     * Serializer that writes {@link Optional}{@code <Double>} values as plain integers (no decimal point),
     * or omits the field when the Optional is empty.
     * Applied via mixin to optional timestamp fields like {@code completedAt}.
     */
    private static class OptionalDoubleAsIntegerSerializer extends JsonSerializer<Optional<Double>> {
        @Override
        public void serialize(Optional<Double> value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            if (value.isPresent()) {
                gen.writeNumber(BigDecimal.valueOf(value.get()).toBigInteger().toString());
            } else {
                gen.writeNull();
            }
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Optional<Double> value) {
            return value == null || value.isEmpty();
        }
    }

    /**
     * Property filter that suppresses fields whose runtime value is a {@link JsonNull} sentinel.
     */
    private static class JsonNullPropertyFilter extends SimpleBeanPropertyFilter {
        @Override
        public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
            if (writer instanceof BeanPropertyWriter bpw) {
                Object value = bpw.get(pojo);
                if (value instanceof JsonNull) {
                    return;
                }
            }
            super.serializeAsField(pojo, jgen, provider, writer);
        }
    }
}
