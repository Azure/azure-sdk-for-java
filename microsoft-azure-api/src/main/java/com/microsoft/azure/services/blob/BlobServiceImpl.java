package com.microsoft.azure.services.blob;

import java.io.InputStream;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class BlobServiceImpl implements BlobService {

    private static final String X_MS_VERSION = "x-ms-version";
    private static final String X_MS_BLOB_PUBLIC_ACCESS = "x-ms-blob-public-access";
    private static final String X_MS_META_PREFIX = "x-ms-meta-";
    private static final String API_VERSION = "2009-09-19";
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

    private WebResource addOptionalQueryParam(WebResource wr, String key, String value) {
        if (value != null) {
            wr = wr.queryParam(key, value);
        }
        return wr;
    }

    private WebResource addOptionalQueryParam(WebResource wr, String key, int value, int defaultValue) {
        if (value != defaultValue) {
            wr = wr.queryParam(key, Integer.toString(value));
        }
        return wr;
    }

    private Builder addOptionalHeader(Builder builder, String name, Object value) {
        if (value != null) {
            builder = builder.header(name, value);
        }
        return builder;
    }

    private Builder addOptionalHeader(Builder builder, String name, int value, int defaultValue) {
        if (value != defaultValue) {
            builder = builder.header(name, Integer.toString(value));
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

        wr.setProperty("canonicalizedResource", value);
        return wr;
    }

    public void createContainer(String container) {
        createContainer(container, new CreateContainerOptions());
    }

    public void createContainer(String container, CreateContainerOptions options) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container");

        webResource = setCanonicalizedResource(webResource, container, null);

        WebResource.Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        // Metadata
        for (Entry<String, String> entry : options.getMetadata().entrySet()) {
            builder = builder.header(X_MS_META_PREFIX + entry.getKey(), entry.getValue());
        }

        // Public access
        if (options.getPublicAccess() != null) {
            builder = builder.header(X_MS_BLOB_PUBLIC_ACCESS, options.getPublicAccess());
        }

        // TODO: We need the following 2 to make sure that "Content-Length:0"
        // header
        // is sent to the server (IIS doesn't accept PUT without a content
        // length).
        // Since we are sending a "dummy" string, we also need to set the
        // "Content-Type" header so that the hmac filter will see it when
        // producing the authorization hmac.
        builder.header("Content-Type", "text/plain").put("");
    }

    public void deleteContainer(String container) {
        WebResource webResource = getResource();
        webResource = setCanonicalizedResource(webResource, container, null);
        webResource.path(container).queryParam("resType", "container").header(X_MS_VERSION, API_VERSION).delete();
    }

    public ContainerProperties getContainerProperties(String container) {
        return getContainerPropertiesImpl(container, null);
    }

    public ContainerProperties getContainerMetadata(String container) {
        return getContainerPropertiesImpl(container, "metadata");
    }

    private ContainerProperties getContainerPropertiesImpl(String container, String operation) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container");
        if (operation != null) {
            webResource = webResource.queryParam("comp", operation);
        }

        webResource = setCanonicalizedResource(webResource, container, operation);

        ClientResponse response = webResource.header(X_MS_VERSION, API_VERSION).get(ClientResponse.class);

        ContainerProperties properties = new ContainerProperties();
        properties.setEtag(response.getHeaders().getFirst("ETag"));
        try {
            properties.setLastModified(new DateMapper().parse(response.getHeaders().getFirst("Last-Modified")));
        }
        catch (ParseException e) {
            // Server returned an invalid/unsupported date format
            throw new IllegalArgumentException(e);
        }
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

        ClientResponse response = webResource.header(X_MS_VERSION, API_VERSION).get(ClientResponse.class);

        ContainerACL.SignedIdentifiers si = response.getEntity(ContainerACL.SignedIdentifiers.class);
        ContainerACL acl = new ContainerACL();
        acl.setSignedIdentifiers(si.getSignedIdentifiers());
        acl.setPublicAccess(response.getHeaders().getFirst(X_MS_BLOB_PUBLIC_ACCESS));
        acl.setEtag(response.getHeaders().getFirst("ETag"));
        try {
            acl.setLastModified(new DateMapper().parse(response.getHeaders().getFirst("Last-Modified")));
        }
        catch (ParseException e) {
            // TODO: Is this the right way to handle this?
            throw new IllegalArgumentException(e);
        }
        return acl;
    }

    public void setContainerACL(String container, ContainerACL acl) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container").queryParam("comp", "acl");
        webResource = setCanonicalizedResource(webResource, container, "acl");

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        // Note: Add content type here to enable proper HMAC signing
        // builder = builder.header("Content-Type", "application/xml");
        builder = addOptionalHeader(builder, X_MS_BLOB_PUBLIC_ACCESS, acl.getPublicAccess());

        ContainerACL.SignedIdentifiers si = new ContainerACL.SignedIdentifiers();
        si.setSignedIdentifiers(acl.getSignedIdentifiers());
        builder.type("application/xml").put(si);
    }

    public void setContainerMetadata(String container, HashMap<String, String> metadata) {
        WebResource webResource = getResource().path(container).queryParam("resType", "container").queryParam("comp", "metadata");

        webResource = setCanonicalizedResource(webResource, container, "metadata");

        WebResource.Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        // Metadata
        for (Entry<String, String> entry : metadata.entrySet()) {
            builder = builder.header(X_MS_META_PREFIX + entry.getKey(), entry.getValue());
        }

        // TODO: We need the following 2 to make sure that "Content-Length:0"
        // header
        // is sent to the server (IIS doesn't accept PUT without a content
        // length).
        // Since we are sending a "dummy" string, we also need to set the
        // "Content-Type" header so that the hmac filter will see it when
        // producing the authorization hmac.
        builder.header("Content-Type", "text/plain").put("");
    }

    public ListContainersResults listContainers() {
        return listContainers(new ListContainersOptions());
    }

    public ListContainersResults listContainers(ListContainersOptions options) {
        WebResource webResource = getResource().path("/").queryParam("comp", "list");

        webResource = setCanonicalizedResource(webResource, null, "list");
        webResource = addOptionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = addOptionalQueryParam(webResource, "marker", options.getMarker());
        webResource = addOptionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);
        if (options.getListingDetails().contains(ContainerListingDetails.METADATA)) {
            webResource = webResource.queryParam("include", "metadata");
        }

        return webResource.header(X_MS_VERSION, API_VERSION).get(ListContainersResults.class);
    }

    public ListBlobsResults listBlobs(String container) {
        return listBlobs(container, new ListBlobsOptions());
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

    public ListBlobsResults listBlobs(String container, ListBlobsOptions options) {
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

        return webResource.header(X_MS_VERSION, API_VERSION).get(ListBlobsResults.class);
    }

    public void putPageBlob(String container, String blob, int length) {
        putPageBlob(container, blob, length, new PutBlobOptions());
    }

    public void putPageBlob(String container, String blob, int length, PutBlobOptions options) {
        WebResource webResource = getResource().path(container + "/" + blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);
        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        builder = builder.header("x-ms-blob-type", "PageBlob");
        builder = addPutBlobHeaders(options, builder);

        builder = builder.header("Content-Length", 0);
        builder = builder.header("x-ms-blob-content-length", length);
        builder = addOptionalHeader(builder, "x-ms-blob-sequence-number", options.getSequenceNumber());
        // TODO: We need the following 2 to make sure that "Content-Length:0"
        // header
        // is sent to the server (IIS doesn't accept PUT without a content
        // length).
        // Since we are sending a "dummy" string, we also need to set the
        // "Content-Type" header so that the hmac filter will see it when
        // producing the authorization hmac.
        builder.type("text/plain").put("");
    }

    public void putBlockBlob(String container, String blob, InputStream content) {
        putBlockBlob(container, blob, content, new PutBlobOptions());
    }

    public void putBlockBlob(String container, String blob, InputStream content, PutBlobOptions options) {
        WebResource webResource = getResource().path(container + "/" + blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);
        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        builder = builder.header("x-ms-blob-type", "BlockBlob");
        builder = addPutBlobHeaders(options, builder);

        builder.put(content);
    }

    private Builder addPutBlobHeaders(PutBlobOptions options, Builder builder) {
        builder = addOptionalHeader(builder, "Content-Type", options.getContentType());
        if (options.getContentType() == null) {
            // This is technically the default, but we explicitly add here to
            // allow proper
            // signing of the request headers.
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

        // Metadata
        for (Entry<String, String> entry : options.getMetadata().entrySet()) {
            builder = builder.header(X_MS_META_PREFIX + entry.getKey(), entry.getValue());
        }

        // TODO: Conditional headers (If Match, etc.)

        return builder;
    }

    public BlobProperties getBlobProperties(String container, String blob) {
        return getBlobProperties(container, blob, new GetBlobPropertiesOptions());
    }

    public BlobProperties getBlobProperties(String container, String blob, GetBlobPropertiesOptions options) {
        WebResource webResource = getResource().path(container).path(blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);

        if (options.getSnapshot() != null) {
            webResource = addOptionalQueryParam(webResource, "snapshot", new DateMapper().format(options.getSnapshot()));
        }

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());

        ClientResponse response = builder.method("HEAD", ClientResponse.class);

        return getBlobPropertiesFromResponse(response);
    }

    public Blob getBlob(String container, String blob) {
        return getBlob(container, blob, new GetBlobOptions());
    }

    public Blob getBlob(String container, String blob, GetBlobOptions options) {
        WebResource webResource = getResource().path(container).path(blob);
        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);

        if (options.getSnapshot() != null) {
            webResource = addOptionalQueryParam(webResource, "snapshot", new DateMapper().format(options.getSnapshot()));
        }

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);

        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());

        if (options.getRangeStart() != null) {
            String range = options.getRangeStart().toString() + "-";
            if (options.getRangeEnd() != null) {
                range += options.getRangeEnd().toString();
            }
            builder = addOptionalHeader(builder, "Range", range);
        }

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
        try {
            properties.setLastModified(new DateMapper().parse(response.getHeaders().getFirst("Last-Modified")));
        }
        catch (ParseException e) {
            // Server returned an invalid/unsupported date format
            throw new IllegalArgumentException(e);
        }

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
        if (options.getSnapshot() != null) {
            webResource = addOptionalQueryParam(webResource, "snapshot", new DateMapper().format(options.getSnapshot()));
        }

        webResource = setCanonicalizedResource(webResource, container + "/" + blob, null);

        Builder builder = webResource.header(X_MS_VERSION, API_VERSION);
        builder = addOptionalHeader(builder, "x-ms-lease-id", options.getLeaseId());
        builder = addOptionalHeader(builder, "x-ms-delete-snapshots", options.getDeleteSnaphots());

        builder.delete();
    }
}
