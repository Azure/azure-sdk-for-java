// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ActionFeatures {
    @JsonGetter
    public String getVideoType() {
        return videoType;
    }

    @JsonSetter
    public ActionFeatures setVideoType(String videoType) {
        this.videoType = videoType;
        return this;
    }

    @JsonGetter
    public Integer getVideoLength() {
        return videoLength;
    }

    @JsonSetter
    public ActionFeatures setVideoLength(Integer videoLength) {
        this.videoLength = videoLength;
        return this;
    }

    @JsonGetter
    public String getDirector() {
        return director;
    }

    @JsonSetter
    public ActionFeatures setDirector(String director) {
        this.director = director;
        return this;
    }

    @JsonProperty
    String videoType;
    @JsonProperty
    Integer videoLength;
    @JsonProperty
    String director;
}
