// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestack.implementation;

import com.azure.resourcemanager.azurestack.fluent.models.ProductInner;
import com.azure.resourcemanager.azurestack.fluent.models.ProductListInner;
import com.azure.resourcemanager.azurestack.models.Product;
import com.azure.resourcemanager.azurestack.models.ProductList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ProductListImpl implements ProductList {
    private ProductListInner innerObject;

    private final com.azure.resourcemanager.azurestack.AzureStackManager serviceManager;

    ProductListImpl(ProductListInner innerObject,
        com.azure.resourcemanager.azurestack.AzureStackManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public String nextLink() {
        return this.innerModel().nextLink();
    }

    public List<Product> value() {
        List<ProductInner> inner = this.innerModel().value();
        if (inner != null) {
            return Collections.unmodifiableList(
                inner.stream().map(inner1 -> new ProductImpl(inner1, this.manager())).collect(Collectors.toList()));
        } else {
            return Collections.emptyList();
        }
    }

    public ProductListInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.azurestack.AzureStackManager manager() {
        return this.serviceManager;
    }
}
