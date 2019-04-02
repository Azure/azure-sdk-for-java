// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 *  Secret is the resource consisting of name, value, id and its other attributes inherited from {@link SecretInfo}.
 *  It is managed by Secret Service.
 */
public class Secret extends SecretInfo {

    /**
     * The secret value.
     */
    @JsonProperty(value = "value")
    private String value;


    public Secret() {

    }

    public Secret(String secretName, String value) {
        withName(secretName);
        this.value = value;
    }

    /**
     * Get the secret value.
     *
     * @return the secret value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the secret value.
     *
     * @param value the secret value to set
     * @return the Secret object itself.
     */
    public Secret withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Set the name value.
     *
     * @param name the name of the secret to be set.
     * @return the Secret object itself.
     */
    @Override
    public Secret withName(String name) {
        super.withName(name);
        return this;
    }


    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret withEnabled(Boolean enabled) {
        super.withEnabled(enabled);
        return this;
    }

    /**
     * Set the notBefore value.
     *
     * @param notBefore the notBefore value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret withNotBefore(OffsetDateTime notBefore) {
        super.withNotBefore(notBefore);
        return this;
    }

    /**
     * Set the expires value.
     *
     * @param expires the expiry time value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret withExpires(OffsetDateTime expires) {
        super.withExpires(expires);
        return this;
    }


    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret withId(String id) {
        super.withId(id);
        return this;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret withContentType(String contentType) {
        super.withContentType(contentType);
        return this;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the Secret object itself.
     */
    @Override
    public Secret withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

}

