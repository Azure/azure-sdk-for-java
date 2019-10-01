// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

final public class ServerProperties {

    final private String agent, version;

    public ServerProperties(String agent, String version) {
        this.agent = agent;
        this.version = version;
    }

    public String getAgent() {
        return this.agent;
    }

    public String getVersion() {
        return this.version;
    }
}
