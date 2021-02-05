// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

public class SalesOrder {
    public String id;
    public String purchaseOrderNumber;

    public int timeToLive;

    public String orderDate;

    public String shippedDate;
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
}
