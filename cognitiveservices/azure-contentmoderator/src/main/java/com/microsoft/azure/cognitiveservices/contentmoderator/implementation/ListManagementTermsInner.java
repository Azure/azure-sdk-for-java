/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import retrofit2.Retrofit;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.cognitiveservices.contentmoderator.APIErrorException;
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
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in ListManagementTerms.
 */
public class ListManagementTermsInner {
    /** The Retrofit service to perform REST calls. */
    private ListManagementTermsService service;
    /** The service client containing this operation class. */
    private ContentModeratorClientImpl client;

    /**
     * Initializes an instance of ListManagementTermsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ListManagementTermsInner(Retrofit retrofit, ContentModeratorClientImpl client) {
        this.service = retrofit.create(ListManagementTermsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ListManagementTerms to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ListManagementTermsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementTerms addTerm" })
        @POST("contentmoderator/lists/v1.0/termlists/{listId}/terms/{term}")
        Observable<Response<ResponseBody>> addTerm(@Path("listId") String listId, @Path("term") String term, @Query("language") String language, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementTerms deleteTerm" })
        @HTTP(path = "contentmoderator/lists/v1.0/termlists/{listId}/terms/{term}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteTerm(@Path("listId") String listId, @Path("term") String term, @Query("language") String language, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementTerms getAllTerms" })
        @GET("contentmoderator/lists/v1.0/termlists/{listId}/terms")
        Observable<Response<ResponseBody>> getAllTerms(@Path("listId") String listId, @Query("language") String language, @Query("offset") Integer offset, @Query("limit") Integer limit, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementTerms deleteAllTerms" })
        @HTTP(path = "contentmoderator/lists/v1.0/termlists/{listId}/terms", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteAllTerms(@Path("listId") String listId, @Query("language") String language, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the Object object if successful.
     */
    public Object addTerm(String listId, String term, String language) {
        return addTermWithServiceResponseAsync(listId, term, language).toBlocking().single().body();
    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Object> addTermAsync(String listId, String term, String language, final ServiceCallback<Object> serviceCallback) {
        return ServiceFuture.fromResponse(addTermWithServiceResponseAsync(listId, term, language), serviceCallback);
    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    public Observable<Object> addTermAsync(String listId, String term, String language) {
        return addTermWithServiceResponseAsync(listId, term, language).map(new Func1<ServiceResponse<Object>, Object>() {
            @Override
            public Object call(ServiceResponse<Object> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a term to the term list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Object object
     */
    public Observable<ServiceResponse<Object>> addTermWithServiceResponseAsync(String listId, String term, String language) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (term == null) {
            throw new IllegalArgumentException("Parameter term is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addTerm(listId, term, language, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<Object> addTermDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Object, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(201, new TypeToken<Object>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteTerm(String listId, String term, String language) {
        return deleteTermWithServiceResponseAsync(listId, term, language).toBlocking().single().body();
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteTermAsync(String listId, String term, String language, final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteTermWithServiceResponseAsync(listId, term, language), serviceCallback);
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteTermAsync(String listId, String term, String language) {
        return deleteTermWithServiceResponseAsync(listId, term, language).map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes a term from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param term Term to be deleted
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteTermWithServiceResponseAsync(String listId, String term, String language) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (term == null) {
            throw new IllegalArgumentException("Parameter term is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.deleteTerm(listId, term, language, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<String> deleteTermDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(204, new TypeToken<String>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TermsInner object if successful.
     */
    public TermsInner getAllTerms(String listId, String language) {
        return getAllTermsWithServiceResponseAsync(listId, language).toBlocking().single().body();
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<TermsInner> getAllTermsAsync(String listId, String language, final ServiceCallback<TermsInner> serviceCallback) {
        return ServiceFuture.fromResponse(getAllTermsWithServiceResponseAsync(listId, language), serviceCallback);
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TermsInner object
     */
    public Observable<TermsInner> getAllTermsAsync(String listId, String language) {
        return getAllTermsWithServiceResponseAsync(listId, language).map(new Func1<ServiceResponse<TermsInner>, TermsInner>() {
            @Override
            public TermsInner call(ServiceResponse<TermsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TermsInner object
     */
    public Observable<ServiceResponse<TermsInner>> getAllTermsWithServiceResponseAsync(String listId, String language) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        final Integer offset = null;
        final Integer limit = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getAllTerms(listId, language, offset, limit, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<TermsInner>>>() {
                @Override
                public Observable<ServiceResponse<TermsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<TermsInner> clientResponse = getAllTermsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @param offset The pagination start index.
     * @param limit The max limit.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TermsInner object if successful.
     */
    public TermsInner getAllTerms(String listId, String language, Integer offset, Integer limit) {
        return getAllTermsWithServiceResponseAsync(listId, language, offset, limit).toBlocking().single().body();
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @param offset The pagination start index.
     * @param limit The max limit.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<TermsInner> getAllTermsAsync(String listId, String language, Integer offset, Integer limit, final ServiceCallback<TermsInner> serviceCallback) {
        return ServiceFuture.fromResponse(getAllTermsWithServiceResponseAsync(listId, language, offset, limit), serviceCallback);
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @param offset The pagination start index.
     * @param limit The max limit.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TermsInner object
     */
    public Observable<TermsInner> getAllTermsAsync(String listId, String language, Integer offset, Integer limit) {
        return getAllTermsWithServiceResponseAsync(listId, language, offset, limit).map(new Func1<ServiceResponse<TermsInner>, TermsInner>() {
            @Override
            public TermsInner call(ServiceResponse<TermsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Gets all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @param offset The pagination start index.
     * @param limit The max limit.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TermsInner object
     */
    public Observable<ServiceResponse<TermsInner>> getAllTermsWithServiceResponseAsync(String listId, String language, Integer offset, Integer limit) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getAllTerms(listId, language, offset, limit, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<TermsInner>>>() {
                @Override
                public Observable<ServiceResponse<TermsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<TermsInner> clientResponse = getAllTermsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<TermsInner> getAllTermsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<TermsInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<TermsInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteAllTerms(String listId, String language) {
        return deleteAllTermsWithServiceResponseAsync(listId, language).toBlocking().single().body();
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteAllTermsAsync(String listId, String language, final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteAllTermsWithServiceResponseAsync(listId, language), serviceCallback);
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteAllTermsAsync(String listId, String language) {
        return deleteAllTermsWithServiceResponseAsync(listId, language).map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes all terms from the list with list Id equal to the list Id passed.
     *
     * @param listId List Id of the image list.
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteAllTermsWithServiceResponseAsync(String listId, String language) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.deleteAllTerms(listId, language, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<String> deleteAllTermsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(204, new TypeToken<String>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
