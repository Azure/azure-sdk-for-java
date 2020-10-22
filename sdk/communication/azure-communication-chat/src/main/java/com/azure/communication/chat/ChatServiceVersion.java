// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Chat Service supported by this client library.
 */
public enum ChatServiceVersion implements ServiceVersion {
    V2020_09_21_preview2("2020-09-21-preview2");

    private final String version;

    ChatServiceVersion(String version) {

        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {

        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link ChatServiceVersion}
     */
    public static ChatServiceVersion getLatest() {

        return V2020_09_21_preview2;
    }
}
