// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinFormDataJSON {
    public String url;
    public Map<String,String> headers;
    public Form form;

    public enum PizzaSize {
        SMALL("small"), MEDIUM("medium"), LARGE("large");

        private String value;
        PizzaSize(String value) {
            this.value = value;
        }
    }

    public static class Form {
        @JsonProperty("custname")
        public String customerName;

        @JsonProperty("custtel")
        public String customerTelephone;

        @JsonProperty("custemail")
        public String customerEmail;

        @JsonProperty("size")
        public PizzaSize pizzaSize;

        @JsonProperty("toppings")
        public List<String> toppings;
    }
}

