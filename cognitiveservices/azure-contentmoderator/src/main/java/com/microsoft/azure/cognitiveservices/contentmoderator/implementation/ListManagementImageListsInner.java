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
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in ListManagementImageLists.
 */
public class ListManagementImageListsInner {
    /** The Retrofit service to perform REST calls. */
    private ListManagementImageListsService service;
    /** The service client containing this operation class. */
    private ContentModeratorClientImpl client;

    /**
     * Initializes an instance of ListManagementImageListsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ListManagementImageListsInner(Retrofit retrofit, ContentModeratorClientImpl client) {
        this.service = retrofit.create(ListManagementImageListsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ListManagementImageLists to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ListManagementImageListsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImageLists getDetails" })
        @GET("contentmoderator/lists/v1.0/imagelists/{listId}")
        Observable<Response<ResponseBody>> getDetails(@Path("listId") String listId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImageLists delete" })
        @HTTP(path = "contentmoderator/lists/v1.0/imagelists/{listId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> delete(@Path("listId") String listId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImageLists update" })
        @PUT("contentmoderator/lists/v1.0/imagelists/{listId}")
        Observable<Response<ResponseBody>> update(@Path("listId") String listId, @Header("Content-Type") String contentType, @Body BodyInner body, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImageLists create" })
        @POST("contentmoderator/lists/v1.0/imagelists")
        Observable<Response<ResponseBody>> create(@Header("Content-Type") String contentType, @Body BodyInner body, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImageLists getAllImageLists" })
        @GET("contentmoderator/lists/v1.0/imagelists")
        Observable<Response<ResponseBody>> getAllImageLists(@Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImageLists refreshIndexMethod" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/RefreshIndex")
        Observable<Response<ResponseBody>> refreshIndexMethod(@Path("listId") String listId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageListInner object if successful.
     */
    public ImageListInner getDetails(String listId) {
        return getDetailsWithServiceResponseAsync(listId).toBlocking().single().body();
    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageListInner> getDetailsAsync(String listId, final ServiceCallback<ImageListInner> serviceCallback) {
        return ServiceFuture.fromResponse(getDetailsWithServiceResponseAsync(listId), serviceCallback);
    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListInner object
     */
    public Observable<ImageListInner> getDetailsAsync(String listId) {
        return getDetailsWithServiceResponseAsync(listId).map(new Func1<ServiceResponse<ImageListInner>, ImageListInner>() {
            @Override
            public ImageListInner call(ServiceResponse<ImageListInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListInner object
     */
    public Observable<ServiceResponse<ImageListInner>> getDetailsWithServiceResponseAsync(String listId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getDetails(listId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageListInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageListInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageListInner> clientResponse = getDetailsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageListInner> getDetailsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageListInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageListInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Deletes image list with the list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String delete(String listId) {
        return deleteWithServiceResponseAsync(listId).toBlocking().single().body();
    }

    /**
     * Deletes image list with the list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteAsync(String listId, final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteWithServiceResponseAsync(listId), serviceCallback);
    }

    /**
     * Deletes image list with the list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteAsync(String listId) {
        return deleteWithServiceResponseAsync(listId).map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes image list with the list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteWithServiceResponseAsync(String listId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.delete(listId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<String> deleteDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageListInner object if successful.
     */
    public ImageListInner update(String listId, String contentType, BodyInner body) {
        return updateWithServiceResponseAsync(listId, contentType, body).toBlocking().single().body();
    }

    /**
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param body Schema of the body.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageListInner> updateAsync(String listId, String contentType, BodyInner body, final ServiceCallback<ImageListInner> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(listId, contentType, body), serviceCallback);
    }

    /**
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListInner object
     */
    public Observable<ImageListInner> updateAsync(String listId, String contentType, BodyInner body) {
        return updateWithServiceResponseAsync(listId, contentType, body).map(new Func1<ServiceResponse<ImageListInner>, ImageListInner>() {
            @Override
            public ImageListInner call(ServiceResponse<ImageListInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListInner object
     */
    public Observable<ServiceResponse<ImageListInner>> updateWithServiceResponseAsync(String listId, String contentType, BodyInner body) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (body == null) {
            throw new IllegalArgumentException("Parameter body is required and cannot be null.");
        }
        Validator.validate(body);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.update(listId, contentType, body, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageListInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageListInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageListInner> clientResponse = updateDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageListInner> updateDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageListInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageListInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Creates an image list.
     *
     * @param contentType The content type.
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageListInner object if successful.
     */
    public ImageListInner create(String contentType, BodyInner body) {
        return createWithServiceResponseAsync(contentType, body).toBlocking().single().body();
    }

    /**
     * Creates an image list.
     *
     * @param contentType The content type.
     * @param body Schema of the body.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageListInner> createAsync(String contentType, BodyInner body, final ServiceCallback<ImageListInner> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(contentType, body), serviceCallback);
    }

    /**
     * Creates an image list.
     *
     * @param contentType The content type.
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListInner object
     */
    public Observable<ImageListInner> createAsync(String contentType, BodyInner body) {
        return createWithServiceResponseAsync(contentType, body).map(new Func1<ServiceResponse<ImageListInner>, ImageListInner>() {
            @Override
            public ImageListInner call(ServiceResponse<ImageListInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Creates an image list.
     *
     * @param contentType The content type.
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListInner object
     */
    public Observable<ServiceResponse<ImageListInner>> createWithServiceResponseAsync(String contentType, BodyInner body) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (body == null) {
            throw new IllegalArgumentException("Parameter body is required and cannot be null.");
        }
        Validator.validate(body);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.create(contentType, body, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageListInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageListInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageListInner> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageListInner> createDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageListInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageListInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Gets all the Image Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;ImageListInner&gt; object if successful.
     */
    public List<ImageListInner> getAllImageLists() {
        return getAllImageListsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Gets all the Image Lists.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<ImageListInner>> getAllImageListsAsync(final ServiceCallback<List<ImageListInner>> serviceCallback) {
        return ServiceFuture.fromResponse(getAllImageListsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Gets all the Image Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;ImageListInner&gt; object
     */
    public Observable<List<ImageListInner>> getAllImageListsAsync() {
        return getAllImageListsWithServiceResponseAsync().map(new Func1<ServiceResponse<List<ImageListInner>>, List<ImageListInner>>() {
            @Override
            public List<ImageListInner> call(ServiceResponse<List<ImageListInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Gets all the Image Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;ImageListInner&gt; object
     */
    public Observable<ServiceResponse<List<ImageListInner>>> getAllImageListsWithServiceResponseAsync() {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getAllImageLists(this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<ImageListInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<ImageListInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<ImageListInner>> clientResponse = getAllImageListsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<ImageListInner>> getAllImageListsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<ImageListInner>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<ImageListInner>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Refreshes the index of the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the RefreshIndexInner object if successful.
     */
    public RefreshIndexInner refreshIndexMethod(String listId) {
        return refreshIndexMethodWithServiceResponseAsync(listId).toBlocking().single().body();
    }

    /**
     * Refreshes the index of the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<RefreshIndexInner> refreshIndexMethodAsync(String listId, final ServiceCallback<RefreshIndexInner> serviceCallback) {
        return ServiceFuture.fromResponse(refreshIndexMethodWithServiceResponseAsync(listId), serviceCallback);
    }

    /**
     * Refreshes the index of the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the RefreshIndexInner object
     */
    public Observable<RefreshIndexInner> refreshIndexMethodAsync(String listId) {
        return refreshIndexMethodWithServiceResponseAsync(listId).map(new Func1<ServiceResponse<RefreshIndexInner>, RefreshIndexInner>() {
            @Override
            public RefreshIndexInner call(ServiceResponse<RefreshIndexInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Refreshes the index of the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the RefreshIndexInner object
     */
    public Observable<ServiceResponse<RefreshIndexInner>> refreshIndexMethodWithServiceResponseAsync(String listId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.refreshIndexMethod(listId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<RefreshIndexInner> refreshIndexMethodDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<RefreshIndexInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<RefreshIndexInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
