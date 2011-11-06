package com.microsoft.azure.services.blob;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.Base64;

public class BlobServiceImpl implements BlobService {

    private static final String X_MS_VERSION = "x-ms-version";
    private static final String X_MS_BLOB_PUBLIC_ACCESS = "x-ms-blob-public-access";
    private static final String X_MS_META_PREFIX = "x-ms-meta-";
    private static final String API_VERSION = "2011-08-18";
    private final Client channel;
    private final String accountName;
    private final String url;

    @Inject
    public BlobServiceImpl(Client channel, @Named(BlobConfiguration.ACCOUNT_NAME) String accountName, @Named(BlobConfiguration.URL) String url,
    // TODO: How to make this configurable though code?
            BlobSharedKeyLiteFilter filter) {

        this.accountName = accountName;
        this.url = url;
        this.channel = channel;
        channel.addFilter(filter);
    }

    private class EnumCommaStringBuilder<E extends Enum<E>> {
        private final StringBuilder sb = new StringBuilder();

        public void addValue(EnumSet<E> enumSet, E value, String representation) {
            if (enumSet.contains(value)) {
                if (sb.length() >= 0) {
                    sb.append(",");
                }
                sb.append(representation);
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private WebResource addOptionalQueryParam(WebResource wr, String key, String value) {
        if (value != null) {
            wr = wr.queryParam(key, value);
        }
        return wr;
    }

    private WebResource addOptionalQueryParam(WebResource webResource, String key, int value, int defaultValue) {
        if (value != defaultValue) {
            webResource = webResource.queryParam(key, Integer.toString(value));
        }
        return webResource;
    }

    private Builder addOptionalHeader(Builder builder, String name, Object value) {
        if (value != null) {
            builder = builder.header(name, value);
        }
        return builder;
    }

    private Builder addOptionalMetadataHeader(Builder builder, Map<String, String> metadata) {
        for (Entry<String, String> entry : metadata.entrySet()) {
            builder = builder.header(X_MS_META_PREFIX + entry.getKey(), entry.getValue());
        }
        return builder;
    }

    private Builder addOptionalRangeHeader(Builder builder, Long rangeStart, Long rangeEnd) {
        if (rangeStart != null) {
            String range = rangeStart.toString() + "-";
            if (rangeEnd != null) {
                range += rangeEnd.toString();
            }
            builder = addOptionalHeader(builder, "Range", "bytes=" + range);
        }
        return builder;
    }

    private WebResource getResource() {
        WebResource res = channel.resource(url).path("/");

        return res;
    }

    private WebResource setCanonicalizedResource(WebResource wr, String resourceName, String operation) {
        String value = "/" + this.accountName + "/";
        if (resourceName != null) {
            value += resourceName;
        }

        if (operation != null) {
            value += "?comp=" + operation;
        }

        // TODO: Is this the best way to pass a parameter to a filter?
        wr.setProperty("canonicalizedResource", value);
        return wr;
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


    public ServiceProperties getServiceProperties() {
        // TODO: timeout
        WebResource webResource = getResource().path("/").queryParam("resType", "service").queryParam("comp", "properties");
        webResource = setCanonicalizedResource(webResource, null, "properties");

        return webResource.header(X_MS_VERSION, API_VERSION).get(ServiceProperties.class);
    }

    public void setServiceProperties(ServiceProperties serviceProperties) {
        // TODO: timeout
        WebResource webResource = getResource().path("/").queryParam("resType", "service").queryParam("comp", "properties");
        webResource = setCanonicalizedResource(webResource, null, "properties");

        webResource.header(X_MS_VERSION, API_VERSION).type("application/xml").put(serviceProperties);
    }

    public void createContainer(String container) {
        createContainer(container, new CreateContainerOptions());
    }

    public void createContainer(String container, CreateContainerOptions options) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container");
        webResource = setCanonicalizedResource(webResource, container, null);

        WebResource.Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalMetadataHeader(builder, options.getMetadata());
        builder = addOptionalHeader(builder, X_MS_BLOB_PUBLIC_ACCESS, options.getPublicAccess());

        // Note: Add content type here to enable proper HMAC signing
        builder.type("text/plain").put("");
    }

    public void deleteContainer(String container) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container");
        webResource = setCanonicalizedResource(webResource, container, null);

        WebResource.Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        builder.delete();
    }

    public ContainerProperties getContainerProperties(String container) {
        return getContainerPropertiesImpl(container, null);
    }

    public ContainerProperties getContainerMetadata(String container) {
        return getContainerPropertiesImpl(container, "metadata");
    }

    private ContainerProperties getContainerPropertiesImpl(String container, String operation) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container");
        webResource = addOptionalQueryParam(webResource, "comp", operation);
        webResource = setCanonicalizedResource(webResource, container, operation);

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        ClientResponse response = builder.get(ClientResponse.class);

        ContainerProperties properties = new ContainerProperties();
        properties.setEtag(response.getHeaders().getFirst("ETag"));
        properties.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));

        // Metadata
        HashMap<String, String> metadata = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            if (entry.getKey().startsWith(X_MS_META_PREFIX)) {
                String name = entry.getKey().substring(X_MS_META_PREFIX.length());
                String value = entry.getValue().get(0);
                metadata.put(name, value);
            }
        }
        properties.setMetadata(metadata);
        return properties;
    }

    public ContainerACL getContainerACL(String container) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container").queryParam("comp", "acl");
        webResource = setCanonicalizedResource(webResource, container, "acl");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        ClientResponse response = builder.get(ClientResponse.class);

        ContainerACL.SignedIdentifiers si = response.getEntity(ContainerACL.SignedIdentifiers.class);
        ContainerACL acl = new ContainerACL();
        acl.setSignedIdentifiers(si.getSignedIdentifiers());
        acl.setPublicAccess(response.getHeaders().getFirst(X_MS_BLOB_PUBLIC_ACCESS));
        acl.setEtag(response.getHeaders().getFirst("ETag"));
        acl.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));
        return acl;
    }

    public void setContainerACL(String container, ContainerACL acl) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container").queryParam("comp", "acl");
        webResource = setCanonicalizedResource(webResource, container, "acl");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, X_MS_BLOB_PUBLIC_ACCESS, acl.getPublicAccess());

        ContainerACL.SignedIdentifiers si = new ContainerACL.SignedIdentifiers();
        si.setSignedIdentifiers(acl.getSignedIdentifiers());
        // Note: Add content type here to enable proper HMAC signing
        builder.type("application/xml").put(si);
    }

    public void setContainerMetadata(String container, HashMap<String, String> metadata) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container").queryParam("comp", "metadata");
        webResource = setCanonicalizedResource(webResource, container, "metadata");

        WebResource.Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalMetadataHeader(builder, metadata);

        // Note: Add content type here to enable proper HMAC signing
        builder.type("text/plain").put("");
    }

    public ListContainersResult listContainers() {
        return listContainers(new ListContainersOptions());
    }

    public ListContainersResult listContainers(ListContainersOptions options) {
        WebResource webResource = getResource().path("/").queryParam("comp", "list");
        webResource = setCanonicalizedResource(webResource, null, "list");
        webResource = addOptionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = addOptionalQueryParam(webResource, "marker", options.getMarker());
        webResource = addOptionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);
        if (options.getListingDetails().contains(ContainerListingDetails.METADATA)) {
            webResource = webResource.queryParam("include", "metadata");
        }

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        return builder.get(ListContainersResult.class);
    }

    public ListBlobsResult listBlobs(String container) {
        return listBlobs(container, new ListBlobsOptions());
    }

    public ListBlobsResult listBlobs(String container, ListBlobsOptions options) {
        WebResource webResource = getResource().path(container).queryParam("comp", "list").queryParam("resType", "container");
        webResource = setCanonicalizedResource(webResource, container, "list");
        webResource = addOptionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = addOptionalQueryParam(webResource, "marker", options.getMarker());
        webResource = addOptionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);

        if (options.getListingDetails().size() > 0) {
            EnumCommaStringBuilder<BlobListingDetails> sb = new EnumCommaStringBuilder<BlobListingDetails>();

            sb.addValue(options.getListingDetails(), BlobListingDetails.SNAPSHOTS, "snapshots");
            sb.addValue(options.getListingDetails(), BlobListingDetails.UNCOMMITTED_BLOBS, "uncommittedblobs");
            sb.addValue(options.getListingDetails(), BlobListingDetails.METADATA, "metadata");

            webResource = addOptionalQueryParam(webResource, "include", sb.toString());
        }

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        return builder.get(ListBlobsResult.class);
    }

    public void createPageBlob(String container, String blob, int length) {
        createPageBlob(container, blob, length, new CreateBlobOptions());
    }

    public void createPageBlob(String container, String blob, int length, CreateBlobOptions options) {
        WebResource webResource = getResource().path(container + "/" + blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-blob-type", "PageBlob");
        builder = addOptionalHeader(builder, "Content-Length", 0);
        builder = addOptionalHeader(builder, "x-ms-blob-content-length", length);
        builder = addOptionalHeader(builder, "x-ms-blob-sequence-number", options.getSequenceNumber());
        builder = addPutBlobHeaders(options, builder);

        // Note: Add content type here to enable proper HMAC signing
        builder.type("text/plain").put("");
    }

    public void createBlockBlob(String container, String blob, InputStream contentStream) {
        createBlockBlob(container, blob, contentStream, new CreateBlobOptions());
    }

    public void createBlockBlob(String container, String blob, InputStream contentStream, CreateBlobOptions options) {
        WebResource webResource = getResource().path(container + "/" + blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        builder = builder.header("x-ms-blob-type", "BlockBlob");
        builder = addPutBlobHeaders(options, builder);

        Object contentObject = (contentStream == null ? new byte[0] : contentStream);
        builder.put(contentObject);
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

        // TODO: Conditional headers (If Match, etc.)

        return builder;
    }

    public BlobProperties getBlobProperties(String container, String blob) {
        return getBlobProperties(container, blob, new GetBlobPropertiesOptions());
    }

    public BlobProperties getBlobProperties(String container, String blob, GetBlobPropertiesOptions options) {
        WebResource webResource = getResource().path(container).path(blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());

        ClientResponse response = builder.method("HEAD", ClientResponse.class);

        return getBlobPropertiesFromResponse(response);
    }

    public SetBlobPropertiesResult setBlobProperties(String container, String blob, SetBlobPropertiesOptions options) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "properties");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "properties");

        WebResource.Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-blob-cache-control", options.getCacheControl());
        builder = addOptionalHeader(builder, "x-ms-blob-content-type", options.getContentType());
        builder = addOptionalHeader(builder, "x-ms-blob-content-md5", options.getContentMD5());
        builder = addOptionalHeader(builder, "x-ms-blob-content-encoding", options.getContentEncoding());
        builder = addOptionalHeader(builder, "x-ms-blob-content-language", options.getContentLanguage());
        builder = addOptionalHeader(builder, "x-ms-blob-content-length", options.getContentLength());
        builder = addOptionalHeader(builder, "x-ms-sequence-number-action", options.getSequenceNumberAction());
        builder = addOptionalHeader(builder, "x-ms-blob-sequence-number", options.getSequenceNumber());
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());

        // Note: Add content type here to enable proper HMAC signing
        ClientResponse response = builder.type("text/plain").put(ClientResponse.class, "");

        SetBlobPropertiesResult result = new SetBlobPropertiesResult();

        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));
        if (response.getHeaders().getFirst("x-ms-blob-sequence-number") != null) {
            result.setSequenceNumber(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-sequence-number")));
        }

        return result;
    }

    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata) {
        return setBlobMetadata(container, blob, metadata, new SetBlobMetadataOptions());
    }

    public SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata, SetBlobMetadataOptions options) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "metadata");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "metadata");

        WebResource.Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalMetadataHeader(builder, metadata);

        // Note: Add content type here to enable proper HMAC signing
        ClientResponse response = builder.type("text/plain").put(ClientResponse.class, "");

        SetBlobMetadataResult result = new SetBlobMetadataResult();
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));
        return result;
    }

    public Blob getBlob(String container, String blob) {
        return getBlob(container, blob, new GetBlobOptions());
    }

    public Blob getBlob(String container, String blob, GetBlobOptions options) {
        WebResource webResource = getResource().path(container).path(blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalRangeHeader(builder, options.getRangeStart(), options.getRangeEnd());

        ClientResponse response = builder.get(ClientResponse.class);

        BlobProperties properties = getBlobPropertiesFromResponse(response);
        Blob blobResult = new Blob();
        blobResult.setProperties(properties);
        blobResult.setContentStream(response.getEntityInputStream());
        return blobResult;
    }

    private BlobProperties getBlobPropertiesFromResponse(ClientResponse response) {
        BlobProperties properties = new BlobProperties();

        // Last-Modified
        properties.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));

        // Metadata
        HashMap<String, String> metadata = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            if (entry.getKey().startsWith(X_MS_META_PREFIX)) {
                String name = entry.getKey().substring(X_MS_META_PREFIX.length());
                String value = entry.getValue().get(0);
                metadata.put(name, value);
            }
        }
        properties.setMetadata(metadata);

        //
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
            properties.setSequenceNUmber(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-sequence-number")));
        }
        return properties;
    }

    public void deleteBlob(String container, String blob) {
        deleteBlob(container, blob, new DeleteBlobOptions());
    }

    public void deleteBlob(String container, String blob, DeleteBlobOptions options) {
        WebResource webResource = getResource().path(container + "/" + blob);
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-delete-snapshots", options.getDeleteSnaphots());

        builder.delete();
    }

    public BlobSnapshot createBlobSnapshot(String container, String blob) {
        return createBlobSnapshot(container, blob, new CreateBlobSnapshotOptions());
    }

    public BlobSnapshot createBlobSnapshot(String container, String blob, CreateBlobSnapshotOptions options) {
        WebResource webResource = getResource().path(container + "/" + blob).queryParam("comp", "snapshot");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "snapshot");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalMetadataHeader(builder, options.getMetadata());

        // TODO: Conditional headers (If Match, etc.)

        // Note: Add content type here to enable proper HMAC signing
        ClientResponse response = builder.type("text/plain").put(ClientResponse.class, "");

        BlobSnapshot blobSnapshot = new BlobSnapshot();
        blobSnapshot.setEtag(response.getHeaders().getFirst("ETag"));
        blobSnapshot.setSnapshot(response.getHeaders().getFirst("x-ms-snapshot"));
        blobSnapshot.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));

        return blobSnapshot;
    }

    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob) {
        copyBlob(destinationContainer, destinationBlob, sourceContainer, sourceBlob, new CopyBlobOptions());
    }

    public void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob, CopyBlobOptions options) {
        WebResource webResource = getResource().path(destinationContainer).path(destinationBlob);
        webResource = setCanonicalizedResource(webResource, destinationContainer + "/" + destinationBlob, null);

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-source-lease-id", options.getSourceLeaseId());
        builder = addOptionalHeader(builder, "x-ms-copy-source", getCopyBlobSourceName(sourceContainer, sourceBlob, options));
        builder = addOptionalMetadataHeader(builder, options.getMetadata());

        // TODO: Conditional headers (If Match, etc.)

        // Note: Add content type here to enable proper HMAC signing
        builder.type("text/plain").put("");
    }

    public String acquireLease(String container, String blob) {
        return putLeaseImpl("acquire", container, blob, null);
    }

    public String renewLease(String container, String blob, String leaseId) {
        return putLeaseImpl("renew", container, blob, leaseId);
    }

    public void releaseLease(String container, String blob, String leaseId) {
        putLeaseImpl("release", container, blob, leaseId);
    }

    public void breakLease(String container, String blob, String leaseId) {
        putLeaseImpl("break", container, blob, leaseId);
    }

    private String putLeaseImpl(String leaseAction, String container, String blob, String leaseId) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "lease");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "lease");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", leaseId);
        builder = addOptionalHeader(builder, "x-ms-lease-action", leaseAction);

        // Note: Add content type here to enable proper HMAC signing
        ClientResponse response = builder.type("text/plain").put(ClientResponse.class, "");

        return response.getHeaders().getFirst("x-ms-lease-id");
    }

    public CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd) {
        return clearBlobPages(container, blob, rangeStart, rangeEnd, new CreateBlobPagesOptions());
    }

    public CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd, CreateBlobPagesOptions options) {
        return updatePageBlobPagesImpl("clear", container, blob, rangeStart, rangeEnd, 0, null, options);
    }

    public CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream) {
        return createBlobPages(container, blob, rangeStart, rangeEnd, length, contentStream, new CreateBlobPagesOptions());
    }

    public CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream,
            CreateBlobPagesOptions options) {
        return updatePageBlobPagesImpl("update", container, blob, rangeStart, rangeEnd, length, contentStream, options);
    }

    private CreateBlobPagesResult updatePageBlobPagesImpl(String action, String container, String blob, Long rangeStart, Long rangeEnd, long length,
            InputStream contentStream, CreateBlobPagesOptions options) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "page");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "page");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalRangeHeader(builder, rangeStart, rangeEnd);
        builder = addOptionalHeader(builder, "Content-Length", length);
        builder = addOptionalHeader(builder, "Content-MD5", options.getContentMD5());
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-page-write", action);

        // Note: Add content type here to enable proper HMAC signing
        Object content = (contentStream == null ? new byte[0] : contentStream);
        ClientResponse response = builder.type("application/octet-stream").put(ClientResponse.class, content);

        CreateBlobPagesResult result = new CreateBlobPagesResult();
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));
        result.setContentMD5(response.getHeaders().getFirst("Content-MD5"));
        result.setSequenceNumber(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-sequence-number")));

        return result;
    }

    public ListBlobRegionsResult listBlobRegions(String container, String blob) {
        return listBlobRegions(container, blob, new ListBlobRegionsOptions());
    }

    public ListBlobRegionsResult listBlobRegions(String container, String blob, ListBlobRegionsOptions options) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "pagelist");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "pagelist");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalRangeHeader(builder, options.getRangeStart(), options.getRangeEnd());
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());

        ClientResponse response = builder.get(ClientResponse.class);

        ListBlobRegionsResult result = response.getEntity(ListBlobRegionsResult.class);
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setContentLength(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-content-length")));
        result.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));

        return result;
    }

    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream) {
        createBlobBlock(container, blob, blockId, contentStream, new CreateBlobBlockOptions());
    }

    public void createBlobBlock(String container, String blob, String blockId, InputStream contentStream, CreateBlobBlockOptions options) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "block");
        webResource = addOptionalQueryParam(webResource, "blockid", new String(Base64.encode(blockId)));
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "block");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "Content-MD5", options.getContentMD5());

        // Note: Add content type here to enable proper HMAC signing
        builder.type("application/octet-stream").put(contentStream);
    }

    public void commitBlobBlocks(String container, String blob, BlockList blockList) {
        commitBlobBlocks(container, blob, blockList, new CommitBlobBlocksOptions());
    }

    public void commitBlobBlocks(String container, String blob, BlockList blockList, CommitBlobBlocksOptions options) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "blocklist");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "blocklist");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-blob-cache-control", options.getBlobCacheControl());
        builder = addOptionalHeader(builder, "x-ms-blob-content-type", options.getBlobContentType());
        builder = addOptionalHeader(builder, "x-ms-blob-content-encoding", options.getBlobContentEncoding());
        builder = addOptionalHeader(builder, "x-ms-blob-content-language", options.getBlobContentLanguage());
        builder = addOptionalHeader(builder, "x-ms-blob-content-md5", options.getBlobContentMD5());
        builder = addOptionalMetadataHeader(builder, options.getMetadata());

        // Note: Add content type here to enable proper HMAC signing
        builder.type("application/xml").put(blockList);
    }

    public ListBlobBlocksResult listBlobBlocks(String container, String blob) {
        return listBlobBlocks(container, blob, new ListBlobBlocksOptions());
    }

    public ListBlobBlocksResult listBlobBlocks(String container, String blob, ListBlobBlocksOptions options) {
        WebResource webResource = getResource().path(container).path(blob).queryParam("comp", "blocklist");
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, "blocklist");
        webResource = addOptionalQueryParam(webResource, "snapshot", options.getSnapshot());
        webResource = addOptionalQueryParam(webResource, "blocklisttype", options.getListType());

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());

        ClientResponse response = builder.get(ClientResponse.class);

        ListBlobBlocksResult result = response.getEntity(ListBlobBlocksResult.class);
        result.setEtag(response.getHeaders().getFirst("ETag"));
        result.setContentType(response.getHeaders().getFirst("Content-Type"));
        result.setContentLength(Long.parseLong(response.getHeaders().getFirst("x-ms-blob-content-length")));
        result.setLastModified(new DateMapper().parseNoThrow(response.getHeaders().getFirst("Last-Modified")));

        return result;
    }
}
