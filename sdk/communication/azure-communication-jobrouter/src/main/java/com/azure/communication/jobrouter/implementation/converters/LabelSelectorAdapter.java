// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.ConditionalQueueSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.ConditionalWorkerSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.PassThroughQueueSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.PassThroughWorkerSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.QueueWeightedAllocationInternal;
import com.azure.communication.jobrouter.implementation.models.RouterQueueSelectorInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerSelectorInternal;
import com.azure.communication.jobrouter.implementation.models.RuleEngineQueueSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.RuleEngineWorkerSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.StaticQueueSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.StaticWorkerSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.WeightedAllocationQueueSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.WeightedAllocationWorkerSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.WorkerSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.WorkerWeightedAllocationInternal;
import com.azure.communication.jobrouter.models.ConditionalQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.ConditionalWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.PassThroughQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.PassThroughWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RouterQueueSelector;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.RuleEngineQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RuleEngineWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.StaticQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.StaticWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.WeightedAllocationQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.WeightedAllocationWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;

import java.util.stream.Collectors;

import static com.azure.communication.jobrouter.implementation.converters.RouterRuleAdapter.getRouterRuleInternal;

/**
 * Converts queue and worker selectors to
 * {@link com.azure.communication.jobrouter.implementation.models.RouterQueueSelectorInternal} and
 * {@link com.azure.communication.jobrouter.implementation.models.RouterWorkerSelectorInternal}.
 */
public class LabelSelectorAdapter {

    public static RouterWorkerSelectorInternal convertWorkerSelectorToInternal(RouterWorkerSelector ws) {
        RouterWorkerSelectorInternal workerSelector = new RouterWorkerSelectorInternal()
        .setKey(ws.getKey())
            .setLabelOperator(ws.getLabelOperator())
            .setValue(RouterValueAdapter.getValue(ws.getValue()))
            .setExpedite(ws.isExpedite())
            .setExpiresAfterSeconds(
                ws.getExpiresAfter() != null ? (double) ws.getExpiresAfter().getSeconds() : null);
        return workerSelector;
    }

    public static RouterQueueSelectorInternal convertQueueSelectorToInternal(RouterQueueSelector qs) {
        RouterQueueSelectorInternal queueSelector = new RouterQueueSelectorInternal()
            .setKey(qs.getKey())
            .setLabelOperator(qs.getLabelOperator())
            .setValue(RouterValueAdapter.getValue(qs.getValue()));

        return queueSelector;
    }

    public static QueueSelectorAttachmentInternal convertQueueSelectorAttachmentToInternal(QueueSelectorAttachment attachment) {
        if (attachment instanceof StaticQueueSelectorAttachment) {
            StaticQueueSelectorAttachment staticAttach = (StaticQueueSelectorAttachment) attachment;
            return new StaticQueueSelectorAttachmentInternal().setQueueSelector(
                LabelSelectorAdapter.convertQueueSelectorToInternal(staticAttach.getQueueSelector()));
        } else if (attachment instanceof ConditionalQueueSelectorAttachment) {
            ConditionalQueueSelectorAttachment conditional = (ConditionalQueueSelectorAttachment) attachment;
            return new ConditionalQueueSelectorAttachmentInternal().setCondition(getRouterRuleInternal(conditional.getCondition()))
                .setQueueSelectors(
                conditional.getQueueSelectors() != null
                    ? conditional.getQueueSelectors().stream().map(LabelSelectorAdapter::convertQueueSelectorToInternal)
                    .collect(Collectors.toList()) : null);
        } else if (attachment instanceof PassThroughQueueSelectorAttachment) {
            PassThroughQueueSelectorAttachment passThrough = (PassThroughQueueSelectorAttachment) attachment;
            return new PassThroughQueueSelectorAttachmentInternal().setKey(passThrough.getKey()).setLabelOperator(
                passThrough.getLabelOperator());
        } else if (attachment instanceof RuleEngineQueueSelectorAttachment) {
            RuleEngineQueueSelectorAttachment rule = (RuleEngineQueueSelectorAttachment) attachment;
            return new RuleEngineQueueSelectorAttachmentInternal().setRule(getRouterRuleInternal(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationQueueSelectorAttachment) {
            WeightedAllocationQueueSelectorAttachment weighted = (WeightedAllocationQueueSelectorAttachment) attachment;
            return new WeightedAllocationQueueSelectorAttachmentInternal().setAllocations(
                weighted.getAllocations() != null
                    ? weighted.getAllocations().stream()
                    .map(a -> new QueueWeightedAllocationInternal().setWeight(a.getWeight()).setQueueSelectors(a.getQueueSelectors() != null
                        ? a.getQueueSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertQueueSelectorToInternal(qs))
                            .collect(Collectors.toList()) : null))
                    .collect(Collectors.toList()) : null);
        }

        return null;
    }

    public static WorkerSelectorAttachmentInternal convertWorkerSelectorAttachmentToInternal(WorkerSelectorAttachment attachment) {
        if (attachment instanceof StaticWorkerSelectorAttachment) {
            StaticWorkerSelectorAttachment staticAttach = (StaticWorkerSelectorAttachment) attachment;
            return new StaticWorkerSelectorAttachmentInternal().setWorkerSelector(
                LabelSelectorAdapter.convertWorkerSelectorToInternal(staticAttach.getWorkerSelector()));
        } else if (attachment instanceof ConditionalWorkerSelectorAttachment) {
            ConditionalWorkerSelectorAttachment conditional = (ConditionalWorkerSelectorAttachment) attachment;
            return new ConditionalWorkerSelectorAttachmentInternal().setCondition(
                getRouterRuleInternal(conditional.getCondition())).setWorkerSelectors(
                conditional.getWorkerSelectors() != null ? conditional.getWorkerSelectors().stream()
                    .map(LabelSelectorAdapter::convertWorkerSelectorToInternal).collect(Collectors.toList()) : null);
        } else if (attachment instanceof PassThroughWorkerSelectorAttachment) {
            PassThroughWorkerSelectorAttachment passThrough = (PassThroughWorkerSelectorAttachment) attachment;
            PassThroughWorkerSelectorAttachmentInternal result = new PassThroughWorkerSelectorAttachmentInternal().setKey(passThrough.getKey())
                    .setLabelOperator(
                passThrough.getLabelOperator()
            );
            if (passThrough.getExpiresAfter() != null) {
                result.setExpiresAfterSeconds((double) passThrough.getExpiresAfter().getSeconds());
            }
            return result;
        } else if (attachment instanceof RuleEngineWorkerSelectorAttachment) {
            RuleEngineWorkerSelectorAttachment rule = (RuleEngineWorkerSelectorAttachment) attachment;
            return new RuleEngineWorkerSelectorAttachmentInternal().setRule(getRouterRuleInternal(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationWorkerSelectorAttachment) {
            WeightedAllocationWorkerSelectorAttachment weighted = (WeightedAllocationWorkerSelectorAttachment) attachment;
            return new WeightedAllocationWorkerSelectorAttachmentInternal().setAllocations(
                weighted.getAllocations() != null ? weighted.getAllocations().stream()
                    .map(a -> new WorkerWeightedAllocationInternal().setWeight(
                        a.getWeight()).setWorkerSelectors(
                        a.getWorkerSelectors() != null ? a.getWorkerSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertWorkerSelectorToInternal(qs))
                            .collect(Collectors.toList()) : null))
                    .collect(Collectors.toList()) : null);
        }

        return null;
    }

}
