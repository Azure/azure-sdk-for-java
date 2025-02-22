// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.mediaservices.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.mediaservices.fluent.StreamingEndpointsClient;
import com.azure.resourcemanager.mediaservices.fluent.models.AsyncOperationResultInner;
import com.azure.resourcemanager.mediaservices.fluent.models.StreamingEndpointInner;
import com.azure.resourcemanager.mediaservices.fluent.models.StreamingEndpointSkuInfoListResultInner;
import com.azure.resourcemanager.mediaservices.models.AsyncOperationResult;
import com.azure.resourcemanager.mediaservices.models.StreamingEndpoint;
import com.azure.resourcemanager.mediaservices.models.StreamingEndpointSkuInfoListResult;
import com.azure.resourcemanager.mediaservices.models.StreamingEndpoints;
import com.azure.resourcemanager.mediaservices.models.StreamingEntityScaleUnit;

public final class StreamingEndpointsImpl implements StreamingEndpoints {
    private static final ClientLogger LOGGER = new ClientLogger(StreamingEndpointsImpl.class);

    private final StreamingEndpointsClient innerClient;

    private final com.azure.resourcemanager.mediaservices.MediaServicesManager serviceManager;

    public StreamingEndpointsImpl(StreamingEndpointsClient innerClient,
        com.azure.resourcemanager.mediaservices.MediaServicesManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<StreamingEndpoint> list(String resourceGroupName, String accountName) {
        PagedIterable<StreamingEndpointInner> inner = this.serviceClient().list(resourceGroupName, accountName);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new StreamingEndpointImpl(inner1, this.manager()));
    }

    public PagedIterable<StreamingEndpoint> list(String resourceGroupName, String accountName, Context context) {
        PagedIterable<StreamingEndpointInner> inner
            = this.serviceClient().list(resourceGroupName, accountName, context);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new StreamingEndpointImpl(inner1, this.manager()));
    }

    public Response<StreamingEndpoint> getWithResponse(String resourceGroupName, String accountName,
        String streamingEndpointName, Context context) {
        Response<StreamingEndpointInner> inner
            = this.serviceClient().getWithResponse(resourceGroupName, accountName, streamingEndpointName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new StreamingEndpointImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public StreamingEndpoint get(String resourceGroupName, String accountName, String streamingEndpointName) {
        StreamingEndpointInner inner = this.serviceClient().get(resourceGroupName, accountName, streamingEndpointName);
        if (inner != null) {
            return new StreamingEndpointImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public void delete(String resourceGroupName, String accountName, String streamingEndpointName) {
        this.serviceClient().delete(resourceGroupName, accountName, streamingEndpointName);
    }

    public void delete(String resourceGroupName, String accountName, String streamingEndpointName, Context context) {
        this.serviceClient().delete(resourceGroupName, accountName, streamingEndpointName, context);
    }

    public Response<StreamingEndpointSkuInfoListResult> skusWithResponse(String resourceGroupName, String accountName,
        String streamingEndpointName, Context context) {
        Response<StreamingEndpointSkuInfoListResultInner> inner
            = this.serviceClient().skusWithResponse(resourceGroupName, accountName, streamingEndpointName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new StreamingEndpointSkuInfoListResultImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public StreamingEndpointSkuInfoListResult skus(String resourceGroupName, String accountName,
        String streamingEndpointName) {
        StreamingEndpointSkuInfoListResultInner inner
            = this.serviceClient().skus(resourceGroupName, accountName, streamingEndpointName);
        if (inner != null) {
            return new StreamingEndpointSkuInfoListResultImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public void start(String resourceGroupName, String accountName, String streamingEndpointName) {
        this.serviceClient().start(resourceGroupName, accountName, streamingEndpointName);
    }

    public void start(String resourceGroupName, String accountName, String streamingEndpointName, Context context) {
        this.serviceClient().start(resourceGroupName, accountName, streamingEndpointName, context);
    }

    public void stop(String resourceGroupName, String accountName, String streamingEndpointName) {
        this.serviceClient().stop(resourceGroupName, accountName, streamingEndpointName);
    }

    public void stop(String resourceGroupName, String accountName, String streamingEndpointName, Context context) {
        this.serviceClient().stop(resourceGroupName, accountName, streamingEndpointName, context);
    }

    public void scale(String resourceGroupName, String accountName, String streamingEndpointName,
        StreamingEntityScaleUnit parameters) {
        this.serviceClient().scale(resourceGroupName, accountName, streamingEndpointName, parameters);
    }

    public void scale(String resourceGroupName, String accountName, String streamingEndpointName,
        StreamingEntityScaleUnit parameters, Context context) {
        this.serviceClient().scale(resourceGroupName, accountName, streamingEndpointName, parameters, context);
    }

    public Response<AsyncOperationResult> asyncOperationWithResponse(String resourceGroupName, String accountName,
        String operationId, Context context) {
        Response<AsyncOperationResultInner> inner
            = this.serviceClient().asyncOperationWithResponse(resourceGroupName, accountName, operationId, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new AsyncOperationResultImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public AsyncOperationResult asyncOperation(String resourceGroupName, String accountName, String operationId) {
        AsyncOperationResultInner inner
            = this.serviceClient().asyncOperation(resourceGroupName, accountName, operationId);
        if (inner != null) {
            return new AsyncOperationResultImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public Response<StreamingEndpoint> operationLocationWithResponse(String resourceGroupName, String accountName,
        String streamingEndpointName, String operationId, Context context) {
        Response<StreamingEndpointInner> inner = this.serviceClient()
            .operationLocationWithResponse(resourceGroupName, accountName, streamingEndpointName, operationId, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new StreamingEndpointImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public StreamingEndpoint operationLocation(String resourceGroupName, String accountName,
        String streamingEndpointName, String operationId) {
        StreamingEndpointInner inner = this.serviceClient()
            .operationLocation(resourceGroupName, accountName, streamingEndpointName, operationId);
        if (inner != null) {
            return new StreamingEndpointImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    public StreamingEndpoint getById(String id) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String accountName = ResourceManagerUtils.getValueFromIdByName(id, "mediaservices");
        if (accountName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'mediaservices'.", id)));
        }
        String streamingEndpointName = ResourceManagerUtils.getValueFromIdByName(id, "streamingEndpoints");
        if (streamingEndpointName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'streamingEndpoints'.", id)));
        }
        return this.getWithResponse(resourceGroupName, accountName, streamingEndpointName, Context.NONE).getValue();
    }

    public Response<StreamingEndpoint> getByIdWithResponse(String id, Context context) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String accountName = ResourceManagerUtils.getValueFromIdByName(id, "mediaservices");
        if (accountName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'mediaservices'.", id)));
        }
        String streamingEndpointName = ResourceManagerUtils.getValueFromIdByName(id, "streamingEndpoints");
        if (streamingEndpointName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'streamingEndpoints'.", id)));
        }
        return this.getWithResponse(resourceGroupName, accountName, streamingEndpointName, context);
    }

    public void deleteById(String id) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String accountName = ResourceManagerUtils.getValueFromIdByName(id, "mediaservices");
        if (accountName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'mediaservices'.", id)));
        }
        String streamingEndpointName = ResourceManagerUtils.getValueFromIdByName(id, "streamingEndpoints");
        if (streamingEndpointName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'streamingEndpoints'.", id)));
        }
        this.delete(resourceGroupName, accountName, streamingEndpointName, Context.NONE);
    }

    public void deleteByIdWithResponse(String id, Context context) {
        String resourceGroupName = ResourceManagerUtils.getValueFromIdByName(id, "resourceGroups");
        if (resourceGroupName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'resourceGroups'.", id)));
        }
        String accountName = ResourceManagerUtils.getValueFromIdByName(id, "mediaservices");
        if (accountName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'mediaservices'.", id)));
        }
        String streamingEndpointName = ResourceManagerUtils.getValueFromIdByName(id, "streamingEndpoints");
        if (streamingEndpointName == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("The resource ID '%s' is not valid. Missing path segment 'streamingEndpoints'.", id)));
        }
        this.delete(resourceGroupName, accountName, streamingEndpointName, context);
    }

    private StreamingEndpointsClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.mediaservices.MediaServicesManager manager() {
        return this.serviceManager;
    }

    public StreamingEndpointImpl define(String name) {
        return new StreamingEndpointImpl(name, this.manager());
    }
}
