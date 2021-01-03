package com.azure.communication.administration;

public class PhoneNumberClient {

    private final PhoneNumberAsyncClient phoneNumberAsyncClient;

    PhoneNumberClient(PhoneNumberAsyncClient phoneNumberAsyncClient) {
        this.phoneNumberAsyncClient = phoneNumberAsyncClient;
    }
}
