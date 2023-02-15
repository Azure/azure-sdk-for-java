# Use case:
#
# The purpose of this script is to enable easier switching of the JDK being used as the JAVA_HOME in a CLI process.
#
# There are two ways of running this script:
#
# - Provide the path of the JDK to use.
# - Allow the script to find Azul and Eclipse JDKs using the registry keys on the machine.
#
# This script won't modify the User or Machine environment variables, only the Process environment variables.
# Additionally, because this uses registry keys and modifies Process environment variables this script is only usable
# on Windows. Linux and macOs users should use the tooling available on those OSes to switch the JAVA_HOME for the
# running process.
param(
    [Parameter(Mandatory=$false)][string]$JdkPath
)

if (!$IsWindows) {
    Write-Error "This script is only usable on Windows."
    exit 1
}

if ("" -eq $JdkPath) {
    # Use the HKEY_LOCAL_MACHINE registry keys to find JDK installs from Azul and Eclipse.
    $registryBase = "HKLM:\SOFTWARE"
    $azulJdks = "$registryBase\Azul Systems\Zulu"
    $eclipseJdks = "$registryBase\Eclipse Adoptium\JDK"

    # Collect all available JDK installations in the following format
    #
    # Option # | <Distributor> | <Major Version> | <Full Version> | <Install Location>
    #
    # For example
    #
    # 1 | Zulu | 17 | 17.28.13 | C:\Program Files\Zulu\zulu-17
    #
    # Then let the caller of the script select which version they want to make the new JAVA_HOME,
    # if any version. Choosing not to select a new version will leave the current JAVA_HOME
    # unmodified.
    [System.Data.DataTable]$jdkOptions = New-Object System.Data.Datatable
    [void]$jdkOptions.Columns.Add("Choice")
    [void]$jdkOptions.Columns.Add("Distributor")
    [void]$jdkOptions.Columns.Add("Major Version")
    [void]$jdkOptions.Columns.Add("Full Version")
    [void]$jdkOptions.Columns.Add("Install Location")

    Write-Host "Searching for JDK installs from Azul and Eclipse"

    $choiceNumber = 1

    # Azul uses a registry key pattern of
    #
    # <Install Name> (ex zulu-17)
    # - \ <CurrentVersion> (ex 17.28.13)
    #     <MajorVersion> (ex 17)
    #     <InstallationPath> (ex C:\Progam Files\Zulu\zulu-17\)
    #
    # There are additional properties that aren't needed and aren't included above.
    #
    # Select the installs in the following format
    #
    # Zulu | <MajorVersion> | <CurrentVersion> | <InstallationPath>
    foreach ($jdk in Get-ChildItem -Path $azulJdks) {
        $jdkHKeyPath = $jdk.Name
        $values = Get-ItemProperty -Path "Registry::$jdkHKeyPath"

        [void]$jdkOptions.Rows.Add($choiceNumber, "Zulu", $values.MajorVersion, $values.CurrentVersion, $values.InstallationPath)
        $choiceNumber++
    }

    # Eclipse uses a registry key pattern of
    #
    # <Current Version> (ex 17.0.4.8)
    # - \ <Implementation> (ex hotspot)
    #     - \ <MSI>
    #         - \ <Path> (ex C:\Program Files\Eclipse Adoptium\jdk-17.0.4.8-hotspot\)
    #
    # There are additional properties that aren't needed and aren't included above.
    #
    # Select the installs in the following format
    #
    # Eclipse | <First number segment in Current Version> | <Current Version> | <Path>
    foreach ($jdk in Get-ChildItem -Path $eclipseJdks) {
        $jdkHKeyPath = $jdk.Name
        $jdkFullVersion = $jdk.PSChildName
        $jdkChoicePath = (Get-ItemProperty -Path "Registry::$jdkHKeyPath\hotspot\MSI").Path

        [void]$jdkOptions.Rows.Add($choiceNumber, "Eclipse", $jdkFullVersion.split(".")[0], $jdkFullVersion, $jdkChoicePath)
        $choiceNumber++
    }

    $choiceNumber--
    $selection = $null

    while ($null -eq $selection) {
        $jdkOptions | Format-Table -AutoSize
        $selection = Read-Host "Select the JDK to set (or select no JDK by entering no option)"

        if ($null -ne $selection) {
            if ($selection.Trim() -eq "") {
                break
            }

            $selectionNumber = $selection -as [int]

            if ($null -eq $selectionNumber -or $selectionNumber -lt 1 -or $selectionNumber -gt $choiceNumber) {
                Write-Host "Invalid selection '$selectionNumber', choose a choice from 1 to $choiceNumber"
                $selection = $null
            } else {
                $JdkPath = $jdkOptions.Rows[$selection - 1]["Install Location"]
                if ($JdkPath.EndsWith("\")) {
                    $JdkPath = $JdkPath.Substring(0, $JdkPath.Length - 1)
                }
            }
        }
    }
}

if ("" -ne $JdkPath) {
    Write-Host "Beginning replacement of process environment variables"
    Write-Host "Replacing current JAVA_HOME '$env:JAVA_HOME' with new JAVA_HOME '$JdkPath'"
    $env:JAVA_HOME = $JdkPath

    Write-Host "Setting '$JdkPath\bin' to the first value in Path"
    $env:Path = "$JdkPath\bin;$env:Path"
}
