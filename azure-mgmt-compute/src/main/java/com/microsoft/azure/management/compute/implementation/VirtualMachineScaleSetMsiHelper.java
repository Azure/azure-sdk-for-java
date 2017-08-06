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
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Utility class to set Managed Service Identity (MSI) and MSI related resources for a virtual machine scale set.
 */
class VirtualMachineScaleSetMsiHelper {
    private static final String CURRENT_RESOURCE_GROUP_SCOPE = "CURRENT_RESOURCE_GROUP";
    private static final int DEFAULT_TOKEN_PORT = 50342;
    private static final String MSI_EXTENSION_PUBLISHER_NAME = "Microsoft.ManagedIdentity";
    private static final String LINUX_MSI_EXTENSION = "ManagedIdentityExtensionForLinux";
    private static final String WINDOWS_MSI_EXTENSION = "ManagedIdentityExtensionForWindows";

    private final GraphRbacManager rbacManager;

    private Integer tokenPort;
    private boolean requireSetup;
    private LinkedHashMap<String, Pair<String, BuiltInRole>> rolesToAssign;
    private LinkedHashMap<String, Pair<String, String>> roleDefinitionsToAssign;

    /**
     * Creates VirtualMachineScaleSetMsiHelper.
     *
     * @param rbacManager the graph rbac manager
     */
    VirtualMachineScaleSetMsiHelper(GraphRbacManager rbacManager) {
        this.rbacManager = rbacManager;
        this.rolesToAssign = new LinkedHashMap<>();
        this.roleDefinitionsToAssign = new LinkedHashMap<>();
        this.clear();
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine scale set.
     *
     * If MSI extension is already installed then the access token will be available in the virtual machine
     * scale set instance at port specified in the extension public setting, otherwise the port for
     * new extension will be 50342.
     *
     * @param scaleSetInner the virtual machine scale set to set the identity
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withManagedServiceIdentity(VirtualMachineScaleSetInner scaleSetInner) {
        return withManagedServiceIdentity(null, scaleSetInner);
    }

    /**
     * Specifies that Managed Service Identity property needs to be set in the virtual machine scale set.
     *
     * The access token will be available in the virtual machine at given port.
     *
     * @param port the port in the virtual machine scale set instance to get the access token from
     * @param scaleSetInner the virtual machine scale set to set the identity
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withManagedServiceIdentity(Integer port, VirtualMachineScaleSetInner scaleSetInner) {
        this.requireSetup = true;
        this.tokenPort = port;
        if (scaleSetInner.identity() == null) {
            scaleSetInner.withIdentity(new VirtualMachineScaleSetIdentity());
        }
        if (scaleSetInner.identity().type() == null) {
            scaleSetInner.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED);
        }
        return this;
    }

    /**
     * Specifies that applications running on the virtual machine scale set instance requires
     * the given access role with scope of access limited to the current resource group that
     * the virtual machine resides.
     *
     * @param asRole access role to assigned to the virtual machine
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withRoleBasedAccessToCurrentResourceGroup(BuiltInRole asRole) {
        return this.withRoleBasedAccessTo(CURRENT_RESOURCE_GROUP_SCOPE, asRole);
    }

    /**
     * Specifies that applications running on the virtual machine scale set instance requires the
     * given access role with scope of access limited to the arm resource identified by the resource
     * id specified in the scope parameter.
     *
     * @param scope scope of the access represented in arm resource id format
     * @param asRole access role to assigned to the virtual machine scale set
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withRoleBasedAccessTo(String scope, BuiltInRole asRole) {
        this.requireSetup = true;
        String key = scope.toLowerCase() + "_" + asRole.toString().toLowerCase();
        this.rolesToAssign.put(key, Pair.of(scope, asRole));
        return this;
    }

    /**
     * Specifies that applications running on the virtual machine scale set instance requires
     * the access described in the given role definition with scope of access limited to the
     * current resource group that the virtual machine resides.
     *
     * @param roleDefintionId the role definition to assigned to the virtual machine
     * @return VirtualMachineScaleSetMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withRoleDefinitionBasedAccessToCurrentResourceGroup(String roleDefintionId) {
        return this.withRoleDefinitionBasedAccessTo(CURRENT_RESOURCE_GROUP_SCOPE, roleDefintionId);
    }

    /**
     * Specifies that applications running on the virtual machine scale set instance requires
     * the access described in the given role definition with scope of access limited to the
     * arm resource identified by the resource id specified in the scope parameter.
     *
     * @param scope scope of the access represented in arm resource id format
     * @param roleDefinition access role definition to assigned to the virtual machine scale set
     * @return VirtualMachineMsiHelper
     */
    VirtualMachineScaleSetMsiHelper withRoleDefinitionBasedAccessTo(String scope, String roleDefinition) {
        this.requireSetup = true;
        String key = scope.toLowerCase() + "_" + roleDefinition.toLowerCase();
        this.roleDefinitionsToAssign.put(key, Pair.of(scope, roleDefinition));
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
                newPort = DEFAULT_TOKEN_PORT;
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
                port = DEFAULT_TOKEN_PORT;
            }
            if (scaleSetImpl.isInCreateMode()) {
                VirtualMachineScaleSet.DefinitionStages.WithCreate creatableVMSS = scaleSetImpl;
                creatableVMSS.defineNewExtension(msiExtensionType)
                        .withPublisher(MSI_EXTENSION_PUBLISHER_NAME)
                        .withType(msiExtensionType)
                        .withVersion("1.0")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("port", port)
                        .attach();
            } else {
                VirtualMachineScaleSet.Update appliableVMSS = scaleSetImpl;
                appliableVMSS.defineNewExtension(msiExtensionType)
                        .withPublisher(MSI_EXTENSION_PUBLISHER_NAME)
                        .withType(msiExtensionType)
                        .withVersion("1.0")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("port", port)
                        .attach();
            }
        }
    }

    /**
     * Creates RBAC role assignments for the virtual machine scale set MSI service principal.
     *
     * @param scaleSet the virtual machine scale set
     * @return an observable that emits the created role assignments.
     */
    Observable<RoleAssignment> createMSIRbacRoleAssignmentsAsync(final VirtualMachineScaleSet scaleSet) {
        final Func0<Observable<RoleAssignment>> empty = new Func0<Observable<RoleAssignment>>() {
            @Override
            public Observable<RoleAssignment> call() {
                clear();
                return Observable.<RoleAssignment>empty();
            }
        };
        if (!requireSetup) {
            return empty.call();
        } else if (!scaleSet.isManagedServiceIdentityEnabled()) {
            // The principal id and tenant id needs to be set before performing role assignment
            return empty.call();
        } else if (this.rolesToAssign.isEmpty()
                && this.roleDefinitionsToAssign.isEmpty()) {
            return empty.call();
        } else {
            return rbacManager
                    .servicePrincipals()
                    .getByIdAsync(scaleSet.inner().identity().principalId())
                    .zipWith(resolveCurrentResourceGroupScopeAsync(scaleSet), new Func2<ServicePrincipal, Boolean, ServicePrincipal>() {
                        @Override
                        public ServicePrincipal call(ServicePrincipal servicePrincipal, Boolean resolvedAny) {
                            return servicePrincipal;
                        }
                    })
                    .flatMap(new Func1<ServicePrincipal, Observable<RoleAssignment>>() {
                        @Override
                        public Observable<RoleAssignment> call(final ServicePrincipal servicePrincipal) {
                        Observable<RoleAssignment> observable1 = Observable.from(rolesToAssign.values())
                                            .flatMap(new Func1<Pair<String, BuiltInRole>, Observable<RoleAssignment>>() {
                                                @Override
                                                public Observable<RoleAssignment> call(Pair<String, BuiltInRole> scopeAndRole) {
                                                final BuiltInRole role = scopeAndRole.getRight();
                                                final String scope = scopeAndRole.getLeft();
                                                return createRbacRoleAssignmentIfNotExistsAsync(servicePrincipal, role.toString(), scope, true);
                                                }
                                            });
                            Observable<RoleAssignment> observable2 = Observable.from(roleDefinitionsToAssign.values())
                                    .flatMap(new Func1<Pair<String, String>, Observable<RoleAssignment>>() {
                                        @Override
                                        public Observable<RoleAssignment> call(Pair<String, String> scopeAndRole) {
                                            final String roleDefinition = scopeAndRole.getRight();
                                            final String scope = scopeAndRole.getLeft();
                                            return createRbacRoleAssignmentIfNotExistsAsync(servicePrincipal, roleDefinition, scope, false);
                                        }
                                    });
                            return Observable.mergeDelayError(observable1, observable2);
                        }
                    })
                    .doAfterTerminate(new Action0() {
                        @Override
                        public void call() {
                            clear();
                        }
                    });
        }
    }

    /**
     * If any of the scope in {@link this#rolesToAssign} is marked with CURRENT_RESOURCE_GROUP_SCOPE placeholder then
     * resolve it and replace the placeholder with actual resource group scope (id).
     *
     * @param scaleSet the virtual machine scale set
     * @return an observable that emits true once if there was a scope to resolve, otherwise emits false once.
     */
    private Observable<Boolean> resolveCurrentResourceGroupScopeAsync(final VirtualMachineScaleSet scaleSet) {
        final List<String> keysWithCurrentResourceGroupScopeForRoles = new ArrayList<>();
        for (Map.Entry<String, Pair<String, BuiltInRole>> entrySet : this.rolesToAssign.entrySet()) {
            if (entrySet.getValue().getLeft().equals(CURRENT_RESOURCE_GROUP_SCOPE)) {
                keysWithCurrentResourceGroupScopeForRoles.add(entrySet.getKey());
            }
        }
        final List<String> keysWithCurrentResourceGroupScopeForRoleDefinitions = new ArrayList<>();
        for (Map.Entry<String, Pair<String, String>> entrySet : this.roleDefinitionsToAssign.entrySet()) {
            if (entrySet.getValue().getLeft().equals(CURRENT_RESOURCE_GROUP_SCOPE)) {
                keysWithCurrentResourceGroupScopeForRoleDefinitions.add(entrySet.getKey());
            }
        }
        if (keysWithCurrentResourceGroupScopeForRoles.isEmpty()
                && keysWithCurrentResourceGroupScopeForRoleDefinitions.isEmpty()) {
            return Observable.just(false);
        } else {
            // TODO: Remove fromCallable wrapper once we have resourceGroups.getByNameAsync implemented.
            //
            return Observable.fromCallable(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    ResourceManager resourceManager = scaleSet.manager().resourceManager();
                    return resourceManager
                            .resourceGroups()
                            .getByName(scaleSet.resourceGroupName())
                            .id();
                 } })
                .subscribeOn(SdkContext.getRxScheduler())
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String resourceGroupScope) {
                        for (String key : keysWithCurrentResourceGroupScopeForRoles) {
                            rolesToAssign.put(key, Pair.of(resourceGroupScope, rolesToAssign.get(key).getRight()));
                        }
                        for (String key : keysWithCurrentResourceGroupScopeForRoleDefinitions) {
                            roleDefinitionsToAssign.put(key, Pair.of(resourceGroupScope, roleDefinitionsToAssign.get(key).getRight()));
                        }
                        return true;
                    }
                });
        }
    }

    /**
     * Creates a RBAC role assignment (using role or role definition) for the given service principal.
     *
     * @param servicePrincipal the service principal
     * @param roleOrRoleDefinition the role or role definition
     * @param scope the scope for the role assignment
     * @return an observable that emits the role assignment if it is created, null if assignment already exists.
     */
    private Observable<RoleAssignment> createRbacRoleAssignmentIfNotExistsAsync(final ServicePrincipal servicePrincipal,
                                                                                final String roleOrRoleDefinition,
                                                                                final String scope,
                                                                                final boolean isRole) {
        Func1<Throwable, Observable<? extends Indexable>> onErrorResumeNext = new Func1<Throwable, Observable<? extends Indexable>>() {
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
        };
        final String roleAssignmentName = SdkContext.randomUuid();
        if (isRole) {
            return rbacManager
                    .roleAssignments()
                    .define(roleAssignmentName)
                    .forServicePrincipal(servicePrincipal)
                    .withBuiltInRole(BuiltInRole.fromString(roleOrRoleDefinition))
                    .withScope(scope)
                    .createAsync()
                    .last()
                    .onErrorResumeNext(onErrorResumeNext)
                    .map(new Func1<Indexable, RoleAssignment>() {
                        @Override
                        public RoleAssignment call(Indexable indexable) {
                            return (RoleAssignment) indexable;
                        }
                    });
        } else {
            return rbacManager
                    .roleAssignments()
                    .define(roleAssignmentName)
                    .forServicePrincipal(servicePrincipal)
                    .withRoleDefinition(roleOrRoleDefinition)
                    .withScope(scope)
                    .createAsync()
                    .last()
                    .onErrorResumeNext(onErrorResumeNext)
                    .map(new Func1<Indexable, RoleAssignment>() {
                        @Override
                        public RoleAssignment call(Indexable indexable) {
                            return (RoleAssignment) indexable;
                        }
                    });
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
        return osType == OperatingSystemTypes.LINUX ? LINUX_MSI_EXTENSION : WINDOWS_MSI_EXTENSION;
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
            if (extension.publisherName().equalsIgnoreCase(MSI_EXTENSION_PUBLISHER_NAME)) {
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
        this.tokenPort = null;
        this.rolesToAssign.clear();
        this.roleDefinitionsToAssign.clear();
    }
}
