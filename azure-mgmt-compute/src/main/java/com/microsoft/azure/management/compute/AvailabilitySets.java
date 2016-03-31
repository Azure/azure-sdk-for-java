/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.models.AvailabilitySet;
import com.microsoft.azure.management.compute.models.VirtualMachineSize;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import java.util.List;

/**
 * An instance of this class provides access to all the operations defined
 * in AvailabilitySets.
 */
public interface AvailabilitySets {
    /**
     * The operation to create or update the availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param name Parameters supplied to the Create Availability Set operation.
     * @param parameters Parameters supplied to the Create Availability Set operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AvailabilitySet object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<AvailabilitySet> createOrUpdate(String resourceGroupName, String name, AvailabilitySet parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * The operation to create or update the availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param name Parameters supplied to the Create Availability Set operation.
     * @param parameters Parameters supplied to the Create Availability Set operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createOrUpdateAsync(String resourceGroupName, String name, AvailabilitySet parameters, final ServiceCallback<AvailabilitySet> serviceCallback) throws IllegalArgumentException;

    /**
     * The operation to delete the availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param availabilitySetName The name of the availability set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> delete(String resourceGroupName, String availabilitySetName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * The operation to delete the availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param availabilitySetName The name of the availability set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String resourceGroupName, String availabilitySetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * The operation to get the availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param availabilitySetName The name of the availability set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AvailabilitySet object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<AvailabilitySet> get(String resourceGroupName, String availabilitySetName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * The operation to get the availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param availabilitySetName The name of the availability set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String resourceGroupName, String availabilitySetName, final ServiceCallback<AvailabilitySet> serviceCallback) throws IllegalArgumentException;

    /**
     * The operation to list the availability sets.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;AvailabilitySet&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<List<AvailabilitySet>> list(String resourceGroupName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * The operation to list the availability sets.
     *
     * @param resourceGroupName The name of the resource group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(String resourceGroupName, final ServiceCallback<List<AvailabilitySet>> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all available virtual machine sizes that can be used to create a new virtual machine in an existing availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param availabilitySetName The name of the availability set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualMachineSize&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<List<VirtualMachineSize>> listAvailableSizes(String resourceGroupName, String availabilitySetName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists all available virtual machine sizes that can be used to create a new virtual machine in an existing availability set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param availabilitySetName The name of the availability set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAvailableSizesAsync(String resourceGroupName, String availabilitySetName, final ServiceCallback<List<VirtualMachineSize>> serviceCallback) throws IllegalArgumentException;

}
