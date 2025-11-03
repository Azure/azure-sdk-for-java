## API-version upgrade and SDK release

### Generate SDK PR
For releases from Swagger and TypeSpec, run code generation pipelines accordingly. 
If run failed, do check reason.

#### Swagger
If service spec is still Swagger, run [legacy generation pipeline](https://dev.azure.com/azure-sdk/internal/_build?definitionId=2238).
- Select `Release from Swagger`
- Open `Variables`
  - `README`, fill in readme.md location, starting from `specification`. E.g. `specification/cosmos-db/resource-manager/Microsoft.DocumentDB/DocumentDB/readme.md`.
  - `SERVICE`, fill in SDK service name. E.g. For `azure-resourcemanager-cosmos`, fill in `cosmos`.
  - `TAG`, fill in the package tag to generate from. This should be deduced from release request. E.g. `package-2025-10-15`.
  - `VERSION`, fill in the SDK version to release. For stable api-version, it's next stable version. Preview for preview api-version.

Example PR: https://github.com/Azure/azure-sdk-for-java/pull/47140

#### TypeSpec
Service usually do self-serve for already `TypeSpec`ed spec. If not, run [self-serve pipeline](https://dev.azure.com/azure-sdk/internal/_build?definitionId=7421).
Parameters are self-explained. Do ensure `Create SDK pull request` is checked.

Example PR: https://github.com/Azure/azure-sdk-for-java/pull/47100

### Dev on SDK PR
Pull PR branch to your local repository. Further development will base on this branch.

#### Review breaking changes (if any)
Pay attention to `CHANGELOG.md`'s `Breaking Changes` section. Check each breaking change's reason.
If it's acceptable, add suppression in [revapi.json](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/lintingconfigs/revapi/track2/revapi.json).

#### Record test
Refer [Record test](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager-test/README.md#record-test).

#### Local verification
Run `mvn verify` and check for failures. If it passes, push local changes to the PR branch.

#### PR review
Request for PR review. Merge PR after approval.

### Release
In Azure DevOps's internal pipelines, look for `java - xx` pipeline. They can be either `java - containerservice` or `java - azure-resourcemanager-cosmos` depending on different services.
If former, there should be an e.g. `azure-resourcemanager-containerservice` choice unselected by default. Make sure to de-select data-plane SDK and select mgmt SDK.
If latter, there should be no choice.
Run and approve release.

### Post Release
Once release is complete, there should be a follow-up `Increment versions after xx release` PR. E.g. https://github.com/Azure/azure-sdk-for-java/pull/46982
Approve and merge the PR.
