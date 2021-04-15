// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenService;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JacksonAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the utility class that includes helper methods used across our clients.
 */
public final class Utils {
    private static final String CONTINUATIONLINK_HEADER_NAME;
    private static final Pattern CONTINUATIONLINK_PATTERN;
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-search-documents.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");

        CONTINUATIONLINK_HEADER_NAME = "Link";
        CONTINUATIONLINK_PATTERN = Pattern.compile("<(.+)>;.*");
    }

    /**
     * This method parses the response to get the continuation token used to make the next pagination call.
     * The continuation token is returned by the service in the form of a header and not as a nextLink field.
     * @param listResponse response that is parsed.
     * @param <T> the model type that is being operated on.
     * @return paged response with the correct continuation token.
     */
    static <T> PagedResponse<T> getPagedResponseWithContinuationToken(PagedResponse<T> listResponse) {
        return Utils.getPagedResponseWithContinuationToken(listResponse, values -> values);
    }

    /**
     * This method parses the response to get the continuation token used to make the next pagination call.
     * The continuation token is returned by the service in the form of a header and not as a nextLink field.
     * @param listResponse response that is parsed.
     * @param mapperFunction the function that maps the rest api response into the public model exposed by the client.
     * @param <T> The model type returned by the rest client.
     * @param <R> The model type returned by the public client.
     * @return paged response with the correct continuation token.
     */
    static <T, R> PagedResponse<T> getPagedResponseWithContinuationToken(PagedResponse<R> listResponse, Function<List<R>, List<T>> mapperFunction) {
        Objects.requireNonNull(mapperFunction);

        String continuationLink = null;
        HttpHeaders headers = listResponse.getHeaders();

        if (headers != null) {
            String continuationLinkHeader = headers.getValue(CONTINUATIONLINK_HEADER_NAME);
            if (!CoreUtils.isNullOrEmpty(continuationLinkHeader)) {
                Matcher matcher = CONTINUATIONLINK_PATTERN.matcher(continuationLinkHeader);
                if (matcher.matches()) {
                    if (matcher.groupCount() == 1) {
                        continuationLink = matcher.group(1);
                    }
                }
            }
        }

        List<T> values = mapperFunction.apply(listResponse.getValue());

        return new PagedResponseBase<String, T>(
            listResponse.getRequest(),
            listResponse.getStatusCode(),
            listResponse.getHeaders(),
            values,
            continuationLink,
            null
        );
    }

    /**
     * This method converts the API response codes into well known exceptions.
     * @param exception The exception returned by the rest client.
     * @return The exception returned by the public methods.
     */
    static Throwable mapException(Throwable exception) {
        if (!(exception instanceof AcrErrorsException)) {
            return exception;
        }

        final AcrErrorsException errorsException = ((AcrErrorsException) exception);
        final HttpResponse errorHttpResponse = errorsException.getResponse();

        final int statusCode = errorHttpResponse.getStatusCode();
        final String errorDetail = errorsException.getMessage();

        switch (statusCode) {
            case 401:
                return new ClientAuthenticationException(errorDetail, errorsException.getResponse(), exception);
            case 404:
                return new ResourceNotFoundException(errorDetail, errorsException.getResponse(), exception);
            case 409:
                return new ResourceExistsException(errorDetail, errorsException.getResponse(), exception);
            case 412:
                return new ResourceModifiedException(errorDetail, errorsException.getResponse(), exception);
            default:
                return new HttpResponseException(errorDetail, errorsException.getResponse(), exception);
        }
    }

    /**
     * This method maps a given response to another based on the mapper function.
     * @param response response that is parsed.
     * @param mapFunction the function that maps the rest api response into the public model exposed by the client.
     * @param <T> The model type returned by the rest client.
     * @param <R> The model type returned by the public client.
     * @return paged response with the correct continuation token.
     */
    static <T, R> Response<R> mapResponse(Response<T> response, Function<T, R> mapFunction) {
        if (response == null || mapFunction == null) {
            return null;
        }

        return new ResponseBase<String, R>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            mapFunction.apply(response.getValue()),
            null);
    }

    /**
     * This method builds the httpPipeline for the builders.
     * @param clientOptions The client options
     * @param logOptions http log options.
     * @param configuration configuration settings.
     * @param retryPolicy retry policy
     * @param credential credentials.
     * @param perCallPolicies per call policies.
     * @param perRetryPolicies per retry policies.
     * @param httpClient http client
     * @param endpoint endpoint to be called
     * @return returns the httpPipeline to be consumed by the builders.
     */
    static HttpPipeline buildHttpPipeline(
        ClientOptions clientOptions,
        HttpLogOptions logOptions,
        Configuration configuration,
        RetryPolicy retryPolicy,
        TokenCredential credential,
        List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies,
        HttpClient httpClient,
        String endpoint) {

        ArrayList<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(
            new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, logOptions), CLIENT_NAME, CLIENT_VERSION, configuration));
        policies.add(new RequestIdPolicy());

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
        policies.add(new CookiePolicy());
        policies.add(new AddDatePolicy());

        policies.addAll(perRetryPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy(logOptions);

        // We generally put credential policy between BeforeRetry and AfterRetry policies and put Logging policy in the end.
        // However since ACR uses the rest endpoints of the service in the credential policy,
        // we want to be able to use the same pipeline (minus the credential policy) to have uniformity in the policy
        // pipelines across all ACR endpoints.
        if (credential != null) {
            ArrayList<HttpPipelinePolicy> credentialPolicies = clone(policies);
            credentialPolicies.add(loggingPolicy);

            ContainerRegistryTokenService tokenService = new ContainerRegistryTokenService(
                credential,
                endpoint,
                new HttpPipelineBuilder()
                    .policies(credentialPolicies.toArray(new HttpPipelinePolicy[0]))
                    .httpClient(httpClient)
                    .build(),
                JacksonAdapter.createDefaultSerializerAdapter());

            ContainerRegistryCredentialsPolicy credentialsPolicy = new ContainerRegistryCredentialsPolicy(tokenService);
            policies.add(credentialsPolicy);
        }

        policies.add(loggingPolicy);
        HttpPipeline httpPipeline =
            new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();
        return httpPipeline;
    }

    private static ArrayList<HttpPipelinePolicy> clone(ArrayList<HttpPipelinePolicy> policies) {
        ArrayList<HttpPipelinePolicy> clonedPolicy = new ArrayList<>();
        for (HttpPipelinePolicy policy:policies) {
            clonedPolicy.add(policy);
        }

        return clonedPolicy;
    }
}
