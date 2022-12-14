// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.specialized;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImplBuilder;
import com.azure.containers.containerregistry.implementation.ContainerRegistriesImpl;
import com.azure.containers.containerregistry.implementation.ContainerRegistryBlobsImpl;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrErrorsException;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistriesCreateManifestHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistriesCreateManifestResponse;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsCompleteUploadHeaders;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsCompleteUploadResponse;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsStartUploadResponse;
import com.azure.containers.containerregistry.implementation.models.ContainerRegistryBlobsUploadChunkResponse;
import com.azure.containers.containerregistry.implementation.models.ManifestWrapper;
import com.azure.containers.containerregistry.models.DownloadBlobResult;
import com.azure.containers.containerregistry.models.DownloadManifestOptions;
import com.azure.containers.containerregistry.models.DownloadManifestResult;
import com.azure.containers.containerregistry.models.OciManifest;
import com.azure.containers.containerregistry.models.UploadBlobResult;
import com.azure.containers.containerregistry.models.UploadManifestOptions;
import com.azure.containers.containerregistry.models.UploadManifestResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.deleteResponseToSuccess;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.enableSync;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.getTracingContext;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.mapAcrErrorsException;
import static com.azure.containers.containerregistry.implementation.UtilsImpl.trimNextLink;

/**
 * This class provides a client that exposes operations to push and pull images into container registry.
 * It exposes methods that upload, download and delete artifacts from the registry i.e. images and manifests.
 *
 * <p>View {@link ContainerRegistryBlobClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ContainerRegistryBlobClientBuilder
 */
@ServiceClient(builder = ContainerRegistryBlobClientBuilder.class)
public class ContainerRegistryBlobClient {
    private final ClientLogger logger = new ClientLogger(ContainerRegistryBlobClient.class);
    private final ContainerRegistryBlobsImpl blobsImpl;
    private final ContainerRegistriesImpl registriesImpl;
    private final String endpoint;
    private final String repositoryName;

    ContainerRegistryBlobClient(String repositoryName, HttpPipeline httpPipeline, String endpoint, String version) {
        this.repositoryName = repositoryName;
        this.endpoint = endpoint;
        AzureContainerRegistryImpl registryImplClient = new AzureContainerRegistryImplBuilder()
            .url(endpoint)
            .pipeline(httpPipeline)
            .apiVersion(version)
            .buildClient();
        this.blobsImpl = registryImplClient.getContainerRegistryBlobs();
        this.registriesImpl = registryImplClient.getContainerRegistries();
    }

    /**
     * This method returns the registry's repository on which operations are being performed.
     *
     * @return The name of the repository
     */
    public String getRepositoryName() {
        return this.repositoryName;
    }

    /**
     * This method returns the complete registry endpoint.
     *
     * @return The registry endpoint including the authority.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Upload the Oci manifest to the repository.
     * The upload is done as a single operation.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param manifest The OciManifest that needs to be updated.
     * @return operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code manifest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadManifestResult uploadManifest(OciManifest manifest) {
        if (manifest == null) {
            throw logger.logExceptionAsError(new NullPointerException("'manifest' can't be null."));
        }
        return this.uploadManifest(new UploadManifestOptions(manifest));
    }

    /**
     * Uploads a manifest to the repository.
     * The client currently only supports uploading OciManifests to the repository.
     * And this operation makes the assumption that the data provided is a valid OCI manifest.
     * <p>
     * Also, the data is read into memory and then an upload operation is performed as a single operation.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options The options for the upload manifest operation.
     * @return The operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadManifestResult uploadManifest(UploadManifestOptions options) {
        return this.uploadManifestWithResponse(options, Context.NONE).getValue();
    }

    /**
     * Uploads a manifest to the repository.
     * The client currently only supports uploading OciManifests to the repository.
     * And this operation makes the assumption that the data provided is a valid OCI manifest.
     * <p>
     * Also, the data is read into memory and then an upload operation is performed as a single operation.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options The options for the upload manifest operation.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The rest response containing the operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UploadManifestResult> uploadManifestWithResponse(UploadManifestOptions options, Context context) {
        if (options == null) {
            throw logger.logExceptionAsError(new NullPointerException("'options' can't be null."));
        }

        BinaryData data = options.getManifest().toReplayableBinaryData();
        String tagOrDigest = options.getTag() != null ? options.getTag() : UtilsImpl.computeDigest(data.toByteBuffer());
        try {
            ContainerRegistriesCreateManifestResponse
                response = this.registriesImpl.createManifestWithResponse(
                repositoryName,
                tagOrDigest,
                data,
                data.getLength(),
                UtilsImpl.OCI_MANIFEST_MEDIA_TYPE,
                enableSync(getTracingContext(context)));

            return new ResponseBase<ContainerRegistriesCreateManifestHeaders, UploadManifestResult>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                new UploadManifestResult(response.getDeserializedHeaders().getDockerContentDigest()),
                response.getDeserializedHeaders());
        } catch (AcrErrorsException exception) {
            throw logger.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Uploads a blob to the repository.
     * The client currently uploads the entire blob\layer as a single unit.
     * <p>
     * Also, the blob is read into memory and then an upload operation is performed as a single operation.
     * We currently do not support breaking the layer into multiple chunks and uploading them one at a time
     *
     * @param data The blob\image content that needs to be uploaded.
     * @return The operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadBlobResult uploadBlob(BinaryData data) {
        return this.uploadBlobWithResponse(data, Context.NONE).getValue();
    }

    /**
     * Uploads a blob to the repository.
     * The client currently uploads the entire blob\layer as a single unit.
     * <p>
     * Also, the blob is read into memory and then an upload operation is performed as a single operation.
     * We currently do not support breaking the layer into multiple chunks and uploading them one at a time
     * The service does support this via range header.
     *
     * @param data The blob\image content that needs to be uploaded.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The rest response containing the operation result.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code data} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UploadBlobResult> uploadBlobWithResponse(BinaryData data, Context context) {
        if (data == null) {
            throw logger.logExceptionAsError(new NullPointerException("'data' can't be null."));
        }
        context = enableSync(getTracingContext(context));

        String digest = UtilsImpl.computeDigest(data.toByteBuffer());
        try {
            ContainerRegistryBlobsStartUploadResponse
                startUploadResponse = this.blobsImpl.startUploadWithResponse(repositoryName, context);

            ContainerRegistryBlobsUploadChunkResponse
                uploadChunkResponse = this.blobsImpl.uploadChunkWithResponse(
                trimNextLink(startUploadResponse.getDeserializedHeaders().getLocation()), data, data.getLength(),
                context);
            ContainerRegistryBlobsCompleteUploadResponse
                completeUploadResponse = this.blobsImpl.completeUploadWithResponse(digest,
                trimNextLink(uploadChunkResponse.getDeserializedHeaders().getLocation()), (BinaryData) null, 0L,
                context);

            return new ResponseBase<ContainerRegistryBlobsCompleteUploadHeaders, UploadBlobResult>(
                completeUploadResponse.getRequest(),
                completeUploadResponse.getStatusCode(),
                completeUploadResponse.getHeaders(),
                new UploadBlobResult(completeUploadResponse.getDeserializedHeaders().getDockerContentDigest()),
                completeUploadResponse.getDeserializedHeaders());
        } catch (AcrErrorsException exception) {
            throw logger.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }

    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options Options for the operation.
     * @return The manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DownloadManifestResult downloadManifest(DownloadManifestOptions options) {
        return this.downloadManifestWithResponse(options, Context.NONE).getValue();
    }

    /**
     * Download the manifest associated with the given tag or digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param options Options for the operation.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The response for the manifest associated with the given tag or digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code tagOrDigest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DownloadManifestResult> downloadManifestWithResponse(DownloadManifestOptions options, Context context) {
        if (options == null) {
            throw logger.logExceptionAsError(new NullPointerException("'options' can't be null."));
        }

        String tagOrDigest = options.getTag() != null ? options.getTag() : options.getDigest();
        Response<ManifestWrapper> response;
        try {
            response =
                this.registriesImpl.getManifestWithResponse(repositoryName, tagOrDigest,
                    UtilsImpl.OCI_MANIFEST_MEDIA_TYPE, enableSync(getTracingContext(context)));
        } catch (AcrErrorsException exception) {
            throw logger.logExceptionAsError(mapAcrErrorsException(exception));
        }
        String digest = UtilsImpl.getDigestFromHeader(response.getHeaders());
        ManifestWrapper wrapper = response.getValue();

        // The service wants us to validate the digest here since a lot of customers forget to do it before consuming
        // the contents returned by the service.
        if (Objects.equals(digest, tagOrDigest) || Objects.equals(response.getValue().getTag(), tagOrDigest)) {
            OciManifest ociManifest = new OciManifest()
                .setAnnotations(wrapper.getAnnotations())
                .setConfig(wrapper.getConfig())
                .setLayers(wrapper.getLayers())
                .setSchemaVersion(wrapper.getSchemaVersion());

            return new SimpleResponse<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                new DownloadManifestResult(digest, ociManifest, BinaryData.fromObject(ociManifest)));
        } else {
            throw logger.logExceptionAsError(
                new ServiceResponseException("The digest in the response does not match the expected digest."));
        }
    }

    /**
     * Download the blob associated with the given digest.
     *
     * @param digest The digest for the given image layer.
     * @return The image associated with the given digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DownloadBlobResult downloadBlob(String digest) {
        return this.downloadBlobWithResponse(digest, Context.NONE).getValue();
    }

    /**
     * Download the blob\layer associated with the given digest.
     *
     * @param digest The digest for the given image layer.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The image associated with the given digest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DownloadBlobResult> downloadBlobWithResponse(String digest, Context context) {
        if (digest == null) {
            throw logger.logExceptionAsError(new NullPointerException("'digest' can't be null."));
        }
        Response<BinaryData> streamResponse;
        try {
            streamResponse = this.blobsImpl.getBlobWithResponse(repositoryName, digest, context);
        } catch (HttpResponseException exception) {
            throw logger.logExceptionAsError(new HttpResponseException(exception.getMessage(), exception.getResponse(),
                exception));
        }
        String resDigest = UtilsImpl.getDigestFromHeader(streamResponse.getHeaders());

        BinaryData binaryData = streamResponse.getValue();

        // The service wants us to validate the digest here since a lot of customers forget to do it before consuming
        // the contents returned by the service.
        if (Objects.equals(resDigest, digest)) {

            return new SimpleResponse<>(
                streamResponse.getRequest(),
                streamResponse.getStatusCode(),
                streamResponse.getHeaders(),
                new DownloadBlobResult(resDigest, binaryData));
        } else {
            throw logger.logExceptionAsError(new ServiceResponseException("The digest in the response does not match the expected digest."));
        }
    }

    /**
     * Delete the image associated with the given digest
     *
     * @param digest The digest for the given image layer.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteBlob(String digest) {
        this.deleteBlobWithResponse(digest, Context.NONE).getValue();
    }

    /**
     * Delete the image associated with the given digest
     *
     * @param digest The digest for the given image layer.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The REST response for the completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteBlobWithResponse(String digest, Context context) {
        try {
            if (digest == null) {
                throw logger.logExceptionAsError(new NullPointerException("'digest' can't be null."));
            }

            Response<BinaryData> streamResponse =
                this.blobsImpl.deleteBlobWithResponse(repositoryName, digest, enableSync(getTracingContext(context)));
            return deleteResponseToSuccess(streamResponse);
        } catch (HttpResponseException ex) {
            if (ex.getResponse().getStatusCode() == 404) {
                HttpResponse response = ex.getResponse();
                // In case of 404, we still convert it to success i.e. no-op.
                return new SimpleResponse<Void>(response.getRequest(), 202,
                    response.getHeaders(), null);
            } else {
                throw logger.logExceptionAsError(ex);
            }
        }
    }

    /**
     * Delete the manifest associated with the given digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param digest The digest of the manifest.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteManifest(String digest) {
        this.deleteManifestWithResponse(digest, Context.NONE).getValue();
    }

    /**
     * Delete the manifest associated with the given digest.
     * We currently only support downloading OCI manifests.
     *
     * @see <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">Oci Manifest Specification</a>
     *
     * @param digest The digest of the manifest.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The REST response for completion.
     * @throws ClientAuthenticationException thrown if the client's credentials do not have access to modify the namespace.
     * @throws NullPointerException thrown if the {@code digest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteManifestWithResponse(String digest, Context context) {
        try {
            Response<Void> response = this.registriesImpl.deleteManifestWithResponse(repositoryName, digest,
                enableSync(getTracingContext(context)));

            return UtilsImpl.deleteResponseToSuccess(response);
        } catch (AcrErrorsException exception) {
            throw logger.logExceptionAsError(mapAcrErrorsException(exception));
        }
    }
}
