/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.routing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.microsoft.azure.cosmosdb.Undefined;
import com.microsoft.azure.cosmosdb.internal.Utils;

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

    private static final PartitionKeyInternal EmptyPartitionKey =
            new PartitionKeyInternal(new ArrayList<IPartitionKeyComponent>());
    public static final String MinimumInclusiveEffectivePartitionKey = EmptyPartitionKey.toHexEncodedBinaryString();
    @SuppressWarnings("serial")
    private static final PartitionKeyInternal InfinityPartitionKey =
            new PartitionKeyInternal(new ArrayList<IPartitionKeyComponent>() {{
                add(new InfinityPartitionKeyComponent());
            }});
    public static final String MaximumExclusiveEffectivePartitionKey = InfinityPartitionKey.toHexEncodedBinaryString();
    private final int MaxPartitionKeyBinarySize = (1 /*type marker */ +
            9 /* hash value*/ +
            1 /* type marker*/ +
            StringPartitionKeyComponent.MaxStringComponentLength +
            1 /*trailing zero*/) * 3;
    private final List<IPartitionKeyComponent> components;

    public PartitionKeyInternal(List<IPartitionKeyComponent> values) {
        if (values == null) {
            throw new IllegalArgumentException("values");
        }

        this.components = values;
    }

    public static PartitionKeyInternal fromObjectArray(List<Object> values, boolean strict) {
        if (values == null) {
            throw new IllegalArgumentException("values");
        }

        List<IPartitionKeyComponent> components = new ArrayList<>();
        for (Object value : values) {
            if (value == JSONObject.NULL || value == null) {
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
                switch (((ObjectNode) value).get(TYPE).asText()) {
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
                }
            } else {
                if (strict) {
                    throw new IllegalStateException("Unable to construct PartitionKeyInternal from objects array");
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

    public static PartitionKeyInternal getExclusiveMaximum() {
        return PartitionKeyInternal.InfinityPartitionKey;
    }

    public static PartitionKeyInternal getEmpty() {
        return PartitionKeyInternal.EmptyPartitionKey;
    }

    private boolean equals(PartitionKeyInternal obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        return this.compareTo(obj) == 0;
    }

    public int compareTo(PartitionKeyInternal other) {
        if (other == null) {
            throw new IllegalArgumentException("other");
        }

        for (int i = 0; i < Math.min(this.components.size(), other.components.size()); i++) {
            int leftOrdinal = this.components.get(i).GetTypeOrdinal();
            int rightOrdinal = other.components.get(i).GetTypeOrdinal();
            if (leftOrdinal != rightOrdinal) {
                return (int) Math.signum(leftOrdinal - rightOrdinal);
            }

            int result = this.components.get(i).CompareTo(other.components.get(i));
            if (result != 0) {
                return (int) Math.signum(result);
            }
        }

        return (int) Math.signum(this.components.size() - other.components.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) 
        { 
            return false;
        }
        return equals(obj.getClass() == PartitionKeyInternal.class ? (PartitionKeyInternal) obj : null);
    }

    public String toHexEncodedBinaryString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(MaxPartitionKeyBinarySize);
        for (int index = 0; index < this.components.size(); index++) {
            this.components.get(index).WriteForBinaryEncoding(stream);
        }

        return HexConvert.bytesToHex(stream.toByteArray());
    }
    
    public String toJson() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable serialize the partition key internal into the JSON string", e);
        }
    }

    public List<IPartitionKeyComponent> getComponents() {
        return components;
    }

    static class HexConvert {
        final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

        public static String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
    }

    @SuppressWarnings("serial")
    static final class PartitionKeyInternalJsonSerializer extends StdSerializer<PartitionKeyInternal> {

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

                writer.writeStartArray();
                for (IPartitionKeyComponent componentValue : partitionKey.getComponents()) {
                    componentValue.JsonEncode(writer);
                }
                writer.writeEndArray();
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
                throw new IllegalStateException(e);
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
                        objects.add(Undefined.Value());
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
