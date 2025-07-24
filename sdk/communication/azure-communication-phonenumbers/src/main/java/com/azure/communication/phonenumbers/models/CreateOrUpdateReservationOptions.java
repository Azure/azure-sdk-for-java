// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.models;

import java.util.List;

/**
 * The parameters for the create or update reservation operation.
 */
public class CreateOrUpdateReservationOptions {
    private final String reservationId;
    private List<AvailablePhoneNumber> phoneNumbersToAdd;
    private List<String> phoneNumbersToRemove;

    /**
     * Creates an instance of CreateOrUpdateReservationOptions class.
     * 
     * @param reservationId The reservation ID.
     */
    public CreateOrUpdateReservationOptions(String reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * Get the reservationId property: The reservation ID.
     * 
     * @return the reservationId value.
     */
    public String getReservationId() {
        return reservationId;
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
