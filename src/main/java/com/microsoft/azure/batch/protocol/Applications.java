/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.batch.protocol.models.ApplicationGetHeaders;
import com.microsoft.azure.batch.protocol.models.ApplicationGetOptions;
import com.microsoft.azure.batch.protocol.models.ApplicationListHeaders;
import com.microsoft.azure.batch.protocol.models.ApplicationListNextOptions;
import com.microsoft.azure.batch.protocol.models.ApplicationListOptions;
import com.microsoft.azure.batch.protocol.models.ApplicationSummary;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in Applications.
 */
public interface Applications {
    /**
     * Lists all of the applications available in the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ApplicationSummary&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<ApplicationSummary>, ApplicationListHeaders> list() throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the applications available in the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final ListOperationCallback<ApplicationSummary> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the applications available in the specified account.
     *
     * @param applicationListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ApplicationSummary&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<ApplicationSummary>, ApplicationListHeaders> list(final ApplicationListOptions applicationListOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the applications available in the specified account.
     *
     * @param applicationListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final ApplicationListOptions applicationListOptions, final ListOperationCallback<ApplicationSummary> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets information about the specified application.
     *
     * @param applicationId The id of the application.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ApplicationSummary object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<ApplicationSummary, ApplicationGetHeaders> get(String applicationId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified application.
     *
     * @param applicationId The id of the application.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String applicationId, final ServiceCallback<ApplicationSummary> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets information about the specified application.
     *
     * @param applicationId The id of the application.
     * @param applicationGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ApplicationSummary object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<ApplicationSummary, ApplicationGetHeaders> get(String applicationId, ApplicationGetOptions applicationGetOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified application.
     *
     * @param applicationId The id of the application.
     * @param applicationGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String applicationId, ApplicationGetOptions applicationGetOptions, final ServiceCallback<ApplicationSummary> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the applications available in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ApplicationSummary&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<ApplicationSummary>, ApplicationListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the applications available in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<ApplicationSummary> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the applications available in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param applicationListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ApplicationSummary&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<ApplicationSummary>, ApplicationListHeaders> listNext(final String nextPageLink, final ApplicationListNextOptions applicationListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the applications available in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param applicationListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ApplicationListNextOptions applicationListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<ApplicationSummary> serviceCallback) throws IllegalArgumentException;

}
