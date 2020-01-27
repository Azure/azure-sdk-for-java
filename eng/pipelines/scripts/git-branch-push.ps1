 #!/usr/bin/env pwsh -c

<#
.DESCRIPTION
Create local branch of the given repo and attempt to push changes. The push may fail if
there has been other changes pushed to the same branch, if so, fetch, rebase and try again.
.PARAMETER RepoOwner
The local branch name 
.PARAMETER RepoName
The GitHub repository name to create the pull request against.
.PARAMETER PROwner
The owner of the branch we want to create a pull request for.
.PARAMETER CommitMsg
The message for this particular commit
.PARAMETER AuthToken
A personal access token
.PARAMETER PushArgs
Optional arguments to the push command
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [Parameter(Mandatory = $true)]
    [string] $PRBranchName,

    [Parameter(Mandatory = $true)]
    [string] $RepoName,

    [Parameter(Mandatory = $true)]
    [string] $PROwner,

    [Parameter(Mandatory = $true)]
    [string] $CommitMsg,

    [Parameter(Mandatory = $true)]
    [string] $AuthToken,

    [Parameter(Mandatory = $false)]
    [string] $PushArgs = ""
)

Write-Host "git remote add azure-sdk-fork https://$($AuthToken)@github.com/$($PROwner)/$($RepoName).git"
git remote add azure-sdk-fork https://$($AuthToken)@github.com/$($PROwner)/$($RepoName).git
if ($LASTEXITCODE -ne 0)
{
    Write-Error "Unable to add remote, see command output above."
    exit $LASTEXITCODE
}

Write-Host "git fetch azure-sdk-fork"
git fetch azure-sdk-fork
if ($LASTEXITCODE -ne 0)
{
    Write-Error "Unable to fetch remote, see command output above."
    exit $LASTEXITCODE
}

try 
{
    Write-Host "git checkout -b $PRBranchName"
    git checkout -b $PRBranchName
    if ($LASTEXITCODE -ne 0)
    {
        Write-Error "Unable to create branch, see command output above."
        exit $LASTEXITCODE
    }
}
catch 
{
    Write-Host "ExceptionToString = $($_.ToString())"
    Write-Host "ErrorDetails.ToString = $($_.ErrorDetails.ToString())"
    Write-Host "ScriptStackTrace = $($_.ScriptStackTrace)"
    Write-Host "ScriptStackTrace = $($_.FullyQualifiedErrorId)"
}

try 
{
    Write-Host "git -c user.name=`"$($PROwner)`" -c user.email=`"azuresdk@microsoft.com`" commit -am `"$($CommitMsg)`""
    git -c user.name=`"$($PROwner)`" -c user.email=`"azuresdk@microsoft.com`" commit -am `"$($CommitMsg)`"
    if ($LASTEXITCODE -ne 0)
    {
        Write-Error "Unable to add files and create commit, see command output above."
        exit $LASTEXITCODE
    }
}
catch 
{
    Write-Host "ExceptionToString = $($_.ToString())"
    Write-Host "ErrorDetails.ToString = $($_.ErrorDetails.ToString())"
    Write-Host "ScriptStackTrace = $($_.ScriptStackTrace)"
    Write-Host "ScriptStackTrace = $($_.FullyQualifiedErrorId)"
}

# The number of retries can be increased if necessary. In theory, the number of retries
# should be the max number of libraries in the largest pipeline -1 as everything except 
# the first commit could hit issues and need to rebase. The reason this isn't set to that
# is because the largest pipeline is cognitive services which has 18 libraries in its
# pipeline and that just seemed a bit too large and 5 seemed like a good starting value.
$numberOfRetries = 5
$needsRetry = $false
$tryNumber = 0
do 
{ 
    $needsRetry = $false
    git push azure-sdk-fork $PRBranchName $PushArgs
    $tryNumber++
    if ($LASTEXITCODE -gt 0)
    {
        $needsRetry = $true
        try 
        {
            Write-Host "Need to fetch and rebase: attempt number=$($tryNumber)"
            Write-Host "git fetch azure-sdk-fork"
            git fetch azure-sdk-fork
            if ($LASTEXITCODE -ne 0)
            {
                Write-Error "Unable to fetch remote, see command output above."
            }
        }
        catch 
        {
            Write-Host "ExceptionToString = $($_.ToString())"
            Write-Host "ErrorDetails.ToString = $($_.ErrorDetails.ToString())"
            Write-Host "ScriptStackTrace = $($_.ScriptStackTrace)"
            Write-Host "ScriptStackTrace = $($_.FullyQualifiedErrorId)"
        }
        try 
        {
            Write-Host "git rebase azure-sdk-fork/$($PRBranchName)"
            git rebase azure-sdk-fork/$($PRBranchName)
            if ($LASTEXITCODE -ne 0)
            {
                Write-Error "Unable to rebase, see command output above."
            }
        }
        catch 
        {
            Write-Host "ExceptionToString = $($_.ToString())"
            Write-Host "ErrorDetails.ToString = $($_.ErrorDetails.ToString())"
            Write-Host "ScriptStackTrace = $($_.ScriptStackTrace)"
            Write-Host "ScriptStackTrace = $($_.FullyQualifiedErrorId)"
        }
    }

} while($needsRetry -and $tryNumber -le $numberOfRetries) 

if ($LASTEXITCODE -ne 0)
{
    Write-Error "Unable to push commit after $($tryNumber) retries, see command output above."
    exit $LASTEXITCODE
}
