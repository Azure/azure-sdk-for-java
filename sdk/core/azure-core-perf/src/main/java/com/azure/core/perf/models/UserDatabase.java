// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserDatabase {

    @JsonProperty("userdata")
    private List<UserData> userList;

    public UserDatabase() {
    }

    public List<UserData> getUserList() {
        return userList;
    }

    public void setUserList(List<UserData> userList) {
        this.userList = userList;
    }
}
