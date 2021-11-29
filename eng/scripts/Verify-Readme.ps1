# Wrapper Script for Readme Verification
[CmdletBinding()]
param (
  [Parameter(Mandatory = $true)]
  [string]$DocWardenVersion,

  [Parameter(Mandatory = $true)]
  [string]$ScanPath,

  [Parameter(Mandatory = $true)]
  [string]$SettingsPath
)

pip install setuptools wheel
pip install doc-warden==$DocWardenVersion
ward scan -d $ScanPath -c $SettingsPath