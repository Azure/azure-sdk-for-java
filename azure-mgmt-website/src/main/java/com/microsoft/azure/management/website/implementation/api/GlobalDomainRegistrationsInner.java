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
import com.microsoft.rest.Validator;
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
 * in GlobalDomainRegistrations.
 */
public final class GlobalDomainRegistrationsInner {
    /** The Retrofit service to perform REST calls. */
    private GlobalDomainRegistrationsService service;
    /** The service client containing this operation class. */
    private WebSiteManagementClientImpl client;

    /**
     * Initializes an instance of GlobalDomainRegistrationsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public GlobalDomainRegistrationsInner(Retrofit retrofit, WebSiteManagementClientImpl client) {
        this.service = retrofit.create(GlobalDomainRegistrationsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for GlobalDomainRegistrations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface GlobalDomainRegistrationsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/domains")
        Call<ResponseBody> getAllDomains(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/generateSsoRequest")
        Call<ResponseBody> getDomainControlCenterSsoRequest(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/validateDomainRegistrationInformation")
        Call<ResponseBody> validateDomainPurchaseInformation(@Path("subscriptionId") String subscriptionId, @Body DomainRegistrationInputInner domainRegistrationInput, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/checkDomainAvailability")
        Call<ResponseBody> checkDomainAvailability(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body NameIdentifier identifier, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/providers/Microsoft.DomainRegistration/listDomainRecommendations")
        Call<ResponseBody> listDomainRecommendations(@Path("subscriptionId") String subscriptionId, @Body DomainRecommendationSearchParametersInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Lists all domains in a subscription.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DomainCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DomainCollectionInner> getAllDomains() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getAllDomains(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getAllDomainsDelegate(call.execute());
    }

    /**
     * Lists all domains in a subscription.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAllDomainsAsync(final ServiceCallback<DomainCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getAllDomains(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DomainCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAllDomainsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<DomainCollectionInner> getAllDomainsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DomainCollectionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<DomainCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Generates a single sign on request for domain management portal.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DomainControlCenterSsoRequestInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DomainControlCenterSsoRequestInner> getDomainControlCenterSsoRequest() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getDomainControlCenterSsoRequest(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDomainControlCenterSsoRequestDelegate(call.execute());
    }

    /**
     * Generates a single sign on request for domain management portal.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getDomainControlCenterSsoRequestAsync(final ServiceCallback<DomainControlCenterSsoRequestInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getDomainControlCenterSsoRequest(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DomainControlCenterSsoRequestInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDomainControlCenterSsoRequestDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<DomainControlCenterSsoRequestInner> getDomainControlCenterSsoRequestDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DomainControlCenterSsoRequestInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<DomainControlCenterSsoRequestInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Validates domain registration information.
     *
     * @param domainRegistrationInput Domain registration information
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> validateDomainPurchaseInformation(DomainRegistrationInputInner domainRegistrationInput) throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (domainRegistrationInput == null) {
            throw new IllegalArgumentException("Parameter domainRegistrationInput is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(domainRegistrationInput);
        Call<ResponseBody> call = service.validateDomainPurchaseInformation(this.client.subscriptionId(), domainRegistrationInput, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return validateDomainPurchaseInformationDelegate(call.execute());
    }

    /**
     * Validates domain registration information.
     *
     * @param domainRegistrationInput Domain registration information
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall validateDomainPurchaseInformationAsync(DomainRegistrationInputInner domainRegistrationInput, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (domainRegistrationInput == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter domainRegistrationInput is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(domainRegistrationInput, serviceCallback);
        Call<ResponseBody> call = service.validateDomainPurchaseInformation(this.client.subscriptionId(), domainRegistrationInput, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(validateDomainPurchaseInformationDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> validateDomainPurchaseInformationDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Checks if a domain is available for registration.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DomainAvailablilityCheckResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DomainAvailablilityCheckResultInner> checkDomainAvailability() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String name = null;
        NameIdentifier identifier = new NameIdentifier();
        identifier.withName(null);
        Call<ResponseBody> call = service.checkDomainAvailability(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), identifier, this.client.userAgent());
        return checkDomainAvailabilityDelegate(call.execute());
    }

    /**
     * Checks if a domain is available for registration.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkDomainAvailabilityAsync(final ServiceCallback<DomainAvailablilityCheckResultInner> serviceCallback) throws IllegalArgumentException {
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
        final String name = null;
        NameIdentifier identifier = new NameIdentifier();
        identifier.withName(null);
        Call<ResponseBody> call = service.checkDomainAvailability(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), identifier, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DomainAvailablilityCheckResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkDomainAvailabilityDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Checks if a domain is available for registration.
     *
     * @param name Name of the object
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DomainAvailablilityCheckResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DomainAvailablilityCheckResultInner> checkDomainAvailability(String name) throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        NameIdentifier identifier = new NameIdentifier();
        identifier.withName(name);
        Call<ResponseBody> call = service.checkDomainAvailability(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), identifier, this.client.userAgent());
        return checkDomainAvailabilityDelegate(call.execute());
    }

    /**
     * Checks if a domain is available for registration.
     *
     * @param name Name of the object
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkDomainAvailabilityAsync(String name, final ServiceCallback<DomainAvailablilityCheckResultInner> serviceCallback) throws IllegalArgumentException {
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
        NameIdentifier identifier = new NameIdentifier();
        identifier.withName(name);
        Call<ResponseBody> call = service.checkDomainAvailability(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), identifier, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DomainAvailablilityCheckResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkDomainAvailabilityDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<DomainAvailablilityCheckResultInner> checkDomainAvailabilityDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DomainAvailablilityCheckResultInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<DomainAvailablilityCheckResultInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists domain recommendations based on keywords.
     *
     * @param parameters Domain recommendation search parameters
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the NameIdentifierCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<NameIdentifierCollectionInner> listDomainRecommendations(DomainRecommendationSearchParametersInner parameters) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.listDomainRecommendations(this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return listDomainRecommendationsDelegate(call.execute());
    }

    /**
     * Lists domain recommendations based on keywords.
     *
     * @param parameters Domain recommendation search parameters
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listDomainRecommendationsAsync(DomainRecommendationSearchParametersInner parameters, final ServiceCallback<NameIdentifierCollectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
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
        Call<ResponseBody> call = service.listDomainRecommendations(this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<NameIdentifierCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listDomainRecommendationsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<NameIdentifierCollectionInner> listDomainRecommendationsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<NameIdentifierCollectionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<NameIdentifierCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
