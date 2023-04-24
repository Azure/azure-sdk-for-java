// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FaultInjectionConditionInternal {
    private final String containerResourceId;

    private OperationType operationType;
    private List<URI> regionEndpoints;
    private List<URI> physicalAddresses;
    private List<IFaultInjectionConditionValidator> validators;

    public FaultInjectionConditionInternal(String containerResourceId) {
        this.containerResourceId = containerResourceId;
        this.validators = new ArrayList<>();
        this.validators.add(new ContainerValidator(this.containerResourceId));
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
        if (operationType != null) {
            this.validators.add(new OperationTypeValidator(operationType));
        }
    }

    public void setRegionEndpoints(List<URI> regionEndpoints) {
        this.regionEndpoints = regionEndpoints;
        if (this.regionEndpoints != null) {
            this.validators.add(new RegionEndpointValidator(this.regionEndpoints));
        }
    }

    public List<URI> getRegionEndpoints() {
        return this.regionEndpoints;
    }

    public List<URI> getAddresses() {
        return physicalAddresses;
    }

    public void setAddresses(List<URI> physicalAddresses) {
        this.physicalAddresses = physicalAddresses;
        if (physicalAddresses != null && physicalAddresses.size() > 0) {
            this.validators.add(new AddressValidator(physicalAddresses));
        }
    }

    public boolean isApplicable(RxDocumentServiceRequest request) {
        return this.validators.stream().allMatch(validator -> validator.isApplicable(request));
    }

    // region ConditionValidators
    interface IFaultInjectionConditionValidator {
        boolean isApplicable(RxDocumentServiceRequest request);
    }

    static class RegionEndpointValidator implements IFaultInjectionConditionValidator {
        private List<URI> regionEndpoints;
        RegionEndpointValidator(List<URI> regionEndpoints) {
            this.regionEndpoints = regionEndpoints;
        }
        @Override
        public boolean isApplicable(RxDocumentServiceRequest request) {
            return this.regionEndpoints.contains(request.faultInjectionRequestContext.getLocationEndpointToRoute());
        }
    }

    static class OperationTypeValidator implements IFaultInjectionConditionValidator {
        private OperationType operationType;
        OperationTypeValidator(OperationType operationType) {
            this.operationType = operationType;
        }

        @Override
        public boolean isApplicable(RxDocumentServiceRequest request) {
            return request.getOperationType() == operationType;
        }
    }

    static class ContainerValidator implements IFaultInjectionConditionValidator {

        private final String containerResourceId;
        ContainerValidator(String containerResourceId) {
            this.containerResourceId = containerResourceId;
        }

        @Override
        public boolean isApplicable(RxDocumentServiceRequest request) {
            return StringUtils.equals(this.containerResourceId, request.requestContext.resolvedCollectionRid);
        }
    }

    static class AddressValidator implements IFaultInjectionConditionValidator {
        private final List<URI> addresses;
        AddressValidator(List<URI> addresses) {
            this.addresses = addresses;
        }

        @Override
        public boolean isApplicable(RxDocumentServiceRequest request) {
            if (addresses != null
                && addresses.size() > 0) {
                return this.addresses
                    .stream()
                    .anyMatch(address -> request.requestContext.storePhysicalAddress.toString().startsWith(address.toString()));
            }

            return true;
        }
    }
    //endregion
}
