// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.generated.AIProjectClientTestBase;
import com.azure.ai.projects.models.Agent;
import com.azure.ai.projects.models.CodeInterpreterToolDefinition;
import com.azure.ai.projects.models.CreateAgentOptions;
import com.azure.ai.projects.models.CreateRunOptions;
import com.azure.ai.projects.models.CreateThreadAndRunOptions;
import com.azure.ai.projects.models.FileDetails;
import com.azure.ai.projects.models.FilePurpose;
import com.azure.ai.projects.models.FileSearchToolResource;
import com.azure.ai.projects.models.FunctionDefinition;
import com.azure.ai.projects.models.FunctionToolDefinition;
import com.azure.ai.projects.models.ListSortOrder;
import com.azure.ai.projects.models.MessageAttachment;
import com.azure.ai.projects.models.MessageContent;
import com.azure.ai.projects.models.MessageRole;
import com.azure.ai.projects.models.MessageTextContent;
import com.azure.ai.projects.models.OpenAIFile;
import com.azure.ai.projects.models.RequiredFunctionToolCall;
import com.azure.ai.projects.models.RequiredToolCall;
import com.azure.ai.projects.models.RunStatus;
import com.azure.ai.projects.models.SubmitToolOutputsAction;
import com.azure.ai.projects.models.ThreadMessage;
import com.azure.ai.projects.models.ThreadMessageOptions;
import com.azure.ai.projects.models.ThreadRun;
import com.azure.ai.projects.models.ToolOutput;
import com.azure.ai.projects.models.UpdateAgentOptions;
import com.azure.ai.projects.models.UploadFileRequest;
import com.azure.ai.projects.models.VectorStore;
import com.azure.ai.projects.models.VectorStoreConfiguration;
import com.azure.ai.projects.models.VectorStoreDataSource;
import com.azure.ai.projects.models.VectorStoreDataSourceAssetType;
import com.azure.ai.projects.models.VectorStoreFileStatusFilter;
import com.azure.ai.projects.models.VectorStoreStatus;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentsAsyncClientTest extends AIProjectClientTestBase {

    @Test
    void testCreateAgent() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        String agentName = "basic_example_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini").setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));
        StepVerifier.create(agentsAsyncClient.createAgent(createAgentOptions)).assertNext(agent -> {
            assertNotNull(agent.getId());
        }).verifyComplete();
    }

    @Test
    void testDeleteAgent() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        String agentName = "delete_agent_test_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini").setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));

        StepVerifier.create(agentsAsyncClient.createAgent(createAgentOptions)
            .flatMap(agent -> agentsAsyncClient.deleteAgent(agent.getId()))).assertNext(deletionStatus -> {
                assertNotNull(deletionStatus);
            }).verifyComplete();
    }

    @Test
    void testListAgents() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        // Create an agent first to ensure there's at least one to list
        String agentName = "list_agents_test_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini").setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));

        StepVerifier.create(agentsAsyncClient.createAgent(createAgentOptions).then(agentsAsyncClient.listAgents()))
            .assertNext(agents -> {
                assertNotNull(agents);
            })
            .verifyComplete();

        // Test pagination and sorting
        StepVerifier.create(agentsAsyncClient.listAgents(2, ListSortOrder.DESCENDING, null, null))
            .assertNext(agents -> {
                assertNotNull(agents);
                assertTrue(agents.getData().size() <= 2, "Should have 2 or fewer agents in the list");
            })
            .verifyComplete();
    }

    @Test
    void testUpdateAgent() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        String agentName = "update_agent_test_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini").setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));

        String updatedInstructions = "You are a very helpful and efficient agent";

        StepVerifier.create(agentsAsyncClient.createAgent(createAgentOptions).flatMap(agent -> {
            UpdateAgentOptions updateOptions
                = new UpdateAgentOptions(agent.getId()).setInstructions(updatedInstructions);
            return agentsAsyncClient.updateAgent(updateOptions);
        })).assertNext(agent -> {
            assertNotNull(agent);
            assertEquals(agentName, agent.getName());
            assertEquals(updatedInstructions, agent.getInstructions());
        }).verifyComplete();
    }

    @Test
    void testCreateThread() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        StepVerifier.create(agentsAsyncClient.createThread()).assertNext(thread -> {
            assertNotNull(thread);
            assertNotNull(thread.getId());
        }).verifyComplete();
    }

    @Test
    void testGetThread() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> threadId = new AtomicReference<>();

        StepVerifier.create(agentsAsyncClient.createThread().flatMap(thread -> {
            threadId.set(thread.getId());
            return agentsAsyncClient.getThread(thread.getId());
        })).assertNext(thread -> {
            assertNotNull(thread);
            assertEquals(threadId.get(), thread.getId());
        }).verifyComplete();
    }

    @Test
    void testCreateMessageInThread() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> threadId = new AtomicReference<>();
        String messageContent = "Hello, this is a test message";

        StepVerifier.create(agentsAsyncClient.createThread().flatMap(thread -> {
            threadId.set(thread.getId());
            return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER,
                "I need to solve the equation `3x + 11 = 14`. Can you help me?");
        })).assertNext(message -> {
            assertNotNull(message);
            assertNotNull(message.getId());
            assertEquals(MessageRole.USER, message.getRole());
        }).verifyComplete();
    }

    @Test
    void testCreateAndRunThread() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        String agentName = "run_thread_test_async";
        String userMessage = "What is the current date?";

        StepVerifier.create(createTestAgent(agentsAsyncClient, agentName).flatMap(agent -> {
            agentId.set(agent.getId());

            // Create a thread
            return agentsAsyncClient.createThread();
        }).flatMap(thread -> {
            threadId.set(thread.getId());

            // Add a message to the thread
            return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER,
                "I need to solve the equation `3x + 11 = 14`. Can you help me?");
        }).flatMap(message -> {
            // Create a run using the agent
            CreateRunOptions runOptions
                = new CreateRunOptions(threadId.get(), agentId.get()).setAdditionalInstructions("");
            return agentsAsyncClient.createRun(runOptions);
        }).flatMap(run -> {
            assertNotNull(run.getId());
            assertEquals(threadId.get(), run.getThreadId());

            // Get the run status
            return agentsAsyncClient.getRun(threadId.get(), run.getId());
        })).assertNext(run -> {
            assertNotNull(run);
            assertNotNull(run.getStatus());
        }).verifyComplete();
    }

    @Test
    void testThreadRunLifecycle() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        AtomicReference<String> runId = new AtomicReference<>();

        String agentName = "thread_lifecycle_test_async";
        String userMessage = "Write a hello world program in Python.";

        StepVerifier.create(createTestAgent(agentsAsyncClient, agentName).map(agent -> {
            agentId.set(agent.getId());
            return agent;
        }).flatMap(agent -> agentsAsyncClient.createThread()).map(thread -> {
            threadId.set(thread.getId());
            return thread;
        }).flatMap(thread -> {
            return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER,
                "I need to solve the equation `3x + 11 = 14`. Can you help me?");
        }).flatMap(message -> {
            CreateRunOptions runOptions
                = new CreateRunOptions(threadId.get(), agentId.get()).setAdditionalInstructions("");
            return agentsAsyncClient.createRun(runOptions);
        }).map(run -> {
            runId.set(run.getId());
            return run;
        })).expectNextCount(1).verifyComplete();

        // Wait for the run to complete and check messages
        Mono<ThreadRun> pollForCompletion = agentsAsyncClient.getRun(threadId.get(), runId.get()).expand(run -> {
            if (run.getStatus() == RunStatus.COMPLETED
                || run.getStatus() == RunStatus.FAILED
                || run.getStatus() == RunStatus.CANCELLED) {
                return Mono.empty();
            }
            return Mono.delay(Duration.ofSeconds(2)).then(agentsAsyncClient.getRun(threadId.get(), run.getId()));
        }).last();

        StepVerifier.create(pollForCompletion).assertNext(run -> {
            assertEquals(RunStatus.COMPLETED, run.getStatus());
        }).verifyComplete();

        // Check that assistant messages were created
        StepVerifier.create(agentsAsyncClient.listMessages(threadId.get())).assertNext(messages -> {
            assertTrue(messages.getData().stream().count() >= 1);
        }).verifyComplete();
    }

    @Test
    void testFileOperations() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        // Upload a file
        FileDetails fileDetails
            = new FileDetails(BinaryData.fromString("This is test file content")).setFilename("test_file_async.txt");
        UploadFileRequest uploadRequest = new UploadFileRequest(fileDetails, FilePurpose.AGENTS);

        StepVerifier.create(agentsAsyncClient.uploadFile(uploadRequest).flatMap(file -> {
            assertNotNull(file);
            assertNotNull(file.getId());
            assertEquals("test_file_async.txt", file.getFilename());

            // Get the file
            return agentsAsyncClient.getFile(file.getId()).flatMap(retrievedFile -> {
                assertNotNull(retrievedFile);
                assertEquals(file.getId(), retrievedFile.getId());
                assertEquals(file.getFilename(), retrievedFile.getFilename());

                // List files without parameters
                return agentsAsyncClient.listFiles().flatMap(files -> {
                    assertNotNull(files);
                    assertFalse(files.getData().isEmpty(), "File list should not be empty");

                    boolean foundFile = false;
                    for (OpenAIFile listedFile : files.getData()) {
                        if (listedFile.getId().equals(file.getId())) {
                            foundFile = true;
                            break;
                        }
                    }
                    assertTrue(foundFile, "Uploaded file should be in the file list");

                    // List files with FilePurpose parameter
                    return agentsAsyncClient.listFiles(FilePurpose.AGENTS);
                }).flatMap(filesWithPurpose -> {
                    assertNotNull(filesWithPurpose);
                    assertFalse(filesWithPurpose.getData().isEmpty(), "File list with purpose should not be empty");

                    boolean foundFile = false;
                    for (OpenAIFile listedFile : filesWithPurpose.getData()) {
                        if (listedFile.getId().equals(file.getId())) {
                            foundFile = true;
                            break;
                        }
                    }
                    assertTrue(foundFile, "Uploaded file should be in the filtered file list");

                    // Download file content
                    return agentsAsyncClient.getFile(file.getId()).flatMap(content -> {
                        assertNotNull(content);

                        // Delete file and return the deletion status
                        return agentsAsyncClient.deleteFile(file.getId());
                    });
                });
            });
        })).expectNextCount(1).verifyComplete();
    }

    @Test
    void testFileAttachmentWithCodeInterpreter() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> agentIdRef = new AtomicReference<>();
        AtomicReference<String> threadIdRef = new AtomicReference<>();
        AtomicReference<String> fileIdRef = new AtomicReference<>();

        StepVerifier.create(
            // Create agent with code interpreter
            createTestAgent(agentsAsyncClient, "code_interpreter_file_test_async").flatMap(agent -> {
                agentIdRef.set(agent.getId());

                // Upload file
                FileDetails fileDetails = new FileDetails(BinaryData.fromString(
                    "<html><body><h1>Test Content</h1><p>This is sample data for testing.</p></body></html>"))
                        .setFilename("sample_test_async.html");
                return agentsAsyncClient.uploadFile(new UploadFileRequest(fileDetails, FilePurpose.AGENTS));
            }).flatMap(file -> {
                fileIdRef.set(file.getId());

                // Create thread
                return agentsAsyncClient.createThread();
            }).flatMap(thread -> {
                threadIdRef.set(thread.getId());

                // Create attachment
                CodeInterpreterToolDefinition ciTool = new CodeInterpreterToolDefinition();
                MessageAttachment attachment
                    = new MessageAttachment(Arrays.asList(BinaryData.fromObject(ciTool))).setFileId(fileIdRef.get());

                // Create message with attachment
                return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER,
                    "What does the attachment say?", Arrays.asList(attachment), null);
            }).flatMap(message -> {
                // Create run
                CreateRunOptions runOptions
                    = new CreateRunOptions(threadIdRef.get(), agentIdRef.get()).setAdditionalInstructions("");
                return agentsAsyncClient.createRun(runOptions);
            })).expectNextCount(1).verifyComplete();

        // Cleanup in a separate step
        StepVerifier
            .create(agentsAsyncClient.deleteAgent(agentIdRef.get())
                .then(agentsAsyncClient.deleteThread(threadIdRef.get()))
                .then(agentsAsyncClient.deleteFile(fileIdRef.get())))
            .assertNext(status -> assertNotNull(status))
            .verifyComplete();
    }

    @Test
    void testAgentWithAdditionalMessages() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> agentIdRef = new AtomicReference<>();
        AtomicReference<String> threadIdRef = new AtomicReference<>();

        // Create agent with additional context messages
        StepVerifier.create(createTestAgent(agentsAsyncClient, "additional_message_test_agent_async").flatMap(agent -> {
            agentIdRef.set(agent.getId());
            return agentsAsyncClient.createThread();
        }).flatMap(thread -> {
            threadIdRef.set(thread.getId());
            return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER, "What is the value of Pi?");
        }).flatMap(message -> {
            // Create run with additional messages to influence response
            CreateRunOptions runOptions
                = new CreateRunOptions(threadIdRef.get(), agentIdRef.get()).setAdditionalMessages(Arrays.asList(
                    new ThreadMessageOptions(MessageRole.AGENT, BinaryData.fromString("Pi is exactly 3.")),
                    new ThreadMessageOptions(MessageRole.USER, BinaryData.fromString("Are you sure about Pi?"))));

            return agentsAsyncClient.createRun(runOptions);
        })).expectNextCount(1).verifyComplete();
    }

    @Test
    void testAgentProperties() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        // Test creating an agent with various properties
        String agentName = "properties_test_agent_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini").setName(agentName)
            .setDescription("Agent for testing properties")
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()))
            .setMetadata(mapOf("purpose", "testing"))
            .setTemperature(0.5)
            .setTopP(0.8);

        StepVerifier.create(agentsAsyncClient.createAgent(createAgentOptions).flatMap(agent -> {
            // Verify all properties were set correctly
            assertNotNull(agent.getId());
            assertEquals(agentName, agent.getName());
            assertEquals("Agent for testing properties", agent.getDescription());
            assertEquals("You are a helpful agent", agent.getInstructions());
            assertEquals("gpt-4o-mini", agent.getModel());
            assertEquals(0.5, agent.getTemperature());
            assertEquals(0.8, agent.getTopP());
            assertNotNull(agent.getMetadata());

            // Get agent to verify properties persisted
            return agentsAsyncClient.getAgent(agent.getId()).flatMap(retrievedAgent -> {
                assertEquals(agent.getId(), retrievedAgent.getId());
                assertEquals(agent.getName(), retrievedAgent.getName());
                assertEquals(agent.getDescription(), retrievedAgent.getDescription());
                assertEquals(agent.getTemperature(), retrievedAgent.getTemperature());

                // Cleanup
                return agentsAsyncClient.deleteAgent(agent.getId());
            });
        })).expectNextCount(1).verifyComplete();
    }

    @Test
    void testVectorStore() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> fileIdRef = new AtomicReference<>();
        AtomicReference<String> vectorStoreIdWithConfigRef = new AtomicReference<>();
        AtomicReference<String> vectorStoreIdWithFileRef = new AtomicReference<>();

        String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "");
        VectorStoreDataSource vectorStoreDataSource
            = new VectorStoreDataSource(dataUri, VectorStoreDataSourceAssetType.URI_ASSET);

        // Create vector store with data source config
        StepVerifier.create(agentsAsyncClient
            .createVectorStore(null, "sample_vector_store_async",
                new VectorStoreConfiguration(Arrays.asList(vectorStoreDataSource)), null, null, null)
            .flatMap(vectorStore -> {
                assertNotNull(vectorStore);
                assertNotNull(vectorStore.getId());
                assertNotNull(vectorStore.getStatus());
                assertNotNull(vectorStore.getName());
                assertNotNull(vectorStore.getFileCounts());
                assertEquals("sample_vector_store_async", vectorStore.getName());
                vectorStoreIdWithConfigRef.set(vectorStore.getId());

                // Update vector store
                return agentsAsyncClient.modifyVectorStore(vectorStore.getId());
            })
            .flatMap(updatedVectorStore -> {
                // Upload a file
                return agentsAsyncClient.uploadFile(new UploadFileRequest(new FileDetails(BinaryData
                    .fromString("The word `apple` uses the code 442345, while the word `banana` uses the code 673457."))
                        .setFilename("sample_file_for_upload_async.txt"),
                    FilePurpose.AGENTS));
            })
            .flatMap(file -> {
                fileIdRef.set(file.getId());
                assertNotNull(file);
                assertNotNull(file.getId());
                assertEquals("sample_file_for_upload_async.txt", file.getFilename());

                // Create vector store with file ID
                return agentsAsyncClient.createVectorStore(Arrays.asList(file.getId()), "my_vector_store_async", null,
                    null, null, null);
            })).assertNext(vectorStore -> {
                assertNotNull(vectorStore);
                assertNotNull(vectorStore.getId());
                assertEquals("my_vector_store_async", vectorStore.getName());
                vectorStoreIdWithFileRef.set(vectorStore.getId());
            }).verifyComplete();

        // Poll until vector store is complete
        Mono<VectorStore> pollForCompletion
            = agentsAsyncClient.getVectorStore(vectorStoreIdWithFileRef.get()).expand(vectorStore -> {
                if (vectorStore.getStatus() == VectorStoreStatus.COMPLETED
                    || vectorStore.getStatus() == VectorStoreStatus.EXPIRED) {
                    return Mono.empty();
                }
                return Mono.delay(Duration.ofMillis(500)).then(agentsAsyncClient.getVectorStore(vectorStore.getId()));
            }).last();

        StepVerifier.create(pollForCompletion).assertNext(vectorStore -> {
            assertEquals(VectorStoreStatus.COMPLETED, vectorStore.getStatus());
        }).verifyComplete();

        // List vector store files with pagination and sorting
        StepVerifier.create(agentsAsyncClient.listVectorStoreFiles(vectorStoreIdWithFileRef.get()).flatMap(files -> {
            assertFalse(files.getData().isEmpty());

            // Test pagination and sorting options
            return agentsAsyncClient.listVectorStoreFiles(vectorStoreIdWithFileRef.get(),
                VectorStoreFileStatusFilter.COMPLETED, 1, ListSortOrder.ASCENDING, null, null);
        })).assertNext(files -> {
            assertFalse(files.getData().isEmpty());
        }).verifyComplete();

        // List vector stores with pagination and sorting
        StepVerifier.create(agentsAsyncClient.listVectorStores().flatMap(vectorStores -> {
            assertFalse(vectorStores.getData().isEmpty());

            // Test pagination and sorting options
            return agentsAsyncClient.listVectorStores(1, ListSortOrder.ASCENDING, null, null);
        })).assertNext(vectorStores -> {
            assertFalse(vectorStores.getData().isEmpty());
        }).verifyComplete();

        // Test with file search tool
        StepVerifier
            .create(Mono
                .just(new FileSearchToolResource().setVectorStoreIds(Arrays.asList(vectorStoreIdWithFileRef.get()))))
            .assertNext(fileSearchToolResource -> {
                assertNotNull(fileSearchToolResource);
                assertEquals(1, fileSearchToolResource.getVectorStoreIds().size());
                assertEquals(vectorStoreIdWithFileRef.get(), fileSearchToolResource.getVectorStoreIds().get(0));
            })
            .verifyComplete();

        // Clean up resources
        StepVerifier.create(agentsAsyncClient.deleteVectorStore(vectorStoreIdWithFileRef.get())
            .then(agentsAsyncClient.deleteVectorStore(vectorStoreIdWithConfigRef.get()))
            .then(agentsAsyncClient.deleteFile(fileIdRef.get()))).expectNextCount(1).verifyComplete();
    }

    @Test
    void testVectorStoreFileBatch() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> fileIdRef = new AtomicReference<>();
        AtomicReference<String> vectorStoreIdRef = new AtomicReference<>();

        StepVerifier.create(
            // Upload a file
            agentsAsyncClient.uploadFile(
                new UploadFileRequest(new FileDetails(BinaryData.fromString("File batch content for vector store test"))
                    .setFilename("vector_store_batch_async_test.txt"), FilePurpose.AGENTS))
                .flatMap(file -> {
                    fileIdRef.set(file.getId());

                    // Create empty vector store
                    return agentsAsyncClient.createVectorStore(null, "async_vector_batch_test", null, null, null, null);
                })
                .flatMap(vectorStore -> {
                    vectorStoreIdRef.set(vectorStore.getId());

                    // Create vector store file batch
                    return agentsAsyncClient.createVectorStoreFileBatch(vectorStore.getId(),
                        Arrays.asList(fileIdRef.get()), null, null);
                }))
            .assertNext(batch -> {
                assertNotNull(batch);
                assertNotNull(batch.getId());
                assertEquals(vectorStoreIdRef.get(), batch.getVectorStoreId());
            })
            .verifyComplete();

        // Clean up resources
        StepVerifier
            .create(agentsAsyncClient.deleteVectorStore(vectorStoreIdRef.get())
                .then(agentsAsyncClient.deleteFile(fileIdRef.get())))
            .assertNext(status -> assertNotNull(status))
            .verifyComplete();
    }

    @Test
    void testDeleteThread() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        StepVerifier.create(agentsAsyncClient.createThread().flatMap(thread -> {
            assertNotNull(thread);
            assertNotNull(thread.getId());
            return agentsAsyncClient.deleteThread(thread.getId());
        })).assertNext(status -> assertNotNull(status)).verifyComplete();
    }

    @Test
    void testListMessages() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();
        AtomicReference<String> threadId = new AtomicReference<>();

        StepVerifier.create(
            // Create a thread
            agentsAsyncClient.createThread().flatMap(thread -> {
                threadId.set(thread.getId());
                // Add multiple messages
                return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER, "First message");
            }).flatMap(message -> {
                return agentsAsyncClient.createMessage(threadId.get(), MessageRole.USER, "Second message");
            }).flatMap(message -> {
                // List messages
                return agentsAsyncClient.listMessages(threadId.get());
            })).assertNext(messageList -> {
                assertNotNull(messageList);
                assertEquals(2, messageList.getData().size());
                assertEquals(MessageRole.USER, messageList.getData().get(0).getRole());
                assertEquals(MessageRole.USER, messageList.getData().get(1).getRole());
            }).verifyComplete();

        // Clean up
        StepVerifier.create(agentsAsyncClient.deleteThread(threadId.get())).expectNextCount(1).verifyComplete();
    }

    @Test
    void testGetMessage() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();
        AtomicReference<String> threadId = new AtomicReference<>();
        AtomicReference<String> messageId = new AtomicReference<>();

        StepVerifier.create(
            // Create a thread
            agentsAsyncClient.createThread().flatMap(thread -> {
                threadId.set(thread.getId());
                // Add a message
                return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER, "Test message for retrieval");
            }).flatMap(message -> {
                messageId.set(message.getId());
                // Get the message
                return agentsAsyncClient.getMessage(threadId.get(), message.getId());
            })).assertNext(message -> {
                assertNotNull(message);
                assertEquals(messageId.get(), message.getId());
                assertEquals(MessageRole.USER, message.getRole());
                assertTrue(message.getContent().get(0) instanceof MessageTextContent);
                assertEquals("Test message for retrieval",
                    ((MessageTextContent) message.getContent().get(0)).getText().getValue());
            }).verifyComplete();

        // Clean up
        StepVerifier.create(agentsAsyncClient.deleteThread(threadId.get())).expectNextCount(1).verifyComplete();
    }

    @Test
    void testSubmitToolOutputsToRun() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> threadId = new AtomicReference<>();
        AtomicReference<String> agentId = new AtomicReference<>();

        FunctionToolDefinition getWeatherTool = new FunctionToolDefinition(new FunctionDefinition("getWeather",
            BinaryData.fromObject(mapOf("type", "object", "properties",
                mapOf("location", mapOf("type", "string", "description", "The city name")), "required",
                new String[] { "location" }))).setDescription("Get weather for a location"));

        // Create agent with function tool
        StepVerifier.create(
            agentsAsyncClient.createAgent(new CreateAgentOptions("gpt-4o-mini").setName("tool_output_test_async")
                .setInstructions("You help with weather information")
                .setTools(Arrays.asList(getWeatherTool))).flatMap(agent -> {
                    agentId.set(agent.getId());
                    return agentsAsyncClient.createThread();
                }).flatMap(thread -> {
                    threadId.set(thread.getId());
                    return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER,
                        "What's the weather in Seattle?");
                }).flatMap(message -> {
                    CreateRunOptions runOptions = new CreateRunOptions(threadId.get(), agentId.get());
                    return agentsAsyncClient.createRun(runOptions);
                }))
            .expectNextCount(1)
            .verifyComplete();

        // Poll until we get REQUIRES_ACTION status or completion
        Mono<ThreadRun> waitForRequiresAction = Mono.defer(() -> agentsAsyncClient.getRun(threadId.get(),
            agentsAsyncClient.listRuns(threadId.get()).block().getData().get(0).getId())).expand(run -> {
                if (run.getStatus() == RunStatus.REQUIRES_ACTION
                    || run.getStatus() == RunStatus.COMPLETED
                    || run.getStatus() == RunStatus.FAILED) {
                    return Mono.empty();
                }
                return Mono.delay(Duration.ofSeconds(1)).then(agentsAsyncClient.getRun(threadId.get(), run.getId()));
            }).filter(run -> run.getStatus() == RunStatus.REQUIRES_ACTION).next();

        // Test submitting tool outputs when the run requires action
        StepVerifier.create(waitForRequiresAction.flatMap(run -> {
            if (run.getRequiredAction() instanceof SubmitToolOutputsAction) {
                SubmitToolOutputsAction action = (SubmitToolOutputsAction) run.getRequiredAction();
                List<ToolOutput> outputs = new ArrayList<>();

                for (RequiredToolCall call : action.getSubmitToolOutputs().getToolCalls()) {
                    if (call instanceof RequiredFunctionToolCall) {
                        RequiredFunctionToolCall functionCall = (RequiredFunctionToolCall) call;
                        if ("getWeather".equals(functionCall.getFunction().getName())) {
                            outputs
                                .add(new ToolOutput().setToolCallId(functionCall.getId()).setOutput("72°F and sunny"));
                        }
                    }
                }

                return agentsAsyncClient.submitToolOutputsToRun(threadId.get(), run.getId(), outputs);
            }
            return Mono.just(run); // No action required
        })).assertNext(run -> {
            assertNotNull(run);
        }).verifyComplete();

        // Clean up
        StepVerifier
            .create(agentsAsyncClient.deleteAgent(agentId.get()).then(agentsAsyncClient.deleteThread(threadId.get())))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void testRunOperations() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();
        AtomicReference<String> threadIdRef = new AtomicReference<>();
        AtomicReference<String> runIdRef = new AtomicReference<>();
        AtomicReference<String> agentIdRef = new AtomicReference<>();

        // Create thread, send message and create run
        StepVerifier.create(createTestAgent(agentsAsyncClient, "run_operations_test_async")
            .doOnNext(agent -> agentIdRef.set(agent.getId()))
            .flatMap(agent -> agentsAsyncClient.createThread())
            .flatMap(thread -> {
                assertNotNull(thread);
                threadIdRef.set(thread.getId());
                // Create first message
                return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER,
                    "what is the value of y: x = 5; y = x + 1;");
            })
            .flatMap(message -> {
                // Create run
                CreateRunOptions runOptions = new CreateRunOptions(threadIdRef.get(), agentIdRef.get());
                return agentsAsyncClient.createRun(runOptions);
            })
            .map(run -> {
                runIdRef.set(run.getId());
                return run;
            })).expectNextCount(1).verifyComplete();

        // Wait for run to complete
        Mono<ThreadRun> pollForCompletion = agentsAsyncClient.getRun(threadIdRef.get(), runIdRef.get()).expand(run -> {
            if (run.getStatus() == RunStatus.COMPLETED
                || run.getStatus() == RunStatus.FAILED
                || run.getStatus() == RunStatus.CANCELLED) {
                threadIdRef.set(run.getThreadId());
                runIdRef.set(run.getId());
                return Mono.empty();
            }
            return Mono.delay(Duration.ofMillis(500)).then(agentsAsyncClient.getRun(run.getThreadId(), run.getId()));
        }).last();

        // Get specific run step
        StepVerifier
            .create(pollForCompletion.flatMap(run -> agentsAsyncClient.listRunSteps(threadIdRef.get(), runIdRef.get()))
                .flatMap(runStepsList -> {
                    String stepId = runStepsList.getData().get(0).getId();
                    // Get individual run step
                    return agentsAsyncClient.getRunStep(threadIdRef.get(), runIdRef.get(), stepId);
                }))
            .assertNext(runStep -> {
                assertNotNull(runStep);
                assertNotNull(runStep.getId());
            })
            .verifyComplete();

        // List run steps with pagination and sorting
        StepVerifier
            .create(agentsAsyncClient.listRunSteps(threadIdRef.get(), runIdRef.get(), null, 1, ListSortOrder.ASCENDING,
                null, null))
            .assertNext(runStepsList -> {
                assertNotNull(runStepsList);
                assertTrue(runStepsList.getData().size() > 0, "Should have run steps in paginated list");
            })
            .verifyComplete();

        // Wait for run to complete and update it
        StepVerifier
            .create(pollForCompletion
                .flatMap(completedRun -> agentsAsyncClient.updateRun(threadIdRef.get(), runIdRef.get())))
            .assertNext(updatedRun -> {
                assertNotNull(updatedRun);
                assertEquals(runIdRef.get(), updatedRun.getId());
            })
            .verifyComplete();

        // List runs
        StepVerifier.create(agentsAsyncClient.listRuns(threadIdRef.get())).assertNext(runsList -> {
            assertNotNull(runsList);
            assertEquals(1, runsList.getData().size(), "Should have 1 run in the thread");

            boolean foundRun1 = false;
            for (ThreadRun run : runsList.getData()) {
                if (run.getId().equals(runIdRef.get())) {
                    foundRun1 = true;
                    break;
                }
            }
            assertTrue(foundRun1, "Run 1 should be in the list");
        }).verifyComplete();

        // List runs with pagination and sorting
        StepVerifier.create(agentsAsyncClient.listRuns(threadIdRef.get(), 1, ListSortOrder.ASCENDING, null, null))
            .assertNext(runsList -> {
                assertNotNull(runsList);
                assertEquals(1, runsList.getData().size(), "Should have 1 run in paginated list");
            })
            .verifyComplete();

        // Create thread and run in one operation
        AtomicReference<String> threadId2Ref = new AtomicReference<>();
        StepVerifier.create(agentsAsyncClient.createThreadAndRun(new CreateThreadAndRunOptions(agentIdRef.get())))
            .assertNext(run2 -> {
                assertNotNull(run2);
                assertNotNull(run2.getId());
                assertNotNull(run2.getThreadId());
                threadId2Ref.set(run2.getThreadId());
            })
            .verifyComplete();

        // Clean up the second thread
        if (threadId2Ref.get() != null) {
            StepVerifier.create(agentsAsyncClient.deleteThread(threadId2Ref.get())).expectNextCount(1).verifyComplete();
        }

        // Clean up the first thread
        StepVerifier.create(agentsAsyncClient.deleteThread(threadIdRef.get())).expectNextCount(1).verifyComplete();
    }

    @Test
    void testCreateRunAndReadMessages() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        AtomicReference<String> runId = new AtomicReference<>();

        // Create a test agent first
        StepVerifier.create(createTestAgent(agentsAsyncClient, "run_messages_test_async").flatMap(agent -> {
            agentId.set(agent.getId());

            // Create a thread
            return agentsAsyncClient.createThread();
        }).flatMap(thread -> {
            threadId.set(thread.getId());

            // Add a message to the thread
            return agentsAsyncClient.createMessage(thread.getId(), MessageRole.USER,
                "I need to solve the equation `3x + 11 = 14`. Can you help me?");
        }).flatMap(message -> {
            // Create a run using the agent
            CreateRunOptions runOptions
                = new CreateRunOptions(threadId.get(), agentId.get()).setAdditionalInstructions("");
            return agentsAsyncClient.createRun(runOptions);
        })).assertNext(run -> {
            assertNotNull(run);
            runId.set(run.getId());
            assertNotNull(run.getId());
            assertEquals(threadId.get(), run.getThreadId());
        }).verifyComplete();

        // Wait for the run to complete
        Mono<ThreadRun> pollForCompletion = agentsAsyncClient.getRun(threadId.get(), runId.get()).expand(run -> {
            if (run.getStatus() == RunStatus.COMPLETED
                || run.getStatus() == RunStatus.FAILED
                || run.getStatus() == RunStatus.CANCELLED) {
                return Mono.empty();
            }
            return Mono.delay(Duration.ofMillis(500)).then(agentsAsyncClient.getRun(threadId.get(), runId.get()));
        }).last();

        // Verify run completed successfully
        StepVerifier.create(pollForCompletion).assertNext(run -> {
            assertEquals(RunStatus.COMPLETED, run.getStatus());
        }).verifyComplete();

        // Test listing messages with pagination and sorting
        StepVerifier
            .create(agentsAsyncClient.listMessages(threadId.get(), runId.get(), 2, ListSortOrder.ASCENDING, null, null))
            .assertNext(messages -> {
                assertNotNull(messages);
                assertFalse(messages.getData().isEmpty(), "Messages list should not be empty");

                // Verify we have the expected messages
                boolean foundUserMessage = false;
                boolean foundAgentMessage = false;

                for (ThreadMessage message : messages.getData()) {
                    for (MessageContent contentItem : message.getContent()) {
                        if (contentItem instanceof MessageTextContent) {
                            String content = ((MessageTextContent) contentItem).getText().getValue();
                            if (message.getRole() == MessageRole.USER && content.contains("3x + 11 = 14")) {
                                foundUserMessage = true;
                            } else if (message.getRole() == MessageRole.AGENT && content.contains("x = 1")) {
                                foundAgentMessage = true;
                            }
                        }
                    }
                }

                assertTrue(foundUserMessage || foundAgentMessage,
                    "Should find user or agent message with expected content");
            })
            .verifyComplete();

        // Clean up resources
        StepVerifier
            .create(agentsAsyncClient.deleteAgent(agentId.get()).then(agentsAsyncClient.deleteThread(threadId.get())))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void testThreadOperations() {
        AgentsAsyncClient agentsAsyncClient = getAIProjectClientBuilder().buildAgentsAsyncClient();

        AtomicReference<String> threadId = new AtomicReference<>();
        AtomicReference<String> messageId1 = new AtomicReference<>();
        AtomicReference<String> messageId2 = new AtomicReference<>();

        // Create a new thread
        StepVerifier.create(agentsAsyncClient.createThread().flatMap(thread -> {
            assertNotNull(thread);
            assertNotNull(thread.getId());
            threadId.set(thread.getId());

            // Update thread
            return agentsAsyncClient.updateThread(thread.getId());
        }).flatMap(updatedThread -> {
            // Get thread to verify update
            return agentsAsyncClient.getThread(threadId.get());
        }).flatMap(retrievedThread -> {
            // Verify thread was retrieved correctly
            assertEquals(threadId.get(), retrievedThread.getId());

            // Create first message
            return agentsAsyncClient.createMessage(threadId.get(), MessageRole.USER, "First message");
        }).flatMap(message1 -> {
            messageId1.set(message1.getId());
            assertNotNull(message1);

            // Create second message
            return agentsAsyncClient.createMessage(threadId.get(), MessageRole.USER, "Second message");
        }).flatMap(message2 -> {
            messageId2.set(message2.getId());
            assertNotNull(message2);
            assertNotEquals(messageId1.get(), message2.getId());

            // List messages
            return agentsAsyncClient.listMessages(threadId.get());
        })).assertNext(messages -> {
            assertNotNull(messages);
            assertEquals(2, messages.getData().size());
        }).verifyComplete();

        // Get a specific message
        StepVerifier.create(agentsAsyncClient.getMessage(threadId.get(), messageId1.get()))
            .assertNext(retrievedMessage -> {
                assertNotNull(retrievedMessage);
                assertEquals(messageId1.get(), retrievedMessage.getId());
            })
            .verifyComplete();

        // Delete thread and verify
        StepVerifier.create(agentsAsyncClient.deleteThread(threadId.get()))
            .assertNext(deletionStatus -> assertNotNull(deletionStatus))
            .verifyComplete();

        // Verify deletion (should emit an error)
        StepVerifier.create(agentsAsyncClient.getThread(threadId.get())).expectError().verify();
    }

    // Helper method to map objects
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }

    // Helper method to create test agents
    private Mono<Agent> createTestAgent(AgentsAsyncClient client, String name) {
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName(name).setInstructions("Test agent for " + name);
        return client.createAgent(options);
    }
}
