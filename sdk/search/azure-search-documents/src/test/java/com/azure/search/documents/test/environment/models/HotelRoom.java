// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "Description", "Description_fr", "Type", "BaseRate", "BedOptions", "BedOptions", "SleepsCount",
})
public class HotelRoom {
    @JsonProperty(value = "Description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonProperty(value = "Description_fr")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String descriptionFr;

    @JsonProperty(value = "Type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String type;

    @JsonProperty(value = "BaseRate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double baseRate;

    @JsonProperty(value = "BedOptions")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bedOptions;

    @JsonProperty(value = "SleepsCount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer sleepsCount;

    @JsonProperty(value = "SmokingAllowed")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean smokingAllowed;

    @JsonProperty(value = "Tags")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[] tags;


    public String description() {
        return this.description;
    }

    public HotelRoom description(String description) {
        this.description = description;
        return this;
    }

    public String descriptionFr() {
        return this.descriptionFr;
    }

    public HotelRoom descriptionFr(String descriptionFr) {
        this.descriptionFr = descriptionFr;
        return this;
    }

    public String type() {
        return this.type;
    }

    public HotelRoom type(String type) {
        this.type = type;
        return this;
    }

    public Double baseRate() {
        return this.baseRate;
    }

    public HotelRoom baseRate(Double baseRate) {
        this.baseRate = baseRate;
        return this;
    }

    public String bedOptions() {
        return this.bedOptions;
    }

    public HotelRoom bedOptions(String bedOptions) {
        this.bedOptions = bedOptions;
        return this;
    }

    public Integer sleepsCount() {
        return this.sleepsCount;
    }

    public HotelRoom sleepsCount(Integer sleepsCount) {
        this.sleepsCount = sleepsCount;
        return this;
    }

    public Boolean smokingAllowed() {
        return this.smokingAllowed;
    }

    public HotelRoom smokingAllowed(Boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
        return this;
    }

    public String[] tags() {
        return CoreUtils.clone(this.tags);
    }

    public HotelRoom tags(String[] tags) {
        this.tags = CoreUtils.clone(tags);
        return this;
    }
}
