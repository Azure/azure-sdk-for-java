package com.azure.tools.apiview.processor.diagnostics.rules.clientcore;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.BuilderTraitsDiagnosticRule;

import java.util.*;

/**
 * This diagnostic ensures that builder traits are applied correctly against client builders.
 */
public class ClientCoreBuilderTraitsDiagnosticRule extends BuilderTraitsDiagnosticRule {
    private static final Map<String, TraitClass> traits;
    static {
        traits = new HashMap<>();
        traits.put("KeyCredentialTrait", new TraitClass(
                new TraitMethod("credential", "KeyCredential")
        ));
        traits.put("ConfigurationTrait", new TraitClass(
                new TraitMethod("configuration", "Configuration")
        ));
        traits.put("ProxyTrait", new TraitClass(
                new TraitMethod("proxyOptions", "ProxyOptions")
        ));
        traits.put("EndpointTrait", new TraitClass(
                new TraitMethod("endpoint", "String")
        ));
        traits.put("HttpTrait", new TraitClass(TraitClass.BUILDER_PROTOCOL_HTTP,
                new TraitMethod("httpClient", "HttpClient"),
                new TraitMethod("httpPipeline", "HttpPipeline"),
                new TraitMethod("addHttpPipelinePolicy", "HttpPipelinePolicy"),
                new TraitMethod("httpRetryOptions", "HttpRetryOptions"),
                new TraitMethod("httpLogOptions", "HttpLogOptions"),
                new TraitMethod("httpRedirectOptions", "HttpRedirectOptions")
        ));
    }

    public ClientCoreBuilderTraitsDiagnosticRule() {
        super(traits);
    }
}
