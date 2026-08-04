// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Azure AI Foundry Agent Server API — Java adapter library.
 * <p>
 * This package provides the server-side framework for implementing agent servers
 * that run on the Azure AI Foundry hosting infrastructure. It implements the
 * OpenAI Responses API protocol with extensions for the Foundry platform.
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link com.microsoft.agentserver.api.ResponseHandler} — interface to implement
 *       for handling response creation requests (streaming and synchronous)</li>
 *   <li>{@link com.microsoft.agentserver.api.AgentServerResponseEventStream} — fluent builder for
 *       constructing streaming SSE event sequences</li>
 *   <li>{@link com.microsoft.agentserver.api.ResponseContext} — per-request context
 *       providing response ID, conversation history, and input item resolution</li>
 *   <li>{@link com.microsoft.agentserver.api.ResponsesProvider} — pluggable persistence
 *       interface for response state storage</li>
 *   <li>{@link com.microsoft.agentserver.api.FoundryEnvironment} — strongly-typed access
 *       to Foundry platform environment variables</li>
 * </ul>
 *
 * <h2>Quick start</h2>
 * <ol>
 *   <li>Implement {@link com.microsoft.agentserver.api.ResponseHandler}</li>
 *   <li>Register your handler with {@link com.microsoft.agentserver.api.AgentServerResponsesApi}</li>
 *   <li>Deploy as a container with the Foundry platform environment variables set</li>
 * </ol>
 *
 * @see com.microsoft.agentserver.api.ResponseHandler
 * @see com.microsoft.agentserver.api.AgentServerResponseEventStream
 */
package com.microsoft.agentserver.api;

