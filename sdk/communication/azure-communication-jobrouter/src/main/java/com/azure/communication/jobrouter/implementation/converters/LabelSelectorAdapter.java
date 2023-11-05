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
        RouterWorkerSelectorInternal workerSelector = new RouterWorkerSelectorInternal(
            ws.getKey(),
            ws.getLabelOperator()
        )
            .setValue(RouterValueAdapter.getValue(ws.getValue()))
            .setExpedite(ws.isExpedite())
            .setExpiresAfterSeconds(
                ws.getExpiresAfter() != null ? (double) ws.getExpiresAfter().getSeconds() : null);
        return workerSelector;
    }

    public static RouterQueueSelectorInternal convertQueueSelectorToInternal(RouterQueueSelector qs) {
        RouterQueueSelectorInternal queueSelector = new RouterQueueSelectorInternal(
            qs.getKey(),
            qs.getLabelOperator()
        )
            .setValue(RouterValueAdapter.getValue(qs.getValue()));

        return queueSelector;
    }

    public static QueueSelectorAttachmentInternal convertQueueSelectorAttachmentToInternal(QueueSelectorAttachment attachment) {
        if (attachment instanceof StaticQueueSelectorAttachment) {
            StaticQueueSelectorAttachment staticAttach = (StaticQueueSelectorAttachment) attachment;
            return new StaticQueueSelectorAttachmentInternal(
                LabelSelectorAdapter.convertQueueSelectorToInternal(staticAttach.getQueueSelector()));
        } else if (attachment instanceof ConditionalQueueSelectorAttachment) {
            ConditionalQueueSelectorAttachment conditional = (ConditionalQueueSelectorAttachment) attachment;
            return new ConditionalQueueSelectorAttachmentInternal(getRouterRuleInternal(conditional.getCondition()),
                conditional.getQueueSelectors().stream()
                    .map(LabelSelectorAdapter::convertQueueSelectorToInternal).collect(Collectors.toList()));
        } else if (attachment instanceof PassThroughQueueSelectorAttachment) {
            PassThroughQueueSelectorAttachment passThrough = (PassThroughQueueSelectorAttachment) attachment;
            return new PassThroughQueueSelectorAttachmentInternal(passThrough.getKey(),
                passThrough.getLabelOperator());
        } else if (attachment instanceof RuleEngineQueueSelectorAttachment) {
            RuleEngineQueueSelectorAttachment rule = (RuleEngineQueueSelectorAttachment) attachment;
            return new RuleEngineQueueSelectorAttachmentInternal(getRouterRuleInternal(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationQueueSelectorAttachment) {
            WeightedAllocationQueueSelectorAttachment weighted = (WeightedAllocationQueueSelectorAttachment) attachment;
            return new WeightedAllocationQueueSelectorAttachmentInternal(
                weighted.getAllocations().stream()
                    .map(a -> new QueueWeightedAllocationInternal(a.getWeight(),
                        a.getQueueSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertQueueSelectorToInternal(qs))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList()));
        }

        return null;
    }

    public static WorkerSelectorAttachmentInternal convertWorkerSelectorAttachmentToInternal(WorkerSelectorAttachment attachment) {
        if (attachment instanceof StaticWorkerSelectorAttachment) {
            StaticWorkerSelectorAttachment staticAttach = (StaticWorkerSelectorAttachment) attachment;
            return new StaticWorkerSelectorAttachmentInternal(
                LabelSelectorAdapter.convertWorkerSelectorToInternal(staticAttach.getWorkerSelector()));
        } else if (attachment instanceof ConditionalWorkerSelectorAttachment) {
            ConditionalWorkerSelectorAttachment conditional = (ConditionalWorkerSelectorAttachment) attachment;
            return new ConditionalWorkerSelectorAttachmentInternal(
                getRouterRuleInternal(conditional.getCondition()),
                conditional.getWorkerSelectors().stream()
                    .map(LabelSelectorAdapter::convertWorkerSelectorToInternal).collect(Collectors.toList()));
        } else if (attachment instanceof PassThroughWorkerSelectorAttachment) {
            PassThroughWorkerSelectorAttachment passThrough = (PassThroughWorkerSelectorAttachment) attachment;
            return new PassThroughWorkerSelectorAttachmentInternal(
                passThrough.getKey(),
                passThrough.getLabelOperator()
            )
                .setExpiresAfterSeconds((double) passThrough.getExpiresAfter().getSeconds());
        } else if (attachment instanceof RuleEngineWorkerSelectorAttachment) {
            RuleEngineWorkerSelectorAttachment rule = (RuleEngineWorkerSelectorAttachment) attachment;
            return new RuleEngineWorkerSelectorAttachmentInternal(getRouterRuleInternal(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationWorkerSelectorAttachment) {
            WeightedAllocationWorkerSelectorAttachment weighted = (WeightedAllocationWorkerSelectorAttachment) attachment;
            return new WeightedAllocationWorkerSelectorAttachmentInternal(
                weighted.getAllocations().stream()
                    .map(a -> new WorkerWeightedAllocationInternal(
                        a.getWeight(),
                        a.getWorkerSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertWorkerSelectorToInternal(qs))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList()));
        }

        return null;
    }
}
