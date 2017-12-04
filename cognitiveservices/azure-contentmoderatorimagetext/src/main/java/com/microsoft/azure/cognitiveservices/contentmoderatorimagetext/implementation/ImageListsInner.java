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
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
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
 * in ImageLists.
 */
public class ImageListsInner {
    /** The Retrofit service to perform REST calls. */
    private ImageListsService service;
    /** The service client containing this operation class. */
    private ContentModeratorImageTextClientImpl client;

    /**
     * Initializes an instance of ImageListsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ImageListsInner(Retrofit retrofit, ContentModeratorImageTextClientImpl client) {
        this.service = retrofit.create(ImageListsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ImageLists to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ImageListsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists getDetails" })
        @GET("contentmoderator/lists/v1.0/imagelists/{listId}")
        Observable<Response<ResponseBody>> getDetails(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists delete" })
        @HTTP(path = "contentmoderator/lists/v1.0/imagelists/{listId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> delete(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists update" })
        @PUT("contentmoderator/lists/v1.0/imagelists/{listId}")
        Observable<Response<ResponseBody>> update(@Path("listId") String listId, @Header("Content-Type") String contentType, @Body BodyInner body, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists create" })
        @POST("contentmoderator/lists/v1.0/imagelists")
        Observable<Response<ResponseBody>> create(@Header("Content-Type") String contentType, @Body BodyInner body, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists getAllImageLists" })
        @GET("contentmoderator/lists/v1.0/imagelists")
        Observable<Response<ResponseBody>> getAllImageLists(@Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists refreshIndexMethod" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/RefreshIndex")
        Observable<Response<ResponseBody>> refreshIndexMethod(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists addImageMethod" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> addImageMethod(@Path("listId") String listId, @Query("tag") Double tag, @Query("label") String label, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists deleteAllImages" })
        @HTTP(path = "contentmoderator/lists/v1.0/imagelists/{listId}/images", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteAllImages(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists getAllImageIdsMethod" })
        @GET("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> getAllImageIdsMethod(@Path("listId") String listId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists deleteImage" })
        @HTTP(path = "contentmoderator/lists/v1.0/imagelists/{listId}/images/{ImageId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteImage(@Path("listId") String listId, @Path("ImageId") String imageId, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists addImageUrlInput" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> addImageUrlInput(@Path("listId") String listId, @Query("tag") Double tag, @Query("label") String label, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.ImageLists addImageFileInput" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> addImageFileInput(@Path("listId") String listId, @Query("tag") Double tag, @Query("label") String label, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageListGetDetailsInner object if successful.
     */
    public ImageListGetDetailsInner getDetails() {
        return getDetailsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageListGetDetailsInner> getDetailsAsync(final ServiceCallback<ImageListGetDetailsInner> serviceCallback) {
        return ServiceFuture.fromResponse(getDetailsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListGetDetailsInner object
     */
    public Observable<ImageListGetDetailsInner> getDetailsAsync() {
        return getDetailsWithServiceResponseAsync().map(new Func1<ServiceResponse<ImageListGetDetailsInner>, ImageListGetDetailsInner>() {
            @Override
            public ImageListGetDetailsInner call(ServiceResponse<ImageListGetDetailsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the details of the image list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageListGetDetailsInner object
     */
    public Observable<ServiceResponse<ImageListGetDetailsInner>> getDetailsWithServiceResponseAsync() {
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
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageListGetDetailsInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageListGetDetailsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageListGetDetailsInner> clientResponse = getDetailsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageListGetDetailsInner> getDetailsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageListGetDetailsInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageListGetDetailsInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes image list with the list Id equal to list Id passed.
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
     * Deletes image list with the list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteAsync(final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Deletes image list with the list Id equal to list Id passed.
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
     * Deletes image list with the list Id equal to list Id passed.
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
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the UpdateImageListInner object if successful.
     */
    public UpdateImageListInner update(BodyInner body) {
        return updateWithServiceResponseAsync(body).toBlocking().single().body();
    }

    /**
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param body Schema of the body.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<UpdateImageListInner> updateAsync(BodyInner body, final ServiceCallback<UpdateImageListInner> serviceCallback) {
        return ServiceFuture.fromResponse(updateWithServiceResponseAsync(body), serviceCallback);
    }

    /**
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the UpdateImageListInner object
     */
    public Observable<UpdateImageListInner> updateAsync(BodyInner body) {
        return updateWithServiceResponseAsync(body).map(new Func1<ServiceResponse<UpdateImageListInner>, UpdateImageListInner>() {
            @Override
            public UpdateImageListInner call(ServiceResponse<UpdateImageListInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Updates an image list with list Id equal to list Id passed.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the UpdateImageListInner object
     */
    public Observable<ServiceResponse<UpdateImageListInner>> updateWithServiceResponseAsync(BodyInner body) {
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
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<UpdateImageListInner>>>() {
                @Override
                public Observable<ServiceResponse<UpdateImageListInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<UpdateImageListInner> clientResponse = updateDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<UpdateImageListInner> updateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<UpdateImageListInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<UpdateImageListInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates an image list.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the CreateImageListInner object if successful.
     */
    public CreateImageListInner create(BodyInner body) {
        return createWithServiceResponseAsync(body).toBlocking().single().body();
    }

    /**
     * Creates an image list.
     *
     * @param body Schema of the body.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<CreateImageListInner> createAsync(BodyInner body, final ServiceCallback<CreateImageListInner> serviceCallback) {
        return ServiceFuture.fromResponse(createWithServiceResponseAsync(body), serviceCallback);
    }

    /**
     * Creates an image list.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateImageListInner object
     */
    public Observable<CreateImageListInner> createAsync(BodyInner body) {
        return createWithServiceResponseAsync(body).map(new Func1<ServiceResponse<CreateImageListInner>, CreateImageListInner>() {
            @Override
            public CreateImageListInner call(ServiceResponse<CreateImageListInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Creates an image list.
     *
     * @param body Schema of the body.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the CreateImageListInner object
     */
    public Observable<ServiceResponse<CreateImageListInner>> createWithServiceResponseAsync(BodyInner body) {
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
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<CreateImageListInner>>>() {
                @Override
                public Observable<ServiceResponse<CreateImageListInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<CreateImageListInner> clientResponse = createDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<CreateImageListInner> createDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<CreateImageListInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<CreateImageListInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets all the Image Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the List&lt;GetAllImageListItemInner&gt; object if successful.
     */
    public List<GetAllImageListItemInner> getAllImageLists() {
        return getAllImageListsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Gets all the Image Lists.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<GetAllImageListItemInner>> getAllImageListsAsync(final ServiceCallback<List<GetAllImageListItemInner>> serviceCallback) {
        return ServiceFuture.fromResponse(getAllImageListsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Gets all the Image Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;GetAllImageListItemInner&gt; object
     */
    public Observable<List<GetAllImageListItemInner>> getAllImageListsAsync() {
        return getAllImageListsWithServiceResponseAsync().map(new Func1<ServiceResponse<List<GetAllImageListItemInner>>, List<GetAllImageListItemInner>>() {
            @Override
            public List<GetAllImageListItemInner> call(ServiceResponse<List<GetAllImageListItemInner>> response) {
                return response.body();
            }
        });
    }

    /**
     * Gets all the Image Lists.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the List&lt;GetAllImageListItemInner&gt; object
     */
    public Observable<ServiceResponse<List<GetAllImageListItemInner>>> getAllImageListsWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.getAllImageLists(this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<List<GetAllImageListItemInner>>>>() {
                @Override
                public Observable<ServiceResponse<List<GetAllImageListItemInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<List<GetAllImageListItemInner>> clientResponse = getAllImageListsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<List<GetAllImageListItemInner>> getAllImageListsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<List<GetAllImageListItemInner>, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<List<GetAllImageListItemInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Refreshes the index of the list with list Id equal to list Id passed.
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
     * Refreshes the index of the list with list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<RefreshIndexInner> refreshIndexMethodAsync(final ServiceCallback<RefreshIndexInner> serviceCallback) {
        return ServiceFuture.fromResponse(refreshIndexMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Refreshes the index of the list with list Id equal to list Id passed.
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
     * Refreshes the index of the list with list Id equal to list Id passed.
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
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the AddImageInner object if successful.
     */
    public AddImageInner addImageMethod() {
        return addImageMethodWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<AddImageInner> addImageMethodAsync(final ServiceCallback<AddImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AddImageInner object
     */
    public Observable<AddImageInner> addImageMethodAsync() {
        return addImageMethodWithServiceResponseAsync().map(new Func1<ServiceResponse<AddImageInner>, AddImageInner>() {
            @Override
            public AddImageInner call(ServiceResponse<AddImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AddImageInner object
     */
    public Observable<ServiceResponse<AddImageInner>> addImageMethodWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.addImageMethod(this.client.listId(), this.client.tag(), this.client.label(), this.client.ocpApimSubscriptionKey(), this.client.contentType(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<AddImageInner>>>() {
                @Override
                public Observable<ServiceResponse<AddImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<AddImageInner> clientResponse = addImageMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<AddImageInner> addImageMethodDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<AddImageInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<AddImageInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteAllImages() {
        return deleteAllImagesWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteAllImagesAsync(final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteAllImagesWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteAllImagesAsync() {
        return deleteAllImagesWithServiceResponseAsync().map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteAllImagesWithServiceResponseAsync() {
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
        return service.deleteAllImages(this.client.listId(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<String>>>() {
                @Override
                public Observable<ServiceResponse<String>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<String> clientResponse = deleteAllImagesDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<String> deleteAllImagesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the GetAllImageIdsInner object if successful.
     */
    public GetAllImageIdsInner getAllImageIdsMethod() {
        return getAllImageIdsMethodWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<GetAllImageIdsInner> getAllImageIdsMethodAsync(final ServiceCallback<GetAllImageIdsInner> serviceCallback) {
        return ServiceFuture.fromResponse(getAllImageIdsMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GetAllImageIdsInner object
     */
    public Observable<GetAllImageIdsInner> getAllImageIdsMethodAsync() {
        return getAllImageIdsMethodWithServiceResponseAsync().map(new Func1<ServiceResponse<GetAllImageIdsInner>, GetAllImageIdsInner>() {
            @Override
            public GetAllImageIdsInner call(ServiceResponse<GetAllImageIdsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the GetAllImageIdsInner object
     */
    public Observable<ServiceResponse<GetAllImageIdsInner>> getAllImageIdsMethodWithServiceResponseAsync() {
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
        return service.getAllImageIdsMethod(this.client.listId(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<GetAllImageIdsInner>>>() {
                @Override
                public Observable<ServiceResponse<GetAllImageIdsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<GetAllImageIdsInner> clientResponse = getAllImageIdsMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<GetAllImageIdsInner> getAllImageIdsMethodDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<GetAllImageIdsInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<GetAllImageIdsInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteImage() {
        return deleteImageWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteImageAsync(final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteImageWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteImageAsync() {
        return deleteImageWithServiceResponseAsync().map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteImageWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.imageId() == null) {
            throw new IllegalArgumentException("Parameter this.client.imageId() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.deleteImage(this.client.listId(), this.client.imageId(), this.client.ocpApimSubscriptionKey(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<String>>>() {
                @Override
                public Observable<ServiceResponse<String>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<String> clientResponse = deleteImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<String> deleteImageDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the AddImageInner object if successful.
     */
    public AddImageInner addImageUrlInput(BodyModelInner imageUrl) {
        return addImageUrlInputWithServiceResponseAsync(imageUrl).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<AddImageInner> addImageUrlInputAsync(BodyModelInner imageUrl, final ServiceCallback<AddImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageUrlInputWithServiceResponseAsync(imageUrl), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AddImageInner object
     */
    public Observable<AddImageInner> addImageUrlInputAsync(BodyModelInner imageUrl) {
        return addImageUrlInputWithServiceResponseAsync(imageUrl).map(new Func1<ServiceResponse<AddImageInner>, AddImageInner>() {
            @Override
            public AddImageInner call(ServiceResponse<AddImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AddImageInner object
     */
    public Observable<ServiceResponse<AddImageInner>> addImageUrlInputWithServiceResponseAsync(BodyModelInner imageUrl) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.addImageUrlInput(this.client.listId(), this.client.tag(), this.client.label(), this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<AddImageInner>>>() {
                @Override
                public Observable<ServiceResponse<AddImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<AddImageInner> clientResponse = addImageUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<AddImageInner> addImageUrlInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<AddImageInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<AddImageInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the AddImageInner object if successful.
     */
    public AddImageInner addImageFileInput(byte[] imageStream) {
        return addImageFileInputWithServiceResponseAsync(imageStream).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageStream The image file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<AddImageInner> addImageFileInputAsync(byte[] imageStream, final ServiceCallback<AddImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageFileInputWithServiceResponseAsync(imageStream), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AddImageInner object
     */
    public Observable<AddImageInner> addImageFileInputAsync(byte[] imageStream) {
        return addImageFileInputWithServiceResponseAsync(imageStream).map(new Func1<ServiceResponse<AddImageInner>, AddImageInner>() {
            @Override
            public AddImageInner call(ServiceResponse<AddImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the AddImageInner object
     */
    public Observable<ServiceResponse<AddImageInner>> addImageFileInputWithServiceResponseAsync(byte[] imageStream) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.listId() == null) {
            throw new IllegalArgumentException("Parameter this.client.listId() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.addImageFileInput(this.client.listId(), this.client.tag(), this.client.label(), this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<AddImageInner>>>() {
                @Override
                public Observable<ServiceResponse<AddImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<AddImageInner> clientResponse = addImageFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<AddImageInner> addImageFileInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<AddImageInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<AddImageInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
