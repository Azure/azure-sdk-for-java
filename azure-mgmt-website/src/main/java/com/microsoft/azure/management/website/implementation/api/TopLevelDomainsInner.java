/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in TopLevelDomains.
 */
public final class TopLevelDomainsInner {
    /** The Retrofit service to perform REST calls. */
    private TopLevelDomainsService service;
    /** The service client containing this operation class. */
    private WebSiteManagementClientImpl client;

    /**
     * Initializes an instance of TopLevelDomainsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public TopLevelDomainsInner(Retrofit retrofit, WebSiteManagementClientImpl client) {
        this.service = retrofit.create(TopLevelDomainsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for TopLevelDomains to be
     * used by Retrofit to perform actually REST calls.
     */
    interface TopLevelDomainsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/topLevelDomains")
        Call<ResponseBody> getGetTopLevelDomains(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/topLevelDomains/{name}")
        Call<ResponseBody> getTopLevelDomain(@Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/topLevelDomains/{name}/listAgreements")
        Call<ResponseBody> listTopLevelDomainAgreements(@Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body TopLevelDomainAgreementOption agreementOption, @Header("User-Agent") String userAgent);

    }

    /**
     * Lists all top level domains supported for registration.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TopLevelDomainCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<TopLevelDomainCollectionInner> getGetTopLevelDomains() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getGetTopLevelDomains(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getGetTopLevelDomainsDelegate(call.execute());
    }

    /**
     * Lists all top level domains supported for registration.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getGetTopLevelDomainsAsync(final ServiceCallback<TopLevelDomainCollectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.getGetTopLevelDomains(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<TopLevelDomainCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getGetTopLevelDomainsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<TopLevelDomainCollectionInner> getGetTopLevelDomainsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<TopLevelDomainCollectionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<TopLevelDomainCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets details of a top level domain.
     *
     * @param name Name of the top level domain
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TopLevelDomainInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<TopLevelDomainInner> getTopLevelDomain(String name) throws CloudException, IOException, IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getTopLevelDomain(name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getTopLevelDomainDelegate(call.execute());
    }

    /**
     * Gets details of a top level domain.
     *
     * @param name Name of the top level domain
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getTopLevelDomainAsync(String name, final ServiceCallback<TopLevelDomainInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.getTopLevelDomain(name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<TopLevelDomainInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getTopLevelDomainDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<TopLevelDomainInner> getTopLevelDomainDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<TopLevelDomainInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<TopLevelDomainInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists legal agreements that user needs to accept before purchasing domain.
     *
     * @param name Name of the top level domain
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TldLegalAgreementCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<TldLegalAgreementCollectionInner> listTopLevelDomainAgreements(String name) throws CloudException, IOException, IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean includePrivacy = null;
        TopLevelDomainAgreementOption agreementOption = new TopLevelDomainAgreementOption();
        agreementOption.withIncludePrivacy(null);
        Call<ResponseBody> call = service.listTopLevelDomainAgreements(name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), agreementOption, this.client.userAgent());
        return listTopLevelDomainAgreementsDelegate(call.execute());
    }

    /**
     * Lists legal agreements that user needs to accept before purchasing domain.
     *
     * @param name Name of the top level domain
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTopLevelDomainAgreementsAsync(String name, final ServiceCallback<TldLegalAgreementCollectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        final Boolean includePrivacy = null;
        TopLevelDomainAgreementOption agreementOption = new TopLevelDomainAgreementOption();
        agreementOption.withIncludePrivacy(null);
        Call<ResponseBody> call = service.listTopLevelDomainAgreements(name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), agreementOption, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<TldLegalAgreementCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listTopLevelDomainAgreementsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists legal agreements that user needs to accept before purchasing domain.
     *
     * @param name Name of the top level domain
     * @param includePrivacy If true then the list of agreements will inclue agreements for domain privacy as well.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TldLegalAgreementCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<TldLegalAgreementCollectionInner> listTopLevelDomainAgreements(String name, Boolean includePrivacy) throws CloudException, IOException, IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        TopLevelDomainAgreementOption agreementOption = new TopLevelDomainAgreementOption();
        agreementOption.withIncludePrivacy(includePrivacy);
        Call<ResponseBody> call = service.listTopLevelDomainAgreements(name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), agreementOption, this.client.userAgent());
        return listTopLevelDomainAgreementsDelegate(call.execute());
    }

    /**
     * Lists legal agreements that user needs to accept before purchasing domain.
     *
     * @param name Name of the top level domain
     * @param includePrivacy If true then the list of agreements will inclue agreements for domain privacy as well.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTopLevelDomainAgreementsAsync(String name, Boolean includePrivacy, final ServiceCallback<TldLegalAgreementCollectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        TopLevelDomainAgreementOption agreementOption = new TopLevelDomainAgreementOption();
        agreementOption.withIncludePrivacy(includePrivacy);
        Call<ResponseBody> call = service.listTopLevelDomainAgreements(name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), agreementOption, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<TldLegalAgreementCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listTopLevelDomainAgreementsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<TldLegalAgreementCollectionInner> listTopLevelDomainAgreementsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<TldLegalAgreementCollectionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<TldLegalAgreementCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
