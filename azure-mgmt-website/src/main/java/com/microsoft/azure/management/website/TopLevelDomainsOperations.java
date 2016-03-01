/**
 * Object]
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.website.models.TldLegalAgreementCollection;
import com.microsoft.azure.management.website.models.TopLevelDomain;
import com.microsoft.azure.management.website.models.TopLevelDomainAgreementOption;
import com.microsoft.azure.management.website.models.TopLevelDomainCollection;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in TopLevelDomainsOperations.
 */
public interface TopLevelDomainsOperations {
    /**
     * Lists all top level domains supported for registration.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TopLevelDomainCollection object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<TopLevelDomainCollection> getGetTopLevelDomains() throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists all top level domains supported for registration.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getGetTopLevelDomainsAsync(final ServiceCallback<TopLevelDomainCollection> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets details of a top level domain.
     *
     * @param name Name of the top level domain
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TopLevelDomain object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<TopLevelDomain> getTopLevelDomain(String name) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets details of a top level domain.
     *
     * @param name Name of the top level domain
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getTopLevelDomainAsync(String name, final ServiceCallback<TopLevelDomain> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists legal agreements that user needs to accept before purchasing domain.
     *
     * @param name Name of the top level domain
     * @param agreementOption Domain agreement options
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TldLegalAgreementCollection object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<TldLegalAgreementCollection> listTopLevelDomainAgreements(String name, TopLevelDomainAgreementOption agreementOption) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists legal agreements that user needs to accept before purchasing domain.
     *
     * @param name Name of the top level domain
     * @param agreementOption Domain agreement options
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listTopLevelDomainAgreementsAsync(String name, TopLevelDomainAgreementOption agreementOption, final ServiceCallback<TldLegalAgreementCollection> serviceCallback) throws IllegalArgumentException;

}
