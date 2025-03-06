// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import io.clientcore.core.utils.configuration.Configuration;

/**
 * Represents abstract base for Client Options used in Managed Identity OAuth Flow .
 */
public abstract class ClientOptionsBase implements Cloneable {

    private HttpPipelineOptions httpPipelineOptions;
    private MsalCommonOptions msalCommonOptions;
    private Configuration configuration;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public ClientOptionsBase() {
        configuration = Configuration.getGlobalConfiguration();
    }

    /**
     * Gets the configured configuration store.
     *
     * @return the configured {@link Configuration} store.
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Sets the configuration store.
     *
     * @param configuration the configuration store
     * @return the ClientOptionsBase itself.
     */
    public ClientOptionsBase setConfigurationStore(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Gets the Msal configuration options.
     * @return the msal config options.
     */
    public MsalCommonOptions getMsalCommonOptions() {
        if (this.msalCommonOptions == null) {
            this.msalCommonOptions = new MsalCommonOptions(configuration);
        }
        return this.msalCommonOptions;
    }

    /**
     * Gets the Http pipeline options.
     * @return the http pipeline options.
     */
    public HttpPipelineOptions getHttpPipelineOptions() {
        if (this.httpPipelineOptions == null) {
            this.httpPipelineOptions = new HttpPipelineOptions();
        }
        return this.httpPipelineOptions;
    }

    /**
     * Sets the Http pipeline options.
     *
     * @param pipelineOptions the http pipeline options.
     * @return the ClientOptionsBase itself.
     */
    ClientOptionsBase setHttpPipelineOptions(HttpPipelineOptions pipelineOptions) {
        this.httpPipelineOptions = pipelineOptions;
        return this;
    }

    /**
     * Sets the Msal configuration options.
     *
     * @param msalCommonOptions the msal configuration options.
     * @return the ClientOptionsBase itself.
     */
    ClientOptionsBase setMsalCommonOptions(MsalCommonOptions msalCommonOptions) {
        this.msalCommonOptions = msalCommonOptions;
        return this;
    }

    public abstract ClientOptionsBase clone();
}
