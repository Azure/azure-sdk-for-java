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
package com.microsoft.windowsazure.core.pipeline;

import com.microsoft.windowsazure.core.utils.AccessConditionHeader;
import com.microsoft.windowsazure.core.utils.AccessConditionHeaderType;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class PipelineHelpers {
    private PipelineHelpers() {
    }

    private static String createErrorMessage(ClientResponse clientResponse) {
        clientResponse.bufferEntity();
        String errorMessage = clientResponse.toString();
        if (clientResponse.hasEntity()) {
            errorMessage = errorMessage + " "
                    + clientResponse.getEntity(String.class);
        }
        return errorMessage;
    }

    public static void throwIfNotSuccess(ClientResponse clientResponse) {
        int statusCode = clientResponse.getStatus();

        if ((statusCode < 200) || (statusCode >= 300)) {
            String errorMessage = createErrorMessage(clientResponse);
            throw new UniformInterfaceException(errorMessage, clientResponse);
        }
    }

    public static void throwIfError(ClientResponse clientResponse) {
        if (clientResponse.getStatus() >= 400) {
            String errorMessage = createErrorMessage(clientResponse);
            throw new UniformInterfaceException(errorMessage, clientResponse);
        }
    }

    public static WebResource addOptionalQueryParam(WebResource webResource,
            String key, Object value) {
        if (value != null) {
            webResource = webResource.queryParam(key, value.toString());
        }
        return webResource;
    }

    public static WebResource addOptionalQueryParam(WebResource webResource,
            String key, int value, int defaultValue) {
        if (value != defaultValue) {
            webResource = webResource.queryParam(key, Integer.toString(value));
        }
        return webResource;
    }

    public static Builder addOptionalHeader(Builder builder, String name,
            Object value) {
        if (value != null) {
            builder = builder.header(name, value);
        }
        return builder;
    }

    public static Builder addOptionalMetadataHeader(Builder builder,
            Map<String, String> metadata) {
        for (Entry<String, String> entry : metadata.entrySet()) {
            builder = builder.header("x-ms-meta-" + entry.getKey(),
                    entry.getValue());
        }
        return builder;
    }

    public static Builder addOptionalRangeHeader(Builder builder,
            Long rangeStart, Long rangeEnd) {
        if (rangeStart != null) {
            String range = rangeStart.toString() + "-";
            if (rangeEnd != null) {
                range += rangeEnd.toString();
            }
            builder = addOptionalHeader(builder, "Range", "bytes=" + range);
        }
        return builder;
    }

    public static Builder addOptionalAccessConditionHeader(Builder builder,
            AccessConditionHeader accessCondition) {
        if (accessCondition != null) {
            if (accessCondition.getHeader() != AccessConditionHeaderType.NONE) {
                builder = addOptionalHeader(builder, accessCondition
                        .getHeader().toString(), accessCondition.getValue());
            }
        }
        return builder;
    }

    public static Builder addOptionalSourceAccessConditionHeader(
            Builder builder, AccessConditionHeader accessCondition) {
        if (accessCondition != null) {
            if (accessCondition.getHeader() != AccessConditionHeaderType.NONE) {
                String headerName;
                switch (accessCondition.getHeader()) {
                case IF_MATCH:
                    headerName = "x-ms-source-if-match";
                    break;
                case IF_UNMODIFIED_SINCE:
                    headerName = "x-ms-source-if-unmodified-since";
                    break;
                case IF_MODIFIED_SINCE:
                    headerName = "x-ms-source-if-modified-since";
                    break;
                case IF_NONE_MATCH:
                    headerName = "x-ms-source-if-none-match";
                    break;
                default:
                    headerName = "";
                }
                builder = addOptionalHeader(builder, headerName,
                        accessCondition.getValue());
            }
        }
        return builder;
    }

    public static HashMap<String, String> getMetadataFromHeaders(
            ClientResponse response) {
        HashMap<String, String> metadata = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : response.getHeaders()
                .entrySet()) {
            if (entry.getKey().startsWith("x-ms-meta-")) {
                String name = entry.getKey().substring("x-ms-meta-".length());
                String value = entry.getValue().get(0);
                metadata.put(name, value);
            }
        }
        return metadata;
    }

}
