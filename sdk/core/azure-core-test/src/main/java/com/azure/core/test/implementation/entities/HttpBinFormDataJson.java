// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Maps to the JSON return values from http://httpbin.org.
 */
public class HttpBinFormDataJson {
    @JsonProperty
    private String url;
    @JsonProperty
    private Map<String, String> headers;
    @JsonProperty
    private Form form;

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

    /**
     * Pizza size.
     */
    public enum PizzaSize {
        SMALL("small"), MEDIUM("medium"), LARGE("large");

        private String value;

        PizzaSize(String value) {
            this.value = value;
        }
    }

    /**
     * Form.
     */
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
    }
}
