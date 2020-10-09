// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.Map;

/** An immutable client-side representation of an extension associated with virtual machine. */
@Fluent
public interface VirtualMachineExtensionBase extends HasInnerModel<VirtualMachineExtensionInner> {
    /** @return the publisher name of the virtual machine extension image this extension is created from */
    String publisherName();

    /** @return the type name of the virtual machine extension image this extension is created from */
    String typeName();

    /** @return the version name of the virtual machine extension image this extension is created from */
    String versionName();

    /**
     * @return true if this extension is configured to upgrade automatically when a new minor version of the extension
     *     image that this extension based on is published
     */
    boolean autoUpgradeMinorVersionEnabled();

    /** @return the public settings of the virtual machine extension as key value pairs */
    Map<String, Object> publicSettings();

    /** @return the public settings of the virtual machine extension as a JSON string */
    String publicSettingsAsJsonString();

    /** @return the provisioning state of the virtual machine extension */
    String provisioningState();

    /** @return the tags for this virtual machine extension */
    Map<String, String> tags();
}
