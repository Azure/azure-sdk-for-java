/**
 * Object]
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.website.models.DomainAvailablilityCheckResult;
import com.microsoft.azure.management.website.models.DomainCollection;
import com.microsoft.azure.management.website.models.DomainControlCenterSsoRequest;
import com.microsoft.azure.management.website.models.DomainRecommendationSearchParameters;
import com.microsoft.azure.management.website.models.DomainRegistrationInput;
import com.microsoft.azure.management.website.models.NameIdentifier;
import com.microsoft.azure.management.website.models.NameIdentifierCollection;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in GlobalDomainRegistrationOperations.
 */
public interface GlobalDomainRegistrationOperations {
    /**
     * Lists all domains in a subscription.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DomainCollection object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<DomainCollection> getAllDomains() throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists all domains in a subscription.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAllDomainsAsync(final ServiceCallback<DomainCollection> serviceCallback) throws IllegalArgumentException;

    /**
     * Generates a single sign on request for domain management portal.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DomainControlCenterSsoRequest object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<DomainControlCenterSsoRequest> getDomainControlCenterSsoRequest() throws CloudException, IOException, IllegalArgumentException;

    /**
     * Generates a single sign on request for domain management portal.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getDomainControlCenterSsoRequestAsync(final ServiceCallback<DomainControlCenterSsoRequest> serviceCallback) throws IllegalArgumentException;

    /**
     * Validates domain registration information.
     *
     * @param domainRegistrationInput Domain registration information
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<Object> validateDomainPurchaseInformation(DomainRegistrationInput domainRegistrationInput) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Validates domain registration information.
     *
     * @param domainRegistrationInput Domain registration information
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall validateDomainPurchaseInformationAsync(DomainRegistrationInput domainRegistrationInput, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException;

    /**
     * Checks if a domain is available for registration.
     *
     * @param identifier Name of the domain
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DomainAvailablilityCheckResult object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<DomainAvailablilityCheckResult> checkDomainAvailability(NameIdentifier identifier) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Checks if a domain is available for registration.
     *
     * @param identifier Name of the domain
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall checkDomainAvailabilityAsync(NameIdentifier identifier, final ServiceCallback<DomainAvailablilityCheckResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists domain recommendations based on keywords.
     *
     * @param parameters Domain recommendation search parameters
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the NameIdentifierCollection object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<NameIdentifierCollection> listDomainRecommendations(DomainRecommendationSearchParameters parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists domain recommendations based on keywords.
     *
     * @param parameters Domain recommendation search parameters
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listDomainRecommendationsAsync(DomainRecommendationSearchParameters parameters, final ServiceCallback<NameIdentifierCollection> serviceCallback) throws IllegalArgumentException;

}
