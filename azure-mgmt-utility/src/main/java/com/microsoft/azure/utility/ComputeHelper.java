/**
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility;

public class ComputeHelper {
    public final static String Subscriptions = "subscriptions";
    public final static String ResourceGroups = "resourceGroups";
    public final static String Providers = "providers";
    public final static String AvailabilitySets = "availabilitySets";
    public final static String ResourceProviderNamespace = "Microsoft.Compute";
    public final static String VirtualMachines = "virtualMachines";

    private static String getEntityReferenceId(
            String subId, String resourceGrpName, String controllerName, String entityName)
    {
        return String.format("/%s/%s/%s/%s/%s/%s/%s/%s",
                Subscriptions, subId, ResourceGroups, resourceGrpName,
                Providers, ResourceProviderNamespace, controllerName,
                entityName);
    }

    public static String getAvailabilitySetRef(String subId, String resourceGrpName, String availabilitySetName) {
        return getEntityReferenceId(subId, resourceGrpName, AvailabilitySets, availabilitySetName);
    }

    public static String getVMReferenceId(String subId, String rgName, String vmName) {
        return getEntityReferenceId(subId, rgName, VirtualMachines, vmName);
    }
}
