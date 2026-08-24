// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.sample.financial.openai;

import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionFunctionTool;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import com.openai.models.chat.completions.ChatCompletionTool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Declares the financial tools the agent may call and dispatches model tool
 * calls to their Java implementations.
 * <p>
 * This is the framework-free counterpart to the langchain4j financial sample:
 * instead of {@code @Tool}-annotated methods, each function's JSON schema is
 * declared explicitly as a {@link ChatCompletionTool}, and
 * {@link #invoke(ChatCompletionMessageFunctionToolCall)} routes a model-issued
 * tool call to the matching {@link BankTool}/{@link ExchangeTool} method.
 */
public class FinancialToolbox {

    private final BankTool bankTool;
    private final ExchangeTool exchangeTool;
    private final List<ToolSpec> specs;
    private final List<ChatCompletionTool> definitions;

    /**
     * Creates a toolbox over the given tool implementations.
     *
     * @param bankTool     the account operations implementation
     * @param exchangeTool the currency conversion implementation
     */
    public FinancialToolbox(BankTool bankTool, ExchangeTool exchangeTool) {
        this.bankTool = bankTool;
        this.exchangeTool = exchangeTool;
        this.specs = List.of(
            new ToolSpec("get_balance", "Get the balance of the given user.",
                Map.of("user", stringProperty("The account holder's name.")),
                List.of("user")),
            new ToolSpec("credit", "Credit the given user with the given amount and return the new balance.",
                Map.of(
                    "user", stringProperty("The account holder's name."),
                    "amount", numberProperty("The amount to credit.")),
                List.of("user", "amount")),
            new ToolSpec("withdraw", "Withdraw the given amount from the given user and return the new balance.",
                Map.of(
                    "user", stringProperty("The account holder's name."),
                    "amount", numberProperty("The amount to withdraw.")),
                List.of("user", "amount")),
            new ToolSpec("exchange", "Exchange an amount of money from one currency to another.",
                Map.of(
                    "originalCurrency", stringProperty("The source currency code, for example USD."),
                    "amount", numberProperty("The amount in the source currency."),
                    "targetCurrency", stringProperty("The target currency code, for example EUR.")),
                List.of("originalCurrency", "amount", "targetCurrency")));

        List<ChatCompletionTool> tools = new ArrayList<>(specs.size());
        for (ToolSpec spec : specs) {
            tools.add(functionTool(spec.name, spec.description, objectSchema(spec.properties, spec.required)));
        }
        this.definitions = List.copyOf(tools);
    }

    /**
     * Returns the tool definitions to advertise to the model on each request.
     *
     * @return the immutable list of function tool definitions
     */
    public List<ChatCompletionTool> definitions() {
        return definitions;
    }

    /**
     * Returns the tool definitions rendered for OpenTelemetry GenAI telemetry as
     * {@code gen_ai.tool.definitions} entries ({@code type}/{@code name}/
     * {@code description}/{@code parameters}). Built from the same source as
     * {@link #definitions()} so the advertised and reported schemas stay in sync.
     *
     * @return an immutable list of tool-definition maps ready for JSON serialization
     */
    public List<Map<String, Object>> toolDefinitionsForTelemetry() {
        List<Map<String, Object>> result = new ArrayList<>(specs.size());
        for (ToolSpec spec : specs) {
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("type", "object");
            parameters.put("properties", spec.properties);
            parameters.put("required", spec.required);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("type", "function");
            entry.put("name", spec.name);
            entry.put("description", spec.description);
            entry.put("parameters", parameters);
            result.add(entry);
        }
        return List.copyOf(result);
    }

    /**
     * Executes a single model-issued tool call and returns its result as a
     * string suitable for a {@code tool} role message.
     *
     * @param call the function tool call requested by the model
     * @return the tool result serialized as a string
     */
    public String invoke(ChatCompletionMessageFunctionToolCall call) {
        String name = call.function().name();
        Map<String, Object> args = parseArguments(call);
        switch (name) {
            case "get_balance":
                return String.valueOf(bankTool.getBalance(requireString(args, "user")));
            case "credit":
                return String.valueOf(bankTool.credit(requireString(args, "user"), requireDouble(args, "amount")));
            case "withdraw":
                return String.valueOf(bankTool.withdraw(requireString(args, "user"), requireDouble(args, "amount")));
            case "exchange":
                return String.valueOf(exchangeTool.exchange(
                    requireString(args, "originalCurrency"),
                    requireDouble(args, "amount"),
                    requireString(args, "targetCurrency")));
            default:
                throw new IllegalArgumentException("Unknown tool: " + name);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseArguments(ChatCompletionMessageFunctionToolCall call) {
        // The OpenAI SDK deserializes the raw JSON arguments string with its own
        // Jackson mapper; a Map keeps dispatch decoupled from per-tool arg types.
        return call.function().arguments(Map.class);
    }

    private static String requireString(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required argument: " + key);
        }
        return value.toString();
    }

    private static double requireDouble(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        throw new IllegalArgumentException("Argument " + key + " is not a number: " + value);
    }

    private static ChatCompletionTool functionTool(String name, String description, FunctionParameters parameters) {
        return ChatCompletionTool.ofFunction(ChatCompletionFunctionTool.builder()
            .function(FunctionDefinition.builder()
                .name(name)
                .description(description)
                .parameters(parameters)
                .build())
            .build());
    }

    private static FunctionParameters objectSchema(Map<String, Object> properties, List<String> required) {
        return FunctionParameters.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty("properties", JsonValue.from(properties))
            .putAdditionalProperty("required", JsonValue.from(required))
            .build();
    }

    private static Map<String, Object> stringProperty(String description) {
        return Map.of("type", "string", "description", description);
    }

    private static Map<String, Object> numberProperty(String description) {
        return Map.of("type", "number", "description", description);
    }

    /**
     * Immutable declaration of a single function tool: its name, description and
     * JSON-schema object properties/required list. Serves as the single source of
     * truth for both the advertised {@link ChatCompletionTool} definitions and the
     * GenAI telemetry rendering.
     */
    private static final class ToolSpec {
        private final String name;
        private final String description;
        private final Map<String, Object> properties;
        private final List<String> required;

        ToolSpec(String name, String description, Map<String, Object> properties, List<String> required) {
            this.name = name;
            this.description = description;
            this.properties = properties;
            this.required = required;
        }
    }
}
