
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Shared managed vs unmanaged disks errors between virtual machine and virtual machine scale set.
 */
@LangDefinition
class ManagedUnmanagedDiskErrors {
    static String VM_BOTH_MANAGED_AND_UNMANAGED_DISK_NOT_ALLOWED = "This virtual machine is based on managed disk(s), both un-managed and managed disk cannot exists together in a virtual machine";
    static String VMSS_BOTH_MANAGED_AND_UNMANAGED_DISK_NOT_ALLOWED = "This virtual machine scale set is based on managed disk(s), both un-managed and managed cannot exists together in a virtual machine scale set";
    static String VM_NO_UNMANAGED_DISK_TO_UPDATE = "This virtual machine is based on managed disk(s) and there is no un-managed disk to update";
    static String VM_NO_MANAGED_DISK_TO_UPDATE = "This virtual machine is based on un-managed disk(s) and there is no managed disk to update";
    static String VMSS_NO_UNMANAGED_DISK_TO_UPDATE = "This virtual machine scale set is based on managed disk(s) and there is no un-managed disk to update";
    static String VMSS_NO_MANAGED_DISK_TO_UPDATE = "This virtual machine scale set is based on un-managed disk(s) and there is no managed disk to update";
    static String VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED = "This virtual machine is based on un-managed disks (s), both un-managed and managed disk cannot exists together in a virtual machine";
    static String VMSS_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED = "This virtual machine scale set is based on un-managed disk(s), both un-managed and managed cannot exists together in a virtual machine scale set";
}
