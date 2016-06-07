/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Represents user crendentials used for publishing activity.
 */
@JsonFlatten
public class UserInner extends Resource {
    /**
     * Username (internal).
     */
    @JsonProperty(value = "properties.name")
    private String userName;

    /**
     * Username used for publishing.
     */
    @JsonProperty(value = "properties.publishingUserName")
    private String publishingUserName;

    /**
     * Password used for publishing.
     */
    @JsonProperty(value = "properties.publishingPassword")
    private String publishingPassword;

    /**
     * Service Control Manager URI, including username and password.
     */
    @JsonProperty(value = "properties.scmUri")
    private String scmUri;

    /**
     * Get the userName value.
     *
     * @return the userName value
     */
    public String userName() {
        return this.userName;
    }

    /**
     * Set the userName value.
     *
     * @param userName the userName value to set
     * @return the UserInner object itself.
     */
    public UserInner withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Get the publishingUserName value.
     *
     * @return the publishingUserName value
     */
    public String publishingUserName() {
        return this.publishingUserName;
    }

    /**
     * Set the publishingUserName value.
     *
     * @param publishingUserName the publishingUserName value to set
     * @return the UserInner object itself.
     */
    public UserInner withPublishingUserName(String publishingUserName) {
        this.publishingUserName = publishingUserName;
        return this;
    }

    /**
     * Get the publishingPassword value.
     *
     * @return the publishingPassword value
     */
    public String publishingPassword() {
        return this.publishingPassword;
    }

    /**
     * Set the publishingPassword value.
     *
     * @param publishingPassword the publishingPassword value to set
     * @return the UserInner object itself.
     */
    public UserInner withPublishingPassword(String publishingPassword) {
        this.publishingPassword = publishingPassword;
        return this;
    }

    /**
     * Get the scmUri value.
     *
     * @return the scmUri value
     */
    public String scmUri() {
        return this.scmUri;
    }

    /**
     * Set the scmUri value.
     *
     * @param scmUri the scmUri value to set
     * @return the UserInner object itself.
     */
    public UserInner withScmUri(String scmUri) {
        this.scmUri = scmUri;
        return this;
    }

}
