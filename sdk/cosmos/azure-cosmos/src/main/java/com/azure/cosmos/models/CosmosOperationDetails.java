// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosRequestContext;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OverridableRequestOptions;

/**
 * Encapsulates the details of an operation and allows for overriding some properties in the internal request options.
 *
 */
public final class CosmosOperationDetails {

    private final OverridableRequestOptions requestOptions;
    private final CosmosDiagnosticsContext cosmosDiagnosticsContext;

    CosmosOperationDetails(OverridableRequestOptions requestOptions, CosmosDiagnosticsContext cosmosDiagnosticsContext) {
        this.requestOptions = requestOptions;
        this.cosmosDiagnosticsContext = cosmosDiagnosticsContext;
    }

    /**
     * Applies the options in CosmosRequestOptions to the internal request options.
     *
     * @param cosmosRequestOptions the common request options for overriding.
     */
    public void setRequestOptions(CosmosRequestOptions cosmosRequestOptions) {
        requestOptions.override(cosmosRequestOptions);
    }

    /**
     * Gets the request context associated with an operation.
     *
     * @return the request context.
     */
    public CosmosRequestContext getRequestContext() {
        return ImplementationBridgeHelpers.CosmosRequestContextHelper.getCosmosRequestContextAccessor().create(requestOptions);
    }

    /**
     * Gets the diagnostics context associated with an operation.
     *
     * @return the diagnostics context.
     */
    public CosmosDiagnosticsContext getDiagnosticsContext() {
        return cosmosDiagnosticsContext;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosOperationDetailsHelper
            .setCosmosOperationDetailsAccessor(
                    CosmosOperationDetails::new);
    }
}
