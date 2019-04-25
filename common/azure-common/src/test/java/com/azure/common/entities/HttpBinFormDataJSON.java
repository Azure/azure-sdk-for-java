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
    @JsonProperty
    private String url;
    @JsonProperty
    private Map<String, String> headers;
    @JsonProperty
    private Form form;

    public String url() {
        return url;
    }

    public void url(String url) {
        this.url = url;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public void headers(Map<String, String> headers) {
        this.headers = headers;
    }

    public Form form() {
        return form;
    }

    public void form(Form form) {
        this.form = form;
    }

    public enum PizzaSize {
        SMALL("small"), MEDIUM("medium"), LARGE("large");

        private String value;
        PizzaSize(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

        public void value(String value) {
            this.value = value;
        }
    }

    public static class Form {
        @JsonProperty("custname")
        private String customerName;

        @JsonProperty("custtel")
        private String customerTelephone;

        @JsonProperty("custemail")
        private String customerEmail;

        @JsonProperty("size")
        private PizzaSize pizzaSize;

        @JsonProperty("toppings")
        private List<String> toppings;

        public String customerName() {
            return this.customerName;
        }

        public void customerName(String customerName) {
            this.customerName = customerName;
        }

        public String customerTelephone() {
            return this.customerTelephone;
        }

        public void customerTelephone(String customerTelephone) {
            this.customerTelephone = customerTelephone;
        }

        public String customerEmail() {
            return this.customerEmail;
        }

        public void customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public PizzaSize pizzaSize() {
            return this.pizzaSize;
        }

        public void pizzaSize(PizzaSize pizzaSize) {
            this.pizzaSize = pizzaSize;
        }

        public List<String> toppings() {
            return this.toppings;
        }

        public void toppings(List<String> toppings) {
            this.toppings = toppings;
        }
    }
}

