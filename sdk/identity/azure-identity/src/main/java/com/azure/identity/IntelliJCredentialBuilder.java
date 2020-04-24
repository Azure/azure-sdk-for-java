// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.CoreUtils;

import java.util.Objects;

/**
 * Fluent credential builder for instantiating a {@link IntelliJCredential}.
 *
 * @see IntelliJCredential
 */
public class IntelliJCredentialBuilder extends CredentialBuilderBase<IntelliJCredentialBuilder> {

    /**
     * Specifies the keep pass database path to read IntelliJ credentials on windows platform. This is required
     * on windows platform.
     *
     * <p>This path can be located in the IntelliJ IDE.
     * Windows: File -&gt; Settings -&gt; Appearance & Behavior -&gt; System Settings -&gt; Passwords </p>
     *
     * @param databasePath the path to the keep pass database.
     * @throws IllegalArgumentException if {@code databasePath is either not specified or is empty}
     * @return An updated instance of this builder with the keep pass database path set as specified.
     */
    public IntelliJCredentialBuilder windowsKeepPassDatabasePath(String databasePath) {
        if (CoreUtils.isNullOrEmpty(databasePath)) {
            throw new IllegalArgumentException("The windows keep pass database path is either empty or not configured."
                                                   + " Please configure it on the builder.");
        }
        this.identityClientOptions.setKeepPassDatabasePath(databasePath);
        return this;
    }

    /**
     * Creates a new {@link IntelliJCredential} with the current configurations.
     *
     * @return a {@link IntelliJCredential} with the current configurations.
     */
    public IntelliJCredential build() {
        return new IntelliJCredential(identityClientOptions);
    }
}
