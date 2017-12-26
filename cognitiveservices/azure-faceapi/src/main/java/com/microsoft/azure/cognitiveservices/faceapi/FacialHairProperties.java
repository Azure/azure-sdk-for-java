/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Properties describing facial hair attributes.
 */
public class FacialHairProperties {
    /**
     * The moustache property.
     */
    @JsonProperty(value = "moustache")
    private double moustache;

    /**
     * The beard property.
     */
    @JsonProperty(value = "beard")
    private double beard;

    /**
     * The sideburns property.
     */
    @JsonProperty(value = "sideburns")
    private double sideburns;

    /**
     * Get the moustache value.
     *
     * @return the moustache value
     */
    public double moustache() {
        return this.moustache;
    }

    /**
     * Set the moustache value.
     *
     * @param moustache the moustache value to set
     * @return the FacialHairProperties object itself.
     */
    public FacialHairProperties withMoustache(double moustache) {
        this.moustache = moustache;
        return this;
    }

    /**
     * Get the beard value.
     *
     * @return the beard value
     */
    public double beard() {
        return this.beard;
    }

    /**
     * Set the beard value.
     *
     * @param beard the beard value to set
     * @return the FacialHairProperties object itself.
     */
    public FacialHairProperties withBeard(double beard) {
        this.beard = beard;
        return this;
    }

    /**
     * Get the sideburns value.
     *
     * @return the sideburns value
     */
    public double sideburns() {
        return this.sideburns;
    }

    /**
     * Set the sideburns value.
     *
     * @param sideburns the sideburns value to set
     * @return the FacialHairProperties object itself.
     */
    public FacialHairProperties withSideburns(double sideburns) {
        this.sideburns = sideburns;
        return this;
    }

}
