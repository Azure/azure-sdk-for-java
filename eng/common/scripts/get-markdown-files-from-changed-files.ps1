param (
  # The root repo we scaned with.
  [string] $RootRepo = '$PSScriptRoot/../../..',
  # The target branch to compare with.
  [string] $targetBranch = ("origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH}" -replace "/refs/heads/")
)

. (Join-Path $PSScriptRoot get-diff-changes-from-PR.ps1)

return Get-PullRequest-Diff-Changes -TargetCommittish $targetBranch -DiffPath '*.md'
