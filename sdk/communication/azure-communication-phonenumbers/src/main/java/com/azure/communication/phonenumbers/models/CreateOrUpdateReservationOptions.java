// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.models;

import java.util.List;
import java.util.UUID;

/**
 * The parameters for the create or update reservation operation.
 */
public class CreateOrUpdateReservationOptions {
    private UUID reservationId;
    private List<AvailablePhoneNumber> phoneNumbersToAdd;
    private List<String> phoneNumbersToRemove;

    /**
     * Creates an instance of CreateOrUpdateReservationOptions class.
     * 
     * @param reservationId The reservation ID.
     * @param phoneNumbersToAdd The list of phone numbers to add to the reservation.
     * @param phoneNumbersToRemove The list of phone numbers to remove from the reservation.
     */
    public CreateOrUpdateReservationOptions(UUID reservationId, List<AvailablePhoneNumber> phoneNumbersToAdd,
        List<String> phoneNumbersToRemove) {
        this.reservationId = reservationId;
        this.phoneNumbersToAdd = phoneNumbersToAdd;
        this.phoneNumbersToRemove = phoneNumbersToRemove;
    }

    /**
     * Get the reservationId property: The reservation ID.
     * 
     * @return the reservationId value.
     */
    public UUID getReservationId() {
        return reservationId;
    }

    /**
     * Set the reservationId property: The reservation ID.
     * 
     * @param reservationId the reservationId value to set.
     * @return the CreateOrUpdateReservationOptions itself.
     */
    public CreateOrUpdateReservationOptions setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
        return this;
    }

    /**
     * Get the phoneNumbersToAdd property: The list of phone numbers to add to the reservation.
     * 
     * @return the phoneNumbersToAdd value.
     */
    public List<AvailablePhoneNumber> getPhoneNumbersToAdd() {
        return phoneNumbersToAdd;
    }

    /**
     * Set the phoneNumbersToAdd property: The list of phone numbers to add to the reservation.
     * 
     * @param phoneNumbersToAdd the phoneNumbersToAdd value to set.
     * @return the CreateOrUpdateReservationOptions itself.
     */
    public CreateOrUpdateReservationOptions setPhoneNumbersToAdd(List<AvailablePhoneNumber> phoneNumbersToAdd) {
        this.phoneNumbersToAdd = phoneNumbersToAdd;
        return this;
    }

    /**
     * Get the phoneNumbersToRemove property: The list of phone numbers to remove from the reservation.
     * 
     * @return the phoneNumbersToRemove value.
     */
    public List<String> getPhoneNumbersToRemove() {
        return phoneNumbersToRemove;
    }

    /**
     * Set the phoneNumbersToRemove property: The list of phone numbers to remove from the reservation.
     * 
     * @param phoneNumbersToRemove the phoneNumbersToRemove value to set.
     * @return the CreateOrUpdateReservationOptions itself.
     */
    public CreateOrUpdateReservationOptions setPhoneNumbersToRemove(List<String> phoneNumbersToRemove) {
        this.phoneNumbersToRemove = phoneNumbersToRemove;
        return this;
    }
}
