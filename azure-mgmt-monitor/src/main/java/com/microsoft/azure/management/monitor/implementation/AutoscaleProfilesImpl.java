/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleProfile;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for AutoscaleProfile collection.
 */
@LangDefinition
class AutoscaleProfilesImpl extends
        ExternalChildResourcesCachedImpl<AutoscaleProfileImpl,
                        AutoscaleProfile,
                        AutoscaleProfileInner,
                        AutoscaleSettingImpl,
                        AutoscaleSetting> {

    AutoscaleProfilesImpl(AutoscaleSettingImpl parent) {
        super(parent, "AutoscaleProfile");
        if (parent.id() != null) {
            this.cacheCollection();
        }
    }

    @Override
    protected List<AutoscaleProfileImpl> listChildResources() {
        List<AutoscaleProfileImpl> childResources = new ArrayList<>();

        for (AutoscaleProfileInner innerProfile : this.parent().inner().profiles()) {
            AutoscaleProfileImpl profile = new AutoscaleProfileImpl(innerProfile.name(), this.parent(), innerProfile);
            childResources.add(profile);
        }
        return Collections.unmodifiableList(childResources);
    }

    @Override
    protected AutoscaleProfileImpl newChildResource(String name) {
        return new AutoscaleProfileImpl(name, this.parent(), new AutoscaleProfileInner());
    }

    Map<String, AutoscaleProfile> profilesAsMap() {
        Map<String, AutoscaleProfile> result = new HashMap<>();
        for (Map.Entry<String, AutoscaleProfileImpl> entry : this.collection().entrySet()) {
            AutoscaleProfileImpl profile = entry.getValue();
            result.put(entry.getKey(), profile);
        }
        return Collections.unmodifiableMap(result);
    }

    public void removeProfile(String name) {
        if(this.collection().size() == 1) {
            throw new IllegalArgumentException("Autoscale Setting '" + this.parent().name() + "' cannot contain at least 1 Autoscale Profile.");
        }
        this.prepareRemove(name);
    }

    public AutoscaleProfileImpl defineProfile(String name) {
        if(this.collection().size() == 20) {
            throw new IllegalArgumentException("Autoscale Setting '" + this.parent().name() + "' cannot have more than 20 Autoscale Profiles.");
        }
        return this.prepareDefine(name);
    }

    public AutoscaleProfileImpl updateProfile(String name) {
        return this.prepareUpdate(name);
    }
}
