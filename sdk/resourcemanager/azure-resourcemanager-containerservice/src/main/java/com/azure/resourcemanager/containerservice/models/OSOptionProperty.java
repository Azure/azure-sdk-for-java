// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** OS option property. */
@Fluent
public final class OSOptionProperty {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(OSOptionProperty.class);

    /*
     * OS type.
     */
    @JsonProperty(value = "os-type", required = true)
    private String osType;

    /*
     * Whether FIPS image is enabled.
     */
    @JsonProperty(value = "enable-fips-image", required = true)
    private boolean enableFipsImage;

    /**
     * Get the osType property: OS type.
     *
     * @return the osType value.
     */
    public String osType() {
        return this.osType;
    }

    /**
     * Set the osType property: OS type.
     *
     * @param osType the osType value to set.
     * @return the OSOptionProperty object itself.
     */
    public OSOptionProperty withOsType(String osType) {
        this.osType = osType;
        return this;
    }

    /**
     * Get the enableFipsImage property: Whether FIPS image is enabled.
     *
     * @return the enableFipsImage value.
     */
    public boolean enableFipsImage() {
        return this.enableFipsImage;
    }

    /**
     * Set the enableFipsImage property: Whether FIPS image is enabled.
     *
     * @param enableFipsImage the enableFipsImage value to set.
     * @return the OSOptionProperty object itself.
     */
    public OSOptionProperty withEnableFipsImage(boolean enableFipsImage) {
        this.enableFipsImage = enableFipsImage;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (osType() == null) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException("Missing required property osType in model OSOptionProperty"));
        }
    }
}
