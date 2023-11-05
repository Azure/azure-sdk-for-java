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
import com.azure.communication.jobrouter.implementation.models.WaitTimeExceptionTriggerInternal;
import com.azure.communication.jobrouter.models.CancelExceptionAction;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.ExceptionAction;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.communication.jobrouter.models.ExceptionTrigger;
import com.azure.communication.jobrouter.models.ManualReclassifyExceptionAction;
import com.azure.communication.jobrouter.models.QueueLengthExceptionTrigger;
import com.azure.communication.jobrouter.models.WaitTimeExceptionTrigger;

import java.util.List;
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
}
