/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.cognitiveservices.computervision.models.AzureRegions;
import com.microsoft.azure.cognitiveservices.computervision.models.ComputerVisionErrorException;
import com.microsoft.azure.cognitiveservices.computervision.models.Details;
import com.microsoft.azure.cognitiveservices.computervision.models.DomainModelResults;
import com.microsoft.azure.cognitiveservices.computervision.models.DomainModels;
import com.microsoft.azure.cognitiveservices.computervision.models.ImageAnalysis;
import com.microsoft.azure.cognitiveservices.computervision.models.ImageDescription;
import com.microsoft.azure.cognitiveservices.computervision.models.Language1;
import com.microsoft.azure.cognitiveservices.computervision.models.ListModelsResult;
import com.microsoft.azure.cognitiveservices.computervision.models.OcrLanguages;
import com.microsoft.azure.cognitiveservices.computervision.models.OcrResult;
import com.microsoft.azure.cognitiveservices.computervision.models.RecognizeTextHeaders;
import com.microsoft.azure.cognitiveservices.computervision.models.RecognizeTextInStreamHeaders;
import com.microsoft.azure.cognitiveservices.computervision.models.TagResult;
import com.microsoft.azure.cognitiveservices.computervision.models.TextOperationResult;
import com.microsoft.azure.cognitiveservices.computervision.models.VisualFeatureTypes;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.InputStream;
import java.util.List;
import rx.Observable;

/**
 * The interface for ComputerVisionAPI class.
 */
public interface ComputerVisionAPI {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
    */
    RestClient restClient();

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    AzureClient getAzureClient();

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    String userAgent();

    /**
     * Gets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @return the azureRegion value.
     */
    AzureRegions azureRegion();

    /**
     * Sets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @param azureRegion the azureRegion value.
     * @return the service client itself
     */
    ComputerVisionAPI withAzureRegion(AzureRegions azureRegion);

    /**
     * Gets Gets or sets the preferred language for the response..
     *
     * @return the acceptLanguage value.
     */
    String acceptLanguage();

    /**
     * Sets Gets or sets the preferred language for the response..
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    ComputerVisionAPI withAcceptLanguage(String acceptLanguage);

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    int longRunningOperationRetryTimeout();

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    ComputerVisionAPI withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout);

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @return the generateClientRequestId value.
     */
    boolean generateClientRequestId();

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    ComputerVisionAPI withGenerateClientRequestId(boolean generateClientRequestId);

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ListModelsResult object if successful.
     */
    ListModelsResult listModels();

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<ListModelsResult> listModelsAsync(final ServiceCallback<ListModelsResult> serviceCallback);

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ListModelsResult object
     */
    Observable<ListModelsResult> listModelsAsync();

    /**
     * This operation returns the list of domain-specific models that are supported by the Computer Vision API.  Currently, the API only supports one domain-specific model: a celebrity recognizer. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ListModelsResult object
     */
    Observable<ServiceResponse<ListModelsResult>> listModelsWithServiceResponseAsync();

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageAnalysis object if successful.
     */
    ImageAnalysis analyzeImage(String url);

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<ImageAnalysis> analyzeImageAsync(String url, final ServiceCallback<ImageAnalysis> serviceCallback);

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ImageAnalysis> analyzeImageAsync(String url);

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ServiceResponse<ImageAnalysis>> analyzeImageWithServiceResponseAsync(String url);
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
     * @return the ImageAnalysis object if successful.
     */
    ImageAnalysis analyzeImage(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language);

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
    ServiceFuture<ImageAnalysis> analyzeImageAsync(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language, final ServiceCallback<ImageAnalysis> serviceCallback);

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image.
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ImageAnalysis> analyzeImageAsync(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language);

    /**
     * This operation extracts a rich set of visual features based on the image content. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.  Within your request, there is an optional parameter to allow you to choose which features to return.  By default, image categories are returned in the response.
     *
     * @param url the String value
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image.
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ServiceResponse<ImageAnalysis>> analyzeImageWithServiceResponseAsync(String url, List<VisualFeatureTypes> visualFeatures, List<Details> details, Language1 language);

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
    InputStream generateThumbnail(int width, int height, String url);

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
    ServiceFuture<InputStream> generateThumbnailAsync(int width, int height, String url, final ServiceCallback<InputStream> serviceCallback);

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    Observable<InputStream> generateThumbnailAsync(int width, int height, String url);

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    Observable<ServiceResponse<InputStream>> generateThumbnailWithServiceResponseAsync(int width, int height, String url);
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
    InputStream generateThumbnail(int width, int height, String url, Boolean smartCropping);

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
    ServiceFuture<InputStream> generateThumbnailAsync(int width, int height, String url, Boolean smartCropping, final ServiceCallback<InputStream> serviceCallback);

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
    Observable<InputStream> generateThumbnailAsync(int width, int height, String url, Boolean smartCropping);

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
    Observable<ServiceResponse<InputStream>> generateThumbnailWithServiceResponseAsync(int width, int height, String url, Boolean smartCropping);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResult object if successful.
     */
    OcrResult recognizePrintedText(boolean detectOrientation, String url);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<OcrResult> recognizePrintedTextAsync(boolean detectOrientation, String url, final ServiceCallback<OcrResult> serviceCallback);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<OcrResult> recognizePrintedTextAsync(boolean detectOrientation, String url);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<ServiceResponse<OcrResult>> recognizePrintedTextWithServiceResponseAsync(boolean detectOrientation, String url);
    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResult object if successful.
     */
    OcrResult recognizePrintedText(boolean detectOrientation, String url, OcrLanguages language);

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
    ServiceFuture<OcrResult> recognizePrintedTextAsync(boolean detectOrientation, String url, OcrLanguages language, final ServiceCallback<OcrResult> serviceCallback);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<OcrResult> recognizePrintedTextAsync(boolean detectOrientation, String url, OcrLanguages language);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param url the String value
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<ServiceResponse<OcrResult>> recognizePrintedTextWithServiceResponseAsync(boolean detectOrientation, String url, OcrLanguages language);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescription object if successful.
     */
    ImageDescription describeImage(String url);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<ImageDescription> describeImageAsync(String url, final ServiceCallback<ImageDescription> serviceCallback);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ImageDescription> describeImageAsync(String url);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ServiceResponse<ImageDescription>> describeImageWithServiceResponseAsync(String url);
    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescription object if successful.
     */
    ImageDescription describeImage(String url, String maxCandidates);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<ImageDescription> describeImageAsync(String url, String maxCandidates, final ServiceCallback<ImageDescription> serviceCallback);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ImageDescription> describeImageAsync(String url, String maxCandidates);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param url the String value
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ServiceResponse<ImageDescription>> describeImageWithServiceResponseAsync(String url, String maxCandidates);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TagResult object if successful.
     */
    TagResult tagImage(String url);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<TagResult> tagImageAsync(String url, final ServiceCallback<TagResult> serviceCallback);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResult object
     */
    Observable<TagResult> tagImageAsync(String url);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResult object
     */
    Observable<ServiceResponse<TagResult>> tagImageWithServiceResponseAsync(String url);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the DomainModelResults object if successful.
     */
    DomainModelResults analyzeImageByDomain(DomainModels model, String url);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<DomainModelResults> analyzeImageByDomainAsync(DomainModels model, String url, final ServiceCallback<DomainModelResults> serviceCallback);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResults object
     */
    Observable<DomainModelResults> analyzeImageByDomainAsync(DomainModels model, String url);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize. Possible values include: 'Celebrities', 'Landmarks'
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResults object
     */
    Observable<ServiceResponse<DomainModelResults>> analyzeImageByDomainWithServiceResponseAsync(DomainModels model, String url);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void recognizeText(String url);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> recognizeTextAsync(String url, final ServiceCallback<Void> serviceCallback);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<Void> recognizeTextAsync(String url);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeaders>> recognizeTextWithServiceResponseAsync(String url);
    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void recognizeText(String url, Boolean detectHandwriting);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> recognizeTextAsync(String url, Boolean detectHandwriting, final ServiceCallback<Void> serviceCallback);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<Void> recognizeTextAsync(String url, Boolean detectHandwriting);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param url the String value
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<ServiceResponseWithHeaders<Void, RecognizeTextHeaders>> recognizeTextWithServiceResponseAsync(String url, Boolean detectHandwriting);

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TextOperationResult object if successful.
     */
    TextOperationResult getTextOperationResult(String operationId);

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<TextOperationResult> getTextOperationResultAsync(String operationId, final ServiceCallback<TextOperationResult> serviceCallback);

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TextOperationResult object
     */
    Observable<TextOperationResult> getTextOperationResultAsync(String operationId);

    /**
     * This interface is used for getting text operation result. The URL to this interface should be retrieved from 'Operation-Location' field returned from Recognize Text interface.
     *
     * @param operationId Id of the text operation returned in the response of the 'Recognize Handwritten Text'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TextOperationResult object
     */
    Observable<ServiceResponse<TextOperationResult>> getTextOperationResultWithServiceResponseAsync(String operationId);

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageAnalysis object if successful.
     */
    ImageAnalysis analyzeImageInStream(byte[] image);

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<ImageAnalysis> analyzeImageInStreamAsync(byte[] image, final ServiceCallback<ImageAnalysis> serviceCallback);

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ImageAnalysis> analyzeImageInStreamAsync(byte[] image);

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ServiceResponse<ImageAnalysis>> analyzeImageInStreamWithServiceResponseAsync(byte[] image);
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
     * @return the ImageAnalysis object if successful.
     */
    ImageAnalysis analyzeImageInStream(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language);

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
    ServiceFuture<ImageAnalysis> analyzeImageInStreamAsync(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language, final ServiceCallback<ImageAnalysis> serviceCallback);

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image. Possible values include: 'Celebrities', 'Landmarks'
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ImageAnalysis> analyzeImageInStreamAsync(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language);

    /**
     * This operation extracts a rich set of visual features based on the image content.
     *
     * @param image An image stream.
     * @param visualFeatures A string indicating what visual feature types to return. Multiple values should be comma-separated. Valid visual feature types include:Categories - categorizes image content according to a taxonomy defined in documentation. Tags - tags the image with a detailed list of words related to the image content. Description - describes the image content with a complete English sentence. Faces - detects if faces are present. If present, generate coordinates, gender and age. ImageType - detects if image is clipart or a line drawing. Color - determines the accent color, dominant color, and whether an image is black&amp;white.Adult - detects if the image is pornographic in nature (depicts nudity or a sex act).  Sexually suggestive content is also detected.
     * @param details A string indicating which domain-specific details to return. Multiple values should be comma-separated. Valid visual feature types include:Celebrities - identifies celebrities if detected in the image. Possible values include: 'Celebrities', 'Landmarks'
     * @param language A string indicating which language to return. The service will return recognition results in specified language. If this parameter is not specified, the default value is &amp;quot;en&amp;quot;.Supported languages:en - English, Default.zh - Simplified Chinese. Possible values include: 'en', 'zh'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageAnalysis object
     */
    Observable<ServiceResponse<ImageAnalysis>> analyzeImageInStreamWithServiceResponseAsync(byte[] image, List<VisualFeatureTypes> visualFeatures, String details, String language);

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
    InputStream generateThumbnailInStream(int width, int height, byte[] image);

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
    ServiceFuture<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image, final ServiceCallback<InputStream> serviceCallback);

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    Observable<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image);

    /**
     * This operation generates a thumbnail image with the user-specified width and height. By default, the service analyzes the image, identifies the region of interest (ROI), and generates smart cropping coordinates based on the ROI. Smart cropping helps when you specify an aspect ratio that differs from that of the input image. A successful response contains the thumbnail image binary. If the request failed, the response contains an error code and a message to help determine what went wrong.
     *
     * @param width Width of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param height Height of the thumbnail. It must be between 1 and 1024. Recommended minimum of 50.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the InputStream object
     */
    Observable<ServiceResponse<InputStream>> generateThumbnailInStreamWithServiceResponseAsync(int width, int height, byte[] image);
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
    InputStream generateThumbnailInStream(int width, int height, byte[] image, Boolean smartCropping);

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
    ServiceFuture<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image, Boolean smartCropping, final ServiceCallback<InputStream> serviceCallback);

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
    Observable<InputStream> generateThumbnailInStreamAsync(int width, int height, byte[] image, Boolean smartCropping);

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
    Observable<ServiceResponse<InputStream>> generateThumbnailInStreamWithServiceResponseAsync(int width, int height, byte[] image, Boolean smartCropping);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResult object if successful.
     */
    OcrResult recognizePrintedTextInStream(boolean detectOrientation, byte[] image);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<OcrResult> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image, final ServiceCallback<OcrResult> serviceCallback);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<OcrResult> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<ServiceResponse<OcrResult>> recognizePrintedTextInStreamWithServiceResponseAsync(boolean detectOrientation, byte[] image);
    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the OcrResult object if successful.
     */
    OcrResult recognizePrintedTextInStream(boolean detectOrientation, byte[] image, OcrLanguages language);

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
    ServiceFuture<OcrResult> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image, OcrLanguages language, final ServiceCallback<OcrResult> serviceCallback);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<OcrResult> recognizePrintedTextInStreamAsync(boolean detectOrientation, byte[] image, OcrLanguages language);

    /**
     * Optical Character Recognition (OCR) detects printed text in an image and extracts the recognized characters into a machine-usable character stream.   Upon success, the OCR results will be returned. Upon failure, the error code together with an error message will be returned. The error code can be one of InvalidImageUrl, InvalidImageFormat, InvalidImageSize, NotSupportedImage,  NotSupportedLanguage, or InternalServerError.
     *
     * @param detectOrientation Whether detect the text orientation in the image. With detectOrientation=true the OCR service tries to detect the image orientation and correct it before further processing (e.g. if it's upside-down).
     * @param image An image stream.
     * @param language The BCP-47 language code of the text to be detected in the image. The default value is 'unk'. Possible values include: 'unk', 'zh-Hans', 'zh-Hant', 'cs', 'da', 'nl', 'en', 'fi', 'fr', 'de', 'el', 'hu', 'it', 'ja', 'ko', 'nb', 'pl', 'pt', 'ru', 'es', 'sv', 'tr', 'ar', 'ro', 'sr-Cyrl', 'sr-Latn', 'sk'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the OcrResult object
     */
    Observable<ServiceResponse<OcrResult>> recognizePrintedTextInStreamWithServiceResponseAsync(boolean detectOrientation, byte[] image, OcrLanguages language);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescription object if successful.
     */
    ImageDescription describeImageInStream(byte[] image);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<ImageDescription> describeImageInStreamAsync(byte[] image, final ServiceCallback<ImageDescription> serviceCallback);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ImageDescription> describeImageInStreamAsync(byte[] image);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ServiceResponse<ImageDescription>> describeImageInStreamWithServiceResponseAsync(byte[] image);
    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the ImageDescription object if successful.
     */
    ImageDescription describeImageInStream(byte[] image, String maxCandidates);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<ImageDescription> describeImageInStreamAsync(byte[] image, String maxCandidates, final ServiceCallback<ImageDescription> serviceCallback);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ImageDescription> describeImageInStreamAsync(byte[] image, String maxCandidates);

    /**
     * This operation generates a description of an image in human readable language with complete sentences.  The description is based on a collection of content tags, which are also returned by the operation. More than one description can be generated for each image.  Descriptions are ordered by their confidence score. All descriptions are in English. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL.A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param image An image stream.
     * @param maxCandidates Maximum number of candidate descriptions to be returned.  The default is 1.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the ImageDescription object
     */
    Observable<ServiceResponse<ImageDescription>> describeImageInStreamWithServiceResponseAsync(byte[] image, String maxCandidates);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the TagResult object if successful.
     */
    TagResult tagImageInStream(byte[] image);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<TagResult> tagImageInStreamAsync(byte[] image, final ServiceCallback<TagResult> serviceCallback);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResult object
     */
    Observable<TagResult> tagImageInStreamAsync(byte[] image);

    /**
     * This operation generates a list of words, or tags, that are relevant to the content of the supplied image. The Computer Vision API can return tags based on objects, living beings, scenery or actions found in images. Unlike categories, tags are not organized according to a hierarchical classification system, but correspond to image content. Tags may contain hints to avoid ambiguity or provide context, for example the tag “cello” may be accompanied by the hint “musical instrument”. All tags are in English.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the TagResult object
     */
    Observable<ServiceResponse<TagResult>> tagImageInStreamWithServiceResponseAsync(byte[] image);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the DomainModelResults object if successful.
     */
    DomainModelResults analyzeImageByDomainInStream(String model, byte[] image);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<DomainModelResults> analyzeImageByDomainInStreamAsync(String model, byte[] image, final ServiceCallback<DomainModelResults> serviceCallback);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResults object
     */
    Observable<DomainModelResults> analyzeImageByDomainInStreamAsync(String model, byte[] image);

    /**
     * This operation recognizes content within an image by applying a domain-specific model.  The list of domain-specific models that are supported by the Computer Vision API can be retrieved using the /models GET request.  Currently, the API only provides a single domain-specific model: celebrities. Two input methods are supported -- (1) Uploading an image or (2) specifying an image URL. A successful response will be returned in JSON.  If the request failed, the response will contain an error code and a message to help understand what went wrong.
     *
     * @param model The domain-specific content to recognize.
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the DomainModelResults object
     */
    Observable<ServiceResponse<DomainModelResults>> analyzeImageByDomainInStreamWithServiceResponseAsync(String model, byte[] image);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void recognizeTextInStream(byte[] image);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> recognizeTextInStreamAsync(byte[] image, final ServiceCallback<Void> serviceCallback);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<Void> recognizeTextInStreamAsync(byte[] image);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeaders>> recognizeTextInStreamWithServiceResponseAsync(byte[] image);
    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ComputerVisionErrorException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void recognizeTextInStream(byte[] image, Boolean detectHandwriting);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<Void> recognizeTextInStreamAsync(byte[] image, Boolean detectHandwriting, final ServiceCallback<Void> serviceCallback);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<Void> recognizeTextInStreamAsync(byte[] image, Boolean detectHandwriting);

    /**
     * Recognize Text operation. When you use the Recognize Text interface, the response contains a field called “Operation-Location”. The “Operation-Location” field contains the URL that you must use for your Get Handwritten Text Operation Result operation.
     *
     * @param image An image stream.
     * @param detectHandwriting If “true” is specified, handwriting recognition is performed. If this parameter is set to “false” or is not specified, printed text recognition is performed.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    Observable<ServiceResponseWithHeaders<Void, RecognizeTextInStreamHeaders>> recognizeTextInStreamWithServiceResponseAsync(byte[] image, Boolean detectHandwriting);

}
