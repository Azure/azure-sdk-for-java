// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class UserFeatures {
    @JsonGetter
    public boolean isPayingUser() {
        return isPayingUser;
    }

    @JsonSetter
    public UserFeatures setPayingUser(boolean payingUser) {
        isPayingUser = payingUser;
        return this;
    }

    @JsonGetter
    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    @JsonSetter
    public UserFeatures setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
        return this;
    }

    @JsonGetter
    public double getHoursOnSite() {
        return hoursOnSite;
    }

    @JsonSetter
    public UserFeatures setHoursOnSite(double hoursOnSite) {
        this.hoursOnSite = hoursOnSite;
        return this;
    }

    @JsonGetter
    public String getLastWatchedType() {
        return lastWatchedType;
    }

    @JsonSetter
    public UserFeatures setLastWatchedType(String lastWatchedType) {
        this.lastWatchedType = lastWatchedType;
        return this;
    }

    @JsonProperty
    private boolean isPayingUser;
    @JsonProperty
    private String favoriteGenre;
    @JsonProperty
    private double hoursOnSite;
    @JsonProperty
    private String lastWatchedType;
}
