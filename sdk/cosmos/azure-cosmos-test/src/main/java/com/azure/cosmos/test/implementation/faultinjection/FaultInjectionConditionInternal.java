// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public void setAddresses(List<URI> physicalAddresses, boolean primaryOnly) {
        this.physicalAddresses = physicalAddresses;
        if (physicalAddresses != null && physicalAddresses.size() > 0) {
            this.validators.add(new AddressValidator(physicalAddresses));
        }

        if (primaryOnly) {
            this.validators.add(new PrimaryAddressValidator());
        }
    }

    public boolean isApplicable(String ruleId, RntbdRequestArgs requestArgs) {
        for (IFaultInjectionConditionValidator conditionValidator : this.validators) {
            if (!conditionValidator.isApplicable(ruleId, requestArgs)) {
                return false;
            }
        }

        return true;
    }

    // region ConditionValidators
    interface IFaultInjectionConditionValidator {
        boolean isApplicable(String ruleId, RntbdRequestArgs requestArgs);
    }

    static class RegionEndpointValidator implements IFaultInjectionConditionValidator {
        private List<URI> regionEndpoints;
        RegionEndpointValidator(List<URI> regionEndpoints) {
            this.regionEndpoints = regionEndpoints;
        }
        @Override
        public boolean isApplicable(String ruleId, RntbdRequestArgs requestArgs) {
            boolean isApplicable = this.regionEndpoints.contains(requestArgs.serviceRequest().faultInjectionRequestContext.getLocationEndpointToRoute());
            if (!isApplicable) {
                requestArgs.serviceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.transportRequestId(),
                        String.format(
                            "%s [RegionEndpoint mismatch: Expected [%s], Actual [%s]]",
                            ruleId,
                            this.regionEndpoints.stream().map(URI::toString).collect(Collectors.toList()),
                            requestArgs.serviceRequest().faultInjectionRequestContext.getLocationEndpointToRoute()));
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
        public boolean isApplicable(String ruleId, RntbdRequestArgs requestArgs) {
            boolean isApplicable = requestArgs.serviceRequest().getOperationType() == operationType;
            if (!isApplicable) {
                requestArgs.serviceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.transportRequestId(),
                        String.format(
                            "%s [OperationType mismatch: Expected [%s], Actual [%s]]",
                            ruleId,
                            operationType,
                            requestArgs.serviceRequest().getOperationType()));
            }

            return isApplicable;
        }
    }

    static class ContainerValidator implements IFaultInjectionConditionValidator {

        private final String containerResourceId;
        ContainerValidator(String containerResourceId) {
            this.containerResourceId = containerResourceId;
        }

        @Override
        public boolean isApplicable(String ruleId, RntbdRequestArgs requestArgs) {
            boolean isApplicable = StringUtils.equals(this.containerResourceId, requestArgs.serviceRequest().requestContext.resolvedCollectionRid);
            if (!isApplicable) {
                requestArgs.serviceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.transportRequestId(),
                        String.format(
                            "%s [ContainerRid mismatch: Expected [%s], Actual [%s]]",
                            ruleId,
                            containerResourceId,
                            requestArgs.serviceRequest().requestContext.resolvedCollectionRid));
            }

            return isApplicable;        }
    }

    static class AddressValidator implements IFaultInjectionConditionValidator {
        private final List<URI> addresses;
        AddressValidator(List<URI> addresses) {
            this.addresses = addresses;
        }

        @Override
        public boolean isApplicable(String ruleId, RntbdRequestArgs requestArgs) {
            if (addresses != null
                && addresses.size() > 0) {

                boolean isApplicable = this.addresses
                    .stream()
                    .anyMatch(address -> requestArgs.physicalAddressUri().getURIAsString().startsWith(address.toString()));

                if (!isApplicable) {
                    requestArgs.serviceRequest().faultInjectionRequestContext
                        .recordFaultInjectionRuleEvaluation(requestArgs.transportRequestId(),
                            String.format(
                                "%s [Addresses mismatch: Expected [%s], Actual [%s]]",
                                ruleId,
                                addresses,
                                requestArgs.physicalAddressUri().getURIAsString()));
                }

                return isApplicable;
            }

            return true;
        }
    }

    static class PrimaryAddressValidator implements IFaultInjectionConditionValidator {
        @Override
        public boolean isApplicable(String ruleId, RntbdRequestArgs requestArgs) {
            boolean isApplicable = requestArgs.physicalAddressUri().isPrimary();
            if (!isApplicable) {
                requestArgs.serviceRequest().faultInjectionRequestContext
                    .recordFaultInjectionRuleEvaluation(requestArgs.transportRequestId(),
                        String.format(
                            "%s [NonPrimary addresses]",
                            ruleId));
            }

            return isApplicable;        }
    }
    //endregion
}
