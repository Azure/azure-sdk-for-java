/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.blob.implementation;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.microsoft.windowsazure.services.blob.BlobContract;
import com.microsoft.windowsazure.services.blob.models.BlobServiceOptions;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobResult;
import com.microsoft.windowsazure.core.RFC1123DateConverter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.core.utils.AccessConditionHeader;
import com.microsoft.windowsazure.core.pipeline.PipelineHelpers;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;

public abstract class BlobOperationRestProxy implements BlobContract
{

    private static final String API_VERSION = "2011-08-18";
    private final Client channel;
    private final String accountName;
    private final String url;
    private final RFC1123DateConverter dateMapper;
    private final ClientFilter[] filters;

    protected BlobOperationRestProxy(Client channel, String accountName,
            String url)
    {
        this(channel, new ClientFilter[0], accountName, url,
                new RFC1123DateConverter());
    }

    protected BlobOperationRestProxy(Client channel, ClientFilter[] filters,
            String accountName, String url, RFC1123DateConverter dateMapper)
    {
        this.channel = channel;
        this.accountName = accountName;
        this.url = url;
        this.filters = filters;
        this.dateMapper = dateMapper;
    }

    @Override
    public abstract BlobContract withRequestFilterFirst(
            ServiceRequestFilter serviceRequestFilter);

    @Override
    public abstract BlobContract withRequestFilterLast(
            ServiceRequestFilter serviceRequestFilter);

    @Override
    public abstract BlobContract withResponseFilterFirst(
            ServiceResponseFilter serviceResponseFilter);

    @Override
    public abstract BlobContract withResponseFilterLast(
            ServiceResponseFilter serviceResponseFilter);

    protected Client getChannel()
    {
        return channel;
    }

    protected String getAccountName()
    {
        return accountName;
    }

    protected String getUrl()
    {
        return url;
    }

    protected RFC1123DateConverter getDateMapper()
    {
        return dateMapper;
    }

    protected ClientFilter[] getFilters()
    {
        return filters;
    }

    private void ThrowIfError(ClientResponse r)
    {
        PipelineHelpers.ThrowIfError(r);
    }

    private WebResource addOptionalQueryParam(WebResource webResource,
            String key, Object value)
    {
        return PipelineHelpers.addOptionalQueryParam(webResource, key, value);
    }

    private Builder addOptionalHeader(Builder builder, String name, Object value)
    {
        return PipelineHelpers.addOptionalHeader(builder, name, value);
    }

    private Builder addOptionalMetadataHeader(Builder builder,
            Map<String, String> metadata)
    {
        return PipelineHelpers.addOptionalMetadataHeader(builder, metadata);
    }

    private Builder addOptionalAccessConditionHeader(Builder builder,
            AccessConditionHeader accessCondition)
    {
        return PipelineHelpers.addOptionalAccessConditionHeader(builder,
                accessCondition);
    }

    private Builder addPutBlobHeaders(CreateBlobOptions options, Builder builder)
    {
        builder = addOptionalHeader(builder, "Content-Type",
                options.getContentType());
        if (options.getContentType() == null)
        {
            // Note: Add content type here to enable proper HMAC signing
            builder = builder.type("application/octet-stream");
        }
        builder = addOptionalHeader(builder, "Content-Encoding",
                options.getContentEncoding());
        builder = addOptionalHeader(builder, "Content-Language",
                options.getContentLanguage());
        builder = addOptionalHeader(builder, "Content-MD5",
                options.getContentMD5());
        builder = addOptionalHeader(builder, "Cache-Control",
                options.getCacheControl());
        builder = addOptionalHeader(builder, "x-ms-blob-content-type",
                options.getBlobContentType());
        builder = addOptionalHeader(builder, "x-ms-blob-content-encoding",
                options.getBlobContentEncoding());
        builder = addOptionalHeader(builder, "x-ms-blob-content-language",
                options.getBlobContentLanguage());
        builder = addOptionalHeader(builder, "x-ms-blob-content-md5",
                options.getBlobContentMD5());
        builder = addOptionalHeader(builder, "x-ms-blob-cache-control",
                options.getBlobCacheControl());
        builder = addOptionalHeader(builder, "x-ms-lease-id",
                options.getLeaseId());
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalAccessConditionHeader(builder,
                options.getAccessCondition());

        return builder;
    }


    private WebResource getResource(BlobServiceOptions options)
    {
        WebResource webResource = channel.resource(url).path("/");
        webResource = addOptionalQueryParam(webResource, "timeout",
                options.getTimeout());
        for (ClientFilter filter : filters)
        {
            webResource.addFilter(filter);
        }

        return webResource;
    }

    @Override
    public CreateBlobResult createBlockBlob(String container, String blob,
            InputStream contentStream) throws ServiceException
    {
        return createBlockBlob(container, blob, contentStream,
                new CreateBlobOptions());
    }
    
    @Override
    public CreateBlobResult createBlockBlob(String container, String blob,
            InputStream contentStream, CreateBlobOptions options)
            throws ServiceException
    {
        String path = createPathFromContainer(container);
        WebResource webResource = getResource(options).path(path).path(blob);

        Builder builder = webResource.header("x-ms-version", API_VERSION);

        builder = builder.header("x-ms-blob-type", "BlockBlob");
        builder = addPutBlobHeaders(options, builder);

        Object contentObject = (contentStream == null ? new byte[0]
                : contentStream);
        ClientResponse clientResponse = builder.put(ClientResponse.class,
                contentObject);
        ThrowIfError(clientResponse);

        CreateBlobResult createBlobResult = new CreateBlobResult();
        createBlobResult.setEtag(clientResponse.getHeaders().getFirst("ETag"));
        createBlobResult.setLastModified(dateMapper.parse(clientResponse
                .getHeaders().getFirst("Last-Modified")));

        return createBlobResult;
    }

    @Override
    public void createBlobBlock(String container, String blob, String blockId,
            InputStream contentStream) throws ServiceException
    {
        createBlobBlock(container, blob, blockId, contentStream,
                new CreateBlobBlockOptions());
    }

    @Override
    public void createBlobBlock(String container, String blob, String blockId,
            InputStream contentStream, CreateBlobBlockOptions options)
            throws ServiceException
    {
        String path = createPathFromContainer(container);
        WebResource webResource = getResource(options).path(path).path(blob)
                .queryParam("comp", "block");
        try
        {
            webResource = addOptionalQueryParam(webResource, "blockid",
                    new String(Base64.encode(blockId), "UTF-8"));
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id",
                options.getLeaseId());
        builder = addOptionalHeader(builder, "Content-MD5",
                options.getContentMD5());

        builder.put(contentStream);
    }

    @Override
    public void commitBlobBlocks(String container, String blob,
            BlockList blockList) throws ServiceException
    {
        commitBlobBlocks(container, blob, blockList,
                new CommitBlobBlocksOptions());
    }

    @Override
    public void commitBlobBlocks(String container, String blob,
            BlockList blockList, CommitBlobBlocksOptions options)
            throws ServiceException
    {
        String path = createPathFromContainer(container);
        WebResource webResource = getResource(options).path(path).path(blob)
                .queryParam("comp", "blocklist");

        Builder builder = webResource.header("x-ms-version", API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id",
                options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-blob-cache-control",
                options.getBlobCacheControl());
        builder = addOptionalHeader(builder, "x-ms-blob-content-type",
                options.getBlobContentType());
        builder = addOptionalHeader(builder, "x-ms-blob-content-encoding",
                options.getBlobContentEncoding());
        builder = addOptionalHeader(builder, "x-ms-blob-content-language",
                options.getBlobContentLanguage());
        builder = addOptionalHeader(builder, "x-ms-blob-content-md5",
                options.getBlobContentMD5());
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalAccessConditionHeader(builder,
                options.getAccessCondition());

        builder.put(blockList);
    }

    private String createPathFromContainer(String containerName)
    {
        String path;
        if (containerName == null || containerName.isEmpty())
        {
            path = "$root";
        } else
        {
            path = containerName;
        }
        return path;
    }

}
