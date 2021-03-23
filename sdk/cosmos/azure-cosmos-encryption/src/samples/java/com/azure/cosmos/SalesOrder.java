// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;

public class SalesOrder {
    public String id;
    public String purchaseOrderNumber;
    public int timeToLive;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonSerialize(using = InstantSerializer.class)
    public Instant orderDate;
    @JsonDeserialize(using = InstantDeserializer.class)
    @JsonSerialize(using = InstantSerializer.class)
    public Instant shippedDate;
    public String accountNumber;
    public Double subTotal;
    public Double taxAmount;
    public Double freight;
    public Double totalDue;
    public SalesOrderDetail[] items;

    public static class SalesOrderDetail {
        public int orderQty;
        public int productId;
        public double unitPrice;
        public double lineTotal;
    }

    static class InstantSerializer extends StdSerializer<Instant> {
        public InstantSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(instant.toEpochMilli());
        }
    }

    static class InstantDeserializer extends StdDeserializer<Instant> {
        public InstantDeserializer() {
            super(Instant.class);
        }

        @Override
        public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return Instant.ofEpochMilli(Long.valueOf(jsonParser.getText()));
        }
    }

}
