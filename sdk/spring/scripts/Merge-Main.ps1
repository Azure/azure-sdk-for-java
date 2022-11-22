# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Use case: This script merges changes from main into the current branch with the behavior:
# 1. Overwrite everything except for Ours and Merge
# 2. For paths in Merge, merge the changes and allow the caller to resolve conflicts
# 3. For paths in Ours, don't do anything

[CmdLetBinding()]
param(
)

& "$PSScriptRoot\..\..\..\eng\scripts\Merge-Branch.ps1" `
    -SourceBranch 'main' `
    -Theirs @('**') `
    -Ours @('sdk/spring', 'sdk/spring-experimental') `
    -Merge @('eng/versioning/version_*.txt', 'eng/jacoco-test-coverage/pom.xml', 'sdk/parents/azure-client-sdk-parent/pom.xml', 'sdk/cosmos/azure-spring-data-cosmos-test/pom.xml', 'sdk/appconfiguration/azure-spring-cloud-test-appconfiguration-config/pom.xml')
