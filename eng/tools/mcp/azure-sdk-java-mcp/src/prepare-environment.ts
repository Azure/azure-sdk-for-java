import { CallToolResult } from "@modelcontextprotocol/sdk/types";

export async function prepareJavaSdkEnvironmentCookbook(cwd: string): Promise<CallToolResult> {
    const cookbook = `
# Java SDK Generation Environment Setup Guide

**Working Directory:** \`${cwd}\`

This guide helps prepare your development environment for Azure Java SDK generation. The agent should follow these steps to ensure all required tools are properly installed and configured.

---
## Step 0: Environment Status Check

**AGENT ACTION:** First, check the current environment status by running this single command:

\`\`\`
node --version && npm --version && tsp-client --version && tsp --version && java -version && mvn --version 
\`\`\`

**EXPECTED OUTCOME:** All tools should show their versions. If any tool shows "not recognized" or "not found", it needs to be installed.

---
## Step 1: Node.js and npm Installation

**REQUIREMENT:** Node.js version 20.0.0 or higher is required.

**AGENT ACTION:** 
- If Node.js version is ≥ 20.0.0, continue to next step
- If Node.js version is below 20.0.0 or not installed, automatically download and install latest Node.js LTS from https://nodejs.org/
- npm is included with Node.js installation
- After installation, verify with: \`node --version && npm --version\`

**SUCCESS CRITERIA:** Node.js ≥ 20.0.0 and npm showing valid version numbers.

---
## Step 2: Java Development Kit (JDK) Installation

**REQUIREMENT:** Java JDK version 17 or higher is required for Azure SDK development.

**AGENT ACTION:**
- If Java is found and version is ≥ 17, continue to next step
- If Java is not found or version is below 17, automatically download and install **latest** Microsoft OpenJDK from https://learn.microsoft.com/en-us/java/openjdk/download
- Automatically configure environment variables (JAVA_HOME and PATH)
- Refresh environment variables in current session

**SUCCESS CRITERIA:** \`java -version\` shows JDK 17+ and displays Java version information.

---
## Step 3: Apache Maven Installation

**REQUIREMENT:** Apache Maven is required for building Java SDK projects.

**AGENT ACTION:**
- If Maven is found, continue to next step
- If Maven is not found, automatically download and install **latest** version of Apache Maven:
  1. Download the latest binary zip from https://maven.apache.org/download.cgi (e.g., apache-maven-3.x.x-bin.zip)
  2. Extract to user directory (e.g., \`$env:USERPROFILE\\maven\\apache-maven\`)
  3. Set MAVEN_HOME environment variable to the Maven installation directory
  4. Add Maven bin directory to PATH environment variable
  5. Apply changes to both current session and user profile for persistence
- Refresh environment variables in current session

**INSTALLATION COMMAND:**
\`\`\`powershell
$mavenVersion = "[REPLACE_WITH_LATEST_VERSION]"; $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"; $downloadPath = "$env:TEMP\\apache-maven-$mavenVersion-bin.zip"; $extractPath = "$env:USERPROFILE\\maven"; Invoke-WebRequest -Uri $mavenUrl -OutFile $downloadPath; if (Test-Path $extractPath) { Remove-Item $extractPath -Recurse -Force }; Expand-Archive -Path $downloadPath -DestinationPath $extractPath; $mavenHome = "$extractPath\\apache-maven-$mavenVersion"; [Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenHome, "User"); $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User"); if ($currentPath -notlike "*$mavenHome\bin*") { [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$mavenHome\bin", "User") }; $env:MAVEN_HOME = $mavenHome; $env:PATH = "$env:PATH;$mavenHome\\bin"; Remove-Item $downloadPath
\`\`\`

**SUCCESS CRITERIA:** \`mvn --version\` shows Maven version and Java information.

---
## Step 4: TypeSpec Tools Installation

**REQUIREMENT:** typespec-client-generator-cli be 0.24.0+, @typespec/compiler be 1.0.0+.

**AGENT ACTION:** 
Automatically install/update TypeSpec tools by running:
\`\`\`
npm install -g @azure-tools/typespec-client-generator-cli@latest @typespec/compiler@latest
\`\`\`

**SUCCESS CRITERIA:** 
- \`tsp-client --version\` shows version (should be 0.24.0+)
- \`tsp --version\` shows TypeSpec compiler version (should be 1.0.0+)

---
## Step 5: Final Verification

**AGENT ACTION:** 
1. Run the complete environment check again:
\`\`\`
node --version && npm --version && tsp-client --version && tsp --version && java -version && mvn --version
\`\`\`
2. Restart VS Code to ensure all environment variables are refreshed.

**SUCCESS CRITERIA:** All commands should return version information without errors.

---
## Important Notes for Agent:

1. **Check ALL tools together:** Always use the combined command to get complete status
2. **Always install the latest version:** If you need to install a tool, always download and install the latest stable version
3. **Use PowerShell commands:** Execute PowerShell commands to download installers and configure environment variables
4. **Environment variable management:** 
   - Automatically set JAVA_HOME, MAVEN_HOME, and update PATH
   - Use \`[Environment]::SetEnvironmentVariable()\` to set both current session and user profile variables
   - For Java: Set JAVA_HOME to JDK installation path and add \`%JAVA_HOME%\\bin\` to PATH
   - For Maven: Set MAVEN_HOME to Maven installation path and add \`%MAVEN_HOME%\\bin\` to PATH
5. **Session refresh:** Refresh environment variables in current session after installation
6. **Version requirements are minimums:** Higher versions are acceptable and often preferred
7. **Complete automation:** Perform all installations without user intervention
8. **Administrator privileges:** Some installations may require elevated permissions
9. **Environment persistence:** Ensure environment variables are set for both current session and permanently for the user

**Troubleshooting:** 
- If installations fail: May need administrator privileges - run VS Code as administrator
- If tools still show "not recognized": Environment variables may need a VS Code restart
- If downloads fail: Check internet connection and firewall settings
- If versions are too old: Re-run the installation commands to get latest versions
  `;

    console.error(`Generated streamlined environment preparation cookbook for: ${cwd}`);

    return {
        content: [
            {
                type: "text",
                text: cookbook,
            },
        ],
    };
}
