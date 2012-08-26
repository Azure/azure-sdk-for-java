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

public class Asset {
    private String id;
    private AssetState state;
    private Date created;
    private Date lastModified;
    private String alternateId;
    private String name;
    private EncryptionOption options;
    private Iterable<Locator> locators;
    private Iterable<ContentKey> contentKeys;
    private Iterable<File> files;
    private Iterable<Asset> parentAssets;

    public String getId() {
        return this.id;
    }

    public Asset setId(String id) {
        this.id = id;
        return this;
    }

    public AssetState getState() {
        return this.state;
    }

    public Asset setState(AssetState state) {
        this.state = state;
        return this;
    }

    public Date getCreated() {
        return this.created;
    }

    public Asset setCreate(Date created) {
        this.created = created;
        return this;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public Asset setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String getAlternateId() {
        return this.alternateId;
    }

    public Asset setAlternateId(String alternateId) {
        this.alternateId = alternateId;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Asset setName(String name) {
        this.name = name;
        return this;
    }

    public EncryptionOption getOptions() {
        return this.options;
    }

    public Asset setOptions(EncryptionOption options) {
        this.options = options;
        return this;
    }

    public Iterable<Locator> getLocators() {
        return this.locators;
    }

    public Asset setLocators(Iterable<Locator> locators) {
        this.locators = locators;
        return this;
    }

    public Iterable<ContentKey> getContentKeys() {
        return this.contentKeys;
    }

    public Asset setContentKeys(Iterable<ContentKey> expectedContentKeys) {
        this.contentKeys = expectedContentKeys;
        return this;
    }

    public Iterable<File> getFiles() {
        return this.files;
    }

    public Asset setFiles(Iterable<File> files) {
        this.files = files;
        return this;
    }

    public Iterable<Asset> getParentAssets() {
        return this.parentAssets;
    }

    public Asset setParentAssets(Iterable<Asset> parentAssets) {
        this.parentAssets = parentAssets;
        return this;
    }

}
