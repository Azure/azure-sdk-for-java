/*
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;

public class Locator {
    private String id;
    private Date expirationDatetime;
    private String path;
    private String accessPolicyId;
    private String assetId;
    private Date startTime;
    private LocatorType locatorType;

    public String getId() {
        return this.id;
    }

    public Locator setId(String id) {
        this.id = id;
        return this;
    }

    public Locator setExpirationDateTime(Date expirationDateTime) {
        this.expirationDatetime = expirationDateTime;
        return this;
    }

    public Date getExpirationDateTime() {
        return this.expirationDatetime;
    }

    public Locator setLocatorType(LocatorType locatorType) {
        this.locatorType = locatorType;
        return this;
    }

    public LocatorType getLocatorType() {
        return this.locatorType;
    }

    public Locator setPath(String path) {
        this.path = path;
        return this;
    }

    public String getPath() {
        return this.path;
    }

    public Locator setAccessPolicyId(String accessPolicyId) {
        this.accessPolicyId = accessPolicyId;
        return this;
    }

    public String getAccessPolicyId() {
        return this.accessPolicyId;
    }

    public Locator setAssetId(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getAssetId() {
        return this.assetId;
    }

    public Locator setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getStartTime() {
        return this.startTime;
    }

}
