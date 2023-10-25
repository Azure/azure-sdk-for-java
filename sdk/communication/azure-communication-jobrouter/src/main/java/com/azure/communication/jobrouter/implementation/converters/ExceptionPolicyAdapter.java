// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.ExceptionPolicyConstructorProxy;
import com.azure.communication.jobrouter.implementation.accesshelpers.LabelValueConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.CancelExceptionActionInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionActionInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionPolicyItemInternal;
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
import com.azure.communication.jobrouter.models.ExceptionPolicyItem;
import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.communication.jobrouter.models.ExceptionTrigger;
import com.azure.communication.jobrouter.models.ManualReclassifyExceptionAction;
import com.azure.communication.jobrouter.models.QueueLengthExceptionTrigger;
import com.azure.communication.jobrouter.models.ReclassifyExceptionAction;
import com.azure.communication.jobrouter.models.UpdateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.WaitTimeExceptionTrigger;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.ETag;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Exception Policy to {@link ExceptionPolicy}.
 */
public class ExceptionPolicyAdapter {
    /**
     * Converts {@link CreateExceptionPolicyOptions} to {@link ExceptionPolicyInternal}.
     * @param createExceptionPolicyOptions
     * @return exception policy.
     */
    public static ExceptionPolicyInternal convertCreateOptionsToExceptionPolicy(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        return new ExceptionPolicyInternal()
            .setName(createExceptionPolicyOptions.getName())
            .setExceptionRules(convertExceptionRulesToInternal(createExceptionPolicyOptions.getExceptionRules()));
    }

    /**
     * Converts {@link UpdateExceptionPolicyOptions} to {@link ExceptionPolicyInternal}.
     * @param updateExceptionPolicyOptions
     * @return exception policy.
     */
    public static ExceptionPolicyInternal convertUpdateOptionsToExceptionPolicy(UpdateExceptionPolicyOptions updateExceptionPolicyOptions) {
        return new ExceptionPolicyInternal()
            .setName(updateExceptionPolicyOptions.getName())
            .setExceptionRules(convertExceptionRulesToInternal(updateExceptionPolicyOptions.getExceptionRules()));
    }

    public static PagedFlux<ExceptionPolicyItem> convertPagedFluxToPublic(PagedFlux<ExceptionPolicyItemInternal> internalPagedFlux) {
        final Function<PagedResponse<ExceptionPolicyItemInternal>, PagedResponse<ExceptionPolicyItem>> responseMapper
            = internalResponse -> new PagedResponseBase<Void, ExceptionPolicyItem>(internalResponse.getRequest(),
            internalResponse.getStatusCode(),
            internalResponse.getHeaders(),
            internalResponse.getValue()
                .stream()
                .map(internal -> new ExceptionPolicyItem()
                    .setExceptionPolicy(ExceptionPolicyConstructorProxy.create(internal.getExceptionPolicy()))
                    .setEtag(new ETag(internal.getEtag())))
                .collect(Collectors.toList()),
            internalResponse.getContinuationToken(),
            null);

        return PagedFlux.create(() -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<ExceptionPolicyItemInternal>> flux = (continuationToken == null)
                ? internalPagedFlux.byPage()
                : internalPagedFlux.byPage(continuationToken);
            return flux.map(responseMapper);
        });
    }

    public static Map<String, ExceptionRuleInternal> convertExceptionRulesToInternal(Map<String, ExceptionRule> rules) {
        return rules != null ? rules.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ExceptionRuleInternal()
                .setTrigger(convertExceptionTriggerToInternal(entry.getValue().getTrigger()))
                .setActions(entry.getValue().getActions().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                        actions -> convertExceptionActionToInternal(actions.getValue()))))))
            : new HashMap<>();
    }

    public static Map<String, ExceptionRule> convertExceptionRulesToPublic(Map<String, ExceptionRuleInternal> rules) {
        return rules != null ? rules.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                entry -> new ExceptionRule(convertExceptionTriggerToPublic(entry.getValue().getTrigger()),
                entry.getValue().getActions().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                        actions -> convertExceptionActionToPublic(actions.getValue()))))))
            : new HashMap<>();
    }

    public static ExceptionTriggerInternal convertExceptionTriggerToInternal(ExceptionTrigger trigger) {
        if (trigger instanceof QueueLengthExceptionTrigger) {
            QueueLengthExceptionTrigger queueLength = (QueueLengthExceptionTrigger) trigger;
            return new QueueLengthExceptionTriggerInternal().setThreshold(queueLength.getThreshold());
        } else if (trigger instanceof WaitTimeExceptionTrigger) {
            WaitTimeExceptionTrigger waitTime = (WaitTimeExceptionTrigger) trigger;
            return new WaitTimeExceptionTriggerInternal().setThresholdSeconds(waitTime.getThresholdSeconds());
        }

        return null;
    }

    public static ExceptionTrigger convertExceptionTriggerToPublic(ExceptionTriggerInternal trigger) {
        if (trigger instanceof QueueLengthExceptionTriggerInternal) {
            QueueLengthExceptionTriggerInternal queueLength = (QueueLengthExceptionTriggerInternal) trigger;
            return new QueueLengthExceptionTrigger(queueLength.getThreshold());
        } else if (trigger instanceof WaitTimeExceptionTriggerInternal) {
            WaitTimeExceptionTriggerInternal waitTime = (WaitTimeExceptionTriggerInternal) trigger;
            return new WaitTimeExceptionTrigger(waitTime.getThresholdSeconds());
        }

        return null;
    }

    public static ExceptionActionInternal convertExceptionActionToInternal(ExceptionAction action) {
        if (action instanceof CancelExceptionAction) {
            CancelExceptionAction cancel = (CancelExceptionAction) action;
            return new CancelExceptionActionInternal()
                .setNote(cancel.getNote())
                .setDispositionCode(cancel.getDispositionCode());
        } else if (action instanceof ManualReclassifyExceptionAction) {
            ManualReclassifyExceptionAction manualReclassify = (ManualReclassifyExceptionAction) action;
            return new ManualReclassifyExceptionActionInternal()
                .setPriority(manualReclassify.getPriority())
                .setQueueId(manualReclassify.getQueueId())
                .setWorkerSelectors(manualReclassify.getWorkerSelectors().stream()
                    .map(LabelSelectorAdapter::convertWorkerSelectorToInternal).collect(Collectors.toList()));
        } else if (action instanceof ReclassifyExceptionAction) {
            ReclassifyExceptionAction reclassify = (ReclassifyExceptionAction) action;
            return new ReclassifyExceptionActionInternal()
                .setClassificationPolicyId(reclassify.getClassificationPolicyId())
                .setLabelsToUpsert(reclassify.getLabelsToUpsert().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())));
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
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> LabelValueConstructorProxy.create(entry.getValue()))));
        }

        return null;
    }
}
