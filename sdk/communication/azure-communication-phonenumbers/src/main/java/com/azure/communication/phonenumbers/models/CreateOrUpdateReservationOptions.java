// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.models;

import java.util.List;
import java.util.UUID;

public class CreateOrUpdateReservationOptions {
    private UUID reservationId;
    private List<AvailablePhoneNumber> phoneNumbersToAdd;
    private List<String> phoneNumbersToRemove;

    public CreateOrUpdateReservationOptions(UUID reservationId, List<AvailablePhoneNumber> phoneNumbersToAdd,
        List<String> phoneNumbersToRemove) {
        this.reservationId = reservationId;
        this.phoneNumbersToAdd = phoneNumbersToAdd;
        this.phoneNumbersToRemove = phoneNumbersToRemove;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public List<AvailablePhoneNumber> getPhoneNumbersToAdd() {
        return phoneNumbersToAdd;
    }

    public void setPhoneNumbersToAdd(List<AvailablePhoneNumber> phoneNumbersToAdd) {
        this.phoneNumbersToAdd = phoneNumbersToAdd;
    }

    public List<String> getPhoneNumbersToRemove() {
        return phoneNumbersToRemove;
    }

    public void setPhoneNumbersToRemove(List<String> phoneNumbersToRemove) {
        this.phoneNumbersToRemove = phoneNumbersToRemove;
    }
}
