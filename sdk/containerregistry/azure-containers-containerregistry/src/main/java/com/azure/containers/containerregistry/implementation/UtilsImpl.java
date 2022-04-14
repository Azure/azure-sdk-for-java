// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.containers.containerregistry.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryCredentialsPolicy;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenService;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
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
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
public final class UtilsImpl {
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;
    private static final int HTTP_STATUS_CODE_NOT_FOUND;
    private static final int HTTP_STATUS_CODE_ACCEPTED;
    private static final String CONTINUATION_LINK_HEADER_NAME;
    private static final Pattern CONTINUATION_LINK_PATTERN;
    private static final ClientLogger LOGGER;

    public static final String DOCKER_DIGEST_HEADER_NAME;
    public static final String OCI_MANIFEST_MEDIA_TYPE;
    public static final String CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE;

    static {
        LOGGER = new ClientLogger(UtilsImpl.class);
        Map<String, String> properties = CoreUtils.getProperties("azure-containers-containerregistry.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
        HTTP_STATUS_CODE_NOT_FOUND = 404;
        HTTP_STATUS_CODE_ACCEPTED = 202;
        OCI_MANIFEST_MEDIA_TYPE = "application/vnd.oci.image.manifest.v1+json";
        DOCKER_DIGEST_HEADER_NAME = "docker-content-digest";
        CONTINUATION_LINK_HEADER_NAME = "Link";
        CONTINUATION_LINK_PATTERN = Pattern.compile("<(.+)>;.*");
        CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE = "Microsoft.ContainerRegistry";
    }

    private UtilsImpl() { }

    /**
     * This method builds the httpPipeline for the builders.
     * @param clientOptions The client options
     * @param logOptions http log options.
     * @param configuration configuration settings.
     * @param retryPolicy retry policy
     * @param retryOptions retry options
     * @param credential credentials.
     * @param perCallPolicies per call policies.
     * @param perRetryPolicies per retry policies.
     * @param httpClient http client
     * @param endpoint endpoint to be called
     * @param serviceVersion the service api version being targeted by the client.
     * @return returns the httpPipeline to be consumed by the builders.
     */
    public static HttpPipeline buildHttpPipeline(
        ClientOptions clientOptions,
        HttpLogOptions logOptions,
        Configuration configuration,
        RetryPolicy retryPolicy,
        RetryOptions retryOptions,
        TokenCredential credential,
        ContainerRegistryAudience audience,
        List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies,
        HttpClient httpClient,
        String endpoint,
        ContainerRegistryServiceVersion serviceVersion,
        ClientLogger logger) {

        ArrayList<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(
            new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, logOptions), CLIENT_NAME, CLIENT_VERSION, configuration));
        policies.add(new RequestIdPolicy());

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));
        policies.add(new CookiePolicy());
        policies.add(new AddDatePolicy());
        policies.add(new ContainerRegistryRedirectPolicy());

        policies.addAll(perRetryPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy(logOptions);

        // We generally put credential policy between BeforeRetry and AfterRetry policies and put Logging policy in the end.
        // However since ACR uses the rest endpoints of the service in the credential policy,
        // we want to be able to use the same pipeline (minus the credential policy) to have uniformity in the policy
        // pipelines across all ACR endpoints.
        if (credential == null) {
            logger.verbose("Credentials are null, enabling anonymous access");
        }

        ArrayList<HttpPipelinePolicy> credentialPolicies = clone(policies);
        credentialPolicies.add(loggingPolicy);

        if (audience == null)  {
            audience = ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD;
        }

        ContainerRegistryTokenService tokenService = new ContainerRegistryTokenService(
            credential,
            audience,
            endpoint,
            serviceVersion,
            new HttpPipelineBuilder()
                .policies(credentialPolicies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build(),
            JacksonAdapter.createDefaultSerializerAdapter());

        ContainerRegistryCredentialsPolicy credentialsPolicy = new ContainerRegistryCredentialsPolicy(tokenService);
        policies.add(credentialsPolicy);

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

    /**
     * This method computes the digest for the buffer content.
     * Docker digest is a SHA256 hash of the docker image content and is deterministic based on the image build.
     * @param buffer The buffer containing the image bytes.
     * @return SHA-256 digest for the given buffer.
     */
    public static String computeDigest(ByteBuffer buffer) {
        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(readOnlyBuffer);
            byte[] digest = md.digest();
            return "sha256:" + byteArrayToHex(digest);

        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("SHA-256 conversion failed with" + e);
            throw new RuntimeException(e);
        }
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private static String byteArrayToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Delete operation should be idempotent.
     * And so should result in a success in case the service response is 400 : Not found.
     * @param responseT The response object.
     * @param <T> The encapsulating value.
     * @return The transformed response object.
     */
    public static <T> Mono<Response<Void>> deleteResponseToSuccess(Response<T> responseT) {
        if (responseT.getStatusCode() != HTTP_STATUS_CODE_NOT_FOUND) {
            // In case of success scenario return Response<Void>.
            return getAcceptedDeleteResponse(responseT, responseT.getStatusCode());
        }

        // In case of 404, we still convert it to success i.e. no-op.
        return getAcceptedDeleteResponse(responseT, HTTP_STATUS_CODE_ACCEPTED);
    }

    static <T> Mono<Response<Void>> getAcceptedDeleteResponse(Response<T> responseT, int statusCode) {
        return Mono.just(new SimpleResponse<Void>(
            responseT.getRequest(),
            statusCode,
            responseT.getHeaders(),
            null));
    }

    /**
     * This method converts the API response codes into well known exceptions.
     * @param exception The exception returned by the rest client.
     * @return The exception returned by the public methods.
     */
    public static Throwable mapException(Throwable exception) {
        AcrErrorsException acrException = null;

        if (exception instanceof AcrErrorsException) {
            acrException = ((AcrErrorsException) exception);
        } else if (exception instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException) exception;
            Throwable throwable = runtimeException.getCause();
            if (throwable instanceof AcrErrorsException) {
                acrException = (AcrErrorsException) throwable;
            }
        }

        if (acrException == null) {
            return exception;
        }

        final HttpResponse errorHttpResponse = acrException.getResponse();
        final int statusCode = errorHttpResponse.getStatusCode();
        final String errorDetail = acrException.getMessage();

        switch (statusCode) {
            case 401:
                return new ClientAuthenticationException(errorDetail, acrException.getResponse(), exception);
            case 404:
                return new ResourceNotFoundException(errorDetail, acrException.getResponse(), exception);
            case 409:
                return new ResourceExistsException(errorDetail, acrException.getResponse(), exception);
            case 412:
                return new ResourceModifiedException(errorDetail, acrException.getResponse(), exception);
            default:
                return new HttpResponseException(errorDetail, acrException.getResponse(), exception);
        }
    }

    /**
     * This method parses the response to get the continuation token used to make the next pagination call.
     * The continuation token is returned by the service in the form of a header and not as a nextLink field.
     * @param listResponse response that is parsed.
     * @param <T> the model type that is being operated on.
     * @return paged response with the correct continuation token.
     */
    public static <T> PagedResponse<T> getPagedResponseWithContinuationToken(PagedResponse<T> listResponse) {
        return getPagedResponseWithContinuationToken(listResponse, values -> values);
    }

    /**
     * This method parses the response to get the continuation token used to make the next pagination call.
     * The continuation token is returned by the service in the form of a header and not as a nextLink field.
     *
     * <p>
     *      Per the Docker v2 HTTP API spec, the Link header is an RFC5988
     *      compliant rel='next' with URL to next result set, if available.
     *      See: https://docs.docker.com/registry/spec/api/
     *
     *      The URI reference can be obtained from link-value as follows:
     *        Link       = "Link" ":" #link-value
     *        link-value = "<" URI-Reference ">" * (";" link-param )
     *      See: https://tools.ietf.org/html/rfc5988#section-5
     * </p>
     * @param listResponse response that is parsed.
     * @param mapperFunction the function that maps the rest api response into the public model exposed by the client.
     * @param <T> The model type returned by the rest client.
     * @param <R> The model type returned by the public client.
     * @return paged response with the correct continuation token.
     */

    public static <T, R> PagedResponse<T> getPagedResponseWithContinuationToken(PagedResponse<R> listResponse, Function<List<R>, List<T>> mapperFunction) {
        Objects.requireNonNull(mapperFunction);

        String continuationLink = null;
        HttpHeaders headers = listResponse.getHeaders();

        if (headers != null) {
            String continuationLinkHeader = headers.getValue(CONTINUATION_LINK_HEADER_NAME);
            if (!CoreUtils.isNullOrEmpty(continuationLinkHeader)) {
                Matcher matcher = CONTINUATION_LINK_PATTERN.matcher(continuationLinkHeader);
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
     * Get the digest from the response header if available.
     * @param headers The headers to parse.
     * @return The digest value.
     */
    public static <T> String getDigestFromHeader(HttpHeaders headers) {
        return headers.getValue(DOCKER_DIGEST_HEADER_NAME);
    }
}
