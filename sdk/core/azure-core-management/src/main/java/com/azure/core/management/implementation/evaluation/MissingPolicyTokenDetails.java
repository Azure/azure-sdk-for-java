// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.evaluation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Internal representation of the external evaluation details returned in a policy-denied response.
 */
public final class MissingPolicyTokenDetails {
    private static final ClientLogger LOGGER = new ClientLogger(MissingPolicyTokenDetails.class);

    private static final String ERROR = "error";
    private static final String CODE = "code";
    private static final String REQUEST_DISALLOWED_BY_POLICY = "RequestDisallowedByPolicy";
    private static final String ADDITIONAL_INFO = "additionalInfo";
    private static final String TYPE = "type";
    private static final String POLICY_VIOLATION = "PolicyViolation";
    private static final String INFO = "info";
    private static final String EVALUATION_DETAILS = "evaluationDetails";
    private static final String MISSING_POLICY_TOKEN_DETAILS = "missingPolicyTokenDetails";
    private static final String SHOULD_DENY = "shouldDeny";
    private static final String IS_CHANGE_REFERENCE_REQUIRED = "isChangeReferenceRequired";

    private final boolean changeReferenceRequired;

    private MissingPolicyTokenDetails(boolean changeReferenceRequired) {
        this.changeReferenceRequired = changeReferenceRequired;
    }

    /**
     * Whether the service requires a change-safety reference to acquire the policy token.
     *
     * @return whether a change reference is required.
     */
    public boolean isChangeReferenceRequired() {
        return changeReferenceRequired;
    }

    /**
     * Parses an actionable external evaluation response.
     * <p>
     * A response is actionable only when it is a {@code RequestDisallowedByPolicy} error containing at least one
     * {@code PolicyViolation}, and every policy violation contains
     * {@code evaluationDetails.missingPolicyTokenDetails.shouldDeny=true}.
     *
     * @param body the raw response body.
     * @return the aggregated details, or {@code null} when token acquisition should not be attempted.
     */
    @SuppressWarnings("unchecked")
    public static MissingPolicyTokenDetails parse(byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }

        try (JsonReader jsonReader = JsonProviders.createReader(body)) {
            Object rootValue = jsonReader.readUntyped();
            if (!(rootValue instanceof Map)) {
                return null;
            }

            Object errorValue = ((Map<String, Object>) rootValue).get(ERROR);
            if (!(errorValue instanceof Map)) {
                return null;
            }
            Map<String, Object> error = (Map<String, Object>) errorValue;
            if (!REQUEST_DISALLOWED_BY_POLICY.equalsIgnoreCase(asString(error.get(CODE)))) {
                return null;
            }

            Object additionalInfoValue = error.get(ADDITIONAL_INFO);
            if (!(additionalInfoValue instanceof List)) {
                return null;
            }

            boolean foundPolicyViolation = false;
            boolean changeReferenceRequired = false;
            for (Object additionalInfoEntry : (List<Object>) additionalInfoValue) {
                if (!(additionalInfoEntry instanceof Map)) {
                    continue;
                }

                Map<String, Object> additionalInfo = (Map<String, Object>) additionalInfoEntry;
                if (!POLICY_VIOLATION.equalsIgnoreCase(asString(additionalInfo.get(TYPE)))) {
                    continue;
                }

                foundPolicyViolation = true;
                Map<String, Object> missingTokenDetails = getMissingTokenDetails(additionalInfo.get(INFO));
                if (missingTokenDetails == null || !Boolean.TRUE.equals(missingTokenDetails.get(SHOULD_DENY))) {
                    return null;
                }
                changeReferenceRequired |= Boolean.TRUE.equals(missingTokenDetails.get(IS_CHANGE_REFERENCE_REQUIRED));
            }

            return foundPolicyViolation ? new MissingPolicyTokenDetails(changeReferenceRequired) : null;
        } catch (IOException | RuntimeException ex) {
            LOGGER.verbose("Failed to parse the policy violation details from the response body.", ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getMissingTokenDetails(Object infoValue) {
        if (!(infoValue instanceof Map)) {
            return null;
        }
        Object evaluationDetailsValue = ((Map<String, Object>) infoValue).get(EVALUATION_DETAILS);
        if (!(evaluationDetailsValue instanceof Map)) {
            return null;
        }
        Object missingTokenDetailsValue
            = ((Map<String, Object>) evaluationDetailsValue).get(MISSING_POLICY_TOKEN_DETAILS);
        return missingTokenDetailsValue instanceof Map ? (Map<String, Object>) missingTokenDetailsValue : null;
    }

    private static String asString(Object value) {
        return value instanceof String ? (String) value : null;
    }
}
