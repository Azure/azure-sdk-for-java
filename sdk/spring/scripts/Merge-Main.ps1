[CmdLetBinding()]
param(
    [string]$Source = 'main',
    [string[]]$Theirs = @('**'), # paths to always overwrite
    [string[]]$Ours = @('sdk/spring'), # paths to never merge or overwrite
    [string[]]$Merge = @('eng/versioning/*.txt') # paths to merge or overwrite
)

# The net desired effect is:
# - for paths matching $merge, merge changes from $Source allowing the user to resolve conflicts manually
# - ensure files in $Ours remain untouched
# - overwrite everything else matching $Theirs

$mergeExcludes = @($Merge | ForEach-Object { ":(top,glob,exclude)$_" })
$ourExcludes = @($Ours | ForEach-Object { ":(top,glob,exclude)$_" })

# start a merge, but leave it open
&git merge $Source --no-ff --no-commit

# update paths matching "theirs" except for "ours" and "merge" to the state in $Source
&git restore -s $Source --staged --worktree -- ":(top,glob)$Theirs" $ourExcludes $mergeExcludes

# update paths matching "ours" except for "merge" to their pre-merge state
&git restore -s (git rev-parse HEAD) --staged --worktree -- ":(top,glob)$Ours" $mergeExcludes

Write-Host "Merge commit started`n" `
"  Use `"git reset --hard`" to revert the partial merge`n" `
"  Use `"git commit --no-edit`" to complete the merge with the default merge message`n" `
"  Use `"git commit -m <message>`" to complete the merge with a custom message"
