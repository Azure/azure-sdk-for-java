// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.azure.cosmos.implementation.Utils.as;

/**
 * Used internally to encapsulate internal information of a partition key in the Azure Cosmos DB database service.
 */
@JsonSerialize(using = PartitionKeyInternal.PartitionKeyInternalJsonSerializer.class)
@JsonDeserialize(using = PartitionKeyInternal.PartitionKeyInternalJsonDeserializer.class)
public class PartitionKeyInternal implements Comparable<PartitionKeyInternal> {

    private static final String TYPE = "type";
    private static final String MIN_NUMBER = "MinNumber";
    private static final String MAX_NUMBER = "MaxNumber";
    private static final String MIN_STRING = "MinString";
    private static final String MAX_STRING = "MaxString";
    private static final String INFINITY = "Infinity";

    public static final PartitionKeyInternal NonePartitionKey =
            new PartitionKeyInternal();

    public static final PartitionKeyInternal EmptyPartitionKey =
            new PartitionKeyInternal(new ArrayList<>());

    @SuppressWarnings("serial")
    public static final PartitionKeyInternal InfinityPartitionKey =
            new PartitionKeyInternal(new ArrayList<IPartitionKeyComponent>() {{
                add(new InfinityPartitionKeyComponent());
            }});

    @SuppressWarnings("serial")
    public static final PartitionKeyInternal UndefinedPartitionKey =
            new PartitionKeyInternal(new ArrayList<IPartitionKeyComponent>() {{
                add(new UndefinedPartitionKeyComponent());
            }});

    public static final PartitionKeyInternal InclusiveMinimum = PartitionKeyInternal.EmptyPartitionKey;
    public static final PartitionKeyInternal ExclusiveMaximum = PartitionKeyInternal.InfinityPartitionKey;
    public static final PartitionKeyInternal Empty = PartitionKeyInternal.EmptyPartitionKey;
    public static final PartitionKeyInternal None = PartitionKeyInternal.NonePartitionKey;

    final List<IPartitionKeyComponent> components;

    public PartitionKeyInternal(List<IPartitionKeyComponent> values) {
        if (values == null) {
            throw new IllegalArgumentException("values");
        }

        this.components = values;
    }

    public PartitionKeyInternal() {
        this.components = null;
    }

    public static PartitionKeyInternal fromJsonString(String partitionKey) {
        if (Strings.isNullOrEmpty(partitionKey)) {
            throw new IllegalArgumentException(String.format(RMResources.UnableToDeserializePartitionKeyValue, partitionKey));
        }

        try {
            return Utils.getSimpleObjectMapper().readValue(partitionKey, PartitionKeyInternal.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static PartitionKeyInternal fromObjectArray(Object[] values, boolean strict) {
        if (values == null) {
            throw new IllegalArgumentException("values");
        }

        return PartitionKeyInternal.fromObjectArray(Arrays.asList(values), strict);
    }

    public static PartitionKeyInternal fromObjectArray(List<Object> values, boolean strict) {
        if (values == null) {
            throw new IllegalArgumentException("values");
        }

        List<IPartitionKeyComponent> components = new ArrayList<>();
        for (Object value : values) {
            if (value == NullNode.instance || value == null) {
                components.add(NullPartitionKeyComponent.VALUE);
            } else if (value instanceof Undefined) {
                components.add(UndefinedPartitionKeyComponent.VALUE);
            } else if (value instanceof Boolean) {
                components.add(new BoolPartitionKeyComponent((boolean) value));
            } else if (value instanceof String) {
                components.add(new StringPartitionKeyComponent((String) value));
            } else if (isNumeric(value)) {
                components.add(new NumberPartitionKeyComponent(((Number) value).doubleValue()));
            } else if (value instanceof ObjectNode && ((ObjectNode) value).get(TYPE) != null) {
                String type = ((ObjectNode) value).get(TYPE).asText();
                switch (type) {
                    case MIN_NUMBER:
                        components.add(MinNumberPartitionKeyComponent.VALUE);
                        break;
                    case MAX_NUMBER:
                        components.add(MaxNumberPartitionKeyComponent.VALUE);
                        break;
                    case MIN_STRING:
                        components.add(MinStringPartitionKeyComponent.VALUE);
                        break;
                    case MAX_STRING:
                        components.add(MaxStringPartitionKeyComponent.VALUE);
                        break;
                    default:
                        throw new IllegalArgumentException("Unable to construct PartitionKeyInternal from object array - unknown type " + type);
                }
            } else {
                if (strict) {
                    throw new IllegalArgumentException(
                        "Unable to construct PartitionKeyInternal from objects array - unknown type " +
                            value.getClass().getName());
                } else {
                    components.add(UndefinedPartitionKeyComponent.VALUE);
                }
            }
        }

        return new PartitionKeyInternal(components);
    }

    private static boolean isNumeric(Object value) {
        return value instanceof Number;
    }

    private static PartitionKeyInternal getExclusiveMaximum() {
        return PartitionKeyInternal.InfinityPartitionKey;
    }

    public static PartitionKeyInternal getEmpty() {
        return PartitionKeyInternal.EmptyPartitionKey;
    }

    @Override
    public boolean equals(Object obj) {
        PartitionKeyInternal pki = as(obj, PartitionKeyInternal.class);
        if (pki == null) {
            return false;
        }

        if (pki == this) {
            return true;
        }

        return this.compareTo(pki) == 0;
    }

    @Override
    public int hashCode() {
//        TODO: @kushagraThapar, @moderakh, mbhaskar to identify proper implementation.
//        Issue: https://github.com/Azure/azure-sdk-for-java/issues/9046
//        if (this.components == null || this.components.size() == 0) {
//            return 0;
//        }
//        int [] ordinals = new int[this.components.size()];
//        for (int i = 0; i < this.components.size(); i++) {
//            ordinals[i] = this.components.get(i).GetTypeOrdinal();
//        }
//        return Arrays.hashCode(ordinals);
        return super.hashCode();
    }

    public int compareTo(PartitionKeyInternal other) {
        if (other == null) {
            throw new IllegalArgumentException("other");
        } else if (other.components == null || this.components == null) {
            int otherComponentsCount = other.components == null ? 0 : other.components.size();
            int thisComponentsCount = this.components == null ? 0 : this.components.size();
            return (int) Math.signum(thisComponentsCount - otherComponentsCount);
        }

        for (int i = 0; i < Math.min(this.components.size(), other.components.size()); i++) {
            int leftOrdinal = this.components.get(i).getTypeOrdinal();
            int rightOrdinal = other.components.get(i).getTypeOrdinal();
            if (leftOrdinal != rightOrdinal) {
                return (int) Math.signum(leftOrdinal - rightOrdinal);
            }

            int result = this.components.get(i).compareTo(other.components.get(i));
            if (result != 0) {
                return (int) Math.signum(result);
            }
        }

        return (int) Math.signum(this.components.size() - other.components.size());
    }

    public String toJson() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable serialize the partition key internal into the JSON string", e);
        }
    }

    public boolean contains(PartitionKeyInternal nestedPartitionKey) {
        if (this.components.size() > nestedPartitionKey.components.size()) {
            return false;
        }

        for (int i = 0; i < this.components.size(); i++) {
            if (this.components.get(i).compareTo(nestedPartitionKey.components.get(i)) != 0) {
                return false;
            }
        }

        return true;
    }

    public List<IPartitionKeyComponent> getComponents() {
        return components;
    }

    public String getEffectivePartitionKeyString(PartitionKeyInternal internalPartitionKey, PartitionKeyDefinition partitionKey) {
        return PartitionKeyInternalHelper.getEffectivePartitionKeyString(internalPartitionKey, partitionKey);
    }

    @SuppressWarnings("serial")
    static final class PartitionKeyInternalJsonSerializer extends StdSerializer<PartitionKeyInternal> {

        private static final long serialVersionUID = 2258093043805843865L;

        protected PartitionKeyInternalJsonSerializer() { this(null); }

        protected PartitionKeyInternalJsonSerializer(Class<PartitionKeyInternal> t) {
            super(t);
        }

        @Override
        public void serialize(PartitionKeyInternal partitionKey, JsonGenerator writer, SerializerProvider serializerProvider) {
            try {
                if (partitionKey.equals(PartitionKeyInternal.getExclusiveMaximum())) {
                    writer.writeString(INFINITY);
                    return;
                }

                //  PartitionKey.None has null components - which returns a null list
                if (partitionKey.getComponents() != null) {
                    writer.writeStartArray();
                    for (IPartitionKeyComponent componentValue : partitionKey.getComponents()) {
                        componentValue.jsonEncode(writer);
                    }
                    writer.writeEndArray();
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        static void jsonEncode(MinNumberPartitionKeyComponent component, JsonGenerator writer) {
            jsonEncodeLimit(writer, MIN_NUMBER);
        }

        static void jsonEncode(MaxNumberPartitionKeyComponent component, JsonGenerator writer) {
            jsonEncodeLimit(writer, MAX_NUMBER);
        }

        static void jsonEncode(MinStringPartitionKeyComponent component, JsonGenerator writer) {
            jsonEncodeLimit(writer, MIN_STRING);
        }

        static void jsonEncode(MaxStringPartitionKeyComponent component, JsonGenerator writer) {
            jsonEncodeLimit(writer, MAX_STRING);
        }

        private static void jsonEncodeLimit(JsonGenerator writer, String value) {
            try {
                writer.writeStartObject();
                writer.writeFieldName(TYPE);
                writer.writeString(value);
                writer.writeEndObject();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @SuppressWarnings("serial")
    static final class PartitionKeyInternalJsonDeserializer extends StdDeserializer<PartitionKeyInternal> {
        private static final long serialVersionUID = -6531933186096854710L;

        protected PartitionKeyInternalJsonDeserializer() { this(null); }

        protected PartitionKeyInternalJsonDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public PartitionKeyInternal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {

            ObjectCodec objectCodec = jsonParser.getCodec();
            JsonNode root;
            try {
                root = objectCodec.readTree(jsonParser);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }

            if (root.isTextual() && root.asText().equals(INFINITY)) {
                return PartitionKeyInternal.getExclusiveMaximum();
            }

            List<Object> objects = new ArrayList<>();
            if (root.isArray()) {
                Iterator<JsonNode> iterator = root.iterator();
                while (iterator.hasNext()) {
                    JsonNode node = iterator.next();
                    if (node.isNull()) {
                        objects.add(null);
                    } else if (node.isNumber()) {
                        objects.add(node.asDouble());
                    } else if (node.isBoolean()) {
                        objects.add(node.asBoolean());
                    } else if (node.isTextual()) {
                        objects.add(node.asText());
                    } else if (node.isArray() && node.size() == 0
                            || node.isObject()
                                && (node.fields() == null || !node.fields().hasNext())) {
                        objects.add(Undefined.value());
                    } else {
                        objects.add(node);
                    }
                }
                return PartitionKeyInternal.fromObjectArray(objects, true);
            }

            throw new IllegalStateException(String.format(
                    "Unable to deserialize PartitionKeyInternal '%s'",
                    root.toString()));
        }
    }
}
