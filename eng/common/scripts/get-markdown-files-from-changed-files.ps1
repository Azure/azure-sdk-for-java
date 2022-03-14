param (
  # The root repo we scaned with.
  [string] $RootRepo = '$PSScriptRoot/../../..',
  # The target branch to compare with.
  [string] $targetBranch = ("origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH}" -replace "/refs/heads/")
)

return get-markdown-files-from-changed-files.ps1 -TargetCommittish $targetBranch -DiffPath '*.md'
