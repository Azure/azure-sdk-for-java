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

Run-Scenario "700m" "uploadoutputstream --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "700m" "uploadblob --warmup 0 --duration 1 --size 1048576000"
