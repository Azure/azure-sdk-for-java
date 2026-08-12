// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.evaluation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Internal representation of the {@code missingPolicyTokenDetails} object that the Azure Resource Manager returns in
 * the body of a {@code 403 RequestDisallowedByPolicy} response when a resource operation requires an external
 * evaluation ("Invoke") policy token.
 * <p>
 * The presence of this object is the discriminator that distinguishes an external evaluation scenario from a plain
 * policy deny (both share the {@code RequestDisallowedByPolicy} error code). The parsed fields are used only to guard
 * the assumptions of the current SDK version before attempting to acquire a token; they are not part of the acquire
 * request itself.
 */
public final class MissingPolicyTokenDetails {
    private static final ClientLogger LOGGER = new ClientLogger(MissingPolicyTokenDetails.class);

    private static final String MISSING_POLICY_TOKEN_DETAILS = "missingPolicyTokenDetails";
    private static final String SHOULD_DENY = "shouldDeny";
    private static final String ENDPOINT_KIND = "endpointKind";
    private static final String IS_CHANGE_REFERENCE_REQUIRED = "isChangeReferenceRequired";

    private final boolean shouldDeny;
    private final String endpointKind;
    private final boolean changeReferenceRequired;

    private MissingPolicyTokenDetails(boolean shouldDeny, String endpointKind, boolean changeReferenceRequired) {
        this.shouldDeny = shouldDeny;
        this.endpointKind = endpointKind;
        this.changeReferenceRequired = changeReferenceRequired;
    }

    /**
     * Whether the resource operation will ultimately be denied when the policy token is not supplied.
     *
     * @return whether the operation will be denied without a policy token.
     */
    public boolean isShouldDeny() {
        return shouldDeny;
    }

    /**
     * The kind of external evaluation endpoint that backs the policy (for example, {@code AzureResourceGraph}).
     *
     * @return the endpoint kind, may be {@code null} if not present in the response.
     */
    public String getEndpointKind() {
        return endpointKind;
    }

    /**
     * Whether the service requires a change-safety reference to be supplied when acquiring the policy token. The
     * current SDK does not support supplying a change reference, so a value of {@code true} indicates the SDK cannot
     * satisfy the flow.
     *
     * @return whether a change reference is required.
     */
    public boolean isChangeReferenceRequired() {
        return changeReferenceRequired;
    }

    /**
     * Attempts to parse the {@code missingPolicyTokenDetails} object out of a {@code 403 RequestDisallowedByPolicy}
     * response body. The object is located by recursively searching the JSON document so that the parser is resilient
     * to the exact nesting of the ARM error contract.
     *
     * @param body the raw response body bytes; may be {@code null} or empty.
     * @return the parsed {@link MissingPolicyTokenDetails}, or {@code null} when the object is not present (indicating
     * the response is not an external evaluation scenario) or when the body cannot be parsed.
     */
    public static MissingPolicyTokenDetails parse(byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try (JsonReader jsonReader = JsonProviders.createReader(body)) {
            Object root = jsonReader.readUntyped();
            Map<String, Object> details = findMissingPolicyTokenDetails(root);
            if (details == null) {
                return null;
            }
            boolean shouldDeny = asBoolean(details.get(SHOULD_DENY), true);
            String endpointKind = asString(details.get(ENDPOINT_KIND));
            boolean changeReferenceRequired = asBoolean(details.get(IS_CHANGE_REFERENCE_REQUIRED), false);
            return new MissingPolicyTokenDetails(shouldDeny, endpointKind, changeReferenceRequired);
        } catch (IOException | RuntimeException ex) {
            // A malformed or unexpected body simply means this is treated as a non-external-evaluation response.
            LOGGER.verbose("Failed to parse missingPolicyTokenDetails from the response body.", ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> findMissingPolicyTokenDetails(Object node) {
        if (node instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) node;
            Object direct = map.get(MISSING_POLICY_TOKEN_DETAILS);
            if (direct instanceof Map) {
                return (Map<String, Object>) direct;
            }
            for (Object value : map.values()) {
                Map<String, Object> found = findMissingPolicyTokenDetails(value);
                if (found != null) {
                    return found;
                }
            }
        } else if (node instanceof List) {
            for (Object value : (List<Object>) node) {
                Map<String, Object> found = findMissingPolicyTokenDetails(value);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    private static String asString(Object value) {
        String stringValue = value == null ? null : value.toString();
        return CoreUtils.isNullOrEmpty(stringValue) ? null : stringValue;
    }
}
