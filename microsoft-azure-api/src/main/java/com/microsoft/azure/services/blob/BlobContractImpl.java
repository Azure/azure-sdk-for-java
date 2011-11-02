package com.microsoft.azure.services.blob;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class BlobContractImpl implements BlobContract {

    private static final String X_MS_VERSION = "x-ms-version";
    private static final String X_MS_BLOB_PUBLIC_ACCESS = "x-ms-blob-public-access";
    private static final String X_MS_META_PREFIX = "x-ms-meta-";
    private static final String API_VERSION = "2009-09-19";
    private final Client channel;
    private final String accountName;
    private final String url;

    @Inject
    public BlobContractImpl(Client channel, @Named(BlobConfig.CONFIG_ACCOUNT_NAME) String accountName, @Named(BlobConfig.CONFIG_URL) String url,
    // TODO: How to make this configurable though code?
            BlobSharedKeyLiteFilter filter) {

        this.accountName = accountName;
        this.url = url;
        this.channel = channel;
        channel.addFilter(filter);
    }

    private WebResource optionalQueryParam(WebResource wr, String key, String value) {
        if (value != null) {
            wr = wr.queryParam(key, value);
        }
        return wr;
    }

    private WebResource optionalQueryParam(WebResource wr, String key, int value, int defaultValue) {
        if (value != defaultValue) {
            wr = wr.queryParam(key, Integer.toString(value));
        }
        return wr;
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

        builder
        // TODO: We need the following 2 to make sure that "Content-Length:0"
        // header
        // is sent to the server (IIS doesn't accept PUT without a content
        // length).
        // Since we are sending a "dummy" string, we also need to set the
        // "Content-Type" header so that the hmac filter will see it when
        // producing the authorization hmac.
        .header("Content-Type", "text/plain").put("");
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
        properties.setLastModified(response.getHeaders().getFirst("Last-Modified"));
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

    public ListContainersResults listContainers() {
        return listContainers(new ListContainersOptions());
    }

    public ListContainersResults listContainers(ListContainersOptions options) {
        WebResource webResource = getResource().path("/").queryParam("comp", "list");

        webResource = setCanonicalizedResource(webResource, null, "list");
        webResource = optionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = optionalQueryParam(webResource, "marker", options.getMarker());
        webResource = optionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);
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
        webResource = optionalQueryParam(webResource, "prefix", options.getPrefix());
        webResource = optionalQueryParam(webResource, "marker", options.getMarker());
        webResource = optionalQueryParam(webResource, "maxresults", options.getMaxResults(), 0);

        if (options.getListingDetails().size() > 0) {
            EnumCommaStringBuilder<BlobListingDetails> sb = new EnumCommaStringBuilder<BlobListingDetails>();

            sb.addValue(options.getListingDetails(), BlobListingDetails.SNAPSHOTS, "snapshots");
            sb.addValue(options.getListingDetails(), BlobListingDetails.UNCOMMITTED_BLOBS, "uncommittedblobs");
            sb.addValue(options.getListingDetails(), BlobListingDetails.METADATA, "metadata");

            webResource = optionalQueryParam(webResource, "include", sb.toString());
        }

        return webResource.header(X_MS_VERSION, API_VERSION).get(ListBlobsResults.class);
    }
}
