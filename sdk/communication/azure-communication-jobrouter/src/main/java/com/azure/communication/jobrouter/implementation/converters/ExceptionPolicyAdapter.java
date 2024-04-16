// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

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
            .setExceptionRules(createExceptionPolicyOptions != null
                ? createExceptionPolicyOptions.getExceptionRules()
                .stream().map(rule -> convertExceptionRule(rule))
                .collect(Collectors.toList())
            : null);
    }

    private static ExceptionTriggerInternal convertExceptionTrigger(ExceptionTrigger exceptionTrigger) {
        ExceptionTriggerInternal exceptionTriggerInternal = null;
        if (exceptionTrigger.getClass() == QueueLengthExceptionTrigger.class) {
            QueueLengthExceptionTrigger queueLengthExceptionTrigger = (QueueLengthExceptionTrigger) exceptionTrigger;
            exceptionTriggerInternal = new QueueLengthExceptionTriggerInternal().setThreshold(queueLengthExceptionTrigger.getThreshold());
        } else if (exceptionTrigger.getClass() == WaitTimeExceptionTrigger.class) {
            WaitTimeExceptionTrigger waitTimeExceptionTrigger = (WaitTimeExceptionTrigger) exceptionTrigger;
            exceptionTriggerInternal = new WaitTimeExceptionTriggerInternal().setThresholdSeconds(waitTimeExceptionTrigger.getThreshold().getSeconds());
        }
        return exceptionTriggerInternal;
    }

    private static ExceptionActionInternal convertExceptionAction(ExceptionAction exceptionAction) {
        ExceptionActionInternal exceptionActionInternal = null;
        if (exceptionAction.getClass() == CancelExceptionAction.class) {
            CancelExceptionAction cancelExceptionAction = (CancelExceptionAction) exceptionAction;
            exceptionActionInternal = new CancelExceptionActionInternal()
                .setNote(cancelExceptionAction.getNote())
                .setId(cancelExceptionAction.getId())
                .setDispositionCode(cancelExceptionAction.getDispositionCode());
        } else if (exceptionAction.getClass() == ManualReclassifyExceptionAction.class) {
            ManualReclassifyExceptionAction manualReclassifyExceptionAction = (ManualReclassifyExceptionAction) exceptionAction;
            exceptionActionInternal = new ManualReclassifyExceptionActionInternal()
                .setPriority(manualReclassifyExceptionAction.getPriority())
                .setQueueId(manualReclassifyExceptionAction.getQueueId())
                .setId(manualReclassifyExceptionAction.getId())
                .setWorkerSelectors(manualReclassifyExceptionAction.getWorkerSelectors() != null
                    ? manualReclassifyExceptionAction.getWorkerSelectors()
                        .stream()
                        .map(LabelSelectorAdapter::convertWorkerSelectorToInternal)
                        .collect(Collectors.toList())
                    : null);
        } else if (exceptionAction.getClass() == ReclassifyExceptionAction.class) {
            ReclassifyExceptionAction reclassifyExceptionAction = (ReclassifyExceptionAction) exceptionAction;
            exceptionActionInternal = new ReclassifyExceptionActionInternal()
                .setClassificationPolicyId(reclassifyExceptionAction.getClassificationPolicyId())
                .setId(reclassifyExceptionAction.getId())
                .setLabelsToUpsert(reclassifyExceptionAction.getLabelsToUpsert() != null
                    ? reclassifyExceptionAction.getLabelsToUpsert().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> RouterValueAdapter.getValue(v.getValue())))
                    : null);
        }
        return exceptionActionInternal;
    }

    private static ExceptionRuleInternal convertExceptionRule(ExceptionRule exceptionRule) {
        String id = exceptionRule.getId();
        ExceptionTriggerInternal exceptionTriggerInternal = convertExceptionTrigger(exceptionRule.getTrigger());
        List<ExceptionActionInternal> exceptionActionInternalList = exceptionRule.getActions() != null
            ? exceptionRule.getActions()
                .stream()
                .map(action -> convertExceptionAction(action))
                .collect(Collectors.toList())
            : null;
        return new ExceptionRuleInternal().setId(id).setTrigger(exceptionTriggerInternal).setActions(exceptionActionInternalList);
    }

}
