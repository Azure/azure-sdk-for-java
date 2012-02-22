/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.implementation;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.microsoft.windowsazure.services.blob.BlobConfiguration;
import com.microsoft.windowsazure.services.blob.BlobContract;
import com.microsoft.windowsazure.services.blob.models.AccessCondition;
import com.microsoft.windowsazure.services.blob.models.AcquireLeaseOptions;
import com.microsoft.windowsazure.services.blob.models.AcquireLeaseResult;
import com.microsoft.windowsazure.services.blob.models.BlobProperties;
import com.microsoft.windowsazure.services.blob.models.BlobServiceOptions;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ContainerACL;
import com.microsoft.windowsazure.services.blob.models.ContainerACL.PublicAccessType;
import com.microsoft.windowsazure.services.blob.models.CopyBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesResult;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotResult;
import com.microsoft.windowsazure.services.blob.models.CreateContainerOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteBlobOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteContainerOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerACLResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobsResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersOptions;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult;
import com.microsoft.windowsazure.services.blob.models.PageRange;
import com.microsoft.windowsazure.services.blob.models.ServiceProperties;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.SetContainerMetadataOptions;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.CommaStringBuilder;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.HttpURLConnectionClient;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.Base64;

public class BlobRestProxy implements BlobContract {
    private static final String API_VERSION = "2011-08-18";
    private final HttpURLConnectionClient channel;
    private final String accountName;
    private final String url;
    private final RFC1123DateConverter dateMapper;
    private final ServiceFilter[] filters;
    private final SharedKeyFilter filter;

    @Inject
    public BlobRestProxy(HttpURLConnectionClient channel, @Named(BlobConfiguration.ACCOUNT_NAME) String accountName,
            @Named(BlobConfiguration.URI) String url, SharedKeyFilter filter) {

        this.channel = channel;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = new RFC1123DateConverter();
        this.filters = new ServiceFilter[0];
        channel.addFilter(filter);
    }

    public BlobRestProxy(HttpURLConnectionClient client, ServiceFilter[] filters, String accountName, String url,
            SharedKeyFilter filter, RFC1123DateConverter dateMapper) {

        this.channel = client;
        this.filters = filters;
        this.accountName = accountName;
        this.url = url;
        this.filter = filter;
        this.dateMapper = dateMapper;
    }

    @Override
    public BlobContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new BlobRestProxy(this.channel, newFilters, this.accountName, this.url, this.filter, this.dateMapper);
    }

    private void ThrowIfError(ClientResponse r) {
        PipelineHelpers.ThrowIfError(r);
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, Object value) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value);
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, int value, int defaultValue) {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value, defaultValue);
    }

    private Builder addOptionalHeader(Builder builder, String name, Object value) {
        return PipelineHelpers.addOptionalHeader(builder, name, value);
    }

    private Builder addOptionalMetadataHeader(Builder builder, Map<String, String> metadata) {
        return PipelineHelpers.addOptionalMetadataHeader(builder, metadata);
    }

    private Builder addOptionalRangeHeader(Builder builder, Long rangeStart, Long rangeEnd) {
        return PipelineHelpers.addOptionalRangeHeader(builder, rangeStart, rangeEnd);
    }

    private Builder addOptionalAccessContitionHeader(Builder builder, AccessCondition accessCondition) {
        return PipelineHelpers.addOptionalAccessContitionHeader(builder, accessCondition);
    }

    private Builder addOptionalSourceAccessContitionHeader(Builder builder, AccessCondition accessCondition) {
        return PipelineHelpers.addOptionalSourceAccessContitionHeader(builder, accessCondition);
    }

    private HashMap<String, String> getMetadataFromHeaders(ClientResponse response) {
        return PipelineHelpers.getMetadataFromHeaders(response);
    }

    private WebResource addOptionalBlobListingIncludeQueryParam(ListBlobsOptions options, WebResource webResource) {
        CommaStringBuilder sb = new CommaStringBuilder();
        sb.addValue(options.isIncludeSnapshots(), "snapshots");
        sb.addValue(options.isIncludeUncommittedBlobs(), "uncommittedblobs");
        sb.addValue(options.isIncludeMetadata(), "metadata");
        webResource = addOptionalQueryParam(webResource, "include", sb.toString());
        return webResource;
    }

    private WebResource addOptionalContainerIncludeQueryParam(ListContainersOptions options, WebResource webResource) {
        CommaStringBuilder sb = new CommaStringBuilder();
        sb.addValue(options.isIncludeMetadata(), "metadata");
        webResource = addOptionalQueryParam(webResource, "include", sb.toString());
        return webResource;
    }

    private Builder addPutBlobHeaders(CreateBlobOptions options, Builder builder) {
        builder = addOptionalHeader(builder, "Content-Type", options.getContentType());
        if (options.getContentType() == null) {
            // Note: Add content type here to enable proper HMAC signing
            builder = builder.type("application/octet-stream");
        }
        builder = addOptionalHeader(builder, "Content-Encoding", options.getContentEncoding());
        builder = addOptionalHeader(builder, "Content-Language", options.getContentLanguage());
        builder = addOptionalHeader(builder, "Content-MD5", options.getContentMD5());
        builder = addOptionalHeader(builder, "Cache-Control", options.getCacheControl());
        builder = addOptionalHeader(builder, "x-ms-blob-content-type", options.getBlobContentType());
        builder = addOptionalHeader(builder, "x-ms-blob-content-encoding", options.getBlobContentEncoding());
        builder = addOptionalHeader(builder, "x-ms-blob-content-language", options.getBlobContentLanguage());
        builder = addOptionalHeader(builder, "x-ms-blob-content-md5", options.getBlobContentMD5());
        builder = addOptionalHeader(builder, "x-ms-blob-cache-control", options.getBlobCacheControl());
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        return builder;
    }

    private GetBlobPropertiesResult getBlobPropertiesResultFromResponse(ClientResponse response) {
        // Properties
        BlobProperties properties = new BlobProperties();
        properties.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));
        properties.setBlobType(response.getHeaders().getFirst("x-ms-blob-type"));
        properties.setLeaseStatus(response.getHeaders().getFirst("x-ms-lease-status"));

        properties.setContentLength(Long.parseLong(response.getHeaders().getFirst("Content-Length")));
        properties.setContentType(response.getHeaders().getFirst("Content-Type"));
        properties.setContentMD5(response.getHeaders().getFirst("Content-MD5"));
        properties.setContentEncoding(response.getHeaders().getFirst("Content-Encoding"));
        properties.setContentLanguage(response.getHeaders().getFirst("Content-Language"));
        properties.setCacheControl(response.getHeaders().getFirst("Cache-Control"));

        properties.setEtag(response.getHeaders().getFirst("Etag"));
        if (response.getHeaders().containsKey("x-ms-blob-sequence-number")) {
            properties.setSequenceNumber(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-sequence-number")));
        }

        // Metadata
        HashMap<String, String> metadata = getMetadataFromHeaders(response);

        // Result
        GetBlobPropertiesResult result = new GetBlobPropertiesResult();
        result.setMetadata(metadata);
        result.setProperties(properties);
        return result;
    }

    private WebResource getResource(BlobServiceOptions options) {
        WebResource webResource = channel.resource(url).path("/");
        webResource = addOptionalQueryParam(webResource, "timeout", options.getTimeout());
        for (ServiceFilter filter : filters) {
            webResource.addFilter(new ClientFilterAdapter(filter));
        }

        return webResource;
    }

    private String getCopyBlobSourceName(String sourceContainer, String sourceBlob, CopyBlobOptions options) {
        // Specifies the name of the source blob, in one of the following
        // formats:
        // Blob in named container: /accountName/containerName/blobName
        //
        // Snapshot in named container:
        // /accountName/containerName/blobName?snapshot=<DateTime>
        //
        // Blob in root container: /accountName/blobName
        //
        // Snapshot in root container: /accountName/blobName?snapshot=<DateTime>
        String sourceName = "/" + this.accountName;
        if (sourceContainer != null) {
            sourceName += "/" + sourceContainer;
        }
        sourceName += "/" + sourceBlob;
        if (options.getSourceSnapshot() != null) {
            sourceName += "?snapshot=" + options.getSourceSnapshot();
        }
        return sourceName;
    }

    @Override
    public GetServicePropertiesResult getServiceProperties() throws ServiceException {
        return getServiceProperties(new BlobServiceOptions());
    }

    @Override
    public GetServicePropertiesResult getServiceProperties(BlobServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        GetServicePropertiesResult result = new GetServicePropertiesResult();
        result.setValue(builder.get(ServiceProperties.class));
        return result;
    }

    @Override
    public void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException {
        setServiceProperties(serviceProperties, new BlobServiceOptions());
    }

    @Override
    public void setServiceProperties(ServiceProperties serviceProperties, BlobServiceOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("resType", "service")
                .queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder.put(serviceProperties);
    }

    @Override
    public void createContainer(String container) throws ServiceException {
        createContainer(container, new CreateContainerOptions());
    }

    @Override
    public void createContainer(String container, CreateContainerOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).queryParam("resType", "container");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalHeader(builder, "x-ms-blob-public-access", options.getPublicAccess());

        builder.put();
    }

    @Override
    public void deleteContainer(String container) throws ServiceException {
        deleteContainer(container, new DeleteContainerOptions());
    }

    @Override
    public void deleteContainer(String container, DeleteContainerOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).queryParam("resType", "container");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        builder.delete();
    }

    @Override
    public GetContainerPropertiesResult getContainerProperties(String container) throws ServiceException {
        return getContainerProperties(container, new BlobServiceOptions());
    }

    @Override
    public GetContainerPropertiesResult getContainerProperties(String container, BlobServiceOptions options)
            throws ServiceException {
        return getContainerPropertiesImpl(container, options, null);
    }

    @Override
    public GetContainerPropertiesResult getContainerMetadata(String container) throws ServiceException {
        return getContainerMetadata(container, new BlobServiceOptions());
    }

    @Override
    public GetContainerPropertiesResult getContainerMetadata(String container, BlobServiceOptions options)
            throws ServiceException {
        return getContainerPropertiesImpl(container, options, "metadata");
    }

    private GetContainerPropertiesResult getContainerPropertiesImpl(String container, BlobServiceOptions options,
            String operation) throws ServiceException {
        WebResource webResource = getResource(options).path(container).queryParam("resType", "container");
        webResource = addOptionalQueryParam(webResource, "comp", operation);

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        GetContainerPropertiesResult properties = new GetContainerPropertiesResult();
        properties.setEtag(response.getHeaders().getFirst("ETag"));
        properties.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));
        properties.setMetadata(getMetadataFromHeaders(response));

        return properties;
    }

    @Override
    public GetContainerACLResult getContainerACL(String container) throws ServiceException {
        return getContainerACL(container, new BlobServiceOptions());
    }

    @Override
    public GetContainerACLResult getContainerACL(String container, BlobServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).queryParam("resType", "container")
                .queryParam("comp", "acl");

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        ContainerACL.SignedIdentifiers si = response.getEntity(ContainerACL.SignedIdentifiers.class);
        ContainerACL acl = new ContainerACL();
        acl.setSignedIdentifiers(si.getSignedIdentifiers());
        if ("container".equals(response.getHeaders().getFirst("x-ms-blob-public-access"))) {
            acl.setPublicAccess(PublicAccessType.CONTAINER_AND_BLOBS);
        }
        else if ("blob".equals(response.getHeaders().getFirst("x-ms-blob-public-access"))) {
            acl.setPublicAccess(PublicAccessType.BLOBS_ONLY);
        }
        else {
            acl.setPublicAccess(PublicAccessType.NONE);
        }
        acl.setEtag(response.getHeaders().getFirst("ETag"));
        acl.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));

        GetContainerACLResult result = new GetContainerACLResult();
        result.setValue(acl);
        return result;
    }

    @Override
    public void setContainerACL(String container, ContainerACL acl) throws ServiceException {
        setContainerACL(container, acl, new BlobServiceOptions());
    }

    @Override
    public void setContainerACL(String container, ContainerACL acl, BlobServiceOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).queryParam("resType", "container")
                .queryParam("comp", "acl");

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        if (acl.getPublicAccess() == PublicAccessType.BLOBS_ONLY) {
            builder = addOptionalHeader(builder, "x-ms-blob-public-access", "blob");
        }
        else if (acl.getPublicAccess() == PublicAccessType.CONTAINER_AND_BLOBS) {
            builder = addOptionalHeader(builder, "x-ms-blob-public-access", "container");
        }

        ContainerACL.SignedIdentifiers si = new ContainerACL.SignedIdentifiers();
        si.setSignedIdentifiers(acl.getSignedIdentifiers());

        builder.put(si);
    }

    @Override
    public void setContainerMetadata(String container, HashMap<String, String> metadata) throws ServiceException {
        setContainerMetadata(container, metadata, new SetContainerMetadataOptions());
    }

    @Override
    public void setContainerMetadata(String container, HashMap<String, String> metadata,
            SetContainerMetadataOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).queryParam("resType", "container")
                .queryParam("comp", "metadata");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalMetadataHeader(builder, metadata);
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        builder.put();
    }

    @Override
    public ListContainersResult listContainers() throws ServiceException {
        return listContainers(new ListContainersOptions());
    }

    @Override
    public ListContainersResult listContainers(ListContainersOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path("/").queryParam("comp", "list");
        webResource = addOptionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = addOptionalQueryParam(webResource, "marker", options.getMarker());
        webResource = addOptionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);
        webResource = addOptionalContainerIncludeQueryParam(options, webResource);

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        return builder.get(ListContainersResult.class);
    }

    @Override
    public ListBlobsResult listBlobs(String container) throws ServiceException {
        return listBlobs(container, new ListBlobsOptions());
    }

    @Override
    public ListBlobsResult listBlobs(String container, ListBlobsOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).queryParam("comp", "list")
                .queryParam("resType", "container");
        webResource = addOptionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = addOptionalQueryParam(webResource, "marker", options.getMarker());
        webResource = addOptionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);
        webResource = addOptionalQueryParam(webResource, "delimiter", options.getDelimiter());
        webResource = addOptionalBlobListingIncludeQueryParam(options, webResource);

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        return builder.get(ListBlobsResult.class);
    }

    @Override
    public void createPageBlob(String container, String blob, int length) throws ServiceException {
        createPageBlob(container, blob, length, new CreateBlobOptions());
    }

    @Override
    public void createPageBlob(String container, String blob, int length, CreateBlobOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container + "/" + blob);

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-blob-type", "PageBlob");
        builder = addOptionalHeader(builder, "Content-Length", 0);
        builder = addOptionalHeader(builder, "x-ms-blob-content-length", length);
        builder = addOptionalHeader(builder, "x-ms-blob-sequence-number", options.getSequenceNumber());
        builder = addPutBlobHeaders(options, builder);

        builder.put();
    }

    @Override
    public void createBlockBlob(String container, String blob, InputStream contentStream) throws ServiceException {
        createBlockBlob(container, blob, contentStream, new CreateBlobOptions());
    }

    @Override
    public void createBlockBlob(String container, String blob, InputStream contentStream, CreateBlobOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container + "/" + blob);

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder = builder.header("x-ms-blob-type", "BlockBlob");
        builder = addPutBlobHeaders(options, builder);

        Object contentObject = (contentStream == null ? new byte[0] : contentStream);
        builder.put(contentObject);
    }

    @Override
    public GetBlobPropertiesResult getBlobProperties(String container, String blob) throws ServiceException {
        return getBlobProperties(container, blob, new GetBlobPropertiesOptions());
    }

    @Override
    public GetBlobPropertiesResult getBlobProperties(String container, String blob, GetBlobPropertiesOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob);
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.method("HEAD", ClientResponse.class);
        ThrowIfError(response);

        return getBlobPropertiesResultFromResponse(response);
    }

    @Override
    public GetBlobMetadataResult getBlobMetadata(String container, String blob) throws ServiceException {
        return getBlobMetadata(container, blob, new GetBlobMetadataOptions());
    }

    @Override
    public GetBlobMetadataResult getBlobMetadata(String container, String blob, GetBlobMetadataOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "metadata");
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        GetBlobMetadataResult properties = new GetBlobMetadataResult();
        properties.setEtag(response.getHeaders().getFirst("ETag"));
        properties.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));
        properties.setMetadata(getMetadataFromHeaders(response));

        return properties;
    }

    @Override
    public SetBlobPropertiesResult setBlobProperties(String container, String blob, SetBlobPropertiesOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "properties");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-blob-cache-control", options.getCacheControl());
        builder = addOptionalHeader(builder, "x-ms-blob-content-type", options.getContentType());
        builder = addOptionalHeader(builder, "x-ms-blob-content-md5", options.getContentMD5());
        builder = addOptionalHeader(builder, "x-ms-blob-content-encoding", options.getContentEncoding());
        builder = addOptionalHeader(builder, "x-ms-blob-content-language", options.getContentLanguage());
        builder = addOptionalHeader(builder, "x-ms-blob-content-length", options.getContentLength());
        builder = addOptionalHeader(builder, "x-ms-sequence-number-action", options.getSequenceNumberAction());
        builder = addOptionalHeader(builder, "x-ms-blob-sequence-number", options.getSequenceNumber());
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.put(ClientResponse.class);
        ThrowIfError(response);

        SetBlobPropertiesResult result = new SetBlobPropertiesResult();

        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));
        if (response.getHeaders().getFirst("x-ms-blob-sequence-number") != null) {
            result.setSequenceNumber(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-sequence-number")));
        }

        return result;
    }

    @Override
    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata)
            throws ServiceException {
        return setBlobMetadata(container, blob, metadata, new SetBlobMetadataOptions());
    }

    @Override
    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata,
            SetBlobMetadataOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "metadata");

        WebResource.Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalMetadataHeader(builder, metadata);
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.put(ClientResponse.class);
        ThrowIfError(response);

        SetBlobMetadataResult result = new SetBlobMetadataResult();
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));
        return result;
    }

    @Override
    public GetBlobResult getBlob(String container, String blob) throws ServiceException {
        return getBlob(container, blob, new GetBlobOptions());
    }

    @Override
    public GetBlobResult getBlob(String container, String blob, GetBlobOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob);
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalRangeHeader(builder, options.getRangeStart(), options.getRangeEnd());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        GetBlobPropertiesResult properties = getBlobPropertiesResultFromResponse(response);
        GetBlobResult blobResult = new GetBlobResult();
        blobResult.setProperties(properties.getProperties());
        blobResult.setMetadata(properties.getMetadata());
        blobResult.setContentStream(response.getEntityInputStream());
        return blobResult;
    }

    @Override
    public void deleteBlob(String container, String blob) throws ServiceException {
        deleteBlob(container, blob, new DeleteBlobOptions());
    }

    @Override
    public void deleteBlob(String container, String blob, DeleteBlobOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container + "/" + blob);
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-delete-snapshots", options.getDeleteSnaphotsOnly() ? "only"
                : "include");
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        builder.delete();
    }

    @Override
    public CreateBlobSnapshotResult createBlobSnapshot(String container, String blob) throws ServiceException {
        return createBlobSnapshot(container, blob, new CreateBlobSnapshotOptions());
    }

    @Override
    public CreateBlobSnapshotResult createBlobSnapshot(String container, String blob, CreateBlobSnapshotOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container + "/" + blob).queryParam("comp", "snapshot");

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.put(ClientResponse.class);
        ThrowIfError(response);

        CreateBlobSnapshotResult blobSnapshot = new CreateBlobSnapshotResult();
        blobSnapshot.setEtag(response.getHeaders().getFirst("ETag"));
        blobSnapshot.setSnapshot(response.getHeaders().getFirst("x-ms-snapshot"));
        blobSnapshot.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));

        return blobSnapshot;
    }

    @Override
    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob)
            throws ServiceException {
        copyBlob(destinationContainer, destinationBlob, sourceContainer, sourceBlob, new CopyBlobOptions());
    }

    @Override
    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer,
            String sourceBlob, CopyBlobOptions options) {
        WebResource webResource = getResource(options).path(destinationContainer).path(destinationBlob);

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-source-lease-id", options.getSourceLeaseId());
        builder = addOptionalHeader(builder, "x-ms-copy-source",
                getCopyBlobSourceName(sourceContainer, sourceBlob, options));
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());
        builder = addOptionalSourceAccessContitionHeader(builder, options.getSourceAccessCondition());

        builder.put();
    }

    @Override
    public AcquireLeaseResult acquireLease(String container, String blob) throws ServiceException {
        return acquireLease(container, blob, new AcquireLeaseOptions());
    }

    @Override
    public AcquireLeaseResult acquireLease(String container, String blob, AcquireLeaseOptions options)
            throws ServiceException {
        return putLeaseImpl("acquire", container, blob, null/* leaseId */, options, options.getAccessCondition());
    }

    @Override
    public AcquireLeaseResult renewLease(String container, String blob, String leaseId) throws ServiceException {
        return renewLease(container, blob, leaseId, new BlobServiceOptions());
    }

    @Override
    public AcquireLeaseResult renewLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException {
        return putLeaseImpl("renew", container, blob, leaseId, options, null/* accessCondition */);
    }

    @Override
    public void releaseLease(String container, String blob, String leaseId) throws ServiceException {
        releaseLease(container, blob, leaseId, new BlobServiceOptions());
    }

    @Override
    public void releaseLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException {
        putLeaseImpl("release", container, blob, leaseId, options, null/* accessCondition */);
    }

    @Override
    public void breakLease(String container, String blob, String leaseId) throws ServiceException {
        breakLease(container, blob, leaseId, new BlobServiceOptions());
    }

    @Override
    public void breakLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException {
        putLeaseImpl("break", container, blob, leaseId, options, null/* accessCondition */);
    }

    private AcquireLeaseResult putLeaseImpl(String leaseAction, String container, String blob, String leaseId,
            BlobServiceOptions options, AccessCondition accessCondition) throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "lease");

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", leaseId);
        builder = addOptionalHeader(builder, "x-ms-lease-action", leaseAction);
        builder = addOptionalAccessContitionHeader(builder, accessCondition);

        // Note: Add content type here to enable proper HMAC signing
        ClientResponse response = builder.put(ClientResponse.class);
        ThrowIfError(response);

        AcquireLeaseResult result = new AcquireLeaseResult();
        result.setLeaseId(response.getHeaders().getFirst("x-ms-lease-id"));
        return result;
    }

    @Override
    public CreateBlobPagesResult clearBlobPages(String container, String blob, PageRange range) throws ServiceException {
        return clearBlobPages(container, blob, range, new CreateBlobPagesOptions());
    }

    @Override
    public CreateBlobPagesResult clearBlobPages(String container, String blob, PageRange range,
            CreateBlobPagesOptions options) throws ServiceException {
        return updatePageBlobPagesImpl("clear", container, blob, range, 0, null, options);
    }

    @Override
    public CreateBlobPagesResult createBlobPages(String container, String blob, PageRange range, long length,
            InputStream contentStream) throws ServiceException {
        return createBlobPages(container, blob, range, length, contentStream, new CreateBlobPagesOptions());
    }

    @Override
    public CreateBlobPagesResult createBlobPages(String container, String blob, PageRange range, long length,
            InputStream contentStream, CreateBlobPagesOptions options) throws ServiceException {
        return updatePageBlobPagesImpl("update", container, blob, range, length, contentStream, options);
    }

    private CreateBlobPagesResult updatePageBlobPagesImpl(String action, String container, String blob,
            PageRange range, long length, InputStream contentStream, CreateBlobPagesOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "page");

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalRangeHeader(builder, range.getStart(), range.getEnd());
        builder = addOptionalHeader(builder, "Content-Length", length);
        builder = addOptionalHeader(builder, "Content-MD5", options.getContentMD5());
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-page-write", action);
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.put(ClientResponse.class, contentStream);
        ThrowIfError(response);

        CreateBlobPagesResult result = new CreateBlobPagesResult();
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));
        result.setContentMD5(response.getHeaders().getFirst("Content-MD5"));
        result.setSequenceNumber(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-sequence-number")));

        return result;
    }

    @Override
    public ListBlobRegionsResult listBlobRegions(String container, String blob) throws ServiceException {
        return listBlobRegions(container, blob, new ListBlobRegionsOptions());
    }

    @Override
    public ListBlobRegionsResult listBlobRegions(String container, String blob, ListBlobRegionsOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "pagelist");

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalRangeHeader(builder, options.getRangeStart(), options.getRangeEnd());
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        ListBlobRegionsResult result = response.getEntity(ListBlobRegionsResult.class);
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setContentLength(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-content-length")));
        result.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));

        return result;
    }

    @Override
    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream)
            throws ServiceException {
        createBlobBlock(container, blob, blockId, contentStream, new CreateBlobBlockOptions());
    }

    @Override
    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream,
            CreateBlobBlockOptions options) throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "block");
        webResource = addOptionalQueryParam(webResource, "blockid", new String(Base64.encode(blockId)));

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "Content-MD5", options.getContentMD5());

        builder.put(contentStream);
    }

    @Override
    public void commitBlobBlocks(String container, String blob, BlockList blockList) throws ServiceException {
        commitBlobBlocks(container, blob, blockList, new CommitBlobBlocksOptions());
    }

    @Override
    public void commitBlobBlocks(String container, String blob, BlockList blockList, CommitBlobBlocksOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "blocklist");

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-blob-cache-control", options.getBlobCacheControl());
        builder = addOptionalHeader(builder, "x-ms-blob-content-type", options.getBlobContentType());
        builder = addOptionalHeader(builder, "x-ms-blob-content-encoding", options.getBlobContentEncoding());
        builder = addOptionalHeader(builder, "x-ms-blob-content-language", options.getBlobContentLanguage());
        builder = addOptionalHeader(builder, "x-ms-blob-content-md5", options.getBlobContentMD5());
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalAccessContitionHeader(builder, options.getAccessCondition());

        builder.put(blockList);
    }

    @Override
    public ListBlobBlocksResult listBlobBlocks(String container, String blob) throws ServiceException {
        return listBlobBlocks(container, blob, new ListBlobBlocksOptions());
    }

    @Override
    public ListBlobBlocksResult listBlobBlocks(String container, String blob, ListBlobBlocksOptions options)
            throws ServiceException {
        WebResource webResource = getResource(options).path(container).path(blob).queryParam("comp", "blocklist");
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());
        if (options.isCommittedList() && options.isUncommittedList()) {
            webResource = addOptionalQueryParam(webResource, "blocklisttype", "all");
        }
        else if (options.isCommittedList()) {
            webResource = addOptionalQueryParam(webResource, "blocklisttype", "committed");
        }
        else if (options.isUncommittedList()) {
            webResource = addOptionalQueryParam(webResource, "blocklisttype", "uncommitted");
        }

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());

        ClientResponse response = builder.get(ClientResponse.class);
        ThrowIfError(response);

        ListBlobBlocksResult result = response.getEntity(ListBlobBlocksResult.class);
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setContentType(response.getHeaders().getFirst("Content-Type"));
        result.setContentLength(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-content-length")));
        result.setLastModified(dateMapper.parse(response.getHeaders().getFirst("Last-Modified")));

        return result;
    }
}
