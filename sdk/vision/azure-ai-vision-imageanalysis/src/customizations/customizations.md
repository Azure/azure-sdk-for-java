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

The following two public methods were added to the class `ImageAnalysisClient` (file `ImageAnalysisClient.java`):

```java
import java.net.URL;

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
     * <!-- end com.azure.ai.vision.imageanalysis.sync-analysis-from-url -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ImageAnalysisResult analyze(URL imageUrl, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), null, null, null, null);
        } else {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), options.getLanguage(),
                options.isGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }

    /**
     * Performs a single Image Analysis operation on a give image buffer.
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
     * <!-- end com.azure.ai.vision.imageanalysis.sync-analysis-from-file -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ImageAnalysisResult analyze(BinaryData imageData, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromBuffer(visualFeatures, imageData, null, null, null, null);
        } else {
            return analyzeFromBuffer(visualFeatures, imageData, options.getLanguage(),
                options.isGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }
```

## Updates to class ImageAnalysisAsyncClient

The following two public methods were added to the class `ImageAnalysisAsyncClient` (file `ImageAnalysisAsyncClient.java`):

```java
import java.net.URL;

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
     * <!-- end com.azure.ai.vision.imageanalysis.async-analysis-from-url -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ImageAnalysisResult> analyze(URL imageUrl, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), null, null, null, null);
        } else {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), options.getLanguage(),
                options.isGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }

    /**
     * Performs a single Image Analysis operation on a give image buffer.
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
     * <!-- end com.azure.ai.vision.imageanalysis.async-analysis-from-file -->
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ImageAnalysisResult> analyze(BinaryData imageData, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromBuffer(visualFeatures, imageData, null, null, null, null);
        } else {
            return analyzeFromBuffer(visualFeatures, imageData, options.getLanguage(),
                options.isGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }
```