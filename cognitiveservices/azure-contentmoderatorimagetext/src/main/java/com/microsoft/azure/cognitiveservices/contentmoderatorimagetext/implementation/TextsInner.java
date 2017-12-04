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
 * in Texts.
 */
public class TextsInner {
    /** The Retrofit service to perform REST calls. */
    private TextsService service;
    /** The service client containing this operation class. */
    private ContentModeratorImageTextClientImpl client;

    /**
     * Initializes an instance of TextsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public TextsInner(Retrofit retrofit, ContentModeratorImageTextClientImpl client) {
        this.service = retrofit.create(TextsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Texts to be
     * used by Retrofit to perform actually REST calls.
     */
    interface TextsService {
        @Headers({ "Content-Type: text/plain", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Texts screenText" })
        @POST("contentmoderator/moderate/v1.0/ProcessText/Screen/")
        Observable<Response<ResponseBody>> screenText(@Query("language") String language, @Query("autocorrect") Boolean autocorrect, @Query("PII") Boolean pII, @Query("listId") String listId, @Query("classify") Boolean classify, @Header("Content-Type") String contentType, @Body String textContent, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: text/plain", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.Texts detectLanguage" })
        @POST("contentmoderator/moderate/v1.0/ProcessText/DetectLanguage")
        Observable<Response<ResponseBody>> detectLanguage(@Header("Ocp-Apim-Subscription-Key") String ocpApimSubscriptionKey, @Header("Content-Type") String contentType, @Body String textContent, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ScreenInner object if successful.
     */
    public ScreenInner screenText(String textContent) {
        return screenTextWithServiceResponseAsync(textContent).toBlocking().single().body();
    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param textContent Content to screen.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ScreenInner> screenTextAsync(String textContent, final ServiceCallback<ScreenInner> serviceCallback) {
        return ServiceFuture.fromResponse(screenTextWithServiceResponseAsync(textContent), serviceCallback);
    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ScreenInner object
     */
    public Observable<ScreenInner> screenTextAsync(String textContent) {
        return screenTextWithServiceResponseAsync(textContent).map(new Func1<ServiceResponse<ScreenInner>, ScreenInner>() {
            @Override
            public ScreenInner call(ServiceResponse<ScreenInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ScreenInner object
     */
    public Observable<ServiceResponse<ScreenInner>> screenTextWithServiceResponseAsync(String textContent) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.language() == null) {
            throw new IllegalArgumentException("Parameter this.client.language() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (textContent == null) {
            throw new IllegalArgumentException("Parameter textContent is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.screenText(this.client.language(), this.client.autocorrect(), this.client.pII(), this.client.listId(), this.client.classify(), this.client.contentType(), textContent, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ScreenInner>>>() {
                @Override
                public Observable<ServiceResponse<ScreenInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ScreenInner> clientResponse = screenTextDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ScreenInner> screenTextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ScreenInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ScreenInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContent Text content whose language is to be detected.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the DetectedLanguageResultInner object if successful.
     */
    public DetectedLanguageResultInner detectLanguage(String textContent) {
        return detectLanguageWithServiceResponseAsync(textContent).toBlocking().single().body();
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContent Text content whose language is to be detected.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<DetectedLanguageResultInner> detectLanguageAsync(String textContent, final ServiceCallback<DetectedLanguageResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(detectLanguageWithServiceResponseAsync(textContent), serviceCallback);
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContent Text content whose language is to be detected.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DetectedLanguageResultInner object
     */
    public Observable<DetectedLanguageResultInner> detectLanguageAsync(String textContent) {
        return detectLanguageWithServiceResponseAsync(textContent).map(new Func1<ServiceResponse<DetectedLanguageResultInner>, DetectedLanguageResultInner>() {
            @Override
            public DetectedLanguageResultInner call(ServiceResponse<DetectedLanguageResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContent Text content whose language is to be detected.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DetectedLanguageResultInner object
     */
    public Observable<ServiceResponse<DetectedLanguageResultInner>> detectLanguageWithServiceResponseAsync(String textContent) {
        if (this.client.azureRegion1() == null) {
            throw new IllegalArgumentException("Parameter this.client.azureRegion1() is required and cannot be null.");
        }
        if (this.client.ocpApimSubscriptionKey() == null) {
            throw new IllegalArgumentException("Parameter this.client.ocpApimSubscriptionKey() is required and cannot be null.");
        }
        if (this.client.contentType() == null) {
            throw new IllegalArgumentException("Parameter this.client.contentType() is required and cannot be null.");
        }
        if (textContent == null) {
            throw new IllegalArgumentException("Parameter textContent is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{azureRegion}", this.client.azureRegion1());
        return service.detectLanguage(this.client.ocpApimSubscriptionKey(), this.client.contentType(), textContent, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<DetectedLanguageResultInner>>>() {
                @Override
                public Observable<ServiceResponse<DetectedLanguageResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<DetectedLanguageResultInner> clientResponse = detectLanguageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<DetectedLanguageResultInner> detectLanguageDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<DetectedLanguageResultInner, CloudException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<DetectedLanguageResultInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
