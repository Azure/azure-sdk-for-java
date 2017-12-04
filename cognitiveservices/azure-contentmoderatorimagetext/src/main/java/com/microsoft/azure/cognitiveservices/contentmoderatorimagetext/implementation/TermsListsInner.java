/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import retrofit2.Retrofit;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.AzureRegion;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in TermsLists.
 */
public class TermsListsInner {
    /** The Retrofit service to perform REST calls. */
    private TermsListsService service;
    /** The service client containing this operation class. */
    private ContentModeratorImageTextClientImpl client;

    /**
     * Initializes an instance of TermsListsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public TermsListsInner(Retrofit retrofit, ContentModeratorImageTextClientImpl client) {
        this.service = retrofit.create(TermsListsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for TermsLists to be
     * used by Retrofit to perform actually REST calls.
     */
    interface TermsListsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.TermsLists getDetails" })
        @GET("contentmoderator/lists/v1.0/termlists/{listId}")
        Observable<Response<ResponseBody>> getDetails(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.TermsLists deleteTerm" })
        @HTTP(path = "contentmoderator/lists/v1.0/termlists/{listId}/terms/{term}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteTerm(@Path("listId") String listId, @Path("term") String term, @Query("language") String language, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Returns list Id details of the term list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TermListGetListIdDetailsInner object if successful.
     */
    public TermListGetListIdDetailsInner getDetails() {
        return getDetailsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Returns list Id details of the term list with list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<TermListGetListIdDetailsInner> getDetailsAsync(final ServiceCallback<TermListGetListIdDetailsInner> serviceCallback) {
        return ServiceFuture.fromResponse(getDetailsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Returns list Id details of the term list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TermListGetListIdDetailsInner object
     */
    public Observable<TermListGetListIdDetailsInner> getDetailsAsync() {
        return getDetailsWithServiceResponseAsync().map(new Func1<ServiceResponse<TermListGetListIdDetailsInner>, TermListGetListIdDetailsInner>() {
            @Override
            public TermListGetListIdDetailsInner call(ServiceResponse<TermListGetListIdDetailsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns list Id details of the term list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TermListGetListIdDetailsInner object
     */
    public Observable<ServiceResponse<TermListGetListIdDetailsInner>> getDetailsWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.getDetails(this.client.listId(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<TermListGetListIdDetailsInner>>>() {
                @Override
                public Observable<ServiceResponse<TermListGetListIdDetailsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<TermListGetListIdDetailsInner> clientResponse = getDetailsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<TermListGetListIdDetailsInner> getDetailsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<TermListGetListIdDetailsInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<TermListGetListIdDetailsInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteTerm() {
        return deleteTermWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteTermAsync(final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteTermWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteTermAsync() {
        return deleteTermWithServiceResponseAsync().map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteTermWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.term() == null) {
            throw new IllegalArgumentException("Parameter this.client.term() is required and cannot be null.");
        }
        if (this.client.language() == null) {
            throw new IllegalArgumentException("Parameter this.client.language() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.deleteTerm(this.client.listId(), this.client.term(), this.client.language(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<String>>>() {
                @Override
                public Observable<ServiceResponse<String>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<String> clientResponse = deleteTermDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<String> deleteTermDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
