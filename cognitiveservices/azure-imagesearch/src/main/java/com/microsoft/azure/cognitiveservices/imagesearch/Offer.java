/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * Defines a merchant's offer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("Offer")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "AggregateOffer", value = AggregateOffer.class)
})
public class Offer extends Thing {
    /**
     * Seller for this offer.
     */
    @JsonProperty(value = "seller", access = JsonProperty.Access.WRITE_ONLY)
    private Organization seller;

    /**
     * The item's price.
     */
    @JsonProperty(value = "price", access = JsonProperty.Access.WRITE_ONLY)
    private Double price;

    /**
     * The monetary currency. For example, USD. Possible values include: 'USD',
     * 'CAD', 'GBP', 'EUR', 'COP', 'JPY', 'CNY', 'AUD', 'INR', 'AED', 'AFN',
     * 'ALL', 'AMD', 'ANG', 'AOA', 'ARS', 'AWG', 'AZN', 'BAM', 'BBD', 'BDT',
     * 'BGN', 'BHD', 'BIF', 'BMD', 'BND', 'BOB', 'BOV', 'BRL', 'BSD', 'BTN',
     * 'BWP', 'BYR', 'BZD', 'CDF', 'CHE', 'CHF', 'CHW', 'CLF', 'CLP', 'COU',
     * 'CRC', 'CUC', 'CUP', 'CVE', 'CZK', 'DJF', 'DKK', 'DOP', 'DZD', 'EGP',
     * 'ERN', 'ETB', 'FJD', 'FKP', 'GEL', 'GHS', 'GIP', 'GMD', 'GNF', 'GTQ',
     * 'GYD', 'HKD', 'HNL', 'HRK', 'HTG', 'HUF', 'IDR', 'ILS', 'IQD', 'IRR',
     * 'ISK', 'JMD', 'JOD', 'KES', 'KGS', 'KHR', 'KMF', 'KPW', 'KRW', 'KWD',
     * 'KYD', 'KZT', 'LAK', 'LBP', 'LKR', 'LRD', 'LSL', 'LYD', 'MAD', 'MDL',
     * 'MGA', 'MKD', 'MMK', 'MNT', 'MOP', 'MRO', 'MUR', 'MVR', 'MWK', 'MXN',
     * 'MXV', 'MYR', 'MZN', 'NAD', 'NGN', 'NIO', 'NOK', 'NPR', 'NZD', 'OMR',
     * 'PAB', 'PEN', 'PGK', 'PHP', 'PKR', 'PLN', 'PYG', 'QAR', 'RON', 'RSD',
     * 'RUB', 'RWF', 'SAR', 'SBD', 'SCR', 'SDG', 'SEK', 'SGD', 'SHP', 'SLL',
     * 'SOS', 'SRD', 'SSP', 'STD', 'SYP', 'SZL', 'THB', 'TJS', 'TMT', 'TND',
     * 'TOP', 'TRY', 'TTD', 'TWD', 'TZS', 'UAH', 'UGX', 'UYU', 'UZS', 'VEF',
     * 'VND', 'VUV', 'WST', 'XAF', 'XCD', 'XOF', 'XPF', 'YER', 'ZAR', 'ZMW'.
     */
    @JsonProperty(value = "priceCurrency", access = JsonProperty.Access.WRITE_ONLY)
    private Currency priceCurrency;

    /**
     * The item's availability. The following are the possible values:
     * Discontinued, InStock, InStoreOnly, LimitedAvailability, OnlineOnly,
     * OutOfStock, PreOrder, SoldOut. Possible values include: 'Discontinued',
     * 'InStock', 'InStoreOnly', 'LimitedAvailability', 'OnlineOnly',
     * 'OutOfStock', 'PreOrder', 'SoldOut'.
     */
    @JsonProperty(value = "availability", access = JsonProperty.Access.WRITE_ONLY)
    private ItemAvailability availability;

    /**
     * An aggregated rating that indicates how well the product has been rated
     * by others.
     */
    @JsonProperty(value = "aggregateRating", access = JsonProperty.Access.WRITE_ONLY)
    private AggregateRating aggregateRating;

    /**
     * The last date that the offer was updated. The date is in the form
     * YYYY-MM-DD.
     */
    @JsonProperty(value = "lastUpdated", access = JsonProperty.Access.WRITE_ONLY)
    private String lastUpdated;

    /**
     * Get the seller value.
     *
     * @return the seller value
     */
    public Organization seller() {
        return this.seller;
    }

    /**
     * Get the price value.
     *
     * @return the price value
     */
    public Double price() {
        return this.price;
    }

    /**
     * Get the priceCurrency value.
     *
     * @return the priceCurrency value
     */
    public Currency priceCurrency() {
        return this.priceCurrency;
    }

    /**
     * Get the availability value.
     *
     * @return the availability value
     */
    public ItemAvailability availability() {
        return this.availability;
    }

    /**
     * Get the aggregateRating value.
     *
     * @return the aggregateRating value
     */
    public AggregateRating aggregateRating() {
        return this.aggregateRating;
    }

    /**
     * Get the lastUpdated value.
     *
     * @return the lastUpdated value
     */
    public String lastUpdated() {
        return this.lastUpdated;
    }

}
