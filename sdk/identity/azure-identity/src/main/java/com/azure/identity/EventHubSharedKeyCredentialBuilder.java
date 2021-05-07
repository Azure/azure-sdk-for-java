// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Fluent credential builder for instantiating a {@link EventHubSharedKeyCredential}.
 *
 * @see EventHubSharedKeyCredential
 */
public class EventHubSharedKeyCredentialBuilder extends AadCredentialBuilderBase<EventHubSharedKeyCredentialBuilder> {

    private static final String HASH_ALGORITHM = "HMACSHA256";

    private String sharedAccessPolicy;
    private SecretKeySpec secretKeySpec;
    private String sharedAccessSignature;

    /**
     * Sets the sharedAccessPolicy of the user.
     *
     * @param sharedAccessPolicy the sharedAccessPolicy of the user
     * @return the EventHubSharedKeyCredentialBuilder itself
     */
    public EventHubSharedKeyCredentialBuilder sharedAccessPolicy(String sharedAccessPolicy) {
        this.sharedAccessPolicy = sharedAccessPolicy;
        return this;
    }

    /**
     * Sets the shardAccessKey of the user.
     *
     * @param sharedAccessKey the sharedAccessKey of the user
     * @return the ServiceBusSharedKeyCredentialBuilder itself
     */
    public EventHubSharedKeyCredentialBuilder sharedAccessKey(String sharedAccessKey) {
        this.secretKeySpec = new SecretKeySpec(sharedAccessKey.getBytes(UTF_8), HASH_ALGORITHM);
        return this;
    }

    /**
     * Sets the sharedAccessSignature of the user.
     *
     * @param sharedAccessSignature the sharedAccessSignature of the user
     * @return the ServiceBusSharedKeyCredentialBuilder itself
     */
    public EventHubSharedKeyCredentialBuilder sharedAccessSignature(String sharedAccessSignature) {
        this.sharedAccessSignature = sharedAccessSignature;
        return this;
    }

    /**
     * Creates a new {@link EventHubSharedKeyCredential} with the current configurations.
     *
     * @return a {@link EventHubSharedKeyCredential} with the current configurations.
     */
    public EventHubSharedKeyCredential build() {
        Map<String, Object> validationMap = new HashMap<String, Object>();
        validationMap.put("sharedAccessPolicy", sharedAccessPolicy);
        validationMap.put("sharedAccessKey", secretKeySpec);
        validationMap.put("sharedAccessSignature", sharedAccessSignature);
        ValidationUtil.validateForSharedAccessKey(getClass().getSimpleName(), validationMap);
        return new EventHubSharedKeyCredential(sharedAccessPolicy, secretKeySpec, sharedAccessSignature);
    }
}
