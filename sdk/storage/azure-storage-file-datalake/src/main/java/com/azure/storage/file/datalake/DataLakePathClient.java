// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.CpkInfo;
import com.azure.storage.file.datalake.implementation.models.PathSetAccessControlRecursiveMode;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import com.azure.storage.file.datalake.models.DataLakeAclChangeFailedException;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.DataLakeAccessOptions;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.PathRemoveAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathUpdateAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class)
public class DataLakePathClient {
    private static final ClientLogger LOGGER = new ClientLogger(DataLakePathClient.class);

    final DataLakePathAsyncClient dataLakePathAsyncClient;
    final BlockBlobClient blockBlobClient;

    DataLakePathClient(DataLakePathAsyncClient dataLakePathAsyncClient, BlockBlobClient blockBlobClient) {
        this.dataLakePathAsyncClient = dataLakePathAsyncClient;
        this.blockBlobClient = blockBlobClient;
    }

    /**
     * Gets the URL of the storage account.
     *
     * @return the URL.
     */
    String getAccountUrl() {
        return dataLakePathAsyncClient.getAccountUrl();
    }

    /**
     * Gets the URL of the object represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    String getPathUrl() {
        return dataLakePathAsyncClient.getPathUrl();
    }

    /**
     * Gets the associated account name.
     *
     * @return Account name associated with this storage resource.
     */
    public String getAccountName() {
        return dataLakePathAsyncClient.getAccountName();
    }

    /**
     * Gets the name of the File System in which this object lives.
     *
     * @return The name of the File System.
     */
    public String getFileSystemName() {
        return dataLakePathAsyncClient.getFileSystemName();
    }

    /**
     * Gets the path of this object, not including the name of the resource itself.
     *
     * @return The path of the object.
     */
    String getObjectPath() {
        return dataLakePathAsyncClient.getObjectPath();
    }

    /**
     * Gets the name of this object, not including its full path.
     *
     * @return The name of the object.
     */
    String getObjectName() {
        return dataLakePathAsyncClient.getObjectName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakePathAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return dataLakePathAsyncClient.getServiceVersion();
    }

    /**
     * Gets the {@link CpkInfo} used to encrypt this path's content on the server.
     *
     * @return the customer provided key used for encryption.
     */
    public CustomerProvidedKey getCustomerProvidedKey() {
        return this.dataLakePathAsyncClient.getCustomerProvidedKey();
    }

    /**
     * Creates a new {@link DataLakePathClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the path,
     * pass {@code null} to use no customer provided key.
     * @return a {@link DataLakePathClient} with the specified {@code customerProvidedKey}.
     */
    public DataLakePathClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        return new DataLakePathClient(dataLakePathAsyncClient.getCustomerProvidedKeyAsyncClient(customerProvidedKey),
            blockBlobClient.getCustomerProvidedKeyClient(Transforms.toBlobCustomerProvidedKey(customerProvidedKey)));
    }

    /**
     * Creates a resource. By default, this method will not overwrite an existing path.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.create -->
     * <pre>
     * System.out.printf&#40;&quot;Last Modified Time:%s&quot;, client.create&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.create -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return Information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo create() {
        return create(false);
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.create#boolean -->
     * <pre>
     * boolean overwrite = true;
     * System.out.printf&#40;&quot;Last Modified Time:%s&quot;, client.create&#40;true&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.create#boolean -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param overwrite Whether to overwrite, should data exist on the path.
     *
     * @return Information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo create(boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return createWithResponse(new DataLakePathCreateOptions().setRequestConditions(requestConditions), null, Context.NONE).getValue();
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     *
     * Response&lt;PathInfo&gt; response = client.createWithResponse&#40;permissions, umask, httpHeaders,
     *     Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Last Modified Time:%s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param permissions POSIX access permissions for the resource owner, the resource owning group, and others.
     * @param umask Restricts permissions of the resource to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing information about the created resource
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathInfo> createWithResponse(String permissions, String umask, PathHttpHeaders headers,
        Map<String, String> metadata, DataLakeRequestConditions requestConditions, Duration timeout,
        Context context) {
        DataLakeAccessOptions accessOptions = new DataLakeAccessOptions()
            .setPermissions(permissions)
            .setUmask(umask);
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setAccessOptions(accessOptions)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.createWithResponse(options, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.createWithResponse#DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * String owner = &quot;rwx&quot;;
     * String group = &quot;r--&quot;;
     * String leaseId = UUID.randomUUID&#40;&#41;.toString&#40;&#41;;
     * int duration = 15;
     * DataLakeAccessOptions accessOptions = new DataLakeAccessOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setOwner&#40;owner&#41;
     *     .setGroup&#40;group&#41;;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setAccessOptions&#40;accessOptions&#41;
     *     .setPathHttpHeaders&#40;httpHeaders&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setMetadata&#40;metadata&#41;
     *     .setProposedLeaseId&#40;leaseId&#41;
     *     .setLeaseDuration&#40;duration&#41;;
     *
     * Response&lt;PathInfo&gt; response = client.createWithResponse&#40;options, timeout, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Last Modified Time:%s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.createWithResponse#DataLakePathCreateOptions-Duration-Context -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing information about the created resource
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathInfo> createWithResponse(DataLakePathCreateOptions options, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.createWithResponse(options, context);
        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }


    /**
     * Creates a resource if a path does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.createIfNotExists -->
     * <pre>
     * PathInfo pathInfo = client.createIfNotExists&#40;&#41;;
     * System.out.printf&#40;&quot;Last Modified Time:%s&quot;, pathInfo.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.createIfNotExists -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return {@link PathInfo} that contains information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo createIfNotExists() {
        return createIfNotExistsWithResponse(new DataLakePathCreateOptions(), null, null).getValue();
    }

    /**
     * Creates a resource if a path does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.createIfNotExistsWithResponse#DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakeAccessOptions accessOptions = new DataLakeAccessOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setAccessOptions&#40;accessOptions&#41;
     *     .setPathHttpHeaders&#40;headers&#41;
     *     .setMetadata&#40;metadata&#41;;
     *
     * Response&lt;PathInfo&gt; response = client.createIfNotExistsWithResponse&#40;options, timeout, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.createIfNotExistsWithResponse#DataLakePathCreateOptions-Duration-Context -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathCreateOptions}
     * metadata key or value, it must be removed or encoded.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A reactive {@link Response} signaling completion, whose {@link Response#getValue() value} contains a
     * {@link PathInfo} containing information about the resource. If {@link Response}'s status code is 201, a new
     * resource was successfully created. If status code is 409, a resource already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathInfo> createIfNotExistsWithResponse(DataLakePathCreateOptions options, Duration timeout,
        Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(dataLakePathAsyncClient
            .createIfNotExistsWithResponse(options, context), timeout);
    }

    /**
     * Deletes paths under the resource if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.deleteIfExists -->
     * <pre>
     * client.create&#40;&#41;;
     * boolean result = client.deleteIfExists&#40;&#41;;
     * System.out.println&#40;&quot;Delete complete: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.deleteIfExists -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     * @return {@code true} if the resource is successfully deleted, {@code false} if resource does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteIfExists() {
        return deleteIfExistsWithResponse(new DataLakePathDeleteOptions(), null, null).getValue();
    }

    /**
     * Deletes all paths under the specified resource if exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;false&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteIfExistsWithResponse&#40;options, timeout, new Context&#40;key1, value1&#41;&#41;;
     *
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathDeleteOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 200, the resource
     * was successfully deleted. If status code is 404, the resource does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteIfExistsWithResponse(DataLakePathDeleteOptions options, Duration timeout,
        Context context) {
        return StorageImplUtils.blockWithOptionalTimeout(dataLakePathAsyncClient
            .deleteIfExistsWithResponse(options, context), timeout);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map -->
     * <pre>
     * client.setMetadata&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     * System.out.println&#40;&quot;Set metadata completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * client.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions, timeout,
     *     new Context&#40;key2, value2&#41;&#41;;
     * System.out.println&#40;&quot;Set metadata completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map-DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blockBlobClient.setMetadataWithResponse(metadata, Transforms.toBlobRequestConditions(requestConditions),
                timeout, context), LOGGER);
    }

    /**
     * Changes a resource's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setHttpHeaders#PathHttpHeaders -->
     * <pre>
     * client.setHttpHeaders&#40;new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setHttpHeaders#PathHttpHeaders -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setHttpHeaders(PathHttpHeaders headers) {
        setHttpHeadersWithResponse(headers, null, null, Context.NONE);
    }

    /**
     * Changes a resource's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * Response&lt;Void&gt; response = client.setHttpHeadersWithResponse&#40;new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;, requestConditions, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Set HTTP headers completed with status %d%n&quot;,
     *             response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setHttpHeadersWithResponse(PathHttpHeaders headers,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blockBlobClient.setHttpHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
                Transforms.toBlobRequestConditions(requestConditions), timeout, context), LOGGER);
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setAccessControlList#List-String-String -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * System.out.printf&#40;&quot;Last Modified Time: %s&quot;, client.setAccessControlList&#40;pathAccessControlEntries, group, owner&#41;
     *     .getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setAccessControlList#List-String-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList A list of {@link PathAccessControlEntry} objects.
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @return The resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo setAccessControlList(List<PathAccessControlEntry> accessControlList, String group, String owner) {
        return setAccessControlListWithResponse(accessControlList, group, owner, null, null, Context.NONE).getValue();
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * Response&lt;PathInfo&gt; response = client.setAccessControlListWithResponse&#40;pathAccessControlEntries, group, owner,
     *     requestConditions, timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Last Modified Time: %s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList A list of {@link PathAccessControlEntry} objects.
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathInfo> setAccessControlListWithResponse(List<PathAccessControlEntry> accessControlList,
        String group, String owner, DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.setAccessControlWithResponse(accessControlList,
            null, group, owner, requestConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setPermissions#PathPermissions-String-String -->
     * <pre>
     * PathPermissions permissions = new PathPermissions&#40;&#41;
     *     .setGroup&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;&#41;
     *     .setOwner&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;.setWritePermission&#40;true&#41;&#41;
     *     .setOther&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * System.out.printf&#40;&quot;Last Modified Time: %s&quot;, client.setPermissions&#40;permissions, group, owner&#41;
     *     .getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setPermissions#PathPermissions-String-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param permissions {@link PathPermissions}
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @return The resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathInfo setPermissions(PathPermissions permissions, String group, String owner) {
        return setPermissionsWithResponse(permissions, group, owner, null, null, Context.NONE).getValue();
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * PathPermissions permissions = new PathPermissions&#40;&#41;
     *     .setGroup&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;&#41;
     *     .setOwner&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;.setWritePermission&#40;true&#41;&#41;
     *     .setOther&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * Response&lt;PathInfo&gt; response = client.setPermissionsWithResponse&#40;permissions, group, owner, requestConditions,
     *     timeout, new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Last Modified Time: %s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param permissions {@link PathPermissions}
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathInfo> setPermissionsWithResponse(PathPermissions permissions, String group, String owner,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.setAccessControlWithResponse(null, permissions,
            group, owner, requestConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Recursively sets the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursive#List -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * AccessControlChangeResult response = client.setAccessControlRecursive&#40;pathAccessControlEntries&#41;;
     *
     * System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *     response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursive#List -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList The POSIX access control list for the file or directory.
     * @return The result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessControlChangeResult setAccessControlRecursive(List<PathAccessControlEntry> accessControlList) {
        return setAccessControlRecursiveWithResponse(new PathSetAccessControlRecursiveOptions(accessControlList), null,
            Context.NONE).getValue();
    }

    /**
     * Recursively sets the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * Integer batchSize = 2;
     * Integer maxBatches = 10;
     * boolean continueOnFailure = false;
     * String continuationToken = null;
     * Consumer&lt;Response&lt;AccessControlChanges&gt;&gt; progressHandler =
     *     response -&gt; System.out.println&#40;&quot;Received response&quot;&#41;;
     *
     * PathSetAccessControlRecursiveOptions options =
     *     new PathSetAccessControlRecursiveOptions&#40;pathAccessControlEntries&#41;
     *         .setBatchSize&#40;batchSize&#41;
     *         .setMaxBatches&#40;maxBatches&#41;
     *         .setContinueOnFailure&#40;continueOnFailure&#41;
     *         .setContinuationToken&#40;continuationToken&#41;
     *         .setProgressHandler&#40;progressHandler&#41;;
     *
     * Response&lt;AccessControlChangeResult&gt; response = client.setAccessControlRecursiveWithResponse&#40;options, timeout,
     *     new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *     response.getValue&#40;&#41;.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param options {@link PathSetAccessControlRecursiveOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessControlChangeResult> setAccessControlRecursiveWithResponse(
        PathSetAccessControlRecursiveOptions options, Duration timeout, Context context) {
        Mono<Response<AccessControlChangeResult>> response =
            dataLakePathAsyncClient.setAccessControlRecursiveWithResponse(
                PathAccessControlEntry.serializeList(options.getAccessControlList()), options.getProgressHandler(),
                PathSetAccessControlRecursiveMode.SET, options.getBatchSize(), options.getMaxBatches(),
                options.isContinueOnFailure(), options.getContinuationToken(), context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Recursively updates the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursive#List -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * AccessControlChangeResult response = client.updateAccessControlRecursive&#40;pathAccessControlEntries&#41;;
     *
     * System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *     response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursive#List -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList The POSIX access control list for the file or directory.
     * @return The result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessControlChangeResult updateAccessControlRecursive(List<PathAccessControlEntry> accessControlList) {
        return updateAccessControlRecursiveWithResponse(new PathUpdateAccessControlRecursiveOptions(accessControlList),
            null, Context.NONE).getValue();
    }

    /**
     * Recursively updates the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * Integer batchSize = 2;
     * Integer maxBatches = 10;
     * boolean continueOnFailure = false;
     * String continuationToken = null;
     * Consumer&lt;Response&lt;AccessControlChanges&gt;&gt; progressHandler =
     *     response -&gt; System.out.println&#40;&quot;Received response&quot;&#41;;
     *
     * PathUpdateAccessControlRecursiveOptions options =
     *     new PathUpdateAccessControlRecursiveOptions&#40;pathAccessControlEntries&#41;
     *         .setBatchSize&#40;batchSize&#41;
     *         .setMaxBatches&#40;maxBatches&#41;
     *         .setContinueOnFailure&#40;continueOnFailure&#41;
     *         .setContinuationToken&#40;continuationToken&#41;
     *         .setProgressHandler&#40;progressHandler&#41;;
     *
     * Response&lt;AccessControlChangeResult&gt; response = client.updateAccessControlRecursiveWithResponse&#40;options, timeout,
     *     new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *     response.getValue&#40;&#41;.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param options {@link PathUpdateAccessControlRecursiveOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessControlChangeResult> updateAccessControlRecursiveWithResponse(
        PathUpdateAccessControlRecursiveOptions options, Duration timeout, Context context) {
        Mono<Response<AccessControlChangeResult>> response =
            dataLakePathAsyncClient.setAccessControlRecursiveWithResponse(
                PathAccessControlEntry.serializeList(options.getAccessControlList()), options.getProgressHandler(),
                PathSetAccessControlRecursiveMode.MODIFY, options.getBatchSize(), options.getMaxBatches(),
                options.isContinueOnFailure(), options.getContinuationToken(), context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Recursively removes the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursive#List -->
     * <pre>
     * PathRemoveAccessControlEntry pathAccessControlEntry = new PathRemoveAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;;
     * List&lt;PathRemoveAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * AccessControlChangeResult response = client.removeAccessControlRecursive&#40;pathAccessControlEntries&#41;;
     *
     * System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *     response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursive#List -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList The POSIX access control list for the file or directory.
     * @return The result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessControlChangeResult removeAccessControlRecursive(
        List<PathRemoveAccessControlEntry> accessControlList) {
        return removeAccessControlRecursiveWithResponse(new PathRemoveAccessControlRecursiveOptions(accessControlList),
            null, Context.NONE).getValue();
    }

    /**
     * Recursively removes the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * PathRemoveAccessControlEntry pathAccessControlEntry = new PathRemoveAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;;
     * List&lt;PathRemoveAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * Integer batchSize = 2;
     * Integer maxBatches = 10;
     * boolean continueOnFailure = false;
     * String continuationToken = null;
     * Consumer&lt;Response&lt;AccessControlChanges&gt;&gt; progressHandler =
     *     response -&gt; System.out.println&#40;&quot;Received response&quot;&#41;;
     *
     * PathRemoveAccessControlRecursiveOptions options =
     *     new PathRemoveAccessControlRecursiveOptions&#40;pathAccessControlEntries&#41;
     *         .setBatchSize&#40;batchSize&#41;
     *         .setMaxBatches&#40;maxBatches&#41;
     *         .setContinueOnFailure&#40;continueOnFailure&#41;
     *         .setContinuationToken&#40;continuationToken&#41;
     *         .setProgressHandler&#40;progressHandler&#41;;
     *
     * Response&lt;AccessControlChangeResult&gt; response = client.removeAccessControlRecursiveWithResponse&#40;options, timeout,
     *     new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *     response.getValue&#40;&#41;.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param options {@link PathRemoveAccessControlRecursiveOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A  response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessControlChangeResult> removeAccessControlRecursiveWithResponse(
        PathRemoveAccessControlRecursiveOptions options, Duration timeout, Context context) {
        Mono<Response<AccessControlChangeResult>> response =
            dataLakePathAsyncClient.setAccessControlRecursiveWithResponse(
                PathRemoveAccessControlEntry.serializeList(options.getAccessControlList()),
                options.getProgressHandler(), PathSetAccessControlRecursiveMode.REMOVE, options.getBatchSize(),
                options.getMaxBatches(), options.isContinueOnFailure(), options.getContinuationToken(), context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }


    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.getAccessControl -->
     * <pre>
     * PathAccessControl response = client.getAccessControl&#40;&#41;;
     * System.out.printf&#40;&quot;Access Control List: %s, Group: %s, Owner: %s, Permissions: %s&quot;,
     *     PathAccessControlEntry.serializeList&#40;response.getAccessControlList&#40;&#41;&#41;, response.getGroup&#40;&#41;,
     *     response.getOwner&#40;&#41;, response.getPermissions&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.getAccessControl -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @return The resource access control.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathAccessControl getAccessControl() {
        return getAccessControlWithResponse(false, null, null, Context.NONE).getValue();
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * boolean userPrincipalNameReturned = false;
     *
     * Response&lt;PathAccessControl&gt; response = client.getAccessControlWithResponse&#40;userPrincipalNameReturned,
     *     requestConditions, timeout, new Context&#40;key1, value1&#41;&#41;;
     *
     * PathAccessControl pac = response.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Access Control List: %s, Group: %s, Owner: %s, Permissions: %s&quot;,
     *     PathAccessControlEntry.serializeList&#40;pac.getAccessControlList&#40;&#41;&#41;, pac.getGroup&#40;&#41;, pac.getOwner&#40;&#41;,
     *     pac.getPermissions&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @param userPrincipalNameReturned When true, user identity values returned as User Principal Names. When false,
     * user identity values returned as Azure Active Directory Object IDs. Default value is false.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource access control.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathAccessControl> getAccessControlWithResponse(boolean userPrincipalNameReturned,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<PathAccessControl>> response = dataLakePathAsyncClient.getAccessControlWithResponse(
            userPrincipalNameReturned, requestConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.getProperties -->
     * <pre>
     * System.out.printf&#40;&quot;Creation Time: %s, Size: %d%n&quot;, client.getProperties&#40;&#41;.getCreationTime&#40;&#41;,
     *     client.getProperties&#40;&#41;.getFileSize&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return The resource properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PathProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * Response&lt;PathProperties&gt; response = client.getPropertiesWithResponse&#40;requestConditions, timeout,
     *     new Context&#40;key2, value2&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Creation Time: %s, Size: %d%n&quot;, response.getValue&#40;&#41;.getCreationTime&#40;&#41;,
     *     response.getValue&#40;&#41;.getFileSize&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PathProperties> getPropertiesWithResponse(DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(
                Transforms.toBlobRequestConditions(requestConditions), timeout, context);
            return new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue()));
        }, LOGGER);
    }

    /**
     * Gets if the path this client represents exists in the cloud.
     * <p>Note that this method does not guarantee that the path type (file/directory) matches expectations.</p>
     * <p>For example, a DataLakeFileClient representing a path to a datalake directory will return true, and vice
     * versa.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.exists -->
     * <pre>
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.exists&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.exists -->
     *
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Gets if the path this client represents exists in the cloud.
     * <p>Note that this method does not guarantee that the path type (file/directory) matches expectations.</p>
     * <p>For example, a DataLakeFileClient representing a path to a datalake directory will return true, and vice
     * versa.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.existsWithResponse#Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.existsWithResponse&#40;timeout, new Context&#40;key2, value2&#41;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.existsWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blockBlobClient.existsWithResponse(timeout, context), LOGGER);
    }

    BlockBlobClient getBlockBlobClient() {
        return blockBlobClient;
    }

    /**
     * Generates a user delegation SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission myPermission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return dataLakePathAsyncClient.generateUserDelegationSas(dataLakeServiceSasSignatureValues, userDelegationKey);
    }

    /**
     * Generates a user delegation SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission myPermission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey, accountName, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return dataLakePathAsyncClient.generateUserDelegationSas(dataLakeServiceSasSignatureValues, userDelegationKey,
            accountName, context);
    }

    /**
     * Generates a service SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission permission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return dataLakePathAsyncClient.generateSas(dataLakeServiceSasSignatureValues);
    }

    /**
     * Generates a service SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission permission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * client.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues, Context context) {
        return dataLakePathAsyncClient.generateSas(dataLakeServiceSasSignatureValues, context);
    }

}
