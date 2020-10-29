[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)]
  [ValidateRange(1, 12)]
  [int] $Month
)

$repoRoot = Resolve-Path "$PSScriptRoot/../..";
. ${repoRoot}\eng\common\scripts\SemVer.ps1
. ${repoRoot}\eng\common\scripts\ChangeLog-Operations.ps1
$InstallNotes = "";
$ReleaseNotes = "";

$date = Get-Date -Month $month -Format "yyyy-MM"
$date += "-\d\d"

Get-ChildItem "$repoRoot/sdk" -Filter CHANGELOG.md -Recurse | Sort-Object -Property Name | % {

    $changeLogEntries = Get-ChangeLogEntries -ChangeLogLocation $_
    $package = $_.Directory.Name
    $serviceDirectory = $_.Directory.Parent.Name
    if ($serviceDirectory -ne "resourcemanager" -and $serviceDirectory -ne "spring") {
        foreach ($changeLogEntry in $changeLogEntries.Values)
        {
            if ($changeLogEntry.ReleaseStatus -notmatch $date)
            {

                continue;
            }

            $version = $changeLogEntry.ReleaseVersion
            $githubAnchor = $changeLogEntry.ReleaseTitle.Replace("## ", "").Replace(".", "").Replace("(", "").Replace(")", "").Replace(" ", "-")
            $TextInfo = (Get-Culture).TextInfo
            $changelogTitle = $TextInfo.ToTitleCase($package.Replace("-", " "))

            $InstallNotes += "<dependency>`n  <groupId>com.azure</groupId>`n  <artifactId>$package</artifactId>`n  <version>$version</version>`n</dependency>`n`n";
            $ReleaseNotes += "### $changelogTitle $version [Changelog](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/$serviceDirectory/$package/CHANGELOG.md#$githubAnchor)`n"
            $changeLogEntry.ReleaseContent | %{

                $ReleaseNotes += $_.Replace("###", "####")
                $ReleaseNotes += "`n"
            }
            $ReleaseNotes += "`n"
        }
    }
}

return $InstallNotes, $ReleaseNotes