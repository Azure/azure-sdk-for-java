---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-computer-vision
urlFragment: image-analysis-samples
---

# Samples for Image Analysis client library for Java

These Java samples show how to use the Image Analysis client library. They cover all the supported visual features. Most use the a synchronous client to analyze an image file or image URL. Two samples use the asynchronous client. The concepts are similar, you can easily modify any of the samples to your needs.

## Prerequisites

See [Prerequisites](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/README.md#prerequisites).

## Adding the package to your product

See [Adding the package to your product](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/README.md#adding-the-package-to-your-product).

## Synchronous client samples

Unless otherwise noted, clients are authenticated using API key.

|**File Name**|**Description**|
|----------------|-------------|
|[SampleAnalyzeAllImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleAnalyzeAllImageFile.java) | Extract all 7 visual features from an image file, using a synchronous client. Logging turned on.|
|[SampleCaptionImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleCaptionImageFile.java), [SampleCaptionImageFileWithResponse.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleCaptionImageFileWithResponse.java) and [SampleCaptionImageUrl.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleCaptionImageUrl.java)| Generate a human-readable sentence that describes the content of an image file or image URL, using a synchronous client. One sample also shows how to get the HTTP [Response](https://learn.microsoft.com/java/api/com.azure.core.http.rest.response?view=azure-java-stable). |
|[SampleCaptionImageFileEntraIdAuth.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleCaptionImageFileEntraIdAuth.java)| Generate a human-readable sentence that describes the content of an image file, using a synchronous client, with Entra ID authentication. |
|[SampleDenseCaptionsImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleDenseCaptionsImageFile.java) | Generating a human-readable caption for up to 10 different regions in the image, including one for the whole image, using a synchronous client.|
|[SampleOcrImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleOcrImageFile.java) and [SampleOcrImageUrl.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleOcrImageUrl.java)|  Extract printed or handwritten text from an image file or image URL, using a synchronous client. |
|[SampleTagsImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleTagsImageFile.java) | Extract content tags for thousands of recognizable objects, living beings, scenery, and actions that appear in an image file, using a synchronous client. |
|[SampleObjectsImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleObjectsImageFile.java) | Detect physical objects in an image file and return their location, using a synchronous client. |
|[SampleSmartCropsImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleSmartCropsImageFile.java) | Find a representative sub-region of the image for thumbnail generation, using a synchronous client .| 
|[SamplePeopleImageFile.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SamplePeopleImageFile.java) | Locate people in the image and return their location, using a synchronous client.|

## Asynchronous client samples

Unless otherwise noted, clients are authenticated using API key.

|**File Name**|**Description**|
|----------------|-------------|
|[SampleCaptionImageFileAsync.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleCaptionImageFileAsync.java) | Generate a human-readable sentence that describes the content of an image file, using an asynchronous client. |
|[SampleCaptionImageFileEntraIdAuthAsync.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleCaptionImageFileEntraIdAuthAsync.java)| Generate a human-readable sentence that describes the content of an image file, using an asynchronous client, with Entra ID authentication. |
|[SampleOcrImageUrlAsync.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleOcrImageUrlAsync.java) | Extract printed or handwritten text from an image URL, using an asynchronous client. |
|[SampleOcrImageUrlWithResponseAsync.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/SampleOcrImageUrlWithResponseAsync.java) | Extract printed or handwritten text from an image URL, using an asynchronous client, with HTTP [Response](https://learn.microsoft.com/java/api/com.azure.core.http.rest.response?view=azure-java-stable). |

## Example console output

The sample `SampleAnalyzeAllImageFile.java` does analysis of the local image file `sample.jpg` using all seven visual features, and prints the result to the console. This is the image:

![sample JPEG image](https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/vision/azure-ai-vision-imageanalysis/src/samples/java/com/azure/ai/vision/imageanalysis/sample.jpg)

When you run it, it will produce console output similar to the following:

```
Image analysis results:
 Caption:
   "a person wearing a mask sitting at a table with a laptop", Confidence 0.8498
 Dense Captions:
   "a person wearing a mask sitting at a table with a laptop", Bounding box {x=0, y=0, width=864, height=576}, Confidence 0.8498
   "a person using a laptop", Bounding box {x=293, y=383, width=195, height=100}, Confidence 0.7724
   "a person wearing a face mask", Bounding box {x=383, y=233, width=275, height=336}, Confidence 0.8209
   "a close-up of a green chair", Bounding box {x=616, y=211, width=164, height=249}, Confidence 0.8763
   "a person wearing a colorful cloth face mask", Bounding box {x=473, y=294, width=68, height=56}, Confidence 0.7086
   "a person using a laptop", Bounding box {x=288, y=211, width=151, height=244}, Confidence 0.7642
   "a person wearing a colorful fabric face mask", Bounding box {x=433, y=240, width=180, height=236}, Confidence 0.7734
   "a close-up of a laptop on a table", Bounding box {x=115, y=443, width=476, height=125}, Confidence 0.8537
   "a person wearing a mask and using a laptop", Bounding box {x=0, y=0, width=774, height=432}, Confidence 0.7816
   "a close up of a text", Bounding box {x=714, y=493, width=130, height=80}, Confidence 0.6407
 Read:
   Line: 'Sample text', Bounding polygon [(x=721, y=502), (x=843, y=502), (x=843, y=519), (x=721, y=519)]
     Word: 'Sample', Bounding polygon [(x=722, y=503), (x=785, y=503), (x=785, y=520), (x=722, y=520)], Confidence 0.9930
     Word: 'text', Bounding polygon [(x=800, y=503), (x=842, y=502), (x=842, y=519), (x=800, y=520)], Confidence 0.9890
   Line: 'Hand writing', Bounding polygon [(x=720, y=525), (x=819, y=526), (x=819, y=544), (x=720, y=543)]
     Word: 'Hand', Bounding polygon [(x=721, y=526), (x=759, y=526), (x=759, y=544), (x=721, y=543)], Confidence 0.9890
     Word: 'writing', Bounding polygon [(x=765, y=526), (x=819, y=527), (x=819, y=545), (x=765, y=544)], Confidence 0.9940
   Line: '123 456', Bounding polygon [(x=721, y=548), (x=791, y=548), (x=791, y=563), (x=721, y=564)]
     Word: '123', Bounding polygon [(x=723, y=548), (x=750, y=548), (x=750, y=564), (x=723, y=564)], Confidence 0.9940
     Word: '456', Bounding polygon [(x=761, y=548), (x=788, y=549), (x=787, y=564), (x=760, y=564)], Confidence 0.9990
 Tags:
   "furniture", Confidence 0.9874
   "clothing", Confidence 0.9793
   "person", Confidence 0.9427
   "houseplant", Confidence 0.9400
   "desk", Confidence 0.9183
   "indoor", Confidence 0.8964
   "laptop", Confidence 0.8782
   "computer", Confidence 0.8482
   "sitting", Confidence 0.8135
   "wall", Confidence 0.7512
   "woman", Confidence 0.7411
   "table", Confidence 0.6811
   "plant", Confidence 0.6445
   "using", Confidence 0.5359
 Objects:
   "chair", Bounding box {x=603, y=225, width=152, height=224}, Confidence 0.6180
   "person", Bounding box {x=399, y=244, width=249, height=325}, Confidence 0.8810
   "Laptop", Bounding box {x=295, y=387, width=211, height=102}, Confidence 0.7670
   "chair", Bounding box {x=441, y=436, width=256, height=136}, Confidence 0.5810
   "dining table", Bounding box {x=123, y=437, width=460, height=125}, Confidence 0.6060
 People:
   Bounding box {x=395, y=241, width=261, height=333}, Confidence 0.9603
   Bounding box {x=831, y=246, width=31, height=255}, Confidence 0.0017
 Crop Suggestions:
   Aspect ratio 0.9: Bounding box {x=238, y=0, width=511, height=568}
   Aspect ratio 1.33: Bounding box {x=54, y=0, width=760, height=571}
 Image height = 576
 Image width = 864
 Model version = 2023-10-01
```

## Troubleshooting

See [Troubleshooting](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/README.md#troubleshooting).

