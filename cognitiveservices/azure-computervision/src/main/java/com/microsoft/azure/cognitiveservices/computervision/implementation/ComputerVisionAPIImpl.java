/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.cognitiveservices.computervision.AzureRegions;
import com.microsoft.azure.cognitiveservices.computervision.ComputerVisionErrorException;
import com.microsoft.azure.cognitiveservices.computervision.Details;
import com.microsoft.azure.cognitiveservices.computervision.DomainModels;
import com.microsoft.azure.cognitiveservices.computervision.ImageUrl;
import com.microsoft.azure.cognitiveservices.computervision.Language1;
import com.microsoft.azure.cognitiveservices.computervision.OcrLanguages;
import com.microsoft.azure.cognitiveservices.computervision.VisualFeatureTypes;
import com.microsoft.rest.CollectionFormat;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseWithHeaders;
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
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * Initializes a new instance of the ComputerVisionAPIImpl class.
 */
public class ComputerVisionAPIImpl extends AzureServiceClient {
    /** The Retrofit service to perform REST calls. */
    private ComputerVisionAPIService service;
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'. */
    private AzureRegions azureRegion;

    /**
     * Gets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @return the azureRegion value.
     */
    public AzureRegions azureRegion() {
        return this.azureRegion;
    }

    /**
     * Sets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @param azureRegion the azureRegion value.
     * @return the service client itself
     */
    public ComputerVisionAPIImpl withAzureRegion(AzureRegions azureRegion) {
        this.azureRegion = azureRegion;
        return this;
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
    public ComputerVisionAPIImpl withAcceptLanguage(String acceptLanguage) {
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
    public ComputerVisionAPIImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
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
    public ComputerVisionAPIImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * Initializes an instance of ComputerVisionAPI client.
     *
     * @param credentials the management credentials for Azure
     */
    public ComputerVisionAPIImpl(ServiceClientCredentials credentials) {
        this("https://{AzureRegion}.api.cognitive.microsoft.com/vision/v1.0", credentials);
    }

    /**
     * Initializes an instance of ComputerVisionAPI client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public ComputerVisionAPIImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of ComputerVisionAPI client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public ComputerVisionAPIImpl(RestClient restClient) {
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
        return String.format("%s (%s, %s)", super.userAgent(), "ComputerVisionAPI", "1.0");
    }

    private void initializeService() {
        service = restClient().retrofit().create(ComputerVisionAPIService.class);
    }

    /**
     * The interface defining all the services for ComputerVisionAPI to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ComputerVisionAPIService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI listModels" })
        @GET("models")
        Observable<Response<ResponseBody>> listModels(@Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI analyzeImage" })
        @POST("analyze")
        Observable<Response<ResponseBody>> analyzeImage(@Query("visualFeatures") String visualFeatures, @Query("details") String details, @Query("language") Language1 language, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI generateThumbnail" })
        @POST("generateThumbnail")
        @Streaming
        Observable<Response<ResponseBody>> generateThumbnail(@Query("width") int width, @Query("height") int height, @Query("smartCropping") Boolean smartCropping, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI recognizePrintedText" })
        @POST("ocr")
        Observable<Response<ResponseBody>> recognizePrintedText(@Query("detectOrientation") boolean detectOrientation, @Query("language") OcrLanguages language, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI describeImage" })
        @POST("describe")
        Observable<Response<ResponseBody>> describeImage(@Query("maxCandidates") String maxCandidates, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI tagImage" })
        @POST("tag")
        Observable<Response<ResponseBody>> tagImage(@Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI analyzeImageByDomain" })
        @POST("models/{model}/analyze")
        Observable<Response<ResponseBody>> analyzeImageByDomain(@Path("model") DomainModels model, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI recognizeText" })
        @POST("recognizeText")
        Observable<Response<ResponseBody>> recognizeText(@Query("detectHandwriting") Boolean detectHandwriting, @Header("accept-language") String acceptLanguage, @Body ImageUrl imageUrl, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI getTextOperationResult" })
        @GET("textOperations/{operationId}")
        Observable<Response<ResponseBody>> getTextOperationResult(@Path("operationId") String operationId, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI analyzeImageInStream" })
        @POST("analyze")
        Observable<Response<ResponseBody>> analyzeImageInStream(@Query("visualFeatures") String visualFeatures, @Query("details") String details, @Query("language") String language, @Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI generateThumbnailInStream" })
        @POST("generateThumbnail")
        @Streaming
        Observable<Response<ResponseBody>> generateThumbnailInStream(@Query("width") int width, @Query("height") int height, @Body RequestBody image, @Query("smartCropping") Boolean smartCropping, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI recognizePrintedTextInStream" })
        @POST("ocr")
        Observable<Response<ResponseBody>> recognizePrintedTextInStream(@Query("language") OcrLanguages language, @Query("detectOrientation") boolean detectOrientation, @Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI describeImageInStream" })
        @POST("describe")
        Observable<Response<ResponseBody>> describeImageInStream(@Query("maxCandidates") String maxCandidates, @Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI tagImageInStream" })
        @POST("tag")
        Observable<Response<ResponseBody>> tagImageInStream(@Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI analyzeImageByDomainInStream" })
        @POST("models/{model}/analyze")
        Observable<Response<ResponseBody>> analyzeImageByDomainInStream(@Path("model") String model, @Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/octet-stream", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.computervision.ComputerVisionAPI recognizeTextInStream" })
        @POST("recognizeText")
        Observable<Response<ResponseBody>> recognizeTextInStream(@Query("detectHandwriting") Boolean detectHandwriting, @Body RequestBody image, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ListModelsResultInner object if successful.
     */
    public ListModelsResultInner listModels() {
        return listModelsWithServiceResponseAsync().toBlocking().single().body();
    }

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ListModelsResultInner> listModelsAsync(final ServiceCallback<ListModelsResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(listModelsWithServiceResponseAsync(), serviceCallback);
    }

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ListModelsResultInner object
     */
    public Observable<ListModelsResultInner> listModelsAsync() {
        return listModelsWithServiceResponseAsync().map(new Func1<ServiceResponse<ListModelsResultInner>, ListModelsResultInner>() {
            @Override
            public ListModelsResultInner call(ServiceResponse<ListModelsResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ListModelsResultInner object
     */
    public Observable<ServiceResponse<ListModelsResultInner>> listModelsWithServiceResponseAsync() {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.listModels(this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ListModelsResultInner>>>() {
                @Override
                public Observable<ServiceResponse<ListModelsResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ListModelsResultInner> clientResponse = listModelsDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ListModelsResultInner> listModelsDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<ListModelsResultInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<ListModelsResultInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageAnalysisInner object if successful.
     */
    public ImageAnalysisInner analyzeImage(String url) {
        return analyzeImageWithServiceResponseAsync(url).toBlocking().single().body();
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageAnalysisInner> analyzeImageAsync(String url, final ServiceCallback<ImageAnalysisInner> serviceCallback) {
        return ServiceFuture.fromResponse(analyzeImageWithServiceResponseAsync(url), serviceCallback);
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ImageAnalysisInner> analyzeImageAsync(String url) {
        return analyzeImageWithServiceResponseAsync(url).map(new Func1<ServiceResponse<ImageAnalysisInner>, ImageAnalysisInner>() {
            @Override
            public ImageAnalysisInner call(ServiceResponse<ImageAnalysisInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ServiceResponse<ImageAnalysisInner>> analyzeImageWithServiceResponseAsync(String url) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final List<VisualFeatureTypes> visualFeatures = null;
        final List<Details> details = null;
        final Language1 language = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        String visualFeaturesConverted = this.serializerAdapter().serializeList(visualFeatures, CollectionFormat.CSV);
        String detailsConverted = this.serializerAdapter().serializeList(details, CollectionFormat.CSV);
        return service.analyzeImage(visualFeaturesConverted, detailsConverted, language, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageAnalysisInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageAnalysisInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageAnalysisInner> clientResponse = analyzeImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image.
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageAnalysisInner object if successful.
     */
    public ImageAnalysisInner analyzeImage(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language) {
        return analyzeImageWithServiceResponseAsync(url, visualFeatures, details, language).toBlocking().single().body();
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image.
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageAnalysisInner> analyzeImageAsync(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language, final ServiceCallback<ImageAnalysisInner> serviceCallback) {
        return ServiceFuture.fromResponse(analyzeImageWithServiceResponseAsync(url, visualFeatures, details, language), serviceCallback);
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image.
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ImageAnalysisInner> analyzeImageAsync(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language) {
        return analyzeImageWithServiceResponseAsync(url, visualFeatures, details, language).map(new Func1<ServiceResponse<ImageAnalysisInner>, ImageAnalysisInner>() {
            @Override
            public ImageAnalysisInner call(ServiceResponse<ImageAnalysisInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image.
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ServiceResponse<ImageAnalysisInner>> analyzeImageWithServiceResponseAsync(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        Validator.validate(visualFeatures);
        Validator.validate(details);
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        String visualFeaturesConverted = this.serializerAdapter().serializeList(visualFeatures, CollectionFormat.CSV);
        String detailsConverted = this.serializerAdapter().serializeList(details, CollectionFormat.CSV);
        return service.analyzeImage(visualFeaturesConverted, detailsConverted, language, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageAnalysisInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageAnalysisInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageAnalysisInner> clientResponse = analyzeImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageAnalysisInner> analyzeImageDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<ImageAnalysisInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<ImageAnalysisInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the InputStream object if successful.
     */
    public InputStream generateThumbnail(int width, int height, String url) {
        return generateThumbnailWithServiceResponseAsync(width, height, url).toBlocking().single().body();
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<InputStream> generateThumbnailAsync(int width, int height, String url, final ServiceCallback<InputStream> serviceCallback) {
        return ServiceFuture.fromResponse(generateThumbnailWithServiceResponseAsync(width, height, url), serviceCallback);
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<InputStream> generateThumbnailAsync(int width, int height, String url) {
        return generateThumbnailWithServiceResponseAsync(width, height, url).map(new Func1<ServiceResponse<InputStream>, InputStream>() {
            @Override
            public InputStream call(ServiceResponse<InputStream> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<ServiceResponse<InputStream>> generateThumbnailWithServiceResponseAsync(int width, int height, String url) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final Boolean smartCropping = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.generateThumbnail(width, height, smartCropping, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<InputStream>>>() {
                @Override
                public Observable<ServiceResponse<InputStream>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<InputStream> clientResponse = generateThumbnailDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the InputStream object if successful.
     */
    public InputStream generateThumbnail(int width, int height, String url, Boolean smartCropping) {
        return generateThumbnailWithServiceResponseAsync(width, height, url, smartCropping).toBlocking().single().body();
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<InputStream> generateThumbnailAsync(int width, int height, String url, Boolean smartCropping, final ServiceCallback<InputStream> serviceCallback) {
        return ServiceFuture.fromResponse(generateThumbnailWithServiceResponseAsync(width, height, url, smartCropping), serviceCallback);
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<InputStream> generateThumbnailAsync(int width, int height, String url, Boolean smartCropping) {
        return generateThumbnailWithServiceResponseAsync(width, height, url, smartCropping).map(new Func1<ServiceResponse<InputStream>, InputStream>() {
            @Override
            public InputStream call(ServiceResponse<InputStream> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<ServiceResponse<InputStream>> generateThumbnailWithServiceResponseAsync(int width, int height, String url, Boolean smartCropping) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.generateThumbnail(width, height, smartCropping, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<InputStream>>>() {
                @Override
                public Observable<ServiceResponse<InputStream>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<InputStream> clientResponse = generateThumbnailDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<InputStream> generateThumbnailDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<InputStream, CloudException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResultInner object if successful.
     */
    public OcrResultInner recognizePrintedText(boolean detectOrientation, String url) {
        return recognizePrintedTextWithServiceResponseAsync(detectOrientation, url).toBlocking().single().body();
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OcrResultInner> recognizePrintedTextAsync(boolean detectOrientation, String url, final ServiceCallback<OcrResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(recognizePrintedTextWithServiceResponseAsync(detectOrientation, url), serviceCallback);
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<OcrResultInner> recognizePrintedTextAsync(boolean detectOrientation, String url) {
        return recognizePrintedTextWithServiceResponseAsync(detectOrientation, url).map(new Func1<ServiceResponse<OcrResultInner>, OcrResultInner>() {
            @Override
            public OcrResultInner call(ServiceResponse<OcrResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<ServiceResponse<OcrResultInner>> recognizePrintedTextWithServiceResponseAsync(boolean detectOrientation, String url) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final OcrLanguages language = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.recognizePrintedText(detectOrientation, language, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OcrResultInner>>>() {
                @Override
                public Observable<ServiceResponse<OcrResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OcrResultInner> clientResponse = recognizePrintedTextDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResultInner object if successful.
     */
    public OcrResultInner recognizePrintedText(boolean detectOrientation, String url, OcrLanguages language) {
        return recognizePrintedTextWithServiceResponseAsync(detectOrientation, url, language).toBlocking().single().body();
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OcrResultInner> recognizePrintedTextAsync(boolean detectOrientation, String url, OcrLanguages language, final ServiceCallback<OcrResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(recognizePrintedTextWithServiceResponseAsync(detectOrientation, url, language), serviceCallback);
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<OcrResultInner> recognizePrintedTextAsync(boolean detectOrientation, String url, OcrLanguages language) {
        return recognizePrintedTextWithServiceResponseAsync(detectOrientation, url, language).map(new Func1<ServiceResponse<OcrResultInner>, OcrResultInner>() {
            @Override
            public OcrResultInner call(ServiceResponse<OcrResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<ServiceResponse<OcrResultInner>> recognizePrintedTextWithServiceResponseAsync(boolean detectOrientation, String url, OcrLanguages language) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.recognizePrintedText(detectOrientation, language, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OcrResultInner>>>() {
                @Override
                public Observable<ServiceResponse<OcrResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OcrResultInner> clientResponse = recognizePrintedTextDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<OcrResultInner> recognizePrintedTextDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<OcrResultInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<OcrResultInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescriptionInner object if successful.
     */
    public ImageDescriptionInner describeImage(String url) {
        return describeImageWithServiceResponseAsync(url).toBlocking().single().body();
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageDescriptionInner> describeImageAsync(String url, final ServiceCallback<ImageDescriptionInner> serviceCallback) {
        return ServiceFuture.fromResponse(describeImageWithServiceResponseAsync(url), serviceCallback);
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ImageDescriptionInner> describeImageAsync(String url) {
        return describeImageWithServiceResponseAsync(url).map(new Func1<ServiceResponse<ImageDescriptionInner>, ImageDescriptionInner>() {
            @Override
            public ImageDescriptionInner call(ServiceResponse<ImageDescriptionInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ServiceResponse<ImageDescriptionInner>> describeImageWithServiceResponseAsync(String url) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final String maxCandidates = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.describeImage(maxCandidates, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageDescriptionInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageDescriptionInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageDescriptionInner> clientResponse = describeImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescriptionInner object if successful.
     */
    public ImageDescriptionInner describeImage(String url, String maxCandidates) {
        return describeImageWithServiceResponseAsync(url, maxCandidates).toBlocking().single().body();
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageDescriptionInner> describeImageAsync(String url, String maxCandidates, final ServiceCallback<ImageDescriptionInner> serviceCallback) {
        return ServiceFuture.fromResponse(describeImageWithServiceResponseAsync(url, maxCandidates), serviceCallback);
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ImageDescriptionInner> describeImageAsync(String url, String maxCandidates) {
        return describeImageWithServiceResponseAsync(url, maxCandidates).map(new Func1<ServiceResponse<ImageDescriptionInner>, ImageDescriptionInner>() {
            @Override
            public ImageDescriptionInner call(ServiceResponse<ImageDescriptionInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ServiceResponse<ImageDescriptionInner>> describeImageWithServiceResponseAsync(String url, String maxCandidates) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.describeImage(maxCandidates, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageDescriptionInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageDescriptionInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageDescriptionInner> clientResponse = describeImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageDescriptionInner> describeImageDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<ImageDescriptionInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<ImageDescriptionInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TagResultInner object if successful.
     */
    public TagResultInner tagImage(String url) {
        return tagImageWithServiceResponseAsync(url).toBlocking().single().body();
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<TagResultInner> tagImageAsync(String url, final ServiceCallback<TagResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(tagImageWithServiceResponseAsync(url), serviceCallback);
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResultInner object
     */
    public Observable<TagResultInner> tagImageAsync(String url) {
        return tagImageWithServiceResponseAsync(url).map(new Func1<ServiceResponse<TagResultInner>, TagResultInner>() {
            @Override
            public TagResultInner call(ServiceResponse<TagResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResultInner object
     */
    public Observable<ServiceResponse<TagResultInner>> tagImageWithServiceResponseAsync(String url) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.tagImage(this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<TagResultInner>>>() {
                @Override
                public Observable<ServiceResponse<TagResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<TagResultInner> clientResponse = tagImageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<TagResultInner> tagImageDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<TagResultInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<TagResultInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the DomainModelResultsInner object if successful.
     */
    public DomainModelResultsInner analyzeImageByDomain(DomainModels model, String url) {
        return analyzeImageByDomainWithServiceResponseAsync(model, url).toBlocking().single().body();
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<DomainModelResultsInner> analyzeImageByDomainAsync(DomainModels model, String url, final ServiceCallback<DomainModelResultsInner> serviceCallback) {
        return ServiceFuture.fromResponse(analyzeImageByDomainWithServiceResponseAsync(model, url), serviceCallback);
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResultsInner object
     */
    public Observable<DomainModelResultsInner> analyzeImageByDomainAsync(DomainModels model, String url) {
        return analyzeImageByDomainWithServiceResponseAsync(model, url).map(new Func1<ServiceResponse<DomainModelResultsInner>, DomainModelResultsInner>() {
            @Override
            public DomainModelResultsInner call(ServiceResponse<DomainModelResultsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResultsInner object
     */
    public Observable<ServiceResponse<DomainModelResultsInner>> analyzeImageByDomainWithServiceResponseAsync(DomainModels model, String url) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (model == null) {
            throw new IllegalArgumentException("Parameter model is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.analyzeImageByDomain(model, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<DomainModelResultsInner>>>() {
                @Override
                public Observable<ServiceResponse<DomainModelResultsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<DomainModelResultsInner> clientResponse = analyzeImageByDomainDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<DomainModelResultsInner> analyzeImageByDomainDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<DomainModelResultsInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<DomainModelResultsInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void recognizeText(String url) {
        recognizeTextWithServiceResponseAsync(url).toBlocking().single().body();
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> recognizeTextAsync(String url, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromHeaderResponse(recognizeTextWithServiceResponseAsync(url), serviceCallback);
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<Void> recognizeTextAsync(String url) {
        return recognizeTextWithServiceResponseAsync(url).map(new Func1<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>, Void>() {
            @Override
            public Void call(ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>> recognizeTextWithServiceResponseAsync(String url) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        final Boolean detectHandwriting = null;
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.recognizeText(detectHandwriting, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>>>() {
                @Override
                public Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner> clientResponse = recognizeTextDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void recognizeText(String url, Boolean detectHandwriting) {
        recognizeTextWithServiceResponseAsync(url, detectHandwriting).toBlocking().single().body();
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> recognizeTextAsync(String url, Boolean detectHandwriting, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromHeaderResponse(recognizeTextWithServiceResponseAsync(url, detectHandwriting), serviceCallback);
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<Void> recognizeTextAsync(String url, Boolean detectHandwriting) {
        return recognizeTextWithServiceResponseAsync(url, detectHandwriting).map(new Func1<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>, Void>() {
            @Override
            public Void call(ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>> recognizeTextWithServiceResponseAsync(String url, Boolean detectHandwriting) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (url == null) {
            throw new IllegalArgumentException("Parameter url is required and cannot be null.");
        }
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.withUrl(url);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.recognizeText(detectHandwriting, this.acceptLanguage(), imageUrl, parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>>>() {
                @Override
                public Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner> clientResponse = recognizeTextDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponseWithHeaders<Void, RecognizeTextHeadersInner> recognizeTextDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<Void, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .buildWithHeaders(response, RecognizeTextHeadersInner.class);
    }

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TextOperationResultInner object if successful.
     */
    public TextOperationResultInner getTextOperationResult(String operationId) {
        return getTextOperationResultWithServiceResponseAsync(operationId).toBlocking().single().body();
    }

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<TextOperationResultInner> getTextOperationResultAsync(String operationId, final ServiceCallback<TextOperationResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(getTextOperationResultWithServiceResponseAsync(operationId), serviceCallback);
    }

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TextOperationResultInner object
     */
    public Observable<TextOperationResultInner> getTextOperationResultAsync(String operationId) {
        return getTextOperationResultWithServiceResponseAsync(operationId).map(new Func1<ServiceResponse<TextOperationResultInner>, TextOperationResultInner>() {
            @Override
            public TextOperationResultInner call(ServiceResponse<TextOperationResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TextOperationResultInner object
     */
    public Observable<ServiceResponse<TextOperationResultInner>> getTextOperationResultWithServiceResponseAsync(String operationId) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (operationId == null) {
            throw new IllegalArgumentException("Parameter operationId is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.getTextOperationResult(operationId, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<TextOperationResultInner>>>() {
                @Override
                public Observable<ServiceResponse<TextOperationResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<TextOperationResultInner> clientResponse = getTextOperationResultDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<TextOperationResultInner> getTextOperationResultDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<TextOperationResultInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<TextOperationResultInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageAnalysisInner object if successful.
     */
    public ImageAnalysisInner analyzeImageInStream(byte[] image) {
        return analyzeImageInStreamWithServiceResponseAsync(image).toBlocking().single().body();
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageAnalysisInner> analyzeImageInStreamAsync(byte[] image, final ServiceCallback<ImageAnalysisInner> serviceCallback) {
        return ServiceFuture.fromResponse(analyzeImageInStreamWithServiceResponseAsync(image), serviceCallback);
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ImageAnalysisInner> analyzeImageInStreamAsync(byte[] image) {
        return analyzeImageInStreamWithServiceResponseAsync(image).map(new Func1<ServiceResponse<ImageAnalysisInner>, ImageAnalysisInner>() {
            @Override
            public ImageAnalysisInner call(ServiceResponse<ImageAnalysisInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ServiceResponse<ImageAnalysisInner>> analyzeImageInStreamWithServiceResponseAsync(byte[] image) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final List<VisualFeatureTypes> visualFeatures = null;
        final String details = null;
        final String language = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        String visualFeaturesConverted = this.serializerAdapter().serializeList(visualFeatures, CollectionFormat.CSV);
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.analyzeImageInStream(visualFeaturesConverted, details, language, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageAnalysisInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageAnalysisInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageAnalysisInner> clientResponse = analyzeImageInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image. Possible values include: 'Celebrities', 'Landmarks'
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageAnalysisInner object if successful.
     */
    public ImageAnalysisInner analyzeImageInStream(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language) {
        return analyzeImageInStreamWithServiceResponseAsync(image, visualFeatures, details, language).toBlocking().single().body();
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image. Possible values include: 'Celebrities', 'Landmarks'
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageAnalysisInner> analyzeImageInStreamAsync(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language, final ServiceCallback<ImageAnalysisInner> serviceCallback) {
        return ServiceFuture.fromResponse(analyzeImageInStreamWithServiceResponseAsync(image, visualFeatures, details, language), serviceCallback);
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image. Possible values include: 'Celebrities', 'Landmarks'
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ImageAnalysisInner> analyzeImageInStreamAsync(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language) {
        return analyzeImageInStreamWithServiceResponseAsync(image, visualFeatures, details, language).map(new Func1<ServiceResponse<ImageAnalysisInner>, ImageAnalysisInner>() {
            @Override
            public ImageAnalysisInner call(ServiceResponse<ImageAnalysisInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image. Possible values include: 'Celebrities', 'Landmarks'
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysisInner object
     */
    public Observable<ServiceResponse<ImageAnalysisInner>> analyzeImageInStreamWithServiceResponseAsync(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        Validator.validate(visualFeatures);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        String visualFeaturesConverted = this.serializerAdapter().serializeList(visualFeatures, CollectionFormat.CSV);
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.analyzeImageInStream(visualFeaturesConverted, details, language, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageAnalysisInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageAnalysisInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageAnalysisInner> clientResponse = analyzeImageInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageAnalysisInner> analyzeImageInStreamDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<ImageAnalysisInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<ImageAnalysisInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the InputStream object if successful.
     */
    public InputStream generateThumbnailInStream(int width, int height, byte[] image) {
        return generateThumbnailInStreamWithServiceResponseAsync(width, height, image).toBlocking().single().body();
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image, final ServiceCallback<InputStream> serviceCallback) {
        return ServiceFuture.fromResponse(generateThumbnailInStreamWithServiceResponseAsync(width, height, image), serviceCallback);
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image) {
        return generateThumbnailInStreamWithServiceResponseAsync(width, height, image).map(new Func1<ServiceResponse<InputStream>, InputStream>() {
            @Override
            public InputStream call(ServiceResponse<InputStream> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<ServiceResponse<InputStream>> generateThumbnailInStreamWithServiceResponseAsync(int width, int height, byte[] image) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final Boolean smartCropping = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.generateThumbnailInStream(width, height, imageConverted, smartCropping, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<InputStream>>>() {
                @Override
                public Observable<ServiceResponse<InputStream>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<InputStream> clientResponse = generateThumbnailInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the InputStream object if successful.
     */
    public InputStream generateThumbnailInStream(int width, int height, byte[] image, Boolean smartCropping) {
        return generateThumbnailInStreamWithServiceResponseAsync(width, height, image, smartCropping).toBlocking().single().body();
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image, Boolean smartCropping, final ServiceCallback<InputStream> serviceCallback) {
        return ServiceFuture.fromResponse(generateThumbnailInStreamWithServiceResponseAsync(width, height, image, smartCropping), serviceCallback);
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image, Boolean smartCropping) {
        return generateThumbnailInStreamWithServiceResponseAsync(width, height, image, smartCropping).map(new Func1<ServiceResponse<InputStream>, InputStream>() {
            @Override
            public InputStream call(ServiceResponse<InputStream> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @param smartCropping Boolean flag for enabling smart cropping.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    public Observable<ServiceResponse<InputStream>> generateThumbnailInStreamWithServiceResponseAsync(int width, int height, byte[] image, Boolean smartCropping) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.generateThumbnailInStream(width, height, imageConverted, smartCropping, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<InputStream>>>() {
                @Override
                public Observable<ServiceResponse<InputStream>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<InputStream> clientResponse = generateThumbnailInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<InputStream> generateThumbnailInStreamDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<InputStream, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResultInner object if successful.
     */
    public OcrResultInner recognizePrintedTextInStream(boolean detectOrientation, byte[] image) {
        return recognizePrintedTextInStreamWithServiceResponseAsync(detectOrientation, image).toBlocking().single().body();
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OcrResultInner> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image, final ServiceCallback<OcrResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(recognizePrintedTextInStreamWithServiceResponseAsync(detectOrientation, image), serviceCallback);
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<OcrResultInner> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image) {
        return recognizePrintedTextInStreamWithServiceResponseAsync(detectOrientation, image).map(new Func1<ServiceResponse<OcrResultInner>, OcrResultInner>() {
            @Override
            public OcrResultInner call(ServiceResponse<OcrResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<ServiceResponse<OcrResultInner>> recognizePrintedTextInStreamWithServiceResponseAsync(boolean detectOrientation, byte[] image) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final OcrLanguages language = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.recognizePrintedTextInStream(language, detectOrientation, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OcrResultInner>>>() {
                @Override
                public Observable<ServiceResponse<OcrResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OcrResultInner> clientResponse = recognizePrintedTextInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResultInner object if successful.
     */
    public OcrResultInner recognizePrintedTextInStream(boolean detectOrientation, byte[] image, OcrLanguages language) {
        return recognizePrintedTextInStreamWithServiceResponseAsync(detectOrientation, image, language).toBlocking().single().body();
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<OcrResultInner> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image, OcrLanguages language, final ServiceCallback<OcrResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(recognizePrintedTextInStreamWithServiceResponseAsync(detectOrientation, image, language), serviceCallback);
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<OcrResultInner> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image, OcrLanguages language) {
        return recognizePrintedTextInStreamWithServiceResponseAsync(detectOrientation, image, language).map(new Func1<ServiceResponse<OcrResultInner>, OcrResultInner>() {
            @Override
            public OcrResultInner call(ServiceResponse<OcrResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResultInner object
     */
    public Observable<ServiceResponse<OcrResultInner>> recognizePrintedTextInStreamWithServiceResponseAsync(boolean detectOrientation, byte[] image, OcrLanguages language) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.recognizePrintedTextInStream(language, detectOrientation, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<OcrResultInner>>>() {
                @Override
                public Observable<ServiceResponse<OcrResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<OcrResultInner> clientResponse = recognizePrintedTextInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<OcrResultInner> recognizePrintedTextInStreamDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<OcrResultInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<OcrResultInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescriptionInner object if successful.
     */
    public ImageDescriptionInner describeImageInStream(byte[] image) {
        return describeImageInStreamWithServiceResponseAsync(image).toBlocking().single().body();
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageDescriptionInner> describeImageInStreamAsync(byte[] image, final ServiceCallback<ImageDescriptionInner> serviceCallback) {
        return ServiceFuture.fromResponse(describeImageInStreamWithServiceResponseAsync(image), serviceCallback);
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ImageDescriptionInner> describeImageInStreamAsync(byte[] image) {
        return describeImageInStreamWithServiceResponseAsync(image).map(new Func1<ServiceResponse<ImageDescriptionInner>, ImageDescriptionInner>() {
            @Override
            public ImageDescriptionInner call(ServiceResponse<ImageDescriptionInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ServiceResponse<ImageDescriptionInner>> describeImageInStreamWithServiceResponseAsync(byte[] image) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final String maxCandidates = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.describeImageInStream(maxCandidates, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageDescriptionInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageDescriptionInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageDescriptionInner> clientResponse = describeImageInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescriptionInner object if successful.
     */
    public ImageDescriptionInner describeImageInStream(byte[] image, String maxCandidates) {
        return describeImageInStreamWithServiceResponseAsync(image, maxCandidates).toBlocking().single().body();
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<ImageDescriptionInner> describeImageInStreamAsync(byte[] image, String maxCandidates, final ServiceCallback<ImageDescriptionInner> serviceCallback) {
        return ServiceFuture.fromResponse(describeImageInStreamWithServiceResponseAsync(image, maxCandidates), serviceCallback);
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ImageDescriptionInner> describeImageInStreamAsync(byte[] image, String maxCandidates) {
        return describeImageInStreamWithServiceResponseAsync(image, maxCandidates).map(new Func1<ServiceResponse<ImageDescriptionInner>, ImageDescriptionInner>() {
            @Override
            public ImageDescriptionInner call(ServiceResponse<ImageDescriptionInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescriptionInner object
     */
    public Observable<ServiceResponse<ImageDescriptionInner>> describeImageInStreamWithServiceResponseAsync(byte[] image, String maxCandidates) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.describeImageInStream(maxCandidates, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<ImageDescriptionInner>>>() {
                @Override
                public Observable<ServiceResponse<ImageDescriptionInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<ImageDescriptionInner> clientResponse = describeImageInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<ImageDescriptionInner> describeImageInStreamDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<ImageDescriptionInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<ImageDescriptionInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TagResultInner object if successful.
     */
    public TagResultInner tagImageInStream(byte[] image) {
        return tagImageInStreamWithServiceResponseAsync(image).toBlocking().single().body();
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<TagResultInner> tagImageInStreamAsync(byte[] image, final ServiceCallback<TagResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(tagImageInStreamWithServiceResponseAsync(image), serviceCallback);
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResultInner object
     */
    public Observable<TagResultInner> tagImageInStreamAsync(byte[] image) {
        return tagImageInStreamWithServiceResponseAsync(image).map(new Func1<ServiceResponse<TagResultInner>, TagResultInner>() {
            @Override
            public TagResultInner call(ServiceResponse<TagResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResultInner object
     */
    public Observable<ServiceResponse<TagResultInner>> tagImageInStreamWithServiceResponseAsync(byte[] image) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.tagImageInStream(imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<TagResultInner>>>() {
                @Override
                public Observable<ServiceResponse<TagResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<TagResultInner> clientResponse = tagImageInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<TagResultInner> tagImageInStreamDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<TagResultInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<TagResultInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the DomainModelResultsInner object if successful.
     */
    public DomainModelResultsInner analyzeImageByDomainInStream(String model, byte[] image) {
        return analyzeImageByDomainInStreamWithServiceResponseAsync(model, image).toBlocking().single().body();
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<DomainModelResultsInner> analyzeImageByDomainInStreamAsync(String model, byte[] image, final ServiceCallback<DomainModelResultsInner> serviceCallback) {
        return ServiceFuture.fromResponse(analyzeImageByDomainInStreamWithServiceResponseAsync(model, image), serviceCallback);
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResultsInner object
     */
    public Observable<DomainModelResultsInner> analyzeImageByDomainInStreamAsync(String model, byte[] image) {
        return analyzeImageByDomainInStreamWithServiceResponseAsync(model, image).map(new Func1<ServiceResponse<DomainModelResultsInner>, DomainModelResultsInner>() {
            @Override
            public DomainModelResultsInner call(ServiceResponse<DomainModelResultsInner> response) {
                return response.body();
            }
        });
    }

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResultsInner object
     */
    public Observable<ServiceResponse<DomainModelResultsInner>> analyzeImageByDomainInStreamWithServiceResponseAsync(String model, byte[] image) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (model == null) {
            throw new IllegalArgumentException("Parameter model is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.analyzeImageByDomainInStream(model, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<DomainModelResultsInner>>>() {
                @Override
                public Observable<ServiceResponse<DomainModelResultsInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<DomainModelResultsInner> clientResponse = analyzeImageByDomainInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<DomainModelResultsInner> analyzeImageByDomainInStreamDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<DomainModelResultsInner, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<DomainModelResultsInner>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .build(response);
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void recognizeTextInStream(byte[] image) {
        recognizeTextInStreamWithServiceResponseAsync(image).toBlocking().single().body();
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> recognizeTextInStreamAsync(byte[] image, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromHeaderResponse(recognizeTextInStreamWithServiceResponseAsync(image), serviceCallback);
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<Void> recognizeTextInStreamAsync(byte[] image) {
        return recognizeTextInStreamWithServiceResponseAsync(image).map(new Func1<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>, Void>() {
            @Override
            public Void call(ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>> recognizeTextInStreamWithServiceResponseAsync(byte[] image) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        final Boolean detectHandwriting = null;
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.recognizeTextInStream(detectHandwriting, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>>>() {
                @Override
                public Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner> clientResponse = recognizeTextInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    public void recognizeTextInStream(byte[] image, Boolean detectHandwriting) {
        recognizeTextInStreamWithServiceResponseAsync(image, detectHandwriting).toBlocking().single().body();
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<Void> recognizeTextInStreamAsync(byte[] image, Boolean detectHandwriting, final ServiceCallback<Void> serviceCallback) {
        return ServiceFuture.fromHeaderResponse(recognizeTextInStreamWithServiceResponseAsync(image, detectHandwriting), serviceCallback);
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<Void> recognizeTextInStreamAsync(byte[] image, Boolean detectHandwriting) {
        return recognizeTextInStreamWithServiceResponseAsync(image, detectHandwriting).map(new Func1<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>, Void>() {
            @Override
            public Void call(ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner> response) {
                return response.body();
            }
        });
    }

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>> recognizeTextInStreamWithServiceResponseAsync(byte[] image, Boolean detectHandwriting) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (image == null) {
            throw new IllegalArgumentException("Parameter image is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        RequestBody imageConverted = RequestBody.create(MediaType.parse("application/octet-stream"), image);
        return service.recognizeTextInStream(detectHandwriting, imageConverted, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>>>() {
                @Override
                public Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner> clientResponse = recognizeTextInStreamDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeadersInner> recognizeTextInStreamDelegate(Response<ResponseBody> response) throws ComputerVisionErrorException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<Void, ComputerVisionErrorException>newInstance(this.serializerAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(ComputerVisionErrorException.class)
                .buildWithHeaders(response, RecognizeTextInStreamHeadersInner.class);
    }

}
