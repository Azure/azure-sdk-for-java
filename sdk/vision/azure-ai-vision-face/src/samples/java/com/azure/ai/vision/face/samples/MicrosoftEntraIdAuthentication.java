// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;

import static com.azure.ai.vision.face.samples.utils.Utils.logObject;

public class MicrosoftEntraIdAuthentication {
    public static void main(String[] args) {
        //Create a FaceClient using Microsoft Entra ID to authenticate. We use DefaultAzureCredential to achieve it.
        FaceClient client = new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        //Verify if the authentication works or not.
        BinaryData imageBinary = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_DETECT_SAMPLE_IMAGE);
        List<FaceDetectionResult> results =
            client.detect(imageBinary, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, false);

        logObject("FaceDetected: ", results.get(0));
    }
}
