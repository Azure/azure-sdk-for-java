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
.PARAMETER ShowCommands
Optional Show the git commands that would be executed without actually executing them
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
    [string] $PushArgs = "",

    [Parameter(Mandatory = $false)]
    [boolean] $ShowCommands = $false
)

<#
.Synopsis
    Invoke git, handling its quirky stderr that isn't error

.Outputs
    Git messages, and lastly the exit code

.Example
    Invoke-Git push

.Example
    Invoke-Git "add ."
#>
function Invoke-Git
{
param(
[Parameter(Mandatory)]
[string] $Command,
[boolean] $ShowCommands )

    if ($ShowCommands)
    {
        Write-Host "echo git $Command"
        return 0
    }

    try 
    {
        $exit = 0
        $path = [System.IO.Path]::GetTempFileName()

        Write-Host "git $Command"
        Invoke-Expression "git $Command 2>&1 > $path"
        $exit = $LASTEXITCODE
        # Verify that there's actually something to write before trying
        # to access the contents of the file
        if ((Test-Path $path) -and ((Get-Item $path).length -gt 0))
        {
            if ( $exit -gt 0 )
            {
                Write-Error (Get-Content $path).ToString()
            }
            else
            {
                Write-Host (Get-Content $path).ToString()
            }
        }
        return $exit
    }
    # Don't bother trying to catch anything here, if something fails let it bubble up
    # and fail the script since there's not a lot we can actually do about it.
    finally
    {
        if ( Test-Path $path )
        {
            Remove-Item $path
        }
    }
}
# git remote add azure-sdk-fork https://github.com/Azure/azure-sdk-for-java
$exitCode = Invoke-Git "remote add azure-sdk-fork https://github.com/Azure/azure-sdk-for-java.git" $ShowCommands
if ($exitCode -ne 0)
{
    Write-Error "Unable to add remote, see command output above."
    exit $exitCode
}

$exitCode = Invoke-Git "fetch azure-sdk-fork" $ShowCommands
if ($exitCode -ne 0)
{
    Write-Error "Unable to fetch remote, see command output above."
    exit $exitCode
}

$exitCode = Invoke-Git "checkout -b $PRBranchName" $ShowCommands
if ($exitCode -ne 0)
{
    Write-Error "Unable to create branch, see command output above."
    exit $exitCode
}

#echo "git -c user.name=""azure-sdk"" -c user.email=""azuresdk@microsoft.com"" commit -am ""${{ parameters.CommitMsg }}"""
$exitCode = Invoke-Git "-c user.name=`"$($PROwner)`" -c user.email=`"azuresdk@microsoft.com`" commit -am `"$($CommitMsg)`"" $ShowCommands
if ($exitCode -ne 0)
{
    Write-Error "Unable to add files and create commit, see command output above."
    exit $exitCode
}

# Number of times to try this operation, this should be equal or greater than the highest number
# of artifacts that can be released from a given pipeline but cognitive services has 18 so let's
# try something smaller, like 10
# 
$numberOfRetries = 5
$needsRetry = $false
$tryNumber = 0
do 
{ 
    $needsRetry = $false
    $exitCode = Invoke-Git "push azure-sdk-fork $PRBranchName $PushArgs" $ShowCommands
    $tryNumber++
    if ($exitCode -gt 0)
    {
        $needsRetry = $true
        Write-Host "Need to fetch and rebase attempt number=$($tryNumber)"
        $exitCode = Invoke-Git "fetch azure-sdk-fork" $ShowCommands
        if ($exitCode -ne 0)
        {
            Write-Error "Unable to fetch remote, see command output above."
        }
        $exitCode = Invoke-Git "rebase azure-sdk-fork/$($PRBranchName)" $ShowCommands
        if ($exitCode -ne 0)
        {
            Write-Error "Unable to rebase, see command output above."
        }
    }

} while($needsRetry -and $tryNumber -le $numberOfRetries) 

if ($exitCode -ne 0)
{
    Write-Error "Unable to push commit after $($tryNumber) retries, see command output above."
    exit $exitCode
}
