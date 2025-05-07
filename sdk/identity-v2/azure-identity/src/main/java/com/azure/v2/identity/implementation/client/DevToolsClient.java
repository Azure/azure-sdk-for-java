// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.models.DevToolsClientOptions;
import com.azure.v2.identity.implementation.models.AzureCliToken;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.identity.implementation.util.ScopeUtil;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.sun.jna.Platform;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.instrumentation.logging.LogLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.azure.v2.identity.implementation.util.IdentityUtil.isWindowsPlatform;

public class DevToolsClient extends ClientBase {

    static final ClientLogger LOGGER = new ClientLogger(DevToolsClient.class);

    static final String WINDOWS_STARTER = "cmd.exe";
    static final String LINUX_MAC_STARTER = "/bin/sh";
    static final String WINDOWS_SWITCHER = "/c";
    static final String LINUX_MAC_SWITCHER = "-c";
    static final Pattern WINDOWS_PROCESS_ERROR_MESSAGE = Pattern.compile("'azd?' is not recognized");
    static final Pattern SH_PROCESS_ERROR_MESSAGE = Pattern.compile("azd?:.*not found");
    static final String DEFAULT_MAC_LINUX_PATH = "/bin/";
    static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\": \"(.*?)(\"|$)");

    final DevToolsClientOptions clientOptions;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param clientOptions the options configuring the client.
     */
    public DevToolsClient(DevToolsClientOptions clientOptions) {
        super(clientOptions);
        this.clientOptions = clientOptions;
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure PowerShell.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithAzurePowerShell(TokenRequestContext request) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        List<CredentialUnavailableException> exceptions = new ArrayList<>(2);

        PowershellManager defaultPowerShellManager = new PowershellManager(false);

        PowershellManager legacyPowerShellManager = Platform.isWindows() ? new PowershellManager(true) : null;

        List<PowershellManager> powershellManagers = new ArrayList<>(2);
        powershellManagers.add(defaultPowerShellManager);
        if (legacyPowerShellManager != null) {
            powershellManagers.add(legacyPowerShellManager);
        }

        for (PowershellManager powershellManager : powershellManagers) {
            try {
                return getAccessTokenFromPowerShell(request, powershellManager);
            } catch (Exception ex) {
                if (ex instanceof CredentialUnavailableException) {
                    exceptions.add((CredentialUnavailableException) ex);
                } else {
                    throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(
                        "Azure Powershell authentication failed. Error Details: " + ex.getMessage()
                            + ". To mitigate this issue, please refer to the troubleshooting guidelines here at "
                            + "https://aka.ms/azsdk/java/identity/powershellcredential/troubleshoot",
                        ex));
                }
            }
        }

        CredentialUnavailableException last = exceptions.get(exceptions.size() - 1);
        for (int z = exceptions.size() - 2; z >= 0; z--) {
            CredentialUnavailableException current = exceptions.get(z);
            last = new CredentialUnavailableException("Azure PowerShell authentication failed using default"
                + "powershell(pwsh) with following error: " + current.getMessage() + "\r\n"
                + "Azure PowerShell authentication failed using powershell-core(powershell)" + " with following error: "
                + last.getMessage(), last.getCause());
        }
        LoggingUtil.logCredentialUnavailableException(LOGGER, (last));
        return null;
    }

    private AccessToken getAccessTokenFromPowerShell(TokenRequestContext request, PowershellManager powershellManager) {
        String scope = ScopeUtil.scopesToResource(request.getScopes());
        try {
            ScopeUtil.validateScope(scope);
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logThrowableAsError(ex);
        }

        String sep = System.lineSeparator();

        String command = "$ErrorActionPreference = 'Stop'" + sep + "[version]$minimumVersion = '2.2.0'" + sep + "" + sep
            + "$m = Import-Module Az.Accounts -MinimumVersion $minimumVersion -PassThru -ErrorAction SilentlyContinue"
            + sep + "" + sep + "if (! $m) {" + sep + "    Write-Output 'VersionTooOld'" + sep + "    exit" + sep + "}"
            + sep + "" + sep + "$useSecureString = $m.Version -ge [version]'2.17.0'" + sep + "" + sep + "$params = @{"
            + sep + "    'WarningAction'='Ignore'" + sep + "    'ResourceUrl'='" + scope + "'" + sep + "}" + sep + ""
            + sep + "if ($useSecureString) {" + sep + "    $params['AsSecureString'] = $true" + sep + "}" + sep + ""
            + sep + "$token = Get-AzAccessToken @params" + sep + "$customToken = New-Object -TypeName psobject" + sep
            + "" + sep
            + "$customToken | Add-Member -MemberType NoteProperty -Name Token -Value ($useSecureString -eq $true ? (ConvertFrom-SecureString -AsPlainText $token.Token) : $token.Token)"
            + sep + "$customToken | Add-Member -MemberType NoteProperty -Name ExpiresOn -Value $token.ExpiresOn" + sep
            + "" + sep + "return $customToken | ConvertTo-Json";

        String output = powershellManager.runCommand(command);
        if (output.contains("VersionTooOld")) {
            LoggingUtil.logCredentialUnavailableException(LOGGER,
                new CredentialUnavailableException("Az.Account module with version >= 2.2.0 is not installed. "
                    + "It needs to be installed to use Azure PowerShell " + "Credential."));
        }

        if (output.contains("Run Connect-AzAccount to login")) {
            LoggingUtil.logCredentialUnavailableException(LOGGER,
                new CredentialUnavailableException("Run Connect-AzAccount to login to Azure account in PowerShell."));
        }

        try (JsonReader reader = JsonReader.fromString(output)) {
            reader.nextToken();
            Map<String, String> objectMap = reader.readMap(JsonReader::getString);
            String accessToken = objectMap.get("Token");
            String time = objectMap.get("ExpiresOn");
            OffsetDateTime expiresOn = OffsetDateTime.parse(time).withOffsetSameInstant(ZoneOffset.UTC);
            return new AccessToken(accessToken, expiresOn);
        } catch (IOException e) {
            LoggingUtil.logCredentialUnavailableException(LOGGER, new CredentialUnavailableException(
                "Encountered error when deserializing response from Azure Power Shell.", e));
        }
        return null;
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure Developer CLI.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithAzureDeveloperCli(TokenRequestContext request) {

        StringBuilder azdCommand = new StringBuilder("azd auth token --output json --scope ");

        List<String> scopes = request.getScopes();

        // It's really unlikely that the request comes with no scope, but we want to
        // validate it as we are adding `--scope` arg to the azd command.
        if (scopes.size() == 0) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Missing scope in request"));
        }

        scopes.forEach(scope -> {
            try {
                ScopeUtil.validateScope(scope);
            } catch (IllegalArgumentException ex) {
                throw LOGGER.logThrowableAsError(ex);
            }
        });

        // At least one scope is appended to the azd command.
        // If there are more than one scope, we add `--scope` before each.
        azdCommand.append(String.join(" --scope ", scopes));

        String tenant = IdentityUtil.resolveTenantId(tenantId, request, clientOptions);
        ValidationUtil.validateTenantIdCharacterRange(tenant, LOGGER);

        if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
            azdCommand.append(" --tenant-id ").append(tenant);
        }

        try {
            return getTokenFromAzureDeveloperCLIAuthentication(azdCommand);
        } catch (RuntimeException e) {
            throw (e instanceof CredentialUnavailableException
                ? LoggingUtil.logCredentialUnavailableException(LOGGER, (CredentialUnavailableException) e)
                : LOGGER.logThrowableAsError(e));
        }
    }

    AccessToken getTokenFromAzureDeveloperCLIAuthentication(StringBuilder azdCommand) {
        AccessToken token;
        try {
            String starter;
            String switcher;
            if (isWindowsPlatform()) {
                starter = WINDOWS_STARTER;
                switcher = WINDOWS_SWITCHER;
            } else {
                starter = LINUX_MAC_STARTER;
                switcher = LINUX_MAC_SWITCHER;
            }

            ProcessBuilder builder = new ProcessBuilder(starter, switcher, azdCommand.toString());
            // Redirects stdin to dev null, helps to avoid messages sent in by the cmd process to upgrade etc.
            builder.redirectInput(ProcessBuilder.Redirect.from(IdentityUtil.NULL_FILE));

            String workingDirectory = getSafeWorkingDirectory();
            if (workingDirectory != null) {
                builder.directory(new File(workingDirectory));
            } else {
                throw LOGGER.logThrowableAsError(new IllegalStateException(
                    "A Safe Working directory could not be" + " found to execute Azure Developer CLI command from."));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader
                = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8.name()))) {
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (WINDOWS_PROCESS_ERROR_MESSAGE.matcher(line).find()
                        || SH_PROCESS_ERROR_MESSAGE.matcher(line).find()) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, new CredentialUnavailableException(
                            "AzureDeveloperCliCredential authentication unavailable. Azure Developer CLI not installed."
                                + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                                + "https://aka.ms/azsdk/java/identity/azdevclicredential/troubleshoot"));
                    }
                    output.append(line);
                }
            }
            String processOutput = output.toString();

            // wait until the process completes or the timeout (10 sec) is reached.
            process.waitFor(clientOptions.getProcessTimeout().getSeconds(), TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                if (processOutput.length() > 0) {
                    String redactedOutput = redactInfo(processOutput);
                    if (redactedOutput.contains("azd auth login") || redactedOutput.contains("not logged in")) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER,
                            new CredentialUnavailableException("AzureDeveloperCliCredential authentication unavailable."
                                + " Please run 'azd auth login' to set up account."));
                    }
                    throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(redactedOutput, null));
                } else {
                    throw LOGGER.logThrowableAsError(
                        new CredentialAuthenticationException("Failed to invoke Azure Developer CLI ", null));
                }
            }

            LOGGER.atLevel(LogLevel.VERBOSE)
                .log(
                    "Azure Developer CLI Authentication => A token response was received from Azure Developer CLI, deserializing the"
                        + " response into an Access Token.");
            try (JsonReader reader = JsonReader.fromString(processOutput)) {
                reader.nextToken();
                Map<String, String> objectMap = reader.readMap(JsonReader::getString);
                String accessToken = objectMap.get("token");
                String time = objectMap.get("expiresOn");
                // az expiresOn format = "2022-11-30 02:38:42.000000" vs
                // azd expiresOn format = "2022-11-30T02:05:08Z"
                String standardTime = time.substring(0, time.indexOf("Z"));
                OffsetDateTime expiresOn = LocalDateTime.parse(standardTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.of("Z"))
                    .toOffsetDateTime()
                    .withOffsetSameInstant(ZoneOffset.UTC);
                token = new AccessToken(accessToken, expiresOn);
            }
        } catch (IOException | InterruptedException e) {
            IllegalStateException ex = new IllegalStateException(redactInfo(e.getMessage()));
            ex.setStackTrace(e.getStackTrace());
            throw LOGGER.logThrowableAsError(ex);
        }

        return token;
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure CLI.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithAzureCli(TokenRequestContext request) {
        StringBuilder azCommand = new StringBuilder("az account get-access-token --output json --resource ");

        String scopes = ScopeUtil.scopesToResource(request.getScopes());

        try {
            ScopeUtil.validateScope(scopes);
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logThrowableAsError(ex);
        }

        azCommand.append(scopes);

        String tenant = IdentityUtil.resolveTenantId(tenantId, request, clientOptions);
        ValidationUtil.validateTenantIdCharacterRange(tenant, LOGGER);

        if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
            azCommand.append(" --tenant ").append(tenant);
        }

        String subscription = clientOptions.getSubscription();
        if (!CoreUtils.isNullOrEmpty(subscription)) {
            azCommand.append(" --subscription ").append(subscription);
        }

        try {
            return getTokenFromAzureCLIAuthentication(azCommand);
        } catch (RuntimeException e) {
            throw (e instanceof CredentialUnavailableException
                ? LoggingUtil.logCredentialUnavailableException(LOGGER, (CredentialUnavailableException) e)
                : LOGGER.logThrowableAsError(e));
        }

    }

    AccessToken getTokenFromAzureCLIAuthentication(StringBuilder azCommand) {
        AccessToken token;
        try {
            String starter;
            String switcher;
            if (isWindowsPlatform()) {
                starter = WINDOWS_STARTER;
                switcher = WINDOWS_SWITCHER;
            } else {
                starter = LINUX_MAC_STARTER;
                switcher = LINUX_MAC_SWITCHER;
            }

            ProcessBuilder builder = new ProcessBuilder(starter, switcher, azCommand.toString());
            // Redirects stdin to dev null, helps to avoid messages sent in by the cmd process to upgrade etc.
            builder.redirectInput(ProcessBuilder.Redirect.from(IdentityUtil.NULL_FILE));

            String workingDirectory = getSafeWorkingDirectory();
            if (workingDirectory != null) {
                builder.directory(new File(workingDirectory));
            } else {
                throw LOGGER.logThrowableAsError(new IllegalStateException("A Safe Working directory could not be"
                    + " found to execute CLI command from. To mitigate this issue, please refer to the troubleshooting "
                    + " guidelines here at https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader
                = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (WINDOWS_PROCESS_ERROR_MESSAGE.matcher(line).find()
                        || SH_PROCESS_ERROR_MESSAGE.matcher(line).find()) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER,
                            new CredentialUnavailableException(
                                "AzureCliCredential authentication unavailable. Azure CLI not installed."
                                    + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                                    + "https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
                    }
                    output.append(line);
                }
            }
            String processOutput = output.toString();

            process.waitFor(this.clientOptions.getProcessTimeout().getSeconds(), TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                if (processOutput.length() > 0) {
                    String redactedOutput = redactInfo(processOutput);
                    if (redactedOutput.contains("az login") || redactedOutput.contains("az account set")) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER,
                            new CredentialUnavailableException("AzureCliCredential authentication unavailable."
                                + " Please run 'az login' to set up account. To further mitigate this"
                                + " issue, please refer to the troubleshooting guidelines here at "
                                + "https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
                    }
                    throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(redactedOutput));
                } else {
                    throw LOGGER
                        .logThrowableAsError(new CredentialAuthenticationException("Failed to invoke Azure CLI "));
                }
            }

            LOGGER.atLevel(LogLevel.VERBOSE)
                .log("Azure CLI Authentication => A token response was received from Azure CLI, deserializing the"
                    + " response into an Access Token.");
            try (JsonReader reader = JsonReader.fromString(processOutput)) {
                AzureCliToken tokenHolder = AzureCliToken.fromJson(reader);
                String accessToken = tokenHolder.getAccessToken();
                OffsetDateTime tokenExpiration = tokenHolder.getTokenExpiration();
                token = new AccessToken(accessToken, tokenExpiration);
            }

        } catch (IOException | InterruptedException e) {
            IllegalStateException ex = new IllegalStateException(redactInfo(e.getMessage()));
            ex.setStackTrace(e.getStackTrace());
            throw LOGGER.logThrowableAsError(ex);
        }
        return token;
    }

    String getSafeWorkingDirectory() {
        if (isWindowsPlatform()) {
            String windowsSystemRoot = System.getenv("SystemRoot");
            if (CoreUtils.isNullOrEmpty(windowsSystemRoot)) {
                return null;
            }

            return windowsSystemRoot + "\\system32";
        } else {
            return DEFAULT_MAC_LINUX_PATH;
        }
    }

    String redactInfo(String input) {
        return ACCESS_TOKEN_PATTERN.matcher(input).replaceAll("****");
    }

    public DevToolsClientOptions getClientOptions() {
        return clientOptions;
    }
}
