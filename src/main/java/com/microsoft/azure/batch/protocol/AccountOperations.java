/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.batch.protocol.models.AccountListNodeAgentSkusHeaders;
import com.microsoft.azure.batch.protocol.models.AccountListNodeAgentSkusNextOptions;
import com.microsoft.azure.batch.protocol.models.AccountListNodeAgentSkusOptions;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.NodeAgentSku;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in AccountOperations.
 */
public interface AccountOperations {
    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkus() throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNodeAgentSkusAsync(final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param accountListNodeAgentSkusOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkus(final AccountListNodeAgentSkusOptions accountListNodeAgentSkusOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param accountListNodeAgentSkusOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNodeAgentSkusAsync(final AccountListNodeAgentSkusOptions accountListNodeAgentSkusOptions, final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkusNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNodeAgentSkusNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param accountListNodeAgentSkusNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkusNext(final String nextPageLink, final AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param accountListNodeAgentSkusNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNodeAgentSkusNextAsync(final String nextPageLink, final AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions, final ServiceCall serviceCall, final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException;

}
