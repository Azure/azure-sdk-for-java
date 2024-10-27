// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import java.util.Objects;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumbersImpl;
import com.azure.communication.phonenumbers.models.PhoneNumberAreaCode;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCountry;
import com.azure.communication.phonenumbers.models.PhoneNumberLocality;
import com.azure.communication.phonenumbers.models.PhoneNumberOffering;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.SyncPoller;

/**
 * Synchronous client for Communication service phone number operations.
 *
 * <p>
 * <strong>Instantiating a synchronous Phone Numbers Client</strong>
 * </p>
 *
 * <!-- src_embed com.azure.communication.phonenumbers.client.instantiation -->
 * <pre>
 * PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;keyCredential&#41;
 *     .httpClient&#40;httpClient&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.communication.phonenumbers.client.instantiation -->
 *
 * @see PhoneNumbersClientBuilder
 */
@ServiceClient(builder = PhoneNumbersClientBuilder.class, isAsync = false)
public final class PhoneNumbersClient {

    private final ClientLogger logger = new ClientLogger(PhoneNumbersClient.class);
    private final PhoneNumbersImpl client;
    private final PhoneNumbersAsyncClient asyncClient;

    private final String acceptLanguage;

    PhoneNumbersClient(PhoneNumberAdminClientImpl phoneNumberAdminClient, PhoneNumbersAsyncClient asyncClient) {
        this(phoneNumberAdminClient, asyncClient, null);
    }

    PhoneNumbersClient(PhoneNumberAdminClientImpl phoneNumberAdminClient, PhoneNumbersAsyncClient asyncClient,
            String acceptLanguage) {
        this.client = phoneNumberAdminClient.getPhoneNumbers();
        this.asyncClient = asyncClient;
        this.acceptLanguage = acceptLanguage;
    }

    /**
     * Gets information about a purchased phone number.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.client.getPurchased -->
     * <pre>
     * PurchasedPhoneNumber phoneNumber = phoneNumberClient.getPurchasedPhoneNumber&#40;&quot;+18001234567&quot;&#41;;
     * System.out.println&#40;&quot;Phone Number Value: &quot; + phoneNumber.getPhoneNumber&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Phone Number Country Code: &quot; + phoneNumber.getCountryCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.client.getPurchased -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                    be either + or encoded
     *                    as %2B.
     * @return {@link PurchasedPhoneNumber} representing the purchased telephone
     *         number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PurchasedPhoneNumber getPurchasedPhoneNumber(String phoneNumber) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        return client.getByNumber(phoneNumber);
    }

    /**
     * Gets information about a purchased phone number with response.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.getPurchasedWithResponse -->
     * 
     * <pre>
     * Response&lt;PurchasedPhoneNumber&gt; response = phoneNumberClient
     *         .getPurchasedPhoneNumberWithResponse&#40;&quot;+18001234567&quot;, Context.NONE&#41;;
     * PurchasedPhoneNumber phoneNumber = response.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Phone Number Value: &quot; + phoneNumber.getPhoneNumber&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Phone Number Country Code: &quot; + phoneNumber.getCountryCode&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.client.getPurchasedWithResponse
     * -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                    be either + or encoded
     *                    as %2B.
     * @param context A {@link Context} representing the request context.
     * @return {@link PurchasedPhoneNumber} representing the purchased telephone
     *         number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PurchasedPhoneNumber> getPurchasedPhoneNumberWithResponse(String phoneNumber, Context context) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        context = context == null ? Context.NONE : context;
        return client.getByNumberWithResponseAsync(phoneNumber, context).block();
    }

    /**
     * Gets the list of the purchased phone numbers.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.client.listPurchased -->
     * <pre>
     * PagedIterable&lt;PurchasedPhoneNumber&gt; phoneNumbers = phoneNumberClient.listPurchasedPhoneNumbers&#40;&#41;;
     * PurchasedPhoneNumber phoneNumber = phoneNumbers.iterator&#40;&#41;.next&#40;&#41;;
     * System.out.println&#40;&quot;Phone Number Value: &quot; + phoneNumber.getPhoneNumber&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Phone Number Country Code: &quot; + phoneNumber.getCountryCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.client.listPurchased -->
     *
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PurchasedPhoneNumber> listPurchasedPhoneNumbers() {
        return this.listPurchasedPhoneNumbers(null);
    }

    /**
     * Gets the list of the purchased phone numbers with context.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.listPurchasedWithContext -->
     * 
     * <pre>
     * PagedIterable&lt;PurchasedPhoneNumber&gt; phoneNumbers = phoneNumberClient.listPurchasedPhoneNumbers&#40;Context.NONE&#41;;
     * PurchasedPhoneNumber phoneNumber = phoneNumbers.iterator&#40;&#41;.next&#40;&#41;;
     * System.out.println&#40;&quot;Phone Number Value: &quot; + phoneNumber.getPhoneNumber&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Phone Number Country Code: &quot; + phoneNumber.getCountryCode&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.client.listPurchasedWithContext
     * -->
     *
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PurchasedPhoneNumber> listPurchasedPhoneNumbers(Context context) {
        context = context == null ? Context.NONE : context;
        return client.listPhoneNumbers(null, null, context);
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.beginSearchAvailable -->
     * 
     * <pre>
     * PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities&#40;&#41;
     *         .setCalling&#40;PhoneNumberCapabilityType.INBOUND&#41;
     *         .setSms&#40;PhoneNumberCapabilityType.INBOUND_OUTBOUND&#41;;
     *
     * SyncPoller&lt;PhoneNumberOperation, PhoneNumberSearchResult&gt; poller = phoneNumberClient
     *         .beginSearchAvailablePhoneNumbers&#40;&quot;US&quot;, PhoneNumberType.TOLL_FREE,
     *                 PhoneNumberAssignmentType.APPLICATION, capabilities&#41;;
     * PollResponse&lt;PhoneNumberOperation&gt; response = poller.waitForCompletion&#40;&#41;;
     * String searchId = &quot;&quot;;
     *
     * if &#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus&#40;&#41;&#41; &#123;
     *     PhoneNumberSearchResult searchResult = poller.getFinalResult&#40;&#41;;
     *     searchId = searchResult.getSearchId&#40;&#41;;
     *     System.out.println&#40;&quot;Searched phone numbers: &quot; + searchResult.getPhoneNumbers&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Search expires by: &quot; + searchResult.getSearchExpiresBy&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Phone number costs:&quot; + searchResult.getCost&#40;&#41;.getAmount&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.client.beginSearchAvailable -->
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} The phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number
     *                        assignment type.
     * @param capabilities {@link PhoneNumberCapabilities} The phone number
     *                        capabilities.
     * @return A {@link SyncPoller} object with the reservation result.
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest}
     *                              is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
            String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType,
            PhoneNumberCapabilities capabilities) {
        return asyncClient.beginSearchAvailablePhoneNumbers(countryCode, phoneNumberType, assignmentType, capabilities)
                .getSyncPoller();
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.beginSearchAvailableWithOptions
     * -->
     * 
     * <pre>
     * PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities&#40;&#41;
     *         .setCalling&#40;PhoneNumberCapabilityType.INBOUND&#41;
     *         .setSms&#40;PhoneNumberCapabilityType.INBOUND_OUTBOUND&#41;;
     * PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions&#40;&#41;.setAreaCode&#40;&quot;800&quot;&#41;.setQuantity&#40;1&#41;;
     *
     * SyncPoller&lt;PhoneNumberOperation, PhoneNumberSearchResult&gt; poller = phoneNumberClient
     *         .beginSearchAvailablePhoneNumbers&#40;&quot;US&quot;, PhoneNumberType.TOLL_FREE,
     *                 PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions, Context.NONE&#41;;
     * PollResponse&lt;PhoneNumberOperation&gt; response = poller.waitForCompletion&#40;&#41;;
     * String searchId = &quot;&quot;;
     *
     * if &#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus&#40;&#41;&#41; &#123;
     *     PhoneNumberSearchResult searchResult = poller.getFinalResult&#40;&#41;;
     *     searchId = searchResult.getSearchId&#40;&#41;;
     *     System.out.println&#40;&quot;Searched phone numbers: &quot; + searchResult.getPhoneNumbers&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Search expires by: &quot; + searchResult.getSearchExpiresBy&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Phone number costs:&quot; + searchResult.getCost&#40;&#41;.getAmount&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * 
     * <!-- end
     * com.azure.communication.phonenumbers.client.beginSearchAvailableWithOptions
     * -->
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} The phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number
     *                        assignment type.
     * @param capabilities {@link PhoneNumberCapabilities} The phone number
     *                        capabilities.
     * @param searchOptions The phone number search options.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with the reservation result.
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest}
     *                              is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
            String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType,
            PhoneNumberCapabilities capabilities, PhoneNumberSearchOptions searchOptions, Context context) {
        return asyncClient.beginSearchAvailablePhoneNumbers(countryCode, phoneNumberType, assignmentType, capabilities,
                searchOptions, context).getSyncPoller();
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated
     * with a given id.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.client.beginPurchase -->
     * <pre>
     * PollResponse&lt;PhoneNumberOperation&gt; purchaseResponse =
     *     phoneNumberClient.beginPurchasePhoneNumbers&#40;searchId&#41;.waitForCompletion&#40;&#41;;
     * System.out.println&#40;&quot;Purchase phone numbers is complete: &quot; + purchaseResponse.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.client.beginPurchase -->
     *
     * @param searchId ID of the search
     * @return A {@link SyncPoller} object with PurchasePhoneNumbersResult.
     * @throws NullPointerException if {@code searchId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers(String searchId) {
        return asyncClient.beginPurchasePhoneNumbers(searchId).getSyncPoller();
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated
     * with a given id.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.beginPurchaseWithContext -->
     * 
     * <pre>
     * PollResponse&lt;PhoneNumberOperation&gt; purchaseResponse = phoneNumberClient
     *         .beginPurchasePhoneNumbers&#40;searchId, Context.NONE&#41;.waitForCompletion&#40;&#41;;
     * System.out.println&#40;&quot;Purchase phone numbers is complete: &quot; + purchaseResponse.getStatus&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.client.beginPurchaseWithContext
     * -->
     *
     * @param searchId ID of the search
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with PurchasePhoneNumbersResult.
     * @throws NullPointerException if {@code searchId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers(String searchId,
            Context context) {
        return asyncClient.beginPurchasePhoneNumbers(searchId, context).getSyncPoller();
    }

    /**
     * Starts the update of capabilities for a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.client.beginRelease -->
     * <pre>
     * PollResponse&lt;PhoneNumberOperation&gt; releaseResponse =
     *     phoneNumberClient.beginReleasePhoneNumber&#40;&quot;+18001234567&quot;&#41;.waitForCompletion&#40;&#41;;
     * System.out.println&#40;&quot;Release phone number is complete: &quot; + releaseResponse.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.client.beginRelease -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                    be either + or encoded
     *                    as %2B.
     * @return A {@link SyncPoller} object with ReleasePhoneNumberResult.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber(String phoneNumber) {
        return asyncClient.beginReleasePhoneNumber(phoneNumber).getSyncPoller();
    }

    /**
     * Starts the update of capabilities for a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.beginReleaseWithContext -->
     * 
     * <pre>
     * PollResponse&lt;PhoneNumberOperation&gt; releaseResponse = phoneNumberClient
     *         .beginReleasePhoneNumber&#40;&quot;+18001234567&quot;, Context.NONE&#41;.waitForCompletion&#40;&#41;;
     * System.out.println&#40;&quot;Release phone number is complete: &quot; + releaseResponse.getStatus&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.client.beginReleaseWithContext
     * -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                    be either + or encoded
     *                    as %2B.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with ReleasePhoneNumberResult.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber(String phoneNumber,
            Context context) {
        return asyncClient.beginReleasePhoneNumber(phoneNumber, context).getSyncPoller();
    }

    /**
     * Update capabilities of a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.beginUpdateCapabilities -->
     * 
     * <pre>
     * PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities&#40;&#41;;
     * capabilities
     *         .setCalling&#40;PhoneNumberCapabilityType.INBOUND&#41;
     *         .setSms&#40;PhoneNumberCapabilityType.INBOUND_OUTBOUND&#41;;
     *
     * SyncPoller&lt;PhoneNumberOperation, PurchasedPhoneNumber&gt; poller = phoneNumberClient
     *         .beginUpdatePhoneNumberCapabilities&#40;&quot;+18001234567&quot;, capabilities&#41;;
     * PollResponse&lt;PhoneNumberOperation&gt; response = poller.waitForCompletion&#40;&#41;;
     *
     * if &#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus&#40;&#41;&#41; &#123;
     *     PurchasedPhoneNumber phoneNumber = poller.getFinalResult&#40;&#41;;
     *     System.out.println&#40;&quot;Phone Number Calling capabilities: &quot; + phoneNumber.getCapabilities&#40;&#41;.getCalling&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Phone Number SMS capabilities: &quot; + phoneNumber.getCapabilities&#40;&#41;.getSms&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.client.beginUpdateCapabilities
     * -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                     be either + or encoded
     *                     as %2B.
     * @param capabilities Update capabilities of a purchased phone number.
     * @return A {@link SyncPoller} object with purchased phone number.
     * @throws NullPointerException if {@code phoneNumber} or {@code capabilities}
     *                              is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber,
            PhoneNumberCapabilities capabilities) {
        return asyncClient.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities).getSyncPoller();
    }

    /**
     * Update capabilities of a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.client.beginUpdateCapabilitiesWithContext
     * -->
     * 
     * <pre>
     * PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities&#40;&#41;;
     * capabilities
     *         .setCalling&#40;PhoneNumberCapabilityType.INBOUND&#41;
     *         .setSms&#40;PhoneNumberCapabilityType.INBOUND_OUTBOUND&#41;;
     *
     * SyncPoller&lt;PhoneNumberOperation, PurchasedPhoneNumber&gt; poller = phoneNumberClient
     *         .beginUpdatePhoneNumberCapabilities&#40;&quot;+18001234567&quot;, capabilities, Context.NONE&#41;;
     * PollResponse&lt;PhoneNumberOperation&gt; response = poller.waitForCompletion&#40;&#41;;
     *
     * if &#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus&#40;&#41;&#41; &#123;
     *     PurchasedPhoneNumber phoneNumber = poller.getFinalResult&#40;&#41;;
     *     System.out.println&#40;&quot;Phone Number Calling capabilities: &quot; + phoneNumber.getCapabilities&#40;&#41;.getCalling&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Phone Number SMS capabilities: &quot; + phoneNumber.getCapabilities&#40;&#41;.getSms&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * 
     * <!-- end
     * com.azure.communication.phonenumbers.client.beginUpdateCapabilitiesWithContext
     * -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                     be either + or encoded
     *                     as %2B.
     * @param capabilities Update capabilities of a purchased phone number.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with purchased phone number.
     * @throws NullPointerException if {@code phoneNumber} or {@code capabilities}
     *                              is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber,
            PhoneNumberCapabilities capabilities, Context context) {
        return asyncClient.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities, context).getSyncPoller();
    }

    /**
     * Gets the list of the available countries.
     *
     * @return A {@link PagedIterable} of {@link PhoneNumberCountry} instances
     *         representing available countries.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberCountry> listAvailableCountries() {
        return this.listAvailableCountries(null);
    }

    /**
     * Gets the list of the purchased phone numbers with context.
     *
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhoneNumberCountry} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberCountry> listAvailableCountries(Context context) {
        context = context == null ? Context.NONE : context;
        return client.listAvailableCountries(null, null, acceptLanguage, context);
    }

    /**
     * Gets the list of the available localities. I.e. cities, towns.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param administrativeDivision An optional parameter. The name or short name
     *                               of the state/province within which to list the
     *                               localities.
     * @return A {@link PagedIterable} of {@link PhoneNumberLocality} instances
     *         representing available localities with phone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberLocality> listAvailableLocalities(String countryCode,
            String administrativeDivision) {
        return this.listAvailableLocalities(countryCode, administrativeDivision, null);
    }

    /**
     * Gets the list of the available localities. I.e. cities, towns.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param administrativeDivision An optional parameter. The name or short name
     *                               of the state/province within which to list the
     *                               localities.
     * @param context A {@link Context} representing the request
     *                               context.
     * @return A {@link PagedIterable} of {@link PhoneNumberLocality} instances
     *         representing available localities with phone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberLocality> listAvailableLocalities(String countryCode, String administrativeDivision,
            Context context) {
        context = context == null ? Context.NONE : context;
        return client.listAvailableLocalities(countryCode, null, null, administrativeDivision, acceptLanguage, context);
    }

    /**
     * Gets the list of the available Toll-Free area codes for a given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @return A {@link PagedIterable} of {@link PhoneNumberAreaCode} instances
     *         representing available area codes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberAreaCode> listAvailableTollFreeAreaCodes(String countryCode) {
        return this.listAvailableTollFreeAreaCodes(countryCode, null);
    }

    /**
     * Gets the list of the available Toll-Free area codes for a given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhoneNumberAreaCode} instances
     *         representing available area codes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberAreaCode> listAvailableTollFreeAreaCodes(String countryCode, Context context) {
        return client.listAreaCodes(countryCode, PhoneNumberType.TOLL_FREE, null, null,
                PhoneNumberAssignmentType.APPLICATION, null, null, acceptLanguage);
    }

    /**
     * Gets the list of the available Geographic area codes for a given country and
     * locality.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone
     *                               number assignment type.
     * @param locality The name of the locality (e.g. city or town
     *                               name) in which to fetch area codes.
     * @param administrativeDivision An optional parameter. The name of the
     *                               administrative division (e.g. state or
     *                               province) of the locality.
     * @return A {@link PagedIterable} of {@link PhoneNumberAreaCode} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberAreaCode> listAvailableGeographicAreaCodes(String countryCode,
            PhoneNumberAssignmentType assignmentType, String locality, String administrativeDivision) {
        return this.listAvailableGeographicAreaCodes(countryCode, assignmentType, locality, administrativeDivision,
                null);
    }

    /**
     * Gets the list of the available Geographic area codes for a given country and
     * locality.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone
     *                               number assignment type.
     * @param locality The name of the locality (e.g. city or town
     *                               name) in which to fetch area codes.
     * @param administrativeDivision An optional parameter. The name of the
     *                               administrative division (e.g. state or
     *                               province) of the locality.
     * @param context A {@link Context} representing the request
     *                               context.
     * @return A {@link PagedIterable} of {@link PhoneNumberAreaCode} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberAreaCode> listAvailableGeographicAreaCodes(String countryCode,
            PhoneNumberAssignmentType assignmentType, String locality, String administrativeDivision, Context context) {
        context = context == null ? Context.NONE : context;
        return client.listAreaCodes(countryCode, PhoneNumberType.GEOGRAPHIC, null, null, assignmentType, locality,
                administrativeDivision, acceptLanguage, context);
    }

    /**
     * Gets the list of the available phone number offerings for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} Optional parameter. Restrict
     *                        the offerings to the phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} Optional parameter.
     *                        Restrict the offerings to the assignment type.
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberOffering> listAvailableOfferings(String countryCode,
            PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType) {
        return this.listAvailableOfferings(countryCode, phoneNumberType, assignmentType, null);
    }

    /**
     * Gets the list of the available phone number offerings for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} Optional parameter. Restrict
     *                        the offerings to the phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} Optional parameter.
     *                        Restrict the offerings to the assignment type.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberOffering> listAvailableOfferings(String countryCode,
            PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType, Context context) {
        context = context == null ? Context.NONE : context;
        return client.listOfferings(countryCode, null, null, phoneNumberType, assignmentType, acceptLanguage, context);
    }
}
