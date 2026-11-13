// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.BuilderTraitsDiagnosticRule;

import java.util.HashMap;
import java.util.Map;

/**
 * This diagnostic ensures that builder traits are applied correctly against client builders.
 */
public class AzureCoreBuilderTraitsDiagnosticRule extends BuilderTraitsDiagnosticRule {
    private static final Map<String, TraitClass> TRAITS;
    static {
        TRAITS = new HashMap<>();
        TRAITS.put("KeyCredentialTrait", new TraitClass(new TraitMethod("credential", "KeyCredential")));
        TRAITS.put("AzureKeyCredentialTrait", new TraitClass(new TraitMethod("credential", "AzureKeyCredential")));
        TRAITS.put("AzureNamedKeyCredentialTrait",
            new TraitClass(new TraitMethod("credential", "AzureNamedKeyCredential")));
        TRAITS.put("AzureSasCredentialTrait", new TraitClass(new TraitMethod("credential", "AzureSasCredential")));
        TRAITS.put("TokenCredentialTrait", new TraitClass(new TraitMethod("credential", "TokenCredential")));
        TRAITS.put("ConfigurationTrait", new TraitClass(new TraitMethod("configuration", "Configuration")));
        TRAITS.put("ConnectionStringTrait", new TraitClass(new TraitMethod("connectionString", "String")));
        TRAITS.put("EndpointTrait", new TraitClass(new TraitMethod("endpoint", "String")));
        TRAITS.put("HttpTrait",
            new TraitClass(TraitClass.BUILDER_PROTOCOL_HTTP, new TraitMethod("httpClient", "HttpClient"),
                new TraitMethod("pipeline", "HttpPipeline"), new TraitMethod("addPolicy", "HttpPipelinePolicy"),
                new TraitMethod("retryOptions", "RetryOptions"), new TraitMethod("httpLogOptions", "HttpLogOptions"),
                new TraitMethod("clientOptions", "ClientOptions")));
        TRAITS.put("AmqpTrait",
            new TraitClass(TraitClass.BUILDER_PROTOCOL_AMQP, new TraitMethod("retryOptions", "AmqpRetryOptions"),
                new TraitMethod("transportType", "AmqpTransportType"), new TraitMethod("proxyOptions", "ProxyOptions"),
                new TraitMethod("clientOptions", "ClientOptions")));
    }

    /**
     * Creates an azure core builder traits diagnostic rule.
     */
    public AzureCoreBuilderTraitsDiagnosticRule() {
        super(TRAITS);
    }
}
