// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * EvaluatorId enumeration.
 */
public class EvaluatorId extends ExpandableStringEnum<EvaluatorId> {

    /**
     * Creates a new instance of EvaluatorId value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public EvaluatorId() {
    }

    /**
     * Creates or finds a EvaluatorId from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding EvaluatorId.
     */
    public static EvaluatorId fromString(String name) {
        return fromString(name, EvaluatorId.class);
    }

    /**
     * EvaluatorId for relevance.
     */
    public static final EvaluatorId RELEVANCE = fromString("azureai://built-in/evaluators/relevance");

    /**
     * EvaluatorId for hate_unfairness.
     */
    public static final EvaluatorId HATE_UNFAIRNESS = fromString("azureai://built-in/evaluators/hate_unfairness");

    /**
     * EvaluatorId for violence.
     */
    public static final EvaluatorId VIOLENCE = fromString("azureai://built-in/evaluators/violence");

    /**
     * EvaluatorId for groundedness.
     */
    public static final EvaluatorId GROUNDEDNESS = fromString("azureai://built-in/evaluators/groundedness");

    /**
     * EvaluatorId for groundedness_pro.
     */
    public static final EvaluatorId GROUNDEDNESS_PRO = fromString("azureai://built-in/evaluators/groundedness_pro");

    /**
     * EvaluatorId for blue_score.
     */
    public static final EvaluatorId BLUE_SCORE = fromString("azureai://built-in/evaluators/blue_score");

    /**
     * EvaluatorId for code_vulnerability.
     */
    public static final EvaluatorId CODE_VULNERABILITY = fromString("azureai://built-in/evaluators/code_vulnerability");

    /**
     * EvaluatorId for coherence.
     */
    public static final EvaluatorId COHERENCE = fromString("azureai://built-in/evaluators/coherence");

    /**
     * EvaluatorId for content_safety.
     */
    public static final EvaluatorId CONTENT_SAFETY = fromString("azureai://built-in/evaluators/content_safety");

    /**
     * EvaluatorId for f1_score.
     */
    public static final EvaluatorId F1_SCORE = fromString("azureai://built-in/evaluators/f1_score");

    /**
     * EvaluatorId for fluency.
     */
    public static final EvaluatorId FLUENCY = fromString("azureai://built-in/evaluators/fluency");

    /**
     * EvaluatorId for gleu_score.
     */
    public static final EvaluatorId GLEU_SCORE = fromString("azureai://built-in/evaluators/gleu_score");

    /**
     * EvaluatorId for indirect_attack.
     */
    public static final EvaluatorId INDIRECT_ATTACK = fromString("azureai://built-in/evaluators/indirect_attack");

    /**
     * EvaluatorId for intent_resolution.
     */
    // public static final EvaluatorId INTENT_RESOLUTION = fromString("azureai://built-in/evaluators/intent_resolution");

    /**
     * EvaluatorId for meteor_score.
     */
    public static final EvaluatorId METEOR_SCORE = fromString("azureai://built-in/evaluators/meteor_score");

    /**
     * EvaluatorId for protected_material.
     */
    public static final EvaluatorId PROTECTED_MATERIAL = fromString("azureai://built-in/evaluators/protected_material");

    /**
     * EvaluatorId for retrieval.
     */
    public static final EvaluatorId RETRIEVAL = fromString("azureai://built-in/evaluators/retrieval");

    /**
     * EvaluatorId for rouge_score.
     */
    public static final EvaluatorId ROUGE_SCORE = fromString("azureai://built-in/evaluators/rouge_score");

    /**
     * EvaluatorId for self_harm.
     */
    public static final EvaluatorId SELF_HARM = fromString("azureai://built-in/evaluators/self_harm");

    /**
     * EvaluatorId for sexual.
     */
    public static final EvaluatorId SEXUAL = fromString("azureai://built-in/evaluators/sexual");

    /**
     * EvaluatorId for similarity_score.
     */
    public static final EvaluatorId SIMILARITY_SCORE = fromString("azureai://built-in/evaluators/similarity_score");

    /**
     * EvaluatorId for task_adherence.
     */
    // public static final EvaluatorId TASK_ADHERENCE = fromString("azureai://built-in/evaluators/task_adherence");

    /**
     * EvaluatorId for tool_call_accuracy.
     */
    // public static final EvaluatorId TOOL_CALL_ACCURACY = fromString("azureai://built-in/evaluators/tool_call_accuracy");

    /**
     * EvaluatorId for ungrounded_attributes.
     */
    public static final EvaluatorId UNGROUNDED_ATTRIBUTES
        = fromString("azureai://built-in/evaluators/ungrounded_attributes");

    /**
     * EvaluatorId for response_completeness.
     */
    public static final EvaluatorId RESPONSE_COMPLETENESS
        = fromString("azureai://built-in/evaluators/response_completeness");
}
