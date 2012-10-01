/**
 * Copyright 2012 Microsoft Corporation
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
import java.util.EnumSet;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.AccessPolicyType;

public class AccessPolicyInfo extends ODataEntity<AccessPolicyType> {

    public AccessPolicyInfo(EntryType entry, AccessPolicyType content) {
        super(entry, content);
    }

    public AccessPolicyInfo() {
        super(new AccessPolicyType());
    }

    public String getId() {
        return getContent().getId();
    }

    public AccessPolicyInfo setId(String id) {
        getContent().setId(id);
        return this;
    }

    public Date getCreated() {
        return getContent().getCreated();
    }

    public AccessPolicyInfo setCreated(Date created) {
        getContent().setCreated(created);
        return this;
    }

    public Date getLastModified() {
        return getContent().getLastModified();
    }

    public AccessPolicyInfo setLastModified(Date lastModified) {
        getContent().setLastModified(lastModified);
        return this;
    }

    public String getName() {
        return getContent().getName();
    }

    public AccessPolicyInfo setName(String name) {
        getContent().setName(name);
        return this;
    }

    public double getDurationInMinutes() {
        return getContent().getDurationInMinutes();
    }

    public AccessPolicyInfo setDurationInMinutes(double durationInMinutes) {
        getContent().setDurationInMinutes(durationInMinutes);
        return this;
    }

    public EnumSet<AccessPolicyPermission> getPermissions() {
        return AccessPolicyPermission.permissionsFromBits(getContent().getPermissions());
    }

    public AccessPolicyInfo setPermissions(EnumSet<AccessPolicyPermission> permissions) {
        getContent().setPermissions(AccessPolicyPermission.bitsFromPermissions(permissions));
        return this;
    }
}
