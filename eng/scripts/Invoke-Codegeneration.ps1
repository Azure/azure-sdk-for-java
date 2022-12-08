<#
.SYNOPSIS
Invokes Autoest in the specified directory.

.DESCRIPTION
Invokes Autorest in the specified directory.

This script is all encompassing for running Autorest where it'll first ensure Autorest is installed then run Autorest.

.PARAMETER Directory
The directory where Autorest will be invoked.

.PARAMETER AutorestOptions
Additional options to pass to the Autorest command. By default, this will just run 'autorest'.
#>

param(
  [Parameter(Mandatory = $true)]
  [string]$Directory,

  [Parameter(Mandatory = $false)]
  [string]$AutorestOptions
)

# Ensure Autorest is installed.
npm install -s -g autorest

$location = (Get-Location).Path
Set-Location $Directory

Invoke-Expression "autorest $AutorestOptions"

Set-Location $location
