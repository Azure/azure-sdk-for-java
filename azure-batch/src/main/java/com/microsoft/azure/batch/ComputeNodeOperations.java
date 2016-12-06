/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.google.common.io.CharStreams;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.rest.ServiceResponseWithHeaders;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

/**
 * Performs compute node related operations on an Azure Batch account.
 */
public class ComputeNodeOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    ComputeNodeOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @return A list of BatchClientBehavior
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    /**
     * Sets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of BatchClientBehavior classes
     * @return The current instance
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    /**
     * Creates a {@link ComputeNodeUser} representing a new compute node user account that
     * does not yet exist in the Batch service.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be created.
     * @param user the user account which to be created
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void addComputeNodeUser(String poolId, String nodeId, ComputeNodeUser user) throws BatchErrorException, IOException {
        addComputeNodeUser(poolId, nodeId, user, null);
    }

    /**
     * Creates a {@link ComputeNodeUser} representing a new compute node user account that
     * does not yet exist in the Batch service.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be created.
     * @param user the user account which to be created
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void addComputeNodeUser(String poolId, String nodeId, ComputeNodeUser user, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeAddUserOptions options = new ComputeNodeAddUserOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().computeNodes().addUser(poolId, nodeId, user, options);
    }

    /**
     * Deletes the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be deleted.
     * @param userName the user name of the account which to be deleted
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteComputeNodeUser(String poolId, String nodeId, String userName) throws BatchErrorException, IOException {
        deleteComputeNodeUser(poolId, nodeId, userName, null);
    }

    /**
     * Deletes the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be deleted.
     * @param userName the user name of the account which to be deleted
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteComputeNodeUser(String poolId, String nodeId, String userName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeDeleteUserOptions options = new ComputeNodeDeleteUserOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().computeNodes().deleteUser(poolId, nodeId, userName, options);
    }

    /**
     * Updates the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be updated.
     * @param userName the user name of the account which to be updated.
     * @param password the password of the account.
     * @param expiryTime the time at which the account should expire.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String password, DateTime expiryTime) throws BatchErrorException, IOException {
        updateComputeNodeUser(poolId, nodeId, userName, password, expiryTime, null);
    }

    /**
     * Updates the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be updated.
     * @param userName the user name of the account which to be updated.
     * @param password the password of the account.
     * @param expiryTime the time at which the account should expire.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String password, DateTime expiryTime, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        NodeUpdateUserParameter param = new NodeUpdateUserParameter();
        param.withPassword(password);
        param.withExpiryTime(expiryTime);

        updateComputeNodeUser(poolId, nodeId, userName, param, additionalBehaviors);
    }

    /**
     * Updates the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be updated.
     * @param userName the user name of the account which to be updated.
     * @param sshPublicKey the SSH public key that can be used for remote login to the compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String sshPublicKey) throws BatchErrorException, IOException {
        updateComputeNodeUser(poolId, nodeId, userName, sshPublicKey, (Iterable<BatchClientBehavior>)null);
    }

    /**
     * Updates the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be updated.
     * @param userName the user name of the account which to be updated.
     * @param sshPublicKey the SSH public key that can be used for remote login to the compute node.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String sshPublicKey, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        NodeUpdateUserParameter param = new NodeUpdateUserParameter();
        param.withSshPublicKey(sshPublicKey);

        updateComputeNodeUser(poolId, nodeId, userName, param, additionalBehaviors);
    }

    /**
     * Updates the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node where the user account will be updated.
     * @param userName the user name of the account which to be updated.
     * @param nodeUpdateUserParameter the parameter for update the user account.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    private void updateComputeNodeUser(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeUpdateUserOptions options = new ComputeNodeUpdateUserOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().computeNodes().updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, options);
    }

    /**
     * Gets the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node to get from the pool.
     * @return A {@link ComputeNode} containing information about the specified compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public ComputeNode getComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getComputeNode(poolId, nodeId, null, null);
    }

    /**
     * Gets the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node to get from the pool.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @return A {@link ComputeNode} containing information about the specified compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public ComputeNode getComputeNode(String poolId, String nodeId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getComputeNode(poolId, nodeId, detailLevel, null);
    }

    /**
     * Gets the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node to get from the pool.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link ComputeNode} containing information about the specified compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public ComputeNode getComputeNode(String poolId, String nodeId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetOptions options = new ComputeNodeGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<ComputeNode, ComputeNodeGetHeaders> response = this._parentBatchClient.protocolLayer().computeNodes().get(poolId, nodeId, options);

        return response.getBody();
    }

    /**
     * Reboots the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node to reboot.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void rebootComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        rebootComputeNode(poolId, nodeId, null, null);
    }

    /**
     * Reboots the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node to reboot.
     * @param nodeRebootOption specifies when to reboot the node and what to do with currently running tasks.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void rebootComputeNode(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption) throws BatchErrorException, IOException {
        rebootComputeNode(poolId, nodeId, nodeRebootOption, null);
    }

    /**
     * Reboots the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node to reboot.
     * @param nodeRebootOption specifies when to reboot the node and what to do with currently running tasks.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void rebootComputeNode(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeRebootOptions options = new ComputeNodeRebootOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().computeNodes().reboot(poolId, nodeId, nodeRebootOption, options);
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node to reimage.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void reimageComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        reimageComputeNode(poolId, nodeId, null, null);
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node to reimage.
     * @param nodeReimageOption Specifies when to reimage the node and what to do with currently running tasks.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void reimageComputeNode(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption) throws BatchErrorException, IOException {
        reimageComputeNode(poolId, nodeId, nodeReimageOption, null);
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node to reimage.
     * @param nodeReimageOption specifies when to reimage the node and what to do with currently running tasks.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void reimageComputeNode(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeReimageOptions options = new ComputeNodeReimageOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().computeNodes().reimage(poolId, nodeId, nodeReimageOption, options);
    }

    /**
     * Disables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableComputeNodeScheduling(String poolId, String nodeId) throws BatchErrorException, IOException {
        disableComputeNodeScheduling(poolId, nodeId, null, null);
    }

    /**
     * Disables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node.
     * @param nodeDisableSchedulingOption specifies what to do with currently running tasks.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableComputeNodeScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption) throws BatchErrorException, IOException {
        disableComputeNodeScheduling(poolId, nodeId, nodeDisableSchedulingOption, null);
    }

    /**
     * Disables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node.
     * @param nodeDisableSchedulingOption specifies what to do with currently running tasks.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableComputeNodeScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeDisableSchedulingOptions options = new ComputeNodeDisableSchedulingOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().computeNodes().disableScheduling(poolId, nodeId, nodeDisableSchedulingOption, options);
    }

    /**
     * Enables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableComputeNodeScheduling(String poolId, String nodeId) throws BatchErrorException, IOException {
        enableComputeNodeScheduling(poolId, nodeId, null);
    }

    /**
     * Enables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableComputeNodeScheduling(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeEnableSchedulingOptions options = new ComputeNodeEnableSchedulingOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().computeNodes().enableScheduling(poolId, nodeId, options);
    }

    /**
     * Gets a Remote Desktop Protocol (RDP) file for the specified node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node for which to get a Remote Desktop file.
     * @return The RDP file contents
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public String getComputeNodeRemoteDesktop(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getComputeNodeRemoteDesktop(poolId, nodeId, null);
    }

    /**
     * Gets a Remote Desktop Protocol (RDP) file for the specified node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node for which to get a Remote Desktop file.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The RDP file contents
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public String getComputeNodeRemoteDesktop(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetRemoteDesktopOptions options = new ComputeNodeGetRemoteDesktopOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeaders> response = this._parentBatchClient.protocolLayer().computeNodes().getRemoteDesktop(poolId, nodeId, options);

        if (response.getBody() != null) {
            return CharStreams.toString(new InputStreamReader(response.getBody(), "UTF-8"));
        }
        else {
            return null;
        }
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node for which to get a remote login settings.
     * @return The settings of the specified compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public ComputeNodeGetRemoteLoginSettingsResult getRemoteLoginSettings(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getRemoteLoginSettings(poolId, nodeId, null);
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node for which to get a remote login settings.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The settings of the specified compute node.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public ComputeNodeGetRemoteLoginSettingsResult getRemoteLoginSettings(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetRemoteLoginSettingsOptions options = new ComputeNodeGetRemoteLoginSettingsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResult, ComputeNodeGetRemoteLoginSettingsHeaders> response = this._parentBatchClient.protocolLayer().computeNodes().getRemoteLoginSettings(poolId, nodeId, options);

        return response.getBody();
    }

    /**
     * Enumerates the {@link ComputeNode compute nodes} of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @return A collection of {@link ComputeNode compute nodes}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<ComputeNode> listComputeNodes(String poolId) throws BatchErrorException, IOException {
        return listComputeNodes(poolId, null, null);
    }

    /**
     * Enumerates the {@link ComputeNode compute nodes} of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @return A collection of {@link ComputeNode compute nodes}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<ComputeNode> listComputeNodes(String poolId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listComputeNodes(poolId, detailLevel, null);
    }

    /**
     * Enumerates the {@link ComputeNode compute nodes} of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link ComputeNode compute nodes}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<ComputeNode> listComputeNodes(String poolId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeListOptions options = new ComputeNodeListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<ComputeNode>, ComputeNodeListHeaders> response = this._parentBatchClient.protocolLayer().computeNodes().list(poolId, options);

        return response.getBody();
    }

}
