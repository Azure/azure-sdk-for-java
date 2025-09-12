// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import com.azure.core.util.CoreUtils;

/**
 * Utility class for powershell auth related ops .
 */
public class PowerShellUtil {
    private static final String DOTNET_DATE_PREFIX = "/Date(";
    private static final String DOTNET_DATE_SUFFIX = ")/";
    
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
            + "$customToken = New-Object -TypeName PSObject" + sep
            + "$customToken | Add-Member -MemberType NoteProperty -Name Token -Value $tokenValue" + sep
            + "$customToken | Add-Member -MemberType NoteProperty -Name ExpiresOn -Value $token.ExpiresOn" + sep
            + "$customToken | ConvertTo-Json -Compress -Depth 10";
    }

    /**
     * Parse ExpiresOn returned from PowerShell. Supports ISO timestamps and the .NET "/Date(ms)/" form.
     *
     * @param time the string value returned by PowerShell
     * @return parsed OffsetDateTime in UTC or null if unable to parse
     */
    public static OffsetDateTime parseExpiresOn(String time) {
        if (CoreUtils.isNullOrEmpty(time)) {
            return null;
        }

        // Try ISO first
        try {
            return OffsetDateTime.parse(time).withOffsetSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignore) {
            // fall through to .NET style parsing
        }

        if (time.length() > DOTNET_DATE_PREFIX.length() + DOTNET_DATE_SUFFIX.length()
            && time.startsWith(DOTNET_DATE_PREFIX) && time.endsWith(DOTNET_DATE_SUFFIX)) {
            String digits = time.substring(DOTNET_DATE_PREFIX.length(), time.length() - DOTNET_DATE_SUFFIX.length());
            for (int i = 0; i < digits.length(); i++) {
                if (!Character.isDigit(digits.charAt(i))) {
                    return null;
                }
            }
            try {
                long epochMs = Long.parseLong(digits);
                return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneOffset.UTC);
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }
}
