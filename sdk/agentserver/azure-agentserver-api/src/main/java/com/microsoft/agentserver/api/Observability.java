// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Provides OpenTelemetry instrumentation for the Azure AI Foundry Agent Server server.
 * <p>
 * This class uses the OpenTelemetry API which is a no-op by default. It becomes active when:
 * <ul>
 *  <li>The OpenTelemetry Java Agent is attached via {@code javaagent:} JVM flag</li>
 *  <li>The OpenTelemetry SDK is programmatically configured (e.g., via {@code opentelemetry-sdk-extension-autoconfigure})</li>
 *  <li>The Application Insights Java Agent is attached</li>
 * </ul>
 * <p>
 * The instrumentation scope is {@code "Azure.AI.AgentServer.Responses"}, matching the.NET convention.
 * <p>
 * <b>Environment variables</b> (used by OTel SDK/Agent when present):
 * <ul>
 *  <li>{@code OTEL_EXPORTER_OTLP_ENDPOINT} — OTLP exporter endpoint</li>
 *  <li>{@code OTEL_SERVICE_NAME} — service name (defaults to agent name)</li>
 *  <li>{@code APPLICATIONINSIGHTS_CONNECTION_STRING} — enables Application Insights export</li>
 * </ul>
 */
public final class Observability {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observability.class);

    /**
     * Instrumentation scope name for the protocol, matching.NET convention.
     */
    public static final String SCOPE_RESPONSES = "Azure.AI.AgentServer.Responses";

    /**
     * Instrumentation scope name for the core hosting layer.
     */
    public static final String SCOPE_CORE = "Azure.AI.AgentServer.Core";

    private static volatile Tracer responsesTracer;
    private static volatile Tracer coreTracer;

    private Observability() {
        // Static utility class
    }

    /**
     * Gets the tracer for the protocol scope.
     * Uses {@link GlobalOpenTelemetry} which is auto-configured by the OTel Java Agent
     * or SDK autoconfigure module.
     *
     * @return the protocol tracer.
     */
    public static Tracer getResponsesTracer() {
        if (responsesTracer == null) {
            responsesTracer = GlobalOpenTelemetry.getTracer(SCOPE_RESPONSES);
        }
        return responsesTracer;
    }

    /**
     * Gets the tracer for the core hosting scope.
     *
     * @return the Core hosting tracer.
     */
    public static Tracer getCoreTracer() {
        if (coreTracer == null) {
            coreTracer = GlobalOpenTelemetry.getTracer(SCOPE_CORE);
        }
        return coreTracer;
    }

    /**
     * Starts a new SERVER span for an incoming HTTP request.
     * Extracts trace context from request headers (W3C traceparent) using
     * the global propagator.
     *
     * @param spanName the span name (e.g., "POST /responses").
     * @param headers  the request headers for context propagation.
     * @return the started span (caller must close/end it).
     */
    public static Span startServerSpan(String spanName, Map<String, String> headers) {
        OpenTelemetry otel = GlobalOpenTelemetry.get();
        Context extractedContext = otel.getPropagators()
            .getTextMapPropagator()
            .extract(Context.current(), headers, MapTextMapGetter.INSTANCE);

        return getCoreTracer()
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.SERVER)
            .setParent(extractedContext)
            .startSpan();
    }

    /**
     * Records standard HTTP attributes on a span.
     *
     * @param span       the span to set attributes on.
     * @param method     the HTTP method (GET, POST, etc.).
     * @param path       the request path.
     * @param statusCode the HTTP response status code.
     */
    public static void setHttpAttributes(Span span, String method, String path, int statusCode) {
        span.setAttribute("http.request.method", method);
        span.setAttribute("url.path", path);
        span.setAttribute("http.response.status_code", statusCode);

        if (statusCode >= 500) {
            span.setStatus(StatusCode.ERROR, "HTTP " + statusCode);
        }
    }

    /**
     * Starts the spec-required {@code invoke_agent {model}} span.
     * <p>
     * Sets all required GenAI semantic-convention tags and namespaced
     * {@code azure.ai.agentserver.responses.*} tags so the platform's
     * trajectory UI can render the response in context.
     *
     * @param model          resolved model name (may be {@code null} or empty)
     * @param responseId     the response id (e.g. {@code caresp_…}); never null/empty
     * @param conversationId conversation id, or {@code null} if absent
     * @param agentName      agent name, or {@code null}
     * @param agentVersion   agent version, or {@code null}
     * @param streaming      whether this is a streaming invocation
     * @return the started span; caller is responsible for ending it
     */
    public static Span startInvokeAgentSpan(
        String model,
        String responseId,
        String conversationId,
        String agentName,
        String agentVersion,
        boolean streaming) {

        String spanName = (model != null && !model.isEmpty())
            ? "invoke_agent " + model
            : "invoke_agent";

        Span span = getResponsesTracer()
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        // GenAI semantic-convention identity tags
        span.setAttribute("service.name", "azure.ai.agentserver");
        span.setAttribute("gen_ai.provider.name", "AzureAI Hosted Agents");
        span.setAttribute("gen_ai.operation.name", "invoke_agent");
        span.setAttribute("gen_ai.response.id", responseId);
        if (model != null && !model.isEmpty()) {
            span.setAttribute("gen_ai.request.model", model);
        }
        if (conversationId != null && !conversationId.isEmpty()) {
            span.setAttribute("gen_ai.conversation.id", conversationId);
        }

        String agentId;
        if (agentName != null && !agentName.isEmpty()) {
            agentId = agentName + ":" + (agentVersion != null ? agentVersion : "");
            span.setAttribute("gen_ai.agent.name", agentName);
            if (agentVersion != null && !agentVersion.isEmpty()) {
                span.setAttribute("gen_ai.agent.version", agentVersion);
            }
        } else {
            agentId = "";
        }
        span.setAttribute("gen_ai.agent.id", agentId);

        // Namespaced tags for dashboards / correlation
        span.setAttribute("azure.ai.agentserver.responses.response_id", responseId);
        span.setAttribute("azure.ai.agentserver.responses.conversation_id",
            conversationId != null ? conversationId : "");
        span.setAttribute("azure.ai.agentserver.responses.streaming", streaming);

        String projectArmId = FoundryEnvironment.PROJECT_ARM_ID;
        if (projectArmId != null && !projectArmId.isEmpty()) {
            span.setAttribute("microsoft.foundry.project.id", projectArmId);
        }

        return span;
    }

    /**
     * Records an error on an {@code invoke_agent} span per
     * the spec (error tags). Sets the status to ERROR
     * and adds the recommended namespaced + OTel exception tags.
     */
    public static void recordInvokeAgentError(Span span, String code, String message, Throwable t) {
        if (span == null) {
            return;
        }
        if (code != null) {
            span.setAttribute("azure.ai.agentserver.responses.error.code", code);
            span.setAttribute("error.type", code);
        }
        if (message != null) {
            span.setAttribute("azure.ai.agentserver.responses.error.message", message);
            span.setAttribute("otel.status_description", message);
        }
        if (t != null) {
            span.recordException(t);
        }
        span.setStatus(StatusCode.ERROR, message != null ? message : "");
        maybeLogRbacGuidance(t);
    }

    /**
     * Pattern matching a missing data-action message of the form:
     * "The principal `<guid>` lacks the required data action `<scope>/<action>`..."
     * emitted by Azure Cognitive Services / OpenAI on 401 PermissionDenied.
     */
    private static final java.util.regex.Pattern PRINCIPAL_LACKS_ACTION = java.util.regex.Pattern.compile(
        "principal\\s+`?([0-9a-fA-F-]{36})`?\\s+lacks the required data action\\s+`?([^`\\s\"]+)`?",
        java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * Backstop: catches the generic "Principal does not have access to API/Operation."
     */
    private static final java.util.regex.Pattern GENERIC_PRINCIPAL_DENIED = java.util.regex.Pattern.compile(
        "Principal does not have access to API/Operation",
        java.util.regex.Pattern.CASE_INSENSITIVE);

    private static final java.util.Set<String> RBAC_GUIDANCE_PRINCIPALS =
        java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    /**
     * Detects Azure RBAC-related authentication failures from an exception
     * chain and prints a one-time human-readable remediation message that
     * includes the exact {@code az role assignment create} command to run.
     */
    public static void maybeLogRbacGuidance(Throwable t) {
        if (t == null) {
            return;
        }

        Throwable cur = t;
        for (int depth = 0; cur != null && depth < 8; depth++, cur = cur.getCause()) {
            String msg = cur.getMessage();
            if (msg == null) {
                continue;
            }
            java.util.regex.Matcher m = PRINCIPAL_LACKS_ACTION.matcher(msg);
            if (m.find()) {
                String principalId = m.group(1);
                String action = m.group(2);
                if (RBAC_GUIDANCE_PRINCIPALS.add("data-action:" + principalId + ":" + action)) {
                    logDataActionGuidance(principalId, action);
                }
                return;
            }
            if (GENERIC_PRINCIPAL_DENIED.matcher(msg).find()) {
                if (RBAC_GUIDANCE_PRINCIPALS.add("generic:" + System.identityHashCode(cur))) {
                    logGenericPrincipalDeniedGuidance();
                }
                return;
            }
        }
    }

    private static void logDataActionGuidance(String principalId, String action) {
        String role = guessRoleForAction(action);
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("==============================================================================\n");
        sb.append(" AZURE RBAC: The agent's managed identity is missing a required role.\n");
        sb.append("==============================================================================\n");
        sb.append(" Principal id   : ").append(principalId).append("\n");
        sb.append(" Missing action : ").append(action).append("\n");
        if (role != null) {
            sb.append(" Suggested role : \"").append(role).append("\"\n");
        }
        sb.append("\n");
        sb.append(" Run the following to grant access (replace <SCOPE> with the resource id of\n");
        sb.append(" the Cognitive Services / Foundry account the request was made against):\n");
        sb.append("\n");
        sb.append("   az role assignment create \\\n");
        sb.append("     --assignee ").append(principalId).append(" \\\n");
        sb.append("     --role \"").append(role != null ? role : "Cognitive Services OpenAI User").append("\" \\\n");
        sb.append("     --scope <SCOPE>\n");
        sb.append("\n");
        sb.append(" Allow ~30s for the assignment to propagate, then retry.\n");
        sb.append("==============================================================================");
        LOGGER.error(sb.toString());
    }

    private static void logGenericPrincipalDeniedGuidance() {
        LOGGER.error("""
            ==============================================================================
             AZURE RBAC: The agent's managed identity is missing a required role.
            ==============================================================================
             The platform returned: "Principal does not have access to API/Operation."
             The principal id is not in the message, but it is the agent's instance
             identity (visible via `az agentservers show` or in the agent metadata).

             For Azure OpenAI calls against a Foundry project, the agent typically needs:
               - "Cognitive Services OpenAI User" on the Cognitive Services account, AND
               - "Azure AI User" on the project (scope ending in /projects/<name>)

             Use:
               az role assignment create --assignee <principal-id> \\
                 --role "Azure AI User" --scope <project-scope>
            ==============================================================================""");
    }

    private static String guessRoleForAction(String action) {
        if (action == null) {
            return null;
        }
        String lower = action.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("openai") && lower.contains("chat")) {
            return "Cognitive Services OpenAI User";
        }
        if (lower.contains("openai")) {
            return "Cognitive Services OpenAI Contributor";
        }
        if (lower.contains("cognitiveservices")) {
            return "Cognitive Services User";
        }
        return null;
    }

    /**
     * Starts the spec-required {@code tools/call {tool_name}} span
     * for outbound calls to the Foundry
     * Toolboxes (MCP) proxy. Use {@link #recordToolboxResult} to set the
     * success/failure tags before ending the span.
     *
     * @param toolboxName  the toolbox name used for this invocation
     * @param fullToolName fully-qualified tool name, e.g. {@code everything.echo}
     * @param serverLabel  server-label prefix, e.g. {@code everything}
     * @return the started span; caller is responsible for ending it
     */
    public static Span startToolboxCallSpan(String toolboxName, String fullToolName, String serverLabel) {
        Span span = getResponsesTracer()
            .spanBuilder("tools/call " + fullToolName)
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();
        if (toolboxName != null) {
            span.setAttribute("azure.ai.agentserver.toolbox.name", toolboxName);
        }
        if (fullToolName != null) {
            span.setAttribute("azure.ai.agentserver.toolbox.tool_name", fullToolName);
        }
        if (serverLabel != null) {
            span.setAttribute("azure.ai.agentserver.toolbox.server_label", serverLabel);
        }
        return span;
    }

    /**
     * Records the success/failure outcome and latency on a {@link
     * #startToolboxCallSpan} span.
     */
    public static void recordToolboxResult(Span span, boolean success, long latencyMs,
                                           String errorCode, String errorMessage, Throwable t) {
        if (span == null) {
            return;
        }
        span.setAttribute("azure.ai.agentserver.toolbox.success", success);
        span.setAttribute("azure.ai.agentserver.toolbox.latency_ms", latencyMs);
        if (!success) {
            if (errorCode != null) {
                span.setAttribute("azure.ai.agentserver.toolbox.error.code", errorCode);
            }
            if (errorMessage != null) {
                span.setAttribute("azure.ai.agentserver.toolbox.error.message", errorMessage);
            }
            if (t != null) {
                span.recordException(t);
            }
            span.setStatus(StatusCode.ERROR, errorMessage != null ? errorMessage : "");
        }
    }

    /**
     * TextMapGetter for extracting trace context from a {@code Map<String, String>}.
     */
    private enum MapTextMapGetter implements TextMapGetter<Map<String, String>> {
        INSTANCE;

        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier == null ? null : carrier.get(key);
        }
    }
}

