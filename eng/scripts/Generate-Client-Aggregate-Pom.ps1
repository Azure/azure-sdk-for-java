# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script requires Powershell 6 which defaults LocalMachine to Restricted on Windows client machines.
# From a Powershell 6 prompt run 'Get-ExecutionPolicy -List' and if the LocalMachine is Restricted or Undefined then
# run the following command from an admin Powershell 6 prompt 'Set-ExecutionPolicy -ExecutionPolicy RemoteSigned'. This
# will enable running scripts locally in Powershell 6.

# Use case: For the From Source runs we want to build and install only the client libraries but because we're
# a mono-repo the root aggregate pom has multiple tracks worth of libraries. This script is used to generate
# a file called ClientAggregatePom.xml in the root of the repostory to be used by the From Source builds.

# This script can be run locally from the root of the repo. .\eng\scripts\Generate-Client-Aggregate-Pom.ps1
param(
  [Parameter(Mandatory=$false,HelpMessage="Indicates if the POM generation is for the aggregate reports.")]
  [System.Boolean]$AggregateReport
)

function New-Element ([System.Xml.XmlDocument]$xmlDocument, [String]$elementName, [String]$elementValue) {
  $element = $xmlDocument.CreateElement($elementName, $xmlDocument.DocumentElement.NamespaceURI)
  $element.InnerXml = $elementValue

  return $element
}
function Add-Module ([String]$modulePath, [System.Xml.XmlDocument]$xmlDocument, [System.Xml.XmlElement]$modulesNode, [System.Xml.XmlElement]$dpendenciesNode) {
  $modulesNode.AppendChild((New-Element -xmlDocument $xmlDocument -elementName "module" -elementValue $modulePath))

  if ($null -ne $dpendenciesNode) {
      $dependency = $xmlDocument.CreateElement("dependency", $xmlDocument.DocumentElement.NamespaceURI)
      $dependency.AppendChild((New-Element -xmlDocument $xmlDocument -elementName "groupId" -elementValue $xmlPomFile.project.groupId))
      $dependency.AppendChild((New-Element -xmlDocument $xmlDocument -elementName "artifactId" -elementValue $xmlPomFile.project.artifactId))
      $dependency.AppendChild((New-Element -xmlDocument $xmlDocument -elementName "version" -elementValue $xmlPomFile.project.version))
      $dpendenciesNode.AppendChild($dependency)
  }
}

# azure-client-sdk-parent is the client track 2 parent, spring-boot-starter-parent is necessary because the
# samples use it and they're part of the spring/ci.yml
$ValidTrack2Parents
if ($AggregateReport) {
  $ValidTrack2Parents = ("azure-client-sdk-parent")
} else {
  $ValidTrack2Parents = ("azure-client-sdk-parent", "spring-boot-starter-parent")
}

$BasePomFile = @"
<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
<modelVersion>4.0.0</modelVersion>
<groupId>com.azure</groupId>
<artifactId>azure-sdk-all</artifactId>
<packaging>pom</packaging>
<version>1.0.0</version>
<modules>
</modules>
</project>
"@

$ParentConfiguration = @"
<parent xmlns="http://maven.apache.org/POM/4.0.0">
  <groupId>com.azure</groupId>
  <artifactId>azure-client-sdk-parent</artifactId>
  <version>1.7.0</version>
  <relativePath>sdk/parents/azure-client-sdk-parent</relativePath>
</parent>
"@

$JacocoAggregateConfiguration = @"
<build xmlns="http://maven.apache.org/POM/4.0.0">
  <plugins>
    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.8.5</version>
      <executions>
        <execution>
          <id>report-aggregate</id>
          <phase>verify</phase>
          <goals>
            <goal>report-aggregate</goal>
          </goals>
          <configuration>
            <outputDirectory>${project.reporting.outputDirectory}/test-coverage</outputDirectory>
            <excludes>
              <exclude>**/com/azure/cosmos/implementation/apachecommons/**/*</exclude>
              <exclude>**/com/azure/cosmos/implementation/guava25/**/*</exclude>
              <exclude>**/com/azure/cosmos/implementation/guava27/**/*</exclude>
            </excludes>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
"@

$RootPath = Resolve-Path ($PSScriptRoot + "/../../")
$ClientAggregatePom = Join-Path $RootPath "ClientAggregatePom.xml"
Write-Host "Creating client aggregate pom file $($ClientAggregatePom)"

# Set The Formatting
$xmlsettings = New-Object System.Xml.XmlWriterSettings
$xmlsettings.Indent = $true
$xmlsettings.IndentChars = "  "
$xmlsettings.NamespaceHandling = [System.Xml.NamespaceHandling]::OmitDuplicates
$xmlsettings.OmitXmlDeclaration = $true

# Set the File Name Create The Document
[System.Xml.XmlWriter]$xmlWriter = $null

try {
  [xml]$xmlDocument = New-Object XML
  $xmlDocument.LoadXml($BasePomFile)

  $projectNode = $xmlDocument.project
  $modulesNode = $projectNode["modules"]

  [System.Xml.XmlElement]$dependenciesNode = $null
  if ($AggregateReport) {
    [xml]$parent = New-Object xml
    $parent.LoadXml($ParentConfiguration)

    [System.Xml.XmlElement]$parentNode = $xmlDocument.ImportNode($parent.parent, $true)
    $projectNode.PrependChild($parentNode)
    $dependenciesNode = $xmlDocument.CreateElement("dependencies", $xmlDocument.DocumentElement.NamespaceURI)
    $projectNode.AppendChild($dependenciesNode)
  }

  $script:FoundError = $false
  $StartTime = $(get-date)

  # Loop through every pom in the system and check if it is one fo the valid parents add the path
  # a module entry for it to the client aggregate pom file.
  Get-ChildItem -Path $RootPath -Filter pom*.xml -Recurse -File | ForEach-Object {
    $xmlPomFilePath = $_.FullName
    [xml]$xmlPomFile = Get-Content $xmlPomFilePath

    # check the parents but exclude items under the eng directory otherwise we're going
    # to be building jacoco and spotbus
    if (($ValidTrack2Parents -contains $xmlPomFile.project.parent.artifactId) -and
        ($xmlPomFilePath.Split([IO.Path]::DirectorySeparatorChar) -notcontains "eng"))
    {
      $xmlPomDirectoryName = $_.DirectoryName
      Add-Module -modulePath $xmlPomDirectoryName.Replace($RootPath,'').Replace('\', '/') -xmlDocument $xmlDocument -modulesNode $modulesNode -dpendenciesNode $dependenciesNode
    }
  }

  if ($AggregateReport) {
    [xml]$jacocoAggregate = New-Object xml
    $jacocoAggregate.LoadXml($JacocoAggregateConfiguration)

    $projectNode.AppendChild($xmlDocument.ImportNode($jacocoAggregate.build, $true))
  }

  $xmlWriter = [System.Xml.XmlWriter]::Create($ClientAggregatePom, $xmlsettings)
  $xmlDocument.Save($xmlWriter)
  $xmlWriter.Flush()
  $xmlWriter.Close()

  Write-Host "Effective Client Pom File"
  Write-Host (Get-Content -Path $ClientAggregatePom -Raw)

  $ElapsedTime = $(get-date) - $StartTime
  $TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
  Write-Host "Total run time=$($TotalRunTime)"
} finally {
  if ($null -ne $xmlWriter -and $xmlWriter.WriteState -ne [System.Xml.WriteState]::Closed) {
    $xmlWriter.Close()
  }
}
