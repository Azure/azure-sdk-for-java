/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.ResourceIdentityType;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetExtension;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetIdentity;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Utility class to set Managed Service Identity (MSI) and MSI related resources for a virtual machine scale set.
 */
class VirtualMachineScaleSetMsiHelper {
    private final int defaultTokenPort = 50342;
    private final String msiExtensionPublisher = "Microsoft.ManagedIdentity";
    private final GraphRbacManager rbacManager;
    private BuiltInRole role;
    private String scope;
    private Integer tokenPort;
    private boolean requireSetup;

    /**
     * Creates VirtualMachineScaleSetMsiHelper.
     *
     * @param rbacManager the graph rbac manager
     */
    VirtualMachineScaleSetMsiHelper(GraphRbacManager rbacManager) {
        this.rbacManager = rbacManager;
        this.clear();
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine scale set.
     * <p>
     * Once setupVirtualMachineMSIResourcesAsync is invoked,  applications running on the virtual machine
     * scale set instance will have "Contributor" access role with scope of access limited to the resource
     * group that this virtual machine scale set belongs to. If MSI extension is already installed then the
     * access token will be available in the virtual machine scale set instance at port specified in the
     * extension public setting, otherwise the port for new extension will be 50342.
     *
     * @param scaleSetInner the virtual machine scale set to set the identity
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withManagedServiceIdentity(VirtualMachineScaleSetInner scaleSetInner) {
        return withManagedServiceIdentity(BuiltInRole.CONTRIBUTOR,
                null,
                null,
                scaleSetInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine scale set.
     * <p>
     * Once setupVirtualMachineScaleSetMSIResourcesAsync is invoked,  applications running on the virtual machine
     * scale set instance will have the given access role and scope of access will be limited to the resource
     * group that this virtual machine scale set belongs to. If MSI extension is already installed then the access
     * token will be available in the virtual machine scale set instance at port specified in the extension public
     * setting, otherwise the port for new extension will be 50342.
     *
     * @param role          the role
     * @param scaleSetInner the virtual machine scale set to set the identity
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withManagedServiceIdentity(BuiltInRole role,
                                                               VirtualMachineScaleSetInner scaleSetInner) {
        return withManagedServiceIdentity(role,
                null,
                null,
                scaleSetInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine scale set.
     * <p>
     * Once setupVirtualMachineMSIResourcesAsync is invoked,  applications running on the virtual machine
     * scale set instance  will have the given access role and scope of access will be limited to the arm
     * resource identified by resource id specified in the scope parameter. If MSI extension is already installed
     * then the access token will be available in the virtual machine scale set instance at port specified in the
     * extension public setting, otherwise the port for new extension will be 50342.
     *
     * @param role          access role to assigned to the virtual machine
     * @param scope         scope of the access represented in arm resource id format
     * @param scaleSetInner the virtual machine scale set to set the identity
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withManagedServiceIdentity(BuiltInRole role,
                                                               String scope,
                                                               VirtualMachineScaleSetInner scaleSetInner) {
        return withManagedServiceIdentity(role,
                scope,
                null,
                scaleSetInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine scale set.
     * <p>
     * Once setupVirtualMachineMSIResourcesAsync is invoked, applications running on the virtual machine
     * scale set instance will have the given access role and scope of access will be limited to the arm
     * resource identified by resource id specified in the scope parameter. The access token will be available
     * in the virtual machine scale set instance at given port.
     *
     * @param role          access role to assigned to the virtual machine scale set
     * @param scope         scope of the access represented in arm resource id format
     * @param port          the port in the virtual machine to get the access token from
     * @param scaleSetInner the virtual machine scale set to set the identity
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withManagedServiceIdentity(BuiltInRole role,
                                                               String scope,
                                                               Integer port,
                                                               VirtualMachineScaleSetInner scaleSetInner) {
        this.requireSetup = true;
        this.role = role;
        this.scope = scope;
        this.tokenPort = port;
        setIdentityType(scaleSetInner);
        return this;
    }

    /**
     * Add or update the Managed Service Identity extension for the given virtual machine scale set.
     *
     * @param scaleSetImpl the scale set
     */
    void addOrUpdateMSIExtension(VirtualMachineScaleSetImpl scaleSetImpl) {
        if (!requireSetup) {
            return;
        }
        // To add or update MSI extension, we relay on methods exposed from interfaces instead of from
        // impl so that any breaking change in the contract cause a compile time error here. So do not
        // change the below 'updateExtension' or 'defineNewExtension' to use impls.
        //
        String msiExtensionType = msiExtensionType(scaleSetImpl.osTypeIntern());
        VirtualMachineScaleSetExtension msiExtension = getMSIExtension(scaleSetImpl.extensions(), msiExtensionType);
        if (msiExtension != null) {
            Object currentTokenPortObj = msiExtension.publicSettings().get("port");
            Integer currentTokenPort = objectToInteger(currentTokenPortObj);
            Integer newPort;
            if (this.tokenPort != null) {
                // user specified a port
                newPort = this.tokenPort;
            } else if (currentTokenPort != null) {
                // user didn't specify a port and currently there is a port
                newPort = currentTokenPort;
            } else {
                // user didn't specify a port and currently there is no port
                newPort = defaultTokenPort;
            }
            VirtualMachineScaleSet.Update appliableVMSS = scaleSetImpl;
            appliableVMSS.updateExtension(msiExtension.name())
                    .withPublicSetting("port", newPort)
                    .parent();
        } else {
            Integer port;
            if (this.tokenPort != null) {
                port = this.tokenPort;
            } else {
                port = defaultTokenPort;
            }
            if (scaleSetImpl.isInCreateMode()) {
                VirtualMachineScaleSet.DefinitionStages.WithCreate creatableVMSS = scaleSetImpl;
                creatableVMSS.defineNewExtension(msiExtensionType)
                        .withPublisher(msiExtensionPublisher)
                        .withType(msiExtensionType)
                        .withVersion("1.0")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("port", port)
                        .attach();
            } else {
                VirtualMachineScaleSet.Update appliableVMSS = scaleSetImpl;
                appliableVMSS.defineNewExtension(msiExtensionType)
                        .withPublisher(msiExtensionPublisher)
                        .withType(msiExtensionType)
                        .withVersion("1.0")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("port", port)
                        .attach();
            }
        }
    }

    /**
     * Install or update the MSI extension in the virtual machine scale set and creates a RBAC role assignment
     * for the auto created service principal with the given role and scope.
     *
     * @param scaleSet the virtual machine scale set for which the MSI needs to be enabled
     * @return the observable that emits result of MSI resource setup.
     */
    Observable<RoleAssignment> setupVirtualMachineScaleSetMSIResourcesAsync(final VirtualMachineScaleSet scaleSet) {
        if (!requireSetup) {
            return Observable.just(null);
        }
        if (!scaleSet.isManagedServiceIdentityEnabled()) {
            // The principal id and tenant id needs to be set before performing role assignment
            //
            return Observable.just(null);
        }
        return createRbacRoleAssignmentIfNotExistsAsync(scaleSet);
    }

    /**
     * Creates RBAC role assignment for the virtual machine scale set service principal if it does not exists.
     *
     * @param scaleSet the virtual machine scale set
     * @return an observable that emits role assignment if it created, if it already exists emits null.
     */
    private Observable<RoleAssignment> createRbacRoleAssignmentIfNotExistsAsync(final VirtualMachineScaleSet scaleSet) {
        final String roleAssignmentName = SdkContext.randomUuid();
        return rbacManager
                .servicePrincipals()
                .getByIdAsync(scaleSet.inner().identity().principalId())
                .flatMap(new Func1<ServicePrincipal, Observable<RoleAssignment>>() {
                    @Override
                    public Observable<RoleAssignment> call(final ServicePrincipal principal) {
                        return resolveScopeAsync(scaleSet)
                                .flatMap(new Func1<String, Observable<RoleAssignment>>() {
                                    @Override
                                    public Observable<RoleAssignment> call(String scope) {
                                        return rbacManager
                                                .roleAssignments()
                                                .define(roleAssignmentName)
                                                .forServicePrincipal(principal)
                                                .withBuiltInRole(role)
                                                .withScope(scope)
                                                .createAsync()
                                                .last()
                                                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Indexable>>() {
                                                    @Override
                                                    public Observable<? extends Indexable> call(Throwable throwable) {
                                                        if (throwable instanceof CloudException) {
                                                            CloudException exception = (CloudException) throwable;
                                                            if (exception.body() != null
                                                                    && exception.body().code() != null
                                                                    && exception.body().code().equalsIgnoreCase("RoleAssignmentExists")) {
                                                                // NOTE: We are unable to lookup the role assignment from principal.roleAssignments() list
                                                                // because role assignment object does not contain 'role' name (the roleDefinitionId refer
                                                                // 'role' using id with GUID).
                                                                //
                                                                return Observable.just(null);
                                                            }
                                                        }
                                                        return Observable.<Indexable>error(throwable);
                                                    }
                                                })
                                                .map(new Func1<Indexable, RoleAssignment>() {
                                                    @Override
                                                    public RoleAssignment call(Indexable indexable) {
                                                        return (RoleAssignment) indexable;
                                                    }
                                                });
                                    }
                                });

                    }
                });
    }

    /**
     * Resolve the scope for the role.
     *
     * @param virtualMachineScaleSet the virtual machine scale set
     * @return the scope
     */
    private Observable<String> resolveScopeAsync(final VirtualMachineScaleSet virtualMachineScaleSet) {
        if (this.scope != null) {
            return Observable.just(this.scope);
        } else {
            // TODO: Remove fromCallable wrapper once we have getByNameAsync implemented.
            //
            return Observable.fromCallable(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return virtualMachineScaleSet.manager()
                            .resourceManager()
                            .resourceGroups()
                            .getByName(virtualMachineScaleSet.resourceGroupName())
                            .id();
                }
            }).subscribeOn(SdkContext.getRxScheduler());
        }
    }

    /**
     * Sets the identity property of the virtual machine scale set.
     *
     * @param scaleSetInner the virtual machine scale set
     */
    private void setIdentityType(VirtualMachineScaleSetInner scaleSetInner) {
        if (scaleSetInner.identity() == null) {
            scaleSetInner.withIdentity(new VirtualMachineScaleSetIdentity());
        }
        if (scaleSetInner.identity().type() == null) {
            scaleSetInner.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED);
        }
    }

    /**
     * Given the OS type, gets the Managed Service Identity extension type.
     * 
     * @param osType the os type
     *
     * @return the extension type.
     */
    private String msiExtensionType(OperatingSystemTypes osType) {
        return osType == OperatingSystemTypes.LINUX ? "ManagedIdentityExtensionForLinux" : "ManagedIdentityExtensionForWindows";
    }

    /**
     * Gets the Managed Service Identity extension from the given extensions.
     *
     * @param extensions the extensions
     * @param typeName the extension type
     * @return the MSI extension if exists, null otherwise
     */
    private VirtualMachineScaleSetExtension getMSIExtension(Map<String, VirtualMachineScaleSetExtension> extensions, String typeName) {
        for (VirtualMachineScaleSetExtension extension : extensions.values()) {
            if (extension.publisherName().equalsIgnoreCase(msiExtensionPublisher)) {
                if (extension.typeName().equalsIgnoreCase(typeName)) {
                    return extension;
                }
            }
        }
        return null;
    }

    /**
     * Given an object holding a numeric in Integer or String format, convert that to
     * Integer.
     *
     * @param obj the object
     * @return the integer value
     */
    private Integer objectToInteger(Object obj) {
        Integer result = null;
        if (obj != null) {
            if (obj instanceof Integer) {
                result = (Integer) obj;
            } else {
                result = Integer.valueOf((String) obj);
            }
        }
        return result;
    }

    /**
     * Clear internal properties.
     */
    private  void clear() {
        this.requireSetup = false;
        this.role = null;
        this.scope = null;
        this.tokenPort = null;
    }
}
