// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.RouterValueConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.CancelExceptionActionInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionActionInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionRuleInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionTriggerInternal;
import com.azure.communication.jobrouter.implementation.models.ManualReclassifyExceptionActionInternal;
import com.azure.communication.jobrouter.implementation.models.QueueLengthExceptionTriggerInternal;
import com.azure.communication.jobrouter.implementation.models.ReclassifyExceptionActionInternal;
import com.azure.communication.jobrouter.implementation.models.WaitTimeExceptionTriggerInternal;
import com.azure.communication.jobrouter.models.CancelExceptionAction;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.ExceptionAction;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.communication.jobrouter.models.ExceptionTrigger;
import com.azure.communication.jobrouter.models.ManualReclassifyExceptionAction;
import com.azure.communication.jobrouter.models.QueueLengthExceptionTrigger;
import com.azure.communication.jobrouter.models.ReclassifyExceptionAction;
import com.azure.communication.jobrouter.models.WaitTimeExceptionTrigger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Exception Policy to {@link ExceptionPolicy}.
 */
public class ExceptionPolicyAdapter {
    /**
     * Converts {@link CreateExceptionPolicyOptions} to {@link ExceptionPolicy}.
     * @param createExceptionPolicyOptions
     * @return exception policy.
     */
    public static ExceptionPolicyInternal convertCreateOptionsToExceptionPolicy(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        return new ExceptionPolicyInternal()
            .setName(createExceptionPolicyOptions.getName())
            .setExceptionRules(createExceptionPolicyOptions.getExceptionRules()
                .stream().map(rule -> convertExceptionRule(rule))
                .collect(Collectors.toList()));
    }

    private static ExceptionTriggerInternal convertExceptionTrigger(ExceptionTrigger exceptionTrigger) {
        ExceptionTriggerInternal exceptionTriggerInternal = null;
        if (exceptionTrigger.getClass() == QueueLengthExceptionTrigger.class) {
            QueueLengthExceptionTrigger queueLengthExceptionTrigger = (QueueLengthExceptionTrigger) exceptionTrigger;
            exceptionTriggerInternal = new QueueLengthExceptionTriggerInternal(queueLengthExceptionTrigger.getThreshold());
        } else if (exceptionTrigger.getClass() == WaitTimeExceptionTrigger.class) {
            WaitTimeExceptionTrigger waitTimeExceptionTrigger = (WaitTimeExceptionTrigger) exceptionTrigger;
            exceptionTriggerInternal = new WaitTimeExceptionTriggerInternal(waitTimeExceptionTrigger.getThreshold().getSeconds());
        }
        return exceptionTriggerInternal;
    }

    private static ExceptionActionInternal convertExceptionAction(ExceptionAction exceptionAction) {
        ExceptionActionInternal exceptionActionInternal = null;
        if (exceptionAction.getClass() == CancelExceptionAction.class) {
            CancelExceptionAction cancelExceptionAction = (CancelExceptionAction) exceptionAction;
            exceptionActionInternal = new CancelExceptionActionInternal()
                .setNote(cancelExceptionAction.getNote())
                .setDispositionCode(cancelExceptionAction.getDispositionCode());
        } else if (exceptionAction.getClass() == ManualReclassifyExceptionAction.class) {
            ManualReclassifyExceptionAction manualReclassifyExceptionAction = (ManualReclassifyExceptionAction) exceptionAction;
            exceptionActionInternal = new ManualReclassifyExceptionActionInternal()
                .setPriority(manualReclassifyExceptionAction.getPriority())
                .setQueueId(manualReclassifyExceptionAction.getQueueId())
                .setWorkerSelectors(manualReclassifyExceptionAction.getWorkerSelectors()
                    .stream()
                    .map(ws -> LabelSelectorAdapter.convertWorkerSelectorToInternal(ws))
                    .collect(Collectors.toList()));
        }
        return exceptionActionInternal;
    }

    private static ExceptionRuleInternal convertExceptionRule(ExceptionRule exceptionRule) {
        String id = exceptionRule.getId();
        ExceptionTriggerInternal exceptionTriggerInternal = convertExceptionTrigger(exceptionRule.getTrigger());
        List<ExceptionActionInternal> exceptionActionInternalList = exceptionRule.getActions()
            .stream()
            .map(action -> convertExceptionAction(action))
            .collect(Collectors.toList());
        return new ExceptionRuleInternal(id, exceptionTriggerInternal, exceptionActionInternalList);
    }

    public static ExceptionTrigger convertExceptionTriggerToPublic(ExceptionTriggerInternal trigger) {
        if (trigger instanceof QueueLengthExceptionTriggerInternal) {
            QueueLengthExceptionTriggerInternal queueLength = (QueueLengthExceptionTriggerInternal) trigger;
            return new QueueLengthExceptionTrigger(queueLength.getThreshold());
        } else if (trigger instanceof WaitTimeExceptionTriggerInternal) {
            WaitTimeExceptionTriggerInternal waitTime = (WaitTimeExceptionTriggerInternal) trigger;
            return new WaitTimeExceptionTrigger(Duration.ofSeconds((long) waitTime.getThresholdSeconds()));
        }

        return null;
    }

    public static ExceptionAction convertExceptionActionToPublic(ExceptionActionInternal action) {
        if (action instanceof CancelExceptionActionInternal) {
            CancelExceptionActionInternal cancel = (CancelExceptionActionInternal) action;
            return new CancelExceptionAction()
                .setNote(cancel.getNote())
                .setDispositionCode(cancel.getDispositionCode());
        } else if (action instanceof ManualReclassifyExceptionActionInternal) {
            ManualReclassifyExceptionActionInternal manualReclassify = (ManualReclassifyExceptionActionInternal) action;
            return new ManualReclassifyExceptionAction()
                .setPriority(manualReclassify.getPriority())
                .setQueueId(manualReclassify.getQueueId())
                .setWorkerSelectors(manualReclassify.getWorkerSelectors().stream()
                    .map(LabelSelectorAdapter::convertWorkerSelectorToPublic).collect(Collectors.toList()));
        } else if (action instanceof ReclassifyExceptionActionInternal) {
            ReclassifyExceptionActionInternal reclassify = (ReclassifyExceptionActionInternal) action;
            return new ReclassifyExceptionAction()
                .setClassificationPolicyId(reclassify.getClassificationPolicyId())
                .setLabelsToUpsert(reclassify.getLabelsToUpsert().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> RouterValueConstructorProxy.create(entry.getValue()))));
        }

        return null;
    }

    public static List<ExceptionRule> convertExceptionRulesToPublic(List<ExceptionRuleInternal> rules) {
        return rules != null ? rules.stream()
            .map(rule -> {
                ExceptionTrigger trigger = convertExceptionTriggerToPublic(rule.getTrigger());
                List<ExceptionAction> actions = rule.getActions().stream()
                    .map(action -> convertExceptionActionToPublic(action))
                    .collect(Collectors.toList());
                return new ExceptionRule(rule.getId(), trigger, actions);
            })
            .collect(Collectors.toList()) : new ArrayList<ExceptionRule>();
    }

    public static ExceptionPolicyInternal convertExceptionPolicyToInternal(ExceptionPolicy exceptionPolicy) {
        return new ExceptionPolicyInternal()
            .setEtag(exceptionPolicy.getEtag())
            .setId(exceptionPolicy.getId())
            .setName(exceptionPolicy.getName())
            .setExceptionRules(exceptionPolicy.getExceptionRules().stream()
                .map(exceptionRule -> convertExceptionRule(exceptionRule))
                .collect(Collectors.toList())
            );
    }
}
