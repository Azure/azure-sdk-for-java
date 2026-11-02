// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.containers.containerregistry.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryCredentialsPolicy;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenService;
import com.azure.containers.containerregistry.implementation.models.AcrErrorInfo;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ArtifactManifestPropertiesInternal;
import com.azure.containers.containerregistry.implementation.models.ArtifactTagPropertiesInternal;
import com.azure.containers.containerregistry.implementation.models.ManifestAttributesBase;
import com.azure.containers.containerregistry.implementation.models.TagAttributesBase;
import com.azure.containers.containerregistry.models.ArtifactManifestProperties;
import com.azure.containers.containerregistry.models.ArtifactTagProperties;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.containers.containerregistry.models.GetManifestResult;
import com.azure.containers.containerregistry.models.ManifestMediaType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
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
import com.azure.core.http.policy.RedirectPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.CoreUtils.bytesToHexString;
import static com.azure.core.util.CoreUtils.extractSizeFromContentRange;

/**
 * This is the utility class that includes helper methods used across our clients.
 */
public final class UtilsImpl {
    private static final ClientLogger LOGGER = new ClientLogger(UtilsImpl.class);
    private static final Map<String, String> PROPERTIES
        = CoreUtils.getProperties("azure-containers-containerregistry.properties");
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault("name", "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault("version", "UnknownVersion");
    private static final ContainerRegistryAudience ACR_ACCESS_TOKEN_AUDIENCE
        = ContainerRegistryAudience.fromString("https://containerregistry.azure.net");
    private static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
    private static final int HTTP_STATUS_CODE_ACCEPTED = 202;

    public static final HttpHeaderName DOCKER_DIGEST_HEADER_NAME = HttpHeaderName.fromString("docker-content-digest");

    public static final String SUPPORTED_MANIFEST_TYPES
        = "*/*" + "," + ManifestMediaType.OCI_IMAGE_MANIFEST + "," + ManifestMediaType.DOCKER_MANIFEST
            + ",application/vnd.oci.image.index.v1+json" + ",application/vnd.docker.distribution.manifest.list.v2+json"
            + ",application/vnd.cncf.oras.artifact.manifest.v1+json";

    private static final String CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE = "Microsoft.ContainerRegistry";
    public static final int CHUNK_SIZE = 4 * 1024 * 1024;
    public static final int MAX_MANIFEST_SIZE = 4 * 1024 * 1024;
    public static final String UPLOAD_BLOB_SPAN_NAME = "ContainerRegistryContentAsyncClient.uploadBlob";
    public static final String DOWNLOAD_BLOB_SPAN_NAME = "ContainerRegistryContentAsyncClient.downloadBlob";

    private UtilsImpl() {
    }

    /**
     * This method builds the httpPipeline for the builders.
     * @param clientOptions The client options
     * @param logOptions http log options.
     * @param configuration configuration settings.
     * @param retryPolicy retry policy
     * @param retryOptions retry options
     * @param credential credentials.
     * @param audience the audience.
     * @param perCallPolicies per call policies.
     * @param perRetryPolicies per retry policies.
     * @param httpClient http client
     * @param endpoint endpoint to be called
     * @param serviceVersion the service api version being targeted by the client.
     * @return returns the httpPipeline to be consumed by the builders.
     */
    public static HttpPipeline buildClientPipeline(ClientOptions clientOptions, HttpLogOptions logOptions,
        Configuration configuration, RetryPolicy retryPolicy, RetryOptions retryOptions, TokenCredential credential,
        ContainerRegistryAudience audience, List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient, String endpoint,
        ContainerRegistryServiceVersion serviceVersion, Tracer tracer) {

        if (credential == null) {
            LOGGER.verbose("Credentials are null, enabling anonymous access");
        }
        if (audience == null) {
            LOGGER.info("Audience is not specified, defaulting to ACR access token scope.");
            audience = ACR_ACCESS_TOKEN_AUDIENCE;
        }

        if (serviceVersion == null) {
            serviceVersion = ContainerRegistryServiceVersion.getLatest();
        }

        HttpPipeline credentialsPipeline = buildPipeline(clientOptions, logOptions, configuration, retryPolicy,
            retryOptions, null, null, perCallPolicies, perRetryPolicies, httpClient, tracer);

        return buildPipeline(clientOptions, logOptions, configuration, retryPolicy, retryOptions,
            buildCredentialsPolicy(credentialsPipeline, credential, audience, endpoint, serviceVersion),
            new RedirectPolicy(), perCallPolicies, perRetryPolicies, httpClient, tracer);
    }

    private static ContainerRegistryCredentialsPolicy buildCredentialsPolicy(HttpPipeline credentialPipeline,
        TokenCredential credential, ContainerRegistryAudience audience, String endpoint,
        ContainerRegistryServiceVersion serviceVersion) {
        AzureContainerRegistryImpl acrClient
            = new AzureContainerRegistryImpl(credentialPipeline, endpoint, serviceVersion.getVersion());

        ContainerRegistryTokenService tokenService = new ContainerRegistryTokenService(credential, audience, acrClient);
        return new ContainerRegistryCredentialsPolicy(tokenService, audience + "/.default");
    }

    private static HttpPipeline buildPipeline(ClientOptions clientOptions, HttpLogOptions logOptions,
        Configuration configuration, RetryPolicy retryPolicy, RetryOptions retryOptions,
        ContainerRegistryCredentialsPolicy credentialPolicy, RedirectPolicy redirectPolicy,
        List<HttpPipelinePolicy> perCallPolicies, List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient,
        Tracer tracer) {

        ArrayList<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, logOptions), CLIENT_NAME,
            CLIENT_VERSION, configuration));

        policies.add(new RequestIdPolicy());
        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));
        policies.add(new CookiePolicy());
        policies.add(new AddDatePolicy());
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        if (credentialPolicy != null) {
            policies.add(credentialPolicy);
        }
        if (redirectPolicy != null) {
            policies.add(redirectPolicy);
        }

        policies.addAll(perRetryPolicies);
        policies.add(new HttpLoggingPolicy(logOptions));

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(clientOptions)
            .tracer(tracer)
            .build();
    }

    public static Tracer createTracer(ClientOptions clientOptions) {
        TracingOptions tracingOptions = clientOptions == null ? null : clientOptions.getTracingOptions();
        return TracerProvider.getDefaultProvider()
            .createTracer(CLIENT_NAME, CLIENT_VERSION, CONTAINER_REGISTRY_TRACING_NAMESPACE_VALUE, tracingOptions);
    }

    /**
     * This method computes the digest for the buffer content.
     * Docker digest is a SHA256 hash of the docker image content and is deterministic based on the image build.
     * @param buffer The buffer containing the image bytes.
     * @return SHA-256 digest for the given buffer.
     */
    public static String computeDigest(ByteBuffer buffer) {
        MessageDigest md = createSha256();
        md.update(buffer.asReadOnlyBuffer());
        return "sha256:" + bytesToHexString(md.digest());
    }

    public static MessageDigest createSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    public static void validateDigest(MessageDigest messageDigest, String requestedDigest) {
        String sha256 = bytesToHexString(messageDigest.digest());
        if (isDigest(requestedDigest) && !requestedDigest.endsWith(sha256)) {
            throw LOGGER.atError()
                .addKeyValue("requestedDigest", requestedDigest)
                .addKeyValue("actualDigest", () -> "sha256:" + sha256)
                .log(new ServiceResponseException("The digest in the response does not match the expected digest."));
        }
    }

    public static Response<GetManifestResult> toGetManifestResponse(String tagOrDigest,
        Response<BinaryData> rawResponse) {
        checkManifestSize(rawResponse.getHeaders());
        String digest = rawResponse.getHeaders().getValue(DOCKER_DIGEST_HEADER_NAME);
        String responseSha256 = computeDigest(rawResponse.getValue().toByteBuffer());

        if (!Objects.equals(responseSha256, digest)
            || (isDigest(tagOrDigest) && !Objects.equals(responseSha256, tagOrDigest))) {
            throw LOGGER.logExceptionAsError(
                new ServiceResponseException("The digest in the response does not match the expected digest."));
        }

        String contentType = rawResponse.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
        ManifestMediaType responseMediaType = contentType != null ? ManifestMediaType.fromString(contentType) : null;

        return new SimpleResponse<>(rawResponse.getRequest(), rawResponse.getStatusCode(), rawResponse.getHeaders(),
            ConstructorAccessors.createGetManifestResult(digest, responseMediaType, rawResponse.getValue()));
    }

    private static long checkManifestSize(HttpHeaders headers) {
        // part of the service threat model - if manifest does not have proper content length or manifest is too big
        // it indicates a malicious or faulty service and should not be trusted.
        String contentLengthString = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (CoreUtils.isNullOrEmpty(contentLengthString)) {
            throw LOGGER
                .logExceptionAsError(new ServiceResponseException("Response does not include `Content-Length` header"));
        }

        try {
            long contentLength = Long.parseLong(contentLengthString);
            if (contentLength > MAX_MANIFEST_SIZE) {
                throw LOGGER.atError()
                    .addKeyValue("contentLength", contentLengthString)
                    .log(new ServiceResponseException("Manifest size is bigger than 4MB"));
            }

            return contentLength;
        } catch (NumberFormatException | NullPointerException e) {
            throw LOGGER.atError()
                .addKeyValue("contentLength", contentLengthString)
                .log(new ServiceResponseException("Could not parse `Content-Length` header"));
        }
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
        return new SimpleResponse<>(responseT.getRequest(), statusCode, responseT.getHeaders(), null);
    }

    /**
     * This method converts AcrErrors inside AcrErrorsException into {@link HttpResponseException}
     * with {@link ResponseError}
     */
    public static HttpResponseException mapAcrErrorsException(AcrErrorsException acrException) {
        final HttpResponse errorHttpResponse = acrException.getResponse();

        if (acrException.getValue() != null && !CoreUtils.isNullOrEmpty(acrException.getValue().getErrors())) {
            AcrErrorInfo first = acrException.getValue().getErrors().get(0);
            ResponseError error = new ResponseError(first.getCode(), first.getMessage());

            switch (errorHttpResponse.getStatusCode()) {
                case 401:
                    throw new ClientAuthenticationException(acrException.getMessage(), acrException.getResponse(),
                        error);

                case 404:
                    return new ResourceNotFoundException(acrException.getMessage(), acrException.getResponse(), error);

                case 409:
                    return new ResourceExistsException(acrException.getMessage(), acrException.getResponse(), error);

                case 412:
                    return new ResourceModifiedException(acrException.getMessage(), acrException.getResponse(), error);

                default:
                    return new HttpResponseException(acrException.getMessage(), acrException.getResponse(), error);
            }
        }

        return acrException;
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

    public static <T, R> PagedResponse<T> getPagedResponseWithContinuationToken(PagedResponse<R> listResponse,
        Function<List<R>, List<T>> mapperFunction) {
        Objects.requireNonNull(mapperFunction);

        String continuationLink = getContinuationLink(listResponse.getHeaders());
        List<T> values = mapperFunction.apply(listResponse.getValue());

        return new PagedResponseBase<String, T>(listResponse.getRequest(), listResponse.getStatusCode(),
            listResponse.getHeaders(), values, continuationLink, null);
    }

    private static String getContinuationLink(HttpHeaders headers) {
        String continuationLinkHeader = headers.getValue(HttpHeaderName.LINK);
        if (!CoreUtils.isNullOrEmpty(continuationLinkHeader) && continuationLinkHeader.charAt(0) == '<') {
            int endIndex = continuationLinkHeader.indexOf(">;");
            if (endIndex < 2) {
                LOGGER.warning("unexpected 'Link' header value - '{}'", continuationLinkHeader);
            }
            return continuationLinkHeader.substring(1, endIndex);
        }

        return null;
    }

    public static List<ArtifactManifestProperties> mapManifestsProperties(List<ManifestAttributesBase> baseArtifacts,
        String repositoryName, String registryLoginServer) {
        if (baseArtifacts == null) {
            return null;
        }

        List<ArtifactManifestProperties> artifactManifestProperties = new ArrayList<>(baseArtifacts.size());
        for (ManifestAttributesBase base : baseArtifacts) {
            ArtifactManifestPropertiesInternal internal
                = new ArtifactManifestPropertiesInternal().setRegistryLoginServer(registryLoginServer)
                    .setRepositoryName(repositoryName)
                    .setDigest(base.getDigest())
                    .setSizeInBytes(base.getSizeInBytes())
                    .setCreatedOn(base.getCreatedOn())
                    .setLastUpdatedOn(base.getLastUpdatedOn())
                    .setArchitecture(base.getArchitecture())
                    .setOperatingSystem(base.getOperatingSystem())
                    .setRelatedArtifacts(base.getRelatedArtifacts())
                    .setTags(base.getTags())
                    .setDeleteEnabled(base.isDeleteEnabled())
                    .setWriteEnabled(base.isWriteEnabled())
                    .setListEnabled(base.isListEnabled())
                    .setReadEnabled(base.isReadEnabled());

            artifactManifestProperties.add(ArtifactManifestPropertiesHelper.create(internal));
        }

        return artifactManifestProperties;
    }

    public static List<ArtifactTagProperties> getTagProperties(List<TagAttributesBase> baseValues,
        String repositoryName) {
        Objects.requireNonNull(baseValues);

        List<ArtifactTagProperties> artifactTagProperties = new ArrayList<>(baseValues.size());
        for (TagAttributesBase base : baseValues) {
            ArtifactTagPropertiesInternal internal
                = new ArtifactTagPropertiesInternal().setRepositoryName(repositoryName)
                    .setName(base.getName())
                    .setDigest(base.getDigest())
                    .setCreatedOn(base.getCreatedOn())
                    .setLastUpdatedOn(base.getLastUpdatedOn())
                    .setDeleteEnabled(base.isDeleteEnabled())
                    .setWriteEnabled(base.isWriteEnabled())
                    .setListEnabled(base.isListEnabled())
                    .setReadEnabled(base.isReadEnabled());

            artifactTagProperties.add(ArtifactTagPropertiesHelper.create(internal));
        }

        return artifactTagProperties;
    }

    public static void validateResponseHeaderDigest(String requestedDigest, HttpHeaders headers) {
        String responseHeaderDigest = headers.getValue(DOCKER_DIGEST_HEADER_NAME);
        if (!requestedDigest.equals(responseHeaderDigest)) {
            throw LOGGER.atError()
                .addKeyValue("requestedDigest", requestedDigest)
                .addKeyValue("responseDigest", responseHeaderDigest)
                .log(new ServiceResponseException(
                    "The digest in the response header does not match the expected digest."));
        }
    }

    public static <H, T> String getLocation(ResponseBase<H, T> response) {
        String locationHeader = response.getHeaders().getValue(HttpHeaderName.LOCATION);
        // The location header returned in the nextLink for upload chunk operations starts with a '/'
        // which the service expects us to remove before calling it.
        if (locationHeader != null && locationHeader.startsWith("/")) {
            return locationHeader.substring(1);
        }

        return locationHeader;
    }

    public static long getBlobSize(HttpHeaders headers) {
        HttpHeader contentRangeHeader = headers.get(HttpHeaderName.CONTENT_RANGE);
        if (contentRangeHeader != null) {
            long size = extractSizeFromContentRange(contentRangeHeader.getValue());
            if (size > 0) {
                return size;
            }
        }

        throw LOGGER.atError()
            .addKeyValue("contentRange", contentRangeHeader)
            .log(new ServiceResponseException("Missing or invalid content-range header in response"));
    }

    public static long getContentLength(HttpHeader contentLengthHeader) {
        if (contentLengthHeader != null && contentLengthHeader.getValue() != null) {
            return Long.parseLong(contentLengthHeader.getValue());
        }

        throw LOGGER
            .logExceptionAsError(new ServiceResponseException("Content-Length header in missing in the response"));
    }

    /**
     * Checks if string represents tag or digest.
     *
     * @param tagOrDigest string to check
     * @return true if digest, false otherwise.
     */
    public static boolean isDigest(String tagOrDigest) {
        return tagOrDigest.length() == 71 && tagOrDigest.startsWith("sha256:");
    }

    public static String formatFullyQualifiedReference(String endpoint, String repositoryName, String tagOrDigest) {
        try {
            URL endpointUrl = new URL(endpoint);
            return endpointUrl.getHost() + "/" + repositoryName + (isDigest(tagOrDigest) ? "@" : ":") + tagOrDigest;
        } catch (MalformedURLException ex) {
            // This will not happen.
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }
    }
}
