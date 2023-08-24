// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.RouterJobConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.JobMatchModeTypeInternal;
import com.azure.communication.jobrouter.implementation.models.JobMatchingModeInternal;
import com.azure.communication.jobrouter.implementation.models.RouterJobAssignmentInternal;
import com.azure.communication.jobrouter.implementation.models.RouterJobInternal;
import com.azure.communication.jobrouter.implementation.models.RouterJobItemInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerSelectorInternal;
import com.azure.communication.jobrouter.implementation.models.ScheduleAndSuspendModeInternal;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateJobWithClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.QueueAndMatchMode;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobAssignment;
import com.azure.communication.jobrouter.models.RouterJobItem;
import com.azure.communication.jobrouter.models.RouterJobMatchModeType;
import com.azure.communication.jobrouter.models.RouterJobMatchingMode;
import com.azure.communication.jobrouter.models.RouterJobNote;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.ScheduleAndSuspendMode;
import com.azure.communication.jobrouter.models.SuspendMode;
import com.azure.communication.jobrouter.models.UpdateJobOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.ETag;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.communication.jobrouter.implementation.converters.LabelSelectorAdapter.convertWorkerSelectorToInternal;

/**
 * Converts request options for create and update Job to {@link RouterJob}.
 */
public class JobAdapter {

    /**
     * Converts {@link CreateJobOptions} to {@link RouterJobInternal}.
     * @param createJobOptions Container with options to create {@link RouterJobInternal}
     * @return RouterJob
     */
    public static RouterJobInternal convertCreateJobOptionsToRouterJob(CreateJobOptions createJobOptions) {
        Map<String, LabelValue> labelValueMap = createJobOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : null;
        Map<String, LabelValue> tagValueMap = createJobOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : null;
        List<RouterWorkerSelector> workerSelectors = createJobOptions.getRequestedWorkerSelectors();
        List<RouterWorkerSelectorInternal> workerSelectorsInternal = workerSelectors != null ? workerSelectors.stream()
            .map(ws -> convertWorkerSelectorToInternal(ws))
            .collect(Collectors.toList()) : null;
        List<RouterJobNote> jobNotes = createJobOptions.getNotes();
        Map<String, String> notes = jobNotes != null ? jobNotes.stream()
            .collect(Collectors.toMap(note -> note.getAddedAt().toString(), note -> note.getMessage())) : null;

        return new RouterJobInternal()
            .setChannelId(createJobOptions.getChannelId())
            .setChannelReference(createJobOptions.getChannelReference())
            .setQueueId(createJobOptions.getQueueId())
            .setLabels(labels)
            .setNotes(notes)
            .setPriority(createJobOptions.getPriority())
            .setDispositionCode(createJobOptions.getDispositionCode())
            .setRequestedWorkerSelectors(workerSelectorsInternal)
            .setTags(tags)
            .setMatchingMode(convertMatchingModeToInternal(createJobOptions.getMatchingMode()));
    }

    /**
     * Converts {@link CreateJobWithClassificationPolicyOptions} to {@link RouterJobInternal}.
     * @param createJobWithClassificationPolicyOptions Container with options to create {@link RouterJobInternal}
     * @return RouterJob
     */
    public static RouterJobInternal convertCreateJobWithClassificationPolicyOptionsToRouterJob(CreateJobWithClassificationPolicyOptions createJobWithClassificationPolicyOptions) {
        Map<String, LabelValue> labelValueMap = createJobWithClassificationPolicyOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : null;
        Map<String, LabelValue> tagValueMap = createJobWithClassificationPolicyOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : null;
        List<RouterWorkerSelector> workerSelectors = createJobWithClassificationPolicyOptions.getRequestedWorkerSelectors();
        List<RouterWorkerSelectorInternal> workerSelectorsInternal = workerSelectors != null ? workerSelectors.stream()
            .map(ws -> convertWorkerSelectorToInternal(ws))
            .collect(Collectors.toList()) : null;
        List<RouterJobNote> jobNotes = createJobWithClassificationPolicyOptions.getNotes();
        Map<String, String> notes = jobNotes != null ? jobNotes.stream()
            .collect(Collectors.toMap(note -> note.getAddedAt().toString(), note -> note.getMessage())) : null;

        return new RouterJobInternal()
            .setChannelId(createJobWithClassificationPolicyOptions.getChannelId())
            .setChannelReference(createJobWithClassificationPolicyOptions.getChannelReference())
            .setQueueId(createJobWithClassificationPolicyOptions.getQueueId())
            .setLabels(labels)
            .setNotes(notes)
            .setPriority(createJobWithClassificationPolicyOptions.getPriority())
            .setClassificationPolicyId(createJobWithClassificationPolicyOptions.getClassificationPolicyId())
            .setDispositionCode(createJobWithClassificationPolicyOptions.getDispositionCode())
            .setRequestedWorkerSelectors(workerSelectorsInternal)
            .setTags(tags)
            .setMatchingMode(convertMatchingModeToInternal(createJobWithClassificationPolicyOptions.getMatchingMode()));
    }

    /**
     * Converts {@link UpdateJobOptions} to {@link RouterJobInternal}.
     * @param updateJobOptions Container with options to update {@link RouterJobInternal}
     * @return RouterJob
     */
    public static RouterJobInternal convertUpdateJobOptionsToRouterJob(UpdateJobOptions updateJobOptions) {
        Map<String, LabelValue> labelValueMap = updateJobOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        Map<String, LabelValue> tagValueMap = updateJobOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        List<RouterWorkerSelector> workerSelectors = updateJobOptions.getRequestedWorkerSelectors();
        List<RouterWorkerSelectorInternal> workerSelectorsInternal = workerSelectors != null ? workerSelectors.stream()
            .map(ws -> LabelSelectorAdapter.convertWorkerSelectorToInternal(ws))
            .collect(Collectors.toList()) : new ArrayList<>();
        List<RouterJobNote> jobNotes = updateJobOptions.getNotes();
        Map<String, String> notes = jobNotes != null ? jobNotes.stream()
            .collect(Collectors.toMap(note -> note.getAddedAt().toString(), note -> note.getMessage())) : new HashMap<>();

        return new RouterJobInternal()
            .setChannelId(updateJobOptions.getChannelId())
            .setChannelReference(updateJobOptions.getChannelReference())
            .setLabels(labels)
            .setNotes(notes)
            .setPriority(updateJobOptions.getPriority())
            .setClassificationPolicyId(updateJobOptions.getClassificationPolicyId())
            .setDispositionCode(updateJobOptions.getDispositionCode())
            .setClassificationPolicyId(updateJobOptions.getClassificationPolicyId())
            .setRequestedWorkerSelectors(workerSelectorsInternal)
            .setTags(tags)
            .setMatchingMode(convertMatchingModeToInternal(updateJobOptions.getMatchingMode()));
    }

    public static PagedFlux<RouterJobItem> convertPagedFluxToPublic(PagedFlux<RouterJobItemInternal> internalPagedFlux) {
        final Function<PagedResponse<RouterJobItemInternal>, PagedResponse<RouterJobItem>> responseMapper
            = internalResponse -> new PagedResponseBase<Void, RouterJobItem>(internalResponse.getRequest(),
            internalResponse.getStatusCode(),
            internalResponse.getHeaders(),
            internalResponse.getValue()
                .stream()
                .map(internal -> new RouterJobItem()
                    .setJob(RouterJobConstructorProxy.create(internal.getJob()))
                    .setEtag(new ETag(internal.getEtag())))
                .collect(Collectors.toList()),
            internalResponse.getContinuationToken(),
            null);

        return PagedFlux.create(() -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<RouterJobItemInternal>> flux = (continuationToken == null)
                ? internalPagedFlux.byPage()
                : internalPagedFlux.byPage(continuationToken);
            return flux.map(responseMapper);
        });
    }

    public static RouterJobAssignment convertJobAssignmentToPublic(RouterJobAssignmentInternal internal) {
        return new RouterJobAssignment()
            .setAssignedAt(internal.getAssignedAt())
            .setClosedAt(internal.getClosedAt())
            .setAssignmentId(internal.getAssignmentId())
            .setWorkerId(internal.getWorkerId())
            .setCompletedAt(internal.getCompletedAt());
    }

    public static RouterJobMatchingMode convertMatchingModeToPublic(JobMatchingModeInternal internal) {
        if (internal != null) {
            if (internal.getModeType() == JobMatchModeTypeInternal.SUSPEND_MODE) {
                return new RouterJobMatchingMode(new SuspendMode());
            } else if (internal.getModeType() == JobMatchModeTypeInternal.SCHEDULE_AND_SUSPEND_MODE) {
                return new RouterJobMatchingMode(new ScheduleAndSuspendMode(internal.getScheduleAndSuspendMode().getScheduleAt()));
            }
        }

        return new RouterJobMatchingMode(new QueueAndMatchMode());
    }

    public static JobMatchingModeInternal convertMatchingModeToInternal(RouterJobMatchingMode matchingMode) {
        JobMatchingModeInternal matchingModeInternal = new JobMatchingModeInternal()
            .setModeType(JobMatchModeTypeInternal.fromString(matchingMode != null
                ? matchingMode.getModeType().toString() : RouterJobMatchModeType.QUEUE_AND_MATCH_MODE.toString()));
        if (matchingMode == null || matchingMode.getModeType() == RouterJobMatchModeType.QUEUE_AND_MATCH_MODE) {
            matchingModeInternal.setQueueAndMatchMode(new QueueAndMatchMode());
        } else if (matchingMode.getModeType() == RouterJobMatchModeType.SUSPEND_MODE) {
            matchingModeInternal.setSuspendMode(new SuspendMode());
        } else if (matchingMode.getModeType() == RouterJobMatchModeType.SCHEDULE_AND_SUSPEND_MODE) {
            matchingModeInternal.setScheduleAndSuspendMode(new ScheduleAndSuspendModeInternal()
                .setScheduleAt(matchingMode.getScheduleAndSuspendMode().getScheduleAt()));
        }

        return matchingModeInternal;
    }
}
