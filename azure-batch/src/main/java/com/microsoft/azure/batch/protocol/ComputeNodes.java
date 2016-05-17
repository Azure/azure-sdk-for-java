/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.ComputeNode;
import com.microsoft.azure.batch.protocol.models.ComputeNodeAddUserHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeAddUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDeleteUserHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDeleteUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDisableSchedulingHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDisableSchedulingOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeEnableSchedulingHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeEnableSchedulingOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteDesktopHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteDesktopOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsResult;
import com.microsoft.azure.batch.protocol.models.ComputeNodeListHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeListNextOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeListOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootOption;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageOption;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUpdateUserHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUpdateUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUser;
import com.microsoft.azure.batch.protocol.models.DisableComputeNodeSchedulingOption;
import com.microsoft.azure.batch.protocol.models.NodeUpdateUserParameter;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.InputStream;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in ComputeNodes.
 */
public interface ComputeNodes {
    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user The user account to be created.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeaders> addUser(String poolId, String nodeId, ComputeNodeUser user) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user The user account to be created.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addUserAsync(String poolId, String nodeId, ComputeNodeUser user, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user The user account to be created.
     * @param computeNodeAddUserOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeaders> addUser(String poolId, String nodeId, ComputeNodeUser user, ComputeNodeAddUserOptions computeNodeAddUserOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user The user account to be created.
     * @param computeNodeAddUserOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addUserAsync(String poolId, String nodeId, ComputeNodeUser user, ComputeNodeAddUserOptions computeNodeAddUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeaders> deleteUser(String poolId, String nodeId, String userName) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteUserAsync(String poolId, String nodeId, String userName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @param computeNodeDeleteUserOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeaders> deleteUser(String poolId, String nodeId, String userName, ComputeNodeDeleteUserOptions computeNodeDeleteUserOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @param computeNodeDeleteUserOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteUserAsync(String poolId, String nodeId, String userName, ComputeNodeDeleteUserOptions computeNodeDeleteUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeaders> updateUser(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updateUserAsync(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @param computeNodeUpdateUserOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeaders> updateUser(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, ComputeNodeUpdateUserOptions computeNodeUpdateUserOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @param computeNodeUpdateUserOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updateUserAsync(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, ComputeNodeUpdateUserOptions computeNodeUpdateUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNode object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<ComputeNode, ComputeNodeGetHeaders> get(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String poolId, String nodeId, final ServiceCallback<ComputeNode> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @param computeNodeGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNode object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<ComputeNode, ComputeNodeGetHeaders> get(String poolId, String nodeId, ComputeNodeGetOptions computeNodeGetOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @param computeNodeGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String poolId, String nodeId, ComputeNodeGetOptions computeNodeGetOptions, final ServiceCallback<ComputeNode> serviceCallback) throws IllegalArgumentException;

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeRebootHeaders> reboot(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall rebootAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeRebootOption When to reboot the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeRebootOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeRebootHeaders> reboot(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, ComputeNodeRebootOptions computeNodeRebootOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeRebootOption When to reboot the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeRebootOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall rebootAsync(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, ComputeNodeRebootOptions computeNodeRebootOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeReimageHeaders> reimage(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall reimageAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeReimageOption When to reimage the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeReimageOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeReimageHeaders> reimage(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, ComputeNodeReimageOptions computeNodeReimageOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeReimageOption When to reimage the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeReimageOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall reimageAsync(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, ComputeNodeReimageOptions computeNodeReimageOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeaders> disableScheduling(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall disableSchedulingAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @param nodeDisableSchedulingOption What to do with currently running tasks when disable task scheduling on the compute node. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion'
     * @param computeNodeDisableSchedulingOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeaders> disableScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, ComputeNodeDisableSchedulingOptions computeNodeDisableSchedulingOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @param nodeDisableSchedulingOption What to do with currently running tasks when disable task scheduling on the compute node. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion'
     * @param computeNodeDisableSchedulingOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall disableSchedulingAsync(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, ComputeNodeDisableSchedulingOptions computeNodeDisableSchedulingOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeaders> enableScheduling(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall enableSchedulingAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @param computeNodeEnableSchedulingOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeaders> enableScheduling(String poolId, String nodeId, ComputeNodeEnableSchedulingOptions computeNodeEnableSchedulingOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @param computeNodeEnableSchedulingOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall enableSchedulingAsync(String poolId, String nodeId, ComputeNodeEnableSchedulingOptions computeNodeEnableSchedulingOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNodeGetRemoteLoginSettingsResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResult, ComputeNodeGetRemoteLoginSettingsHeaders> getRemoteLoginSettings(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getRemoteLoginSettingsAsync(String poolId, String nodeId, final ServiceCallback<ComputeNodeGetRemoteLoginSettingsResult> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @param computeNodeGetRemoteLoginSettingsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNodeGetRemoteLoginSettingsResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResult, ComputeNodeGetRemoteLoginSettingsHeaders> getRemoteLoginSettings(String poolId, String nodeId, ComputeNodeGetRemoteLoginSettingsOptions computeNodeGetRemoteLoginSettingsOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @param computeNodeGetRemoteLoginSettingsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getRemoteLoginSettingsAsync(String poolId, String nodeId, ComputeNodeGetRemoteLoginSettingsOptions computeNodeGetRemoteLoginSettingsOptions, final ServiceCallback<ComputeNodeGetRemoteLoginSettingsResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeaders> getRemoteDesktop(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getRemoteDesktopAsync(String poolId, String nodeId, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @param computeNodeGetRemoteDesktopOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeaders> getRemoteDesktop(String poolId, String nodeId, ComputeNodeGetRemoteDesktopOptions computeNodeGetRemoteDesktopOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @param computeNodeGetRemoteDesktopOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getRemoteDesktopAsync(String poolId, String nodeId, ComputeNodeGetRemoteDesktopOptions computeNodeGetRemoteDesktopOptions, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<ComputeNode>, ComputeNodeListHeaders> list(final String poolId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String poolId, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @param computeNodeListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<ComputeNode>, ComputeNodeListHeaders> list(final String poolId, final ComputeNodeListOptions computeNodeListOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @param computeNodeListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String poolId, final ComputeNodeListOptions computeNodeListOptions, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param computeNodeListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> listNext(final String nextPageLink, final ComputeNodeListNextOptions computeNodeListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param computeNodeListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ComputeNodeListNextOptions computeNodeListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException;

}
