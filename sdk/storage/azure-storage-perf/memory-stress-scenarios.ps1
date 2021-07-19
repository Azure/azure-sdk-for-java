$JavaPath = Join-Path -Path $Env:JAVA_HOME -ChildPath "bin" | Join-Path -ChildPath "java"
$TargetDirPath = Join-Path -Path $PSScriptRoot -ChildPath "target"
$PerfJarPath = Get-ChildItem -Path (Join-Path -Path $TargetDirPath -ChildPath "*") -Include azure-storage-perf-*-jar-with-dependencies.jar -File

Invoke-Expression "& '$JavaPath' -version"

function Run-Scenario {
    param (
        [Parameter(Mandatory=$true, Position=0)]
        [string] $HeapSize,
        [Parameter(Mandatory=$true, Position=1)]
        [string] $Scenario
    )
    Write-Host "Executing '$Scenario' with '$HeapSize' heap"
    Invoke-Expression "& '$JavaPath' -Xms$HeapSize -Xmx$HeapSize -jar '$PerfJarPath' $Scenario"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Scenario failed, exiting"
        exit 1
    }
}

$env:STORAGE_CONNECTION_STRING=$env:PRIMARY_STORAGE_CONNECTION_STRING
Run-Scenario "600m" "uploadoutputstream --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "600m" "uploadblob --warmup 0 --duration 1 --size 1048576000"
Run-Scenario "400m" "uploadblob --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "400m" "uploadblobnolength --warmup 0 --duration 1 --size 1048576000 --sync"

$env:STORAGE_CONNECTION_STRING=$env:STORAGE_DATA_LAKE_CONNECTION_STRING
Run-Scenario "200m" "uploadfiledatalake --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "300m" "uploadfiledatalake --warmup 0 --duration 1 --size 1048576000"
