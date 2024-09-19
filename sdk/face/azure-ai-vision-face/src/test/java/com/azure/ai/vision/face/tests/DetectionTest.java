// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests;

import com.azure.ai.vision.face.FaceAsyncClient;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceServiceVersion;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.tests.commands.detection.DetectSyncFunction;
import com.azure.ai.vision.face.tests.commands.detection.DetectionFunctionProvider;
import com.azure.ai.vision.face.tests.utils.FaceDisplayNameGenerator;
import com.azure.ai.vision.face.tests.utils.TestUtils;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@DisplayNameGeneration(FaceDisplayNameGenerator.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RecordWithoutRequestBody
public class DetectionTest extends FaceClientTestBase {

    @ParameterizedTest
    @MethodSource("getDataFortTestDetectFaceReturnFaceIdAndReturnRecognitionModel")
    public void testDetectFaceReturnFaceIdAndReturnRecognitionModel(
            String httpClientName, FaceServiceVersion serviceVersion, Supplier<DetectSyncFunction> detectionFunctionSupplier,
            boolean returnFaceId, boolean returnRecognitionModel) {

        DetectSyncFunction detectionFunction = detectionFunctionSupplier.get();

        FaceRecognitionModel recognitionModel = FaceRecognitionModel.RECOGNITION_04;
        List<FaceDetectionResult> detectResults = detectionFunction.execute(
                FaceDetectionModel.DETECTION_03, recognitionModel, returnFaceId, null,
                null, returnRecognitionModel, null);

        Assertions.assertNotNull(detectResults);

        Assertions.assertEquals(detectResults.size(), 1);
        FaceDetectionResult result = detectResults.get(0);

        Assertions.assertEquals(returnFaceId, result.getFaceId() != null);

        FaceRecognitionModel expectReturnedModel = (returnFaceId && returnRecognitionModel) ? recognitionModel : null;
        Assertions.assertEquals(expectReturnedModel, result.getRecognitionModel());
    }

    private Stream<Arguments> getDataFortTestDetectFaceReturnFaceIdAndReturnRecognitionModel() {
        Boolean[] booleanArray = {false, true};
        DetectionFunctionProvider[] providers = DetectionFunctionProvider.getFunctionProviders(
                Resources.TEST_IMAGE_PATH_FAMILY1_DAD1, Resources.TEST_IMAGE_URL_DETECT_SAMPLE);

        Stream<Tuple3<String, FaceServiceVersion, Supplier<DetectSyncFunction>>> clientArumentStream =
                createClientArgumentStream(FaceClient.class, FaceAsyncClient.class, providers);

        return TestUtils.createCombinationWithClientArguments(clientArumentStream, booleanArray, booleanArray);
    }
}
