// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.*;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs pool-related operations on an Azure Batch account.
 */
public class PoolOperations implements IInheritedBehaviors {
    PoolOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.inheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> customBehaviors;

    private final BatchClient parentBatchClient;

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch
     * service.
     *
     * @return A collection of {@link BatchClientBehavior} instances.
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch
     * service.
     *
     * @param behaviors
     *            The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        customBehaviors = behaviors;
        return this;
    }

    /**
     * Lists the {@link CloudPool pools} in the Batch account.
     *
     * @return A list of {@link CloudPool} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<CloudPool> listPools() throws BatchErrorException, IOException {
        return listPools(null, null);
    }

    /**
     * Lists the {@link CloudPool pools} in the Batch account.
     *
     * @param detailLevel
     *            A {@link DetailLevel} used for filtering the list and for
     *            controlling which properties are retrieved from the service.
     * @return A list of {@link CloudPool} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<CloudPool> listPools(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPools(detailLevel, null);
    }

    /**
     * Lists the {@link CloudPool pools} in the Batch account.
     *
     * @param detailLevel
     *            A {@link DetailLevel} used for filtering the list and for
     *            controlling which properties are retrieved from the service.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return A list of {@link CloudPool} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<CloudPool> listPools(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolListOptions options = new PoolListOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().pools().list(options);
    }

    /**
     * Gets the specified {@link CloudPool}.
     *
     * @param poolId
     *            The ID of the pool to get.
     * @return A {@link CloudPool} containing information about the specified Azure
     *         Batch pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public CloudPool getPool(String poolId) throws BatchErrorException, IOException {
        return getPool(poolId, null, null);
    }

    /**
     * Gets the specified {@link CloudPool}.
     *
     * @param poolId
     *            The ID of the pool to get.
     * @param detailLevel
     *            A {@link DetailLevel} used for controlling which properties are
     *            retrieved from the service.
     * @return A {@link CloudPool} containing information about the specified Azure
     *         Batch pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public CloudPool getPool(String poolId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getPool(poolId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudPool}.
     *
     * @param poolId
     *            The ID of the pool to get.
     * @param detailLevel
     *            A {@link DetailLevel} used for controlling which properties are
     *            retrieved from the service.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return A {@link CloudPool} containing information about the specified Azure
     *         Batch pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public CloudPool getPool(String poolId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolGetOptions options = new PoolGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().pools().get(poolId, options);
    }

    /**
     * Deletes the specified pool.
     *
     * @param poolId
     *            The ID of the pool to delete.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void deletePool(String poolId) throws BatchErrorException, IOException {
        deletePool(poolId, null);
    }

    /**
     * Deletes the specified pool.
     *
     * @param poolId
     *            The ID of the pool to delete.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void deletePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolDeleteOptions options = new PoolDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().pools().delete(poolId, options);
    }

    /**
     * Adds a pool to the Batch account.
     *
     * @param poolId
     *            The ID of the pool.
     * @param virtualMachineSize
     *            The size of virtual machines in the pool. See <a href=
     *            "https://azure.microsoft.com/documentation/articles/virtual-machines-size-specs/">https://azure.microsoft.com/documentation/articles/virtual-machines-size-specs/</a>
     *            for sizes.
     * @param virtualMachineConfiguration
     *            The {@link VirtualMachineConfiguration} for the pool.
     * @param targetDedicatedNodes
     *            The desired number of dedicated compute nodes in the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void createPool(String poolId, String virtualMachineSize,
                           VirtualMachineConfiguration virtualMachineConfiguration, int targetDedicatedNodes)
            throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, virtualMachineConfiguration, targetDedicatedNodes, 0, null);
    }

    /**
     * Adds a pool to the Batch account.
     *
     * @param poolId
     *            The ID of the pool.
     * @param virtualMachineSize
     *            The size of virtual machines in the pool. See <a href=
     *            "https://azure.microsoft.com/documentation/articles/virtual-machines-size-specs/">https://azure.microsoft.com/documentation/articles/virtual-machines-size-specs/</a>
     *            for sizes.
     * @param virtualMachineConfiguration
     *            The {@link VirtualMachineConfiguration} for the pool.
     * @param targetDedicatedNodes
     *            The desired number of dedicated compute nodes in the pool.
     * @param targetLowPriorityNodes
     *            The desired number of low-priority compute nodes in the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void createPool(String poolId, String virtualMachineSize,
            VirtualMachineConfiguration virtualMachineConfiguration, int targetDedicatedNodes,
            int targetLowPriorityNodes) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, virtualMachineConfiguration, targetDedicatedNodes,
                targetLowPriorityNodes, null);
    }

    /**
     * Adds a pool to the Batch account.
     *
     * @param poolId
     *            The ID of the pool.
     * @param virtualMachineSize
     *            The size of virtual machines in the pool. See <a href=
     *            "https://azure.microsoft.com/documentation/articles/virtual-machines-size-specs/">https://azure.microsoft.com/documentation/articles/virtual-machines-size-specs/</a>
     *            for sizes.
     * @param virtualMachineConfiguration
     *            The {@link VirtualMachineConfiguration} for the pool.
     * @param targetDedicatedNodes
     *            The desired number of dedicated compute nodes in the pool.
     * @param targetLowPriorityNodes
     *            The desired number of low-priority compute nodes in the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void createPool(String poolId, String virtualMachineSize,
            VirtualMachineConfiguration virtualMachineConfiguration, int targetDedicatedNodes,
            int targetLowPriorityNodes, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolAddParameter parameter = new PoolAddParameter().withId(poolId)
                .withVirtualMachineConfiguration(virtualMachineConfiguration)
                .withTargetDedicatedNodes(targetDedicatedNodes).withTargetLowPriorityNodes(targetLowPriorityNodes)
                .withVmSize(virtualMachineSize);

        createPool(parameter, additionalBehaviors);
    }

    /**
     * Adds a pool to the Batch account.
     *
     * @param pool
     *            The pool to be added.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void createPool(PoolAddParameter pool) throws BatchErrorException, IOException {
        createPool(pool, null);
    }

    /**
     * Adds a pool to the Batch account.
     *
     * @param pool
     *            The pool to be added.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void createPool(PoolAddParameter pool, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolAddOptions options = new PoolAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().pools().add(pool, options);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param targetDedicatedNodes
     *            The desired number of dedicated compute nodes in the pool.
     * @param targetLowPriorityNodes
     *            The desired number of low-priority compute nodes in the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void resizePool(String poolId, Integer targetDedicatedNodes, Integer targetLowPriorityNodes)
            throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicatedNodes, targetLowPriorityNodes, null, null, null);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param targetDedicatedNodes
     *            The desired number of dedicated compute nodes in the pool.
     * @param targetLowPriorityNodes
     *            The desired number of low-priority compute nodes in the pool.
     * @param resizeTimeout
     *            The timeout for allocation of compute nodes to the pool or removal
     *            of compute nodes from the pool. If the pool has not reached the
     *            target size after this time, the resize is stopped.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void resizePool(String poolId, Integer targetDedicatedNodes, Integer targetLowPriorityNodes,
            Period resizeTimeout) throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicatedNodes, targetLowPriorityNodes, resizeTimeout, null, null);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param targetDedicatedNodes
     *            The desired number of dedicated compute nodes in the pool.
     * @param targetLowPriorityNodes
     *            The desired number of low-priority compute nodes in the pool.
     * @param resizeTimeout
     *            The timeout for allocation of compute nodes to the pool or removal
     *            of compute nodes from the pool. If the pool has not reached the
     *            target size after this time, the resize is stopped.
     * @param deallocationOption
     *            Specifies when nodes may be removed from the pool, if the pool
     *            size is decreasing.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void resizePool(String poolId, Integer targetDedicatedNodes, Integer targetLowPriorityNodes,
            Period resizeTimeout, ComputeNodeDeallocationOption deallocationOption)
            throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicatedNodes, targetLowPriorityNodes, resizeTimeout, deallocationOption, null);
    }

    /**
     * Resizes the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param targetDedicatedNodes
     *            The desired number of dedicated compute nodes in the pool.
     * @param targetLowPriorityNodes
     *            The desired number of low-priority compute nodes in the pool.
     * @param resizeTimeout
     *            The timeout for allocation of compute nodes to the pool or removal
     *            of compute nodes from the pool. If the pool has not reached the
     *            target size after this time, the resize is stopped.
     * @param deallocationOption
     *            Specifies when nodes may be removed from the pool, if the pool
     *            size is decreasing.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void resizePool(String poolId, Integer targetDedicatedNodes, Integer targetLowPriorityNodes,
            Period resizeTimeout, ComputeNodeDeallocationOption deallocationOption,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolResizeOptions options = new PoolResizeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolResizeParameter param = new PoolResizeParameter().withResizeTimeout(resizeTimeout)
                .withNodeDeallocationOption(deallocationOption).withTargetDedicatedNodes(targetDedicatedNodes)
                .withTargetLowPriorityNodes(targetLowPriorityNodes);

        this.parentBatchClient.protocolLayer().pools().resize(poolId, param, options);
    }

    /**
     * Stops a pool resize operation.
     *
     * @param poolId
     *            The ID of the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void stopResizePool(String poolId) throws BatchErrorException, IOException {
        stopResizePool(poolId, null);
    }

    /**
     * Stops a pool resize operation.
     *
     * @param poolId
     *            The ID of the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void stopResizePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolStopResizeOptions options = new PoolStopResizeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().pools().stopResize(poolId, options);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void enableAutoScale(String poolId) throws BatchErrorException, IOException {
        enableAutoScale(poolId, null, null, null);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param autoScaleFormula
     *            The formula for the desired number of compute nodes in the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void enableAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException {
        enableAutoScale(poolId, autoScaleFormula, null, null);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param autoScaleFormula
     *            The formula for the desired number of compute nodes in the pool.
     * @param autoScaleEvaluationInterval
     *            The time interval at which to automatically adjust the pool size.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void enableAutoScale(String poolId, String autoScaleFormula, Period autoScaleEvaluationInterval)
            throws BatchErrorException, IOException {
        enableAutoScale(poolId, autoScaleFormula, autoScaleEvaluationInterval, null);
    }

    /**
     * Enables automatic scaling on the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param autoScaleFormula
     *            The formula for the desired number of compute nodes in the pool.
     * @param autoScaleEvaluationInterval
     *            The time interval at which to automatically adjust the pool size.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void enableAutoScale(String poolId, String autoScaleFormula, Period autoScaleEvaluationInterval,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolEnableAutoScaleOptions options = new PoolEnableAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolEnableAutoScaleParameter param = new PoolEnableAutoScaleParameter().withAutoScaleFormula(autoScaleFormula)
                .withAutoScaleEvaluationInterval(autoScaleEvaluationInterval);

        this.parentBatchClient.protocolLayer().pools().enableAutoScale(poolId, param, options);
    }

    /**
     * Disables automatic scaling on the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void disableAutoScale(String poolId) throws BatchErrorException, IOException {
        disableAutoScale(poolId, null);
    }

    /**
     * Disables automatic scaling on the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void disableAutoScale(String poolId, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolDisableAutoScaleOptions options = new PoolDisableAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().pools().disableAutoScale(poolId, options);
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the specified
     * pool. This is primarily for validating an autoscale formula, as it simply
     * returns the result without applying the formula to the pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param autoScaleFormula
     *            The formula to be evaluated on the pool.
     * @return The result of evaluating the formula on the specified pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public AutoScaleRun evaluateAutoScale(String poolId, String autoScaleFormula)
            throws BatchErrorException, IOException {
        return evaluateAutoScale(poolId, autoScaleFormula, null);
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the specified
     * pool. This is primarily for validating an autoscale formula, as it simply
     * returns the result without applying the formula to the pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param autoScaleFormula
     *            The formula to be evaluated on the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return The result of evaluating the formula on the specified pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public AutoScaleRun evaluateAutoScale(String poolId, String autoScaleFormula,
                                          Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolEvaluateAutoScaleOptions options = new PoolEvaluateAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().pools().evaluateAutoScale(poolId, autoScaleFormula, options);
    }

    /**
     * Removes the specified compute node from the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param computeNodeId
     *            The ID of the compute node to remove from the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void removeNodeFromPool(String poolId, String computeNodeId) throws BatchErrorException, IOException {
        removeNodeFromPool(poolId, computeNodeId, null, null, null);
    }

    /**
     * Removes the specified compute node from the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param computeNodeId
     *            The ID of the compute node to remove from the pool.
     * @param deallocationOption
     *            Specifies when nodes may be removed from the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void removeNodeFromPool(String poolId, String computeNodeId,
            ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        removeNodeFromPool(poolId, computeNodeId, deallocationOption, null, null);
    }

    /**
     * Removes the specified compute node from the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param computeNodeId
     *            The ID of the compute node to remove from the pool.
     * @param deallocationOption
     *            Specifies when nodes may be removed from the pool.
     * @param resizeTimeout
     *            Specifies the timeout for removal of compute nodes from the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void removeNodeFromPool(String poolId, String computeNodeId,
            ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        List<String> nodeIds = new LinkedList<>();
        nodeIds.add(computeNodeId);

        removeNodesFromPool(poolId, nodeIds, deallocationOption, resizeTimeout, additionalBehaviors);
    }

    /**
     * Removes the specified compute nodes from the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param computeNodes
     *            The compute nodes to remove from the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes)
            throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, null, null, null);
    }

    /**
     * Removes the specified compute nodes from the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param computeNodes
     *            The compute nodes to remove from the pool.
     * @param deallocationOption
     *            Specifies when nodes may be removed from the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes,
            ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, deallocationOption, null, null);
    }

    /**
     * Removes the specified compute nodes from the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param computeNodes
     *            The compute nodes to remove from the pool.
     * @param deallocationOption
     *            Specifies when nodes may be removed from the pool.
     * @param resizeTimeout
     *            Specifies the timeout for removal of compute nodes from the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes,
            ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        List<String> nodeIds = new LinkedList<>();
        for (ComputeNode node : computeNodes) {
            nodeIds.add(node.id());
        }

        removeNodesFromPool(poolId, nodeIds, deallocationOption, resizeTimeout, additionalBehaviors);
    }

    /**
     * Removes the specified compute nodes from the specified pool.
     *
     * @param poolId
     *            The ID of the pool.
     * @param computeNodeIds
     *            The IDs of the compute nodes to remove from the pool.
     * @param deallocationOption
     *            Specifies when nodes may be removed from the pool.
     * @param resizeTimeout
     *            Specifies the timeout for removal of compute nodes from the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void removeNodesFromPool(String poolId, List<String> computeNodeIds,
            ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolRemoveNodesOptions options = new PoolRemoveNodesOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        NodeRemoveParameter param = new NodeRemoveParameter().withNodeList(computeNodeIds)
                .withNodeDeallocationOption(deallocationOption).withResizeTimeout(resizeTimeout);

        this.parentBatchClient.protocolLayer().pools().removeNodes(poolId, param, options);
    }

    /**
     * Checks whether the specified pool exists.
     *
     * @param poolId
     *            The ID of the pool.
     * @return True if the pool exists; otherwise, false.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public boolean existsPool(String poolId) throws BatchErrorException, IOException {
        return existsPool(poolId, null);
    }

    /**
     * Checks whether the specified pool exists.
     *
     * @param poolId
     *            The ID of the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return True if the pool exists; otherwise, false.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public boolean existsPool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {

        PoolExistsOptions options = new PoolExistsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().pools().exists(poolId, options);
    }

    /**
     * Updates the specified pool. This method fully replaces all the updatable
     * properties of the pool. For example, if the startTask parameter is null and
     * the pool has a start task associated with it, then the Batch service will
     * remove the existing start task.
     *
     * @param poolId
     *            The ID of the pool.
     * @param startTask
     *            A task to run on each compute node as it joins the pool. If null,
     *            any existing start task is removed from the pool.
     * @param certificateReferences
     *            A collection of certificates to be installed on each compute node
     *            in the pool. If null, any existing certificate references are
     *            removed from the pool.
     * @param applicationPackageReferences
     *            A collection of application packages to be installed on each
     *            compute node in the pool. If null, any existing application
     *            packages references are removed from the pool.
     * @param metadata
     *            A collection of name-value pairs associated with the pool as
     *            metadata. If null, any existing metadata is removed from the pool.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void updatePoolProperties(String poolId, StartTask startTask,
                                     Collection<CertificateReference> certificateReferences,
                                     Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata)
            throws BatchErrorException, IOException {
        updatePoolProperties(poolId, startTask, certificateReferences, applicationPackageReferences, metadata, null);
    }

    /**
     * Updates the specified pool. This method fully replaces all the updatable
     * properties of the pool. For example, if the startTask parameter is null and
     * the pool has a start task associated with it, then the Batch service will
     * remove the existing start task.
     *
     * @param poolId
     *            The ID of the pool.
     * @param startTask
     *            A task to run on each compute node as it joins the pool. If null,
     *            any existing start task is removed from the pool.
     * @param certificateReferences
     *            A collection of certificates to be installed on each compute node
     *            in the pool. If null, any existing certificate references are
     *            removed from the pool.
     * @param applicationPackageReferences
     *            A collection of application packages to be installed on each
     *            compute node in the pool. If null, any existing application
     *            packages references are removed from the pool.
     * @param metadata
     *            A collection of name-value pairs associated with the pool as
     *            metadata. If null, any existing metadata is removed from the pool.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void updatePoolProperties(String poolId, StartTask startTask,
                                     Collection<CertificateReference> certificateReferences,
                                     Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata,
                                     Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpdatePropertiesParameter param = new PoolUpdatePropertiesParameter()
                .withMetadata(metadata == null ? new LinkedList<MetadataItem>() : new LinkedList<>(metadata))
                .withApplicationPackageReferences(
                        applicationPackageReferences == null ? new LinkedList<ApplicationPackageReference>()
                                : new LinkedList<>(applicationPackageReferences))
                .withCertificateReferences(certificateReferences == null ? new LinkedList<CertificateReference>()
                        : new LinkedList<>(certificateReferences))
                .withStartTask(startTask);

        updatePoolProperties(poolId, param, additionalBehaviors);
    }

    /**
     * Updates the specified pool. This method fully replaces all the updatable
     * properties of the pool. For example, if the startTask parameter is null and
     * the pool has a start task associated with it, then the Batch service will
     * remove the existing start task.
     *
     * @param poolId
     *            The ID of the pool.
     * @param param
     *             The Pool update properties
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void updatePoolProperties(String poolId, PoolUpdatePropertiesParameter param) throws BatchErrorException, IOException {
        updatePoolProperties(poolId, param, null);
    }

    /**
     * Updates the specified pool. This method fully replaces all the updatable
     * properties of the pool. For example, if the startTask parameter is null and
     * the pool has a start task associated with it, then the Batch service will
     * remove the existing start task.
     *
     * @param poolId
     *            The ID of the pool.
     * @param param
     *            The Pool update properties
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void updatePoolProperties(String poolId, PoolUpdatePropertiesParameter param, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpdatePropertiesOptions options = new PoolUpdatePropertiesOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().pools().updateProperties(poolId, param, options);
    }

    /**
     * Updates the specified pool. This method only replaces the properties
     * specified with non-null values.
     *
     * @param poolId
     *            The ID of the pool.
     * @param startTask
     *            A task to run on each compute node as it joins the pool. If null,
     *            any existing start task is left unchanged.
     * @param certificateReferences
     *            A collection of certificates to be installed on each compute node
     *            in the pool. If null, any existing certificate references are left
     *            unchanged.
     * @param applicationPackageReferences
     *            A collection of application packages to be installed on each
     *            compute node in the pool. If null, any existing application
     *            packages references are left unchanged.
     * @param metadata
     *            A collection of name-value pairs associated with the pool as
     *            metadata. If null, any existing metadata is left unchanged.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences,
            Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata)
            throws BatchErrorException, IOException {
        patchPool(poolId, startTask, certificateReferences, applicationPackageReferences, metadata, null);
    }

    /**
     * Updates the specified pool. This method only replaces the properties
     * specified with non-null values.
     *
     * @param poolId
     *            The ID of the pool.
     * @param startTask
     *            A task to run on each compute node as it joins the pool. If null,
     *            any existing start task is left unchanged.
     * @param certificateReferences
     *            A collection of certificates to be installed on each compute node
     *            in the pool. If null, any existing certificate references are left
     *            unchanged.
     * @param applicationPackageReferences
     *            A collection of application packages to be installed on each
     *            compute node in the pool. If null, any existing application
     *            packages references are left unchanged.
     * @param metadata
     *            A collection of name-value pairs associated with the pool as
     *            metadata. If null, any existing metadata is left unchanged.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences,
            Collection<ApplicationPackageReference> applicationPackageReferences, Collection<MetadataItem> metadata,
            Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolPatchParameter param = new PoolPatchParameter().withStartTask(startTask);
        if (metadata != null) {
            param.withMetadata(new LinkedList<>(metadata));
        }
        if (applicationPackageReferences != null) {
            param.withApplicationPackageReferences(new LinkedList<>(applicationPackageReferences));
        }
        if (certificateReferences != null) {
            param.withCertificateReferences(new LinkedList<>(certificateReferences));
        }

        patchPool(poolId, param, additionalBehaviors);
    }

    /**
     * Updates the specified pool. This method only replaces the properties
     * specified with non-null values.
     *
     * @param poolId
     *            The ID of the pool.
     * @param param
     *             The Pool patch properties
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void patchPool(String poolId, PoolPatchParameter param) throws BatchErrorException, IOException {
        patchPool(poolId, param, null);
    }

    /**
     * Updates the specified pool. This method only replaces the properties
     * specified with non-null values.
     *
     * @param poolId
     *            The ID of the pool.
     * @param param
     *             The Pool patch properties
     * @param additionalBehaviors
     *             A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public void patchPool(String poolId, PoolPatchParameter param, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolPatchOptions options = new PoolPatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().pools().patch(poolId, param, options);
    }

    /**
     * Lists pool usage metrics.
     *
     * @param startTime
     *            The start time of the aggregation interval covered by this entry.
     * @param endTime
     *            The end time of the aggregation interval for this entry.
     * @return A list of {@link PoolUsageMetrics} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime)
            throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, null, null);
    }

    /**
     * Lists pool usage metrics.
     *
     * @param startTime
     *            The start time of the aggregation interval covered by this entry.
     * @param endTime
     *            The end time of the aggregation interval for this entry.
     * @param detailLevel
     *            A {@link DetailLevel} used for filtering the list and for
     *            controlling which properties are retrieved from the service.
     * @return A list of {@link PoolUsageMetrics} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime,
            DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, detailLevel, null);
    }

    /**
     * Lists pool usage metrics.
     *
     * @param startTime
     *            The start time of the aggregation interval covered by this entry.
     * @param endTime
     *            The end time of the aggregation interval for this entry.
     * @param detailLevel
     *            A {@link DetailLevel} used for filtering the list and for
     *            controlling which properties are retrieved from the service.
     * @param additionalBehaviors
     *            A collection of {@link BatchClientBehavior} instances that are
     *            applied to the Batch service request.
     * @return A list of {@link PoolUsageMetrics} objects.
     * @throws BatchErrorException
     *             Exception thrown when an error response is received from the
     *             Batch service.
     * @throws IOException
     *             Exception thrown when there is an error in
     *             serialization/deserialization of data sent to/received from the
     *             Batch service.
     */
    public PagedList<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime,
            DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors)
            throws BatchErrorException, IOException {
        PoolListUsageMetricsOptions options = new PoolListUsageMetricsOptions().withStartTime(startTime)
                .withEndTime(endTime);
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().pools().listUsageMetrics(options);
    }
}
