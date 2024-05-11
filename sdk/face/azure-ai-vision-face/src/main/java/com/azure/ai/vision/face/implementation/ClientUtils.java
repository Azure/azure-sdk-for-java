// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.implementation;

import com.azure.ai.vision.face.models.FaceAttributeType;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
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

    public static <T> List<T> listDeserializationHelperSync(BinaryData json, ReadValueCallback<JsonReader, T> deserializer) {
        try (JsonReader jsonReader = JsonProviders.createReader(json.toBytes())) {
            return jsonReader.readArray(deserializer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> Mono<List<T>> listDeserializationHelperAsync(BinaryData json,
                                                                   ReadValueCallback<JsonReader, T> deserializer) {
        return Mono.usingWhen(FluxUtil.collectBytesInByteBufferStream(json.toFluxByteBuffer()).flatMap(bytes ->
                Mono.fromCallable(() -> JsonProviders.createReader(bytes))),
            jsonReader -> Mono.fromCallable(() -> jsonReader.readArray(deserializer)),
            jsonReader -> Mono.fromCallable(() -> {
                jsonReader.close();
                return null;
            }));
    }

    private static void addRequireQueryParameters(RequestOptions requestOptions, String parameterName, Object paramterObject) {
        if (paramterObject == null) {
            throw new NullPointerException("Query Parameter '" + parameterName + "' is null");
        }

        requestOptions.addQueryParam(parameterName, paramterObject.toString());
    }
}
