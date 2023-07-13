package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.LabelValueConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.ConditionalQueueSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.ConditionalWorkerSelectorAttachmentInternal;
import com.azure.communication.jobrouter.implementation.models.LabelOperatorInternal;
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
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.PassThroughQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.PassThroughWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.QueueWeightedAllocation;
import com.azure.communication.jobrouter.models.RouterQueueSelector;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.RuleEngineQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RuleEngineWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.StaticQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.StaticWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.WeightedAllocationQueueSelectorAttachment;
import com.azure.communication.jobrouter.models.WeightedAllocationWorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;
import com.azure.communication.jobrouter.models.WorkerWeightedAllocation;

import java.util.stream.Collectors;

/**
 * Converts queue and worker selectors to
 * {@link com.azure.communication.jobrouter.implementation.models.RouterQueueSelectorInternal} and
 * {@link com.azure.communication.jobrouter.implementation.models.RouterWorkerSelectorInternal}.
 */
public class LabelSelectorAdapter {

    public static RouterWorkerSelectorInternal convertWorkerSelectorToInternal(RouterWorkerSelector ws) {
        var workerSelector = new RouterWorkerSelectorInternal()
            .setKey(ws.getKey())
            .setValue(ws.getValue())
            .setExpedite(ws.isExpedite())
            .setExpiresAfterSeconds(ws.getExpiresAfterSeconds())
            .setLabelOperator(LabelOperatorInternal.fromString(ws.getLabelOperator().toString()));

        return workerSelector;
    }

    public static RouterWorkerSelector convertWorkerSelectorToPublic(RouterWorkerSelectorInternal ws) {
        var workerSelector = new RouterWorkerSelector()
            .setKey(ws.getKey())
            .setValue(LabelValueConstructorProxy.create(ws.getValue()))
            .setExpedite(ws.isExpedite())
            .setExpiresAfterSeconds(ws.getExpiresAfterSeconds())
            .setLabelOperator(LabelOperator.fromString(ws.getLabelOperator().toString()));

        return workerSelector;
    }

    public static RouterQueueSelectorInternal convertQueueSelectorToInternal(RouterQueueSelector qs) {
        var QueueSelector = new RouterQueueSelectorInternal()
            .setKey(qs.getKey())
            .setValue(qs.getValue())
            .setLabelOperator(LabelOperatorInternal.fromString(qs.getLabelOperator().toString()));

        return QueueSelector;
    }

    public static RouterQueueSelector convertQueueSelectorToPublic(RouterQueueSelectorInternal qs) {
        var QueueSelector = new RouterQueueSelector()
            .setKey(qs.getKey())
            .setValue(LabelValueConstructorProxy.create(qs.getValue()))
            .setLabelOperator(LabelOperator.fromString(qs.getLabelOperator().toString()));

        return QueueSelector;
    }

    public static QueueSelectorAttachmentInternal convertQueueSelectorAttachmentToInternal(QueueSelectorAttachment attachment) {
        if (attachment instanceof StaticQueueSelectorAttachment staticAttach) {
            return new StaticQueueSelectorAttachmentInternal().setQueueSelector(
                LabelSelectorAdapter.convertQueueSelectorToInternal(staticAttach.getQueueSelector()));
        } else if (attachment instanceof ConditionalQueueSelectorAttachment conditional) {
            return new ConditionalQueueSelectorAttachmentInternal()
                .setCondition(RouterRuleAdapter.convertRouterRuleToInternal(conditional.getCondition()))
                .setQueueSelectors(conditional.getQueueSelectors().stream()
                    .map(LabelSelectorAdapter::convertQueueSelectorToInternal).collect(Collectors.toList()));
        } else if (attachment instanceof PassThroughQueueSelectorAttachment passThrough) {
            return new PassThroughQueueSelectorAttachmentInternal()
                .setKey(passThrough.getKey())
                .setLabelOperator(LabelOperatorInternal.fromString(passThrough.getLabelOperator().toString()));
        } else if (attachment instanceof RuleEngineQueueSelectorAttachment rule) {
            return new RuleEngineQueueSelectorAttachmentInternal()
                .setRule(RouterRuleAdapter.convertRouterRuleToInternal(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationQueueSelectorAttachment weighted) {
            return new WeightedAllocationQueueSelectorAttachmentInternal()
                .setAllocations(weighted.getAllocations().stream()
                    .map(a -> new QueueWeightedAllocationInternal()
                        .setWeight(a.getWeight())
                        .setQueueSelectors(a.getQueueSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertQueueSelectorToInternal(qs))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList()));
        }

        return new QueueSelectorAttachmentInternal();
    }

    public static QueueSelectorAttachment convertQueueSelectorAttachmentToPublic(QueueSelectorAttachmentInternal attachment) {
        if (attachment instanceof StaticQueueSelectorAttachmentInternal staticAttach) {
            return new StaticQueueSelectorAttachment().setQueueSelector(
                LabelSelectorAdapter.convertQueueSelectorToPublic(staticAttach.getQueueSelector()));
        } else if (attachment instanceof ConditionalQueueSelectorAttachmentInternal conditional) {
            return new ConditionalQueueSelectorAttachment()
                .setCondition(RouterRuleAdapter.convertRouterRuleToPublic(conditional.getCondition()))
                .setQueueSelectors(conditional.getQueueSelectors().stream()
                    .map(LabelSelectorAdapter::convertQueueSelectorToPublic).collect(Collectors.toList()));
        } else if (attachment instanceof PassThroughQueueSelectorAttachmentInternal passThrough) {
            return new PassThroughQueueSelectorAttachment()
                .setKey(passThrough.getKey())
                .setLabelOperator(LabelOperator.fromString(passThrough.getLabelOperator().toString()));
        } else if (attachment instanceof RuleEngineQueueSelectorAttachmentInternal rule) {
            return new RuleEngineQueueSelectorAttachment()
                .setRule(RouterRuleAdapter.convertRouterRuleToPublic(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationQueueSelectorAttachmentInternal weighted) {
            return new WeightedAllocationQueueSelectorAttachment()
                .setAllocations(weighted.getAllocations().stream()
                    .map(a -> new QueueWeightedAllocation()
                        .setWeight(a.getWeight())
                        .setQueueSelectors(a.getQueueSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertQueueSelectorToPublic(qs))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList()));
        }

        return null;
    }

    public static WorkerSelectorAttachmentInternal convertWorkerSelectorAttachmentToInternal(WorkerSelectorAttachment attachment) {
        if (attachment instanceof StaticWorkerSelectorAttachment staticAttach) {
            return new StaticWorkerSelectorAttachmentInternal().setWorkerSelector(
                LabelSelectorAdapter.convertWorkerSelectorToInternal(staticAttach.getWorkerSelector()));
        } else if (attachment instanceof ConditionalWorkerSelectorAttachment conditional) {
            return new ConditionalWorkerSelectorAttachmentInternal()
                .setCondition(RouterRuleAdapter.convertRouterRuleToInternal(conditional.getCondition()))
                .setWorkerSelectors(conditional.getWorkerSelectors().stream()
                    .map(LabelSelectorAdapter::convertWorkerSelectorToInternal).collect(Collectors.toList()));
        } else if (attachment instanceof PassThroughWorkerSelectorAttachment passThrough) {
            return new PassThroughWorkerSelectorAttachmentInternal()
                .setExpiresAfterSeconds(passThrough.getExpiresAfterSeconds())
                .setKey(passThrough.getKey())
                .setLabelOperator(LabelOperatorInternal.fromString(passThrough.getLabelOperator().toString()));
        } else if (attachment instanceof RuleEngineWorkerSelectorAttachment rule) {
            return new RuleEngineWorkerSelectorAttachmentInternal()
                .setRule(RouterRuleAdapter.convertRouterRuleToInternal(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationWorkerSelectorAttachment weighted) {
            return new WeightedAllocationWorkerSelectorAttachmentInternal()
                .setAllocations(weighted.getAllocations().stream()
                    .map(a -> new WorkerWeightedAllocationInternal()
                        .setWeight(a.getWeight())
                        .setWorkerSelectors(a.getWorkerSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertWorkerSelectorToInternal(qs))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList()));
        }

        return new WorkerSelectorAttachmentInternal();
    }

    public static WorkerSelectorAttachment convertWorkerSelectorAttachmentToPublic(WorkerSelectorAttachmentInternal attachment) {
        if (attachment instanceof StaticWorkerSelectorAttachmentInternal staticAttach) {
            return new StaticWorkerSelectorAttachment().setWorkerSelector(
                LabelSelectorAdapter.convertWorkerSelectorToPublic(staticAttach.getWorkerSelector()));
        } else if (attachment instanceof ConditionalWorkerSelectorAttachmentInternal conditional) {
            return new ConditionalWorkerSelectorAttachment()
                .setCondition(RouterRuleAdapter.convertRouterRuleToPublic(conditional.getCondition()))
                .setWorkerSelectors(conditional.getWorkerSelectors().stream()
                    .map(LabelSelectorAdapter::convertWorkerSelectorToPublic).collect(Collectors.toList()));
        } else if (attachment instanceof PassThroughWorkerSelectorAttachmentInternal passThrough) {
            return new PassThroughWorkerSelectorAttachment()
                .setExpiresAfterSeconds(passThrough.getExpiresAfterSeconds())
                .setKey(passThrough.getKey())
                .setLabelOperator(LabelOperator.fromString(passThrough.getLabelOperator().toString()));
        } else if (attachment instanceof RuleEngineWorkerSelectorAttachmentInternal rule) {
            return new RuleEngineWorkerSelectorAttachment()
                .setRule(RouterRuleAdapter.convertRouterRuleToPublic(rule.getRule()));
        } else if (attachment instanceof WeightedAllocationWorkerSelectorAttachmentInternal weighted) {
            return new WeightedAllocationWorkerSelectorAttachment()
                .setAllocations(weighted.getAllocations().stream()
                    .map(a -> new WorkerWeightedAllocation()
                        .setWeight(a.getWeight())
                        .setWorkerSelectors(a.getWorkerSelectors().stream()
                            .map(qs -> LabelSelectorAdapter.convertWorkerSelectorToPublic(qs))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList()));
        }

        return null;
    }
}
