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
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in ListManagementImages.
 */
public class ListManagementImagesInner {
    /** The Retrofit service to perform REST calls. */
    private ListManagementImagesService service;
    /** The service client containing this operation class. */
    private ContentModeratorClientImpl client;

    /**
     * Initializes an instance of ListManagementImagesInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ListManagementImagesInner(Retrofit retrofit, ContentModeratorClientImpl client) {
        this.service = retrofit.create(ListManagementImagesService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ListManagementImages to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ListManagementImagesService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImages addImage" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> addImage(@Path("listId") String listId, @Query("tag") Integer tag, @Query("label") String label, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImages deleteAllImages" })
        @HTTP(path = "contentmoderator/lists/v1.0/imagelists/{listId}/images", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteAllImages(@Path("listId") String listId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImages getAllImageIds" })
        @GET("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> getAllImageIds(@Path("listId") String listId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImages deleteImage" })
        @HTTP(path = "contentmoderator/lists/v1.0/imagelists/{listId}/images/{ImageId}", method = "DELETE", hasBody = true)
        Observable<Response<ResponseBody>> deleteImage(@Path("listId") String listId, @Path("ImageId") String imageId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImages addImageUrlInput" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> addImageUrlInput(@Path("listId") String listId, @Query("tag") Integer tag, @Query("label") String label, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ListManagementImages addImageFileInput" })
        @POST("contentmoderator/lists/v1.0/imagelists/{listId}/images")
        Observable<Response<ResponseBody>> addImageFileInput(@Path("listId") String listId, @Query("tag") Integer tag, @Query("label") String label, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageInner object if successful.
     */
    public ImageInner addImage(String listId) {
        return addImageWithServiceResponseAsync(listId).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageInner> addImageAsync(String listId, final ServiceCallback<ImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageWithServiceResponseAsync(listId), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ImageInner> addImageAsync(String listId) {
        return addImageWithServiceResponseAsync(listId).map(new Func1<ServiceResponse<ImageInner>, ImageInner>() {
            @Override
            public ImageInner call(ServiceResponse<ImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ServiceResponse<ImageInner>> addImageWithServiceResponseAsync(String listId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        final Integer tag = null;
        final String label = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addImage(listId, tag, label, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageInner> clientResponse = addImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageInner object if successful.
     */
    public ImageInner addImage(String listId, Integer tag, String label) {
        return addImageWithServiceResponseAsync(listId, tag, label).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param tag Tag for the image.
     * @param label The image label.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageInner> addImageAsync(String listId, Integer tag, String label, final ServiceCallback<ImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageWithServiceResponseAsync(listId, tag, label), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ImageInner> addImageAsync(String listId, Integer tag, String label) {
        return addImageWithServiceResponseAsync(listId, tag, label).map(new Func1<ServiceResponse<ImageInner>, ImageInner>() {
            @Override
            public ImageInner call(ServiceResponse<ImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ServiceResponse<ImageInner>> addImageWithServiceResponseAsync(String listId, Integer tag, String label) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addImage(listId, tag, label, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageInner> clientResponse = addImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageInner> addImageDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteAllImages(String listId) {
        return deleteAllImagesWithServiceResponseAsync(listId).toBlocking().single().body();
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteAllImagesAsync(String listId, final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteAllImagesWithServiceResponseAsync(listId), serviceCallback);
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteAllImagesAsync(String listId) {
        return deleteAllImagesWithServiceResponseAsync(listId).map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes all images from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteAllImagesWithServiceResponseAsync(String listId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.deleteAllImages(listId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<String> deleteAllImagesDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageIdsInner object if successful.
     */
    public ImageIdsInner getAllImageIds(String listId) {
        return getAllImageIdsWithServiceResponseAsync(listId).toBlocking().single().body();
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageIdsInner> getAllImageIdsAsync(String listId, final ServiceCallback<ImageIdsInner> serviceCallback) {
        return ServiceFuture.fromResponse(getAllImageIdsWithServiceResponseAsync(listId), serviceCallback);
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageIdsInner object
     */
    public Observable<ImageIdsInner> getAllImageIdsAsync(String listId) {
        return getAllImageIdsWithServiceResponseAsync(listId).map(new Func1<ServiceResponse<ImageIdsInner>, ImageIdsInner>() {
            @Override
            public ImageIdsInner call(ServiceResponse<ImageIdsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Gets all image Ids from the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageIdsInner object
     */
    public Observable<ServiceResponse<ImageIdsInner>> getAllImageIdsWithServiceResponseAsync(String listId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.getAllImageIds(listId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageIdsInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageIdsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageIdsInner> clientResponse = getAllImageIdsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageIdsInner> getAllImageIdsDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageIdsInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageIdsInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageId Id of the image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the String object if successful.
     */
    public String deleteImage(String listId, String imageId) {
        return deleteImageWithServiceResponseAsync(listId, imageId).toBlocking().single().body();
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageId Id of the image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<String> deleteImageAsync(String listId, String imageId, final ServiceCallback<String> serviceCallback) {
        return ServiceFuture.fromResponse(deleteImageWithServiceResponseAsync(listId, imageId), serviceCallback);
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageId Id of the image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<String> deleteImageAsync(String listId, String imageId) {
        return deleteImageWithServiceResponseAsync(listId, imageId).map(new Func1<ServiceResponse<String>, String>() {
            @Override
            public String call(ServiceResponse<String> response) {
                return response.body();
            }
        });
    }

    /**
     * Deletes an image from the list with list Id and image Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageId Id of the image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the String object
     */
    public Observable<ServiceResponse<String>> deleteImageWithServiceResponseAsync(String listId, String imageId) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (imageId == null) {
            throw new IllegalArgumentException("Parameter imageId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.deleteImage(listId, imageId, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<String> deleteImageDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<String, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<String>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageInner object if successful.
     */
    public ImageInner addImageUrlInput(String listId, String contentType, BodyModelInner imageUrl) {
        return addImageUrlInputWithServiceResponseAsync(listId, contentType, imageUrl).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageInner> addImageUrlInputAsync(String listId, String contentType, BodyModelInner imageUrl, final ServiceCallback<ImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageUrlInputWithServiceResponseAsync(listId, contentType, imageUrl), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ImageInner> addImageUrlInputAsync(String listId, String contentType, BodyModelInner imageUrl) {
        return addImageUrlInputWithServiceResponseAsync(listId, contentType, imageUrl).map(new Func1<ServiceResponse<ImageInner>, ImageInner>() {
            @Override
            public ImageInner call(ServiceResponse<ImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ServiceResponse<ImageInner>> addImageUrlInputWithServiceResponseAsync(String listId, String contentType, BodyModelInner imageUrl) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        final Integer tag = null;
        final String label = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addImageUrlInput(listId, tag, label, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageInner> clientResponse = addImageUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageInner object if successful.
     */
    public ImageInner addImageUrlInput(String listId, String contentType, BodyModelInner imageUrl, Integer tag, String label) {
        return addImageUrlInputWithServiceResponseAsync(listId, contentType, imageUrl, tag, label).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param tag Tag for the image.
     * @param label The image label.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageInner> addImageUrlInputAsync(String listId, String contentType, BodyModelInner imageUrl, Integer tag, String label, final ServiceCallback<ImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageUrlInputWithServiceResponseAsync(listId, contentType, imageUrl, tag, label), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ImageInner> addImageUrlInputAsync(String listId, String contentType, BodyModelInner imageUrl, Integer tag, String label) {
        return addImageUrlInputWithServiceResponseAsync(listId, contentType, imageUrl, tag, label).map(new Func1<ServiceResponse<ImageInner>, ImageInner>() {
            @Override
            public ImageInner call(ServiceResponse<ImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ServiceResponse<ImageInner>> addImageUrlInputWithServiceResponseAsync(String listId, String contentType, BodyModelInner imageUrl, Integer tag, String label) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.addImageUrlInput(listId, tag, label, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageInner> clientResponse = addImageUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageInner> addImageUrlInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageInner object if successful.
     */
    public ImageInner addImageFileInput(String listId, byte[] imageStream) {
        return addImageFileInputWithServiceResponseAsync(listId, imageStream).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageInner> addImageFileInputAsync(String listId, byte[] imageStream, final ServiceCallback<ImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageFileInputWithServiceResponseAsync(listId, imageStream), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ImageInner> addImageFileInputAsync(String listId, byte[] imageStream) {
        return addImageFileInputWithServiceResponseAsync(listId, imageStream).map(new Func1<ServiceResponse<ImageInner>, ImageInner>() {
            @Override
            public ImageInner call(ServiceResponse<ImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ServiceResponse<ImageInner>> addImageFileInputWithServiceResponseAsync(String listId, byte[] imageStream) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        final Integer tag = null;
        final String label = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.addImageFileInput(listId, tag, label, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageInner> clientResponse = addImageFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageInner object if successful.
     */
    public ImageInner addImageFileInput(String listId, byte[] imageStream, Integer tag, String label) {
        return addImageFileInputWithServiceResponseAsync(listId, imageStream, tag, label).toBlocking().single().body();
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @param tag Tag for the image.
     * @param label The image label.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageInner> addImageFileInputAsync(String listId, byte[] imageStream, Integer tag, String label, final ServiceCallback<ImageInner> serviceCallback) {
        return ServiceFuture.fromResponse(addImageFileInputWithServiceResponseAsync(listId, imageStream, tag, label), serviceCallback);
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ImageInner> addImageFileInputAsync(String listId, byte[] imageStream, Integer tag, String label) {
        return addImageFileInputWithServiceResponseAsync(listId, imageStream, tag, label).map(new Func1<ServiceResponse<ImageInner>, ImageInner>() {
            @Override
            public ImageInner call(ServiceResponse<ImageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Add an image to the list with list Id equal to list Id passed.
     *
     * @param listId List Id of the image list.
     * @param imageStream The image file.
     * @param tag Tag for the image.
     * @param label The image label.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageInner object
     */
    public Observable<ServiceResponse<ImageInner>> addImageFileInputWithServiceResponseAsync(String listId, byte[] imageStream, Integer tag, String label) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (listId == null) {
            throw new IllegalArgumentException("Parameter listId is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.addImageFileInput(listId, tag, label, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageInner> clientResponse = addImageFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageInner> addImageFileInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ImageInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ImageInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
