// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

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
