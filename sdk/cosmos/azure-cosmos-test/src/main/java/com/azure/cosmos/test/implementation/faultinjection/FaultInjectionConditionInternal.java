// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestArgs;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FaultInjectionConditionInternal {
    private final String containerResourceId;
    private final String containerName;
    private OperationType operationType;
    private List<URI> regionEndpoints;
    private List<URI> physicalAddresses;
    private List<IFaultInjectionConditionValidator> validators;

    public FaultInjectionConditionInternal(String containerResourceId, String containerName) {
        this.containerResourceId = containerResourceId;
        this.containerName = containerName;
        this.validators = new ArrayList<>();
        this.validators.add(new ContainerValidator(this.containerResourceId, this.containerName));
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

    public void setResourceType(ResourceType resourceType) {
        if (resourceType != null) {
            this.validators.add(new ResourceTypeValidator(resourceType));
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

    public void setAddresses(List<URI> physicalAddresses, boolean primaryOnly) {
        this.physicalAddresses = physicalAddresses;
        if (physicalAddresses != null && physicalAddresses.size() > 0) {
            this.validators.add(new AddressValidator(physicalAddresses));
        }

        if (primaryOnly) {
            this.validators.add(new PrimaryAddressValidator());
        }
    }

    public void setPartitionKeyRangeIds(List<String> partitionKeyRangeIds) {
        if (partitionKeyRangeIds != null && partitionKeyRangeIds.size() > 0) {
            this.validators.add(new PartitionKeyRangeIdValidator(partitionKeyRangeIds));
        }
    }

    public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
        for (IFaultInjectionConditionValidator conditionValidator : this.validators) {
            if (!conditionValidator.isApplicable(ruleId, requestArgs)) {
                return false;
            }
        }

        return true;
    }

    // region ConditionValidators
    interface IFaultInjectionConditionValidator {
        boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs);
    }

    static class RegionEndpointValidator implements IFaultInjectionConditionValidator {
        private List<URI> regionEndpoints;
        RegionEndpointValidator(List<URI> regionEndpoints) {
            this.regionEndpoints = regionEndpoints;
        }
        @Override
        public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
            boolean isApplicable =
                this.regionEndpoints.contains(requestArgs.getServiceRequest().faultInjectionRequestContext.getLocationEndpointToRoute());
            if (!isApplicable) {
                requestArgs.getServiceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                        String.format(
                            "%s [RegionEndpoint mismatch: Expected [%s], Actual [%s]]",
                            ruleId,
                            this.regionEndpoints.stream().map(URI::toString).collect(Collectors.toList()),
                            requestArgs.getServiceRequest().faultInjectionRequestContext.getLocationEndpointToRoute()));
            }

            return isApplicable;
        }
    }

    static class OperationTypeValidator implements IFaultInjectionConditionValidator {
        private OperationType operationType;
        OperationTypeValidator(OperationType operationType) {
            this.operationType = operationType;
        }

        @Override
        public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
            boolean isApplicable = requestArgs.getServiceRequest().getOperationType() == operationType;
            if (!isApplicable) {
                requestArgs.getServiceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                        String.format(
                            "%s [OperationType mismatch: Expected [%s], Actual [%s]]",
                            ruleId,
                            operationType,
                            requestArgs.getServiceRequest().getOperationType()));
            }

            return isApplicable;
        }
    }

    static class ContainerValidator implements IFaultInjectionConditionValidator {

        private final String containerResourceId;
        private final String containerName;
        ContainerValidator(String containerResourceId, String containerName) {
            this.containerResourceId = containerResourceId;
            this.containerName = containerName;
        }

        @Override
        public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
            // collection read does not have collectionRid being defined, so need to filter by collection name
            boolean isApplicable = isApplicableContainerRid(requestArgs) || isApplicableCollectionRead(requestArgs);
            if (!isApplicable) {
                if (this.isCollectionRead(requestArgs)) {
                    requestArgs.getServiceRequest().faultInjectionRequestContext
                        .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                            String.format(
                                "%s [CollectionName mismatch: Expected [%s], Actual [%s]]",
                                ruleId,
                                containerName,
                                this.getCollectionNameForCollectionRead(requestArgs.getServiceRequest().getResourceAddress())));
                } else {
                    requestArgs.getServiceRequest().faultInjectionRequestContext
                        .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                            String.format(
                                "%s [ContainerRid mismatch: Expected [%s], Actual [%s]]",
                                ruleId,
                                containerResourceId,
                                requestArgs.getCollectionRid()));
                }
            }

            return isApplicable;
        }

        private boolean isApplicableContainerRid(FaultInjectionRequestArgs requestArgs) {
            return StringUtils.equals(this.containerResourceId, requestArgs.getCollectionRid());
        }

        private boolean isApplicableCollectionRead(FaultInjectionRequestArgs requestArgs) {
            if (this.isCollectionRead(requestArgs)) {
                String collectionName = getCollectionNameForCollectionRead(requestArgs.getServiceRequest().getResourceAddress());
                return this.containerName.equals(collectionName);
            }

            return false;
        }

        private boolean isCollectionRead(FaultInjectionRequestArgs requestArgs) {
            return requestArgs.getServiceRequest().getResourceType() == ResourceType.DocumentCollection
                && requestArgs.getServiceRequest().getOperationType() == OperationType.Read;
        }

        private String getCollectionNameForCollectionRead(String resourceAddress) {
            if (resourceAddress != null) {
                String trimmedResourceFullName = Utils.trimBeginningAndEndingSlashes(resourceAddress);
                return trimmedResourceFullName.split("/")[3];
            }

            return null;
        }
    }

    static class AddressValidator implements IFaultInjectionConditionValidator {
        private final List<URI> addresses;
        AddressValidator(List<URI> addresses) {
            this.addresses = addresses;
        }

        @Override
        public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
            if (addresses != null
                && addresses.size() > 0) {

                boolean isApplicable = this.addresses
                    .stream()
                    .anyMatch(address -> requestArgs.getRequestURI().toString().startsWith(address.toString()));

                if (!isApplicable) {
                    requestArgs.getServiceRequest().faultInjectionRequestContext
                        .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                            String.format(
                                "%s [Addresses mismatch: Expected [%s], Actual [%s]]",
                                ruleId,
                                addresses,
                                requestArgs.getRequestURI().toString()));
                }

                return isApplicable;
            }

            return true;
        }
    }

    static class PrimaryAddressValidator implements IFaultInjectionConditionValidator {
        @Override
        public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
            boolean isApplicable = requestArgs.isPrimary();
            if (!isApplicable) {
                requestArgs.getServiceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                        String.format(
                            "%s [NonPrimary addresses]",
                            ruleId));
            }

            return isApplicable;        }
    }

    static class ResourceTypeValidator implements IFaultInjectionConditionValidator {
        private ResourceType resourceType;
        ResourceTypeValidator(ResourceType resourceType) {
            this.resourceType = resourceType;
        }

        @Override
        public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
            boolean isApplicable =
                requestArgs.getServiceRequest().getResourceType() == this.resourceType
                    || (this.resourceType == ResourceType.Address && requestArgs.getServiceRequest().isAddressRefresh());

            if (!isApplicable) {
                requestArgs.getServiceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                        String.format(
                            "%s [ResourceType mismatch: Expected [%s], Actual [%s], isAddressRefresh [%s]]",
                            ruleId,
                            resourceType,
                            requestArgs.getServiceRequest().getResourceType(),
                            requestArgs.getServiceRequest().isAddressRefresh()));
            }

            return isApplicable;
        }
    }

    static class PartitionKeyRangeIdValidator implements IFaultInjectionConditionValidator {
        private List<String> partitionKeyRangeIdList;
        PartitionKeyRangeIdValidator(List<String> partitionKeyRangeIdList) {
            this.partitionKeyRangeIdList = partitionKeyRangeIdList;
        }

        @Override
        public boolean isApplicable(String ruleId, FaultInjectionRequestArgs requestArgs) {
            boolean isApplicable = requestArgs.getPartitionKeyRangeIds() != null
                && !requestArgs.getPartitionKeyRangeIds().isEmpty()
                && this.partitionKeyRangeIdList.containsAll(requestArgs.getPartitionKeyRangeIds());

            if (!isApplicable) {
                requestArgs.getServiceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.getTransportRequestId(),
                        String.format(
                            "%s [PartitionKeyRangeId mismatch: Expected [%s], Actual [%s]]",
                            ruleId,
                            partitionKeyRangeIdList,
                            requestArgs.getPartitionKeyRangeIds()));
            }

            return isApplicable;
        }
    }
    //endregion
}
