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
 * in TextModerations.
 */
public class TextModerationsInner {
    /** The Retrofit service to perform REST calls. */
    private TextModerationsService service;
    /** The service client containing this operation class. */
    private ContentModeratorClientImpl client;

    /**
     * Initializes an instance of TextModerationsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public TextModerationsInner(Retrofit retrofit, ContentModeratorClientImpl client) {
        this.service = retrofit.create(TextModerationsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for TextModerations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface TextModerationsService {
        @Headers({ "Content-Type: text/plain", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.TextModerations screenText" })
        @POST("contentmoderator/moderate/v1.0/ProcessText/Screen/")
        Observable<Response<ResponseBody>> screenText(@Query("language") String language, @Query("autocorrect") Boolean autocorrect, @Query("PII") Boolean pII, @Query("listId") String listId, @Query("classify") Boolean classify, @Header("Content-Type") String textContentType, @Body String textContent, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: text/plain", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.contentmoderator.TextModerations detectLanguage" })
        @POST("contentmoderator/moderate/v1.0/ProcessText/DetectLanguage")
        Observable<Response<ResponseBody>> detectLanguage(@Header("Content-Type") String textContentType, @Body String textContent, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ScreenInner object if successful.
     */
    public ScreenInner screenText(String language, String textContentType, String textContent) {
        return screenTextWithServiceResponseAsync(language, textContentType, textContent).toBlocking().single().body();
    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ScreenInner> screenTextAsync(String language, String textContentType, String textContent, final ServiceCallback<ScreenInner> serviceCallback) {
        return ServiceFuture.fromResponse(screenTextWithServiceResponseAsync(language, textContentType, textContent), serviceCallback);
    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ScreenInner object
     */
    public Observable<ScreenInner> screenTextAsync(String language, String textContentType, String textContent) {
        return screenTextWithServiceResponseAsync(language, textContentType, textContent).map(new Func1<ServiceResponse<ScreenInner>, ScreenInner>() {
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
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ScreenInner object
     */
    public Observable<ServiceResponse<ScreenInner>> screenTextWithServiceResponseAsync(String language, String textContentType, String textContent) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        if (textContentType == null) {
            throw new IllegalArgumentException("Parameter textContentType is required and cannot be null.");
        }
        if (textContent == null) {
            throw new IllegalArgumentException("Parameter textContent is required and cannot be null.");
        }
        final Boolean autocorrect = null;
        final Boolean pII = null;
        final String listId = null;
        final Boolean classify = null;
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.screenText(language, autocorrect, pII, listId, classify, textContentType, textContent, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @param autocorrect Autocorrect text.
     * @param pII Detect personal identifiable information.
     * @param listId The list Id.
     * @param classify Classify input.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ScreenInner object if successful.
     */
    public ScreenInner screenText(String language, String textContentType, String textContent, Boolean autocorrect, Boolean pII, String listId, Boolean classify) {
        return screenTextWithServiceResponseAsync(language, textContentType, textContent, autocorrect, pII, listId, classify).toBlocking().single().body();
    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @param autocorrect Autocorrect text.
     * @param pII Detect personal identifiable information.
     * @param listId The list Id.
     * @param classify Classify input.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ScreenInner> screenTextAsync(String language, String textContentType, String textContent, Boolean autocorrect, Boolean pII, String listId, Boolean classify, final ServiceCallback<ScreenInner> serviceCallback) {
        return ServiceFuture.fromResponse(screenTextWithServiceResponseAsync(language, textContentType, textContent, autocorrect, pII, listId, classify), serviceCallback);
    }

    /**
     * Detect profanity and match against custom and shared blacklists.
     * Detects profanity in more than 100 languages and match against custom and shared blacklists.
     *
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @param autocorrect Autocorrect text.
     * @param pII Detect personal identifiable information.
     * @param listId The list Id.
     * @param classify Classify input.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ScreenInner object
     */
    public Observable<ScreenInner> screenTextAsync(String language, String textContentType, String textContent, Boolean autocorrect, Boolean pII, String listId, Boolean classify) {
        return screenTextWithServiceResponseAsync(language, textContentType, textContent, autocorrect, pII, listId, classify).map(new Func1<ServiceResponse<ScreenInner>, ScreenInner>() {
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
     * @param language Language of the terms.
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @param autocorrect Autocorrect text.
     * @param pII Detect personal identifiable information.
     * @param listId The list Id.
     * @param classify Classify input.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ScreenInner object
     */
    public Observable<ServiceResponse<ScreenInner>> screenTextWithServiceResponseAsync(String language, String textContentType, String textContent, Boolean autocorrect, Boolean pII, String listId, Boolean classify) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Parameter language is required and cannot be null.");
        }
        if (textContentType == null) {
            throw new IllegalArgumentException("Parameter textContentType is required and cannot be null.");
        }
        if (textContent == null) {
            throw new IllegalArgumentException("Parameter textContent is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.screenText(language, autocorrect, pII, listId, classify, textContentType, textContent, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
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

    private ServiceResponse<ScreenInner> screenTextDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<ScreenInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<ScreenInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws APIErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the DetectedLanguageInner object if successful.
     */
    public DetectedLanguageInner detectLanguage(String textContentType, String textContent) {
        return detectLanguageWithServiceResponseAsync(textContentType, textContent).toBlocking().single().body();
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<DetectedLanguageInner> detectLanguageAsync(String textContentType, String textContent, final ServiceCallback<DetectedLanguageInner> serviceCallback) {
        return ServiceFuture.fromResponse(detectLanguageWithServiceResponseAsync(textContentType, textContent), serviceCallback);
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DetectedLanguageInner object
     */
    public Observable<DetectedLanguageInner> detectLanguageAsync(String textContentType, String textContent) {
        return detectLanguageWithServiceResponseAsync(textContentType, textContent).map(new Func1<ServiceResponse<DetectedLanguageInner>, DetectedLanguageInner>() {
            @Override
            public DetectedLanguageInner call(ServiceResponse<DetectedLanguageInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation will detect the language of given input content. Returns the &lt;a href="http://www-01.sil.org/iso639-3/codes.asp"&gt;ISO 639-3 code&lt;/a&gt; for the predominant language comprising the submitted text. Over 110 languages supported.
     *
     * @param textContentType The content type. Possible values include: 'text/plain', 'text/html', 'text/xml', 'text/markdown'
     * @param textContent Content to screen.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DetectedLanguageInner object
     */
    public Observable<ServiceResponse<DetectedLanguageInner>> detectLanguageWithServiceResponseAsync(String textContentType, String textContent) {
        if (this.client.baseUrl() == null) {
            throw new IllegalArgumentException("Parameter this.client.baseUrl() is required and cannot be null.");
        }
        if (textContentType == null) {
            throw new IllegalArgumentException("Parameter textContentType is required and cannot be null.");
        }
        if (textContent == null) {
            throw new IllegalArgumentException("Parameter textContent is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{baseUrl}", this.client.baseUrl());
        return service.detectLanguage(textContentType, textContent, this.client.acceptLanguage(), parameterizedHost, this.client.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<DetectedLanguageInner>>>() {
                @Override
                public Observable<ServiceResponse<DetectedLanguageInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<DetectedLanguageInner> clientResponse = detectLanguageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<DetectedLanguageInner> detectLanguageDelegate(Response<ResponseBody> response) throws APIErrorException, IOException, IllegalArgumentException {
        return this.client.restClient().responseBuilderFactory().<DetectedLanguageInner, APIErrorException>newInstance(this.client.serializerAdapter())
                .register(200, new TypeToken<DetectedLanguageInner>() { }.getType())
                .registerError(APIErrorException.class)
                .build(response);
    }

}
