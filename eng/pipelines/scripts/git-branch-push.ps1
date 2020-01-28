 #!/usr/bin/env pwsh -c

<#
.DESCRIPTION
Create local branch of the given repo and attempt to push changes. The push may fail if
there has been other changes pushed to the same branch, if so, fetch, rebase and try again.
.PARAMETER PRBranchName
The name of the github branch the changes are being put into
.PARAMETER CommitMsg
The message for this particular commit
.PARAMETER GitUrl
The GitHub repository URL
.PARAMETER PushArgs
Optional arguments to the push command
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [Parameter(Mandatory = $true)]
    [string] $PRBranchName,

    [Parameter(Mandatory = $true)]
    [string] $CommitMsg,

    [Parameter(Mandatory = $true)]
    [string] $GitUrl,

    [Parameter(Mandatory = $false)]
    [string] $PushArgs = ""
)

# This is necessay because of the janky git command output writing to stderr.
# Without explicitly setting the 
$ErrorActionPreference = "Continue"

# Because we're going to need to change the user.name and user.email to
# deal with fetching/rebasing, grab the original values and set them
# back before exiting the script
$OriginalUserName = git config user.name
$OriginalUserEmail = git config user.email

try
{
    # Set the user.name and user.email for the account
    Write-Host "git config user.name `"azure-sdk`""
    git config user.name "azure-sdk"
    Write-Host "git config user.email `"azuresdk@microsoft.com`""
    git config user.email "azuresdk@microsoft.com"

    Write-Host "git remote add azure-sdk-fork $GitUrl"
    git remote add azure-sdk-fork $GitUrl
    if ($LASTEXITCODE -ne 0)
    {
        Write-Error "Unable to add remote LASTEXITCODE=$($LASTEXITCODE), see command output above."
        exit $LASTEXITCODE
    }

    Write-Host "git fetch azure-sdk-fork"
    git fetch azure-sdk-fork
    if ($LASTEXITCODE -ne 0)
    {
        Write-Error "Unable to fetch remote LASTEXITCODE=$($LASTEXITCODE), see command output above."
        exit $LASTEXITCODE
    }

    Write-Host "git checkout -b $PRBranchName"
    git checkout -b $PRBranchName
    if ($LASTEXITCODE -ne 0)
    {
        Write-Error "Unable to create branch LASTEXITCODE=$($LASTEXITCODE), see command output above."
        exit $LASTEXITCODE
    }

    #Write-Host "git -c user.name=`"azure-sdk`" -c user.email=`"azuresdk@microsoft.com`" commit -am `"$($CommitMsg)`""
    #git -c user.name=`"azure-sdk`" -c user.email=`"azuresdk@microsoft.com`" commit -am `"$($CommitMsg)`"
    Write-Host "git commit -am `"$($CommitMsg)`""
    git commit -am "$($CommitMsg)"
    if ($LASTEXITCODE -ne 0)
    {
        Write-Error "Unable to add files and create commit LASTEXITCODE=$($LASTEXITCODE), see command output above."
        exit $LASTEXITCODE
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
        Write-Host "git push azure-sdk-fork $PRBranchName $PushArgs"
        git push azure-sdk-fork $PRBranchName $PushArgs
        $tryNumber++
        if ($LASTEXITCODE -ne 0)
        {
            $needsRetry = $true
            Write-Host "Git push failed with LASTEXITCODE=$($LASTEXITCODE) Need to fetch and rebase: attempt number=$($tryNumber)"
            Write-Host "git fetch azure-sdk-fork"
            git fetch azure-sdk-fork
            if ($LASTEXITCODE -ne 0)
            {
                Write-Error "Unable to fetch remote LASTEXITCODE=$($LASTEXITCODE), see command output above."
            }
            Write-Host "git rebase azure-sdk-fork/$($PRBranchName)"
            git rebase azure-sdk-fork/$($PRBranchName)
            if ($LASTEXITCODE -ne 0)
            {
                Write-Error "Unable to rebase LASTEXITCODE=$($LASTEXITCODE), see command output above."
            }
        }

    } while($needsRetry -and $tryNumber -le $numberOfRetries) 

    if ($LASTEXITCODE -ne 0)
    {
        Write-Error "Unable to push commit after $($tryNumber) retries LASTEXITCODE=$($LASTEXITCODE), see command output above."
        exit $LASTEXITCODE
    }
}
finally
{
    Write-Host "Setting the user.name and user.email back to their original setting"
    Write-Host "git config user.name `"$OriginalUserName`""
    git config user.name "$OriginalUserName"
    Write-Host "git config user.email `"$OriginalUserEmail`""
    git config user.email "$OriginalUserEmail"
}