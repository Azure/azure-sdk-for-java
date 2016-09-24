package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionImageInner;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure virtual machine extension image version.
 */
@LangDefinition  // No @Fluent, since methods -> Property conversion is what we needed
public interface VirtualMachineExtensionImageVersion extends
        Wrapper<VirtualMachineExtensionImageInner> {
    /**
     * @return the resource ID of the extension image version
     */
    String id();

    /**
     * @return the name of the virtual machine extension image version
     */
    String name();

    /**
     * @return the region in which virtual machine extension image version is available
     */
    String regionName();

    /**
     * @return the virtual machine extension image type this version belongs to
     */
    @LangMethodDefinition(AsType =  LangMethodDefinition.LangMethodType.Property) // By default emitted as method adding this to emit Property
    VirtualMachineExtensionImageType type();

    /**
     * @return virtual machine extension image this version represents
     */
    @Method
    VirtualMachineExtensionImage image();
}