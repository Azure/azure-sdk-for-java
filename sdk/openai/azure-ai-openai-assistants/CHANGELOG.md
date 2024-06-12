# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

- Added `createRunStream` and `createThreadAndRunStream` methods to `AssistantsClient` and `AssistantsAsyncClient` classes. A suite of classes extending from `StreamUpdate` were added for users to be able to consume the incremental updates from the service

### Breaking Changes

- Removed method `uploadFile(FileDetails file, FilePurpose purpose)`. Use `uploadFile(FileDetails file, FilePurpose purpose, String fileName)` instead.

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
