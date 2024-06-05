# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

- Added `createRunStream`, `createThreadAndRunStream` and `submitToolOutputsToRunStream` methods to `AssistantsClient` and `AssistantsAsyncClient` classes. A suite of classes extending from `StreamUpdate` were added for users to be able to consume the incremental updates from the service
- Added support for vector store, vector store with file and a batch of files operations. 

#### 1. Assistants

- Model updates:
    - `Assistant`, `AssistantCreateOptions` and `UpdateAssistantOptions` models:
        - new fields: `toolResources` , `temperature`, `topP` and `responseFormat`

#### 2. Files

- Model updates:
    - `OpenAIFile` model chages:
        - new fields: `status` of `FileState` type and `status_details` of `string` type.
    - New enum `FileState` representing the type of the `status` field mentioned in the previous point.
    - New possible values for `FilePurpose`: `batch`, `batch_output` and `vision`

#### 3. Messages

- Operation updates: 
  - Updated `listMessages` to accept the filter `runId`

- Model updates:
  - New model `MessageAttachment`
  - Updated docs renaming mentions of `retrieval` tool to `file_search`

#### 4. Run Step

- Model updates:
    - `ThreadRun` model updates:
        - Added fields: `temperature`, `topP`, `maxPromptTokens`, `maxCompletionTokens`, `truncationStrategy`, `toolChoice` and `responseFormat`.
    - Updated documentation for `RunCompletionUsage`
    - `CreateRunOptions` model updates:
        - Added fields: `temperature`, `topP`, `maxPromptTokens`, `maxCompletionTokens`, `truncationStrategy`, `toolChoice` and `responseFormat`
    - `CreateAndRunThreadOptions` model updates:
        - Added fields: `toolResources`, `temperature`, `topP`, `maxPromptTokens`, `maxCompletionTokens`, `truncationStrategy`, `toolChoice` and `responseFormat`.
    - Added new model for all the `truncationStrategy` fields called `TruncationObject`

#### 5. Threads

#### 6. Tools

#### 7. Tool Resources (new)

#### 8. Vector Stores (new)


### Breaking Changes

#### 1. Assistants

- Model updates:
    - `Assistant`, `AssistantCreateOptions` and `UpdateAssistantOptions` models:
        - removed fields: `fileIds`
- Operation updates:
  - Removed operations: `createAssistantFile` , `listAssistantFiles`, `getAssistantFile` and `deleteAssistantFile`

#### 2. Files

- Removed method `uploadFile(FileDetails file, FilePurpose purpose)`. Use `uploadFile(FileDetails file, FilePurpose purpose, String fileName)` instead

#### 3. Messages

- Model updates:
  - Renamed `ThreadInitializationMessage` to `ThreadMessageOptions`.
  - Removed `MessageFile` model.
  - Updated `ThreadMessage` model:
    - The field `incomplete_details` was of the wrong type. Corrected from `MessageIncompleteDetailsReason` -> `MessageIncompleteDetails`.
    - `assistant_id` was marked as optional, but it was in fact nullable
    - `run_id` was marked as optional, but it was in fact nullable
    - removed field `file_ids`
    - Added new field `attachments` a nullable array of `MessageAttachment`
    
- Operation updates:
  - Removed `MessageFile` related operations: `listMessageFiles` and `getMessageFile` 
  - Updated `createMessage` to accept the `ThreadMessageOptions` model (also used in `AssistantThreadCreationOptions`) 

#### 4. Run Step


- Model updates:
    - `ThreadRun` model updates:
        - removed field `fileIds`

#### 5. Threads

#### 6. Tools

#### 7. Tool Resources (new)

#### 8. Vector Stores (new)


### Bugs Fixed

- A combination of inputs for `uploadFile` would allow users to not send `String filename` to the service resulting always in an error, as this is actually mandatory.

### Other Changes

## 1.0.0-beta.2 (2024-02-13)

### Breaking Changes

- The following class have been removed:
  - `FileListResponse`
  - `OpenAIPageableListOfAssistant`
  - `OpenAIPageableListOfAssistantFile`
  - `OpenAIPageableListOfMessageFile`
  - `OpenAIPageableListOfRunStep`
  - `OpenAIPageableListOfThreadMessage`
  - `OpenAIPageableListOfThreadRun` 

- We've introduced `PageableList<T>` these classes were used, except for `FileListResponse` where we simply return `List<OpenAIFile>`.
- If you are using `listFilesWithResponse` and need to manually deserialize the `BinaryData` in the response, you can still use `PageableList<T>` like so:
```java
client.listFilesWithResponse(requestOptions)
    .getValue()
    .toObject(new TypeReference<PageableList<OpenAIFile>>() {})
    .getData();
```

### Other Changes

- On `uploadFile` method in `AssistantsClient` and `AssistantsAsyncClient`, it is required to set the "filename" of the file, via `setFilename` method in `FileDetails` class. The double quote character `"`, the newline charactor `0x0A`, the return charactor `0x0D` in "filename" would be escaped by the client library.


## 1.0.0-beta.1 (2024-02-07)

### Features Added

- This is the initial release of `azure-ai-openai-assistants` client library for OpenAI beta Assistants.
- Full support for OpenAI's beta Assistants features is included:
  - [OpenAI's documentation](https://platform.openai.com/docs/assistants/overview)
  - [Azure OpenAI's documentation](https://learn.microsoft.com/azure/ai-services/openai/assistants-quickstart?tabs=command-line&pivots=programming-language-studio)
