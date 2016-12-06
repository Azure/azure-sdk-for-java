package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionInner;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.Map;

/**
 * An immutable client-side representation of an extension associated with virtual machine.
 */
@Fluent
public interface VirtualMachineExtensionBase extends
        Wrapper<VirtualMachineExtensionInner> {
    /**
     * @return the publisher name of the virtual machine extension image this extension is created from
     */
    String publisherName();

    /**
     * @return the type name of the virtual machine extension image this extension is created from
     */
    String typeName();

    /**
     * @return the version name of the virtual machine extension image this extension is created from
     */
    String versionName();

    /**
     * @return true if this extension is configured to upgrade automatically when a new minor version of the
     * extension image that this extension based on is published
     */
    boolean autoUpgradeMinorVersionEnabled();

    /**
     * @return the public settings of the virtual machine extension as key value pairs
     */
    Map<String, Object> publicSettings();

    /**
     * @return the public settings of the virtual machine extension as a JSON string
     */
    String publicSettingsAsJsonString();

    /**
     * @return the provisioning state of the virtual machine extension
     */
    String provisioningState();

    /**
     * @return the instance view of the virtual machine extension
     */
    VirtualMachineExtensionInstanceView instanceView();

    /**
     * @return the tags for this virtual machine extension
     */
    Map<String, String> tags();
}
