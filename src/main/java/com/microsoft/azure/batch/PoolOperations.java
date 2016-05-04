/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.implementation.api.AutoScaleRunInner;
import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
import com.microsoft.azure.batch.protocol.implementation.api.CertificateReference;
import com.microsoft.azure.batch.protocol.implementation.api.CloudPoolInner;
import com.microsoft.azure.batch.protocol.implementation.api.CloudServiceConfiguration;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeDeallocationOption;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeInner;
import com.microsoft.azure.batch.protocol.implementation.api.MetadataItem;
import com.microsoft.azure.batch.protocol.implementation.api.NodeRemoveParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolAddOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolAddParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolDeleteOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolDisableAutoScaleOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolEnableAutoScaleOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolEnableAutoScaleParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolEvaluateAutoScaleHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolEvaluateAutoScaleOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolExistsHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolExistsOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolGetAllPoolsLifetimeStatisticsHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolGetAllPoolsLifetimeStatisticsOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolGetHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolGetOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolListHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolListOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolListPoolUsageMetricsHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolListPoolUsageMetricsOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolPatchOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolPatchParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolRemoveNodesOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolResizeOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolResizeParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolStatisticsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolStopResizeOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolUpdatePropertiesOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolUpdatePropertiesParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolUpgradeOSOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.PoolUsageMetricsInner;
import com.microsoft.azure.batch.protocol.implementation.api.StartTask;
import com.microsoft.azure.batch.protocol.implementation.api.VirtualMachineConfiguration;
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
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
    }

    public List<CloudPoolInner> listPools() throws BatchErrorException, IOException {
        return listPools(null, null);
    }

    public List<CloudPoolInner> listPools(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPools(detailLevel, null);
    }

    public List<CloudPoolInner> listPools(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolListOptionsInner options = new PoolListOptionsInner();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudPoolInner>, PoolListHeadersInner> response = this._parentBatchClient.getProtocolLayer().pools().list(options);

        return response.getBody();
    }

    public CloudPoolInner getPool(String poolId) throws BatchErrorException, IOException {
        return getPool(poolId, null, null);
    }

    public CloudPoolInner getPool(String poolId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getPool(poolId, detailLevel, null);
    }

    public CloudPoolInner getPool(String poolId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolGetOptionsInner options = new PoolGetOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudPoolInner, PoolGetHeadersInner> response = this._parentBatchClient.getProtocolLayer().pools().get(poolId, options);

        return response.getBody();
    }

    public void deletePool(String poolId) throws BatchErrorException, IOException {
        deletePool(poolId, null);
    }

    public void deletePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolDeleteOptionsInner options = new PoolDeleteOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().pools().delete(poolId, options);
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
        PoolAddParameterInner parameter = new PoolAddParameterInner();
        parameter.setId(poolId);
        parameter.setCloudServiceConfiguration(cloudServiceConfiguration);
        parameter.setTargetDedicated(targetDedicated);
        parameter.setVmSize(virtualMachineSize);

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
        PoolAddParameterInner parameter = new PoolAddParameterInner();
        parameter.setId(poolId);
        parameter.setVirtualMachineConfiguration(virtualMachineConfiguration);
        parameter.setTargetDedicated(targetDedicated);
        parameter.setVmSize(virtualMachineSize);

        createPool(parameter, additionalBehaviors);
    }

    public void createPool(PoolAddParameterInner parameter) throws BatchErrorException, IOException {
        createPool(parameter, null);
    }

    public void createPool(PoolAddParameterInner parameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolAddOptionsInner options = new PoolAddOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().pools().add(parameter, options);
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
        PoolResizeOptionsInner options = new PoolResizeOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolResizeParameterInner param = new PoolResizeParameterInner();
        param.setResizeTimeout(resizeTimeout);
        param.setNodeDeallocationOption(deallocationOption);
        param.setTargetDedicated(targetDedicated);

        this._parentBatchClient.getProtocolLayer().pools().resize(poolId, param, options);
    }

    public void stopResizePool(String poolId) throws BatchErrorException, IOException {
        stopResizePool(poolId, null);
    }

    public void stopResizePool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolStopResizeOptionsInner options = new PoolStopResizeOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().pools().stopResize(poolId, options);
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
        PoolEnableAutoScaleOptionsInner options = new PoolEnableAutoScaleOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolEnableAutoScaleParameterInner param = new PoolEnableAutoScaleParameterInner();
        param.setAutoScaleFormula(autoScaleFormula);
        param.setAutoScaleEvaluationInterval(autoScaleEvaluationInterval);

        this._parentBatchClient.getProtocolLayer().pools().enableAutoScale(poolId, param, options);
    }

    public void disableAutoScale(String poolId) throws BatchErrorException, IOException {
        disableAutoScale(poolId, null);
    }

    public void disableAutoScale(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolDisableAutoScaleOptionsInner options = new PoolDisableAutoScaleOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().pools().disableAutoScale(poolId, options);
    }

    public AutoScaleRunInner evaluateAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException {
        return evaluateAutoScale(poolId, autoScaleFormula, null);
    }

    public AutoScaleRunInner evaluateAutoScale(String poolId, String autoScaleFormula, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolEvaluateAutoScaleOptionsInner options = new PoolEvaluateAutoScaleOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<AutoScaleRunInner, PoolEvaluateAutoScaleHeadersInner> response = this._parentBatchClient.getProtocolLayer().pools().evaluateAutoScale(poolId, autoScaleFormula, options);
        return response.getBody();
    }

    public void changeOSVersion(String poolId, String targetOSVersion) throws BatchErrorException, IOException {
        changeOSVersion(poolId, targetOSVersion, null);
    }

    public void changeOSVersion(String poolId, String targetOSVersion, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpgradeOSOptionsInner options = new PoolUpgradeOSOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().pools().upgradeOS(poolId, targetOSVersion, options);
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

    public void removeNodesFromPool(String poolId, Collection<ComputeNodeInner> computeNodes) throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, null, null, null);
    }

    public void removeNodesFromPool(String poolId, Collection<ComputeNodeInner> computeNodes, ComputeNodeDeallocationOption deallocationOption) throws BatchErrorException, IOException {
        removeNodesFromPool(poolId, computeNodes, deallocationOption, null, null);
    }

    public void removeNodesFromPool(String poolId, Collection<ComputeNodeInner> computeNodes, ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        List<String> nodeIds = new LinkedList<>();
        for (ComputeNodeInner node : computeNodes) {
            nodeIds.add(node.id());
        }

        removeNodesFromPool(poolId, nodeIds, deallocationOption, resizeTimeout, additionalBehaviors);
    }

    private void removeNodesFromPool(String poolId, List<String> computeNodesId, ComputeNodeDeallocationOption deallocationOption, Period resizeTimeout, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolRemoveNodesOptionsInner options = new PoolRemoveNodesOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        NodeRemoveParameterInner param = new NodeRemoveParameterInner();
        param.setNodeList(computeNodesId);
        param.setNodeDeallocationOption(deallocationOption);
        param.setResizeTimeout(resizeTimeout);

        this._parentBatchClient.getProtocolLayer().pools().removeNodes(poolId, param, options);
    }

    public boolean existsPool(String poolId) throws BatchErrorException, IOException {
        return existsPool(poolId, null);
    }

    public boolean existsPool(String poolId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {

        PoolExistsOptionsInner options = new PoolExistsOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Boolean, PoolExistsHeadersInner> response = this._parentBatchClient.getProtocolLayer().pools().exists(poolId, options);

        return response.getBody();
    }

    public void updatePoolProperties(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata) throws BatchErrorException, IOException {
        updatePoolProperties(poolId, startTask, certificateReferences, metadata, null);
    }

    public void updatePoolProperties(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolUpdatePropertiesOptionsInner options = new PoolUpdatePropertiesOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolUpdatePropertiesParameterInner param = new PoolUpdatePropertiesParameterInner();
        param.setMetadata(new LinkedList<MetadataItem>(metadata));
        param.setCertificateReferences(new LinkedList<CertificateReference>(certificateReferences));
        param.setStartTask(startTask);

        this._parentBatchClient.getProtocolLayer().pools().updateProperties(poolId, param, options);
    }

    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchPool(poolId, startTask, certificateReferences, metadata, null);
    }

    public void patchPool(String poolId, StartTask startTask, Collection<CertificateReference> certificateReferences, Collection<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolPatchOptionsInner options = new PoolPatchOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        PoolPatchParameterInner param = new PoolPatchParameterInner();
        param.setMetadata(new LinkedList<MetadataItem>(metadata));
        param.setCertificateReferences(new LinkedList<CertificateReference>(certificateReferences));
        param.setStartTask(startTask);

        this._parentBatchClient.getProtocolLayer().pools().patch(poolId, param, options);
    }

    public List<PoolUsageMetricsInner> listPoolUsageMetrics(DateTime startTime, DateTime endTime) throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, null, null);
    }

    public List<PoolUsageMetricsInner> listPoolUsageMetrics(DateTime startTime, DateTime endTime, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listPoolUsageMetrics(startTime, endTime, detailLevel, null);
    }

    public List<PoolUsageMetricsInner> listPoolUsageMetrics(DateTime startTime, DateTime endTime, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolListPoolUsageMetricsOptionsInner options = new PoolListPoolUsageMetricsOptionsInner();
        options.setStartTime(startTime);
        options.setEndTime(endTime);
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> response = this._parentBatchClient.getProtocolLayer().pools().listPoolUsageMetrics(options);

        return response.getBody();
    }

    public PoolStatisticsInner getAllPoolsLifetimeStatistics() throws BatchErrorException, IOException {
        return getAllPoolsLifetimeStatistics(null);
    }

    public PoolStatisticsInner getAllPoolsLifetimeStatistics(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        PoolGetAllPoolsLifetimeStatisticsOptionsInner options = new PoolGetAllPoolsLifetimeStatisticsOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PoolStatisticsInner, PoolGetAllPoolsLifetimeStatisticsHeadersInner> response = this._parentBatchClient.getProtocolLayer().pools().getAllPoolsLifetimeStatistics(options);

        return response.getBody();
    }
}
