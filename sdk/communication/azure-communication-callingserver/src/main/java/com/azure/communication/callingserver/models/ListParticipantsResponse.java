// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.implementation.accesshelpers.ListParticipantsResponseConstructorProxy;
import com.azure.communication.callingserver.implementation.accesshelpers.TransferCallResponseConstructorProxy;
import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.models.GetParticipantsResponseInternal;
import com.azure.communication.callingserver.implementation.models.TransferCallResponseInternal;
import com.azure.core.annotation.Immutable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** The ListParticipantsResponse model. */
@Immutable
public final class ListParticipantsResponse {
    /*
     * The list of participants in the call.
     */
    private List<CallParticipant> values;

    /*
     * The nextLink property.
     */
    private String nextLink;

    static {
        ListParticipantsResponseConstructorProxy.setAccessor(
            new ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor() {
                @Override
                public ListParticipantsResponse create(GetParticipantsResponseInternal internalHeaders) {
                    return new ListParticipantsResponse(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public ListParticipantsResponse() {
        this.values = null;
        this.nextLink = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param getParticipantsResponseInternal The response from the service.
     */
    ListParticipantsResponse(GetParticipantsResponseInternal getParticipantsResponseInternal) {
        Objects.requireNonNull(getParticipantsResponseInternal, "transferCallResponseInternal must not be null");

        this.values = getParticipantsResponseInternal
            .getValues()
            .stream()
            .map(CallParticipantConverter::convert).collect(Collectors.toList());

        this.nextLink = getParticipantsResponseInternal.getNextLink();
    }

    /**
     * Get the values property: The list of participants in the call.
     *
     * @return The list of participants in the call.
     */
    public List<CallParticipant> getValues() {
        return this.values;
    }

    /**
     * Get the nextLink property: The nextLink property.
     *
     * @return the nextLink value.
     */
    public String getNextLink() {
        return this.nextLink;
    }
}
