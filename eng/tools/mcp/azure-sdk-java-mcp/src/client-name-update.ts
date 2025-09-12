import { CallToolResult } from "@modelcontextprotocol/sdk/types";

export async function clientNameUpdateCookbook(): Promise<CallToolResult> {
    const cookbook = `
  Follow below instruction to update old client name to new client name in both client.tsp and the generated Java SDK.

# How to Update the Client Name in the Generated Java SDK

1. **Synchronize the TypeSpec source for Java SDK**

  Check if there is already a directory named 'TempTypeSpecFiles'. If there is not, find the directory in the workspace that contains 'tsp-location.yaml'. Use the tool to synchronize the TypeSpec source for the Java SDK.

2. **Look at all \`.tsp\` files under folder 'TempTypeSpecFiles' and get the path of the Model or Operation or operation parameter declaration with the old client name**

  Look for the model or operation you want to rename under '.tsp' files. Get the path of the model or operation. For example, model \`OldModelName\`'s path is 'Azure.Communication.MessagesService.OldModelName', operation 'sendMessage''s path is 'Azure.Communication.MessagesService.AdminOperations.sendMessage'.
  \`\`\`typespec
  namespace Azure.Communication.MessagesService;

  model OldModelName {
  // model properties
  }

  interface AdminOperations {
    @path("/messages")
    operation sendMessage(@body message: OldModelName): void;
  }
  \`\`\`

3. **Update client.tsp**

  Use and founded path and @clientName decorator to update the client name to the new client name. Make sure you only update client.tsp file, other .tsp files should not be updated.
  For example, for model \`OldModelName\`, you can add below line to \`NewModelName\` to client.tsp like this. Update operation name or operation parameter name are similar.
  \`\`\`typespec
  
@@clientName(Azure.Communication.MessagesService.OldModelName,
  "NewModelName",
  "java"
);
  \`\`\`

4. Print a .diff file in current working directory on what you have changed in the client.tsp file.

5. **Generate the Java SDK**

  Find the directory in the workspace that contains 'tsp-location.yaml'. Use the tool to generate the Java SDK.

6. **Update Downstream Code**


  If Check the current working directory, if there is existing java code or documentation that references the old class, update those references to the new class.

7. **Verify the Changes**
  Call the tool to build java sdk. If build fails, check the error message and fix the code until build succeeds.
  After build succeeds, if the old class is not used anywhere,  delete the old class file.

8. **Commit the Changes**
  Ask the user to commit the SDK changes to the repository. And open the .diff file to review the changes and apply the changes to azure-rest-api-specs repository.
---

**Tip:** Use your IDE’s “rename symbol” or “find and replace” feature to ensure you update all references safely.
  `;

    console.error(`Generated client name update cookbook:\n${cookbook}`);

    return {
        content: [
            {
                type: "text",
                text: cookbook,
            },
        ],
    };
}
