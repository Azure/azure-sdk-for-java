package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * The ChatCompletionOptions model.
 */
public final class ChatCompletionOptions {

    /*
     * A list of functions the model may generate JSON inputs for.
     */
    @JsonProperty(value = "functions")
    private List<FunctionDefinition> functions;

    /*
     * Controls how the model responds to function calls. "none" means the model does not call a function,
     * and responds to the end-user. "auto" means the model can pick between an end-user or calling a function.
     * Specifying a particular function via `{"name": "my_function"}` forces the model to call that function.
     * "none" is the default when no functions are present. "auto" is the default if functions are present.
     */

    @JsonProperty(value = "function_call")
    private BinaryData functionCall;

    /*
     * The maximum number of tokens to generate.
     */

    @JsonProperty(value = "max_tokens")
    private Integer maxTokens;

    /*
     * The sampling temperature to use that controls the apparent creativity of generated completions.
     * Higher values will make output more random while lower values will make results more focused
     * and deterministic.
     * It is not recommended to modify temperature and top_p for the same completions request as the
     * interaction of these two settings is difficult to predict.
     */

    @JsonProperty(value = "temperature")
    private Double temperature;

    /*
     * An alternative to sampling with temperature called nucleus sampling. This value causes the
     * model to consider the results of tokens with the provided probability mass. As an example, a
     * value of 0.15 will cause only the tokens comprising the top 15% of probability mass to be
     * considered.
     * It is not recommended to modify temperature and top_p for the same completions request as the
     * interaction of these two settings is difficult to predict.
     */

    @JsonProperty(value = "top_p")
    private Double nucleusSamplingFactor;

    /*
     * A map between GPT token IDs and bias scores that influences the probability of specific tokens
     * appearing in a completions response. Token IDs are computed via external tokenizer tools, while
     * bias scores reside in the range of -100 to 100 with minimum and maximum values corresponding to
     * a full ban or exclusive selection of a token, respectively. The exact behavior of a given bias
     * score varies by model.
     */

    @JsonProperty(value = "logit_bias")
    private Map<String, Integer> tokenSelectionBiases;

    /*
     * An identifier for the caller or end user of the operation. This may be used for tracking
     * or rate-limiting purposes.
     */

    @JsonProperty(value = "user")
    private String user;


    /*
     * A collection of textual sequences that will end completions generation.
     */
    @JsonProperty(value = "stop")
    private List<String> stopSequences;

    /*
     * A value that influences the probability of generated tokens appearing based on their existing
     * presence in generated text.
     * Positive values will make tokens less likely to appear when they already exist and increase the
     * model's likelihood to output new topics.
     */

    @JsonProperty(value = "presence_penalty")
    private Double presencePenalty;

    /*
     * A value that influences the probability of generated tokens appearing based on their cumulative
     * frequency in generated text.
     * Positive values will make tokens less likely to appear as their frequency increases and
     * decrease the likelihood of the model repeating the same statements verbatim.
     */

    @JsonProperty(value = "frequency_penalty")
    private Double frequencyPenalty;

    /*
     * If specified, the system will make a best effort to sample deterministically such that repeated requests with
     * the
     * same seed and parameters should return the same result. Determinism is not guaranteed, and you should refer to
     * the
     * system_fingerprint response parameter to monitor changes in the backend."
     */
    @JsonProperty(value = "seed")
    private Long seed;

    /*
     * Whether to return log probabilities of the output tokens or not. If true, returns the log probabilities of each
     * output token returned in the `content` of `message`. This option is currently not available on the
     * `gpt-4-vision-preview` model.
     */

    @JsonProperty(value = "logprobs")
    private Boolean includeLogProbabilities;

    /*
     * An integer between 0 and 5 specifying the number of most likely tokens to return at each token position, each
     * with an associated log probability. `logprobs` must be set to `true` if this parameter is used.
     */

    @JsonProperty(value = "top_logprobs")
    private Integer logProbabilityCount;

    /*
     * An object specifying the format that the model must output. Used to enable JSON mode.
     */

    @JsonProperty(value = "response_format")
    private ChatCompletionsResponseFormat responseFormat;

    /*
     * The available tool definitions that the chat completions request can use, including caller-defined functions.
     */

    @JsonProperty(value = "tools")
    private List<ChatCompletionsToolDefinition> tools;


    /**
     * Creates an instance of ChatCompletionOptions class.
     *
     */

    @JsonCreator
    public ChatCompletionOptions() {
    }

    /**
     * Get the functions property: A list of functions the model may generate JSON inputs for.
     *
     * @return the functions value.
     */

    public List<FunctionDefinition> getFunctions() {
        return this.functions;
    }

    /**
     * Set the functions property: A list of functions the model may generate JSON inputs for.
     *
     * @param functions the functions value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setFunctions(List<FunctionDefinition> functions) {
        this.functions = functions;
        return this;
    }

    /**
     * Get the functionCall property: Controls how the model responds to function calls. "none" means the model does
     * not call a function,
     * and responds to the end-user. "auto" means the model can pick between an end-user or calling a function.
     * Specifying a particular function via `{"name": "my_function"}` forces the model to call that function.
     * "none" is the default when no functions are present. "auto" is the default if functions are present.
     *
     * @return the functionCall value.
     */

    public BinaryData getFunctionCall() {
        return this.functionCall;
    }

    /**
     * Set the functionCall property: Controls how the model responds to function calls. "none" means the model does
     * not call a function,
     * and responds to the end-user. "auto" means the model can pick between an end-user or calling a function.
     * Specifying a particular function via `{"name": "my_function"}` forces the model to call that function.
     * "none" is the default when no functions are present. "auto" is the default if functions are present.
     *
     * @param functionCall the functionCall value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setFunctionCall(BinaryData functionCall) {
        this.functionCall = functionCall;
        return this;
    }

    /**
     * Get the maxTokens property: The maximum number of tokens to generate.
     *
     * @return the maxTokens value.
     */

    public Integer getMaxTokens() {
        return this.maxTokens;
    }

    /**
     * Set the maxTokens property: The maximum number of tokens to generate.
     *
     * @param maxTokens the maxTokens value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * Get the temperature property: The sampling temperature to use that controls the apparent creativity of generated
     * completions.
     * Higher values will make output more random while lower values will make results more focused
     * and deterministic.
     * It is not recommended to modify temperature and top_p for the same completions request as the
     * interaction of these two settings is difficult to predict.
     *
     * @return the temperature value.
     */

    public Double getTemperature() {
        return this.temperature;
    }

    /**
     * Set the temperature property: The sampling temperature to use that controls the apparent creativity of generated
     * completions.
     * Higher values will make output more random while lower values will make results more focused
     * and deterministic.
     * It is not recommended to modify temperature and top_p for the same completions request as the
     * interaction of these two settings is difficult to predict.
     *
     * @param temperature the temperature value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Get the topP property: An alternative to sampling with temperature called nucleus sampling. This value causes
     * the
     * model to consider the results of tokens with the provided probability mass. As an example, a
     * value of 0.15 will cause only the tokens comprising the top 15% of probability mass to be
     * considered.
     * It is not recommended to modify temperature and top_p for the same completions request as the
     * interaction of these two settings is difficult to predict.
     *
     * @return the topP value.
     */

    public Double getNucleusSamplingFactor() {
        return this.nucleusSamplingFactor;
    }

    /**
     * Set the topP property: An alternative to sampling with temperature called nucleus sampling. This value causes
     * the
     * model to consider the results of tokens with the provided probability mass. As an example, a
     * value of 0.15 will cause only the tokens comprising the top 15% of probability mass to be
     * considered.
     * It is not recommended to modify temperature and top_p for the same completions request as the
     * interaction of these two settings is difficult to predict.
     *
     * @param nucleusSamplingFactor the topP value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setNucleusSamplingFactor(Double nucleusSamplingFactor) {
        this.nucleusSamplingFactor = nucleusSamplingFactor;
        return this;
    }

    /**
     * Get the logitBias property: A map between GPT token IDs and bias scores that influences the probability of
     * specific tokens
     * appearing in a completions response. Token IDs are computed via external tokenizer tools, while
     * bias scores reside in the range of -100 to 100 with minimum and maximum values corresponding to
     * a full ban or exclusive selection of a token, respectively. The exact behavior of a given bias
     * score varies by model.
     *
     * @return the logitBias value.
     */

    public Map<String, Integer> getTokenSelectionBiases() {
        return this.tokenSelectionBiases;
    }

    /**
     * Set the logitBias property: A map between GPT token IDs and bias scores that influences the probability of
     * specific tokens
     * appearing in a completions response. Token IDs are computed via external tokenizer tools, while
     * bias scores reside in the range of -100 to 100 with minimum and maximum values corresponding to
     * a full ban or exclusive selection of a token, respectively. The exact behavior of a given bias
     * score varies by model.
     *
     * @param tokenSelectionBiases the logitBias value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setTokenSelectionBiases(Map<String, Integer> tokenSelectionBiases) {
        this.tokenSelectionBiases = tokenSelectionBiases;
        return this;
    }

    /**
     * Get the user property: An identifier for the caller or end user of the operation. This may be used for tracking
     * or rate-limiting purposes.
     *
     * @return the user value.
     */

    public String getUser() {
        return this.user;
    }

    /**
     * Set the user property: An identifier for the caller or end user of the operation. This may be used for tracking
     * or rate-limiting purposes.
     *
     * @param user the user value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Get the stop property: A collection of textual sequences that will end completions generation.
     *
     * @return the stop value.
     */

    public List<String> getStopSequences() {
        return this.stopSequences;
    }

    /**
     * Set the stop property: A collection of textual sequences that will end completions generation.
     *
     * @param stopSequences the stop value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setStopSequences(List<String> stopSequences) {
        this.stopSequences = stopSequences;
        return this;
    }

    /**
     * Get the presencePenalty property: A value that influences the probability of generated tokens appearing based on
     * their existing
     * presence in generated text.
     * Positive values will make tokens less likely to appear when they already exist and increase the
     * model's likelihood to output new topics.
     *
     * @return the presencePenalty value.
     */

    public Double getPresencePenalty() {
        return this.presencePenalty;
    }

    /**
     * Set the presencePenalty property: A value that influences the probability of generated tokens appearing based on
     * their existing
     * presence in generated text.
     * Positive values will make tokens less likely to appear when they already exist and increase the
     * model's likelihood to output new topics.
     *
     * @param presencePenalty the presencePenalty value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
        return this;
    }

    /**
     * Get the frequencyPenalty property: A value that influences the probability of generated tokens appearing based
     * on their cumulative
     * frequency in generated text.
     * Positive values will make tokens less likely to appear as their frequency increases and
     * decrease the likelihood of the model repeating the same statements verbatim.
     *
     * @return the frequencyPenalty value.
     */

    public Double getFrequencyPenalty() {
        return this.frequencyPenalty;
    }

    /**
     * Set the frequencyPenalty property: A value that influences the probability of generated tokens appearing based
     * on their cumulative
     * frequency in generated text.
     * Positive values will make tokens less likely to appear as their frequency increases and
     * decrease the likelihood of the model repeating the same statements verbatim.
     *
     * @param frequencyPenalty the frequencyPenalty value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
        return this;
    }

    /**
     * Get the seed property: If specified, the system will make a best effort to sample deterministically such that
     * repeated requests with the
     * same seed and parameters should return the same result. Determinism is not guaranteed, and you should refer to
     * the
     * system_fingerprint response parameter to monitor changes in the backend.".
     *
     * @return the seed value.
     */

    public Long getSeed() {
        return this.seed;
    }

    /**
     * Set the seed property: If specified, the system will make a best effort to sample deterministically such that
     * repeated requests with the
     * same seed and parameters should return the same result. Determinism is not guaranteed, and you should refer to
     * the
     * system_fingerprint response parameter to monitor changes in the backend.".
     *
     * @param seed the seed value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setSeed(Long seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Get the logprobs property: Whether to return log probabilities of the output tokens or not. If true, returns the
     * log probabilities of each output token returned in the `content` of `message`. This option is currently not
     * available on the `gpt-4-vision-preview` model.
     *
     * @return the logprobs value.
     */

    public Boolean isLogprobs() {
        return this.includeLogProbabilities;
    }

    /**
     * Set the logprobs property: Whether to return log probabilities of the output tokens or not. If true, returns the
     * log probabilities of each output token returned in the `content` of `message`. This option is currently not
     * available on the `gpt-4-vision-preview` model.
     *
     * @param includeLogProbabilities the logprobs value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setIncludeLogProbabilities(Boolean includeLogProbabilities) {
        this.includeLogProbabilities = includeLogProbabilities;
        return this;
    }

    /**
     * Get the topLogprobs property: An integer between 0 and 5 specifying the number of most likely tokens to return
     * at each token position, each with an associated log probability. `logprobs` must be set to `true` if this
     * parameter is used.
     *
     * @return the topLogprobs value.
     */

    public Integer getLogProbabilityCount() {
        return this.logProbabilityCount;
    }

    /**
     * Set the topLogprobs property: An integer between 0 and 5 specifying the number of most likely tokens to return
     * at each token position, each with an associated log probability. `logprobs` must be set to `true` if this
     * parameter is used.
     *
     * @param logProbabilityCount the topLogprobs value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setLogProbabilityCount(Integer logProbabilityCount) {
        this.logProbabilityCount = logProbabilityCount;
        return this;
    }

    /**
     * Get the responseFormat property: An object specifying the format that the model must output. Used to enable JSON
     * mode.
     *
     * @return the responseFormat value.
     */

    public ChatCompletionsResponseFormat getResponseFormat() {
        return this.responseFormat;
    }

    /**
     * Set the responseFormat property: An object specifying the format that the model must output. Used to enable JSON
     * mode.
     *
     * @param responseFormat the responseFormat value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setResponseFormat(ChatCompletionsResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    /**
     * Get the tools property: The available tool definitions that the chat completions request can use, including
     * caller-defined functions.
     *
     * @return the tools value.
     */

    public List<ChatCompletionsToolDefinition> getTools() {
        return this.tools;
    }

    /**
     * Set the tools property: The available tool definitions that the chat completions request can use, including
     * caller-defined functions.
     *
     * @param tools the tools value to set.
     * @return the ChatCompletionOptions object itself.
     */

    public ChatCompletionOptions setTools(List<ChatCompletionsToolDefinition> tools) {
        this.tools = tools;
        return this;
    }
}
