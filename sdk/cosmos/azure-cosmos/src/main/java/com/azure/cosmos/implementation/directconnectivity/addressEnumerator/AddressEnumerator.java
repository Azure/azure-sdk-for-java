// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.addressEnumerator;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.Uri;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Unhealthy;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.UnhealthyPending;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class AddressEnumerator {

    public List<Uri> getTransportAddresses(RxDocumentServiceRequest request, List<Uri> addresses) {
        checkNotNull(addresses, "Argument 'addresses' should not be null");
        checkNotNull(request, "Argument 'request' should not be null");

        List<Uri> randomPermutation = this.getAddressesPermutationInternal(addresses);

        return this.sortAddresses(randomPermutation, request);
    }

    private List<Uri> sortAddresses(List<Uri> addressesPermutation, RxDocumentServiceRequest request) {
        if (!request.requestContext.replicaAddressValidationEnabled) {
            // When replica address validation is disabled, we will rely on RxDocumentServiceRequest to transition away unknown/unhealthyPending
            // so we prefer connected/unknown/unhealthyPending to unhealthy
            addressesPermutation.sort(new Comparator<Uri>() {
                @Override
                public int compare(Uri o1, Uri o2) {
                    Uri.HealthStatus o1Status = getEffectiveStatus(o1, request.requestContext.getFailedEndpoints());
                    Uri.HealthStatus o2Status = getEffectiveStatus(o2, request.requestContext.getFailedEndpoints());

                    if (o1Status != Unhealthy && o2Status != Unhealthy) {
                        return 0;
                    }
                    if (o1Status == Unhealthy) {
                        return 1;
                    }

                    return -1;
                }
            });
        } else {

            // When replica address validation is enabled, we will prefer connected/unknown > unhealthyPending > unhealthy.
            // We will prefer to use open connection request to transit away unknown/unhealthyPending status,
            // but in case open connection request can not happen due to any reason,
            // then after some extended time, we are going to rolling unknown/unhealthyPending into Healthy category (please check details of getEffectiveHealthStatus)
            addressesPermutation.sort(new Comparator<Uri>() {
                @Override
                public int compare(Uri o1, Uri o2) {
                    Uri.HealthStatus o1Status = getEffectiveStatus(o1, request.requestContext.getFailedEndpoints());
                    Uri.HealthStatus o2Status = getEffectiveStatus(o2, request.requestContext.getFailedEndpoints());

                    if (o1Status == o2Status) {
                        return 0;
                    }

                    if (o1Status.getPriority() < UnhealthyPending.getPriority()
                        && o2Status.getPriority() < UnhealthyPending.getPriority()) {
                        return 0;
                    }

                    return o1Status.getPriority() - o2Status.getPriority();
                }
            });
        }

        return addressesPermutation;
    }

    private List<Uri> getAddressesPermutationInternal(List<Uri> addresses) {
        checkNotNull(addresses, "Argument 'addresses' should not be null");

        // Permutation is faster and has less over head compared to Fisher-Yates shuffle
        // Permutation is optimized for most common scenario where replica count is 5 or less
        // Fisher-Yates shuffle is used in-case the passed in URI list is larger than the predefined permutation list.
        if (AddressEnumeratorUsingPermutations.isSizeInPermutationLimits(addresses.size())) {
            return AddressEnumeratorUsingPermutations.getTransportAddressUris(addresses);
        }
        return AddressEnumeratorFisherYateShuffle.getTransportAddressUris(addresses);
    }

    private Uri.HealthStatus getEffectiveStatus(Uri addressUri, Set<Uri> failedEndpoints) {
        checkNotNull(addressUri, "Argument 'addressUri' should not be null");

        if (failedEndpoints != null && failedEndpoints.contains(addressUri)) {
            return Unhealthy;
        }

        return addressUri.getEffectiveHealthStatus();
    }
}
