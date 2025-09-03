// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

/**
 * Utility class for powershell auth related ops .
 */
public class PowerShellUtil {
    public static String getPwshCommand(String tenantId, String scope, String sep) {
        return "$ErrorActionPreference = 'Stop'" + sep
            + "$ProgressPreference = 'SilentlyContinue'" + sep
            + "$VerbosePreference = 'SilentlyContinue'" + sep
            + "$WarningPreference = 'SilentlyContinue'" + sep
            + "$InformationPreference = 'SilentlyContinue'" + sep
            + "[version]$minimumVersion = '2.2.0'" + sep
            + "$m = Import-Module Az.Accounts -MinimumVersion $minimumVersion -PassThru -ErrorAction SilentlyContinue" + sep
            + "if (! $m) {" + sep
            + "    Write-Output 'VersionTooOld'" + sep
            + "    exit" + sep
            + "}" + sep
            + "$params = @{ 'ResourceUrl' = '" + scope + "'; 'WarningAction' = 'Ignore' }" + sep
            + "$tenantId = '" + tenantId + "'" + sep
            + "if ($tenantId.Length -gt 0) {" + sep
            + "    $params['TenantId'] = $tenantId" + sep
            + "}" + sep
            + "if ($m.Version -ge [version]'2.17.0' -and $m.Version -lt [version]'5.0.0') {" + sep
            + "    $params['AsSecureString'] = $true" + sep
            + "}" + sep
            + "$token = Get-AzAccessToken @params" + sep
            + "$tokenValue = $token.Token" + sep
            + "if ($tokenValue -is [System.Security.SecureString]) {" + sep
            + "    if ($PSVersionTable.PSVersion.Major -lt 7) {" + sep
            + "        $ssPtr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($tokenValue)" + sep
            + "        try {" + sep
            + "            $tokenValue = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($ssPtr)" + sep
            + "        }" + sep
            + "        finally {" + sep
            + "            [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ssPtr)" + sep
            + "        }" + sep
            + "    } else {" + sep
            + "        $tokenValue = $tokenValue | ConvertFrom-SecureString -AsPlainText" + sep
            + "    }" + sep
            + "}" + sep
            + "$customToken = [PSCustomObject]@{" + sep
            + "    Token = $tokenValue" + sep
            + "    ExpiresOn = $token.ExpiresOn" + sep
            + "}" + sep
            + "$customToken | ConvertTo-Json -Compress";
    }
}
