// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.ListParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CallParticipantConverter;
import com.azure.communication.callautomation.implementation.models.GetParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** The ListParticipantsResult model. */
@Immutable
public final class ListParticipantsResult {
    /*
     * The list of participants in the call.
     */
    private final List<CallParticipant> values;

    static {
        ListParticipantsResponseConstructorProxy.setAccessor(
            new ListParticipantsResponseConstructorProxy.ListParticipantsResponseConstructorAccessor() {
                @Override
                public ListParticipantsResult create(GetParticipantsResponseInternal internalHeaders) {
                    return new ListParticipantsResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public ListParticipantsResult() {
        this.values = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param getParticipantsResponseInternal The response from the service.
     */
    ListParticipantsResult(GetParticipantsResponseInternal getParticipantsResponseInternal) {
        Objects.requireNonNull(getParticipantsResponseInternal, "transferCallResponseInternal must not be null");

        this.values = getParticipantsResponseInternal
            .getValues()
            .stream()
            .map(CallParticipantConverter::convert).collect(Collectors.toList());
    }

    /**
     * Get the values property: The list of participants in the call.
     *
     * @return The list of participants in the call.
     */
    public List<CallParticipant> getValues() {
        return this.values;
    }
}
