package com.microsoft.windowsazure.services.blob.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.microsoft.windowsazure.services.blob.models.AccessCondition;
import com.microsoft.windowsazure.services.blob.models.AccessConditionHeaderType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class JerseyHelpers {
    public static void ThrowIfError(ClientResponse r) {
        if (r.getStatus() >= 300) {
            throw new UniformInterfaceException(r);
        }
    }

    public static class EnumCommaStringBuilder {
        private final StringBuilder sb = new StringBuilder();

        public void add(String representation) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(representation);
        }

        public void addValue(boolean value, String representation) {
            if (value) {
                add(representation);
            }
        }

        public String getValue() {
            if (sb.length() == 0)
                return null;
            return sb.toString();
        }
    }

    public static WebResource addOptionalQueryParam(WebResource webResource, String key, Object value) {
        if (value != null) {
            webResource = webResource.queryParam(key, value.toString());
        }
        return webResource;
    }

    public static WebResource addOptionalQueryParam(WebResource webResource, String key, int value, int defaultValue) {
        if (value != defaultValue) {
            webResource = webResource.queryParam(key, Integer.toString(value));
        }
        return webResource;
    }

    public static Builder addOptionalHeader(Builder builder, String name, Object value) {
        if (value != null) {
            builder = builder.header(name, value);
        }
        return builder;
    }

    public static Builder addOptionalMetadataHeader(Builder builder, Map<String, String> metadata) {
        for (Entry<String, String> entry : metadata.entrySet()) {
            builder = builder.header("x-ms-meta-" + entry.getKey(), entry.getValue());
        }
        return builder;
    }

    public static Builder addOptionalRangeHeader(Builder builder, Long rangeStart, Long rangeEnd) {
        if (rangeStart != null) {
            String range = rangeStart.toString() + "-";
            if (rangeEnd != null) {
                range += rangeEnd.toString();
            }
            builder = addOptionalHeader(builder, "Range", "bytes=" + range);
        }
        return builder;
    }

    public static Builder addOptionalAccessContitionHeader(Builder builder, AccessCondition accessCondition) {
        if (accessCondition != null) {
            if (accessCondition.getHeader() != AccessConditionHeaderType.NONE) {
                builder = addOptionalHeader(builder, accessCondition.getHeader().toString(), accessCondition.getValue());
            }
        }
        return builder;
    }

    public static Builder addOptionalSourceAccessContitionHeader(Builder builder, AccessCondition accessCondition) {
        if (accessCondition != null) {
            if (accessCondition.getHeader() != AccessConditionHeaderType.NONE) {
                String headerName;
                switch (accessCondition.getHeader()) {
                    case IF_MATCH:
                        headerName = "x-ms-source-if-match";
                    case IF_UNMODIFIED_SINCE:
                        headerName = "x-ms-source-if-unmodified-since";
                    case IF_MODIFIED_SINCE:
                        headerName = "x-ms-source-if-modified-since";
                    case IF_NONE_MATCH:
                        headerName = "x-ms-source-if-none-match";
                    default:
                        headerName = "";
                }
                builder = addOptionalHeader(builder, headerName, accessCondition.getValue());
            }
        }
        return builder;
    }

    public static HashMap<String, String> getMetadataFromHeaders(ClientResponse response) {
        HashMap<String, String> metadata = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
            if (entry.getKey().startsWith("x-ms-meta-")) {
                String name = entry.getKey().substring("x-ms-meta-".length());
                String value = entry.getValue().get(0);
                metadata.put(name, value);
            }
        }
        return metadata;
    }

}
