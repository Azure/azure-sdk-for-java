// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.Resource;
import com.azure.core.management.implementation.ProxyResourceAccessHelper;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ManagementExceptionTests {

    @Test
    public void testDeserialization() throws IOException {
        final String errorBody
            = "{\"error\":{\"code\":\"ResourceGroupNotFound\",\"message\":\"Resource group 'rg-not-exist' could not be found.\"}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        ManagementError managementError
            = serializerAdapter.deserialize(errorBody, ManagementError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("ResourceGroupNotFound", managementError.getCode());
    }

    @Test
    public void testSubclassDeserialization() throws IOException {
        final String errorBody
            = "{\"error\":{\"code\":\"WepAppError\",\"message\":\"Web app error.\",\"innererror\":\"Deployment error.\",\"details\":[{\"innererror\":\"Inner error.\"}]}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        WebError webError = serializerAdapter.deserialize(errorBody, WebError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("WepAppError", webError.getCode());

        // The response is actually not a valid management error response. But still accommodate.
        final String errorBodyWithoutErrorProperty
            = "{\"code\":\"ResourceGroupNotFound\",\"message\":\"Resource group 'rg-not-exist' could not be found.\"}";

        ManagementError managementError = serializerAdapter.deserialize(errorBodyWithoutErrorProperty,
            ManagementError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("ResourceGroupNotFound", managementError.getCode());
    }

    @Test
    public void testCaseInsensitiveSubclassDeserialization() throws IOException {
        final String errorBody
            = "{\"error\":{\"Code\":\"WepAppError\",\"MESSAGE\":\"Web app error.\",\"Details\":[{\"code\":\"e\"}],\"TaRgeT\":\"foo\"}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        WebError webError = serializerAdapter.deserialize(errorBody, WebError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("WepAppError", webError.getCode());
        Assertions.assertEquals("Web app error.", webError.getMessage());
        Assertions.assertEquals(1, webError.getDetails().size());
        Assertions.assertEquals("foo", webError.getTarget());
    }

    @Test
    public void testDeserializationInResource() throws IOException {
        final String virtualMachineJson
            = "{\"properties\":{\"instanceView\":{\"patchStatus\":{\"availablePatchSummary\":{\"error\":{}}}}}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        VirtualMachine virtualMachine
            = serializerAdapter.deserialize(virtualMachineJson, VirtualMachine.class, SerializerEncoding.JSON);

        Assertions.assertNotNull(virtualMachine.instanceView.patchStatus.availablePatchSummary.error);
    }

    @Immutable
    private static class WebError extends ManagementError {
        @JsonProperty(value = "innererror", access = JsonProperty.Access.WRITE_ONLY)
        private String innererror;

        @JsonProperty(value = "details", access = JsonProperty.Access.WRITE_ONLY)
        private List<WebError> details;

        public String getInnererror() {
            return this.innererror;
        }

        public List<WebError> getDetails() {
            return details;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("code", getCode())
                .writeStringField("message", getMessage())
                .writeStringField("target", getTarget())
                .writeArrayField("details", details, (writer, element) -> element.toJson(writer))
                .writeStringField("innererror", innererror)
                .writeArrayField("additionalInfo", getAdditionalInfo(), JsonWriter::writeJson)
                .writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link WebError}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link WebError} that the JSON stream represented, or null if it pointed to JSON null.
         * @throws IOException If a {@link WebError} fails to be read from the {@code jsonReader}.
         */
        public static WebError fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                // Buffer the next JSON object as ResponseError can take two forms:
                //
                // - A ResponseError object
                // - A ResponseError object wrapped in an "error" node.
                JsonReader bufferedReader = reader.bufferObject();
                bufferedReader.nextToken(); // Get to the START_OBJECT token.
                while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = bufferedReader.getFieldName();
                    bufferedReader.nextToken();

                    if ("error".equals(fieldName)) {
                        // If the ResponseError was wrapped in the "error" node begin reading it now.
                        return readWebError(bufferedReader);
                    } else {
                        bufferedReader.skipChildren();
                    }
                }

                // Otherwise reset the JsonReader and read the whole JSON object.
                return readWebError(bufferedReader.reset());
            });
        }

        private static WebError readWebError(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                WebError webError = new WebError();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("code".equalsIgnoreCase(fieldName)) {
                        webError.setCode(reader.getString());
                    } else if ("message".equalsIgnoreCase(fieldName)) {
                        webError.setMessage(reader.getString());
                    } else if ("target".equalsIgnoreCase(fieldName)) {
                        webError.setTarget(reader.getString());
                    } else if ("details".equalsIgnoreCase(fieldName)) {
                        webError.details = reader.readArray(WebError::fromJson);
                    } else if ("innererror".equalsIgnoreCase(fieldName)) {
                        webError.innererror = reader.getString();
                    } else if ("additionalInfo".equalsIgnoreCase(fieldName)) {
                        webError.setAdditionalInfo(reader.readArray(AdditionalInfo::fromJson));
                    } else {
                        reader.skipChildren();
                    }
                }

                return webError;
            });
        }
    }

    @JsonFlatten
    @Fluent
    private static final class VirtualMachine extends Resource {
        @JsonProperty(value = "properties.instanceView", access = JsonProperty.Access.WRITE_ONLY)
        private VirtualMachineInstanceView instanceView;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject()
                .writeStringField("location", location())
                .writeMapField("tags", tags(), JsonWriter::writeString);

            if (instanceView != null) {
                jsonWriter.writeStartObject("properties").writeJsonField("instanceView", instanceView).writeEndObject();
            }

            return jsonWriter.writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link VirtualMachine}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link VirtualMachine} that the JSON stream represented, may return null.
         * @throws IOException If a {@link VirtualMachine} fails to be read from the {@code jsonReader}.
         */
        public static VirtualMachine fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                VirtualMachine virtualMachine = new VirtualMachine();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("id".equals(fieldName)) {
                        ProxyResourceAccessHelper.setId(virtualMachine, reader.getString());
                    } else if ("name".equals(fieldName)) {
                        ProxyResourceAccessHelper.setName(virtualMachine, reader.getString());
                    } else if ("type".equals(fieldName)) {
                        ProxyResourceAccessHelper.setType(virtualMachine, reader.getString());
                    } else if ("location".equals(fieldName)) {
                        virtualMachine.withLocation(reader.getString());
                    } else if ("tags".equals(fieldName)) {
                        virtualMachine.withTags(reader.readMap(JsonReader::getString));
                    } else if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                        while (reader.nextToken() != JsonToken.END_OBJECT) {
                            fieldName = reader.getFieldName();
                            reader.nextToken();

                            if ("instanceView".equals(fieldName)) {
                                virtualMachine.instanceView = reader.readObject(VirtualMachineInstanceView::fromJson);
                            } else {
                                reader.skipChildren();
                            }
                        }
                    } else {
                        reader.skipChildren();
                    }
                }

                return virtualMachine;
            });
        }
    }

    @Fluent
    public static final class VirtualMachineInstanceView implements JsonSerializable<VirtualMachineInstanceView> {
        @JsonProperty(value = "patchStatus")
        private VirtualMachinePatchStatus patchStatus;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject().writeJsonField("patchStatus", patchStatus).writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link VirtualMachineInstanceView}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link VirtualMachineInstanceView} that the JSON stream represented, may return null.
         * @throws IOException If a {@link VirtualMachineInstanceView} fails to be read from the {@code jsonReader}.
         */
        public static VirtualMachineInstanceView fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                VirtualMachineInstanceView virtualMachineInstanceView = new VirtualMachineInstanceView();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("patchStatus".equals(fieldName)) {
                        virtualMachineInstanceView.patchStatus = reader.readObject(VirtualMachinePatchStatus::fromJson);
                    } else {
                        reader.skipChildren();
                    }
                }

                return virtualMachineInstanceView;
            });
        }
    }

    @Fluent
    public static final class VirtualMachinePatchStatus implements JsonSerializable<VirtualMachinePatchStatus> {
        @JsonProperty(value = "availablePatchSummary")
        private AvailablePatchSummary availablePatchSummary;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeJsonField("availablePatchSummary", availablePatchSummary)
                .writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link VirtualMachinePatchStatus}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link VirtualMachinePatchStatus} that the JSON stream represented, may return null.
         * @throws IOException If a {@link VirtualMachinePatchStatus} fails to be read from the {@code jsonReader}.
         */
        public static VirtualMachinePatchStatus fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                VirtualMachinePatchStatus virtualMachinePatchStatus = new VirtualMachinePatchStatus();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("availablePatchSummary".equals(fieldName)) {
                        virtualMachinePatchStatus.availablePatchSummary
                            = reader.readObject(AvailablePatchSummary::fromJson);
                    } else {
                        reader.skipChildren();
                    }
                }

                return virtualMachinePatchStatus;
            });
        }
    }

    @Immutable
    public static final class AvailablePatchSummary implements JsonSerializable<AvailablePatchSummary> {
        @JsonProperty(value = "error", access = JsonProperty.Access.WRITE_ONLY)
        private ManagementError error;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject().writeEndObject();
        }

        /**
         * Reads a JSON stream into an {@link AvailablePatchSummary}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link AvailablePatchSummary} that the JSON stream represented, may return null.
         * @throws IOException If an {@link AvailablePatchSummary} fails to be read from the {@code jsonReader}.
         */
        public static AvailablePatchSummary fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                AvailablePatchSummary availablePatchSummary = new AvailablePatchSummary();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("error".equals(fieldName)) {
                        availablePatchSummary.error = reader.readObject(ManagementError::fromJson);
                    } else {
                        reader.skipChildren();
                    }
                }

                return availablePatchSummary;
            });
        }
    }
}
