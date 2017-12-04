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
 * in Images.
 */
public class ImagesInner {
    /** The Retrofit service to perform REST calls. */
    private ImagesService service;
    /** The service client containing this operation class. */
    private ContentModeratorImageTextClientImpl client;

    /**
     * Initializes an instance of ImagesInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ImagesInner(Retrofit retrofit, ContentModeratorImageTextClientImpl client) {
        this.service = retrofit.create(ImagesService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Images to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ImagesService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images findFaces" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/FindFaces")
        Observable<Response<ResponseBody>> findFaces(@Query("CacheImage") Boolean cacheImage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images oCRMethod" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/OCR")
        Observable<Response<ResponseBody>> oCRMethod(@Query("language") String language, @Query("CacheImage") Boolean cacheImage, @Query("enhanced") Boolean enhanced, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images evaluateMethod" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Evaluate")
        Observable<Response<ResponseBody>> evaluateMethod(@Query("CacheImage") Boolean cacheImage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images match" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Match")
        Observable<Response<ResponseBody>> match(@Query("listId") String listId, @Query("CacheImage") Boolean cacheImage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images findFacesFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/FindFaces")
        Observable<Response<ResponseBody>> findFacesFileInput(@Query("CacheImage") Boolean cacheImage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images findFacesUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/FindFaces")
        Observable<Response<ResponseBody>> findFacesUrlInput(@Query("CacheImage") Boolean cacheImage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images oCRUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/OCR")
        Observable<Response<ResponseBody>> oCRUrlInput(@Query("language") String language, @Query("CacheImage") Boolean cacheImage, @Query("enhanced") Boolean enhanced, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images oCRFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/OCR")
        Observable<Response<ResponseBody>> oCRFileInput(@Query("language") String language, @Query("CacheImage") Boolean cacheImage, @Query("enhanced") Boolean enhanced, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images evaluateFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Evaluate")
        Observable<Response<ResponseBody>> evaluateFileInput(@Query("CacheImage") Boolean cacheImage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images evaluateUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Evaluate")
        Observable<Response<ResponseBody>> evaluateUrlInput(@Query("CacheImage") Boolean cacheImage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images matchUrlInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Match")
        Observable<Response<ResponseBody>> matchUrlInput(@Query("listId") String listId, @Query("cacheimage") Boolean cacheimage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body BodyModelInner imageUrl, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: image/gif", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Images matchFileInput" })
        @POST("contentmoderator/moderate/v1.0/ProcessImage/Match")
        Observable<Response<ResponseBody>> matchFileInput(@Query("listId") String listId, @Query("cacheimage") Boolean cacheimage, @Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body RequestBody imageStream, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Returns the list of faces found.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.findFaces(this.client.cacheImage(), this.client.ocpApimSubscriptionKey(), this.client.contentType(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<FoundFacesInner> findFacesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<FoundFacesInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<FoundFacesInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRMethod() {
        return oCRMethodWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRMethodAsync(final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRMethodWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRMethodAsync() {
        return oCRMethodWithServiceResponseAsync().map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRMethodWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.language() == null) {
            throw new IllegalArgumentException("Parameter this.client.language() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.oCRMethod(this.client.language(), this.client.cacheImage(), this.client.enhanced(), this.client.ocpApimSubscriptionKey(), this.client.contentType(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<OCRInner> oCRMethodDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<OCRInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<OCRInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.evaluateMethod(this.client.cacheImage(), this.client.ocpApimSubscriptionKey(), this.client.contentType(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<EvaluateInner> evaluateMethodDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<EvaluateInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<EvaluateInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner match() {
        return matchWithServiceResponseAsync().toBlocking().single().body();
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
    public ServiceFuture<MatchResponseInner> matchAsync(final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchWithServiceResponseAsync(), serviceCallback);
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
    public Observable<MatchResponseInner> matchAsync() {
        return matchWithServiceResponseAsync().map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
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
    public Observable<ServiceResponse<MatchResponseInner>> matchWithServiceResponseAsync() {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.match(this.client.listId(), this.client.cacheImage(), this.client.ocpApimSubscriptionKey(), this.client.contentType(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<MatchResponseInner>>>() {
                @Override
                public Observable<ServiceResponse<MatchResponseInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<MatchResponseInner> clientResponse = matchDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<MatchResponseInner> matchDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<MatchResponseInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<MatchResponseInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.findFacesFileInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        return service.findFacesFileInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<FoundFacesInner> findFacesFileInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<FoundFacesInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<FoundFacesInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFacesUrlInput(BodyModelInner imageUrl) {
        return findFacesUrlInputWithServiceResponseAsync(imageUrl).toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesUrlInputAsync(BodyModelInner imageUrl, final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesUrlInputWithServiceResponseAsync(imageUrl), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesUrlInputAsync(BodyModelInner imageUrl) {
        return findFacesUrlInputWithServiceResponseAsync(imageUrl).map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesUrlInputWithServiceResponseAsync(BodyModelInner imageUrl) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.findFacesUrlInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the FoundFacesInner object if successful.
     */
    public FoundFacesInner findFacesUrlInput(BodyModelInner imageUrl, Boolean cacheImage) {
        return findFacesUrlInputWithServiceResponseAsync(imageUrl, cacheImage).toBlocking().single().body();
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<FoundFacesInner> findFacesUrlInputAsync(BodyModelInner imageUrl, Boolean cacheImage, final ServiceCallback<FoundFacesInner> serviceCallback) {
        return ServiceFuture.fromResponse(findFacesUrlInputWithServiceResponseAsync(imageUrl, cacheImage), serviceCallback);
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<FoundFacesInner> findFacesUrlInputAsync(BodyModelInner imageUrl, Boolean cacheImage) {
        return findFacesUrlInputWithServiceResponseAsync(imageUrl, cacheImage).map(new Func1<ServiceResponse<FoundFacesInner>, FoundFacesInner>() {
            @Override
            public FoundFacesInner call(ServiceResponse<FoundFacesInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns the list of faces found.
     *
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the FoundFacesInner object
     */
    public Observable<ServiceResponse<FoundFacesInner>> findFacesUrlInputWithServiceResponseAsync(BodyModelInner imageUrl, Boolean cacheImage) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        return service.findFacesUrlInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<FoundFacesInner> findFacesUrlInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<FoundFacesInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<FoundFacesInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRUrlInput(String language, BodyModelInner imageUrl) {
        return oCRUrlInputWithServiceResponseAsync(language, imageUrl).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRUrlInputAsync(String language, BodyModelInner imageUrl, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRUrlInputWithServiceResponseAsync(language, imageUrl), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRUrlInputAsync(String language, BodyModelInner imageUrl) {
        return oCRUrlInputWithServiceResponseAsync(language, imageUrl).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRUrlInputWithServiceResponseAsync(String language, BodyModelInner imageUrl) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
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
        final Boolean cacheImage = null;
        final Boolean enhanced = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.oCRUrlInput(language, cacheImage, enhanced, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRUrlInput(String language, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced) {
        return oCRUrlInputWithServiceResponseAsync(language, imageUrl, cacheImage, enhanced).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OCRInner> oCRUrlInputAsync(String language, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced, final ServiceCallback<OCRInner> serviceCallback) {
        return ServiceFuture.fromResponse(oCRUrlInputWithServiceResponseAsync(language, imageUrl, cacheImage, enhanced), serviceCallback);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<OCRInner> oCRUrlInputAsync(String language, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced) {
        return oCRUrlInputWithServiceResponseAsync(language, imageUrl, cacheImage, enhanced).map(new Func1<ServiceResponse<OCRInner>, OCRInner>() {
            @Override
            public OCRInner call(ServiceResponse<OCRInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRUrlInputWithServiceResponseAsync(String language, BodyModelInner imageUrl, Boolean cacheImage, Boolean enhanced) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
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
        return service.oCRUrlInput(language, cacheImage, enhanced, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<OCRInner> oCRUrlInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<OCRInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<OCRInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRFileInput(String language, byte[] imageStream) {
        return oCRFileInputWithServiceResponseAsync(language, imageStream).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
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
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
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
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRFileInputWithServiceResponseAsync(String language, byte[] imageStream) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
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
        final Boolean cacheImage = null;
        final Boolean enhanced = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.oCRFileInput(language, cacheImage, enhanced, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OCRInner object if successful.
     */
    public OCRInner oCRFileInput(String language, byte[] imageStream, Boolean cacheImage, Boolean enhanced) {
        return oCRFileInputWithServiceResponseAsync(language, imageStream, cacheImage, enhanced).toBlocking().single().body();
    }

    /**
     * Returns any text found in the image for the language specified. If no language is specified in input then the detection defaults to English.
     *
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
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
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
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
     * @param language &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt;.
     * @param imageStream The image file.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param enhanced When set to True, the image goes through additional processing to come with additional candidates.
     image/tiff is not supported when enhanced is set to true
     Note: This impacts the response time.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OCRInner object
     */
    public Observable<ServiceResponse<OCRInner>> oCRFileInputWithServiceResponseAsync(String language, byte[] imageStream, Boolean cacheImage, Boolean enhanced) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
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
        return service.oCRFileInput(language, cacheImage, enhanced, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<OCRInner> oCRFileInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<OCRInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<OCRInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageStream The image file.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.evaluateFileInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        return service.evaluateFileInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<EvaluateInner> evaluateFileInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<EvaluateInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<EvaluateInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateUrlInput(BodyModelInner imageUrl) {
        return evaluateUrlInputWithServiceResponseAsync(imageUrl).toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateUrlInputAsync(BodyModelInner imageUrl, final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateUrlInputWithServiceResponseAsync(imageUrl), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateUrlInputAsync(BodyModelInner imageUrl) {
        return evaluateUrlInputWithServiceResponseAsync(imageUrl).map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateUrlInputWithServiceResponseAsync(BodyModelInner imageUrl) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        final Boolean cacheImage = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.evaluateUrlInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the EvaluateInner object if successful.
     */
    public EvaluateInner evaluateUrlInput(BodyModelInner imageUrl, Boolean cacheImage) {
        return evaluateUrlInputWithServiceResponseAsync(imageUrl, cacheImage).toBlocking().single().body();
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<EvaluateInner> evaluateUrlInputAsync(BodyModelInner imageUrl, Boolean cacheImage, final ServiceCallback<EvaluateInner> serviceCallback) {
        return ServiceFuture.fromResponse(evaluateUrlInputWithServiceResponseAsync(imageUrl, cacheImage), serviceCallback);
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<EvaluateInner> evaluateUrlInputAsync(BodyModelInner imageUrl, Boolean cacheImage) {
        return evaluateUrlInputWithServiceResponseAsync(imageUrl, cacheImage).map(new Func1<ServiceResponse<EvaluateInner>, EvaluateInner>() {
            @Override
            public EvaluateInner call(ServiceResponse<EvaluateInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Returns probabilities of the image containing racy or adult content.
     *
     * @param imageUrl The image url.
     * @param cacheImage Whether to retain the submitted image for future use; defaults to false if omitted.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the EvaluateInner object
     */
    public Observable<ServiceResponse<EvaluateInner>> evaluateUrlInputWithServiceResponseAsync(BodyModelInner imageUrl, Boolean cacheImage) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        return service.evaluateUrlInput(cacheImage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<EvaluateInner> evaluateUrlInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<EvaluateInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<EvaluateInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchUrlInput(BodyModelInner imageUrl) {
        return matchUrlInputWithServiceResponseAsync(imageUrl).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageUrl The image url.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchUrlInputAsync(BodyModelInner imageUrl, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchUrlInputWithServiceResponseAsync(imageUrl), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchUrlInputAsync(BodyModelInner imageUrl) {
        return matchUrlInputWithServiceResponseAsync(imageUrl).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
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
     * @param imageUrl The image url.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchUrlInputWithServiceResponseAsync(BodyModelInner imageUrl) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        final String listId = null;
        final Boolean cacheimage = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.matchUrlInput(listId, cacheimage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @param imageUrl The image url.
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchUrlInput(BodyModelInner imageUrl, String listId, Boolean cacheimage) {
        return matchUrlInputWithServiceResponseAsync(imageUrl, listId, cacheimage).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageUrl The image url.
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchUrlInputAsync(BodyModelInner imageUrl, String listId, Boolean cacheimage, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchUrlInputWithServiceResponseAsync(imageUrl, listId, cacheimage), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageUrl The image url.
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchUrlInputAsync(BodyModelInner imageUrl, String listId, Boolean cacheimage) {
        return matchUrlInputWithServiceResponseAsync(imageUrl, listId, cacheimage).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
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
     * @param imageUrl The image url.
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchUrlInputWithServiceResponseAsync(BodyModelInner imageUrl, String listId, Boolean cacheimage) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        return service.matchUrlInput(listId, cacheimage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageUrl, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<MatchResponseInner> matchUrlInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<MatchResponseInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<MatchResponseInner>() { }.getType())
                .registerError(CloudException.class)
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
     * @throws CloudException thrown if the request is rejected by server
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
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        final String listId = null;
        final Boolean cacheimage = null;
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        RequestBody imageStreamConverted = RequestBody.create(MediaType.parse("image/gif"), imageStream);
        return service.matchFileInput(listId, cacheimage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the MatchResponseInner object if successful.
     */
    public MatchResponseInner matchFileInput(byte[] imageStream, String listId, Boolean cacheimage) {
        return matchFileInputWithServiceResponseAsync(imageStream, listId, cacheimage).toBlocking().single().body();
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<MatchResponseInner> matchFileInputAsync(byte[] imageStream, String listId, Boolean cacheimage, final ServiceCallback<MatchResponseInner> serviceCallback) {
        return ServiceFuture.fromResponse(matchFileInputWithServiceResponseAsync(imageStream, listId, cacheimage), serviceCallback);
    }

    /**
     * Fuzzily match an image against one of your custom Image Lists. You can create and manage your custom image lists using &lt;a href="/docs/services/578ff44d2703741568569ab9/operations/578ff7b12703741568569abe"&gt;this&lt;/a&gt; API.
     Returns ID and tags of matching image.&lt;br/&gt;
     &lt;br/&gt;
     Note: Refresh Index must be run on the corresponding Image List before additions and removals are reflected in the response.
     *
     * @param imageStream The image file.
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<MatchResponseInner> matchFileInputAsync(byte[] imageStream, String listId, Boolean cacheimage) {
        return matchFileInputWithServiceResponseAsync(imageStream, listId, cacheimage).map(new Func1<ServiceResponse<MatchResponseInner>, MatchResponseInner>() {
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
     * @param listId The list id.
     * @param cacheimage Use cached image.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the MatchResponseInner object
     */
    public Observable<ServiceResponse<MatchResponseInner>> matchFileInputWithServiceResponseAsync(byte[] imageStream, String listId, Boolean cacheimage) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
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
        return service.matchFileInput(listId, cacheimage, this.client.ocpApimSubscriptionKey(), this.client.contentType(), imageStreamConverted, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<MatchResponseInner> matchFileInputDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<MatchResponseInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<MatchResponseInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
