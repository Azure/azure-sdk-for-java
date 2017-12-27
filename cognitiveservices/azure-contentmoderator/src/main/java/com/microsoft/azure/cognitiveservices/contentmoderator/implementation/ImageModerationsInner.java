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
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * An instance of this class provides access to all the operations defined
 * in ImageModerations.
 */
public class ImageModerationsInner {
    /** The Retrofit service to perform REST calls. */
    private ImageModerationsService service;
    /** The service client containing this operation class. */
    private ContentModeratorClientImpl client;

    /**
     * Initializes an instance of ImageModerationsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ImageModerationsInner(Retrofit retrofit, ContentModeratorClientImpl client) {
        this.service = retrofit.create(ImageModerationsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ImageModerations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ImageModerationsService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations findFaces" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/FindFaces")
        Observable<Response<ResponseBody>> findFaces(@Query("CacheImage") Boolean cacheImage, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations oCRMethod" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/OCR")
        Observable<Response<ResponseBody>> oCRMethod(@Query("language") String language, @Query("CacheImage") Boolean cacheImage, @Query("enhanced") Boolean enhanced, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations evaluateMethod" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Evaluate")
        Observable<Response<ResponseBody>> evaluateMethod(@Query("CacheImage") Boolean cacheImage, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations matchMethod" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Match")
        Observable<Response<ResponseBody>> matchMethod(@Query("listId") String listId, @Query("CacheImage") Boolean cacheImage, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations findFacesFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/FindFaces")
        Observable<Response<ResponseBody>> findFacesFileInput(@Query("CacheImage") Boolean cacheImage, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations findFacesUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/FindFaces")
        Observable<Response<ResponseBody>> findFacesUrlInput(@Query("CacheImage") Boolean cacheImage, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations oCRUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/OCR")
        Observable<Response<ResponseBody>> oCRUrlInput(@Query("language") String language, @Query("CacheImage") Boolean cacheImage, @Query("enhanced") Boolean enhanced, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations oCRFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/OCR")
        Observable<Response<ResponseBody>> oCRFileInput(@Query("language") String language, @Query("CacheImage") Boolean cacheImage, @Query("enhanced") Boolean enhanced, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations evaluateFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Evaluate")
        Observable<Response<ResponseBody>> evaluateFileInput(@Query("CacheImage") Boolean cacheImage, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations evaluateUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Evaluate")
        Observable<Response<ResponseBody>> evaluateUrlInput(@Query("CacheImage") Boolean cacheImage, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations matchUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Match")
        Observable<Response<ResponseBody>> matchUrlInput(@Query("listId") String listId, @Query("CacheImage") Boolean cacheImage, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.ImageModerations matchFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Match")
        Observable<Response<ResponseBody>> matchFileInput(@Query("listId") String listId, @Query("CacheImage") Boolean cacheImage, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Returns the list of faces found.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFaces() {
        return findFacesWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesAsync(final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesAsync() {
        return findFacesWithServiceResponseAsync().map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesWithServiceResponseAsync() {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.findFaces(cacheImage, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FoundFacesInner>>>() {
                @Override
                public Observable<ServiceResponse<FoundFacesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FoundFacesInner> clientResponse = findFacesDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns the list of faces found.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFaces(Boolean cacheImage) {
        return findFacesWithServiceResponseAsync(cacheImage).toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesAsync(Boolean cacheImage, final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesWithServiceResponseAsync(cacheImage), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesAsync(Boolean cacheImage) {
        return findFacesWithServiceResponseAsync(cacheImage).map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesWithServiceResponseAsync(Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.findFaces(cacheImage, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FoundFacesInner>>>() {
                @Override
                public Observable<ServiceResponse<FoundFacesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FoundFacesInner> clientResponse = findFacesDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<FoundFacesInner> findFacesDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<FoundFacesInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<FoundFacesInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRMethod(String language) {
        return oCRMethodWithServiceResponseAsync(language).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRMethodAsync(String language, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRMethodWithServiceResponseAsync(language), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRMethodAsync(String language) {
        return oCRMethodWithServiceResponseAsync(language).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRMethodWithServiceResponseAsync(String language) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        final Boolean cacheImage = null;
        final Boolean enhanced = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.oCRMethod(language, cacheImage, enhanced, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OCRInner>>>() {
                @Override
                public Observable<ServiceResponse<OCRInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OCRInner> clientResponse = oCRMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRMethod(String language, Boolean cacheImage, Boolean enhanced) {
        return oCRMethodWithServiceResponseAsync(language, cacheImage, enhanced).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRMethodAsync(String language, Boolean cacheImage, Boolean enhanced, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRMethodWithServiceResponseAsync(language, cacheImage, enhanced), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRMethodAsync(String language, Boolean cacheImage, Boolean enhanced) {
        return oCRMethodWithServiceResponseAsync(language, cacheImage, enhanced).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRMethodWithServiceResponseAsync(String language, Boolean cacheImage, Boolean enhanced) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.oCRMethod(language, cacheImage, enhanced, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OCRInner>>>() {
                @Override
                public Observable<ServiceResponse<OCRInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OCRInner> clientResponse = oCRMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<OCRInner> oCRMethodDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<OCRInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<OCRInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateMethod() {
        return evaluateMethodWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateMethodAsync(final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateMethodAsync() {
        return evaluateMethodWithServiceResponseAsync().map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateMethodWithServiceResponseAsync() {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.evaluateMethod(cacheImage, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<EvaluateInner>>>() {
                @Override
                public Observable<ServiceResponse<EvaluateInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<EvaluateInner> clientResponse = evaluateMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateMethod(Boolean cacheImage) {
        return evaluateMethodWithServiceResponseAsync(cacheImage).toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateMethodAsync(Boolean cacheImage, final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateMethodWithServiceResponseAsync(cacheImage), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateMethodAsync(Boolean cacheImage) {
        return evaluateMethodWithServiceResponseAsync(cacheImage).map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateMethodWithServiceResponseAsync(Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.evaluateMethod(cacheImage, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<EvaluateInner>>>() {
                @Override
                public Observable<ServiceResponse<EvaluateInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<EvaluateInner> clientResponse = evaluateMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<EvaluateInner> evaluateMethodDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<EvaluateInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<EvaluateInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchMethod() {
        return matchMethodWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchMethodAsync(final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchMethodAsync() {
        return matchMethodWithServiceResponseAsync().map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
            @Override
            public MatchResponseInner call(ServiceResponse<MatchResponseInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchMethodWithServiceResponseAsync() {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        final String listId = null;
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.matchMethod(listId, cacheImage, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<MatchResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<MatchResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<MatchResponseInner> clientResponse = matchMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchMethod(String listId, Boolean cacheImage) {
        return matchMethodWithServiceResponseAsync(listId, cacheImage).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchMethodAsync(String listId, Boolean cacheImage, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchMethodWithServiceResponseAsync(listId, cacheImage), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchMethodAsync(String listId, Boolean cacheImage) {
        return matchMethodWithServiceResponseAsync(listId, cacheImage).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
            @Override
            public MatchResponseInner call(ServiceResponse<MatchResponseInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchMethodWithServiceResponseAsync(String listId, Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.matchMethod(listId, cacheImage, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<MatchResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<MatchResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<MatchResponseInner> clientResponse = matchMethodDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<MatchResponseInner> matchMethodDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<MatchResponseInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<MatchResponseInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFacesFileInput(byte[] imageStream) {
        return findFacesFileInputWithServiceResponseAsync(imageStream).toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesFileInputAsync(byte[] imageStream, final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesFileInputWithServiceResponseAsync(imageStream), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesFileInputAsync(byte[] imageStream) {
        return findFacesFileInputWithServiceResponseAsync(imageStream).map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesFileInputWithServiceResponseAsync(byte[] imageStream) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.findFacesFileInput(cacheImage, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FoundFacesInner>>>() {
                @Override
                public Observable<ServiceResponse<FoundFacesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FoundFacesInner> clientResponse = findFacesFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFacesFileInput(byte[] imageStream, Boolean cacheImage) {
        return findFacesFileInputWithServiceResponseAsync(imageStream, cacheImage).toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesFileInputAsync(byte[] imageStream, Boolean cacheImage, final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesFileInputWithServiceResponseAsync(imageStream, cacheImage), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesFileInputAsync(byte[] imageStream, Boolean cacheImage) {
        return findFacesFileInputWithServiceResponseAsync(imageStream, cacheImage).map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesFileInputWithServiceResponseAsync(byte[] imageStream, Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.findFacesFileInput(cacheImage, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FoundFacesInner>>>() {
                @Override
                public Observable<ServiceResponse<FoundFacesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FoundFacesInner> clientResponse = findFacesFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<FoundFacesInner> findFacesFileInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<FoundFacesInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<FoundFacesInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFacesUrlInput(String contentType, BodyModelInner imageUrl) {
        return findFacesUrlInputWithServiceResponseAsync(contentType, imageUrl).toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesUrlInputAsync(String contentType, BodyModelInner imageUrl, final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesUrlInputWithServiceResponseAsync(contentType, imageUrl), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesUrlInputAsync(String contentType, BodyModelInner imageUrl) {
        return findFacesUrlInputWithServiceResponseAsync(contentType, imageUrl).map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesUrlInputWithServiceResponseAsync(String contentType, BodyModelInner imageUrl) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.findFacesUrlInput(cacheImage, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FoundFacesInner>>>() {
                @Override
                public Observable<ServiceResponse<FoundFacesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FoundFacesInner> clientResponse = findFacesUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFacesUrlInput(String contentType, BodyModelInner imageUrl, Boolean cacheImage) {
        return findFacesUrlInputWithServiceResponseAsync(contentType, imageUrl, cacheImage).toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesUrlInputAsync(String contentType, BodyModelInner imageUrl, Boolean cacheImage, final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesUrlInputWithServiceResponseAsync(contentType, imageUrl, cacheImage), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesUrlInputAsync(String contentType, BodyModelInner imageUrl, Boolean cacheImage) {
        return findFacesUrlInputWithServiceResponseAsync(contentType, imageUrl, cacheImage).map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesUrlInputWithServiceResponseAsync(String contentType, BodyModelInner imageUrl, Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.findFacesUrlInput(cacheImage, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<FoundFacesInner>>>() {
                @Override
                public Observable<ServiceResponse<FoundFacesInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<FoundFacesInner> clientResponse = findFacesUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<FoundFacesInner> findFacesUrlInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<FoundFacesInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<FoundFacesInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRUrlInput(String language, String contentType, BodyModelInner imageUrl) {
        return oCRUrlInputWithServiceResponseAsync(language, contentType, imageUrl).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRUrlInputAsync(String language, String contentType, BodyModelInner imageUrl, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRUrlInputWithServiceResponseAsync(language, contentType, imageUrl), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRUrlInputAsync(String language, String contentType, BodyModelInner imageUrl) {
        return oCRUrlInputWithServiceResponseAsync(language, contentType, imageUrl).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRUrlInputWithServiceResponseAsync(String language, String contentType, BodyModelInner imageUrl) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        final Boolean cacheImage = null;
        final Boolean enhanced = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.oCRUrlInput(language, cacheImage, enhanced, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OCRInner>>>() {
                @Override
                public Observable<ServiceResponse<OCRInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OCRInner> clientResponse = oCRUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRUrlInput(String language, String contentType, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced) {
        return oCRUrlInputWithServiceResponseAsync(language, contentType, imageUrl, cacheImage, enhanced).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRUrlInputAsync(String language, String contentType, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRUrlInputWithServiceResponseAsync(language, contentType, imageUrl, cacheImage, enhanced), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRUrlInputAsync(String language, String contentType, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced) {
        return oCRUrlInputWithServiceResponseAsync(language, contentType, imageUrl, cacheImage, enhanced).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRUrlInputWithServiceResponseAsync(String language, String contentType, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.oCRUrlInput(language, cacheImage, enhanced, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OCRInner>>>() {
                @Override
                public Observable<ServiceResponse<OCRInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OCRInner> clientResponse = oCRUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<OCRInner> oCRUrlInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<OCRInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<OCRInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRFileInput(String language, byte[] imageStream) {
        return oCRFileInputWithServiceResponseAsync(language, imageStream).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRFileInputAsync(String language, byte[] imageStream, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRFileInputWithServiceResponseAsync(language, imageStream), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRFileInputAsync(String language, byte[] imageStream) {
        return oCRFileInputWithServiceResponseAsync(language, imageStream).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRFileInputWithServiceResponseAsync(String language, byte[] imageStream) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        final Boolean cacheImage = null;
        final Boolean enhanced = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.oCRFileInput(language, cacheImage, enhanced, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OCRInner>>>() {
                @Override
                public Observable<ServiceResponse<OCRInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OCRInner> clientResponse = oCRFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRFileInput(String language, byte[] imageStream, Boolean cacheImage, Boolean enhanced) {
        return oCRFileInputWithServiceResponseAsync(language, imageStream, cacheImage, enhanced).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRFileInputAsync(String language, byte[] imageStream, Boolean cacheImage, Boolean enhanced, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRFileInputWithServiceResponseAsync(language, imageStream, cacheImage, enhanced), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRFileInputAsync(String language, byte[] imageStream, Boolean cacheImage, Boolean enhanced) {
        return oCRFileInputWithServiceResponseAsync(language, imageStream, cacheImage, enhanced).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language Language of the terms.
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRFileInputWithServiceResponseAsync(String language, byte[] imageStream, Boolean cacheImage, Boolean enhanced) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.oCRFileInput(language, cacheImage, enhanced, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OCRInner>>>() {
                @Override
                public Observable<ServiceResponse<OCRInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OCRInner> clientResponse = oCRFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<OCRInner> oCRFileInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<OCRInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<OCRInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateFileInput(byte[] imageStream) {
        return evaluateFileInputWithServiceResponseAsync(imageStream).toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateFileInputAsync(byte[] imageStream, final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateFileInputWithServiceResponseAsync(imageStream), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateFileInputAsync(byte[] imageStream) {
        return evaluateFileInputWithServiceResponseAsync(imageStream).map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateFileInputWithServiceResponseAsync(byte[] imageStream) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.evaluateFileInput(cacheImage, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<EvaluateInner>>>() {
                @Override
                public Observable<ServiceResponse<EvaluateInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<EvaluateInner> clientResponse = evaluateFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateFileInput(byte[] imageStream, Boolean cacheImage) {
        return evaluateFileInputWithServiceResponseAsync(imageStream, cacheImage).toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateFileInputAsync(byte[] imageStream, Boolean cacheImage, final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateFileInputWithServiceResponseAsync(imageStream, cacheImage), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateFileInputAsync(byte[] imageStream, Boolean cacheImage) {
        return evaluateFileInputWithServiceResponseAsync(imageStream, cacheImage).map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateFileInputWithServiceResponseAsync(byte[] imageStream, Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.evaluateFileInput(cacheImage, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<EvaluateInner>>>() {
                @Override
                public Observable<ServiceResponse<EvaluateInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<EvaluateInner> clientResponse = evaluateFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<EvaluateInner> evaluateFileInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<EvaluateInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<EvaluateInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateUrlInput(String contentType, BodyModelInner imageUrl) {
        return evaluateUrlInputWithServiceResponseAsync(contentType, imageUrl).toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateUrlInputAsync(String contentType, BodyModelInner imageUrl, final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateUrlInputWithServiceResponseAsync(contentType, imageUrl), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateUrlInputAsync(String contentType, BodyModelInner imageUrl) {
        return evaluateUrlInputWithServiceResponseAsync(contentType, imageUrl).map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateUrlInputWithServiceResponseAsync(String contentType, BodyModelInner imageUrl) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.evaluateUrlInput(cacheImage, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<EvaluateInner>>>() {
                @Override
                public Observable<ServiceResponse<EvaluateInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<EvaluateInner> clientResponse = evaluateUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateUrlInput(String contentType, BodyModelInner imageUrl, Boolean cacheImage) {
        return evaluateUrlInputWithServiceResponseAsync(contentType, imageUrl, cacheImage).toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateUrlInputAsync(String contentType, BodyModelInner imageUrl, Boolean cacheImage, final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateUrlInputWithServiceResponseAsync(contentType, imageUrl, cacheImage), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateUrlInputAsync(String contentType, BodyModelInner imageUrl, Boolean cacheImage) {
        return evaluateUrlInputWithServiceResponseAsync(contentType, imageUrl, cacheImage).map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateUrlInputWithServiceResponseAsync(String contentType, BodyModelInner imageUrl, Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.evaluateUrlInput(cacheImage, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<EvaluateInner>>>() {
                @Override
                public Observable<ServiceResponse<EvaluateInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<EvaluateInner> clientResponse = evaluateUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<EvaluateInner> evaluateUrlInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<EvaluateInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<EvaluateInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchUrlInput(String contentType, BodyModelInner imageUrl) {
        return matchUrlInputWithServiceResponseAsync(contentType, imageUrl).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchUrlInputAsync(String contentType, BodyModelInner imageUrl, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchUrlInputWithServiceResponseAsync(contentType, imageUrl), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchUrlInputAsync(String contentType, BodyModelInner imageUrl) {
        return matchUrlInputWithServiceResponseAsync(contentType, imageUrl).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
            @Override
            public MatchResponseInner call(ServiceResponse<MatchResponseInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchUrlInputWithServiceResponseAsync(String contentType, BodyModelInner imageUrl) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        final String listId = null;
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.matchUrlInput(listId, cacheImage, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<MatchResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<MatchResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<MatchResponseInner> clientResponse = matchUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchUrlInput(String contentType, BodyModelInner imageUrl, String listId, Boolean cacheImage) {
        return matchUrlInputWithServiceResponseAsync(contentType, imageUrl, listId, cacheImage).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchUrlInputAsync(String contentType, BodyModelInner imageUrl, String listId, Boolean cacheImage, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchUrlInputWithServiceResponseAsync(contentType, imageUrl, listId, cacheImage), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchUrlInputAsync(String contentType, BodyModelInner imageUrl, String listId, Boolean cacheImage) {
        return matchUrlInputWithServiceResponseAsync(contentType, imageUrl, listId, cacheImage).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
            @Override
            public MatchResponseInner call(ServiceResponse<MatchResponseInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param contentType The content type.
     * @param imageUrl The image url.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchUrlInputWithServiceResponseAsync(String contentType, BodyModelInner imageUrl, String listId, Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("Parameter contentType is required and cannot be null.");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("Parameter imageUrl is required and cannot be null.");
        }
        Validator.validate(imageUrl);
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.matchUrlInput(listId, cacheImage, contentType, imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<MatchResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<MatchResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<MatchResponseInner> clientResponse = matchUrlInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<MatchResponseInner> matchUrlInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<MatchResponseInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<MatchResponseInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchFileInput(byte[] imageStream) {
        return matchFileInputWithServiceResponseAsync(imageStream).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchFileInputAsync(byte[] imageStream, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchFileInputWithServiceResponseAsync(imageStream), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchFileInputAsync(byte[] imageStream) {
        return matchFileInputWithServiceResponseAsync(imageStream).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
            @Override
            public MatchResponseInner call(ServiceResponse<MatchResponseInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchFileInputWithServiceResponseAsync(byte[] imageStream) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        final String listId = null;
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.matchFileInput(listId, cacheImage, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<MatchResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<MatchResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<MatchResponseInner> clientResponse = matchFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchFileInput(byte[] imageStream, String listId, Boolean cacheImage) {
        return matchFileInputWithServiceResponseAsync(imageStream, listId, cacheImage).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchFileInputAsync(byte[] imageStream, String listId, Boolean cacheImage, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchFileInputWithServiceResponseAsync(imageStream, listId, cacheImage), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchFileInputAsync(byte[] imageStream, String listId, Boolean cacheImage) {
        return matchFileInputWithServiceResponseAsync(imageStream, listId, cacheImage).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
            @Override
            public MatchResponseInner call(ServiceResponse<MatchResponseInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @param listId The list Id.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchFileInputWithServiceResponseAsync(byte[] imageStream, String listId, Boolean cacheImage) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (imageStream == null) {
            throw new IllegalArgumentException("Parameter imageStream is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.matchFileInput(listId, cacheImage, imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<MatchResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<MatchResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<MatchResponseInner> clientResponse = matchFileInputDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<MatchResponseInner> matchFileInputDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<MatchResponseInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<MatchResponseInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
