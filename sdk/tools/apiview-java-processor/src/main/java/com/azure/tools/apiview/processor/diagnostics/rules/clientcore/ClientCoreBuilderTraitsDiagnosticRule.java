// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.clientcore;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.BuilderTraitsDiagnosticRule;
import java.util.HashMap;
import java.util.Map;

/**
 * This diagnostic ensures that builder traits are applied correctly against client builders.
 */
public class ClientCoreBuilderTraitsDiagnosticRule extends BuilderTraitsDiagnosticRule {
    private static final Map<String, TraitClass> TRAITS;
    static {
        TRAITS = new HashMap<>();
        TRAITS.put("KeyCredentialTrait", new TraitClass(new TraitMethod("credential", "KeyCredential")));
        TRAITS.put("ConfigurationTrait", new TraitClass(new TraitMethod("configuration", "Configuration")));
        TRAITS.put("ProxyTrait", new TraitClass(new TraitMethod("proxyOptions", "ProxyOptions")));
        TRAITS.put("EndpointTrait", new TraitClass(new TraitMethod("endpoint", "String")));
        TRAITS.put("HttpTrait",
            new TraitClass(TraitClass.BUILDER_PROTOCOL_HTTP, new TraitMethod("httpClient", "HttpClient"),
                new TraitMethod("httpPipeline", "HttpPipeline"),
                new TraitMethod("addHttpPipelinePolicy", "HttpPipelinePolicy"),
                new TraitMethod("httpRetryOptions", "HttpRetryOptions"),
                new TraitMethod("httpLogOptions", "HttpLogOptions"),
                new TraitMethod("httpRedirectOptions", "HttpRedirectOptions")));
    }

    /**
     * Creates a client core builder traits diagnostic rule.
     */
    public ClientCoreBuilderTraitsDiagnosticRule() {
        super(TRAITS);
    }
}
