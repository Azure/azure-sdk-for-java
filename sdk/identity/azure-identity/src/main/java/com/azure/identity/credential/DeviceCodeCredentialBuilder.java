// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.DeviceCodeChallenge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent credential builder for instantiating a {@link DeviceCodeCredential}.
 *
 * @see DeviceCodeCredential
 */
public class DeviceCodeCredentialBuilder extends AadCredentialBuilderBase<DeviceCodeCredentialBuilder> {
    private Consumer<DeviceCodeChallenge> deviceCodeChallengeConsumer;

    /**
     * Sets the port for the local HTTP server, for which {@code http://localhost:{port}} must be
     * registered as a valid reply URL on the application.
     *
     * @param deviceCodeChallengeConsumer a method allowing the user to meet the device code challenge
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public DeviceCodeCredentialBuilder deviceCodeChallengeConsumer(Consumer<DeviceCodeChallenge> deviceCodeChallengeConsumer) {
        this.deviceCodeChallengeConsumer = deviceCodeChallengeConsumer;
        return this;
    }

    /**
     * @return a {@link DeviceCodeCredential} with the current configurations.
     */
    public DeviceCodeCredential build() {
        List<String> missing = new ArrayList<>();
        if (clientId == null) {
            missing.add("clientId");
        }
        if (deviceCodeChallengeConsumer == null) {
            missing.add("deviceCodeChallengeConsumer");
        }
        if (missing.size() > 0) {
            throw new IllegalArgumentException("Must provide non-null values for "
                + String.join(", ", missing) + " properties in " + this.getClass().getSimpleName());
        }
        return new DeviceCodeCredential(clientId, deviceCodeChallengeConsumer, identityClientOptions);
    }
}
