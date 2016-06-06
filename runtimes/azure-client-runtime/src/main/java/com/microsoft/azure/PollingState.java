/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JacksonMapperAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * An instance of this class defines the state of a long running operation.
 *
 * @param <T> the type of the resource the operation returns.
 */
public class PollingState<T> {
    /** The Retrofit response object. */
    private Response<ResponseBody> response;
    /** The polling status. */
    private String status;
    /** The link in 'Azure-AsyncOperation' header. */
    private String azureAsyncOperationHeaderLink;
    /** The link in 'Location' Header. */
    private String locationHeaderLink;
    /** The timeout interval between two polling operations. */
    private Integer retryTimeout;
    /** The response resource object. */
    private T resource;
    /** The type of the response resource object. */
    private Type resourceType;
    /** The error during the polling operations. */
    private CloudError error;
    /** The adapter for {@link com.fasterxml.jackson.databind.ObjectMapper}. */
    private JacksonMapperAdapter mapperAdapter;

    /**
     * Initializes an instance of {@link PollingState}.
     *
     * @param response the response from Retrofit REST call.
     * @param retryTimeout the long running operation retry timeout.
     * @param resourceType the type of the resource the long running operation returns
     * @param mapperAdapter the adapter for the Jackson object mapper
     * @throws IOException thrown by deserialization
     */
    public PollingState(Response<ResponseBody> response, Integer retryTimeout, Type resourceType, JacksonMapperAdapter mapperAdapter) throws IOException {
        this.retryTimeout = retryTimeout;
        this.withResponse(response);
        this.resourceType = resourceType;
        this.mapperAdapter = mapperAdapter;

        String responseContent = null;
        PollingResource resource = null;
        if (response.body() != null) {
            responseContent = response.body().string();
            response.body().close();
        }
        if (responseContent != null && !responseContent.isEmpty()) {
            this.resource = mapperAdapter.deserialize(responseContent, resourceType);
            resource = mapperAdapter.deserialize(responseContent, PollingResource.class);
        }
        if (resource != null && resource.getProperties() != null
                && resource.getProperties().getProvisioningState() != null) {
            withStatus(resource.getProperties().getProvisioningState());
        } else {
            switch (this.response.code()) {
                case 202:
                    withStatus(AzureAsyncOperation.IN_PROGRESS_STATUS);
                    break;
                case 204:
                case 201:
                case 200:
                    withStatus(AzureAsyncOperation.SUCCESS_STATUS);
                    break;
                default:
                    withStatus(AzureAsyncOperation.FAILED_STATUS);
            }
        }
    }

    /**
     * Updates the polling state from a PUT or PATCH operation.
     *
     * @param response the response from Retrofit REST call
     * @throws CloudException thrown if the response is invalid
     * @throws IOException thrown by deserialization
     */
    public void updateFromResponseOnPutPatch(Response<ResponseBody> response) throws CloudException, IOException {
        String responseContent = null;
        if (response.body() != null) {
            responseContent = response.body().string();
            response.body().close();
        }

        if (responseContent == null || responseContent.isEmpty()) {
            CloudException exception = new CloudException("no body");
            exception.withResponse(response);
            throw exception;
        }

        PollingResource resource = mapperAdapter.deserialize(responseContent, PollingResource.class);
        if (resource != null && resource.getProperties() != null && resource.getProperties().getProvisioningState() != null) {
            this.withStatus(resource.getProperties().getProvisioningState());
        } else {
            this.withStatus(AzureAsyncOperation.SUCCESS_STATUS);
        }

        CloudError error = new CloudError();
        this.withError(error);
        error.withCode(this.getStatus());
        error.withMessage("Long running operation failed");
        this.withResponse(response);
        this.withResource(mapperAdapter.<T>deserialize(responseContent, resourceType));
    }

    /**
     * Updates the polling state from a DELETE or POST operation.
     *
     * @param response the response from Retrofit REST call
     * @throws IOException thrown by deserialization
     */

    public void updateFromResponseOnDeletePost(Response<ResponseBody> response) throws IOException {
        this.withResponse(response);
        String responseContent = null;
        if (response.body() != null) {
            responseContent = response.body().string();
            response.body().close();
        }
        this.withResource(mapperAdapter.<T>deserialize(responseContent, resourceType));
        withStatus(AzureAsyncOperation.SUCCESS_STATUS);
    }

    /**
     * Gets long running operation delay in milliseconds.
     *
     * @return the delay in milliseconds.
     */
    public int getDelayInMilliseconds() {
        if (this.retryTimeout != null) {
            return this.retryTimeout * 1000;
        }
        if (this.response != null && response.headers().get("Retry-After") != null) {
            return Integer.parseInt(response.headers().get("Retry-After")) * 1000;
        }
        return AzureAsyncOperation.DEFAULT_DELAY * 1000;
    }

    /**
     * Gets the polling status.
     *
     * @return the polling status.
     */
    public String getStatus() {
        return status;
    }


    /**
     * Sets the polling status.
     *
     * @param status the polling status.
     * @throws IllegalArgumentException thrown if status is null.
     */
    public void withStatus(String status) throws IllegalArgumentException {
        if (status == null) {
            throw new IllegalArgumentException("Status is null.");
        }
        this.status = status;
    }

    /**
     * Gets the last operation response.
     *
     * @return the last operation response.
     */
    public Response<ResponseBody> getResponse() {
        return this.response;
    }


    /**
     * Sets the last operation response.
     *
     * @param response the last operation response.
     */
    public void withResponse(Response<ResponseBody> response) {
        this.response = response;
        if (response != null) {
            String asyncHeader = response.headers().get("Azure-AsyncOperation");
            String locationHeader = response.headers().get("Location");
            if (asyncHeader != null) {
                this.azureAsyncOperationHeaderLink = asyncHeader;
            }
            if (locationHeader != null) {
                this.locationHeaderLink = locationHeader;
            }
        }
    }

    /**
     * Gets the latest value captured from Azure-AsyncOperation header.
     *
     * @return the link in the header.
     */
    public String getAzureAsyncOperationHeaderLink() {
        return azureAsyncOperationHeaderLink;
    }

    /**
     * Gets the latest value captured from Location header.
     *
     * @return the link in the header.
     */
    public String getLocationHeaderLink() {
        return locationHeaderLink;
    }

    /**
     * Gets the resource.
     *
     * @return the resource.
     */
    public T getResource() {
        return resource;
    }

    /**
     * Sets the resource.
     *
     * @param resource the resource.
     */
    public void withResource(T resource) {
        this.resource = resource;
    }

    /**
     * Gets {@link CloudError} from current instance.
     *
     * @return the cloud error.
     */
    public CloudError getError() {
        return error;
    }

    /**
     * Sets {@link CloudError} from current instance.
     *
     * @param error the cloud error.
     */
    public void withError(CloudError error) {
        this.error = error;
    }

    /**
     * An instance of this class describes the status of a long running operation
     * and is returned from server each time.
     */
    static class PollingResource {
        /** Inner properties object. */
        @JsonProperty(value = "properties")
        private Properties properties;

        /**
         * Gets the inner properties object.
         *
         * @return the inner properties.
         */
        public Properties getProperties() {
            return properties;
        }

        /**
         * Sets the inner properties object.
         *
         * @param properties the inner properties.
         */
        public void withProperties(Properties properties) {
            this.properties = properties;
        }

        /**
         * Inner properties class.
         */
        static class Properties {
            /** The provisioning state of the resource. */
            @JsonProperty(value = "provisioningState")
            private String provisioningState;

            /**
             * Gets the provisioning state of the resource.
             *
             * @return the provisioning state.
             */
            public String getProvisioningState() {
                return provisioningState;
            }

            /**
             * Sets the provisioning state of the resource.
             *
             * @param provisioningState the provisioning state.
             */
            public void withProvisioningState(String provisioningState) {
                this.provisioningState = provisioningState;
            }
        }
    }
}
