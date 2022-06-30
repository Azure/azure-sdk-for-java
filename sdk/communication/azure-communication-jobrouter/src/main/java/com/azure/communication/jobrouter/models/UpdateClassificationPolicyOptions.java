package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ClassificationPolicyOptions;
import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.implementation.models.RouterRule;
import com.azure.communication.jobrouter.implementation.models.WorkerSelectorAttachment;
import com.azure.core.annotation.Fluent;

import java.util.List;

/** Request options for Update ClassificationPolicy.
 * ClassificationPolicy: A container for the rules that govern how jobs are classified.
 */
@Fluent
public class UpdateClassificationPolicyOptions extends ClassificationPolicyOptions {
    /**
     * Constructor for UpdateClassificationPolicyOptions
     * @param id Unique identifier of this policy.
     */
    public UpdateClassificationPolicyOptions(String id) {
        this.id = id;
    }

    /**
     * Sets name.
     * @param name Friendly name of this policy.
     * @return this
     */
    public UpdateClassificationPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets fallbackQueueId.
     * @param fallbackQueueId  The fallback queue to select if the queue selector doesn't find a match.
     * @return this
     */
    public UpdateClassificationPolicyOptions setFallbackQueueId(String fallbackQueueId) {
        this.fallbackQueueId = fallbackQueueId;
        return this;
    }

    /**
     * Sets queueSelectors.
     * @param queueSelectors The queue selectors to resolve a queue for a given job.
     * @return this
     */
    public UpdateClassificationPolicyOptions setQueueSelectors(List<QueueSelectorAttachment> queueSelectors) {
        this.queueSelectors = queueSelectors;
        return this;
    }

    /**
     * Sets prioritizationRule.
     * @param prioritizationRule A rule of one of the following types:
     *   StaticRule:  A rule providing static rules that always return the same result, regardless of input.
     *   DirectMapRule:  A rule that return the same labels as the input labels.
     *   ExpressionRule: A rule providing inline expression rules.
     *   AzureFunctionRule: A rule providing a binding to an HTTP Triggered Azure Function.
     * @return this
     */
    public UpdateClassificationPolicyOptions setPrioritizationRule(RouterRule prioritizationRule) {
        this.prioritizationRule = prioritizationRule;
        return this;
    }

    /**
     * Sets workerSelectors.
     * @param workerSelectors The worker label selectors to attach to a given job.
     * @return this
     */
    public UpdateClassificationPolicyOptions setWorkerSelectors(List<WorkerSelectorAttachment> workerSelectors) {
        this.workerSelectors = workerSelectors;
        return this;
    }
}
