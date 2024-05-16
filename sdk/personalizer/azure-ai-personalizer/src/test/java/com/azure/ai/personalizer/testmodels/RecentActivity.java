// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class RecentActivity {
    @JsonGetter
    public Integer getItemsInCart() {
        return itemsInCart;
    }

    @JsonSetter
    public RecentActivity setItemsInCart(Integer itemsInCart) {
        this.itemsInCart = itemsInCart;
        return this;
    }

    @JsonProperty
    Integer itemsInCart;
}
