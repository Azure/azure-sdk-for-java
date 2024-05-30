// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceAsyncClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.DetectOptions;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;

import java.util.Arrays;

import static com.azure.ai.vision.face.models.FaceAttributeType.ModelDetection01;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelDetection03;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelRecognition04;
import static com.azure.ai.vision.face.samples.utils.Utils.log;

public class DetectFacesAsync {
    public static void main(String[] args) {
        FaceAsyncClient client = new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildAsyncClient();

        BinaryData imageBinary = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_DETECT_SAMPLE_IMAGE);
        Flux<FaceDetectionResult> flux = client.detect(
                        imageBinary,
            FaceDetectionModel.DETECTION_03,
            FaceRecognitionModel.RECOGNITION_04,
            true,
            Arrays.asList(ModelDetection03.HEAD_POSE, ModelDetection03.MASK, ModelDetection03.BLUR, ModelRecognition04.QUALITY_FOR_RECOGNITION),
            false,
            true,
            120)
            .flatMapMany(Flux::fromIterable);

        flux.subscribe(face -> log("Detected Face by file:" + Utils.toString(face) + "\n"));

        DetectOptions options = new DetectOptions(FaceDetectionModel.DETECTION_01, FaceRecognitionModel.RECOGNITION_04, false)
            .setReturnFaceAttributes(Arrays.asList(ModelDetection01.ACCESSORIES, ModelDetection01.GLASSES, ModelDetection01.EXPOSURE, ModelDetection01.NOISE))
            .setReturnFaceLandmarks(true);

        flux = client.detect(Resources.TEST_IMAGE_URL_DETECT_SAMPLE, options)
            .flatMapMany(Flux::fromIterable);

        flux.subscribe(face -> log("Detected Face from URL:" + Utils.toString(face) + "\n"));


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
