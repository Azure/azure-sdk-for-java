// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetTime;

public class SalesOrder {
    //You can use JsonProperty attributes to control how your objects are
    //handled by the Json Serializer/Deserializer
    //Any of the supported JSON.NET attributes here are supported, including the use of JsonConverters
    //if you really want fine grained control over the process

    //Here we are using JsonProperty to control how the Id property is passed over the wire
    //In this case, we're just making it a lowerCase string but you could entirely rename it
    //like we do with PurchaseOrderNumber below
    @JsonProperty("id")
    public String id;

    @JsonProperty("ponumber")
    public String PurchaseOrderNumber;

    // used to set expiration policy
    @JsonProperty("ttl")
    public int TimeToLive;

    public OffsetTime OrderDate;
    public OffsetTime ShippedDate;
    public String AccountNumber;
    public Double SubTotal;
    public Double TaxAmount;
    public Double Freight;
    public Double TotalDue;
    public SalesOrderDetail[] Items;

    public static class SalesOrderDetail {
        public int OrderQty;
        public int ProductId;
        public double UnitPrice;
        public double LineTotal;
    }
}
