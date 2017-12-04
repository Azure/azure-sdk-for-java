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
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in Termlists.
 */
public class TermlistsInner {
    /** The Retrofit service to perform REST calls. */
    private TermlistsService service;
    /** The service client containing this operation class. */
    private ContentModeratorImageTextClientImpl client;

    /**
     * Initializes an instance of TermlistsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public TermlistsInner(Retrofit retrofit, ContentModeratorImageTextClientImpl client) {
        this.service = retrofit.create(TermlistsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Termlists to be
     * used by Retrofit to perform actually REST calls.
     */
    interface TermlistsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists delete" })
        @HTTP(path = "contentmoderator/lists/v1.0/termlists/{listId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> delete(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists update" })
        @PUT("contentmoderator/lists/v1.0/termlists/{listId}")
        Observable<Response<ResponseBody>> update(@Path("listId") String listId, @Header("Content-Type") String contentType, @Body BodyInner body, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists create" })
        @POST("contentmoderator/lists/v1.0/termlists")
        Observable<Response<ResponseBody>> create(@Header("Content-Type") String contentType, @Body BodyInner body, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists getAllTermLists" })
        @GET("contentmoderator/lists/v1.0/termlists")
        Observable<Response<ResponseBody>> getAllTermLists(@Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists refreshIndexMethod" })
        @POST("contentmoderator/lists/v1.0/termlists/{listId}/RefreshIndex")
        Observable<Response<ResponseBody>> refreshIndexMethod(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists addTerm" })
        @POST("contentmoderator/lists/v1.0/termlists/{listId}/terms/{term}")
        Observable<Response<ResponseBody>> addTerm(@Path("listId") String listId, @Path("term") String term, @Query("language") String language, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists getAllTermsMethod" })
        @GET("contentmoderator/lists/v1.0/termlists/{listId}/terms")
        Observable<Response<ResponseBody>> getAllTermsMethod(@Path("listId") String listId, @Query("language") String language, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Termlists deleteAllTerms" })
        @HTTP(path = "contentmoderator/lists/v1.0/termlists/{listId}/terms", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteAllTerms(@Path("listId") String listId, @Query("language") String language, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Deletes term list with the list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String delete() {
        return deleteWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Deletes term list with the list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteAsync(final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Deletes term list with the list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteAsync() {
        return deleteWithServiceResponseAsync().map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes term list with the list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteWithServiceResponseAsync() {
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
        return service.delete(this.client.listId(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<String>>>() {
                @Override
                public Observable<ServiceResponse<String>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<String> clientResponse = deleteDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<String> deleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Updates an Term List.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the UpdateTermListInner object if successful.
     */
    public UpdateTermListInner update(BodyInner body) {
        return updateWithServiceResponseAsync(body).toBlocking().single().body();
    }

    /**
     * Updates an Term List.
     *
     * @param body Schema of the body.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<UpdateTermListInner> updateAsync(BodyInner body, final ServiceCallback<UpdateTermListInner> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(body), serviceCallback);
    }

    /**
     * Updates an Term List.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the UpdateTermListInner object
     */
    public Observable<UpdateTermListInner> updateAsync(BodyInner body) {
        return updateWithServiceResponseAsync(body).map(new Func1<ServiceResponse<UpdateTermListInner>, UpdateTermListInner>() {
            @Override
            public UpdateTermListInner call(ServiceResponse<UpdateTermListInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Updates an Term List.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the UpdateTermListInner object
     */
    public Observable<ServiceResponse<UpdateTermListInner>> updateWithServiceResponseAsync(BodyInner body) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (body == null) {
            throw new IllegalArgumentException("Parameter body is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        Validator.validate(body);
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.update(this.client.listId(), this.client.contentType(), body, this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<UpdateTermListInner>>>() {
                @Override
                public Observable<ServiceResponse<UpdateTermListInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<UpdateTermListInner> clientResponse = updateDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<UpdateTermListInner> updateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<UpdateTermListInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<UpdateTermListInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates a Term List.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreateTermListInner object if successful.
     */
    public CreateTermListInner create(BodyInner body) {
        return createWithServiceResponseAsync(body).toBlocking().single().body();
    }

    /**
     * Creates a Term List.
     *
     * @param body Schema of the body.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CreateTermListInner> createAsync(BodyInner body, final ServiceCallback<CreateTermListInner> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(body), serviceCallback);
    }

    /**
     * Creates a Term List.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateTermListInner object
     */
    public Observable<CreateTermListInner> createAsync(BodyInner body) {
        return createWithServiceResponseAsync(body).map(new Func1<ServiceResponse<CreateTermListInner>, CreateTermListInner>() {
            @Override
            public CreateTermListInner call(ServiceResponse<CreateTermListInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Creates a Term List.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateTermListInner object
     */
    public Observable<ServiceResponse<CreateTermListInner>> createWithServiceResponseAsync(BodyInner body) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (body == null) {
            throw new IllegalArgumentException("Parameter body is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        Validator.validate(body);
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.create(this.client.contentType(), body, this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreateTermListInner>>>() {
                @Override
                public Observable<ServiceResponse<CreateTermListInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreateTermListInner> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<CreateTermListInner> createDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<CreateTermListInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<CreateTermListInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * gets all the Term Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;GetAllTermListItemInner&gt; object if successful.
     */
    public List<GetAllTermListItemInner> getAllTermLists() {
        return getAllTermListsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * gets all the Term Lists.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<GetAllTermListItemInner>> getAllTermListsAsync(final ServiceCallback<List<GetAllTermListItemInner>> serviceCallback) {
        return ServiceFuture.fromResponse(getAllTermListsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * gets all the Term Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;GetAllTermListItemInner&gt; object
     */
    public Observable<List<GetAllTermListItemInner>> getAllTermListsAsync() {
        return getAllTermListsWithServiceResponseAsync().map(new Func1<ServiceResponse<List<GetAllTermListItemInner>>, List<GetAllTermListItemInner>>() {
            @Override
            public List<GetAllTermListItemInner> call(ServiceResponse<List<GetAllTermListItemInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * gets all the Term Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;GetAllTermListItemInner&gt; object
     */
    public Observable<ServiceResponse<List<GetAllTermListItemInner>>> getAllTermListsWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.getAllTermLists(this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<GetAllTermListItemInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<GetAllTermListItemInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<GetAllTermListItemInner>> clientResponse = getAllTermListsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<GetAllTermListItemInner>> getAllTermListsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<GetAllTermListItemInner>, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<GetAllTermListItemInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Refreshes the index of the list with list Id equal to list ID passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the RefreshIndexInner object if successful.
     */
    public RefreshIndexInner refreshIndexMethod() {
        return refreshIndexMethodWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Refreshes the index of the list with list Id equal to list ID passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<RefreshIndexInner> refreshIndexMethodAsync(final ServiceCallback<RefreshIndexInner> serviceCallback) {
        return ServiceFuture.fromResponse(refreshIndexMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Refreshes the index of the list with list Id equal to list ID passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the RefreshIndexInner object
     */
    public Observable<RefreshIndexInner> refreshIndexMethodAsync() {
        return refreshIndexMethodWithServiceResponseAsync().map(new Func1<ServiceResponse<RefreshIndexInner>, RefreshIndexInner>() {
            @Override
            public RefreshIndexInner call(ServiceResponse<RefreshIndexInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Refreshes the index of the list with list Id equal to list ID passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the RefreshIndexInner object
     */
    public Observable<ServiceResponse<RefreshIndexInner>> refreshIndexMethodWithServiceResponseAsync() {
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
        return service.refreshIndexMethod(this.client.listId(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<RefreshIndexInner>>>() {
                @Override
                public Observable<ServiceResponse<RefreshIndexInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<RefreshIndexInner> clientResponse = refreshIndexMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<RefreshIndexInner> refreshIndexMethodDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<RefreshIndexInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<RefreshIndexInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the Object object if successful.
     */
    public Object addTerm() {
        return addTermWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Object> addTermAsync(final ServiceCallback<Object> serviceCallback) {
        return ServiceFuture.fromResponse(addTermWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    public Observable<Object> addTermAsync() {
        return addTermWithServiceResponseAsync().map(new Func1<ServiceResponse<Object>, Object>() {
            @Override
            public Object call(ServiceResponse<Object> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    public Observable<ServiceResponse<Object>> addTermWithServiceResponseAsync() {
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
        return service.addTerm(this.client.listId(), this.client.term(), this.client.language(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Object>>>() {
                @Override
                public Observable<ServiceResponse<Object>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Object> clientResponse = addTermDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Object> addTermDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Object, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the GetAllTermsInner object if successful.
     */
    public GetAllTermsInner getAllTermsMethod() {
        return getAllTermsMethodWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<GetAllTermsInner> getAllTermsMethodAsync(final ServiceCallback<GetAllTermsInner> serviceCallback) {
        return ServiceFuture.fromResponse(getAllTermsMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GetAllTermsInner object
     */
    public Observable<GetAllTermsInner> getAllTermsMethodAsync() {
        return getAllTermsMethodWithServiceResponseAsync().map(new Func1<ServiceResponse<GetAllTermsInner>, GetAllTermsInner>() {
            @Override
            public GetAllTermsInner call(ServiceResponse<GetAllTermsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GetAllTermsInner object
     */
    public Observable<ServiceResponse<GetAllTermsInner>> getAllTermsMethodWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.language() == null) {
            throw new IllegalArgumentException("Parameter this.client.language() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.getAllTermsMethod(this.client.listId(), this.client.language(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<GetAllTermsInner>>>() {
                @Override
                public Observable<ServiceResponse<GetAllTermsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<GetAllTermsInner> clientResponse = getAllTermsMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<GetAllTermsInner> getAllTermsMethodDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<GetAllTermsInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<GetAllTermsInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteAllTerms() {
        return deleteAllTermsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteAllTermsAsync(final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteAllTermsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteAllTermsAsync() {
        return deleteAllTermsWithServiceResponseAsync().map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteAllTermsWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.language() == null) {
            throw new IllegalArgumentException("Parameter this.client.language() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.deleteAllTerms(this.client.listId(), this.client.language(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<String>>>() {
                @Override
                public Observable<ServiceResponse<String>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<String> clientResponse = deleteAllTermsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<String> deleteAllTermsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
