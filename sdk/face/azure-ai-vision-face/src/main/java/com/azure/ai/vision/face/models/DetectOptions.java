// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Options for detectFromUrl API.
 */
@Fluent
public final class DetectOptions {
    /*
     * Return faceIds of the detected faces or not. The default value is true.
     */
    private boolean returnFaceId;

    /*
     * Return face landmarks of the detected faces or not. The default value is false.
     */
    private Boolean returnFaceLandmarks;

    /*
     * Analyze and return the one or more specified face attributes in the comma-separated string like 'returnFaceAttributes=headPose,glasses'. Face attribute analysis has additional computational and time cost.
     */
    private List<FaceAttributeType> returnFaceAttributes;

    /*
     * The 'recognitionModel' associated with the detected faceIds. Supported 'recognitionModel' values include 'recognition_01', 'recognition_02', 'recognition_03' or 'recognition_04'. The default value is 'recognition_01'. 'recognition_04' is recommended since its accuracy is improved on faces wearing masks compared with 'recognition_03', and its overall accuracy is improved compared with 'recognition_01' and 'recognition_02'.
     */
    private FaceRecognitionModel recognitionModel;

    /*
     * Return 'recognitionModel' or not. The default value is false.
     */
    private Boolean returnRecognitionModel;

    /*
     * The 'detectionModel' associated with the detected faceIds. Supported 'detectionModel' values include 'detection_01', 'detection_02' and 'detection_03'. The default value is 'detection_01'.
     */
    private FaceDetectionModel detectionModel;

    /*
     * The number of seconds for the face ID being cached. Supported range from 60 seconds up to 86400 seconds. The default value is 86400 (24 hours).
     */
    private Integer faceIdTimeToLive;

    /**
     * Creates an instance of DetectFromUrlOptions class.
     *
     * @param detectionModel The 'detectionModel' associated with the detected faceIds. Supported 'detectionModel'
     * values include 'detection_01', 'detection_02' and 'detection_03'. The default value is 'detection_01'
     * @param recognitionModel The 'recognitionModel' associated with the detected faceIds. Supported 'recognitionModel'
     * values include 'recognition_01', 'recognition_02', 'recognition_03' or 'recognition_04'. The default value is
     * 'recognition_01'. 'recognition_04' is recommended since its accuracy is improved on faces wearing masks compared
     * with 'recognition_03', and its overall accuracy is improved compared with 'recognition_01' and 'recognition_02'.
     * @param returnFaceId Return faceIds of the detected faces or not. The default value is true.
     */
    public DetectOptions(FaceDetectionModel detectionModel, FaceRecognitionModel recognitionModel, boolean returnFaceId) {
        this.detectionModel = detectionModel;
        this.recognitionModel = recognitionModel;
        this.returnFaceId = returnFaceId;
    }

    /**
     * Get the returnFaceId property: Return faceIds of the detected faces or not. The default value is true.
     *
     * @return the returnFaceId value.
     */
    public boolean isReturnFaceId() {
        return this.returnFaceId;
    }

    /**
     * Set the returnFaceId property: Return faceIds of the detected faces or not. The default value is true.
     *
     * @param returnFaceId the returnFaceId value to set.
     * @return the DetectFromUrlOptions object itself.
     */
    public DetectOptions setReturnFaceId(boolean returnFaceId) {
        this.returnFaceId = returnFaceId;
        return this;
    }

    /**
     * Get the returnFaceLandmarks property: Return face landmarks of the detected faces or not. The default value is
     * false.
     *
     * @return the returnFaceLandmarks value.
     */
    public Boolean isReturnFaceLandmarks() {
        return this.returnFaceLandmarks;
    }

    /**
     * Set the returnFaceLandmarks property: Return face landmarks of the detected faces or not. The default value is
     * false.
     *
     * @param returnFaceLandmarks the returnFaceLandmarks value to set.
     * @return the DetectFromUrlOptions object itself.
     */
    public DetectOptions setReturnFaceLandmarks(Boolean returnFaceLandmarks) {
        this.returnFaceLandmarks = returnFaceLandmarks;
        return this;
    }

    /**
     * Get the returnFaceAttributes property: Analyze and return the one or more specified face attributes in the
     * comma-separated string like 'returnFaceAttributes=headPose,glasses'. Face attribute analysis has additional
     * computational and time cost.
     *
     * @return the returnFaceAttributes value.
     */
    public List<FaceAttributeType> getReturnFaceAttributes() {
        return this.returnFaceAttributes;
    }

    /**
     * Set the returnFaceAttributes property: Analyze and return the one or more specified face attributes in the
     * comma-separated string like 'returnFaceAttributes=headPose,glasses'. Face attribute analysis has additional
     * computational and time cost.
     *
     * @param returnFaceAttributes the returnFaceAttributes value to set.
     * @return the DetectFromUrlOptions object itself.
     */
    public DetectOptions setReturnFaceAttributes(List<FaceAttributeType> returnFaceAttributes) {
        this.returnFaceAttributes = returnFaceAttributes;
        return this;
    }

    /**
     * Get the recognitionModel property: The 'recognitionModel' associated with the detected faceIds. Supported
     * 'recognitionModel' values include 'recognition_01', 'recognition_02', 'recognition_03' or 'recognition_04'. The
     * default value is 'recognition_01'. 'recognition_04' is recommended since its accuracy is improved on faces
     * wearing masks compared with 'recognition_03', and its overall accuracy is improved compared with 'recognition_01'
     * and 'recognition_02'.
     *
     * @return the recognitionModel value.
     */
    public FaceRecognitionModel getRecognitionModel() {
        return this.recognitionModel;
    }

    /**
     * Set the recognitionModel property: The 'recognitionModel' associated with the detected faceIds. Supported
     * 'recognitionModel' values include 'recognition_01', 'recognition_02', 'recognition_03' or 'recognition_04'. The
     * default value is 'recognition_01'. 'recognition_04' is recommended since its accuracy is improved on faces
     * wearing masks compared with 'recognition_03', and its overall accuracy is improved compared with 'recognition_01'
     * and 'recognition_02'.
     *
     * @param recognitionModel the recognitionModel value to set.
     * @return the DetectFromUrlOptions object itself.
     */
    public DetectOptions setRecognitionModel(FaceRecognitionModel recognitionModel) {
        this.recognitionModel = recognitionModel;
        return this;
    }

    /**
     * Get the returnRecognitionModel property: Return 'recognitionModel' or not. The default value is false.
     *
     * @return the returnRecognitionModel value.
     */
    public Boolean isReturnRecognitionModel() {
        return this.returnRecognitionModel;
    }

    /**
     * Set the returnRecognitionModel property: Return 'recognitionModel' or not. The default value is false.
     *
     * @param returnRecognitionModel the returnRecognitionModel value to set.
     * @return the DetectFromUrlOptions object itself.
     */
    public DetectOptions setReturnRecognitionModel(Boolean returnRecognitionModel) {
        this.returnRecognitionModel = returnRecognitionModel;
        return this;
    }

    /**
     * Get the detectionModel property: The 'detectionModel' associated with the detected faceIds. Supported
     * 'detectionModel' values include 'detection_01', 'detection_02' and 'detection_03'. The default value is
     * 'detection_01'.
     *
     * @return the detectionModel value.
     */
    public FaceDetectionModel getDetectionModel() {
        return this.detectionModel;
    }

    /**
     * Set the detectionModel property: The 'detectionModel' associated with the detected faceIds. Supported
     * 'detectionModel' values include 'detection_01', 'detection_02' and 'detection_03'. The default value is
     * 'detection_01'.
     *
     * @param detectionModel the detectionModel value to set.
     * @return the DetectFromUrlOptions object itself.
     */
    public DetectOptions setDetectionModel(FaceDetectionModel detectionModel) {
        this.detectionModel = detectionModel;
        return this;
    }

    /**
     * Get the faceIdTimeToLive property: The number of seconds for the face ID being cached. Supported range from 60
     * seconds up to 86400 seconds. The default value is 86400 (24 hours).
     *
     * @return the faceIdTimeToLive value.
     */
    public Integer getFaceIdTimeToLive() {
        return this.faceIdTimeToLive;
    }

    /**
     * Set the faceIdTimeToLive property: The number of seconds for the face ID being cached. Supported range from 60
     * seconds up to 86400 seconds. The default value is 86400 (24 hours).
     *
     * @param faceIdTimeToLive the faceIdTimeToLive value to set.
     * @return the DetectFromUrlOptions object itself.
     */
    public DetectOptions setFaceIdTimeToLive(Integer faceIdTimeToLive) {
        this.faceIdTimeToLive = faceIdTimeToLive;
        return this;
    }
}
