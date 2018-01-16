/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.spellcheck.implementation;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.cognitiveservices.spellcheck.ActionType;
import com.microsoft.azure.cognitiveservices.spellcheck.ErrorResponseException;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;

/**
 * Initializes a new instance of the SpellCheckAPIImpl class.
 */
public class SpellCheckAPIImpl extends AzureServiceClient {
    /** The Retrofit service to perform REST calls. */
    private SpellCheckAPIService service;
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets Gets or sets the preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    public SpellCheckAPIImpl withAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /** Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    public SpellCheckAPIImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
        return this;
    }

    /** When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean generateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    public SpellCheckAPIImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * Initializes an instance of SpellCheckAPI client.
     *
     * @param credentials the management credentials for Azure
     */
    public SpellCheckAPIImpl(ServiceClientCredentials credentials) {
        this("https://api.cognitive.microsoft.com/bing/v7.0", credentials);
    }

    /**
     * Initializes an instance of SpellCheckAPI client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public SpellCheckAPIImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of SpellCheckAPI client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public SpellCheckAPIImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.azureClient = new AzureClient(this);
        initializeService();
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("%s (%s, %s)", super.userAgent(), "SpellCheckAPI", "1.0");
    }

    private void initializeService() {
        service = restClient().retrofit().create(SpellCheckAPIService.class);
    }

    /**
     * The interface defining all the services for SpellCheckAPI to be
     * used by Retrofit to perform actually REST calls.
     */
    interface SpellCheckAPIService {
        @FormUrlEncoded
        @POST("spellcheck")
        Observable<Response<ResponseBody>> spellChecker(@Header("X-BingApis-SDK") String xBingApisSDK, @Header("Accept-Language") String acceptLanguage, @Header("Pragma") String pragma, @Header("User-Agent") String userAgent, @Header("X-MSEdge-ClientID") String clientId, @Header("X-MSEdge-ClientIP") String clientIp, @Header("X-Search-Location") String location, @Query("ActionType") ActionType actionType, @Query("AppName") String appName, @Query("cc") String countryCode, @Query("ClientMachineName") String clientMachineName, @Query("DocId") String docId, @Query("mkt") String market, @Query("SessionId") String sessionId, @Query("SetLang") String setLang, @Query("UserId") String userId, @Field("Mode") String mode, @Field("PreContextText") String preContextText, @Field("PostContextText") String postContextText, @Field("Text") String text);

    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the SpellCheckInner object if successful.
     */
    public SpellCheckInner spellChecker() {
        return spellCheckerWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<SpellCheckInner> spellCheckerAsync(final ServiceCallback<SpellCheckInner> serviceCallback) {
        return ServiceFuture.fromResponse(spellCheckerWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SpellCheckInner object
     */
    public Observable<SpellCheckInner> spellCheckerAsync() {
        return spellCheckerWithServiceResponseAsync().map(new Func1<ServiceResponse<SpellCheckInner>, SpellCheckInner>() {
            @Override
            public SpellCheckInner call(ServiceResponse<SpellCheckInner> response) {
                return response.body();
            }
        });
    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SpellCheckInner object
     */
    public Observable<ServiceResponse<SpellCheckInner>> spellCheckerWithServiceResponseAsync() {
        final String xBingApisSDK = "true";
        final String acceptLanguage = null;
        final String pragma = null;
        final String clientId = null;
        final String clientIp = null;
        final String location = null;
        final ActionType actionType = null;
        final String appName = null;
        final String countryCode = null;
        final String clientMachineName = null;
        final String docId = null;
        final String market = null;
        final String sessionId = null;
        final String setLang = null;
        final String userId = null;
        final String mode = null;
        final String preContextText = null;
        final String postContextText = null;
        final String text = null;
        return service.spellChecker(xBingApisSDK, acceptLanguage, pragma, this.userAgent(), clientId, clientIp, location, actionType, appName, countryCode, clientMachineName, docId, market, sessionId, setLang, userId, mode, preContextText, postContextText, text)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<SpellCheckInner>>>() {
                @Override
                public Observable<ServiceResponse<SpellCheckInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<SpellCheckInner> clientResponse = spellCheckerDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @param acceptLanguage A comma-delimited list of one or more languages to use for user interface strings. The list is in decreasing order of preference. For additional information, including expected format, see [RFC2616](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html). This header and the setLang query parameter are mutually exclusive; do not specify both. If you set this header, you must also specify the cc query parameter. Bing will use the first supported language it finds from the list, and combine that language with the cc parameter value to determine the market to return results for. If the list does not include a supported language, Bing will find the closest language and market that supports the request, and may use an aggregated or default market for the results instead of a specified one. You should use this header and the cc query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. A user interface string is a string that's used as a label in a user interface. There are very few user interface strings in the JSON response objects. Any links in the response objects to Bing.com properties will apply the specified language.
     * @param pragma By default, Bing returns cached content, if available. To prevent Bing from returning cached content, set the Pragma header to no-cache (for example, Pragma: no-cache).
     * @param clientId Bing uses this header to provide users with consistent behavior across Bing API calls. Bing often flights new features and improvements, and it uses the client ID as a key for assigning traffic on different flights. If you do not use the same client ID for a user across multiple requests, then Bing may assign the user to multiple conflicting flights. Being assigned to multiple conflicting flights can lead to an inconsistent user experience. For example, if the second request has a different flight assignment than the first, the experience may be unexpected. Also, Bing can use the client ID to tailor web results to that client ID’s search history, providing a richer experience for the user. Bing also uses this header to help improve result rankings by analyzing the activity generated by a client ID. The relevance improvements help with better quality of results delivered by Bing APIs and in turn enables higher click-through rates for the API consumer. IMPORTANT: Although optional, you should consider this header required. Persisting the client ID across multiple requests for the same end user and device combination enables 1) the API consumer to receive a consistent user experience, and 2) higher click-through rates via better quality of results from the Bing APIs. Each user that uses your application on the device must have a unique, Bing generated client ID. If you do not include this header in the request, Bing generates an ID and returns it in the X-MSEdge-ClientID response header. The only time that you should NOT include this header in a request is the first time the user uses your app on that device. Use the client ID for each Bing API request that your app makes for this user on the device. Persist the client ID. To persist the ID in a browser app, use a persistent HTTP cookie to ensure the ID is used across all sessions. Do not use a session cookie. For other apps such as mobile apps, use the device's persistent storage to persist the ID. The next time the user uses your app on that device, get the client ID that you persisted. Bing responses may or may not include this header. If the response includes this header, capture the client ID and use it for all subsequent Bing requests for the user on that device. If you include the X-MSEdge-ClientID, you must not include cookies in the request.
     * @param clientIp The IPv4 or IPv6 address of the client device. The IP address is used to discover the user's location. Bing uses the location information to determine safe search behavior. Although optional, you are encouraged to always specify this header and the X-Search-Location header. Do not obfuscate the address (for example, by changing the last octet to 0). Obfuscating the address results in the location not being anywhere near the device's actual location, which may result in Bing serving erroneous results.
     * @param location A semicolon-delimited list of key/value pairs that describe the client's geographical location. Bing uses the location information to determine safe search behavior and to return relevant local content. Specify the key/value pair as &lt;key&gt;:&lt;value&gt;. The following are the keys that you use to specify the user's location. lat (required): The latitude of the client's location, in degrees. The latitude must be greater than or equal to -90.0 and less than or equal to +90.0. Negative values indicate southern latitudes and positive values indicate northern latitudes. long (required): The longitude of the client's location, in degrees. The longitude must be greater than or equal to -180.0 and less than or equal to +180.0. Negative values indicate western longitudes and positive values indicate eastern longitudes. re (required): The radius, in meters, which specifies the horizontal accuracy of the coordinates. Pass the value returned by the device's location service. Typical values might be 22m for GPS/Wi-Fi, 380m for cell tower triangulation, and 18,000m for reverse IP lookup. ts (optional): The UTC UNIX timestamp of when the client was at the location. (The UNIX timestamp is the number of seconds since January 1, 1970.) head (optional): The client's relative heading or direction of travel. Specify the direction of travel as degrees from 0 through 360, counting clockwise relative to true north. Specify this key only if the sp key is nonzero. sp (optional): The horizontal velocity (speed), in meters per second, that the client device is traveling. alt (optional): The altitude of the client device, in meters. are (optional): The radius, in meters, that specifies the vertical accuracy of the coordinates. Specify this key only if you specify the alt key. Although many of the keys are optional, the more information that you provide, the more accurate the location results are. Although optional, you are encouraged to always specify the user's geographical location. Providing the location is especially important if the client's IP address does not accurately reflect the user's physical location (for example, if the client uses VPN). For optimal results, you should include this header and the  X-Search-ClientIP header, but at a minimum, you should include this header.
     * @param actionType A string that's used by logging to determine whether the request is coming from an interactive session or a page load. The following are the possible values. 1) Edit—The request is from an interactive session 2) Load—The request is from a page load. Possible values include: 'Edit', 'Load'
     * @param appName The unique name of your app. The name must be known by Bing. Do not include this parameter unless you have previously contacted Bing to get a unique app name. To get a unique name, contact your Bing Business Development manager.
     * @param countryCode A 2-character country code of the country where the results come from. This API supports only the United States market. If you specify this query parameter, it must be set to us. If you set this parameter, you must also specify the Accept-Language header. Bing uses the first supported language it finds from the languages list, and combine that language with the country code that you specify to determine the market to return results for. If the languages list does not include a supported language, Bing finds the closest language and market that supports the request, or it may use an aggregated or default market for the results instead of a specified one. You should use this query parameter and the Accept-Language query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. This parameter and the mkt query parameter are mutually exclusive—do not specify both.
     * @param clientMachineName A unique name of the device that the request is being made from. Generate a unique value for each device (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param docId A unique ID that identifies the document that the text belongs to. Generate a unique value for each document (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param market The market where the results come from. You are strongly encouraged to always specify the market, if known. Specifying the market helps Bing route the request and return an appropriate and optimal response. This parameter and the cc query parameter are mutually exclusive—do not specify both.
     * @param sessionId A unique ID that identifies this user session. Generate a unique value for each user session (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections
     * @param setLang The language to use for user interface strings. Specify the language using the ISO 639-1 2-letter language code. For example, the language code for English is EN. The default is EN (English). Although optional, you should always specify the language. Typically, you set setLang to the same language specified by mkt unless the user wants the user interface strings displayed in a different language. This parameter and the Accept-Language header are mutually exclusive—do not specify both. A user interface string is a string that's used as a label in a user interface. There are few user interface strings in the JSON response objects. Also, any links to Bing.com properties in the response objects apply the specified language.
     * @param userId A unique ID that identifies the user. Generate a unique value for each user (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param mode The type of spelling and grammar checks to perform. The following are the possible values (the values are case insensitive). The default is Proof. 1) Proof—Finds most spelling and grammar mistakes. 2) Spell—Finds most spelling mistakes but does not find some of the grammar errors that Proof catches (for example, capitalization and repeated words). Possible values include: 'Proof', 'Spell'
     * @param preContextText A string that gives context to the text string. For example, the text string petal is valid. However, if you set preContextText to bike, the context changes and the text string becomes not valid. In this case, the API suggests that you change petal to pedal (as in bike pedal). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param postContextText A string that gives context to the text string. For example, the text string read is valid. However, if you set postContextText to carpet, the context changes and the text string becomes not valid. In this case, the API suggests that you change read to red (as in red carpet). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param text The text string to check for spelling and grammar errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request. Because of the query string length limit, you'll typically use a POST request unless you're checking only short strings.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the SpellCheckInner object if successful.
     */
    public SpellCheckInner spellChecker(String acceptLanguage, String pragma, String clientId, String clientIp, String location, ActionType actionType, String appName, String countryCode, String clientMachineName, String docId, String market, String sessionId, String setLang, String userId, String mode, String preContextText, String postContextText, String text) {
        return spellCheckerWithServiceResponseAsync(acceptLanguage, pragma, clientId, clientIp, location, actionType, appName, countryCode, clientMachineName, docId, market, sessionId, setLang, userId, mode, preContextText, postContextText, text).toBlocking().single().body();
    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @param acceptLanguage A comma-delimited list of one or more languages to use for user interface strings. The list is in decreasing order of preference. For additional information, including expected format, see [RFC2616](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html). This header and the setLang query parameter are mutually exclusive; do not specify both. If you set this header, you must also specify the cc query parameter. Bing will use the first supported language it finds from the list, and combine that language with the cc parameter value to determine the market to return results for. If the list does not include a supported language, Bing will find the closest language and market that supports the request, and may use an aggregated or default market for the results instead of a specified one. You should use this header and the cc query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. A user interface string is a string that's used as a label in a user interface. There are very few user interface strings in the JSON response objects. Any links in the response objects to Bing.com properties will apply the specified language.
     * @param pragma By default, Bing returns cached content, if available. To prevent Bing from returning cached content, set the Pragma header to no-cache (for example, Pragma: no-cache).
     * @param clientId Bing uses this header to provide users with consistent behavior across Bing API calls. Bing often flights new features and improvements, and it uses the client ID as a key for assigning traffic on different flights. If you do not use the same client ID for a user across multiple requests, then Bing may assign the user to multiple conflicting flights. Being assigned to multiple conflicting flights can lead to an inconsistent user experience. For example, if the second request has a different flight assignment than the first, the experience may be unexpected. Also, Bing can use the client ID to tailor web results to that client ID’s search history, providing a richer experience for the user. Bing also uses this header to help improve result rankings by analyzing the activity generated by a client ID. The relevance improvements help with better quality of results delivered by Bing APIs and in turn enables higher click-through rates for the API consumer. IMPORTANT: Although optional, you should consider this header required. Persisting the client ID across multiple requests for the same end user and device combination enables 1) the API consumer to receive a consistent user experience, and 2) higher click-through rates via better quality of results from the Bing APIs. Each user that uses your application on the device must have a unique, Bing generated client ID. If you do not include this header in the request, Bing generates an ID and returns it in the X-MSEdge-ClientID response header. The only time that you should NOT include this header in a request is the first time the user uses your app on that device. Use the client ID for each Bing API request that your app makes for this user on the device. Persist the client ID. To persist the ID in a browser app, use a persistent HTTP cookie to ensure the ID is used across all sessions. Do not use a session cookie. For other apps such as mobile apps, use the device's persistent storage to persist the ID. The next time the user uses your app on that device, get the client ID that you persisted. Bing responses may or may not include this header. If the response includes this header, capture the client ID and use it for all subsequent Bing requests for the user on that device. If you include the X-MSEdge-ClientID, you must not include cookies in the request.
     * @param clientIp The IPv4 or IPv6 address of the client device. The IP address is used to discover the user's location. Bing uses the location information to determine safe search behavior. Although optional, you are encouraged to always specify this header and the X-Search-Location header. Do not obfuscate the address (for example, by changing the last octet to 0). Obfuscating the address results in the location not being anywhere near the device's actual location, which may result in Bing serving erroneous results.
     * @param location A semicolon-delimited list of key/value pairs that describe the client's geographical location. Bing uses the location information to determine safe search behavior and to return relevant local content. Specify the key/value pair as &lt;key&gt;:&lt;value&gt;. The following are the keys that you use to specify the user's location. lat (required): The latitude of the client's location, in degrees. The latitude must be greater than or equal to -90.0 and less than or equal to +90.0. Negative values indicate southern latitudes and positive values indicate northern latitudes. long (required): The longitude of the client's location, in degrees. The longitude must be greater than or equal to -180.0 and less than or equal to +180.0. Negative values indicate western longitudes and positive values indicate eastern longitudes. re (required): The radius, in meters, which specifies the horizontal accuracy of the coordinates. Pass the value returned by the device's location service. Typical values might be 22m for GPS/Wi-Fi, 380m for cell tower triangulation, and 18,000m for reverse IP lookup. ts (optional): The UTC UNIX timestamp of when the client was at the location. (The UNIX timestamp is the number of seconds since January 1, 1970.) head (optional): The client's relative heading or direction of travel. Specify the direction of travel as degrees from 0 through 360, counting clockwise relative to true north. Specify this key only if the sp key is nonzero. sp (optional): The horizontal velocity (speed), in meters per second, that the client device is traveling. alt (optional): The altitude of the client device, in meters. are (optional): The radius, in meters, that specifies the vertical accuracy of the coordinates. Specify this key only if you specify the alt key. Although many of the keys are optional, the more information that you provide, the more accurate the location results are. Although optional, you are encouraged to always specify the user's geographical location. Providing the location is especially important if the client's IP address does not accurately reflect the user's physical location (for example, if the client uses VPN). For optimal results, you should include this header and the  X-Search-ClientIP header, but at a minimum, you should include this header.
     * @param actionType A string that's used by logging to determine whether the request is coming from an interactive session or a page load. The following are the possible values. 1) Edit—The request is from an interactive session 2) Load—The request is from a page load. Possible values include: 'Edit', 'Load'
     * @param appName The unique name of your app. The name must be known by Bing. Do not include this parameter unless you have previously contacted Bing to get a unique app name. To get a unique name, contact your Bing Business Development manager.
     * @param countryCode A 2-character country code of the country where the results come from. This API supports only the United States market. If you specify this query parameter, it must be set to us. If you set this parameter, you must also specify the Accept-Language header. Bing uses the first supported language it finds from the languages list, and combine that language with the country code that you specify to determine the market to return results for. If the languages list does not include a supported language, Bing finds the closest language and market that supports the request, or it may use an aggregated or default market for the results instead of a specified one. You should use this query parameter and the Accept-Language query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. This parameter and the mkt query parameter are mutually exclusive—do not specify both.
     * @param clientMachineName A unique name of the device that the request is being made from. Generate a unique value for each device (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param docId A unique ID that identifies the document that the text belongs to. Generate a unique value for each document (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param market The market where the results come from. You are strongly encouraged to always specify the market, if known. Specifying the market helps Bing route the request and return an appropriate and optimal response. This parameter and the cc query parameter are mutually exclusive—do not specify both.
     * @param sessionId A unique ID that identifies this user session. Generate a unique value for each user session (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections
     * @param setLang The language to use for user interface strings. Specify the language using the ISO 639-1 2-letter language code. For example, the language code for English is EN. The default is EN (English). Although optional, you should always specify the language. Typically, you set setLang to the same language specified by mkt unless the user wants the user interface strings displayed in a different language. This parameter and the Accept-Language header are mutually exclusive—do not specify both. A user interface string is a string that's used as a label in a user interface. There are few user interface strings in the JSON response objects. Also, any links to Bing.com properties in the response objects apply the specified language.
     * @param userId A unique ID that identifies the user. Generate a unique value for each user (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param mode The type of spelling and grammar checks to perform. The following are the possible values (the values are case insensitive). The default is Proof. 1) Proof—Finds most spelling and grammar mistakes. 2) Spell—Finds most spelling mistakes but does not find some of the grammar errors that Proof catches (for example, capitalization and repeated words). Possible values include: 'Proof', 'Spell'
     * @param preContextText A string that gives context to the text string. For example, the text string petal is valid. However, if you set preContextText to bike, the context changes and the text string becomes not valid. In this case, the API suggests that you change petal to pedal (as in bike pedal). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param postContextText A string that gives context to the text string. For example, the text string read is valid. However, if you set postContextText to carpet, the context changes and the text string becomes not valid. In this case, the API suggests that you change read to red (as in red carpet). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param text The text string to check for spelling and grammar errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request. Because of the query string length limit, you'll typically use a POST request unless you're checking only short strings.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<SpellCheckInner> spellCheckerAsync(String acceptLanguage, String pragma, String clientId, String clientIp, String location, ActionType actionType, String appName, String countryCode, String clientMachineName, String docId, String market, String sessionId, String setLang, String userId, String mode, String preContextText, String postContextText, String text, final ServiceCallback<SpellCheckInner> serviceCallback) {
        return ServiceFuture.fromResponse(spellCheckerWithServiceResponseAsync(acceptLanguage, pragma, clientId, clientIp, location, actionType, appName, countryCode, clientMachineName, docId, market, sessionId, setLang, userId, mode, preContextText, postContextText, text), serviceCallback);
    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @param acceptLanguage A comma-delimited list of one or more languages to use for user interface strings. The list is in decreasing order of preference. For additional information, including expected format, see [RFC2616](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html). This header and the setLang query parameter are mutually exclusive; do not specify both. If you set this header, you must also specify the cc query parameter. Bing will use the first supported language it finds from the list, and combine that language with the cc parameter value to determine the market to return results for. If the list does not include a supported language, Bing will find the closest language and market that supports the request, and may use an aggregated or default market for the results instead of a specified one. You should use this header and the cc query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. A user interface string is a string that's used as a label in a user interface. There are very few user interface strings in the JSON response objects. Any links in the response objects to Bing.com properties will apply the specified language.
     * @param pragma By default, Bing returns cached content, if available. To prevent Bing from returning cached content, set the Pragma header to no-cache (for example, Pragma: no-cache).
     * @param clientId Bing uses this header to provide users with consistent behavior across Bing API calls. Bing often flights new features and improvements, and it uses the client ID as a key for assigning traffic on different flights. If you do not use the same client ID for a user across multiple requests, then Bing may assign the user to multiple conflicting flights. Being assigned to multiple conflicting flights can lead to an inconsistent user experience. For example, if the second request has a different flight assignment than the first, the experience may be unexpected. Also, Bing can use the client ID to tailor web results to that client ID’s search history, providing a richer experience for the user. Bing also uses this header to help improve result rankings by analyzing the activity generated by a client ID. The relevance improvements help with better quality of results delivered by Bing APIs and in turn enables higher click-through rates for the API consumer. IMPORTANT: Although optional, you should consider this header required. Persisting the client ID across multiple requests for the same end user and device combination enables 1) the API consumer to receive a consistent user experience, and 2) higher click-through rates via better quality of results from the Bing APIs. Each user that uses your application on the device must have a unique, Bing generated client ID. If you do not include this header in the request, Bing generates an ID and returns it in the X-MSEdge-ClientID response header. The only time that you should NOT include this header in a request is the first time the user uses your app on that device. Use the client ID for each Bing API request that your app makes for this user on the device. Persist the client ID. To persist the ID in a browser app, use a persistent HTTP cookie to ensure the ID is used across all sessions. Do not use a session cookie. For other apps such as mobile apps, use the device's persistent storage to persist the ID. The next time the user uses your app on that device, get the client ID that you persisted. Bing responses may or may not include this header. If the response includes this header, capture the client ID and use it for all subsequent Bing requests for the user on that device. If you include the X-MSEdge-ClientID, you must not include cookies in the request.
     * @param clientIp The IPv4 or IPv6 address of the client device. The IP address is used to discover the user's location. Bing uses the location information to determine safe search behavior. Although optional, you are encouraged to always specify this header and the X-Search-Location header. Do not obfuscate the address (for example, by changing the last octet to 0). Obfuscating the address results in the location not being anywhere near the device's actual location, which may result in Bing serving erroneous results.
     * @param location A semicolon-delimited list of key/value pairs that describe the client's geographical location. Bing uses the location information to determine safe search behavior and to return relevant local content. Specify the key/value pair as &lt;key&gt;:&lt;value&gt;. The following are the keys that you use to specify the user's location. lat (required): The latitude of the client's location, in degrees. The latitude must be greater than or equal to -90.0 and less than or equal to +90.0. Negative values indicate southern latitudes and positive values indicate northern latitudes. long (required): The longitude of the client's location, in degrees. The longitude must be greater than or equal to -180.0 and less than or equal to +180.0. Negative values indicate western longitudes and positive values indicate eastern longitudes. re (required): The radius, in meters, which specifies the horizontal accuracy of the coordinates. Pass the value returned by the device's location service. Typical values might be 22m for GPS/Wi-Fi, 380m for cell tower triangulation, and 18,000m for reverse IP lookup. ts (optional): The UTC UNIX timestamp of when the client was at the location. (The UNIX timestamp is the number of seconds since January 1, 1970.) head (optional): The client's relative heading or direction of travel. Specify the direction of travel as degrees from 0 through 360, counting clockwise relative to true north. Specify this key only if the sp key is nonzero. sp (optional): The horizontal velocity (speed), in meters per second, that the client device is traveling. alt (optional): The altitude of the client device, in meters. are (optional): The radius, in meters, that specifies the vertical accuracy of the coordinates. Specify this key only if you specify the alt key. Although many of the keys are optional, the more information that you provide, the more accurate the location results are. Although optional, you are encouraged to always specify the user's geographical location. Providing the location is especially important if the client's IP address does not accurately reflect the user's physical location (for example, if the client uses VPN). For optimal results, you should include this header and the  X-Search-ClientIP header, but at a minimum, you should include this header.
     * @param actionType A string that's used by logging to determine whether the request is coming from an interactive session or a page load. The following are the possible values. 1) Edit—The request is from an interactive session 2) Load—The request is from a page load. Possible values include: 'Edit', 'Load'
     * @param appName The unique name of your app. The name must be known by Bing. Do not include this parameter unless you have previously contacted Bing to get a unique app name. To get a unique name, contact your Bing Business Development manager.
     * @param countryCode A 2-character country code of the country where the results come from. This API supports only the United States market. If you specify this query parameter, it must be set to us. If you set this parameter, you must also specify the Accept-Language header. Bing uses the first supported language it finds from the languages list, and combine that language with the country code that you specify to determine the market to return results for. If the languages list does not include a supported language, Bing finds the closest language and market that supports the request, or it may use an aggregated or default market for the results instead of a specified one. You should use this query parameter and the Accept-Language query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. This parameter and the mkt query parameter are mutually exclusive—do not specify both.
     * @param clientMachineName A unique name of the device that the request is being made from. Generate a unique value for each device (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param docId A unique ID that identifies the document that the text belongs to. Generate a unique value for each document (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param market The market where the results come from. You are strongly encouraged to always specify the market, if known. Specifying the market helps Bing route the request and return an appropriate and optimal response. This parameter and the cc query parameter are mutually exclusive—do not specify both.
     * @param sessionId A unique ID that identifies this user session. Generate a unique value for each user session (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections
     * @param setLang The language to use for user interface strings. Specify the language using the ISO 639-1 2-letter language code. For example, the language code for English is EN. The default is EN (English). Although optional, you should always specify the language. Typically, you set setLang to the same language specified by mkt unless the user wants the user interface strings displayed in a different language. This parameter and the Accept-Language header are mutually exclusive—do not specify both. A user interface string is a string that's used as a label in a user interface. There are few user interface strings in the JSON response objects. Also, any links to Bing.com properties in the response objects apply the specified language.
     * @param userId A unique ID that identifies the user. Generate a unique value for each user (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param mode The type of spelling and grammar checks to perform. The following are the possible values (the values are case insensitive). The default is Proof. 1) Proof—Finds most spelling and grammar mistakes. 2) Spell—Finds most spelling mistakes but does not find some of the grammar errors that Proof catches (for example, capitalization and repeated words). Possible values include: 'Proof', 'Spell'
     * @param preContextText A string that gives context to the text string. For example, the text string petal is valid. However, if you set preContextText to bike, the context changes and the text string becomes not valid. In this case, the API suggests that you change petal to pedal (as in bike pedal). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param postContextText A string that gives context to the text string. For example, the text string read is valid. However, if you set postContextText to carpet, the context changes and the text string becomes not valid. In this case, the API suggests that you change read to red (as in red carpet). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param text The text string to check for spelling and grammar errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request. Because of the query string length limit, you'll typically use a POST request unless you're checking only short strings.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SpellCheckInner object
     */
    public Observable<SpellCheckInner> spellCheckerAsync(String acceptLanguage, String pragma, String clientId, String clientIp, String location, ActionType actionType, String appName, String countryCode, String clientMachineName, String docId, String market, String sessionId, String setLang, String userId, String mode, String preContextText, String postContextText, String text) {
        return spellCheckerWithServiceResponseAsync(acceptLanguage, pragma, clientId, clientIp, location, actionType, appName, countryCode, clientMachineName, docId, market, sessionId, setLang, userId, mode, preContextText, postContextText, text).map(new Func1<ServiceResponse<SpellCheckInner>, SpellCheckInner>() {
            @Override
            public SpellCheckInner call(ServiceResponse<SpellCheckInner> response) {
                return response.body();
            }
        });
    }

    /**
     * The Bing Spell Check API lets you perform contextual grammar and spell checking. Bing has developed a web-based spell-checker that leverages machine learning and statistical machine translation to dynamically train a constantly evolving and highly contextual algorithm. The spell-checker is based on a massive corpus of web searches and documents.
     *
     * @param acceptLanguage A comma-delimited list of one or more languages to use for user interface strings. The list is in decreasing order of preference. For additional information, including expected format, see [RFC2616](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html). This header and the setLang query parameter are mutually exclusive; do not specify both. If you set this header, you must also specify the cc query parameter. Bing will use the first supported language it finds from the list, and combine that language with the cc parameter value to determine the market to return results for. If the list does not include a supported language, Bing will find the closest language and market that supports the request, and may use an aggregated or default market for the results instead of a specified one. You should use this header and the cc query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. A user interface string is a string that's used as a label in a user interface. There are very few user interface strings in the JSON response objects. Any links in the response objects to Bing.com properties will apply the specified language.
     * @param pragma By default, Bing returns cached content, if available. To prevent Bing from returning cached content, set the Pragma header to no-cache (for example, Pragma: no-cache).
     * @param clientId Bing uses this header to provide users with consistent behavior across Bing API calls. Bing often flights new features and improvements, and it uses the client ID as a key for assigning traffic on different flights. If you do not use the same client ID for a user across multiple requests, then Bing may assign the user to multiple conflicting flights. Being assigned to multiple conflicting flights can lead to an inconsistent user experience. For example, if the second request has a different flight assignment than the first, the experience may be unexpected. Also, Bing can use the client ID to tailor web results to that client ID’s search history, providing a richer experience for the user. Bing also uses this header to help improve result rankings by analyzing the activity generated by a client ID. The relevance improvements help with better quality of results delivered by Bing APIs and in turn enables higher click-through rates for the API consumer. IMPORTANT: Although optional, you should consider this header required. Persisting the client ID across multiple requests for the same end user and device combination enables 1) the API consumer to receive a consistent user experience, and 2) higher click-through rates via better quality of results from the Bing APIs. Each user that uses your application on the device must have a unique, Bing generated client ID. If you do not include this header in the request, Bing generates an ID and returns it in the X-MSEdge-ClientID response header. The only time that you should NOT include this header in a request is the first time the user uses your app on that device. Use the client ID for each Bing API request that your app makes for this user on the device. Persist the client ID. To persist the ID in a browser app, use a persistent HTTP cookie to ensure the ID is used across all sessions. Do not use a session cookie. For other apps such as mobile apps, use the device's persistent storage to persist the ID. The next time the user uses your app on that device, get the client ID that you persisted. Bing responses may or may not include this header. If the response includes this header, capture the client ID and use it for all subsequent Bing requests for the user on that device. If you include the X-MSEdge-ClientID, you must not include cookies in the request.
     * @param clientIp The IPv4 or IPv6 address of the client device. The IP address is used to discover the user's location. Bing uses the location information to determine safe search behavior. Although optional, you are encouraged to always specify this header and the X-Search-Location header. Do not obfuscate the address (for example, by changing the last octet to 0). Obfuscating the address results in the location not being anywhere near the device's actual location, which may result in Bing serving erroneous results.
     * @param location A semicolon-delimited list of key/value pairs that describe the client's geographical location. Bing uses the location information to determine safe search behavior and to return relevant local content. Specify the key/value pair as &lt;key&gt;:&lt;value&gt;. The following are the keys that you use to specify the user's location. lat (required): The latitude of the client's location, in degrees. The latitude must be greater than or equal to -90.0 and less than or equal to +90.0. Negative values indicate southern latitudes and positive values indicate northern latitudes. long (required): The longitude of the client's location, in degrees. The longitude must be greater than or equal to -180.0 and less than or equal to +180.0. Negative values indicate western longitudes and positive values indicate eastern longitudes. re (required): The radius, in meters, which specifies the horizontal accuracy of the coordinates. Pass the value returned by the device's location service. Typical values might be 22m for GPS/Wi-Fi, 380m for cell tower triangulation, and 18,000m for reverse IP lookup. ts (optional): The UTC UNIX timestamp of when the client was at the location. (The UNIX timestamp is the number of seconds since January 1, 1970.) head (optional): The client's relative heading or direction of travel. Specify the direction of travel as degrees from 0 through 360, counting clockwise relative to true north. Specify this key only if the sp key is nonzero. sp (optional): The horizontal velocity (speed), in meters per second, that the client device is traveling. alt (optional): The altitude of the client device, in meters. are (optional): The radius, in meters, that specifies the vertical accuracy of the coordinates. Specify this key only if you specify the alt key. Although many of the keys are optional, the more information that you provide, the more accurate the location results are. Although optional, you are encouraged to always specify the user's geographical location. Providing the location is especially important if the client's IP address does not accurately reflect the user's physical location (for example, if the client uses VPN). For optimal results, you should include this header and the  X-Search-ClientIP header, but at a minimum, you should include this header.
     * @param actionType A string that's used by logging to determine whether the request is coming from an interactive session or a page load. The following are the possible values. 1) Edit—The request is from an interactive session 2) Load—The request is from a page load. Possible values include: 'Edit', 'Load'
     * @param appName The unique name of your app. The name must be known by Bing. Do not include this parameter unless you have previously contacted Bing to get a unique app name. To get a unique name, contact your Bing Business Development manager.
     * @param countryCode A 2-character country code of the country where the results come from. This API supports only the United States market. If you specify this query parameter, it must be set to us. If you set this parameter, you must also specify the Accept-Language header. Bing uses the first supported language it finds from the languages list, and combine that language with the country code that you specify to determine the market to return results for. If the languages list does not include a supported language, Bing finds the closest language and market that supports the request, or it may use an aggregated or default market for the results instead of a specified one. You should use this query parameter and the Accept-Language query parameter only if you specify multiple languages; otherwise, you should use the mkt and setLang query parameters. This parameter and the mkt query parameter are mutually exclusive—do not specify both.
     * @param clientMachineName A unique name of the device that the request is being made from. Generate a unique value for each device (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param docId A unique ID that identifies the document that the text belongs to. Generate a unique value for each document (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param market The market where the results come from. You are strongly encouraged to always specify the market, if known. Specifying the market helps Bing route the request and return an appropriate and optimal response. This parameter and the cc query parameter are mutually exclusive—do not specify both.
     * @param sessionId A unique ID that identifies this user session. Generate a unique value for each user session (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections
     * @param setLang The language to use for user interface strings. Specify the language using the ISO 639-1 2-letter language code. For example, the language code for English is EN. The default is EN (English). Although optional, you should always specify the language. Typically, you set setLang to the same language specified by mkt unless the user wants the user interface strings displayed in a different language. This parameter and the Accept-Language header are mutually exclusive—do not specify both. A user interface string is a string that's used as a label in a user interface. There are few user interface strings in the JSON response objects. Also, any links to Bing.com properties in the response objects apply the specified language.
     * @param userId A unique ID that identifies the user. Generate a unique value for each user (the value is unimportant). The service uses the ID to help debug issues and improve the quality of corrections.
     * @param mode The type of spelling and grammar checks to perform. The following are the possible values (the values are case insensitive). The default is Proof. 1) Proof—Finds most spelling and grammar mistakes. 2) Spell—Finds most spelling mistakes but does not find some of the grammar errors that Proof catches (for example, capitalization and repeated words). Possible values include: 'Proof', 'Spell'
     * @param preContextText A string that gives context to the text string. For example, the text string petal is valid. However, if you set preContextText to bike, the context changes and the text string becomes not valid. In this case, the API suggests that you change petal to pedal (as in bike pedal). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param postContextText A string that gives context to the text string. For example, the text string read is valid. However, if you set postContextText to carpet, the context changes and the text string becomes not valid. In this case, the API suggests that you change read to red (as in red carpet). This text is not checked for grammar or spelling errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request.
     * @param text The text string to check for spelling and grammar errors. The combined length of the text string, preContextText string, and postContextText string may not exceed 10,000 characters. You may specify this parameter in the query string of a GET request or in the body of a POST request. Because of the query string length limit, you'll typically use a POST request unless you're checking only short strings.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SpellCheckInner object
     */
    public Observable<ServiceResponse<SpellCheckInner>> spellCheckerWithServiceResponseAsync(String acceptLanguage, String pragma, String clientId, String clientIp, String location, ActionType actionType, String appName, String countryCode, String clientMachineName, String docId, String market, String sessionId, String setLang, String userId, String mode, String preContextText, String postContextText, String text) {
        final String xBingApisSDK = "true";
        return service.spellChecker(xBingApisSDK, acceptLanguage, pragma, this.userAgent(), clientId, clientIp, location, actionType, appName, countryCode, clientMachineName, docId, market, sessionId, setLang, userId, mode, preContextText, postContextText, text)
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<SpellCheckInner>>>() {
                @Override
                public Observable<ServiceResponse<SpellCheckInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<SpellCheckInner> clientResponse = spellCheckerDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<SpellCheckInner> spellCheckerDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException {
        return this.restClient().responseBuilderFactory().<SpellCheckInner, ErrorResponseException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<SpellCheckInner>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

}
