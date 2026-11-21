package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.diagnostics.rules.utils.BuilderTraitsDiagnosticRule;

import java.util.HashMap;
import java.util.Map;

/**
 * This diagnostic ensures that builder traits are applied correctly against client builders.
 */
public class AzureCoreBuilderTraitsDiagnosticRule extends BuilderTraitsDiagnosticRule {
    private static final Map<String, TraitClass> traits;
    static {
        traits = new HashMap<>();
        traits.put("KeyCredentialTrait", new TraitClass(new TraitMethod("credential", "KeyCredential")));
        traits.put("AzureKeyCredentialTrait", new TraitClass(new TraitMethod("credential", "AzureKeyCredential")));
        traits.put("AzureNamedKeyCredentialTrait",
            new TraitClass(new TraitMethod("credential", "AzureNamedKeyCredential")));
        traits.put("AzureSasCredentialTrait", new TraitClass(new TraitMethod("credential", "AzureSasCredential")));
        traits.put("TokenCredentialTrait", new TraitClass(new TraitMethod("credential", "TokenCredential")));
        traits.put("ConfigurationTrait", new TraitClass(new TraitMethod("configuration", "Configuration")));
        traits.put("ConnectionStringTrait", new TraitClass(new TraitMethod("connectionString", "String")));
        traits.put("EndpointTrait", new TraitClass(new TraitMethod("endpoint", "String")));
        traits.put("HttpTrait",
            new TraitClass(TraitClass.BUILDER_PROTOCOL_HTTP, new TraitMethod("httpClient", "HttpClient"),
                new TraitMethod("pipeline", "HttpPipeline"), new TraitMethod("addPolicy", "HttpPipelinePolicy"),
                new TraitMethod("retryOptions", "RetryOptions"), new TraitMethod("httpLogOptions", "HttpLogOptions"),
                new TraitMethod("clientOptions", "ClientOptions")));
        traits.put("AmqpTrait",
            new TraitClass(TraitClass.BUILDER_PROTOCOL_AMQP, new TraitMethod("retryOptions", "AmqpRetryOptions"),
                new TraitMethod("transportType", "AmqpTransportType"), new TraitMethod("proxyOptions", "ProxyOptions"),
                new TraitMethod("clientOptions", "ClientOptions")));
    }

    public AzureCoreBuilderTraitsDiagnosticRule() {
        super(traits);
    }
}
