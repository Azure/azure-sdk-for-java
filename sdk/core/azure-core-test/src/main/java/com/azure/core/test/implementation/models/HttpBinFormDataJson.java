// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public final class HttpBinFormDataJson implements JsonSerializable<HttpBinFormDataJson> {
    private String url;
    private Map<String, String> headers;
    private Form form;

    /**
     * Creates an instance of HttpBinFormDataJson.
     */
    public HttpBinFormDataJson() {
    }

    /**
     * Gets the URL associated with this request.
     *
     * @return he URL associated with the request.
     */
    public String url() {
        return url;
    }

    /**
     * Sets the URL associated with this request.
     *
     * @param url The URL associated with the request.
     */
    public void url(String url) {
        this.url = url;
    }

    /**
     * Gets the headers.
     *
     * @return The headers.
     */
    public Map<String, String> headers() {
        return headers;
    }

    /**
     * Sets the headers.
     *
     * @param headers The headers.
     */
    public void headers(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Gets the form.
     *
     * @return The form.
     */
    public Form form() {
        return form;
    }

    /**
     * Sets the form.
     *
     * @param form The form.
     */
    public void form(Form form) {
        this.form = form;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("url", url)
            .writeMapField("headers", headers, JsonWriter::writeString)
            .writeJsonField("form", form)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of HttpBinFormDataJson from the input JSON.
     *
     * @param jsonReader The JSON reader to read from.
     * @return An instance of HttpBinFormDataJson deserialized from the input JSON.
     * @throws IOException If an error occurs while reading from the JSON.
     */
    public static HttpBinFormDataJson fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HttpBinFormDataJson form = new HttpBinFormDataJson();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("url".equals(fieldName)) {
                    form.url = reader.getString();
                } else if ("headers".equals(fieldName)) {
                    form.headers = reader.readMap(JsonReader::getString);
                } else if ("form".equals(fieldName)) {
                    form.form = Form.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return form;
        });
    }

    /**
     * Form.
     */
    public static class Form implements JsonSerializable<Form> {
        private String customerName;
        private String customerTelephone;
        private String customerEmail;
        private PizzaSize pizzaSize;
        private List<String> toppings;

        /**
         * Creates an instance of Form.
         */
        public Form() {
        }

        /**
         * Gets the customer name.
         *
         * @return The customer name.
         */
        public String customerName() {
            return this.customerName;
        }

        /**
         * Sets the customer name.
         *
         * @param customerName The customer name.
         */
        public void customerName(String customerName) {
            this.customerName = customerName;
        }

        /**
         * Gets the customer telephone.
         *
         * @return The customer telephone.
         */
        public String customerTelephone() {
            return this.customerTelephone;
        }

        /**
         * Sets the customer telephone.
         *
         * @param customerTelephone The customer telephone.
         */
        public void customerTelephone(String customerTelephone) {
            this.customerTelephone = customerTelephone;
        }

        /**
         * Gets the customer email.
         *
         * @return The customer email.
         */
        public String customerEmail() {
            return this.customerEmail;
        }

        /**
         * Sets the customer email.
         *
         * @param customerEmail The customer email.
         */
        public void customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        /**
         * Gets the pizza size.
         *
         * @return The pizza size.
         */
        public PizzaSize pizzaSize() {
            return this.pizzaSize;
        }

        /**
         * Sets the pizza size.
         *
         * @param pizzaSize The pizza size.
         */
        public void pizzaSize(PizzaSize pizzaSize) {
            this.pizzaSize = pizzaSize;
        }

        /**
         * Gets the toppings.
         *
         * @return The toppings.
         */
        public List<String> toppings() {
            return this.toppings;
        }

        /**
         * Sets the toppings.
         *
         * @param toppings The toppings.
         */
        public void toppings(List<String> toppings) {
            this.toppings = toppings;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("custname", customerName)
                .writeStringField("custtel", customerTelephone)
                .writeStringField("custemail", customerEmail)
                .writeStringField("size", Objects.toString(pizzaSize, null))
                .writeArrayField("toppings", toppings, JsonWriter::writeString)
                .writeEndObject();
        }

        /**
         * Deserializes an instance of Form from the input JSON.
         *
         * @param jsonReader The JSON reader to read from.
         * @return An instance of Form deserialized from the input JSON.
         * @throws IOException If an error occurs while reading from the JSON.
         */
        public static Form fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                Form form = new Form();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("custname".equals(fieldName)) {
                        form.customerName = reader.getString();
                    } else if ("custtel".equals(fieldName)) {
                        form.customerTelephone = reader.getString();
                    } else if ("custemail".equals(fieldName)) {
                        form.customerEmail = reader.getString();
                    } else if ("size".equals(fieldName)) {
                        form.pizzaSize = PizzaSize.fromString(reader.getString());
                    } else if ("toppings".equals(fieldName)) {
                        form.toppings = reader.readArray(JsonReader::getString);
                    } else {
                        reader.skipChildren();
                    }
                }

                return form;
            });
        }
    }
}
