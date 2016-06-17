package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachinesInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizesInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeInner;
import com.microsoft.azure.management.compute.implementation.api.StorageProfile;
import com.microsoft.azure.management.compute.implementation.api.OSDisk;
import com.microsoft.azure.management.compute.implementation.api.DataDisk;
import com.microsoft.azure.management.compute.implementation.api.OSProfile;
import com.microsoft.azure.management.compute.implementation.api.HardwareProfile;
import com.microsoft.azure.management.compute.implementation.api.NetworkProfile;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineCaptureParametersInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineCaptureResultInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link VirtualMachines}.
 */
class VirtualMachinesImpl
        implements VirtualMachines {
    private final VirtualMachinesInner client;
    private final VirtualMachineSizesInner virtualMachineSizesClient;
    private final ComputeManager computeManager;
    private final ResourceManager resourceManager;
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    private final PagedListConverter<VirtualMachineInner, VirtualMachine> converter;

    VirtualMachinesImpl(VirtualMachinesInner client,
                        VirtualMachineSizesInner virtualMachineSizesClient,
                        ComputeManager computeManager,
                        ResourceManager resourceManager,
                        StorageManager storageManager,
                        NetworkManager networkManager) {
        this.client = client;
        this.virtualMachineSizesClient = virtualMachineSizesClient;
        this.computeManager = computeManager;
        this.resourceManager = resourceManager;
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.converter = new PagedListConverter<VirtualMachineInner, VirtualMachine>() {
            @Override
            public VirtualMachine typeConvert(VirtualMachineInner inner) {
                return createFluentModel(inner);
            }
        };
    }

    // Actions
    //

    @Override
    public PagedList<VirtualMachine> list() throws CloudException, IOException {
        ServiceResponse<PagedList<VirtualMachineInner>> response = client.listAll();
        return converter.convert(response.getBody());
    }

    @Override
    public PagedList<VirtualMachine> listByGroup(String groupName) throws CloudException, IOException {
        ServiceResponse<List<VirtualMachineInner>> response = client.list(groupName);
        return converter.convert(toPagedList(response.getBody()));
    }

    @Override
    public VirtualMachine getByGroup(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<VirtualMachineInner> response = this.client.get(groupName, name);
        return createFluentModel(response.getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.client.delete(groupName, name);
    }

    @Override
    public VirtualMachine.DefinitionBlank define(String name) {
        return createFluentModel(name);
    }

    @Override
    public PagedList<VirtualMachineSize> availableSizesByRegion(String region) throws CloudException, IOException {
        PagedListConverter<VirtualMachineSizeInner, VirtualMachineSize> converter =
                new PagedListConverter<VirtualMachineSizeInner, VirtualMachineSize>() {
            @Override
            public VirtualMachineSize typeConvert(VirtualMachineSizeInner virtualMachineSizeInner) {
                return new VirtualMachineSizeImpl(virtualMachineSizeInner);
            }
        };

        PageImpl<VirtualMachineSizeInner> page = new PageImpl<>();
        page.setItems(virtualMachineSizesClient.list(region).getBody());
        page.setNextPageLink(null);
        return converter.convert(new PagedList<VirtualMachineSizeInner>(page) {
            @Override
            public Page<VirtualMachineSizeInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        });
    }

    @Override
    public void deallocate(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.client.deallocate(groupName, name);
    }

    @Override
    public void generalize(String groupName, String name) throws CloudException, IOException {
        this.client.generalize(groupName, name);
    }

    @Override
    public void powerOff(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.client.powerOff(groupName, name);
    }

    @Override
    public void restart(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.client.restart(groupName, name);
    }

    @Override
    public void start(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.client.start(groupName, name);
    }

    @Override
    public void redeploy(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.client.redeploy(groupName, name);
    }

    @Override
    public String capture(String groupName, String name,
                          String containerName,
                          boolean overwriteVhd) throws CloudException, IOException, InterruptedException {
        VirtualMachineCaptureParametersInner parameters = new VirtualMachineCaptureParametersInner();
        parameters.withDestinationContainerName(containerName);
        parameters.withOverwriteVhds(overwriteVhd);
        ServiceResponse<VirtualMachineCaptureResultInner> captureResult = this.client.capture(groupName, name, parameters);
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON string
        return mapper.writeValueAsString(captureResult.getBody().output());
    }

    // Helper methods
    //

    private VirtualMachineImpl createFluentModel(String name) {
        VirtualMachineInner inner = new VirtualMachineInner();
        inner.withStorageProfile(new StorageProfile()
            .withOsDisk(new OSDisk())
            .withDataDisks(new ArrayList<DataDisk>()));
        inner.withOsProfile(new OSProfile().withComputerName(name));
        inner.withHardwareProfile(new HardwareProfile());
        inner.withNetworkProfile(new NetworkProfile()
                .withNetworkInterfaces(new ArrayList<NetworkInterfaceReference>()));
        return new VirtualMachineImpl(name,
            inner,
            this.client,
            this.computeManager,
            this.resourceManager,
            this.storageManager,
            this.networkManager);
    }

    private VirtualMachineImpl createFluentModel(VirtualMachineInner virtualMachineInner) {
        return new VirtualMachineImpl(virtualMachineInner.name(),
                virtualMachineInner,
                this.client,
                this.computeManager,
                this.resourceManager,
                this.storageManager,
                this.networkManager);
    }

    private PagedList<VirtualMachineInner> toPagedList(List<VirtualMachineInner> list) {
        PageImpl<VirtualMachineInner> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        return new PagedList<VirtualMachineInner>(page) {
            @Override
            public Page<VirtualMachineInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }
}
