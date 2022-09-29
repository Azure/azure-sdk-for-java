# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Use case: This script merges changes from a source branch into the current branch with the behavior:
# 1. Overwrite paths matching $Theirs (this includes deletes)
# 2. Ensure files in $Ours remain untouched
# 3. For paths matching $Merge, merge changes from $SourceBranch allowing the user to resolve conflicts manually
#
# Adding paths to $Merge excludes them from the default keep or overwrite behaviour of $Ours and $Theirs.
#
# This script can be run locally from the root of the repo:
# .\eng\scripts\Merge-Branch.ps1 -SourceBranch 'main' -Theirs '**' -Ours 'sdk/template' -Merge 'sdk/template/ci.yml', '**/README.md'
#
# This would merge main into the local branch, making the working folder look like main. It will not overwrite sdk\template.
# Changes in sdk\template\ci.yml and readme.md files would be merged, not excluded or overwritten.

[CmdLetBinding()]
param(
    [string]$SourceBranch,
    [string[]]$Theirs, # paths to always overwrite
    [string[]]$Ours, # paths to never merge or overwrite
    [string[]]$Merge # paths to merge or overwrite
)

# Pathspec glossary entry: https://git-scm.com/docs/gitglossary#Documentation/gitglossary.txt-aiddefpathspecapathspec
#
# - They're space separeted strings that match file paths in the repository
# - They're repository paths, not filesystem paths and are slash direction sensitive.
# - They support "magic words" between parenthesis that control how the following path matches files:
#   - top: treat the path a top level / repository rooted
#   - glob: treat wildcards using glob patterns
#       /*/ == single wildcarded directory level
#       /**/ == all subdirectories, recursive
#   - exclude: after processing other pathspecs, remove any path matching this pathspec from the results

# Apply git pathspec magic to the paths
$theirIncludes = @($Theirs | ForEach-Object { ":(top,glob)$_" })
$ourIncludes = @($Ours | ForEach-Object { ":(top,glob)$_" })
$mergeExcludes = @($Merge | ForEach-Object { ":(top,glob,exclude)$_" })
$ourExcludes = @($Ours | ForEach-Object { ":(top,glob,exclude)$_" })

function ErrorExit($exitCode) {
    Write-Host "`nError creating merge commit`n" `
    "  Your local repository is in an unknown state`n" `
    "  Run `"git reset --hard`" to revert the partial merge"

    exit $exitCode
}

# start a merge, but leave it open
Write-Verbose "git merge $SourceBranch --no-ff --no-commit"
git merge $SourceBranch --no-ff --no-commit
if ($LASTEXITCODE) { ErrorExit $LASTEXITCODE }

# update paths matching "theirs", except for "ours" and "merge", to the state in $SourceBranch
if ($Theirs.Length) {
    Write-Verbose "git restore -s $SourceBranch --staged --worktree -- $theirIncludes $ourExcludes $mergeExcludes"
    git restore -s $SourceBranch --staged --worktree -- $theirIncludes $ourExcludes $mergeExcludes
    if ($LASTEXITCODE) { ErrorExit $LASTEXITCODE }
}

# update paths matching "ours", except for "merge", to their pre-merge state
if ($Ours.Length) {
    Write-Verbose "git restore -s (git rev-parse HEAD) --staged --worktree -- $ourIncludes $mergeExcludes"
    git restore -s (git rev-parse HEAD) --staged --worktree -- $ourIncludes $mergeExcludes
    if ($LASTEXITCODE) { ErrorExit $LASTEXITCODE }
}

Write-Host "Merge commit started`n" `
"  Use `"git reset --hard`" to revert the partial merge`n" `
"  Use `"git commit --no-edit`" to complete the merge with the default merge message`n" `
"  Use `"git commit -m <message>`" to complete the merge with a custom message"

exit 0
