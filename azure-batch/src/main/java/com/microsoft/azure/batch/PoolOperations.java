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

public class PoolOperations implements IInheritedBehaviors {
    PoolOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    public List<CloudPool> listPools() throws BatchErrorException, IOException {
        return listPools(null, null);
    }

    public List<CloudPool> listPools(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPools(detailLevel, null);
    }

    public List<CloudPool> listPools(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolListOptions options = new PoolListOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudPool>, PoolListHeaders> response = this._parentBatchClient.protocolLayer().pools().list(options);

        return response.getBody();
    }

    public CloudPool getPool(String poolId) throws BatchErrorException, IOException {
        return getPool(poolId, null, null);
    }

    public CloudPool getPool(String poolId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getPool(poolId, detailLevel, null);
    }

    public CloudPool getPool(String poolId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolGetOptions options = new PoolGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudPool, PoolGetHeaders> response = this._parentBatchClient.protocolLayer().pools().get(poolId, options);

        return response.getBody();
    }

    public void deletePool(String poolId) throws BatchErrorException, IOException {
        deletePool(poolId, null);
    }

    public void deletePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolDeleteOptions options = new PoolDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().delete(poolId, options);
    }

    public void createPool(String poolId, String virtualMachineSize, CloudServiceConfiguration cloudServiceConfiguration) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, cloudServiceConfiguration, null, null);
    }

    public void createPool(String poolId, String virtualMachineSize, CloudServiceConfiguration cloudServiceConfiguration, int targetDedicated) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, cloudServiceConfiguration, targetDedicated, null);
    }

    public void createPool(String poolId, String virtualMachineSize, CloudServiceConfiguration cloudServiceConfiguration, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, cloudServiceConfiguration, null, additionalBehaviors);
    }

    public void createPool(String poolId, String virtualMachineSize, CloudServiceConfiguration cloudServiceConfiguration, Integer targetDedicated, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolAddParameter parameter = new PoolAddParameter()
                .withId(poolId)
                .withCloudServiceConfiguration(cloudServiceConfiguration)
                .withTargetDedicated(targetDedicated)
                .withVmSize(virtualMachineSize);

        createPool(parameter, additionalBehaviors);
    }

    public void createPool(String poolId, String virtualMachineSize, VirtualMachineConfiguration virtualMachineConfiguration) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, virtualMachineConfiguration, null, null);
    }

    public void createPool(String poolId, String virtualMachineSize, VirtualMachineConfiguration virtualMachineConfiguration, int targetDedicated) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, virtualMachineConfiguration, targetDedicated, null);
    }

    public void createPool(String poolId, String virtualMachineSize, VirtualMachineConfiguration virtualMachineConfiguration, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        createPool(poolId, virtualMachineSize, virtualMachineConfiguration, null, additionalBehaviors);
    }

    public void createPool(String poolId, String virtualMachineSize, VirtualMachineConfiguration virtualMachineConfiguration, Integer targetDedicated, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolAddParameter parameter = new PoolAddParameter()
                .withId(poolId)
                .withVirtualMachineConfiguration(virtualMachineConfiguration)
                .withTargetDedicated(targetDedicated)
                .withVmSize(virtualMachineSize);

        createPool(parameter, additionalBehaviors);
    }

    public void createPool(PoolAddParameter parameter) throws BatchErrorException, IOException {
        createPool(parameter, null);
    }

    public void createPool(PoolAddParameter parameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolAddOptions options = new PoolAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().add(parameter, options);
    }

    public void resizePool(String poolId, int targetDedicated) throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicated, null, null, null);
    }

    public void resizePool(String poolId, int targetDedicated, Period resizeTimeout) throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicated, resizeTimeout, null, null);
    }

    public void resizePool(String poolId, int targetDedicated, Period resizeTimeout, ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        resizePool(poolId, targetDedicated, resizeTimeout, deallocationOption, null);
    }

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

    public void stopResizePool(String poolId) throws BatchErrorException, IOException {
        stopResizePool(poolId, null);
    }

    public void stopResizePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolStopResizeOptions options = new PoolStopResizeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().stopResize(poolId, options);
    }

    public void enableAutoScale(String poolId) throws BatchErrorException, IOException {
        enableAutoScale(poolId, null, null, null);
    }

    public void enableAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException {
        enableAutoScale(poolId, autoScaleFormula, null, null);
    }

    public void enableAutoScale(String poolId, String autoScaleFormula, Period autoScaleEvaluationInterval) throws BatchErrorException, IOException {
        enableAutoScale(poolId, autoScaleFormula, autoScaleEvaluationInterval, null);
    }

    public void enableAutoScale(String poolId, String autoScaleFormula, Period autoScaleEvaluationInterval, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolEnableAutoScaleOptions options = new PoolEnableAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolEnableAutoScaleParameter param = new PoolEnableAutoScaleParameter()
                .withAutoScaleFormula(autoScaleFormula)
                .withAutoScaleEvaluationInterval(autoScaleEvaluationInterval);

        this._parentBatchClient.protocolLayer().pools().enableAutoScale(poolId, param, options);
    }

    public void disableAutoScale(String poolId) throws BatchErrorException, IOException {
        disableAutoScale(poolId, null);
    }

    public void disableAutoScale(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolDisableAutoScaleOptions options = new PoolDisableAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().disableAutoScale(poolId, options);
    }

    public AutoScaleRun evaluateAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException {
        return evaluateAutoScale(poolId, autoScaleFormula, null);
    }

    public AutoScaleRun evaluateAutoScale(String poolId, String autoScaleFormula, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolEvaluateAutoScaleOptions options = new PoolEvaluateAutoScaleOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<AutoScaleRun, PoolEvaluateAutoScaleHeaders> response = this._parentBatchClient.protocolLayer().pools().evaluateAutoScale(poolId, autoScaleFormula, options);
        return response.getBody();
    }

    public void changeOSVersion(String poolId, String targetOSVersion) throws BatchErrorException, IOException {
        changeOSVersion(poolId, targetOSVersion, null);
    }

    public void changeOSVersion(String poolId, String targetOSVersion, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpgradeOSOptions options = new PoolUpgradeOSOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().pools().upgradeOS(poolId, targetOSVersion, options);
    }

    public void removeNodeFromPool(String poolId, String computeNodeId) throws BatchErrorException, IOException {
        removeNodeFromPool(poolId, computeNodeId, null, null, null);
    }

    public void removeNodeFromPool(String poolId, String computeNodeId, ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        removeNodeFromPool(poolId, computeNodeId, deallocationOption, null, null);
    }

    public void removeNodeFromPool(String poolId, String computeNodeId, ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        List<String> nodeIds = new LinkedList<>();
        nodeIds.add(computeNodeId);

        removeNodesFromPool(poolId, nodeIds, deallocationOption, resizeTimeout, additionalBehaviors);
    }

    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes) throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, null, null, null);
    }

    public void removeNodesFromPool(String poolId, Collection<ComputeNode> computeNodes, ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, deallocationOption, null, null);
    }

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

    public boolean existsPool(String poolId) throws BatchErrorException, IOException {
        return existsPool(poolId, null);
    }

    public boolean existsPool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {

        PoolExistsOptions options = new PoolExistsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Boolean, PoolExistsHeaders> response = this._parentBatchClient.protocolLayer().pools().exists(poolId, options);

        return response.getBody();
    }

    public void updatePoolProperties(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata) throws BatchErrorException, IOException {
        updatePoolProperties(poolId, startTask, certificateReferences, metadata, null);
    }

    public void updatePoolProperties(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpdatePropertiesOptions options = new PoolUpdatePropertiesOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolUpdatePropertiesParameter param = new PoolUpdatePropertiesParameter()
                .withMetadata(new LinkedList<MetadataItem>(metadata))
                .withCertificateReferences(new LinkedList<CertificateReference>(certificateReferences))
                .withStartTask(startTask);

        this._parentBatchClient.protocolLayer().pools().updateProperties(poolId, param, options);
    }

    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchPool(poolId, startTask, certificateReferences, metadata, null);
    }

    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolPatchOptions options = new PoolPatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolPatchParameter param = new PoolPatchParameter()
                .withMetadata(new LinkedList<MetadataItem>(metadata))
                .withCertificateReferences(new LinkedList<CertificateReference>(certificateReferences))
                .withStartTask(startTask);

        this._parentBatchClient.protocolLayer().pools().patch(poolId, param, options);
    }

    public List<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime) throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, null, null);
    }

    public List<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, detailLevel, null);
    }

    public List<PoolUsageMetrics> listPoolUsageMetrics(DateTime startTime, DateTime endTime, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolListPoolUsageMetricsOptions options = new PoolListPoolUsageMetricsOptions()
                .withStartTime(startTime)
                .withEndTime(endTime);
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<PoolUsageMetrics>, PoolListPoolUsageMetricsHeaders> response = this._parentBatchClient.protocolLayer().pools().listPoolUsageMetrics(options);

        return response.getBody();
    }

    public PoolStatistics getAllPoolsLifetimeStatistics() throws BatchErrorException, IOException {
        return getAllPoolsLifetimeStatistics(null);
    }

    public PoolStatistics getAllPoolsLifetimeStatistics(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolGetAllPoolsLifetimeStatisticsOptions options = new PoolGetAllPoolsLifetimeStatisticsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PoolStatistics, PoolGetAllPoolsLifetimeStatisticsHeaders> response = this._parentBatchClient.protocolLayer().pools().getAllPoolsLifetimeStatistics(options);

        return response.getBody();
    }
}
