package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionInstanceView;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.ResourceServiceCall;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.HashMap;

/**
 * Implementation of {@link VirtualMachineExtension}.
 */
class VirtualMachineExtensionImpl
        extends ExternalChildResourceImpl<VirtualMachineExtension,
            VirtualMachineExtensionInner,
            VirtualMachineExtensionImpl,
            VirtualMachineImpl>
        implements VirtualMachineExtension,
        VirtualMachineExtension.Definition<VirtualMachine.DefinitionStages.WithCreate>,
        VirtualMachineExtension.UpdateDefinition<VirtualMachine.Update>,
        VirtualMachineExtension.Update {
    private final VirtualMachineExtensionsInner client;
    private boolean requireCreateOrUpdate = false;

    VirtualMachineExtensionImpl(String name,
                                VirtualMachineImpl parent,
                                VirtualMachineExtensionInner inner,
                                VirtualMachineExtensionsInner client) {
        super(name, parent, inner);
        this.client = client;
    }

    protected static VirtualMachineExtensionImpl newVirtualMachineExtension(String name,
                                                                            VirtualMachineImpl parent,
                                                                            VirtualMachineExtensionsInner client) {
        return new VirtualMachineExtensionImpl(name, parent, new VirtualMachineExtensionInner(), client);
    }

    @Override
    public String publisherName() {
        return this.inner().publisher();
    }

    @Override
    public String typeName() {
        return this.inner().type();
    }

    @Override
    public String versionName() {
        return this.inner().typeHandlerVersion();
    }

    @Override
    public boolean autoUpgradeMinorVersionEnabled() {
        return this.inner().autoUpgradeMinorVersion();
    }

    @Override
    public HashMap<String, Object> publicSettings() {
        return null;
    }

    @Override
    public String publicSettingsAsJsonString() {
        return null;
    }

    @Override
    public VirtualMachineExtensionInstanceView instanceView() {
        return this.inner().instanceView();
    }

    @Override
    public VirtualMachineExtension refresh() throws Exception {
        ServiceResponse<VirtualMachineExtensionInner> response =
                this.client.get(this.parent.resourceGroupName(), this.parent.name(), this.name());
        this.setInner(response.getBody());
        return this;
    }

    @Override
    public ServiceCall<Resource> createResourceAsync(ServiceCallback<Resource> serviceCallback) {
        ResourceServiceCall<VirtualMachineExtension,
                VirtualMachineExtensionInner,
                VirtualMachineExtensionImpl> serviceCall = new ResourceServiceCall<>(this);
        serviceCall.withSuccessHandler(new ResourceServiceCall.SuccessHandler<VirtualMachineExtensionInner>() {
            @Override
            public void success(ServiceResponse<VirtualMachineExtensionInner> response) {
            }
        });
        this.client.createOrUpdateAsync(this.parent.resourceGroupName(),
                this.parent.name(),
                this.name(),
                this.inner(),
                serviceCall.wrapCallBack(serviceCallback));
        return serviceCall;
    }

    @Override
    public VirtualMachineExtensionImpl createResource() throws Exception {
        ServiceResponse<VirtualMachineExtensionInner> response =
            this.client.createOrUpdate(this.parent.resourceGroupName(),
                    this.parent.name(),
                    this.name(),
                    this.inner());
        this.setInner(response.getBody());
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withAutoUpgradeMinorVersionEnabled() {
        this.inner().withAutoUpgradeMinorVersion(true);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withAutoUpgradeMinorVersionDisabled() {
        this.inner().withAutoUpgradeMinorVersion(false);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withImage(VirtualMachineExtensionImage image) {
        this.inner().withPublisher(image.publisherName())
                .withVirtualMachineExtensionType(image.typeName())
                .withTypeHandlerVersion(image.versionName());
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPublisher(String extensionImagePublisherName) {
        this.inner().withPublisher(extensionImagePublisherName);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPublicSetting(String key, Object value) {
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPrivateSetting(String key, Object value) {
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPublicSettings(HashMap<String, Object> settings) {
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPrivateSettings(HashMap<String, Object> settings) {
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withType(String extensionImageTypeName) {
        this.inner().withVirtualMachineExtensionType(extensionImageTypeName);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withVersion(String extensionImageVersionName) {
        this.inner().withTypeHandlerVersion(extensionImageVersionName);
        return this;
    }

    @Override
    public VirtualMachineImpl parent() {
        this.requireCreateOrUpdate = true;
        return this.parent;
    }

    @Override
    public VirtualMachineImpl attach() {
        this.requireCreateOrUpdate = true;
        return this.parent.withExtension(this);
    }

    /**
     * @return true if this child resource needs to be created or updated.
     */
    protected boolean requireCreateOrUpdate() {
        return this.requireCreateOrUpdate;
    }
}
