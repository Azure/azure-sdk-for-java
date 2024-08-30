// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.ComputeNode;
import com.microsoft.azure.batch.protocol.models.ComputeNodeAddUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDeleteUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDisableSchedulingOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeEnableSchedulingOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsResult;
import com.microsoft.azure.batch.protocol.models.ComputeNodeListOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootOption;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageOption;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUpdateUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUploadBatchServiceLogsOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUser;
import com.microsoft.azure.batch.protocol.models.DisableComputeNodeSchedulingOption;
import com.microsoft.azure.batch.protocol.models.NodeUpdateUserParameter;
import com.microsoft.azure.batch.protocol.models.UploadBatchServiceLogsConfiguration;
import com.microsoft.azure.batch.protocol.models.UploadBatchServiceLogsResult;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Collection;

/**
 * Performs compute node-related operations on an Azure Batch account.
 */
public class ComputeNodeOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> customBehaviors;

    private BatchClient parentBatchClient;

    ComputeNodeOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.inheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @return A collection of {@link BatchClientBehavior} instances.
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        customBehaviors = behaviors;
        return this;
    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be created.
     * @param user The user account to be created.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void addComputeNodeUser(String poolId, String nodeId, ComputeNodeUser user) throws BatchErrorException, IOException {
        addComputeNodeUser(poolId, nodeId, user, null);
    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be created.
     * @param user The user account to be created.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void addComputeNodeUser(String poolId, String nodeId, ComputeNodeUser user, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeAddUserOptions options = new ComputeNodeAddUserOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().computeNodes().addUser(poolId, nodeId, user, options);
    }

    /**
     * Deletes the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be deleted.
     * @param userName The name of the user account to be deleted.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteComputeNodeUser(String poolId, String nodeId, String userName) throws BatchErrorException, IOException {
        deleteComputeNodeUser(poolId, nodeId, userName, null);
    }

    /**
     * Deletes the specified user account from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be deleted.
     * @param userName The name of the user account to be deleted.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteComputeNodeUser(String poolId, String nodeId, String userName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeDeleteUserOptions options = new ComputeNodeDeleteUserOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().computeNodes().deleteUser(poolId, nodeId, userName, options);
    }

    /**
     * Updates the specified user account on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be updated.
     * @param userName The name of the user account to update.
     * @param password The password of the account. If null, the password is removed.
     * @param expiryTime The time at which the account should expire. If null, the expiry time is replaced with its default value.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String password, DateTime expiryTime) throws BatchErrorException, IOException {
        updateComputeNodeUser(poolId, nodeId, userName, password, expiryTime, null);
    }

    /**
     * Updates the specified user account on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be updated.
     * @param userName The name of the user account to update.
     * @param password The password of the account. If null, the password is removed.
     * @param expiryTime The time at which the account should expire. If null, the expiry time is replaced with its default value.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String password, DateTime expiryTime, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        NodeUpdateUserParameter param = new NodeUpdateUserParameter();
        param.withPassword(password);
        param.withExpiryTime(expiryTime);

        updateComputeNodeUser(poolId, nodeId, userName, param, additionalBehaviors);
    }

    /**
     * Updates the specified user account on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be updated.
     * @param userName The name of the user account to update.
     * @param sshPublicKey The SSH public key that can be used for remote login to the compute node. If null, the SSH public key is removed.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String sshPublicKey) throws BatchErrorException, IOException {
        updateComputeNodeUser(poolId, nodeId, userName, sshPublicKey, (Iterable<BatchClientBehavior>) null);
    }

    /**
      * Updates the specified user account on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be updated.
     * @param userName The name of the user account to update.
     * @param sshPublicKey The SSH public key that can be used for remote login to the compute node. If null, the SSH public key is removed.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String sshPublicKey, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        NodeUpdateUserParameter param = new NodeUpdateUserParameter();
        param.withSshPublicKey(sshPublicKey);

        updateComputeNodeUser(poolId, nodeId, userName, param, additionalBehaviors);
    }

    /**
      * Updates the specified user account on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node where the user account will be updated.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The set of changes to be made to the user account.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    private void updateComputeNodeUser(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeUpdateUserOptions options = new ComputeNodeUpdateUserOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().computeNodes().updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, options);
    }

    /**
     * Gets the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node to get from the pool.
     * @return A {@link ComputeNode} containing information about the specified compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public ComputeNode getComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getComputeNode(poolId, nodeId, null, null);
    }

    /**
     * Gets the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId The ID of the compute node to get from the pool.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @return A {@link ComputeNode} containing information about the specified compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public ComputeNode getComputeNode(String poolId, String nodeId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getComputeNode(poolId, nodeId, detailLevel, null);
    }

    /**
     * Gets the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId The ID of the compute node to get from the pool.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link ComputeNode} containing information about the specified compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public ComputeNode getComputeNode(String poolId, String nodeId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetOptions options = new ComputeNodeGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().computeNodes().get(poolId, nodeId, options);
    }

    /**
     * Reboots the specified compute node.
     * <p>You can reboot a compute node only when it is in the {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#IDLE Idle} or {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#RUNNING Running} state.</p>
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node to reboot.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void rebootComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        rebootComputeNode(poolId, nodeId, null, null);
    }

    /**
     * Reboots the specified compute node.
     * <p>You can reboot a compute node only when it is in the {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#IDLE Idle} or {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#RUNNING Running} state.</p>
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node to reboot.
     * @param nodeRebootOption Specifies when to reboot the node and what to do with currently running tasks.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void rebootComputeNode(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption) throws BatchErrorException, IOException {
        rebootComputeNode(poolId, nodeId, nodeRebootOption, null);
    }

    /**
     * Reboots the specified compute node.
     * <p>You can reboot a compute node only when it is in the {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#IDLE Idle} or {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#RUNNING Running} state.</p>
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node to reboot.
     * @param nodeRebootOption Specifies when to reboot the node and what to do with currently running tasks.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void rebootComputeNode(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeRebootOptions options = new ComputeNodeRebootOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().computeNodes().reboot(poolId, nodeId, nodeRebootOption, options);
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     * <p>You can reimage a compute node only when it is in the {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#IDLE Idle} or {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#RUNNING Running} state.</p>
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node to reimage.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void reimageComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        reimageComputeNode(poolId, nodeId, null, null);
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     * <p>You can reimage a compute node only when it is in the {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#IDLE Idle} or {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#RUNNING Running} state.</p>
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node to reimage.
     * @param nodeReimageOption Specifies when to reimage the node and what to do with currently running tasks.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void reimageComputeNode(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption) throws BatchErrorException, IOException {
        reimageComputeNode(poolId, nodeId, nodeReimageOption, null);
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     * <p>You can reimage a compute node only when it is in the {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#IDLE Idle} or {@link com.microsoft.azure.batch.protocol.models.ComputeNodeState#RUNNING Running} state.</p>
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node to reimage.
     * @param nodeReimageOption Specifies when to reimage the node and what to do with currently running tasks.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void reimageComputeNode(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeReimageOptions options = new ComputeNodeReimageOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().computeNodes().reimage(poolId, nodeId, nodeReimageOption, options);
    }

    /**
     * Disables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId the ID of the compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void disableComputeNodeScheduling(String poolId, String nodeId) throws BatchErrorException, IOException {
        disableComputeNodeScheduling(poolId, nodeId, null, null);
    }

    /**
     * Disables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId The ID of the compute node.
     * @param nodeDisableSchedulingOption Specifies what to do with currently running tasks.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void disableComputeNodeScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption) throws BatchErrorException, IOException {
        disableComputeNodeScheduling(poolId, nodeId, nodeDisableSchedulingOption, null);
    }

    /**
     * Disables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId The ID of the compute node.
     * @param nodeDisableSchedulingOption Specifies what to do with currently running tasks.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void disableComputeNodeScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeDisableSchedulingOptions options = new ComputeNodeDisableSchedulingOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().computeNodes().disableScheduling(poolId, nodeId, nodeDisableSchedulingOption, options);
    }

    /**
     * Enables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId The ID of the compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void enableComputeNodeScheduling(String poolId, String nodeId) throws BatchErrorException, IOException {
        enableComputeNodeScheduling(poolId, nodeId, null);
    }

    /**
     * Enables task scheduling on the specified compute node.
     *
     * @param poolId The ID of the pool.
     * @param nodeId The ID of the compute node.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void enableComputeNodeScheduling(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeEnableSchedulingOptions options = new ComputeNodeEnableSchedulingOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().computeNodes().enableScheduling(poolId, nodeId, options);
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node for which to get a remote login settings.
     * @return The remote settings for the specified compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public ComputeNodeGetRemoteLoginSettingsResult getComputeNodeRemoteLoginSettings(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getComputeNodeRemoteLoginSettings(poolId, nodeId, null);
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node for which to get a remote login settings.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The remote login settings for the specified compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public ComputeNodeGetRemoteLoginSettingsResult getComputeNodeRemoteLoginSettings(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetRemoteLoginSettingsOptions options = new ComputeNodeGetRemoteLoginSettingsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().computeNodes().getRemoteLoginSettings(poolId, nodeId, options);
    }

    /**
     * Lists the {@link ComputeNode compute nodes} of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @return A list of {@link ComputeNode} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ComputeNode> listComputeNodes(String poolId) throws BatchErrorException, IOException {
        return listComputeNodes(poolId, null, null);
    }

    /**
     * Lists the {@link ComputeNode compute nodes} of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link ComputeNode} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ComputeNode> listComputeNodes(String poolId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listComputeNodes(poolId, detailLevel, null);
    }

    /**
     * Lists the {@link ComputeNode compute nodes} of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link ComputeNode} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ComputeNode> listComputeNodes(String poolId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeListOptions options = new ComputeNodeListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().computeNodes().list(poolId, options);
    }

    /**
     * Upload Azure Batch service log files from the specified compute node to Azure Blob Storage.
     * This is for gathering Azure Batch service log files in an automated fashion from nodes if you are experiencing an error and wish to escalate to Azure support. The Azure Batch service log files should be shared with Azure support to aid in debugging issues with the Batch service.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node from which you want to upload the Azure Batch service log files.
     * @param containerUrl The URL of the container within Azure Blob Storage to which to upload the Batch Service log file(s).
     * @param startTime The start of the time range from which to upload Batch Service log file(s).
     * @return The result of uploading Batch service log files from a specific compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public UploadBatchServiceLogsResult uploadBatchServiceLogs(String poolId, String nodeId, String containerUrl, DateTime startTime) throws BatchErrorException, IOException {
        return uploadBatchServiceLogs(poolId, nodeId, containerUrl, startTime, null, null);

    }

    /**
     * Upload Azure Batch service log files from the specified compute node to Azure Blob Storage.
     * This is for gathering Azure Batch service log files in an automated fashion from nodes if you are experiencing an error and wish to escalate to Azure support. The Azure Batch service log files should be shared with Azure support to aid in debugging issues with the Batch service.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node from which you want to upload the Azure Batch service log files.
     * @param containerUrl The URL of the container within Azure Blob Storage to which to upload the Batch Service log file(s).
     * @param startTime The start of the time range from which to upload Batch Service log file(s).
     * @param endTime The end of the time range from which to upload Batch Service log file(s).
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The result of uploading Batch service log files from a specific compute node.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public UploadBatchServiceLogsResult uploadBatchServiceLogs(String poolId, String nodeId, String containerUrl, DateTime startTime, DateTime endTime, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        UploadBatchServiceLogsConfiguration configuration = new UploadBatchServiceLogsConfiguration();
        configuration.withContainerUrl(containerUrl);
        configuration.withStartTime(startTime);
        configuration.withEndTime(endTime);

        ComputeNodeUploadBatchServiceLogsOptions options = new ComputeNodeUploadBatchServiceLogsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().computeNodes().uploadBatchServiceLogs(poolId, nodeId, configuration, options);
    }
}
