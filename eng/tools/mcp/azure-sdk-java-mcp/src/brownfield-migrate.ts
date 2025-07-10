import { CallToolResult } from "@modelcontextprotocol/sdk/types";

export async function brownfieldMigration(): Promise<CallToolResult> {
  const cookbook = `Follow the instructions below to migrate the Java SDK to generate from TypeSpec.

1. Initiate the Java SDK with the given URL to the "tspconfig.yaml" file.
   Use the tool to initiate the Java SDK, from tspconfig.yaml URL. This tool create a "tsp-location.yaml" file.

2. Find the path to SDK module and its "pom.xml" file.
   Run "git status --porcelain" to find the "tsp-location.yaml" file. The path to the SDK module is the directory containing the "tsp-location.yaml" file. The "pom.xml" file is located in the same directory.

3. Clean the Java source in SDK module.
   Use the tool to clean the Java source in the SDK module.

4. Synchronize the TypeSpec source for Java SDK.
   Use the tool to synchronize the TypeSpec source for the Java SDK. This will download the the TypeSpec source specified in the "tsp-location.yaml" file.

5. Build the Java SDK.
   Use the tool to build the Java SDK. This will compile the Java code and generate the necessary artifacts, e.g. JAR file.

6. Get the changelog for the Java SDK.
   Use the tool to get the changelog for the Java SDK. This will produce a changelog in markdown format.

7. Review the changelog from step 6. Find possible places that can be fixed by renaming a model.
   DO NOT read "CHANGELOG.md" in the folder of the Java SDK module. It is for released library, and not suitable for the migration process.
   The changelog is in JSON format. Focus on "breakingChanges" section and "newFeature" section.
   Find possible places that can be fixed by renaming a model. For example, if a model with name "*Ip*" is removed, and a model with name "*IP*" is added, this can be fixed by rename to "*IP*" model to "*Ip*".
   If there is no such candidates, the migration is completed, and you can skip all the next steps.

8. Apply the changes to the TypeSpec source.
   Use the tool to apply the rename changes to the TypeSpec source. Ask it to e.g. "Update client name, rename model from <old_name> to <new_name>".

9. Go to step 5.
`;

  return {
    content: [
      {
        type: "text",
        text: cookbook,
      },
    ],
  };
}
