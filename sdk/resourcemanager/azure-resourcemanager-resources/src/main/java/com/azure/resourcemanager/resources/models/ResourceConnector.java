// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

///**
// * Copyright (c) Microsoft Corporation. All rights reserved.
// * Licensed under the MIT License. See License.txt in the project root for
// * license information.
// */
//
//package com.azure.management.resources;
//
//import com.microsoft.rest.RestClient;
//import com.microsoft.azure.management.apigeneration.Fluent;
//
///**
// * Defines a connector that connects other resources to a resource group.
// * Implementations of this class can let users browse resources inside a
// * specific resource group.
// */
//@Fluent
//public interface ResourceConnector {
//    /**
//     * Implementations of this interface defines how to create a connector.
//     *
//     * @param <T> the type of the connector to create.
//     */
//    interface Builder<T extends ResourceConnector> {
//        T create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup);
//    }
//}
