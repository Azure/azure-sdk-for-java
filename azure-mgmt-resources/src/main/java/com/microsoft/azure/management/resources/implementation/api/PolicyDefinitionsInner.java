/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.Validator;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in PolicyDefinitions.
 */
public final class PolicyDefinitionsInner {
    /** The Retrofit service to perform REST calls. */
    private PolicyDefinitionsService service;
    /** The service client containing this operation class. */
    private ResourceManagementClientImpl client;

    /**
     * Initializes an instance of PolicyDefinitionsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public PolicyDefinitionsInner(Retrofit retrofit, ResourceManagementClientImpl client) {
        this.service = retrofit.create(PolicyDefinitionsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for PolicyDefinitions to be
     * used by Retrofit to perform actually REST calls.
     */
    interface PolicyDefinitionsService {
        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyDefinitions, 2015-11-01)"})
        @PUT("subscriptions/{subscriptionId}/providers/Microsoft.Authorization/policydefinitions/{policyDefinitionName}")
        Call<ResponseBody> createOrUpdate(@Path("policyDefinitionName") String policyDefinitionName, @Path("subscriptionId") String subscriptionId, @Body PolicyDefinitionInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyDefinitions, 2015-11-01)"})
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.Authorization/policydefinitions/{policyDefinitionName}")
        Call<ResponseBody> get(@Path("policyDefinitionName") String policyDefinitionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyDefinitions, 2015-11-01)"})
        @HTTP(path = "subscriptions/{subscriptionId}/providers/Microsoft.Authorization/policydefinitions/{policyDefinitionName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("policyDefinitionName") String policyDefinitionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Create or update policy definition.
     *
     * @param policyDefinitionName The policy definition name.
     * @param parameters The policy definition properties
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyDefinitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyDefinitionInner> createOrUpdate(String policyDefinitionName, PolicyDefinitionInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (policyDefinitionName == null) {
            throw new IllegalArgumentException("Parameter policyDefinitionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Call<ResponseBody> call = service.createOrUpdate(policyDefinitionName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage());
        return createOrUpdateDelegate(call.execute());
    }

    /**
     * Create or update policy definition.
     *
     * @param policyDefinitionName The policy definition name.
     * @param parameters The policy definition properties
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createOrUpdateAsync(String policyDefinitionName, PolicyDefinitionInner parameters, final ServiceCallback<PolicyDefinitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (policyDefinitionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyDefinitionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdate(policyDefinitionName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyDefinitionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createOrUpdateDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PolicyDefinitionInner> createOrUpdateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyDefinitionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<PolicyDefinitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy definition.
     *
     * @param policyDefinitionName The policy definition name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyDefinitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyDefinitionInner> get(String policyDefinitionName) throws CloudException, IOException, IllegalArgumentException {
        if (policyDefinitionName == null) {
            throw new IllegalArgumentException("Parameter policyDefinitionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(policyDefinitionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        return getDelegate(call.execute());
    }

    /**
     * Gets policy definition.
     *
     * @param policyDefinitionName The policy definition name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String policyDefinitionName, final ServiceCallback<PolicyDefinitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (policyDefinitionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyDefinitionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.get(policyDefinitionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyDefinitionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PolicyDefinitionInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyDefinitionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PolicyDefinitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes policy definition.
     *
     * @param policyDefinitionName The policy definition name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> delete(String policyDefinitionName) throws CloudException, IOException, IllegalArgumentException {
        if (policyDefinitionName == null) {
            throw new IllegalArgumentException("Parameter policyDefinitionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.delete(policyDefinitionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes policy definition.
     *
     * @param policyDefinitionName The policy definition name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String policyDefinitionName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (policyDefinitionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyDefinitionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.delete(policyDefinitionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> deleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

}
