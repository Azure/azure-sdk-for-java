package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.implementation.CosmosQueryRequestOptionsImpl;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;

public class CosmosRequestOptionsTransformer {


    private final RequestOptions requestOptions;
    private final CosmosQueryRequestOptionsImpl queryRequestOptions;
    private final CosmosDiagnosticsContext cosmosDiagnosticsContext;


    CosmosRequestOptionsTransformer(RequestOptions requestOptions, CosmosQueryRequestOptionsImpl queryRequestOptions, CosmosDiagnosticsContext cosmosDiagnosticsContext) {
        this.requestOptions= requestOptions;
        this.queryRequestOptions = queryRequestOptions;
        this.cosmosDiagnosticsContext = cosmosDiagnosticsContext;
    }

    public void applyOverride(CosmosCommonRequestOptions cosmosCommonRequestOptions) {
        // check they did not give us a RequestOption
        requestOptions.override(cosmosCommonRequestOptions);
    }

    public ICosmosCommonRequestOptions getRequestOptions() {
        return requestOptions;
    }

    public CosmosDiagnosticsContext getCosmosDiagnosticsContext() {
        return cosmosDiagnosticsContext;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers
            .CosmosRequestOptionsTransformerHelper
            .setCosmosRequestOptionsTransformerAccessor(
                new ImplementationBridgeHelpers
                    .CosmosRequestOptionsTransformerHelper.CosmosRequestOptionsTransformerAccessor() {

                    @Override
                    public CosmosRequestOptionsTransformer create(RequestOptions requestOptions, CosmosQueryRequestOptionsImpl queryRequestOptions,
                                                                  CosmosDiagnosticsContext cosmosDiagnosticsContext) {
                        return new CosmosRequestOptionsTransformer(requestOptions, queryRequestOptions, cosmosDiagnosticsContext);
                    }
                });
    }
}
