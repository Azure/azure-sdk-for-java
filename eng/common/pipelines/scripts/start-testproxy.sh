#!/bin/bash

if [[ "$(uname)" == "Darwin" ]]; then
    export DOTNET_ROOT="$HOME/.dotnet"
fi
nohup $(Build.BinariesDirectory)/test-proxy/test-proxy start `
    -u --storage-location ${{ parameters.rootFolder }} -- `
    --urls "${{ parameters.proxyUrl }}" `
    1>${{ parameters.rootFolder }}/test-proxy.log `
    2>${{ parameters.rootFolder }}/test-proxy-error.log &

echo $! > $(Build.SourcesDirectory)/test-proxy.pid

echo "Setting PROXY_PID to $(cat $(Build.SourcesDirectory)/test-proxy.pid)"
echo "##vso[task.setvariable variable=PROXY_PID]$(cat $(Build.SourcesDirectory)/test-proxy.pid)"