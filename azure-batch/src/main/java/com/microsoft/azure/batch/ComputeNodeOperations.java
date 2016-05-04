/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.google.common.io.CharStreams;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeAddUserOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeDeleteUserOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeDisableSchedulingOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeEnableSchedulingOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeGetHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeGetOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeGetRemoteDesktopHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeGetRemoteDesktopOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeGetRemoteLoginSettingsHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeGetRemoteLoginSettingsOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeGetRemoteLoginSettingsResultInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeListHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeListOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeRebootOption;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeRebootOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeReimageOption;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeReimageOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeUpdateUserOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ComputeNodeUserInner;
import com.microsoft.azure.batch.protocol.implementation.api.DisableComputeNodeSchedulingOption;
import com.microsoft.azure.batch.protocol.implementation.api.NodeUpdateUserParameterInner;
import com.microsoft.rest.ServiceResponseWithHeaders;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class ComputeNodeOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    ComputeNodeOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    @Override
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        this._customBehaviors = behaviors;
    }

    public void addComputeNodeUser(String poolId, String nodeId, ComputeNodeUserInner user) throws BatchErrorException, IOException {
        addComputeNodeUser(poolId, nodeId, user, null);
    }

    public void addComputeNodeUser(String poolId, String nodeId, ComputeNodeUserInner user, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeAddUserOptionsInner options = new ComputeNodeAddUserOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().computeNodes().addUser(poolId, nodeId, user, options);
    }

    public void deleteComputeNodeUser(String poolId, String nodeId, String userName) throws BatchErrorException, IOException {
        deleteComputeNodeUser(poolId, nodeId, userName, null);
    }

    public void deleteComputeNodeUser(String poolId, String nodeId, String userName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeDeleteUserOptionsInner options = new ComputeNodeDeleteUserOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().computeNodes().deleteUser(poolId, nodeId, userName, options);
    }

    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String password, DateTime expiryTime) throws BatchErrorException, IOException {
        updateComputeNodeUser(poolId, nodeId, userName, password, expiryTime, null);
    }

    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String password, DateTime expiryTime, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        NodeUpdateUserParameterInner param = new NodeUpdateUserParameterInner();
        param.setPassword(password);
        param.setExpiryTime(expiryTime);

        updateComputeNodeUser(poolId, nodeId, userName, param, additionalBehaviors);
    }

    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String sshPublicKey) throws BatchErrorException, IOException {
        updateComputeNodeUser(poolId, nodeId, userName, sshPublicKey, (Iterable<BatchClientBehavior>)null);
    }

    public void updateComputeNodeUser(String poolId, String nodeId, String userName, String sshPublicKey, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        NodeUpdateUserParameterInner param = new NodeUpdateUserParameterInner();
        param.setSshPublicKey(sshPublicKey);

        updateComputeNodeUser(poolId, nodeId, userName, param, additionalBehaviors);
    }

    private void updateComputeNodeUser(String poolId, String nodeId, String userName, NodeUpdateUserParameterInner nodeUpdateUserParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeUpdateUserOptionsInner options = new ComputeNodeUpdateUserOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().computeNodes().updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, options);
    }

    public ComputeNodeInner getComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getComputeNode(poolId, nodeId, null, null);
    }


    public ComputeNodeInner getComputeNode(String poolId, String nodeId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getComputeNode(poolId, nodeId, detailLevel, null);
    }

    public ComputeNodeInner getComputeNode(String poolId, String nodeId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetOptionsInner options = new ComputeNodeGetOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<ComputeNodeInner, ComputeNodeGetHeadersInner> response = this._parentBatchClient.getProtocolLayer().computeNodes().get(poolId, nodeId, options);

        return response.getBody();
    }

    public void rebootComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        rebootComputeNode(poolId, nodeId, null, null);
    }

    public void rebootComputeNode(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption) throws BatchErrorException, IOException {
        rebootComputeNode(poolId, nodeId, nodeRebootOption, null);
    }

    public void rebootComputeNode(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeRebootOptionsInner options = new ComputeNodeRebootOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().computeNodes().reboot(poolId, nodeId, nodeRebootOption, options);
    }

    public void reimageComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        reimageComputeNode(poolId, nodeId, null, null);
    }

    public void reimageComputeNode(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption) throws BatchErrorException, IOException {
        reimageComputeNode(poolId, nodeId, nodeReimageOption, null);
    }

    public void reimageComputeNode(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeReimageOptionsInner options = new ComputeNodeReimageOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().computeNodes().reimage(poolId, nodeId, nodeReimageOption, options);
    }

    public void disableComputeNodeScheduling(String poolId, String nodeId) throws BatchErrorException, IOException {
        disableComputeNodeScheduling(poolId, nodeId, null, null);
    }

    public void disableComputeNodeScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption) throws BatchErrorException, IOException {
        disableComputeNodeScheduling(poolId, nodeId, nodeDisableSchedulingOption, null);
    }

    public void disableComputeNodeScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeDisableSchedulingOptionsInner options = new ComputeNodeDisableSchedulingOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().computeNodes().disableScheduling(poolId, nodeId, nodeDisableSchedulingOption, options);
    }

    public void enableComputeNodeScheduling(String poolId, String nodeId) throws BatchErrorException, IOException {
        enableComputeNodeScheduling(poolId, nodeId, null);
    }

    public void enableComputeNodeScheduling(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeEnableSchedulingOptionsInner options = new ComputeNodeEnableSchedulingOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().computeNodes().enableScheduling(poolId, nodeId, options);
    }

    public String getComputeNodeRemoteDesktop(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getComputeNodeRemoteDesktop(poolId, nodeId, null);
    }

    public String getComputeNodeRemoteDesktop(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetRemoteDesktopOptionsInner options = new ComputeNodeGetRemoteDesktopOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeadersInner> response = this._parentBatchClient.getProtocolLayer().computeNodes().getRemoteDesktop(poolId, nodeId, options);

        if (response.getBody() != null) {
            return CharStreams.toString(new InputStreamReader(response.getBody(), "UTF-8"));
        }
        else {
            return null;
        }
    }

    public ComputeNodeGetRemoteLoginSettingsResultInner getRemoteLoginSettings(String poolId, String nodeId) throws BatchErrorException, IOException {
        return getRemoteLoginSettings(poolId, nodeId, null);
    }

    public ComputeNodeGetRemoteLoginSettingsResultInner getRemoteLoginSettings(String poolId, String nodeId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeGetRemoteLoginSettingsOptionsInner options = new ComputeNodeGetRemoteLoginSettingsOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResultInner, ComputeNodeGetRemoteLoginSettingsHeadersInner> response = this._parentBatchClient.getProtocolLayer().computeNodes().getRemoteLoginSettings(poolId, nodeId, options);

        return response.getBody();
    }

    public List<ComputeNodeInner> listComputeNodes(String poolId) throws BatchErrorException, IOException {
        return listComputeNodes(poolId, null, null);
    }

    public List<ComputeNodeInner> listComputeNodes(String poolId, DetailLevel detaiLevel) throws BatchErrorException, IOException {
        return listComputeNodes(poolId, detaiLevel, null);
    }

    public List<ComputeNodeInner> listComputeNodes(String poolId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ComputeNodeListOptionsInner options = new ComputeNodeListOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<ComputeNodeInner>, ComputeNodeListHeadersInner> response = this._parentBatchClient.getProtocolLayer().computeNodes().list(poolId, options);

        return response.getBody();
    }

}
