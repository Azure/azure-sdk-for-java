## Customization list

After codegen, the following list of customization need to be restored.

We should find a way to implement these customization using the appropriate tooling when we have the capacity

- Adding `azure-ai-decorators` to `<repo_root>/eng/emitter-package.json` and `<repo_root>/eng/emitter-package-lock.json` 
  - How-to: rename files to `package.json` and `package-lock.json` respectively
  - run `npm install`
  - revert rename
- Removing `ConversationsClientBuilder` and making `ConversationsClient` and `ConversationsAsyncClient` use the `AgentsClientBuilder` instead
  - Just remove the file and revert changes relating to its usage in the corresponding clients
- Added `SLF4J` dependency for logging. Not sure if this is the standardized way, but it was the only way to get something out in the tests.
- Using generated surface for `ResponsesAsyncClient` and `ResponsesClient` but using the OpenAI official SDK for Java to complete the requests.