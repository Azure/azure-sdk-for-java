# Java Image Analysis SDK Customization

This document specifies the hand-customization done to the auto-generated Java Image Analysis SDK.

## Add ImageBoundingBox.toString()

Add the following method to the class ImageBoundingBox in file ImageBoundingBox.java:
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

Add the following method to the bottom of the class ImagePoint in file ImagePoint.java

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

Copy the new file ImageAnalysisOptions.java in to the folder: `src\main\java\com\azure\ai\vision\imageanalysis`

## Updates to ImageAnalysisClient.java

Add the following two methods at the bottom of the source file:

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
     */
    public ImageAnalysisResult analyze(URL imageUrl, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), null, null, null, null);
        } else {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), options.getLanguage(),
                options.getGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }

    /**
     * Performs a single Image Analysis operation on a give image buffer.
     *
     * @param imageBuffer The image to analyze.
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
     */
    public ImageAnalysisResult analyze(BinaryData imageBuffer, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromBuffer(visualFeatures, imageBuffer, null, null, null, null);
        } else {
            return analyzeFromBuffer(visualFeatures, imageBuffer, options.getLanguage(),
                options.getGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }
```

## Updates to ImageAnalysisAsyncClient.java

Add the following two methods at the bottom of the source file:

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
     */
    public Mono<ImageAnalysisResult> analyze(URL imageUrl, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), null, null, null, null);
        } else {
            return analyzeFromUrl(visualFeatures, new ImageUrl(imageUrl.toString()), options.getLanguage(),
                options.getGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }

    /**
     * Performs a single Image Analysis operation on a give image buffer.
     *
     * @param imageBuffer The image to analyze.
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
     */
    public Mono<ImageAnalysisResult> analyze(BinaryData imageBuffer, List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) {
        if (options == null) {
            return analyzeFromBuffer(visualFeatures, imageBuffer, null, null, null, null);
        } else {
            return analyzeFromBuffer(visualFeatures, imageBuffer, options.getLanguage(),
                options.getGenderNeutralCaption(), options.getSmartCropsAspectRatios(), options.getModelVersion());
        }
    }
```