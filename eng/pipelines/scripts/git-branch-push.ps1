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
[string] $Command)

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
            # Q) Why do we need this janky processing to deal with git output?
            # A) Git and DevOps powershell don't quite place nice. Git 'kinda' 
            #    writes to std error with regular output and even successful
            #    commands could cause a powershell script to bomb out. With that
            #    being said, we are actually expecting push to fail in some cases
            #    in which we'll rebase and try again.
            if ( $exit -gt 0 )
            {
                Write-Error (Get-Content $path -Raw)
            }
            else
            {
                Write-Host (Get-Content $path -Raw)
            }
        }
        return $exit
    }
    catch [System.Management.Automation.RemoteException]
    {
        $errorRecord = $_.Exception.ErrorRecord
        Write-Host "Error, unexpected exception: $_`n$($_.ScriptStackTrace)"
        Write-Host "Error, unexpected exception $($ex) caught."
        Write-Host "ErrorRecord = $($errorRecord)"
        Write-Host "ErrorRecord.ErrorDetails = $($errorRecord.ErrorDetails)"
        Write-Host "ErrorRecord.Exception = $($errorRecord.Exception)"
        Write-Host "ErrorRecord.PSMessageDetails = $($errorRecord.PSMessageDetails)"
        Write-Host "ErrorRecord.FullyQualifiedErrorId = $($errorRecord.FullyQualifiedErrorId)"

    }
    catch
    {
        $msg = $_.Exception.Message
        $ex = $_.Exception.GetType().FullName
        Write-Host "Error, unexpected exception: $_`n$($_.ScriptStackTrace)"
        Write-Host "Error, unexpected exception $($ex) caught."
        Write-Host "Exception message = $($msg)"
        return -1
    }    
    finally
    {
        if ( Test-Path $path )
        {
            Remove-Item $path
        }
    }
}

$exitCode = Invoke-Git "remote add azure-sdk-fork https://$($AuthToken)@github.com/$($PROwner)/$($RepoName).git"
if ($exitCode -ne 0)
{
    Write-Error "Unable to add remote, see command output above."
    exit $exitCode
}

$exitCode = Invoke-Git "fetch azure-sdk-fork"
if ($exitCode -ne 0)
{
    Write-Error "Unable to fetch remote, see command output above."
    exit $exitCode
}

$exitCode = Invoke-Git "checkout -b $PRBranchName"
if ($exitCode -ne 0)
{
    Write-Error "Unable to create branch, see command output above."
    exit $exitCode
}

$exitCode = Invoke-Git "-c user.name=`"$($PROwner)`" -c user.email=`"azuresdk@microsoft.com`" commit -am `"$($CommitMsg)`""
if ($exitCode -ne 0)
{
    Write-Error "Unable to add files and create commit, see command output above."
    exit $exitCode
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
    $exitCode = Invoke-Git "push azure-sdk-fork $PRBranchName $PushArgs"
    $tryNumber++
    if ($exitCode -gt 0)
    {
        $needsRetry = $true
        Write-Host "Need to fetch and rebase: attempt number=$($tryNumber)"
        $exitCode = Invoke-Git "fetch azure-sdk-fork"
        if ($exitCode -ne 0)
        {
            Write-Error "Unable to fetch remote, see command output above."
        }
        $exitCode = Invoke-Git "rebase azure-sdk-fork/$($PRBranchName)"
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
