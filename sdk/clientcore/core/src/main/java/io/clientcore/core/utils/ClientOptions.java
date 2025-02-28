// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.TypeConditions;
import io.clientcore.core.http.pipeline.SetUserAgentPolicy;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

/**
 * General configuration options for clients.
 */
@Metadata(conditions = TypeConditions.FLUENT)
public class ClientOptions {
    private static final String INVALID_APPLICATION_ID_SPACE = "'applicationId' cannot contain spaces.";

    // ClientOptions is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(ClientOptions.class);

    private String applicationId;
    private InstrumentationOptions instrumentationOptions;

    /**
     * Creates a new instance of {@link ClientOptions}.
     */
    public ClientOptions() {
    }

    /**
     * Gets the application ID.
     * <p>
     * The {@code applicationId} is used to configure {@link SetUserAgentPolicy} for telemetry/monitoring purposes.
     *
     * @return The application ID.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID.
     * <p>
     * The {@code applicationId} is used to configure {@link SetUserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create ClientOptions with application ID 'myApplicationId'</p>
     *
     * <!-- src_embed io.clientcore.core.util.ClientOptions.setApplicationId#String -->
     * <pre>
     * ClientOptions clientOptions = new ClientOptions&#40;&#41;
     *     .setApplicationId&#40;&quot;myApplicationId&quot;&#41;;
     * </pre>
     * <!-- end io.clientcore.core.util.ClientOptions.setApplicationId#String -->
     *
     * @param applicationId The application ID.
     * @return The updated ClientOptions object.
     * @throws IllegalArgumentException If {@code applicationId} contains spaces.
     */
    public ClientOptions setApplicationId(String applicationId) {
        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.contains(" ")) {
                throw LOGGER.logThrowableAsError(new IllegalArgumentException(INVALID_APPLICATION_ID_SPACE));
            }
        }

        this.applicationId = applicationId;
        return this;
    }

    /**
     * Sets the {@link InstrumentationOptions} that are used to apply instrumentation to the client.
     *
     * @param instrumentationOptions instance of {@link InstrumentationOptions} to set.
     * @return The updated {@link ClientOptions} object.
     */
    public ClientOptions setInstrumentationOptions(InstrumentationOptions instrumentationOptions) {
        this.instrumentationOptions = instrumentationOptions;
        return this;
    }

    /**
     * Gets the {@link InstrumentationOptions} that are used to apply instrumentation to the client.
     *
     * @return The {@link InstrumentationOptions} that are used to apply instrumentation to the client.
     */
    public InstrumentationOptions getInstrumentationOptions() {
        return instrumentationOptions;
    }
}
