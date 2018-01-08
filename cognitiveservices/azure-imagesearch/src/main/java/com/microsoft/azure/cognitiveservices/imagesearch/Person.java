/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a person.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Person.class)
@JsonTypeName("Person")
public class Person extends Thing {
    /**
     * The person's job title.
     */
    @JsonProperty(value = "jobTitle", access = JsonProperty.Access.WRITE_ONLY)
    private String jobTitle;

    /**
     * The URL of the person's twitter profile.
     */
    @JsonProperty(value = "twitterProfile", access = JsonProperty.Access.WRITE_ONLY)
    private String twitterProfile;

    /**
     * Get the jobTitle value.
     *
     * @return the jobTitle value
     */
    public String jobTitle() {
        return this.jobTitle;
    }

    /**
     * Get the twitterProfile value.
     *
     * @return the twitterProfile value
     */
    public String twitterProfile() {
        return this.twitterProfile;
    }

}
