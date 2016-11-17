/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.rest.ServiceResponseWithHeaders;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs pool related operations on an Azure Batch account.
 */
public class PoolOperations implements IInheritedBehaviors {
    PoolOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

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
     * Enumerates the {@link CloudPool pools} in the Batch account.
     *
     * @return A collection of {@link CloudPool pools}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudPool> listPools() throws BatchErrorException, IOException {
        return listPools(null, null);
    }

    /**
     * Enumerates the {@link CloudPool pools} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link CloudPool pools}.     *
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudPool> listPools(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPools(detailLevel, null);
    }

    /**
     * Enumerates the {@link CloudPool pools} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link CloudPool pools}.     *
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudPool> listPools(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolListOptions options = new PoolListOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudPool>, PoolListHeaders> response = this._parentBatchClient.protocolLayer().pools().list(options);

        return response.getBody();
    }

    /**
     * Gets the specified {@link CloudPool}.
     *
     * @param poolId The ID of the pool to get.
     * @return A {@link CloudPool} containing information about the specified Azure Batch pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudPool getPool(String poolId) throws BatchErrorException, IOException {
        return getPool(poolId, null, null);
    }

    /**
     * Gets the specified {@link CloudPool}.
     *
     * @param poolId The ID of the pool to get.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A {@link CloudPool} containing information about the specified Azure Batch pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudPool getPool(String poolId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getPool(poolId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudPool}.
     *
     * @param poolId The ID of the pool to get.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link CloudPool} containing information about the specified Azure Batch pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudPool getPool(String poolId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolGetOptions options = new PoolGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudPool, PoolGetHeaders> response = this._parentBatchClient.protocolLayer().pools().get(poolId, options);

        return response.getBody();
    }

    /**
     * Deletes the specified pool.
     *
     * @param poolId The ID of the pool to delete.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deletePool(String poolId) throws BatchErrorException, IOException {
        deletePool(poolId, null);
    }

    /**
     * Deletes the specified pool.
     *
     * @param poolId The ID of the pool to delete.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deletePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolDeleteOptions options = new PoolDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().delete(poolId, options);
    }

    /**
     * Add a specified pool to the Batch account.
     *
     * @param poolId The ID of the pool.
     * @param virtualMachineSize The size of virtual machines in the pool.  See https://azure.microsoft.com/en-us/documentation/articles/virtual-machines-size-specs/ for sizes.
     * @param cloudServiceConfiguration The {@link CloudServiceConfiguration} for the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createPool(String poolId, String virtualMachineSize, CloudServiceConfiguration cloudServiceConfiguration, int targetDedicated) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, cloudServiceConfiguration, targetDedicated, null);
    }

    /**
     * Add a specified pool to the Batch account.
     *
     * @param poolId The ID of the pool.
     * @param virtualMachineSize The size of virtual machines in the pool.  See https://azure.microsoft.com/en-us/documentation/articles/virtual-machines-size-specs/ for sizes.
     * @param cloudServiceConfiguration The {@link CloudServiceConfiguration} for the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createPool(String poolId, String virtualMachineSize, CloudServiceConfiguration cloudServiceConfiguration, Integer targetDedicated, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolAddParameter parameter = new PoolAddParameter()
                .withId(poolId)
                .withCloudServiceConfiguration(cloudServiceConfiguration)
                .withTargetDedicated(targetDedicated)
                .withVmSize(virtualMachineSize);

        createPool(parameter, additionalBehaviors);
    }

    /**
     * Add a specified pool to the Batch account.
     *
     * @param poolId The ID of the pool.
     * @param virtualMachineSize The size of virtual machines in the pool.  See https://azure.microsoft.com/en-us/documentation/articles/virtual-machines-size-specs/ for sizes.
     * @param virtualMachineConfiguration The {@link VirtualMachineConfiguration} for the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createPool(String poolId, String virtualMachineSize, VirtualMachineConfiguration virtualMachineConfiguration, int targetDedicated) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, virtualMachineConfiguration, targetDedicated, null);
    }

    /**
     * Add a specified pool to the Batch account.
     *
     * @param poolId The ID of the pool.
     * @param virtualMachineSize The size of virtual machines in the pool.  See https://azure.microsoft.com/en-us/documentation/articles/virtual-machines-size-specs/ for sizes.
     * @param virtualMachineConfiguration The {@link VirtualMachineConfiguration} for the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createPool(String poolId, String virtualMachineSize, VirtualMachineConfiguration virtualMachineConfiguration, Integer targetDedicated, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolAddParameter parameter = new PoolAddParameter()
                .withId(poolId)
                .withVirtualMachineConfiguration(virtualMachineConfiguration)
                .withTargetDedicated(targetDedicated)
                .withVmSize(virtualMachineSize);

        createPool(parameter, additionalBehaviors);
    }

    /**
     * Add a specified pool to the Batch account.
     *
     * @param parameter The parameter to create the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createPool(PoolAddParameter parameter) throws BatchErrorException, IOException {
        createPool(parameter, null);
    }

    /**
     * Add a specified pool to the Batch account.
     *
     * @param parameter The parameter to create the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createPool(PoolAddParameter parameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolAddOptions options = new PoolAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().add(parameter, options);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void resizePool(String poolId, int targetDedicated) throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicated, null, null, null);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @param resizeTimeout The timeout for allocation of compute nodes to the pool or removal of compute nodes from the pool. If the pool has not reached the target size after this time, the resize is stopped.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void resizePool(String poolId, int targetDedicated, Period resizeTimeout) throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicated, resizeTimeout, null, null);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @param resizeTimeout The timeout for allocation of compute nodes to the pool or removal of compute nodes from the pool. If the pool has not reached the target size after this time, the resize is stopped.
     * @param deallocationOption Specifies when nodes may be removed from the pool, if the pool size is decreasing.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void resizePool(String poolId, int targetDedicated, Period resizeTimeout, ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicated, resizeTimeout, deallocationOption, null);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param targetDedicated The desired number of compute nodes in the pool.
     * @param resizeTimeout The timeout for allocation of compute nodes to the pool or removal of compute nodes from the pool. If the pool has not reached the target size after this time, the resize is stopped.
     * @param deallocationOption Specifies when nodes may be removed from the pool, if the pool size is decreasing.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void resizePool(String poolId, int targetDedicated, Period resizeTimeout, ComputeNodeDeallocationOption deallocationOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolResizeOptions options = new PoolResizeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolResizeParameter param = new PoolResizeParameter()
                .withResizeTimeout(resizeTimeout)
                .withNodeDeallocationOption(deallocationOption)
                .withTargetDedicated(targetDedicated);

        this._parentBatchClient.protocolLayer().pools().resize(poolId, param, options);
    }

    /**
     * Stops a pool resize operation.
     *
     * @param poolId The ID of the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void stopResizePool(String poolId) throws BatchErrorException, IOException {
        stopResizePool(poolId, null);
    }

    /**
     * Stops a pool resize operation.
     *
     * @param poolId The ID of the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void stopResizePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolStopResizeOptions options = new PoolStopResizeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().stopResize(poolId, options);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId The ID of the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableAutoScale(String poolId) throws BatchErrorException, IOException {
        enableAutoScale(poolId, null, null, null);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param autoScaleFormula The formula for the desired number of compute nodes in the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException {
        enableAutoScale(poolId, autoScaleFormula, null, null);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param autoScaleFormula The formula for the desired number of compute nodes in the pool.
     * @param autoScaleEvaluationInterval the Time interval at which to automatically adjust the pool size according to the AutoScale formula.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableAutoScale(String poolId, String autoScaleFormula, Period autoScaleEvaluationInterval) throws BatchErrorException, IOException {
        enableAutoScale(poolId, autoScaleFormula, autoScaleEvaluationInterval, null);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param autoScaleFormula The formula for the desired number of compute nodes in the pool.
     * @param autoScaleEvaluationInterval The time interval at which to automatically adjust the pool size according to the AutoScale formula.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableAutoScale(String poolId, String autoScaleFormula, Period autoScaleEvaluationInterval, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolEnableAutoScaleOptions options = new PoolEnableAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolEnableAutoScaleParameter param = new PoolEnableAutoScaleParameter()
                .withAutoScaleFormula(autoScaleFormula)
                .withAutoScaleEvaluationInterval(autoScaleEvaluationInterval);

        this._parentBatchClient.protocolLayer().pools().enableAutoScale(poolId, param, options);
    }


    /**
     * Disables automatic scaling on the specified pool.
     *
     * @param poolId The ID of the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableAutoScale(String poolId) throws BatchErrorException, IOException {
        disableAutoScale(poolId, null);
    }

    /**
     * Disables automatic scaling on the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableAutoScale(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolDisableAutoScaleOptions options = new PoolDisableAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().disableAutoScale(poolId, options);
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the specified pool. This
     * is primarily for validating an autoscale formula, as it simply returns the result
     * without applying the formula to the pool.
     *
     * @param poolId The ID of the pool.
     * @param autoScaleFormula The formula to be evaluated on the pool.
     * @return The result of evaluating the formula on the specified pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public AutoScaleRun evaluateAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException {
        return evaluateAutoScale(poolId, autoScaleFormula, null);
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the specified pool. This
     * is primarily for validating an autoscale formula, as it simply returns the result
     * without applying the formula to the pool.
     *
     * @param poolId The ID of the pool.
     * @param autoScaleFormula The formula to be evaluated on the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The result of evaluating the formula on the specified pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public AutoScaleRun evaluateAutoScale(String poolId, String autoScaleFormula, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolEvaluateAutoScaleOptions options = new PoolEvaluateAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<AutoScaleRun, PoolEvaluateAutoScaleHeaders> response = this._parentBatchClient.protocolLayer().pools().evaluateAutoScale(poolId, autoScaleFormula, options);
        return response.getBody();
    }

    /**
     * Changes the operating system version of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param targetOSVersion The Azure Guest OS version to be installed on the virtual machines in the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void changeOSVersion(String poolId, String targetOSVersion) throws BatchErrorException, IOException {
        changeOSVersion(poolId, targetOSVersion, null);
    }

    /**
     * Changes the operating system version of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param targetOSVersion The Azure Guest OS version to be installed on the virtual machines in the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void changeOSVersion(String poolId, String targetOSVersion, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpgradeOSOptions options = new PoolUpgradeOSOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().upgradeOS(poolId, targetOSVersion, options);
    }

    /**
     * Removes the specified compute node from the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param computeNodeId The ID of the compute node to remove from the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void removeNodeFromPool(String poolId, String computeNodeId) throws BatchErrorException, IOException {
        removeNodeFromPool(poolId, computeNodeId, null, null, null);
    }

    /**
     * Removes the specified compute node from the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param computeNodeId The ID of the compute node to remove from the pool.
     * @param deallocationOption Specifies when nodes may be removed from the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void removeNodeFromPool(String poolId, String computeNodeId, ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        removeNodeFromPool(poolId, computeNodeId, deallocationOption, null, null);
    }

    /**
     * Removes the specified compute node from the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param computeNodeId The ID of the compute node to remove from the pool.
     * @param deallocationOption Specifies when nodes may be removed from the pool.
     * @param resizeTimeout Specifies the timeout for removal of compute nodes from the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void removeNodeFromPool(String poolId, String computeNodeId, ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        List<String> nodeIds = new LinkedList<>();
        nodeIds.add(computeNodeId);

        removeNodesFromPool(poolId, nodeIds, deallocationOption, resizeTimeout, additionalBehaviors);
    }

    /**
     * Removes the specified compute nodes from the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param computeNodes The ids of the compute nodes to remove from the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes) throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, null, null, null);
    }

    /**
     * Removes the specified compute nodes from the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param computeNodes The ids of the compute nodes to remove from the pool.
     * @param deallocationOption Specifies when nodes may be removed from the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes, ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, deallocationOption, null, null);
    }

    /**
     * Removes the specified compute nodes from the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param computeNodes The ids of the compute nodes to remove from the pool.
     * @param deallocationOption Specifies when nodes may be removed from the pool.
     * @param resizeTimeout Specifies the timeout for removal of compute nodes from the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes, ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        List<String> nodeIds = new LinkedList<>();
        for (ComputeNode node : computeNodes) {
            nodeIds.add(node.id());
        }

        removeNodesFromPool(poolId, nodeIds, deallocationOption, resizeTimeout, additionalBehaviors);
    }

    private void removeNodesFromPool(String poolId, List<String> computeNodesId, ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolRemoveNodesOptions options = new PoolRemoveNodesOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        NodeRemoveParameter param = new NodeRemoveParameter()
                .withNodeList(computeNodesId)
                .withNodeDeallocationOption(deallocationOption)
                .withResizeTimeout(resizeTimeout);

        this._parentBatchClient.protocolLayer().pools().removeNodes(poolId, param, options);
    }

    /**
     * Checks existence of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @return True if pool exists, otherwise, return false
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public boolean existsPool(String poolId) throws BatchErrorException, IOException {
        return existsPool(poolId, null);
    }

    /**
     * Checks existence of the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return True if pool exists, otherwise, return false
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public boolean existsPool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {

        PoolExistsOptions options = new PoolExistsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Boolean, PoolExistsHeaders> response = this._parentBatchClient.protocolLayer().pools().exists(poolId, options);

        return response.getBody();
    }

    /**
     * Updates the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param startTask A task to run on each compute node as it joins the pool. If omitted, any existing start task is removed from the pool.
     * @param certificateReferences A list of certificates to be installed on each compute node in the pool. If omitted, any existing certificate references are removed from the pool.
     * @param applicationPackageReferences a list of application packages to be installed on each compute node in the pool. If omitted, any existing application packages references are removed from the pool.
     * @param metadata A list of name-value pairs associated with the pool as metadata. If omitted, any existing metadata is removed from the pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updatePoolProperties(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences,
                                     Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata) throws BatchErrorException, IOException {
        updatePoolProperties(poolId, startTask, certificateReferences, applicationPackageReferences, metadata, null);
    }

    /**
     * Updates the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param startTask A task to run on each compute node as it joins the pool. If omitted, any existing start task is removed from the pool.
     * @param certificateReferences A list of certificates to be installed on each compute node in the pool. If omitted, any existing certificate references are removed from the pool.
     * @param applicationPackageReferences A list of application packages to be installed on each compute node in the pool. If omitted, any existing application packages references are removed from the pool.
     * @param metadata A list of name-value pairs associated with the pool as metadata. If omitted, any existing metadata is removed from the pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updatePoolProperties(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences,
                                     Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata,
                                     Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpdatePropertiesOptions options = new PoolUpdatePropertiesOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolUpdatePropertiesParameter param = new PoolUpdatePropertiesParameter()
                .withMetadata(metadata == null ?
                        new LinkedList<MetadataItem>() : new LinkedList<MetadataItem>(metadata))
                .withApplicationPackageReferences(applicationPackageReferences == null ?
                        new LinkedList<ApplicationPackageReference>() : new LinkedList<ApplicationPackageReference>(applicationPackageReferences))
                .withCertificateReferences(certificateReferences == null ?
                        new LinkedList<CertificateReference>() : new LinkedList<CertificateReference>(certificateReferences))
                .withStartTask(startTask);

        this._parentBatchClient.protocolLayer().pools().updateProperties(poolId, param, options);
    }

    /**
     * Updates the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param startTask A task to run on each compute node as it joins the pool. If omitted, any existing start task is left unchanged.
     * @param certificateReferences A list of certificates to be installed on each compute node in the pool. If omitted, any existing certificate references are left unchanged.
     * @param applicationPackageReferences A list of application packages to be installed on each compute node in the pool. If omitted, any existing application packages references are left unchanged.
     * @param metadata A list of name-value pairs associated with the pool as metadata. If omitted, any existing metadata is left unchanged.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences,
                          Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchPool(poolId, startTask, certificateReferences, applicationPackageReferences, metadata, null);
    }

    /**
     * Updates the specified pool.
     *
     * @param poolId The ID of the pool.
     * @param startTask A task to run on each compute node as it joins the pool. If omitted, any existing start task is left unchanged.
     * @param certificateReferences A list of certificates to be installed on each compute node in the pool. If omitted, any existing certificate references are left unchanged.
     * @param applicationPackageReferences A list of application packages to be installed on each compute node in the pool. If omitted, any existing application packages references are left unchanged.
     * @param metadata A list of name-value pairs associated with the pool as metadata. If omitted, any existing metadata is left unchanged.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences,
                          Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata,
                          Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolPatchOptions options = new PoolPatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolPatchParameter param = new PoolPatchParameter()
                .withStartTask(startTask);
        if (metadata != null) {
            param.withMetadata(new LinkedList<MetadataItem>(metadata));
        }
        if (applicationPackageReferences != null) {
            param.withApplicationPackageReferences(new LinkedList<ApplicationPackageReference>(applicationPackageReferences));
        }
        if (certificateReferences != null) {
            param.withCertificateReferences(new LinkedList<CertificateReference>(certificateReferences));
        }

        this._parentBatchClient.protocolLayer().pools().patch(poolId, param, options);
    }

    /**
     * Enumerates pool usage metrics.
     *
     * @param startTime The start time of the aggregation interval covered by this entry.
     * @param endTime The end time of the aggregation interval for this entry.
     * @return A collection of {@link PoolUsageMetrics pool usage metrics}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime) throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, null, null);
    }

    /**
     * Enumerates pool usage metrics.
     *
     * @param startTime The start time of the aggregation interval covered by this entry.
     * @param endTime The end time of the aggregation interval for this entry.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link PoolUsageMetrics pool usage metrics}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, detailLevel, null);
    }

    /**
     * Enumerates pool usage metrics.
     *
     * @param startTime The start time of the aggregation interval covered by this entry.
     * @param endTime The end time of the aggregation interval for this entry.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link PoolUsageMetrics pool usage metrics}.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime, DetailLevel detailLevel,
                                                       Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolListPoolUsageMetricsOptions options = new PoolListPoolUsageMetricsOptions()
                .withStartTime(startTime)
                .withEndTime(endTime);
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<PoolUsageMetrics>, PoolListPoolUsageMetricsHeaders> response = this._parentBatchClient.protocolLayer().pools().listPoolUsageMetrics(options);

        return response.getBody();
    }

    /**
     * Gets lifetime summary statistics for all of the pools in the current account.
     * Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @return The aggregated pool statistics.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public PoolStatistics getAllPoolsLifetimeStatistics() throws BatchErrorException, IOException {
        return getAllPoolsLifetimeStatistics(null);
    }

    /**
     * Gets lifetime summary statistics for all of the pools in the current account.
     * Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The aggregated pool statistics.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public PoolStatistics getAllPoolsLifetimeStatistics(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolGetAllPoolsLifetimeStatisticsOptions options = new PoolGetAllPoolsLifetimeStatisticsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PoolStatistics, PoolGetAllPoolsLifetimeStatisticsHeaders> response = this._parentBatchClient.protocolLayer().pools().getAllPoolsLifetimeStatistics(options);

        return response.getBody();
    }
}
