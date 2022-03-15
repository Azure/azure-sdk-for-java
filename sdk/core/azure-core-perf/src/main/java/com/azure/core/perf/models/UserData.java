// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserData implements Cloneable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("guid")
    private String guid;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("balance")
    private String balance;

    @JsonProperty("picture")
    private String picture;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("eyeColor")
    private String eyeColor;

    @JsonProperty("company")
    private String company;

    @JsonProperty("about")
    private String about;


    public UserData() { }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(final Integer index) {
        this.index = index;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(final Boolean active) {
        isActive = active;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(final String balance) {
        this.balance = balance;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(final String picture) {
        this.picture = picture;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(final String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        this.company = company;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(final String about) {
        this.about = about;
    }

    @Override
    public UserData clone() {
        UserData userData = null;
        try {
            userData = (UserData) super.clone();
        } catch (CloneNotSupportedException e) {
            userData = new UserData();
            userData.setPicture(this.getPicture());
            userData.setId(this.getId());
            userData.setIndex(this.getIndex());
            userData.setEyeColor(this.getEyeColor());
            userData.setCompany(this.getCompany());
            userData.setAge(this.getAge());
            userData.setGuid(this.getGuid());
            userData.setActive(this.getActive());
            userData.setBalance(this.getBalance());
            userData.setAbout(this.getAbout());
        }
        return userData;
    }
}
