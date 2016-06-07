/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Location information.
 */
public class LocationInner {
    /**
     * Gets or sets the ID of the resource (/subscriptions/SubscriptionId).
     */
    private String id;

    /**
     * Gets or sets the subscription Id.
     */
    private String subscriptionId;

    /**
     * Gets or sets the location name.
     */
    private String name;

    /**
     * Gets or sets the display name of the location.
     */
    private String displayName;

    /**
     * Gets or sets the latitude of the location.
     */
    private String latitude;

    /**
     * Gets or sets the longitude of the location.
     */
    private String longitude;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the LocationInner object itself.
     */
    public LocationInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the subscriptionId value.
     *
     * @return the subscriptionId value
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Set the subscriptionId value.
     *
     * @param subscriptionId the subscriptionId value to set
     * @return the LocationInner object itself.
     */
    public LocationInner withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the LocationInner object itself.
     */
    public LocationInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the LocationInner object itself.
     */
    public LocationInner withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the latitude value.
     *
     * @return the latitude value
     */
    public String latitude() {
        return this.latitude;
    }

    /**
     * Set the latitude value.
     *
     * @param latitude the latitude value to set
     * @return the LocationInner object itself.
     */
    public LocationInner withLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    /**
     * Get the longitude value.
     *
     * @return the longitude value
     */
    public String longitude() {
        return this.longitude;
    }

    /**
     * Set the longitude value.
     *
     * @param longitude the longitude value to set
     * @return the LocationInner object itself.
     */
    public LocationInner withLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

}
