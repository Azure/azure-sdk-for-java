<#
.SYNOPSIS
Captures and processes Checkstyle, RevApi, and Spotbugs linting reports.

.DESCRIPTION
Given an SDK directory this will capture the Checkstyle, RevApi, and Spotbugs linting reports.
Found Checkstyle and Spotbugs reports will be converted from their XML object representation to a more easily parsed
CSV.

For each SDK containing non-empty linting reports, found in /target after running build verification, a folder with the
SDK name will be created. The sub-folder may contain a processed Checkstyle report, processed Spotbugs report, and the
default RevApi report. To prevent potential collisions of SDKs the sub-folder will use <SDK group>-<SDK>, so for
azure-core it will be core_azure-core.

The searched for reports are /target/checkstyle-result.xml, /target/revapi.json, and /target/spotbugs.xml. There is no
guarantee that each, or any, of these reports will exist, so if processing is safe if the file doesn't exist.

For Checkstyle the XML report will have the file name, line number, column number, linting error message, and linting
error reported. For Spotbugs the XML will have the file name, line number, linting error message, and linting error
reported, Spotbugs doesn't report the column number.

.PARAMETER StagingDirectory
The directory where the linting reports will be output.

.PARAMETER SdkDirectory
An optional SDK directory, such as core, that scopes the linting report searching and processing to the specific SDK
directory. If this isn't passed all SDK directories will be searched and processed.
#>

param(
  [Parameter(Mandatory = $true)]
  [string]$StagingDirectory,

  [Parameter(Mandatory = $false)]
  [string]$SdkDirectory
)

function NewFolderIfNotExists($FolderPath) {
  if (!(Test-Path -Path $FolderPath)) {
    New-Item -Path $FolderPath -ItemType Directory | Out-Null
  }
}

function WriteCheckstyleProcessedReport($CheckstyleXmlReport, $ReportOutputFolder) {
  # Load the Checkstyle XML report.
  $xml = New-Object -TypeName System.Xml.XmlDocument
  $xml.Load($CheckstyleXmlReport)

  $reportBuilder = New-Object -TypeName System.Text.StringBuilder

  # List all "file" nodes contained in the report.
  foreach ($fileNode in $xml.GetElementsByTagName("file")) {
    if ($fileNode.HasChildNodes) {
      # Name of the file is an XML attribute "name".
      $fileName = $fileNode.Attributes["name"].Value
      foreach ($errorNode in $fileNode.ChildNodes) {
        # Information about the Checkstyle error is maintained as XML attributes.
        $lineNumber = $errorNode.Attributes["line"].Value
        $columnNumber = $errorNode.Attributes["column"].Value
        $lintingMessage = $errorNode.Attributes["message"].Value
        $lintingType = $errorNode.Attributes["source"].Value
        $reportBuilder.AppendLine("""$fileName"",$lineNumber,$columnNumber,""$lintingMessage"",""$lintingType""")
      }
    }
  }

  if ($reportBuilder.Length -gt 0) {
    $reportBuilder.Insert(0, "File Name,Line Number,Column Number,Message,Type`n")
    NewFolderIfNotExists($ReportOutputFolder)
    Write-Host $reportBuilder
    New-Item -Path (Join-Path $ReportOutputFolder "checkstyle-report.csv") -ItemType File -Value $reportBuilder | Out-Null
  }
}

function WriteSpotbugsProcessedReport($SpotbugsXmlReport, $ReportOutputFolder) {
  # Load the Spotbugs XML report.
  $xml = New-Object -TypeName System.Xml.XmlDocument
  $xml.Load($SpotbugsXmlReport)

  $reportBuilder = New-Object -TypeName System.Text.StringBuilder

  # List all "file" nodes contained in the report.
  foreach ($fileNode in $xml.GetElementsByTagName("file")) {
    if ($fileNode.HasChildNodes) {
      # Name of the class is an XML attribute "classname".
      $className = $fileNode.Attributes["classname"].Value
      foreach ($errorNode in $fileNode.ChildNodes) {
        # Information about the Checkstyle error is maintained as XML attributes.
        $lineNumber = $errorNode.Attributes["lineNumber"].Value
        $lintingMessage = $errorNode.Attributes["message"].Value
        $lintingType = $errorNode.Attributes["type"].Value
        $reportBuilder.AppendLine("""$className"",$lineNumber,""$lintingMessage"",""$lintingType""")
      }
    }
  }

  if ($reportBuilder.Length -gt 0) {
    $reportBuilder.Insert(0, "Class Name,Line Number,Message,Type`n")
    NewFolderIfNotExists($ReportOutputFolder)
    Write-Host $reportBuilder
    New-Item -Path (Join-Path $ReportOutputFolder "spotbugs-report.csv") -ItemType File -Value $reportBuilder | Out-Null
  }
}

# Change the working directory to the root of the repository
Set-Location -ErrorAction Stop -LiteralPath (Join-Path $PSScriptRoot "../../../")

# Always create the output directory
$OutputDirectory = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath("$StagingDirectory/linting-report")
New-Item -Path $OutputDirectory -ItemType Directory | Out-Null

$path = "sdk/*/"
if ($SdkDirectory) {
  $path = "sdk/$SdkDirectory/*/"
}

foreach ($targetFolder in (Get-ChildItem -Path $path -Filter "target" -Directory -Recurse)) {
  # An assumption is being made here that the file path is /sdk/<SDK group>/<SDK name>
  $sdkGroup = $targetFolder.Parent.Parent.Name
  $sdk = $targetFolder.Parent.Name

  $reportOutputFolder = Join-Path $OutputDirectory "$sdkGroup-$sdk"

  $checkstyleXmlReport = Join-Path $targetFolder.FullName "checkstyle-result.xml"
  if (Test-Path -Path $checkstyleXmlReport) {
    WriteCheckstyleProcessedReport $checkstyleXmlReport $reportOutputFolder
  }

  $spotbugsXmlReport = Join-Path $targetFolder.FullName "spotbugs.xml"
  if (Test-Path -Path $spotbugsXmlReport) {
    WriteSpotbugsProcessedReport $spotbugsXmlReport $reportOutputFolder
  }

  $revapiReport = Join-Path $targetFolder.FullName "revapi.json"
  if (Test-Path -Path $revapiReport) {
    $json = [System.Text.Json.JsonDocument]::Parse((Get-Content -Raw -Path $revapiReport))

    # Only include the RevApi report if it contains an errors.
    if ($json.RootElement.GetArrayLength() -gt 0) {
      NewFolderIfNotExists($reportOutputFolder)
      Get-Content $revapiReport | Write-Host
      Copy-Item -Path $revapiReport -Destination (Join-Path $reportOutputFolder "revapi-report.json")
    }
  }
}

if ((Get-ChildItem -Path $OutputDirectory -Directory).Count -eq 0) {
  exit 0
}

if (-not (Test-Path "$StagingDirectory/troubleshooting")) {
  New-Item -ItemType Directory -Path "$StagingDirectory/troubleshooting" | Out-Null
}

Compress-Archive -Path $OutputDirectory -DestinationPath "$StagingDirectory/troubleshooting/linting-report.zip"
Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
