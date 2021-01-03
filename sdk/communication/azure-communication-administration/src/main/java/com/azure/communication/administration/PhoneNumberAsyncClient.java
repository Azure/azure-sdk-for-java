package com.azure.communication.administration;

import com.azure.communication.administration.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.administration.implementation.PhoneNumbersImpl;
import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.PhoneNumberSearchRequest;
import com.azure.communication.common.PhoneNumber;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollerFlux;

import java.time.Duration;
import java.util.List;

import static com.azure.core.util.FluxUtil.pagedFluxError;

/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = true)
public final class PhoneNumberAsyncClient {
    private final ClientLogger logger = new ClientLogger(PhoneNumberAsyncClient.class);
    private final PhoneNumbersImpl phoneNumbers;
    private final Duration defaultPollInterval = Duration.ofSeconds(1);

    PhoneNumberAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        this.phoneNumbers = phoneNumberAdminClient.getPhoneNumbers();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AcquiredPhoneNumber> listPhoneNumbers(String locale) {
        return listPhoneNumbers(locale, null);
    }

    PagedFlux<AcquiredPhoneNumber> listPhoneNumbers(String locale, Context context) {

            return null;
    }

    public void updatePhoneNumber(){

    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Void, Void> beginPurchasePhoneNumbers(String reservationId, Duration pollInterval) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Void, Void> beginReleasePhoneNumber( Duration pollInterval) {
        return null;
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<PhoneNumberSearchRequest, PhoneNumberSearchRequest> beginSearchAvailablePhoneNumbers(
        Duration pollInterval){
        return null;
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AcquiredPhoneNumber, AcquiredPhoneNumber> beginUpdatePhoneNumberCapabilities(
        Duration pollInterval){
        return null;
    }


}
