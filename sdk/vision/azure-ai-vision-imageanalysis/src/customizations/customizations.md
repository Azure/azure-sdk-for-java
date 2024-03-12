# Java Image Analysis SDK Customization

This document specifies the hand-customization done to the auto-generated Java Image Analysis SDK.

## Add ImageBoundingBox.toString()

The following method was added to the class `ImageBoundingBox` (file `ImageBoundingBox.java`):
```java
    /**
     * Returns a compact string containing the bounding box values in a style suitable for printing.
     *
     * @return a string containing the bounding box values.
     */
    public String toString() {
        return new StringBuilder().append("{x=").append(this.getX()).append(", y=").append(this.getY())
            .append(", width=").append(this.getWidth()).append(", height=").append(this.getHeight()).append("}")
            .toString();
    }
```

## Add ImagePoint.toString()

The following method was added to the class `ImagePoint` (file `ImagePoint.java`):

```Java
    /**
     * Returns a compact string containing the image point values in a style suitable for printing.
     * 
     * @return A string containing the image point values.
     */
    public String toString() {
        return "(x=" + this.getX() + ", y=" + this.getY() + ")";
    }
```

## Add new class ImageAnalysisOptions

A new Java file `ImageAnalysisOptions.java` was written and placed in the folder `src\main\java\com\azure\ai\vision\imageanalysis\models`.

## Updates to class ImageAnalysisClient

The following one private method and four public methods were added at the end of the class `ImageAnalysisClient` (file `ImageAnalysisClient.java`):

```java
    /**
     * Apply the Image Analysis Options as query parameters on a given RequestOptions object.
     * Return an updated RequestOptions object.
     *
     * @param inputRequestOptions The input request options (can be null).
     * @param imageAnalysisOptions The Image Analysis Options to apply to the request to be sent (can be null).
     *
     * @return An updated RequestOptions with addition query parameters set based on the Image Analysis Options. Can be
     * null.
     */
    static RequestOptions updateRequestOptions(RequestOptions inputRequestOptions,
        ImageAnalysisOptions imageAnalysisOptions) {
        RequestOptions outputRequestOptions = inputRequestOptions;
        if (imageAnalysisOptions != null) {
            String language = imageAnalysisOptions.getLanguage();
            Boolean isGenderNeutralCaption = imageAnalysisOptions.isGenderNeutralCaption();
            List<Double> smartCropsAspectRatios = imageAnalysisOptions.getSmartCropsAspectRatios();
            String modelVersion = imageAnalysisOptions.getModelVersion();
            if (language != null || isGenderNeutralCaption != null || smartCropsAspectRatios != null
                || modelVersion != null) {
                if (outputRequestOptions == null) {
                    outputRequestOptions = new RequestOptions();
                }
                if (language != null) {
                    outputRequestOptions.addQueryParam("language", language, false);
                }
                if (isGenderNeutralCaption != null) {
                    outputRequestOptions.addQueryParam("gender-neutral-caption", String.valueOf(isGenderNeutralCaption),
                        false);
                }
                if (smartCropsAspectRatios != null) {
                    outputRequestOptions.addQueryParam("smartcrops-aspect-ratios",
                        JacksonAdapter.createDefaultSerializerAdapter().serializeIterable(smartCropsAspectRatios,
                            CollectionFormat.CSV),
                        false);
                }
                if (modelVersion != null) {
                    outputRequestOptions.addQueryParam("model-version", modelVersion, false);
                }
            }
        }
        return outputRequestOptions;
    }

    /**
     * Performs a single Image Analysis operation on a give image URL.
     *
     * @param imageUrl The publicly accessible URL of the image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param options Additional image analysis options.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation.
     *
     * <!-- src_embed com.azure.ai.vision.imageanalysis.sync-analysis-from-url -->
     * <pre>
     * &#47;&#47;
     * &#47;&#47; Synchronous analysis of an image file &#40;https:&#47;&#47;aka.ms&#47;azsdk&#47;image-analysis&#47;sample.jpg&#41;,
     * &#47;&#47; using all visual features, with all options set. You will likely need fewer visual features
     * &#47;&#47; and only some &#40;or no&#41; options set.
     * &#47;&#47;
     * ImageAnalysisOptions options = new ImageAnalysisOptions&#40;&#41;
     *     .setLanguage&#40;&quot;en&quot;&#41;
     *     .setGenderNeutralCaption&#40;true&#41;
     *     .setSmartCropsAspectRatios&#40;Arrays.asList&#40;0.9, 1.33&#41;&#41;
     *     .setModelVersion&#40;&quot;latest&quot;&#41;;
     *
     * ImageAnalysisResult result = client.analyzeFromUrl&#40;
     *     &quot;https:&#47;&#47;aka.ms&#47;azsdk&#47;image-analysis&#47;sample.jpg&quot;,
     *     Arrays.asList&#40;
     *         VisualFeatures.SMART_CROPS,
     *         VisualFeatures.CAPTION,
     *         VisualFeatures.DENSE_CAPTIONS,
     *         VisualFeatures.OBJECTS,
     *         VisualFeatures.PEOPLE,
     *         VisualFeatures.READ,
     *         VisualFeatures.TAGS&#41;,
     *     options&#41;;
     * </pre>
     * <!-- end com.azure.ai.vision.imageanalysis.sync-analysis-from-url -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ImageAnalysisResult analyzeFromUrl(String imageUrl, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        return analyzeFromUrlWithResponse(imageUrl, visualFeatures, options, null).getValue();
    }

    /**
     * Performs a single Image Analysis operation on a given image buffer.
     *
     * @param imageData The image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param options Additional image analysis options.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation.
     *
     * <!-- src_embed com.azure.ai.vision.imageanalysis.sync-analysis-from-file -->
     * <pre>
     * &#47;&#47;
     * &#47;&#47; Synchronous analysis of an image file &#40;sample.jpg&#41;, using all visual features,
     * &#47;&#47; with all options set. You will likely need fewer visual features and only
     * &#47;&#47; some &#40;or no&#41; options set.
     * &#47;&#47;
     * ImageAnalysisOptions options = new ImageAnalysisOptions&#40;&#41;
     *     .setLanguage&#40;&quot;en&quot;&#41;
     *     .setGenderNeutralCaption&#40;true&#41;
     *     .setSmartCropsAspectRatios&#40;Arrays.asList&#40;0.9, 1.33&#41;&#41;
     *     .setModelVersion&#40;&quot;latest&quot;&#41;;
     *
     * ImageAnalysisResult result = client.analyze&#40;
     *     BinaryData.fromFile&#40;new File&#40;&quot;sample.jpg&quot;&#41;.toPath&#40;&#41;&#41;,
     *     Arrays.asList&#40;
     *         VisualFeatures.SMART_CROPS,
     *         VisualFeatures.CAPTION,
     *         VisualFeatures.DENSE_CAPTIONS,
     *         VisualFeatures.OBJECTS,
     *         VisualFeatures.PEOPLE,
     *         VisualFeatures.READ,
     *         VisualFeatures.TAGS&#41;,
     *     options&#41;;
     * </pre>
     * <!-- end com.azure.ai.vision.imageanalysis.sync-analysis-from-file -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ImageAnalysisResult analyze(BinaryData imageData, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        return analyzeWithResponse(imageData, visualFeatures, options, null).getValue();
    }

    /**
     * Performs a single Image Analysis operation on a given image URL, while allowing customization
     * of the HTTP request and access to HTTP request and response details. This is not commonly used.
     *
     * @param imageUrl The publicly accessible URL of the image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param imageAnalysisOptions Additional image analysis options.
     * @param requestOptions The options to configure the HTTP request before the HTTP client sends it.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation along with <a href="https://learn.microsoft.com/java/api/com.azure.core.http.rest.response">Response</a>.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ImageAnalysisResult> analyzeFromUrlWithResponse(String imageUrl,
        List<VisualFeatures> visualFeatures, ImageAnalysisOptions imageAnalysisOptions, RequestOptions requestOptions) {
        List<String> visualFeaturesAsStrings = visualFeatures.stream()
            .map(paramItemValue -> Objects.toString(paramItemValue, "")).collect(Collectors.toList());
        Response<BinaryData> response = analyzeFromUrlWithResponse(visualFeaturesAsStrings,
            BinaryData.fromObject(new ImageUrl(imageUrl)), updateRequestOptions(requestOptions, imageAnalysisOptions));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            response.getValue().toObject(ImageAnalysisResult.class));
    }

    /**
     * Performs a single Image Analysis operation on a given image buffer, while allowing customization
     * of the HTTP request and access to HTTP request and response details. This is not commonly used.
     *
     * @param imageData The image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param imageAnalysisOptions Additional image analysis options.
     * @param requestOptions The options to configure the HTTP request before the HTTP client sends it.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation along with <a href="https://learn.microsoft.com/java/api/com.azure.core.http.rest.response">Response</a>.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ImageAnalysisResult> analyzeWithResponse(BinaryData imageData, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions imageAnalysisOptions, RequestOptions requestOptions) {
        List<String> visualFeaturesAsStrings = visualFeatures.stream()
            .map(paramItemValue -> Objects.toString(paramItemValue, "")).collect(Collectors.toList());
        Response<BinaryData> response = analyzeFromImageDataWithResponse(visualFeaturesAsStrings, imageData,
            updateRequestOptions(requestOptions, imageAnalysisOptions));
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            response.getValue().toObject(ImageAnalysisResult.class));
    }
```

## Updates to class ImageAnalysisAsyncClient

The following one private method and four public methods were added at the end of the class `ImageAnalysisAsyncClient` (file `ImageAnalysisAsyncClient.java`):

```java
    /**
     * Performs a single Image Analysis operation on a give image URL.
     *
     * @param imageUrl The publicly accessible URL of the image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param options Additional image analysis options.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation on successful completion of {@link Mono}.
     *
     * <!-- src_embed com.azure.ai.vision.imageanalysis.async-analysis-from-url -->
     * <pre>
     * &#47;&#47;
     * &#47;&#47; Asynchronous analysis of an image file &#40;https:&#47;&#47;aka.ms&#47;azsdk&#47;image-analysis&#47;sample.jpg&#41;,
     * &#47;&#47; using all visual features, with all options set. You will likely need fewer visual features
     * &#47;&#47; and only some &#40;or no&#41; options set.
     * &#47;&#47;
     * ImageAnalysisOptions options = new ImageAnalysisOptions&#40;&#41;
     *     .setLanguage&#40;&quot;en&quot;&#41;
     *     .setGenderNeutralCaption&#40;true&#41;
     *     .setSmartCropsAspectRatios&#40;Arrays.asList&#40;0.9, 1.33&#41;&#41;
     *     .setModelVersion&#40;&quot;latest&quot;&#41;;
     *
     * Mono&lt;ImageAnalysisResult&gt; result = client.analyzeFromUrl&#40;
     *     &quot;https:&#47;&#47;aka.ms&#47;azsdk&#47;image-analysis&#47;sample.jpg&quot;,
     *     Arrays.asList&#40;
     *         VisualFeatures.SMART_CROPS,
     *         VisualFeatures.CAPTION,
     *         VisualFeatures.DENSE_CAPTIONS,
     *         VisualFeatures.OBJECTS,
     *         VisualFeatures.PEOPLE,
     *         VisualFeatures.READ,
     *         VisualFeatures.TAGS&#41;,
     *     options&#41;;
     * </pre>
     * <!-- end com.azure.ai.vision.imageanalysis.async-analysis-from-url -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ImageAnalysisResult> analyzeFromUrl(String imageUrl, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        return analyzeFromUrlWithResponse(imageUrl, visualFeatures, options, null).map(response -> {
            return response.getValue();
        });
    }

    /**
     * Performs a single Image Analysis operation on a given image buffer.
     *
     * @param imageData The image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param options Additional image analysis options.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation on successful completion of {@link Mono}.
     *
     * <!-- src_embed com.azure.ai.vision.imageanalysis.async-analysis-from-file -->
     * <pre>
     * &#47;&#47;
     * &#47;&#47; Asynchronous analysis of an image file &#40;sample.jpg&#41;, using all visual features,
     * &#47;&#47; with all options set. You will likely need fewer visual features and only
     * &#47;&#47; some &#40;or no&#41; options set.
     * &#47;&#47;
     * ImageAnalysisOptions options = new ImageAnalysisOptions&#40;&#41;
     *     .setLanguage&#40;&quot;en&quot;&#41;
     *     .setGenderNeutralCaption&#40;true&#41;
     *     .setSmartCropsAspectRatios&#40;Arrays.asList&#40;0.9, 1.33&#41;&#41;
     *     .setModelVersion&#40;&quot;latest&quot;&#41;;
     *
     * Mono&lt;ImageAnalysisResult&gt; result = client.analyze&#40;
     *     BinaryData.fromFile&#40;new File&#40;&quot;sample.jpg&quot;&#41;.toPath&#40;&#41;&#41;,
     *     Arrays.asList&#40;
     *         VisualFeatures.SMART_CROPS,
     *         VisualFeatures.CAPTION,
     *         VisualFeatures.DENSE_CAPTIONS,
     *         VisualFeatures.OBJECTS,
     *         VisualFeatures.PEOPLE,
     *         VisualFeatures.READ,
     *         VisualFeatures.TAGS&#41;,
     *     options&#41;;
     * </pre>
     * <!-- end com.azure.ai.vision.imageanalysis.async-analysis-from-file -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ImageAnalysisResult> analyze(BinaryData imageData, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        return analyzeWithResponse(imageData, visualFeatures, options, null).map(response -> {
            return response.getValue();
        });
    }

    /**
     * Performs a single Image Analysis operation on a give image URL, while allowing customization
     * of the HTTP request and access to HTTP request and response details. This is not commonly used.
     *
     * @param imageUrl The publicly accessible URL of the image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param imageAnalysisOptions Additional image analysis options.
     * @param requestOptions The options to configure the HTTP request before the HTTP client sends it.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation along with <a href="https://learn.microsoft.com/java/api/com.azure.core.http.rest.response">Response</a>,
     * on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ImageAnalysisResult>> analyzeFromUrlWithResponse(String imageUrl,
        List<VisualFeatures> visualFeatures, ImageAnalysisOptions imageAnalysisOptions, RequestOptions requestOptions) {
        List<String> visualFeaturesAsStrings = visualFeatures.stream()
            .map(paramItemValue -> Objects.toString(paramItemValue, "")).collect(Collectors.toList());
        Mono<Response<BinaryData>> monoResponse = analyzeFromUrlWithResponse(visualFeaturesAsStrings,
            BinaryData.fromObject(new ImageUrl(imageUrl)), ImageAnalysisClient.updateRequestOptions(requestOptions, imageAnalysisOptions));
        return monoResponse.map(response -> {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                response.getValue().toObject(ImageAnalysisResult.class));
        });
    }

    /**
     * Performs a single Image Analysis operation on a given image buffer, while allowing customization
     * of the HTTP request and access to HTTP request and response details. This is not commonly used.
     *
     * @param imageData The image to analyze.
     * @param visualFeatures A list of visual features to analyze.
     * Seven visual features are supported: Caption, DenseCaptions, Read (OCR), Tags, Objects, SmartCrops, and People.
     * At least one visual feature must be specified.
     * @param imageAnalysisOptions Additional image analysis options.
     * @param requestOptions The options to configure the HTTP request before the HTTP client sends it.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     *
     * @return represents the outcome of an Image Analysis operation along with <a href="https://learn.microsoft.com/java/api/com.azure.core.http.rest.response">Response</a>,
     * on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ImageAnalysisResult>> analyzeWithResponse(BinaryData imageData,
        List<VisualFeatures> visualFeatures, ImageAnalysisOptions imageAnalysisOptions, RequestOptions requestOptions) {
        List<String> visualFeaturesAsStrings = visualFeatures.stream()
            .map(paramItemValue -> Objects.toString(paramItemValue, "")).collect(Collectors.toList());
        Mono<Response<BinaryData>> monoResponse = analyzeFromImageDataWithResponse(visualFeaturesAsStrings, imageData,
            ImageAnalysisClient.updateRequestOptions(requestOptions, imageAnalysisOptions));
        return monoResponse.map(response -> {
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                response.getValue().toObject(ImageAnalysisResult.class));
        });
    }
```