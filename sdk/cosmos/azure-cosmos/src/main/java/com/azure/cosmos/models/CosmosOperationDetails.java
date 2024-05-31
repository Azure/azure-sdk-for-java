package com.azure.cosmos.models;

import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OverridableRequestOptions;

/**
 * Encapsulates the details of an operation and allows for overriding some properties in the internal request options.
 *
 */
public class CosmosOperationDetails {

    private final OverridableRequestOptions requestOptions;
    private final CosmosDiagnosticsContext cosmosDiagnosticsContext;

    CosmosOperationDetails(OverridableRequestOptions requestOptions, CosmosDiagnosticsContext cosmosDiagnosticsContext) {
        this.requestOptions = requestOptions;
        this.cosmosDiagnosticsContext = cosmosDiagnosticsContext;
    }

    /**
     * Applies the options in the CosmosCommonRequestOptions to the internal request options.
     *
     */
    public void setCommonOptions(CosmosCommonRequestOptions cosmosCommonRequestOptions) {
        requestOptions.override(cosmosCommonRequestOptions);
    }

    /**
     * Gets the internal request options associated with an operation.
     *
     * @return the internal request options.
     */
    public ICosmosCommonRequestOptions getRequestOptions() {
        return requestOptions;
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
