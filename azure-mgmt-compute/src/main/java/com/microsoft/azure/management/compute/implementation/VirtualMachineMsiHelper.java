/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.ResourceIdentityType;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineIdentity;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Utility class to set Managed Service Identity (MSI) and MSI related resources for a virtual machine.
 */
class VirtualMachineMsiHelper {
    private final int defaultTokenPort = 50342;
    private final GraphRbacManager rbacManager;
    private BuiltInRole role;
    private String scope;
    private Integer tokenPort;
    private boolean requireSetup;

    /**
     * Creates VirtualMachineMsiHelper.
     *
     * @param rbacManager the graph rbac manager
     */
    VirtualMachineMsiHelper(final GraphRbacManager rbacManager) {
        this.rbacManager = rbacManager;
        clear();
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine.
     *
     * Once setupVirtualMachineMSIResourcesAsync is invoked,  applications running on the virtual machine will
     * have "Contributor" access role with scope of access limited to the resource group that this
     * virtual machine belongs to. The access token will be available in the virtual machine at port 50342.
     *
     * @param virtualMachineInner the virtual machine to set the identity
     * @return VirtualMachineMsiHelper
     */
     VirtualMachineMsiHelper withManagedServiceIdentity(VirtualMachineInner virtualMachineInner) {
        return withManagedServiceIdentity(BuiltInRole.CONTRIBUTOR,
                null,
                defaultTokenPort,
                virtualMachineInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine.
     *
     * Once setupVirtualMachineMSIResourcesAsync is invoked,  applications running on the virtual machine will
     * have the given access role and scope of access will be limited to the resource group that this
     * virtual machine belongs to. The access token will be available in the virtual machine at port 50342.
     *
     *
     * @param role the role
     * @param virtualMachineInner the virtual machine to set the identity
     * @return VirtualMachineMsiHelper
     */
     VirtualMachineMsiHelper withManagedServiceIdentity(BuiltInRole role,
                                                        VirtualMachineInner virtualMachineInner) {
        return withManagedServiceIdentity(role,
                null,
                defaultTokenPort,
                virtualMachineInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine.
     *
     * Once setupVirtualMachineMSIResourcesAsync is invoked,  applications running on the virtual machine will
     * have the given access role and scope of access will be limited to the arm resource identified by
     * resource id specified in the scope parameter. The access token will be available in the virtual
     * machine at port 50342.
     *
     * @param role access role to assigned to the virtual machine
     * @param scope scope of the access represented in arm resource id format
     * @param virtualMachineInner the virtual machine to set the identity
     * @return VirtualMachineMsiHelper
     */
     VirtualMachineMsiHelper withManagedServiceIdentity(BuiltInRole role,
                                                        String scope,
                                                        VirtualMachineInner virtualMachineInner) {
        return withManagedServiceIdentity(role,
                scope,
                defaultTokenPort,
                virtualMachineInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine.
     *
     * Once setupVirtualMachineMSIResourcesAsync is invoked, applications running on the virtual machine will
     * have the given access role and scope of access will be limited to the arm resource identified by
     * resource id specified in the scope parameter. The access token will be available in the virtual
     * machine at given port.
     *
     * @param role access role to assigned to the virtual machine
     * @param scope scope of the access represented in arm resource id format
     * @param port the port in the virtual machine to get the access token from
     * @param virtualMachineInner the virtual machine to set the identity

     * @param virtualMachineInner the virtual machine to set the identity
     * @return VirtualMachineMsiHelper
     */
     VirtualMachineMsiHelper withManagedServiceIdentity(BuiltInRole role,
                                                        String scope,
                                                        int port,
                                                        VirtualMachineInner virtualMachineInner) {
        this.requireSetup = true;
        this.role = role;
        this.scope = scope;
        this.tokenPort = port;
        setIdentityType(virtualMachineInner);
        return this;
    }

    /**
     * Install or update the MSI extension in the virtual machine and creates a RBAC role assignment
     * for the auto created service principal with the given role and scope.
     *
     * @param virtualMachine the virtual machine for which the MSI needs to be enabled
     * @return the observable that emits result of MSI resource setup.
     */
     Observable<Result> setupVirtualMachineMSIResourcesAsync(final VirtualMachine virtualMachine) {
        if (!requireSetup) {
            return Observable.just(new Result(false, null));
        }
        if (!virtualMachine.isManagedServiceIdentityEnabled()) {
            // The principal id and tenant id needs to be set before performing role assignment
            //
            return Observable.just(new Result(false, null));
        }

        OperatingSystemTypes osType = virtualMachine.osType();
        final String extensionTypeName = osType == OperatingSystemTypes.LINUX ? "ManagedIdentityExtensionForLinux"
                : "ManagedIdentityExtensionForWindows";
        return getMSIExtensionAsync(virtualMachine, extensionTypeName)
                .flatMap(new Func1<VirtualMachineExtension, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(VirtualMachineExtension extension) {
                        return updateMSIExtensionAsync(virtualMachine, extension, extensionTypeName);
                    }
                })
                .switchIfEmpty(installMSIExtensionAsync(virtualMachine, extensionTypeName))
                .flatMap(new Func1<Boolean, Observable<Result>>() {
                    @Override
                    public Observable<Result> call(final Boolean extensionUpdateOrInstalled) {
                        return createRbacRoleAssignmentIfNotExistsAsync(virtualMachine)
                                .map(new Func1<RoleAssignment, Result>() {
                                    @Override
                                    public Result call(RoleAssignment roleAssignment) {
                                        clear();
                                        return new Result(extensionUpdateOrInstalled, roleAssignment);
                                    }
                                });
                    }
                });
    }

    /**
     * Creates RBAC role assignment for the virtual machine service principal if it does not exists.
     *
     * @param virtualMachine the virtual machine
     * @return an observable that emits role assignment if it created, if it already exists emits null.
     */
    private Observable<RoleAssignment> createRbacRoleAssignmentIfNotExistsAsync(final VirtualMachine virtualMachine) {
        final String roleAssignmentName = SdkContext.randomUuid();
        return rbacManager
                .servicePrincipals()
                .getByIdAsync(virtualMachine.inner().identity().principalId())
                .flatMap(new Func1<ServicePrincipal, Observable<RoleAssignment>>() {
                    @Override
                    public Observable<RoleAssignment> call(final ServicePrincipal principal) {
                        return resolveScopeAsync(virtualMachine)
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
     * @param virtualMachine the virtual machine
     * @return the scope
     */
    private Observable<String> resolveScopeAsync(final VirtualMachine virtualMachine) {
        if (this.scope != null) {
            return Observable.just(this.scope);
        } else {
            // TODO: Remove fromCallable wrapper once we have getByNameAsync implemented.
            //
            return Observable.fromCallable(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return virtualMachine.manager()
                            .resourceManager()
                            .resourceGroups()
                            .getByName(virtualMachine.resourceGroupName())
                            .id();
                }
            }).subscribeOn(SdkContext.getRxScheduler());
        }
    }

    /**
     * Checks the virtual machine already has the Managed Service Identity extension installed if so return it.
     *
     * @param virtualMachine the virtual machine
     * @param typeName the Managed Service Identity extension type name
     * @return an observable that emits MSI extension if exists
     */
    private Observable<VirtualMachineExtension> getMSIExtensionAsync(VirtualMachine virtualMachine, final String typeName) {
        return virtualMachine.listExtensionsAsync().filter(new Func1<VirtualMachineExtension, Boolean>() {
            @Override
            public Boolean call(VirtualMachineExtension extension) {
                return extension.publisherName().equalsIgnoreCase("Microsoft.ManagedIdentity")
                        && extension.typeName().equalsIgnoreCase(typeName);
            }
        });
    }

    /**
     * Install Managed Service Identity extension in the virtual machine.
     *
     * @param virtualMachine the virtual machine
     * @param typeName the Managed Service Identity extension type name
     * @return an observable that emits true indicating MSI extension installed
     */
    private Observable<Boolean> installMSIExtensionAsync(final VirtualMachine virtualMachine, final String typeName) {
        return  Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                VirtualMachineExtensionInner extensionParameter = new VirtualMachineExtensionInner();
                extensionParameter
                        .withPublisher("Microsoft.ManagedIdentity")
                        .withVirtualMachineExtensionType(typeName)
                        .withTypeHandlerVersion("1.0")
                        .withAutoUpgradeMinorVersion(true)
                        .withLocation(virtualMachine.regionName());
                Map<String, Object> settings = new HashMap<>();
                settings.put("port", tokenPort);
                extensionParameter.withSettings(settings);
                extensionParameter.withProtectedSettings(null);

                return virtualMachine.manager().inner().virtualMachineExtensions()
                        .createOrUpdateAsync(virtualMachine.resourceGroupName(), virtualMachine.name(), typeName, extensionParameter)
                        .map(new Func1<VirtualMachineExtensionInner, Boolean>() {
                            @Override
                            public Boolean call(VirtualMachineExtensionInner extension) {
                                return true;
                            }
                        });
            }
        });
    }

    /**
     * Update the Managed Service Identity extension installed in the virtual machine.
     *
     * @param virtualMachine the virtual machine
     * @param extension the Managed Service Identity extension
     * @param typeName the Managed Service Identity extension type name
     * @return an observable that emits true if MSI extension updated, false otherwise.
     */
    private Observable<Boolean> updateMSIExtensionAsync(final VirtualMachine virtualMachine, VirtualMachineExtension extension, final String typeName) {
        Object currentTokenPortObj = extension.publicSettings().get("port");
        Integer currentTokenPort = null;
        if (currentTokenPortObj != null) {
            if (currentTokenPortObj instanceof Integer) {
                currentTokenPort = (Integer) currentTokenPortObj;
            } else {
                currentTokenPort = Integer.valueOf((String) currentTokenPortObj);
            }
        }
        if (this.tokenPort.intValue() != currentTokenPort.intValue()) {
            Map<String, Object> settings = new HashMap<>();
            settings.put("port", tokenPort);
            extension.inner().withSettings(settings);

            return virtualMachine.manager().inner().virtualMachineExtensions()
                    .createOrUpdateAsync(virtualMachine.resourceGroupName(), virtualMachine.name(), typeName, extension.inner())
                    .map(new Func1<VirtualMachineExtensionInner, Boolean>() {
                        @Override
                        public Boolean call(VirtualMachineExtensionInner extension) {
                            return true;
                        }
                    });
        } else {
            return Observable.just(false);
        }
    }

    /**
     * Sets the identity property of the virtual machine.
     *
     * @param virtualMachineInner the virtual machine
     */
    private void setIdentityType(VirtualMachineInner virtualMachineInner) {
        if (virtualMachineInner.identity() == null) {
            virtualMachineInner.withIdentity(new VirtualMachineIdentity());
        }
        if (virtualMachineInner.identity().type() == null) {
            virtualMachineInner.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED);
        }
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

    // Result of MSI operation.
    //
    class Result {
        boolean isExtensionInstalledOrUpdated;
        RoleAssignment roleAssignment;

        Result(boolean isExtensionInstalledOrUpdated, RoleAssignment roleAssignment) {
            this.isExtensionInstalledOrUpdated = isExtensionInstalledOrUpdated;
            this.roleAssignment = roleAssignment;
        }
    }
}