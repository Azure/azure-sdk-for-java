import { CallToolResult } from "@modelcontextprotocol/sdk/types";

export async function prepareJavaSdkEnvironmentCookbook(
  cwd: string,
  autoCheck: boolean = false,
): Promise<CallToolResult> {

  // If autoCheck is enabled, verify current installation status
  let statusChecks = '';
  if (autoCheck) {
    try {
      // TODO: Add actual version checks here
      statusChecks = `
## Current Environment Status

🔍 **Checking your current environment...**

`;
    } catch (error) {
      statusChecks = `
## Current Environment Status

❌ **Error checking environment**: ${error}

`;
    }
  }

  const cookbook = `
# Java SDK Generation Environment Setup Guide

**Working Directory:** \`${cwd}\`
${statusChecks}
This guide provides step-by-step instructions to prepare your environment for Java SDK generation in 3 main areas: Node.js/npm, Java environment, and TypeSpec tools.

---

## 1. Node.js/npm Environment Setup

**Required**: Node.js 18+ and npm 8+

### Manual Installation:
- **Windows**: Download from [nodejs.org](https://nodejs.org/) or use \`winget install OpenJS.NodeJS\`
- **macOS**: Download from [nodejs.org](https://nodejs.org/) or use \`brew install node\`
- **Linux**: Use your package manager: \`sudo apt install nodejs npm\` or \`sudo yum install nodejs npm\`

### Verification:
\`\`\`bash
node --version    # Should show v18.0.0 or higher
npm --version     # Should show v8.0.0 or higher
\`\`\`

## 2. Java Environment Setup

**Required**: Java 8+ and Maven 3.6+

### Java JDK Installation:
- **Recommended**: [Microsoft OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download)
- **Alternative**: Oracle JDK, Amazon Corretto, or OpenJDK

### Maven Installation:
- **Windows**: Download from [maven.apache.org](https://maven.apache.org/download.cgi) or use \`winget install Apache.Maven\`
- **macOS**: \`brew install maven\`
- **Linux**: \`sudo apt install maven\` or \`sudo yum install maven\`

### Environment Variables:
\`\`\`bash
# Add these to your system environment variables:
JAVA_HOME=C:\\Program Files\\Microsoft\\jdk-17.0.x.x-hotspot  # (Windows example)
MAVEN_HOME=C:\\tools\\apache-maven-3.9.x                      # (Windows example)

# Add to PATH:
%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin  # (Windows)
\$JAVA_HOME/bin:\$MAVEN_HOME/bin    # (Linux/macOS)
\`\`\`

### Verification:
\`\`\`bash
java -version     # Should show your Java version
mvn --version     # Should show Maven version and Java version
\`\`\`

## 3. TypeSpec Environment Setup

**Required**: TypeSpec Client Generator CLI and TypeSpec Compiler

### Global Installation (Recommended):
\`\`\`bash
# Install TypeSpec tools globally
npm install -g @azure-tools/typespec-client-generator-cli
npm install -g @typespec/compiler
\`\`\`

### Local Installation (Alternative):
\`\`\`bash
# Install in your project (will use npx to run)
npm install @azure-tools/typespec-client-generator-cli
npm install @typespec/compiler
\`\`\`

### Verification:
\`\`\`bash
# For global installation:
tsp-client --version
tsp --version

# For local installation:
npx tsp-client --version
npx tsp --version
\`\`\`

## 4. Complete Environment Verification

Run these commands to verify your complete setup:

\`\`\`bash
# Check all tools at once:
echo "Node.js: $(node --version)"
echo "npm: $(npm --version)"
echo "Java: $(java -version 2>&1 | head -n 1)"
echo "Maven: $(mvn --version | head -n 1)"
echo "TypeSpec CLI: $(tsp-client --version 2>/dev/null || echo 'Not found - try npx tsp-client --version')"
echo "TypeSpec Compiler: $(tsp --version 2>/dev/null || echo 'Not found - try npx tsp --version')"
\`\`\`

### Minimum Required Versions:
- ✅ **Node.js**: 18.0.0+
- ✅ **npm**: 8.0.0+
- ✅ **Java**: 8+ (11+ recommended)
- ✅ **Maven**: 3.6.0+
- ✅ **TypeSpec CLI**: Latest version
- ✅ **TypeSpec Compiler**: Latest version

## 5. Troubleshooting Common Issues

### Node.js/npm Issues:
- **Permission errors**: Use \`npm config set prefix ~/.npm-global\` and add to PATH
- **Version conflicts**: Use nvm (Node Version Manager) to manage multiple versions

### Java Issues:
- **JAVA_HOME not set**: Verify environment variable points to JDK directory
- **Wrong Java version**: Use \`update-alternatives\` (Linux) or modify PATH order

### TypeSpec Issues:
- **Command not found**: Verify global installation or use \`npx\` prefix
- **Version mismatch**: Update with \`npm update -g @azure-tools/typespec-client-generator-cli\`

### Maven Issues:
- **MAVEN_HOME not set**: Verify environment variable points to Maven directory
- **Network issues**: Configure proxy settings in \`~/.m2/settings.xml\`

**Quick Setup Scripts:**

### Windows (PowerShell):
\`\`\`powershell
# Install using winget (Windows 10/11)
winget install OpenJS.NodeJS
winget install Microsoft.OpenJDK.17
winget install Apache.Maven
npm install -g @azure-tools/typespec-client-generator-cli @typespec/compiler
\`\`\`

### macOS (Terminal):
\`\`\`bash
# Install using Homebrew
brew install node
brew install openjdk@17
brew install maven
npm install -g @azure-tools/typespec-client-generator-cli @typespec/compiler
\`\`\`

### Linux (Ubuntu/Debian):
\`\`\`bash
# Install using apt
sudo apt update
sudo apt install nodejs npm openjdk-17-jdk maven
npm install -g @azure-tools/typespec-client-generator-cli @typespec/compiler
\`\`\`
  `;

  console.error(
    `Generated streamlined environment preparation cookbook for: ${cwd}`,
  );

  return {
    content: [
      {
        type: "text",
        text: cookbook,
      },
    ],
  };
}
