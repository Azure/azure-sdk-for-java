/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import retrofit2.Retrofit;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.cognitiveservices.faceapi.APIErrorException;
import com.microsoft.azure.cognitiveservices.faceapi.AzureRegions;
import com.microsoft.azure.cognitiveservices.faceapi.CreateFaceListRequest;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in FaceLists.
 */
public class FaceListsInner {
    /** The Retrofit service to perform REST calls. */
    private FaceListsService service;
    /** The service client containing this operation class. */
    private FaceAPIImpl client;

    /**
     * Initializes an instance of FaceListsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public FaceListsInner(Retrofit retrofit, FaceAPIImpl client) {
        this.service = retrofit.create(FaceListsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for FaceLists to be
     * used by Retrofit to perform actually REST calls.
     */
    interface FaceListsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists create" })
        @PUT("facelists/{faceListId}")
        Observable<Response<ResponseBody>> create(@Path("faceListId") String faceListId, @Header("accept-language") String acceptLanguage, @Body CreateFaceListRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists get" })
        @GET("facelists/{faceListId}")
        Observable<Response<ResponseBody>> get(@Path("faceListId") String faceListId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists update" })
        @PATCH("facelists/{faceListId}")
        Observable<Response<ResponseBody>> update(@Path("faceListId") String faceListId, @Header("accept-language") String acceptLanguage, @Body CreateFaceListRequest body, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists delete" })
        @HTTP(path = "facelists/{faceListId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> delete(@Path("faceListId") String faceListId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists list" })
        @GET("facelists")
        Observable<Response<ResponseBody>> list(@Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists deleteFace" })
        @HTTP(path = "facelists/{faceListId}/persistedFaces/{persistedFaceId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteFace(@Path("faceListId") String faceListId, @Path("persistedFaceId") String persistedFaceId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists addFace" })
        @POST("facelists/{faceListId}/persistedFaces")
        Observable<Response<ResponseBody>> addFace(@Path("faceListId") String faceListId, @Query("userData") String userData, @Query("targetFace") String targetFace, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.faceapi.FaceLists addFaceFromStream" })
        @POST("facelists/{faceListId}/persistedFaces")
        Observable<Response<ResponseBody>> addFaceFromStream(@Path("faceListId") String faceListId, @Query("userData") String userData, @Query("targetFace") String targetFace, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void create(String faceListId) {
        createWithServiceResponseAsync(faceListId).toBlocking().single().body();
    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> createAsync(String faceListId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(faceListId), serviceCallback);
    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> createAsync(String faceListId) {
        return createWithServiceResponseAsync(faceListId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> createWithServiceResponseAsync(String faceListId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        final String name = null;
        final String userData = null;
        CreateFaceListRequest body = new CreateFaceListRequest();
        body.withName(null);
        body.withUserData(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.create(faceListId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void create(String faceListId, String name, String userData) {
        createWithServiceResponseAsync(faceListId, name, userData).toBlocking().single().body();
    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> createAsync(String faceListId, String name, String userData, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(faceListId, name, userData), serviceCallback);
    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> createAsync(String faceListId, String name, String userData) {
        return createWithServiceResponseAsync(faceListId, name, userData).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Create an empty face list. Up to 64 face lists are allowed to exist in one subscription.
     *
     * @param faceListId Id referencing a particular face list.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> createWithServiceResponseAsync(String faceListId, String name, String userData) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        CreateFaceListRequest body = new CreateFaceListRequest();
        body.withName(name);
        body.withUserData(userData);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.create(faceListId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> createDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Retrieve a face list's information.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the GetFaceListResultInner object if successful.
     */
    public GetFaceListResultInner get(String faceListId) {
        return getWithServiceResponseAsync(faceListId).toBlocking().single().body();
    }

    /**
     * Retrieve a face list's information.
     *
     * @param faceListId Id referencing a Face List.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<GetFaceListResultInner> getAsync(String faceListId, final ServiceCallback<GetFaceListResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(getWithServiceResponseAsync(faceListId), serviceCallback);
    }

    /**
     * Retrieve a face list's information.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GetFaceListResultInner object
     */
    public Observable<GetFaceListResultInner> getAsync(String faceListId) {
        return getWithServiceResponseAsync(faceListId).map(new Func1<ServiceResponse<GetFaceListResultInner>, GetFaceListResultInner>() {
            @Override
            public GetFaceListResultInner call(ServiceResponse<GetFaceListResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Retrieve a face list's information.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GetFaceListResultInner object
     */
    public Observable<ServiceResponse<GetFaceListResultInner>> getWithServiceResponseAsync(String faceListId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.get(faceListId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<GetFaceListResultInner>>>() {
                @Override
                public Observable<ServiceResponse<GetFaceListResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<GetFaceListResultInner> clientResponse = getDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<GetFaceListResultInner> getDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<GetFaceListResultInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<GetFaceListResultInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void update(String faceListId) {
        updateWithServiceResponseAsync(faceListId).toBlocking().single().body();
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateAsync(String faceListId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(faceListId), serviceCallback);
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateAsync(String faceListId) {
        return updateWithServiceResponseAsync(faceListId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String faceListId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        final String name = null;
        final String userData = null;
        CreateFaceListRequest body = new CreateFaceListRequest();
        body.withName(null);
        body.withUserData(null);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.update(faceListId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = updateDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void update(String faceListId, String name, String userData) {
        updateWithServiceResponseAsync(faceListId, name, userData).toBlocking().single().body();
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> updateAsync(String faceListId, String name, String userData, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(faceListId, name, userData), serviceCallback);
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> updateAsync(String faceListId, String name, String userData) {
        return updateWithServiceResponseAsync(faceListId, name, userData).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Update information of a face list.
     *
     * @param faceListId Id referencing a Face List.
     * @param name Name of the face list, maximum length is 128.
     * @param userData Optional user defined data for the face list. Length should not exceed 16KB.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> updateWithServiceResponseAsync(String faceListId, String name, String userData) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        CreateFaceListRequest body = new CreateFaceListRequest();
        body.withName(name);
        body.withUserData(userData);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.update(faceListId, this.client.acceptLanguage(), body, parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = updateDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> updateDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Delete an existing face list according to faceListId. Persisted face images in the face list will also be deleted.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void delete(String faceListId) {
        deleteWithServiceResponseAsync(faceListId).toBlocking().single().body();
    }

    /**
     * Delete an existing face list according to faceListId. Persisted face images in the face list will also be deleted.
     *
     * @param faceListId Id referencing a Face List.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> deleteAsync(String faceListId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(deleteWithServiceResponseAsync(faceListId), serviceCallback);
    }

    /**
     * Delete an existing face list according to faceListId. Persisted face images in the face list will also be deleted.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> deleteAsync(String faceListId) {
        return deleteWithServiceResponseAsync(faceListId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Delete an existing face list according to faceListId. Persisted face images in the face list will also be deleted.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> deleteWithServiceResponseAsync(String faceListId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.delete(faceListId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = deleteDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> deleteDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Retrieve information about all existing face lists. Only faceListId, name and userData will be returned.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;GetFaceListResultInner&gt; object if successful.
     */
    public List<GetFaceListResultInner> list() {
        return listWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Retrieve information about all existing face lists. Only faceListId, name and userData will be returned.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<GetFaceListResultInner>> listAsync(final ServiceCallback<List<GetFaceListResultInner>> serviceCallback) {
        return ServiceFuture.fromResponse(listWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Retrieve information about all existing face lists. Only faceListId, name and userData will be returned.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;GetFaceListResultInner&gt; object
     */
    public Observable<List<GetFaceListResultInner>> listAsync() {
        return listWithServiceResponseAsync().map(new Func1<ServiceResponse<List<GetFaceListResultInner>>, List<GetFaceListResultInner>>() {
            @Override
            public List<GetFaceListResultInner> call(ServiceResponse<List<GetFaceListResultInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Retrieve information about all existing face lists. Only faceListId, name and userData will be returned.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;GetFaceListResultInner&gt; object
     */
    public Observable<ServiceResponse<List<GetFaceListResultInner>>> listWithServiceResponseAsync() {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.list(this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<GetFaceListResultInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<GetFaceListResultInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<GetFaceListResultInner>> clientResponse = listDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<GetFaceListResultInner>> listDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<GetFaceListResultInner>, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<GetFaceListResultInner>>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Delete an existing face from a face list (given by a persisitedFaceId and a faceListId). Persisted image related to the face will also be deleted.
     *
     * @param faceListId faceListId of an existing face list.
     * @param persistedFaceId persistedFaceId of an existing face.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void deleteFace(String faceListId, String persistedFaceId) {
        deleteFaceWithServiceResponseAsync(faceListId, persistedFaceId).toBlocking().single().body();
    }

    /**
     * Delete an existing face from a face list (given by a persisitedFaceId and a faceListId). Persisted image related to the face will also be deleted.
     *
     * @param faceListId faceListId of an existing face list.
     * @param persistedFaceId persistedFaceId of an existing face.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> deleteFaceAsync(String faceListId, String persistedFaceId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(deleteFaceWithServiceResponseAsync(faceListId, persistedFaceId), serviceCallback);
    }

    /**
     * Delete an existing face from a face list (given by a persisitedFaceId and a faceListId). Persisted image related to the face will also be deleted.
     *
     * @param faceListId faceListId of an existing face list.
     * @param persistedFaceId persistedFaceId of an existing face.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> deleteFaceAsync(String faceListId, String persistedFaceId) {
        return deleteFaceWithServiceResponseAsync(faceListId, persistedFaceId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Delete an existing face from a face list (given by a persisitedFaceId and a faceListId). Persisted image related to the face will also be deleted.
     *
     * @param faceListId faceListId of an existing face list.
     * @param persistedFaceId persistedFaceId of an existing face.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> deleteFaceWithServiceResponseAsync(String faceListId, String persistedFaceId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        if (persistedFaceId == null) {
            throw new IllegalArgumentException("Parameter persistedFaceId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.deleteFace(faceListId, persistedFaceId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = deleteFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> deleteFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFace(String faceListId) {
        addFaceWithServiceResponseAsync(faceListId).toBlocking().single().body();
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceAsync(String faceListId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceWithServiceResponseAsync(faceListId), serviceCallback);
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceAsync(String faceListId) {
        return addFaceWithServiceResponseAsync(faceListId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceWithServiceResponseAsync(String faceListId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        final String userData = null;
        final String targetFace = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.addFace(faceListId, userData, targetFace, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFace(String faceListId, String userData, String targetFace) {
        addFaceWithServiceResponseAsync(faceListId, userData, targetFace).toBlocking().single().body();
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceAsync(String faceListId, String userData, String targetFace, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceWithServiceResponseAsync(faceListId, userData, targetFace), serviceCallback);
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceAsync(String faceListId, String userData, String targetFace) {
        return addFaceWithServiceResponseAsync(faceListId, userData, targetFace).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceWithServiceResponseAsync(String faceListId, String userData, String targetFace) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.addFace(faceListId, userData, targetFace, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addFaceDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFaceFromStream(String faceListId) {
        addFaceFromStreamWithServiceResponseAsync(faceListId).toBlocking().single().body();
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceFromStreamAsync(String faceListId, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceFromStreamWithServiceResponseAsync(faceListId), serviceCallback);
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceFromStreamAsync(String faceListId) {
        return addFaceFromStreamWithServiceResponseAsync(faceListId).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceFromStreamWithServiceResponseAsync(String faceListId) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        final String userData = null;
        final String targetFace = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.addFaceFromStream(faceListId, userData, targetFace, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceFromStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void addFaceFromStream(String faceListId, String userData, String targetFace) {
        addFaceFromStreamWithServiceResponseAsync(faceListId, userData, targetFace).toBlocking().single().body();
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> addFaceFromStreamAsync(String faceListId, String userData, String targetFace, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromResponse(addFaceFromStreamWithServiceResponseAsync(faceListId, userData, targetFace), serviceCallback);
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<Void> addFaceFromStreamAsync(String faceListId, String userData, String targetFace) {
        return addFaceFromStreamWithServiceResponseAsync(faceListId, userData, targetFace).map(new Func1<ServiceResponse<Void>, Void>() {
            @Override
            public Void call(ServiceResponse<Void> response) {
                return response.body();
            }
        });
    }

    /**
     * Add a face to a face list. The input face is specified as an image with a targetFace rectangle. It returns a persistedFaceId representing the added face, and persistedFaceId will not expire.
     *
     * @param faceListId Id referencing a Face List.
     * @param userData User-specified data about the face list for any purpose. The  maximum length is 1KB.
     * @param targetFace A face rectangle to specify the target face to be added into the face list, in the format of "targetFace=left,top,width,height". E.g. "targetFace=10,10,100,100". If there is more than one face in the image, targetFace is required to specify which face to add. No targetFace means there is only one face detected in the entire image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponse} object if successful.
     */
    public Observable<ServiceResponse<Void>> addFaceFromStreamWithServiceResponseAsync(String faceListId, String userData, String targetFace) {
        if (this.client.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion() is required and cannot be null.");
        }
        if (faceListId == null) {
            throw new IllegalArgumentException("Parameter faceListId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.client.azureRegion());
        return service.addFaceFromStream(faceListId, userData, targetFace, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Void>>>() {
                @Override
                public Observable<ServiceResponse<Void>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<Void> clientResponse = addFaceFromStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<Void> addFaceFromStreamDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<Void, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
