// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.hostedagent.cli;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command-line hosted-agent deployment tool for Azure AI Foundry (vnext experience).
 *
 * <p>Command-line argument parsing uses JCommander (the same library the repo's perf/benchmark tooling
 * relies on) and every operation is driven through the {@code com.azure:azure-ai-agents} Java SDK.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   deploy   --endpoint &lt;url&gt; --name &lt;agent&gt; --image &lt;ref&gt; [--size medium]
 *            [--model &lt;deployment&gt;] [--description &lt;text&gt;] [--env KEY=VALUE]...
 *   status   --endpoint &lt;url&gt; --name &lt;agent&gt;
 *   list     --endpoint &lt;url&gt;
 *   delete   --endpoint &lt;url&gt; --name &lt;agent&gt;
 *   logs     --endpoint &lt;url&gt; --name &lt;agent&gt; [--source stream] [--version N] [--session ID]
 *   logs     --endpoint &lt;url&gt; --name &lt;agent&gt; --source insights --app-insights &lt;resource-id&gt;
 *            [--log-type traces] [--since 1h] [--limit 20]
 * </pre>
 *
 * <p>Any invocation may instead (or additionally) draw its options from a YAML file with
 * {@code --config <file>}; see {@link ConfigLoader}. The command is always given on the command line, and
 * any option passed there overrides the file.</p>
 *
 * <p>Agent endpoints and sessions are preview features that only work with hosted agents, so the client is
 * built with {@code allowPreview(true)}.</p>
 */
public final class HostedAgentCli {

    private static final String CONFIG_FLAG = "--config";

    private HostedAgentCli() {
    }

    public static void main(String[] args) {
        JCommander jc = JCommander.newBuilder()
            .programName("hosted-agent-cli")
            .addCommand("deploy", new DeployCommand())
            .addCommand("status", new StatusCommand())
            .addCommand("list", new ListCommand())
            .addCommand("delete", new DeleteCommand())
            .addCommand("logs", new LogsCommand())
            .build();

        String[] effectiveArgs;
        try {
            effectiveArgs = applyConfig(jc, args);
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return;
        }

        try {
            jc.parse(effectiveArgs);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            e.getJCommander().usage();
            System.exit(1);
            return;
        }

        String parsedCommand = jc.getParsedCommand();
        if (parsedCommand == null) {
            jc.usage();
            System.exit(1);
            return;
        }

        AgentCommand command = (AgentCommand) jc.getCommands().get(parsedCommand).getObjects().get(0);

        com.azure.core.credential.TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        AgentsClient client = new AgentsClientBuilder()
            .credential(credential)
            .endpoint(command.endpoint)
            .allowPreview(true)
            .buildAgentsClient();

        try {
            command.run(new HostedAgentService(client, command.endpoint, credential));
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Resolves {@code --config <file>} (if present) and returns the effective argument array:
     * {@code <command> <yaml-derived args> <remaining CLI args>}. The command-line arguments are placed
     * last so an explicit flag always overrides the value from the file. Only YAML keys that map to an
     * option the selected command actually accepts are applied, so a single config file (for example one
     * carrying deploy-specific keys) can be shared across commands such as {@code list} or {@code status}.
     */
    private static String[] applyConfig(JCommander jc, String[] args) {
        List<String> remaining = new ArrayList<>(Arrays.asList(args));

        String configPath = null;
        for (int i = 0; i < remaining.size(); i++) {
            if (CONFIG_FLAG.equals(remaining.get(i))) {
                if (i + 1 >= remaining.size()) {
                    throw new IllegalArgumentException("Missing value for " + CONFIG_FLAG);
                }
                configPath = remaining.get(i + 1);
                remaining.remove(i + 1);
                remaining.remove(i);
                break;
            }
        }

        if (configPath == null) {
            return args;
        }

        ConfigLoader config = ConfigLoader.load(configPath);

        String cliCommand = remaining.stream().filter(a -> !a.startsWith("--")).findFirst().orElse(null);
        if (cliCommand == null) {
            throw new IllegalArgumentException("No command specified. Provide one of: deploy, status, list, "
                + "delete, logs (before --config).");
        }

        List<String> merged = new ArrayList<>();
        merged.add(cliCommand);

        Set<String> cliFlags = remaining.stream().filter(a -> a.startsWith("--")).collect(Collectors.toSet());
        merged.addAll(config.toArguments(cliFlags, acceptedOptions(jc, cliCommand)));

        for (String arg : remaining) {
            if (arg.equals(cliCommand)) {
                cliCommand = null;
                continue;
            }
            merged.add(arg);
        }
        return merged.toArray(new String[0]);
    }

    /**
     * Returns every {@code --option} name accepted by the given command (including options inherited from
     * {@link AgentCommand}), or an empty set if the command is unknown.
     */
    private static Set<String> acceptedOptions(JCommander jc, String command) {
        Set<String> names = new java.util.HashSet<>();
        JCommander sub = jc.getCommands().get(command);
        if (sub == null) {
            return names;
        }
        Object commandObject = sub.getObjects().get(0);
        for (Class<?> c = commandObject.getClass(); c != null; c = c.getSuperclass()) {
            for (java.lang.reflect.Field field : c.getDeclaredFields()) {
                Parameter parameter = field.getAnnotation(Parameter.class);
                if (parameter != null) {
                    names.addAll(Arrays.asList(parameter.names()));
                }
            }
        }
        return names;
    }

    /**
     * Base command carrying the project endpoint shared by every sub-command.
     */
    abstract static class AgentCommand {
        @Parameter(names = "--endpoint", required = true, description = "Azure AI Foundry project endpoint")
        String endpoint;

        abstract void run(HostedAgentService service);
    }

    @Parameters(commandDescription = "Deploy a container image as a hosted agent version and wait until active")
    static final class DeployCommand extends AgentCommand {
        @Parameter(names = "--name", required = true, description = "Hosted-agent name")
        String name;

        @Parameter(names = "--image", required = true, description = "Container image reference")
        String image;

        @Parameter(names = "--size",
            description = "Sandbox size: small (0.5 vCPU/1Gi), medium (1 vCPU/2Gi) or large (2 vCPU/4Gi)")
        String size = AgentSize.MEDIUM.name().toLowerCase(java.util.Locale.ROOT);

        @Parameter(names = "--model", description = "Model deployment; added as the MODEL_DEPLOYMENT_NAME env var")
        String model;

        @Parameter(names = "--subscription",
            description = "Azure subscription id of the AI Services account (enables the Azure OpenAI "
                + "permission check after deploy)")
        String subscription;

        @Parameter(names = "--grant-openai-access", arity = 1,
            description = "When true, attempt to grant the agent identity the Azure OpenAI role if it is "
                + "missing (requires --subscription); prints the manual fix if the grant fails")
        boolean grantOpenAiAccess = false;

        @Parameter(names = "--description", description = "Version description")
        String description = "Hosted agent deployed by the Azure AI Agents Java CLI.";

        @Parameter(names = "--env", description = "Container environment variable KEY=VALUE (repeatable)")
        List<String> env = new ArrayList<>();

        @Override
        void run(HostedAgentService service) {
            Map<String, String> environment = new LinkedHashMap<>();
            for (String entry : env) {
                int eq = entry.indexOf('=');
                if (eq <= 0) {
                    throw new IllegalArgumentException("--env expects KEY=VALUE but got: " + entry);
                }
                environment.put(entry.substring(0, eq), entry.substring(eq + 1));
            }
            if (model != null) {
                environment.put("MODEL_DEPLOYMENT_NAME", model);
            }
            AgentSize resolved = AgentSize.fromName(size);
            service.deploy(name, image, resolved.cpu, resolved.memory, environment, description, subscription,
                grantOpenAiAccess);
        }
    }

    /**
     * The fixed set of hosted-agent sandbox sizes supported by the service, each mapping to a
     * CPU/memory pair. See the Azure AI Foundry hosted-agent sandbox size documentation.
     */
    enum AgentSize {
        SMALL("0.5", "1Gi"),
        MEDIUM("1", "2Gi"),
        LARGE("2", "4Gi");

        final String cpu;
        final String memory;

        AgentSize(String cpu, String memory) {
            this.cpu = cpu;
            this.memory = memory;
        }

        static AgentSize fromName(String name) {
            try {
                return AgentSize.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Unknown --size '" + name + "'. Valid sizes are: small, medium, large.");
            }
        }
    }

    @Parameters(commandDescription = "Show the status of an agent and all of its versions")
    static final class StatusCommand extends AgentCommand {
        @Parameter(names = "--name", required = true, description = "Hosted-agent name")
        String name;

        @Override
        void run(HostedAgentService service) {
            service.status(name);
        }
    }

    @Parameters(commandDescription = "List every hosted agent in the project")
    static final class ListCommand extends AgentCommand {
        @Override
        void run(HostedAgentService service) {
            service.list();
        }
    }

    @Parameters(commandDescription = "Delete all versions of an agent and then the agent itself")
    static final class DeleteCommand extends AgentCommand {
        @Parameter(names = "--name", required = true, description = "Hosted-agent name")
        String name;

        @Override
        void run(HostedAgentService service) {
            service.delete(name);
        }
    }

    @Parameters(commandDescription = "Show container logs: stream a live session, or dump Application Insights history")
    static final class LogsCommand extends AgentCommand {
        @Parameter(names = "--name", required = true, description = "Hosted-agent name")
        String name;

        @Parameter(names = "--source",
            description = "Log source: 'stream' (live SSE console session) or 'insights' (dump Application "
                + "Insights history and exit)")
        String source = "stream";

        @Parameter(names = "--version", description = "[stream] Agent version to stream (defaults to the latest)")
        String version;

        @Parameter(names = "--session", description = "[stream] Session id to stream (a new session is created when omitted)")
        String session;

        @Parameter(names = "--app-insights",
            description = "[insights] ARM resource id of the Application Insights component to query")
        String appInsights;

        @Parameter(names = "--log-type",
            description = "[insights] Telemetry table: traces, exceptions, requests, dependencies or all")
        String logType = "traces";

        @Parameter(names = "--since", description = "[insights] Look-back window such as 30m, 1h or 2d")
        String since = "1h";

        @Parameter(names = "--limit", description = "[insights] Maximum number of rows to return")
        int limit = 20;

        @Override
        void run(HostedAgentService service) {
            if ("insights".equalsIgnoreCase(source)) {
                AppInsightsLogs.LogType type;
                try {
                    type = AppInsightsLogs.LogType.valueOf(logType.trim().toUpperCase(java.util.Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid --log-type '" + logType
                        + "'. Use: traces, exceptions, requests, dependencies or all.");
                }
                service.dumpLogs(name, appInsights, type, since, limit);
            } else if ("stream".equalsIgnoreCase(source)) {
                service.streamLogs(name, version, session);
            } else {
                throw new IllegalArgumentException("Invalid --source '" + source + "'. Use 'stream' or 'insights'.");
            }
        }
    }
}
