// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.implementation;

import com.azure.ai.vision.face.models.FaceAttributeType;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.core.http.rest.RequestOptions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ClientUtils {
    public static void addRequiredQueryParameterForDetection(
        RequestOptions requestOptions, FaceDetectionModel detectionModel, FaceRecognitionModel recognitionModel, boolean returnFaceId) {
        ClientUtils.addRequireQueryParameters(requestOptions, "detectionModel", detectionModel);
        ClientUtils.addRequireQueryParameters(requestOptions, "recognitionModel", recognitionModel);
        ClientUtils.addRequireQueryParameters(requestOptions, "returnFaceId", returnFaceId);
    }

    public static void addOptionalQueryParameterForDetection(RequestOptions requestOptions, List<FaceAttributeType> returnFaceAttributes,
        Boolean returnFaceLandmarks, Boolean returnRecognitionModel, Integer faceIdTimeToLive) {
        if (returnFaceAttributes != null) {
            requestOptions.addQueryParam("returnFaceAttributes",
                returnFaceAttributes.stream()
                    .map(paramItemValue -> Objects.toString(paramItemValue, ""))
                    .collect(Collectors.joining(",")),
                false);
        }

        if (returnFaceLandmarks != null) {
            requestOptions.addQueryParam("returnFaceLandmarks", String.valueOf(returnFaceLandmarks), false);
        }

        if (returnRecognitionModel != null) {
            requestOptions.addQueryParam("returnRecognitionModel", String.valueOf(returnRecognitionModel), false);
        }

        if (faceIdTimeToLive != null) {
            requestOptions.addQueryParam("faceIdTimeToLive", String.valueOf(faceIdTimeToLive), false);
        }
    }

    private static void addRequireQueryParameters(RequestOptions requestOptions, String parameterName, Object paramterObject) {
        if (paramterObject == null) {
            throw new NullPointerException("Query Parameter '" + parameterName + "' is null");
        }

        requestOptions.addQueryParam(parameterName, paramterObject.toString());
    }
}
