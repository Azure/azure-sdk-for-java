// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.containers.containerregistry.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryCredentialsPolicy;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenService;
import com.azure.containers.containerregistry.implementation.models.ManifestAttributesBase;
import com.azure.containers.containerregistry.implementation.models.TagAttributesBase;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
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
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This is the utility class that includes helper methods used across our clients.
 */
public final class UtilsImpl {
    private static final ClientLogger LOGGER = new ClientLogger(UtilsImpl.class);
    private static final Map<String, String> PROPERTIES = CoreUtils.getProperties("azure-containers-containerregistry.properties");
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault("name", "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault("version", "UnknownVersion");
    private static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
    private static final int HTTP_STATUS_CODE_ACCEPTED = 202;
    private static final HttpHeaderName CONTINUATION_LINK_HEADER_NAME = HttpHeaderName.fromString("Link");
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    public static final HttpHeaderName DOCKER_DIGEST_HEADER_NAME = HttpHeaderName.fromString("docker-content-digest");
    public static final String OCI_MANIFEST_MEDIA_TYPE = "application/vnd.oci.image.manifest.v1+json";
    public static final String CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE = "Microsoft.ContainerRegistry";

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
        ContainerRegistryServiceVersion serviceVersion) {

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
            LOGGER.verbose("Credentials are null, enabling anonymous access");
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

    @SuppressWarnings("unchecked")
    private static ArrayList<HttpPipelinePolicy> clone(ArrayList<HttpPipelinePolicy> policies) {
        return (ArrayList<HttpPipelinePolicy>) policies.clone();
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
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
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
    public static <T> Response<Void> deleteResponseToSuccess(Response<T> responseT) {
        if (responseT.getStatusCode() != HTTP_STATUS_CODE_NOT_FOUND) {
            // In case of success scenario return Response<Void>.
            return getAcceptedDeleteResponse(responseT, responseT.getStatusCode());
        }

        // In case of 404, we still convert it to success i.e. no-op.
        return getAcceptedDeleteResponse(responseT, HTTP_STATUS_CODE_ACCEPTED);
    }

    private static <T> Response<Void> getAcceptedDeleteResponse(Response<T> responseT, int statusCode) {
        return new SimpleResponse<Void>(
            responseT.getRequest(),
            statusCode,
            responseT.getHeaders(),
            null);
    }

    /**
     * This method converts the API response codes into well known exceptions.
     * @param exception The exception returned by the rest client.
     * @return The exception returned by the public methods.
     */
    public static Throwable mapException(Throwable exception) {
        HttpResponseException acrException = null;

        if (exception instanceof HttpResponseException) {
            acrException = ((HttpResponseException) exception);
        } else if (exception instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException) exception;
            Throwable throwable = runtimeException.getCause();
            if (throwable instanceof HttpResponseException) {
                acrException = (HttpResponseException) throwable;
            }
        }

        if (acrException == null) {
            return exception;
        }

        return mapAcrErrorsException(acrException);
    }

    public static HttpResponseException mapAcrErrorsException(HttpResponseException acrException) {
        final HttpResponse errorHttpResponse = acrException.getResponse();
        final int statusCode = errorHttpResponse.getStatusCode();
        final String errorDetail = acrException.getMessage();

        switch (statusCode) {
            case 401:
                return new ClientAuthenticationException(errorDetail, acrException.getResponse(), acrException);
            case 404:
                return new ResourceNotFoundException(errorDetail, acrException.getResponse(), acrException);
            case 409:
                return new ResourceExistsException(errorDetail, acrException.getResponse(), acrException);
            case 412:
                return new ResourceModifiedException(errorDetail, acrException.getResponse(), acrException);
            default:
                return new HttpResponseException(errorDetail, acrException.getResponse(), acrException);
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

        String continuationLink = getContinuationLink(listResponse.getHeaders());
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

    private static String getContinuationLink(HttpHeaders headers) {
        String continuationLinkHeader = headers.getValue(CONTINUATION_LINK_HEADER_NAME);
        if (!CoreUtils.isNullOrEmpty(continuationLinkHeader) && continuationLinkHeader.charAt(0) == '<') {
            int endIndex = continuationLinkHeader.indexOf(">;");
            if (endIndex < 2) {
                LOGGER.warning("expected 'Link' header value - '{}'", continuationLinkHeader);
            }
            return continuationLinkHeader.substring(1, endIndex);
        }

        return null;
    }

    public static List<ArtifactManifestProperties> mapManifestsProperties(List<ManifestAttributesBase> baseArtifacts,
                                                                          String repositoryName,
                                                                          String registryLoginServer) {
        if (baseArtifacts == null) {
            return null;
        }

        return baseArtifacts.stream().map(value -> {
            ArtifactManifestProperties manifestProperties = new ArtifactManifestProperties()
                .setDeleteEnabled(value.isDeleteEnabled())
                .setListEnabled(value.isListEnabled())
                .setWriteEnabled(value.isWriteEnabled())
                .setReadEnabled(value.isReadEnabled());

            ArtifactManifestPropertiesHelper.setRepositoryName(manifestProperties, repositoryName);
            ArtifactManifestPropertiesHelper.setRegistryLoginServer(manifestProperties, registryLoginServer);
            ArtifactManifestPropertiesHelper.setDigest(manifestProperties, value.getDigest());
            ArtifactManifestPropertiesHelper.setRelatedArtifacts(manifestProperties, value.getRelatedArtifacts());
            ArtifactManifestPropertiesHelper.setCpuArchitecture(manifestProperties, value.getArchitecture());
            ArtifactManifestPropertiesHelper.setOperatingSystem(manifestProperties, value.getOperatingSystem());
            ArtifactManifestPropertiesHelper.setCreatedOn(manifestProperties, value.getCreatedOn());
            ArtifactManifestPropertiesHelper.setlastUpdatedOn(manifestProperties, value.getLastUpdatedOn());
            ArtifactManifestPropertiesHelper.setSizeInBytes(manifestProperties, value.getSize());
            ArtifactManifestPropertiesHelper.setTags(manifestProperties, value.getTags());
            return manifestProperties;
        }).collect(Collectors.toList());
    }

    public static List<ArtifactTagProperties> getTagProperties(List<TagAttributesBase> baseValues,
                                                               String repositoryName) {
        Objects.requireNonNull(baseValues);

        return baseValues.stream().map(value -> {
            ArtifactTagProperties tagProperties = new ArtifactTagProperties()
                .setDeleteEnabled(value.isDeleteEnabled())
                .setReadEnabled(value.isReadEnabled())
                .setListEnabled(value.isListEnabled())
                .setWriteEnabled(value.isWriteEnabled());

            ArtifactTagPropertiesHelper.setCreatedOn(tagProperties, value.getCreatedOn());
            ArtifactTagPropertiesHelper.setlastUpdatedOn(tagProperties, value.getLastUpdatedOn());
            ArtifactTagPropertiesHelper.setRepositoryName(tagProperties, repositoryName);
            ArtifactTagPropertiesHelper.setName(tagProperties, value.getName());
            ArtifactTagPropertiesHelper.setDigest(tagProperties, value.getDigest());
            return tagProperties;
        }).collect(Collectors.toList());
    }

    /**
     * Get the digest from the response header if available.
     * @param headers The headers to parse.
     * @return The digest value.
     */
    public static <T> String getDigestFromHeader(HttpHeaders headers) {
        return headers.getValue(DOCKER_DIGEST_HEADER_NAME);
    }

    public static Context enableSync(Context tracingContext) {
        return tracingContext.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }
    public static Context getTracingContext(Context context) {
        return context.addData(AZ_TRACING_NAMESPACE_KEY, CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE);
    }

    public static String trimNextLink(String locationHeader) {
        // The location header returned in the nextLink for upload chunk operations starts with a '/'
        // which the service expects us to remove before calling it.
        if (locationHeader != null && locationHeader.startsWith("/")) {
            return locationHeader.substring(1);
        }

        return locationHeader;
    }
}
