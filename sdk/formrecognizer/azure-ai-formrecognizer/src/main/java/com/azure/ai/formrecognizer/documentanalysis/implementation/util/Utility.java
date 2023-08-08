// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisAudience;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String COGNITIVE_TRACING_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    private static final String DEFAULT_SCOPE = "/.default";

    static {
        Map<String, String> properties = CoreUtils.getProperties(Constants.FORM_RECOGNIZER_PROPERTIES);
        CLIENT_NAME = properties.getOrDefault(Constants.NAME, "UnknownName");
        CLIENT_VERSION = properties.getOrDefault(Constants.VERSION, "UnknownVersion");
    }

    private Utility() {
    }

    public static HttpPipeline buildHttpPipeline(ClientOptions clientOptions, HttpLogOptions logOptions,
                                                 Configuration configuration, RetryPolicy retryPolicy,
                                                 RetryOptions retryOptions, AzureKeyCredential azureKeyCredential,
                                                 TokenCredential tokenCredential, DocumentAnalysisAudience audience,
                                                 List<HttpPipelinePolicy> perCallPolicies,
                                                 List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient) {

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ClientOptions buildClientOptions = (clientOptions == null) ? Constants.DEFAULT_CLIENT_OPTIONS : clientOptions;
        HttpLogOptions buildLogOptions = (logOptions == null) ? Constants.DEFAULT_LOG_OPTIONS : logOptions;

        String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
        httpPipelinePolicies.add(new AddHeadersPolicy(Constants.DEFAULT_HTTP_HEADERS));
        httpPipelinePolicies.add(new AddHeadersFromContextPolicy());
        httpPipelinePolicies.add(new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        httpPipelinePolicies.add(new RequestIdPolicy());

        httpPipelinePolicies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(httpPipelinePolicies);
        httpPipelinePolicies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        httpPipelinePolicies.add(new AddDatePolicy());

        // Authentications
        if (tokenCredential != null) {
            if (audience == null) {
                audience = DocumentAnalysisAudience.AZURE_PUBLIC_CLOUD;
            }
            httpPipelinePolicies.add(new BearerTokenAuthenticationPolicy(tokenCredential,
                audience + DEFAULT_SCOPE));
        } else if (azureKeyCredential != null) {
            httpPipelinePolicies.add(new AzureKeyCredentialPolicy(Constants.OCP_APIM_SUBSCRIPTION_KEY,
                azureKeyCredential));
        } else {
            // Throw exception that azureKeyCredential and tokenCredential cannot be null
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
        httpPipelinePolicies.addAll(perRetryPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(httpPipelinePolicies);

        HttpHeaders headers = new HttpHeaders();
        buildClientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));
        if (headers.getSize() > 0) {
            httpPipelinePolicies.add(new AddHeadersPolicy(headers));
        }

        httpPipelinePolicies.add(new HttpLoggingPolicy(buildLogOptions));

        return new HttpPipelineBuilder()
            .clientOptions(buildClientOptions)
            .httpClient(httpClient)
            .policies(httpPipelinePolicies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

    /**
     * Extracts the result ID from the URL.
     *
     * @param operationLocation The URL specified in the 'Operation-Location' response header containing the
     * resultId used to track the progress and obtain the result of the analyze operation.
     * @return The resultId used to track the progress.
     */
    public static String parseResultId(String operationLocation) {

        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            int firstIndex = operationLocation.indexOf('?');
            if (firstIndex != -1 && lastIndex != -1) {
                return operationLocation.substring(operationLocation.lastIndexOf('/') + 1,
                    operationLocation.indexOf('?'));
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }

    /*
     * Poller's ACTIVATION operation that takes URL as input.
     */
    public static Function<PollingContext<OperationResult>, Mono<OperationResult>>
        activationOperation(
        Supplier<Mono<OperationResult>> activationOperation,
        ClientLogger logger) {
        return pollingContext -> {
            try {
                return activationOperation.get().onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /**
     * Generates a random UUID String.
     * @return the UUID model Identifier.
     */
    public static String generateRandomModelID() {
        return UUID.randomUUID().toString();
    }
    public static Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    public static Context getTracingContext(Context context) {
        if (context == null) {
            context = Context.NONE;
        }
        return context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
    }

    public static BuildDocumentModelOptions getBuildDocumentModelOptions(
        BuildDocumentModelOptions buildDocumentModelOptions) {
        buildDocumentModelOptions =  buildDocumentModelOptions == null
            ? new BuildDocumentModelOptions() : buildDocumentModelOptions;
        return buildDocumentModelOptions;
    }

    public static CopyAuthorizationOptions getCopyAuthorizationOptions(
        CopyAuthorizationOptions copyAuthorizationOptions) {
        copyAuthorizationOptions = copyAuthorizationOptions == null
            ? new CopyAuthorizationOptions() : copyAuthorizationOptions;
        return copyAuthorizationOptions;
    }

    public static ComposeDocumentModelOptions getComposeModelOptions(ComposeDocumentModelOptions userProvidedOptions) {
        return userProvidedOptions == null ? new ComposeDocumentModelOptions() : userProvidedOptions;
    }

    public static AnalyzeDocumentOptions getAnalyzeDocumentOptions(AnalyzeDocumentOptions userProvidedOptions) {
        return userProvidedOptions == null ? new AnalyzeDocumentOptions() : userProvidedOptions;
    }
}
