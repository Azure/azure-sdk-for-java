param(
  [Parameter(Mandatory=$false)]
  [string]$SourcesDirectory = $env:BUILD_SOURCESDIRECTORY
)

dotnet tool install azure.sdk.tools.httpfaultinjector --global --prerelease --add-source https://pkgs.dev.azure.com/azure-sdk/public/_packaging/azure-sdk-for-net/nuget/v3/index.json
dotnet tool update azure.sdk.tools.httpfaultinjector --global --prerelease --add-source https://pkgs.dev.azure.com/azure-sdk/public/_packaging/azure-sdk-for-net/nuget/v3/index.json
http-fault-injector --version

Write-Host "##vso[task.setvariable variable=ASPNETCORE_Kestrel__Certificates__Default__Path]$SourcesDirectory/eng/common/testproxy/dotnet-devcert.pfx"
Write-Host "##vso[task.setvariable variable=ASPNETCORE_Kestrel__Certificates__Default__Password]password"
Write-Host "##vso[task.setvariable variable=PROXY_MANUAL_START]true"

# Trust http-fault-injector self-siigned certificate
if (Test-Path $env:JAVA_HOME/jre/lib/security) {
    Set-Location $env:JAVA_HOME/jre/lib/security
} elseif (Test-Path $env:JAVA_HOME/lib/security) {
    Set-Location $env:JAVA_HOME/lib/security
} else {
    Write-Error "JDK directory structure is unknown and unsupported. JAVA_HOME is '$env:JAVA_HOME'"
    exit 1
}

dotnet dev-certs https --export-path http-fault-injector.pfx

if ($IsWindows) {
    keytool -keystore cacerts -importcert -noprompt -trustcacerts -alias HttpFaultInject -file http-fault-injector.pfx -storepass changeit
} else {
    sudo keytool -keystore cacerts -importcert -noprompt -trustcacerts -alias HttpFaultInject -file http-fault-injector.pfx -storepass changeit
}
