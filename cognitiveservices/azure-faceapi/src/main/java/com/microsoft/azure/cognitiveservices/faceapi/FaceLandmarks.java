/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A collection of 27-point face landmarks pointing to the important positions
 * of face components.
 */
public class FaceLandmarks {
    /**
     * The pupilLeft property.
     */
    @JsonProperty(value = "pupilLeft")
    private Position pupilLeft;

    /**
     * The pupilRight property.
     */
    @JsonProperty(value = "pupilRight")
    private Position pupilRight;

    /**
     * The noseTip property.
     */
    @JsonProperty(value = "noseTip")
    private Position noseTip;

    /**
     * The mouthLeft property.
     */
    @JsonProperty(value = "mouthLeft")
    private Position mouthLeft;

    /**
     * The mouthRight property.
     */
    @JsonProperty(value = "mouthRight")
    private Position mouthRight;

    /**
     * The eyebrowLeftOuter property.
     */
    @JsonProperty(value = "eyebrowLeftOuter")
    private Position eyebrowLeftOuter;

    /**
     * The eyebrowLeftInner property.
     */
    @JsonProperty(value = "eyebrowLeftInner")
    private Position eyebrowLeftInner;

    /**
     * The eyeLeftOuter property.
     */
    @JsonProperty(value = "eyeLeftOuter")
    private Position eyeLeftOuter;

    /**
     * The eyeLeftTop property.
     */
    @JsonProperty(value = "eyeLeftTop")
    private Position eyeLeftTop;

    /**
     * The eyeLeftBottom property.
     */
    @JsonProperty(value = "eyeLeftBottom")
    private Position eyeLeftBottom;

    /**
     * The eyeLeftInner property.
     */
    @JsonProperty(value = "eyeLeftInner")
    private Position eyeLeftInner;

    /**
     * The eyebrowRightInner property.
     */
    @JsonProperty(value = "eyebrowRightInner")
    private Position eyebrowRightInner;

    /**
     * The eyebrowRightOuter property.
     */
    @JsonProperty(value = "eyebrowRightOuter")
    private Position eyebrowRightOuter;

    /**
     * The eyeRightInner property.
     */
    @JsonProperty(value = "eyeRightInner")
    private Position eyeRightInner;

    /**
     * The eyeRightTop property.
     */
    @JsonProperty(value = "eyeRightTop")
    private Position eyeRightTop;

    /**
     * The eyeRightBottom property.
     */
    @JsonProperty(value = "eyeRightBottom")
    private Position eyeRightBottom;

    /**
     * The eyeRightOuter property.
     */
    @JsonProperty(value = "eyeRightOuter")
    private Position eyeRightOuter;

    /**
     * The noseRootLeft property.
     */
    @JsonProperty(value = "noseRootLeft")
    private Position noseRootLeft;

    /**
     * The noseRootRight property.
     */
    @JsonProperty(value = "noseRootRight")
    private Position noseRootRight;

    /**
     * The noseLeftAlarTop property.
     */
    @JsonProperty(value = "noseLeftAlarTop")
    private Position noseLeftAlarTop;

    /**
     * The noseRightAlarTop property.
     */
    @JsonProperty(value = "noseRightAlarTop")
    private Position noseRightAlarTop;

    /**
     * The noseLeftAlarOutTip property.
     */
    @JsonProperty(value = "noseLeftAlarOutTip")
    private Position noseLeftAlarOutTip;

    /**
     * The noseRightAlarOutTip property.
     */
    @JsonProperty(value = "noseRightAlarOutTip")
    private Position noseRightAlarOutTip;

    /**
     * The upperLipTop property.
     */
    @JsonProperty(value = "upperLipTop")
    private Position upperLipTop;

    /**
     * The upperLipBottom property.
     */
    @JsonProperty(value = "upperLipBottom")
    private Position upperLipBottom;

    /**
     * The underLipTop property.
     */
    @JsonProperty(value = "underLipTop")
    private Position underLipTop;

    /**
     * The underLipBottom property.
     */
    @JsonProperty(value = "underLipBottom")
    private Position underLipBottom;

    /**
     * Get the pupilLeft value.
     *
     * @return the pupilLeft value
     */
    public Position pupilLeft() {
        return this.pupilLeft;
    }

    /**
     * Set the pupilLeft value.
     *
     * @param pupilLeft the pupilLeft value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withPupilLeft(Position pupilLeft) {
        this.pupilLeft = pupilLeft;
        return this;
    }

    /**
     * Get the pupilRight value.
     *
     * @return the pupilRight value
     */
    public Position pupilRight() {
        return this.pupilRight;
    }

    /**
     * Set the pupilRight value.
     *
     * @param pupilRight the pupilRight value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withPupilRight(Position pupilRight) {
        this.pupilRight = pupilRight;
        return this;
    }

    /**
     * Get the noseTip value.
     *
     * @return the noseTip value
     */
    public Position noseTip() {
        return this.noseTip;
    }

    /**
     * Set the noseTip value.
     *
     * @param noseTip the noseTip value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withNoseTip(Position noseTip) {
        this.noseTip = noseTip;
        return this;
    }

    /**
     * Get the mouthLeft value.
     *
     * @return the mouthLeft value
     */
    public Position mouthLeft() {
        return this.mouthLeft;
    }

    /**
     * Set the mouthLeft value.
     *
     * @param mouthLeft the mouthLeft value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withMouthLeft(Position mouthLeft) {
        this.mouthLeft = mouthLeft;
        return this;
    }

    /**
     * Get the mouthRight value.
     *
     * @return the mouthRight value
     */
    public Position mouthRight() {
        return this.mouthRight;
    }

    /**
     * Set the mouthRight value.
     *
     * @param mouthRight the mouthRight value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withMouthRight(Position mouthRight) {
        this.mouthRight = mouthRight;
        return this;
    }

    /**
     * Get the eyebrowLeftOuter value.
     *
     * @return the eyebrowLeftOuter value
     */
    public Position eyebrowLeftOuter() {
        return this.eyebrowLeftOuter;
    }

    /**
     * Set the eyebrowLeftOuter value.
     *
     * @param eyebrowLeftOuter the eyebrowLeftOuter value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyebrowLeftOuter(Position eyebrowLeftOuter) {
        this.eyebrowLeftOuter = eyebrowLeftOuter;
        return this;
    }

    /**
     * Get the eyebrowLeftInner value.
     *
     * @return the eyebrowLeftInner value
     */
    public Position eyebrowLeftInner() {
        return this.eyebrowLeftInner;
    }

    /**
     * Set the eyebrowLeftInner value.
     *
     * @param eyebrowLeftInner the eyebrowLeftInner value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyebrowLeftInner(Position eyebrowLeftInner) {
        this.eyebrowLeftInner = eyebrowLeftInner;
        return this;
    }

    /**
     * Get the eyeLeftOuter value.
     *
     * @return the eyeLeftOuter value
     */
    public Position eyeLeftOuter() {
        return this.eyeLeftOuter;
    }

    /**
     * Set the eyeLeftOuter value.
     *
     * @param eyeLeftOuter the eyeLeftOuter value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeLeftOuter(Position eyeLeftOuter) {
        this.eyeLeftOuter = eyeLeftOuter;
        return this;
    }

    /**
     * Get the eyeLeftTop value.
     *
     * @return the eyeLeftTop value
     */
    public Position eyeLeftTop() {
        return this.eyeLeftTop;
    }

    /**
     * Set the eyeLeftTop value.
     *
     * @param eyeLeftTop the eyeLeftTop value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeLeftTop(Position eyeLeftTop) {
        this.eyeLeftTop = eyeLeftTop;
        return this;
    }

    /**
     * Get the eyeLeftBottom value.
     *
     * @return the eyeLeftBottom value
     */
    public Position eyeLeftBottom() {
        return this.eyeLeftBottom;
    }

    /**
     * Set the eyeLeftBottom value.
     *
     * @param eyeLeftBottom the eyeLeftBottom value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeLeftBottom(Position eyeLeftBottom) {
        this.eyeLeftBottom = eyeLeftBottom;
        return this;
    }

    /**
     * Get the eyeLeftInner value.
     *
     * @return the eyeLeftInner value
     */
    public Position eyeLeftInner() {
        return this.eyeLeftInner;
    }

    /**
     * Set the eyeLeftInner value.
     *
     * @param eyeLeftInner the eyeLeftInner value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeLeftInner(Position eyeLeftInner) {
        this.eyeLeftInner = eyeLeftInner;
        return this;
    }

    /**
     * Get the eyebrowRightInner value.
     *
     * @return the eyebrowRightInner value
     */
    public Position eyebrowRightInner() {
        return this.eyebrowRightInner;
    }

    /**
     * Set the eyebrowRightInner value.
     *
     * @param eyebrowRightInner the eyebrowRightInner value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyebrowRightInner(Position eyebrowRightInner) {
        this.eyebrowRightInner = eyebrowRightInner;
        return this;
    }

    /**
     * Get the eyebrowRightOuter value.
     *
     * @return the eyebrowRightOuter value
     */
    public Position eyebrowRightOuter() {
        return this.eyebrowRightOuter;
    }

    /**
     * Set the eyebrowRightOuter value.
     *
     * @param eyebrowRightOuter the eyebrowRightOuter value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyebrowRightOuter(Position eyebrowRightOuter) {
        this.eyebrowRightOuter = eyebrowRightOuter;
        return this;
    }

    /**
     * Get the eyeRightInner value.
     *
     * @return the eyeRightInner value
     */
    public Position eyeRightInner() {
        return this.eyeRightInner;
    }

    /**
     * Set the eyeRightInner value.
     *
     * @param eyeRightInner the eyeRightInner value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeRightInner(Position eyeRightInner) {
        this.eyeRightInner = eyeRightInner;
        return this;
    }

    /**
     * Get the eyeRightTop value.
     *
     * @return the eyeRightTop value
     */
    public Position eyeRightTop() {
        return this.eyeRightTop;
    }

    /**
     * Set the eyeRightTop value.
     *
     * @param eyeRightTop the eyeRightTop value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeRightTop(Position eyeRightTop) {
        this.eyeRightTop = eyeRightTop;
        return this;
    }

    /**
     * Get the eyeRightBottom value.
     *
     * @return the eyeRightBottom value
     */
    public Position eyeRightBottom() {
        return this.eyeRightBottom;
    }

    /**
     * Set the eyeRightBottom value.
     *
     * @param eyeRightBottom the eyeRightBottom value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeRightBottom(Position eyeRightBottom) {
        this.eyeRightBottom = eyeRightBottom;
        return this;
    }

    /**
     * Get the eyeRightOuter value.
     *
     * @return the eyeRightOuter value
     */
    public Position eyeRightOuter() {
        return this.eyeRightOuter;
    }

    /**
     * Set the eyeRightOuter value.
     *
     * @param eyeRightOuter the eyeRightOuter value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withEyeRightOuter(Position eyeRightOuter) {
        this.eyeRightOuter = eyeRightOuter;
        return this;
    }

    /**
     * Get the noseRootLeft value.
     *
     * @return the noseRootLeft value
     */
    public Position noseRootLeft() {
        return this.noseRootLeft;
    }

    /**
     * Set the noseRootLeft value.
     *
     * @param noseRootLeft the noseRootLeft value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withNoseRootLeft(Position noseRootLeft) {
        this.noseRootLeft = noseRootLeft;
        return this;
    }

    /**
     * Get the noseRootRight value.
     *
     * @return the noseRootRight value
     */
    public Position noseRootRight() {
        return this.noseRootRight;
    }

    /**
     * Set the noseRootRight value.
     *
     * @param noseRootRight the noseRootRight value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withNoseRootRight(Position noseRootRight) {
        this.noseRootRight = noseRootRight;
        return this;
    }

    /**
     * Get the noseLeftAlarTop value.
     *
     * @return the noseLeftAlarTop value
     */
    public Position noseLeftAlarTop() {
        return this.noseLeftAlarTop;
    }

    /**
     * Set the noseLeftAlarTop value.
     *
     * @param noseLeftAlarTop the noseLeftAlarTop value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withNoseLeftAlarTop(Position noseLeftAlarTop) {
        this.noseLeftAlarTop = noseLeftAlarTop;
        return this;
    }

    /**
     * Get the noseRightAlarTop value.
     *
     * @return the noseRightAlarTop value
     */
    public Position noseRightAlarTop() {
        return this.noseRightAlarTop;
    }

    /**
     * Set the noseRightAlarTop value.
     *
     * @param noseRightAlarTop the noseRightAlarTop value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withNoseRightAlarTop(Position noseRightAlarTop) {
        this.noseRightAlarTop = noseRightAlarTop;
        return this;
    }

    /**
     * Get the noseLeftAlarOutTip value.
     *
     * @return the noseLeftAlarOutTip value
     */
    public Position noseLeftAlarOutTip() {
        return this.noseLeftAlarOutTip;
    }

    /**
     * Set the noseLeftAlarOutTip value.
     *
     * @param noseLeftAlarOutTip the noseLeftAlarOutTip value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withNoseLeftAlarOutTip(Position noseLeftAlarOutTip) {
        this.noseLeftAlarOutTip = noseLeftAlarOutTip;
        return this;
    }

    /**
     * Get the noseRightAlarOutTip value.
     *
     * @return the noseRightAlarOutTip value
     */
    public Position noseRightAlarOutTip() {
        return this.noseRightAlarOutTip;
    }

    /**
     * Set the noseRightAlarOutTip value.
     *
     * @param noseRightAlarOutTip the noseRightAlarOutTip value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withNoseRightAlarOutTip(Position noseRightAlarOutTip) {
        this.noseRightAlarOutTip = noseRightAlarOutTip;
        return this;
    }

    /**
     * Get the upperLipTop value.
     *
     * @return the upperLipTop value
     */
    public Position upperLipTop() {
        return this.upperLipTop;
    }

    /**
     * Set the upperLipTop value.
     *
     * @param upperLipTop the upperLipTop value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withUpperLipTop(Position upperLipTop) {
        this.upperLipTop = upperLipTop;
        return this;
    }

    /**
     * Get the upperLipBottom value.
     *
     * @return the upperLipBottom value
     */
    public Position upperLipBottom() {
        return this.upperLipBottom;
    }

    /**
     * Set the upperLipBottom value.
     *
     * @param upperLipBottom the upperLipBottom value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withUpperLipBottom(Position upperLipBottom) {
        this.upperLipBottom = upperLipBottom;
        return this;
    }

    /**
     * Get the underLipTop value.
     *
     * @return the underLipTop value
     */
    public Position underLipTop() {
        return this.underLipTop;
    }

    /**
     * Set the underLipTop value.
     *
     * @param underLipTop the underLipTop value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withUnderLipTop(Position underLipTop) {
        this.underLipTop = underLipTop;
        return this;
    }

    /**
     * Get the underLipBottom value.
     *
     * @return the underLipBottom value
     */
    public Position underLipBottom() {
        return this.underLipBottom;
    }

    /**
     * Set the underLipBottom value.
     *
     * @param underLipBottom the underLipBottom value to set
     * @return the FaceLandmarks object itself.
     */
    public FaceLandmarks withUnderLipBottom(Position underLipBottom) {
        this.underLipBottom = underLipBottom;
        return this;
    }

}
