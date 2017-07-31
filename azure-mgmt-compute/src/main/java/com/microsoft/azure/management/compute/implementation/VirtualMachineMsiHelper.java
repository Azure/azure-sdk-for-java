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
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Utility class to set Managed Service Identity (MSI) and MSI related resources for a virtual machine.
 */
class VirtualMachineMsiHelper {
    private static final String CURRENT_RESOURCE_GROUP_SCOPE = "CURRENT_RESOURCE_GROUP";
    private static final int DEFAULT_TOKEN_PORT = 50342;
    private static final String MSI_EXTENSION_PUBLISHER_NAME = "Microsoft.ManagedIdentity";
    private static final String LINUX_MSI_EXTENSION = "ManagedIdentityExtensionForLinux";
    private static final String WINDOWS_MSI_EXTENSION = "ManagedIdentityExtensionForWindows";

    private final GraphRbacManager rbacManager;

    private Integer tokenPort;
    private boolean requireSetup;
    private HashMap<String, Pair<String, BuiltInRole>> rolesToAssign;

    /**
     * Creates VirtualMachineMsiHelper.
     *
     * @param rbacManager the graph rbac manager
     */
    VirtualMachineMsiHelper(final GraphRbacManager rbacManager) {
        this.rbacManager = rbacManager;
        this.rolesToAssign = new HashMap<>();
        clear();
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine.
     *
     * If MSI extension is already installed then the access token will be available in the virtual machine
     * at port specified in the extension public setting, otherwise the port for new extension will be 50342.
     *
     * @param virtualMachineInner the virtual machine to set the identity
     * @return VirtualMachineMsiHelper
     */
     VirtualMachineMsiHelper withManagedServiceIdentity(VirtualMachineInner virtualMachineInner) {
        return withManagedServiceIdentity(null, virtualMachineInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine.
     *
     * The access token will be available in the virtual machine at given port.
     *
     * @param port the port in the virtual machine to get the access token from
     * @param virtualMachineInner the virtual machine to set the identity

     * @param virtualMachineInner the virtual machine to set the identity
     * @return VirtualMachineMsiHelper
     */
    VirtualMachineMsiHelper withManagedServiceIdentity(Integer port, VirtualMachineInner virtualMachineInner) {
        this.requireSetup = true;
        this.tokenPort = port;
        if (virtualMachineInner.identity() == null) {
            virtualMachineInner.withIdentity(new VirtualMachineIdentity());
        }
        if (virtualMachineInner.identity().type() == null) {
            virtualMachineInner.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED);
        }
        return this;
    }

    /**
     * Specifies that applications running on the virtual machine requires the given access role
     * with scope of access limited to the current resource group that the virtual machine
     * resides.
     *
     * @param asRole access role to assigned to the virtual machine
     * @return VirtualMachineMsiHelper
     */
    VirtualMachineMsiHelper withAccessToCurrentResourceGroup(BuiltInRole asRole) {
        return this.withAccessTo(CURRENT_RESOURCE_GROUP_SCOPE, asRole);
    }

    /**
     * Specifies that applications running on the virtual machine requires contributor access
     * with scope of access limited to the arm resource identified by the resource id specified
     * in the scope parameter.
     *
     * @param scope scope of the access represented in arm resource id format
     * @return VirtualMachineMsiHelper
     */
    VirtualMachineMsiHelper withContributorAccessTo(String scope) {
        return this.withAccessTo(scope, BuiltInRole.CONTRIBUTOR);
    }

    /**
     * Specifies that applications running on the virtual machine requires the given access role
     * with scope of access limited to the arm resource identified by the resource id specified
     * in the scope parameter.
     *
     * @param scope scope of the access represented in arm resource id format
     * @param asRole access role to assigned to the virtual machine
     * @return VirtualMachineMsiHelper
     */
    VirtualMachineMsiHelper withAccessTo(String scope, BuiltInRole asRole) {
        this.requireSetup = true;
        String key = scope.toLowerCase() + "_" + asRole.toString().toLowerCase();
        this.rolesToAssign.put(key, Pair.of(scope, asRole));
        return this;
    }

    /**
     * Install or update the MSI extension in the virtual machine and creates a RBAC role assignment
     * for the auto created service principal with the given role and scope.
     *
     * @param virtualMachine the virtual machine for which the MSI needs to be enabled
     * @return the observable that emits result of MSI resource setup.
     */
     Observable<MSIResourcesSetupResult> setupVirtualMachineMSIResourcesAsync(final VirtualMachine virtualMachine) {
        if (!requireSetup) {
            return Observable.just(new MSIResourcesSetupResult());
        }
        if (!virtualMachine.isManagedServiceIdentityEnabled()) {
            // The principal id and tenant id needs to be set before performing role assignments
            //
            return Observable.just(new MSIResourcesSetupResult());
        }

        OperatingSystemTypes osType = virtualMachine.osType();
        final String extensionTypeName = osType == OperatingSystemTypes.LINUX ? LINUX_MSI_EXTENSION : WINDOWS_MSI_EXTENSION;
        final MSIResourcesSetupResult result = new MSIResourcesSetupResult();
        return getMSIExtensionAsync(virtualMachine, extensionTypeName)
                .flatMap(new Func1<VirtualMachineExtension, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(VirtualMachineExtension extension) {
                        return updateMSIExtensionAsync(virtualMachine, extension, extensionTypeName);
                    }
                })
                .switchIfEmpty(installMSIExtensionAsync(virtualMachine, extensionTypeName))
                .map(new Func1<Boolean, Void>() {
                    @Override
                    public Void call(Boolean extensionInstalledOrUpdated) {
                        result.isExtensionInstalledOrUpdated = extensionInstalledOrUpdated;
                        return null;
                    }
                })
                .flatMap(new Func1<Void, Observable<RoleAssignment>>() {
                    @Override
                    public Observable<RoleAssignment> call(final Void aVoid) {
                        return createRbacRoleAssignmentsAsync(virtualMachine);
                    }
                })
                .collect(new Func0<MSIResourcesSetupResult>() {
                    @Override
                    public MSIResourcesSetupResult call() {
                        return result;
                    }
                }, new Action2<MSIResourcesSetupResult, RoleAssignment>() {
                    @Override
                    public void call(MSIResourcesSetupResult result, RoleAssignment roleAssignment) {
                        result.roleAssignments.add(roleAssignment);
                    }
                })
                .switchIfEmpty(Observable.just(result))
                .doAfterTerminate(new Action0() {
                    @Override
                    public void call() {
                        clear();
                    }
                });
    }

    /**
     * Creates RBAC role assignments for the virtual machine service principal.
     *
     * @param virtualMachine the virtual machine
     * @return an observable that emits the created role assignments.
     */
    private Observable<RoleAssignment> createRbacRoleAssignmentsAsync(final VirtualMachine virtualMachine) {
        if (this.rolesToAssign.isEmpty()) {
            return Observable.empty();
        }
        return rbacManager
                .servicePrincipals()
                .getByIdAsync(virtualMachine.inner().identity().principalId())
                .zipWith(resolveCurrentResourceGroupScopeAsync(virtualMachine), new Func2<ServicePrincipal, Boolean, ServicePrincipal>() {
                    @Override
                    public ServicePrincipal call(ServicePrincipal servicePrincipal, Boolean resolvedAny) {
                        return servicePrincipal;
                    }
                })
                .flatMap(new Func1<ServicePrincipal, Observable<RoleAssignment>>() {
                    @Override
                    public Observable<RoleAssignment> call(final ServicePrincipal servicePrincipal) {
                        return
                        Observable.from(rolesToAssign.values())
                                .flatMap(new Func1<Pair<String, BuiltInRole>, Observable<RoleAssignment>>() {
                                    @Override
                                    public Observable<RoleAssignment> call(Pair<String, BuiltInRole> scopeAndRole) {
                                        final BuiltInRole role = scopeAndRole.getRight();
                                        final String scope = scopeAndRole.getLeft();
                                        return createRbacRoleAssignmentIfNotExistsAsync(servicePrincipal, role, scope);
                                    }
                                });
                    }
                });
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
                return extension.publisherName().equalsIgnoreCase(MSI_EXTENSION_PUBLISHER_NAME)
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
                Integer tokenPortToUse = tokenPort != null ? tokenPort : DEFAULT_TOKEN_PORT;
                VirtualMachineExtensionInner extensionParameter = new VirtualMachineExtensionInner();
                extensionParameter
                        .withPublisher(MSI_EXTENSION_PUBLISHER_NAME)
                        .withVirtualMachineExtensionType(typeName)
                        .withTypeHandlerVersion("1.0")
                        .withAutoUpgradeMinorVersion(true)
                        .withLocation(virtualMachine.regionName());
                Map<String, Object> settings = new HashMap<>();
                settings.put("port", tokenPortToUse);
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
        Integer currentTokenPort = objectToInteger(extension.publicSettings().get("port"));
        Integer tokenPortToUse;
        if (this.tokenPort != null) {
            // User specified a port
            tokenPortToUse = this.tokenPort;
        } else if (currentTokenPort == null) {
            // User didn't specify a port and port is not already set
            tokenPortToUse = this.DEFAULT_TOKEN_PORT;
        } else {
            // User didn't specify a port and port is already set in the extension
            // No need to do a PUT on extension
            //
            return Observable.just(false);
        }
        Map<String, Object> settings = new HashMap<>();
        settings.put("port", tokenPortToUse);
        extension.inner().withSettings(settings);

        return virtualMachine.manager().inner().virtualMachineExtensions()
                .createOrUpdateAsync(virtualMachine.resourceGroupName(), virtualMachine.name(), typeName, extension.inner())
                .map(new Func1<VirtualMachineExtensionInner, Boolean>() {
                    @Override
                    public Boolean call(VirtualMachineExtensionInner extension) {
                        return true;
                    }
                });
    }


    /**
     * If any of the scope in {@link this#rolesToAssign} is marked with CURRENT_RESOURCE_GROUP_SCOPE placeholder then
     * resolve it and replace the placeholder with actual resource group scope (id).
     *
     * @param virtualMachine the virtual machine
     * @return an observable that emits true once if there was a scope to resolve, otherwise emits false once.
     */
    private Observable<Boolean> resolveCurrentResourceGroupScopeAsync(final VirtualMachine virtualMachine) {
        final List<String> entryKeysWithCurrentResourceGroupScope = new ArrayList<>();
        for (Map.Entry<String, Pair<String, BuiltInRole>> entrySet : this.rolesToAssign.entrySet()) {
            if (entrySet.getValue().getLeft().equals(CURRENT_RESOURCE_GROUP_SCOPE)) {
                entryKeysWithCurrentResourceGroupScope.add(entrySet.getKey());
            }
        }
        if (entryKeysWithCurrentResourceGroupScope.isEmpty()) {
            return Observable.just(false);
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
                }})
                .subscribeOn(SdkContext.getRxScheduler())
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String resourceGroupScope) {
                        for (String key : entryKeysWithCurrentResourceGroupScope) {
                            rolesToAssign.put(key, Pair.of(resourceGroupScope, rolesToAssign.get(key).getRight()));
                        }
                        return true;
                    }
                });
        }
    }

    /**
     * Creates a RBAC role assignment for the given service principal.
     *
     * @param servicePrincipal the service principal
     * @param role the role
     * @param scope the scope for the role assignment
     * @return an observable that emits the role assignment if it is created, null if assignment already exists.
     */
    private Observable<RoleAssignment> createRbacRoleAssignmentIfNotExistsAsync(final ServicePrincipal servicePrincipal,
                                                                                final BuiltInRole role,
                                                                                final String scope) {
        final String roleAssignmentName = SdkContext.randomUuid();
        return rbacManager
                .roleAssignments()
                .define(roleAssignmentName)
                .forServicePrincipal(servicePrincipal)
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
                                return Observable.empty();
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
        this.tokenPort = null;
        this.rolesToAssign.clear();
    }

    // MSIResourcesSetupResult of MSI operation.
    //
    class MSIResourcesSetupResult {
        boolean isExtensionInstalledOrUpdated;
        List<RoleAssignment> roleAssignments;

        MSIResourcesSetupResult() {
            this.isExtensionInstalledOrUpdated = false;
            this.roleAssignments = new ArrayList<>();
        }
    }
}