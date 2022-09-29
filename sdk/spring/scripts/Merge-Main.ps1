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
    -SourceBranch main `
    -Theirs @('**') `
    -Ours @('sdk/spring', 'sdk/spring-experimental') `
    -Merge @('eng/versioning/*.txt')
