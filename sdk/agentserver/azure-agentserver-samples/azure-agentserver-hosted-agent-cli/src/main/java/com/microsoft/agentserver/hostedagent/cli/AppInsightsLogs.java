// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.hostedagent.cli;

import com.azure.core.credential.TokenCredential;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableColumn;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Dumps historical Application Insights telemetry for a deployed hosted agent and returns — the
 * non-streaming counterpart to {@link HostedAgentService#streamLogs}. It runs a KQL query against the
 * Application Insights resource with {@link LogsQueryClient#queryResource} (no shelling out to {@code az}).
 */
final class AppInsightsLogs {

    /**
     * Telemetry tables that can be queried, mirroring the well-known Application Insights views.
     */
    enum LogType {
        TRACES, EXCEPTIONS, REQUESTS, DEPENDENCIES, ALL
    }

    private static final Pattern SINCE = Pattern.compile("(?i)^(\\d+)([mhd])$");
    private static final int MAX_CELL = 150;

    private final LogsQueryClient client;

    AppInsightsLogs(TokenCredential credential) {
        this.client = new LogsQueryClientBuilder().credential(credential).buildClient();
    }

    /**
     * Queries the given Application Insights resource and prints the most recent rows for the agent.
     *
     * @param resourceId the ARM resource id of the Application Insights component
     * @param name       the hosted-agent name used to scope the query
     * @param type       which telemetry table to read
     * @param since      look-back window such as {@code 30m}, {@code 1h} or {@code 2d}
     * @param limit      maximum number of rows to return
     */
    void dump(String resourceId, String name, LogType type, String since, int limit) {
        QueryTimeInterval interval = new QueryTimeInterval(parseSince(since));
        String kql = buildQuery(type, name, limit);

        System.out.printf("Agent: %s  |  Type: %s  |  Since: %s  |  Limit: %d%n%n",
            name, type.name().toLowerCase(Locale.ROOT), since, limit);

        LogsQueryResult result = client.queryResource(resourceId, kql, interval);
        LogsTable table = result.getTable();
        if (table == null || table.getRows().isEmpty()) {
            System.out.printf("No %s found in the last %s.%n", type.name().toLowerCase(Locale.ROOT), since);
            return;
        }

        System.out.printf("Found %d row(s):%n%n", table.getRows().size());
        System.out.println(table.getColumns().stream().map(LogsTableColumn::getColumnName)
            .collect(Collectors.joining(" | ")));
        System.out.println("---");
        for (LogsTableRow row : table.getRows()) {
            System.out.println(row.getRow().stream().map(AppInsightsLogs::cell)
                .collect(Collectors.joining("  ")));
        }
    }

    private static String cell(LogsTableCell cell) {
        String value = cell.getValueAsString();
        if (value == null) {
            return "";
        }
        value = value.replaceAll("\\s+", " ").trim();
        return value.length() > MAX_CELL ? value.substring(0, MAX_CELL) + "…" : value;
    }

    private static Duration parseSince(String since) {
        Matcher matcher = SINCE.matcher(since.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid --since '" + since + "'. Use a value like 30m, 1h or 2d.");
        }
        long amount = Long.parseLong(matcher.group(1));
        switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
            case "m":
                return Duration.ofMinutes(amount);
            case "h":
                return Duration.ofHours(amount);
            default:
                return Duration.ofDays(amount);
        }
    }

    /**
     * Builds a KQL query for the requested table, scoped to the agent. The look-back window is supplied
     * separately through {@link QueryTimeInterval}, so the query itself does not repeat an {@code ago(...)}
     * filter.
     */
    private static String buildQuery(LogType type, String name, int limit) {
        switch (type) {
            case EXCEPTIONS:
                return "exceptions"
                    + "| where cloud_RoleName contains 'agent' or customDimensions contains '" + name + "'"
                    + "| top " + limit + " by timestamp desc"
                    + "| project timestamp, type, outerMessage, cloud_RoleName, innermostMessage";
            case REQUESTS:
                return "requests"
                    + "| where name contains '" + name + "' or name contains 'execute_agent'"
                    + "| top " + limit + " by timestamp desc"
                    + "| project timestamp, name, resultCode, duration, success";
            case DEPENDENCIES:
                return "let agent_ops = dependencies"
                    + "| where name contains 'invoke_agent' and name contains '" + name + "'"
                    + "| distinct operation_Id;"
                    + "dependencies"
                    + "| where operation_Id in (agent_ops)"
                    + "| where name !contains '/msi/token'"
                    + "| top " + limit + " by timestamp desc"
                    + "| project timestamp, type, name, resultCode, duration, success";
            case ALL:
                return "union isfuzzy=true exceptions, customEvents, dependencies, requests, traces"
                    + "| where message !contains 'readiness' and message !contains 'liveness'"
                    + "| where * contains '" + name + "'"
                    + "| top " + limit + " by timestamp desc"
                    + "| project timestamp, itemType, message, name, resultCode, type, severityLevel";
            case TRACES:
            default:
                return "traces"
                    + "| where message !contains 'liveness' and message !contains 'readiness'"
                    + "| where cloud_RoleName contains 'agent' or customDimensions contains '" + name + "'"
                    + "| top " + limit + " by timestamp desc"
                    + "| project timestamp, severityLevel, message, cloud_RoleName";
        }
    }
}
