// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.implementation.accesshelpers;

import java.util.Map;

import com.azure.communication.phonenumbers.models.AvailablePhoneNumber;
import com.azure.communication.phonenumbers.models.PhoneNumbersReservation;

/**
 * Class containing helper methods for accessing private members of {@link PhoneNumbersReservation}.
 */
public final class PhoneNumbersReservationAccessHelper {
    private static PhoneNumbersReservationAccessor accessor;

    /**
     * Type defining the methods to set the non-public properties of an {@link PhoneNumbersReservation} instance.
     */
    public interface PhoneNumbersReservationAccessor {
        /**
         * Sets the phoneNumbers property of the {@link PhoneNumbersReservation}.
         *
         * @param reservation The {@link PhoneNumbersReservation} instance
         * @param phoneNumbers The map of phone numbers to set
         */
        void setPhoneNumbers(PhoneNumbersReservation reservation, Map<String, AvailablePhoneNumber> phoneNumbers);
    }

    /**
     * The method called from {@link PhoneNumbersReservation} to set it's accessor.
     *
     * @param phoneNumbersReservationAccessor The accessor.
     */
    public static void setAccessor(final PhoneNumbersReservationAccessor phoneNumbersReservationAccessor) {
        accessor = phoneNumbersReservationAccessor;
    }

    /**
         * Sets the phoneNumbers property of the {@link PhoneNumbersReservation}.
         *
         * @param reservation The {@link PhoneNumbersReservation} instance
         * @param phoneNumbers The map of phone numbers to set
         */
    public static void setPhoneNumbers(PhoneNumbersReservation reservation,
        Map<String, AvailablePhoneNumber> phoneNumbers) {
        accessor.setPhoneNumbers(reservation, phoneNumbers);
    }

    private PhoneNumbersReservationAccessHelper() {
    }
}
