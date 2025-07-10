import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { prepareJavaSdkEnvironmentCookbook } from './prepare-environment.js';

describe('prepareJavaSdkEnvironmentCookbook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should generate environment setup guide with working directory', async () => {
    // Arrange
    const cwd = '/test/working/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    
    // Check that working directory is included
    expect(text).toContain(`**Working Directory:** \`${cwd}\``);
    
    // Check main sections are present
    expect(text).toContain('# Java SDK Generation Environment Setup Guide');
    expect(text).toContain('## 1. Node.js/npm Environment Setup');
    expect(text).toContain('## 2. Java Environment Setup');
    expect(text).toContain('## 3. TypeSpec Environment Setup');
    expect(text).toContain('## 4. Complete Environment Verification');
    expect(text).toContain('## 5. Troubleshooting Common Issues');
  });

  it('should include required version information', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    const text = result.content[0].text;
    
    // Check version requirements
    expect(text).toContain('**Required**: Node.js 18+ and npm 8+');
    expect(text).toContain('**Required**: Java 8+ and Maven 3.6+');
    expect(text).toContain('**Required**: TypeSpec Client Generator CLI and TypeSpec Compiler');
    
    // Check minimum versions section
    expect(text).toContain('### Minimum Required Versions:');
    expect(text).toContain('✅ **Node.js**: 18.0.0+');
    expect(text).toContain('✅ **npm**: 8.0.0+');
    expect(text).toContain('✅ **Java**: 8+ (11+ recommended)');
    expect(text).toContain('✅ **Maven**: 3.6.0+');
    expect(text).toContain('✅ **TypeSpec CLI**: Latest version');
    expect(text).toContain('✅ **TypeSpec Compiler**: Latest version');
  });

  it('should include platform-specific installation instructions', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    const text = result.content[0].text;
    
    // Check Windows instructions
    expect(text).toContain('**Windows**: Download from [nodejs.org]');
    expect(text).toContain('winget install OpenJS.NodeJS');
    expect(text).toContain('winget install Microsoft.OpenJDK.17');
    expect(text).toContain('winget install Apache.Maven');
    
    // Check macOS instructions
    expect(text).toContain('**macOS**: Download from [nodejs.org]');
    expect(text).toContain('brew install node');
    expect(text).toContain('brew install openjdk@17');
    expect(text).toContain('brew install maven');
    
    // Check Linux instructions
    expect(text).toContain('**Linux**: Use your package manager');
    expect(text).toContain('sudo apt install nodejs npm');
    expect(text).toContain('sudo apt install nodejs npm openjdk-17-jdk maven');
  });

  it('should include verification commands', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    const text = result.content[0].text;
    
    // Check verification commands
    expect(text).toContain('node --version    # Should show v18.0.0 or higher');
    expect(text).toContain('npm --version     # Should show v8.0.0 or higher');
    expect(text).toContain('java -version     # Should show your Java version');
    expect(text).toContain('mvn --version     # Should show Maven version and Java version');
    expect(text).toContain('tsp-client --version');
    expect(text).toContain('tsp --version');
    
    // Check comprehensive verification script
    expect(text).toContain('echo "Node.js: $(node --version)"');
    expect(text).toContain('echo "npm: $(npm --version)"');
    expect(text).toContain('echo "Java: $(java -version 2>&1 | head -n 1)"');
    expect(text).toContain('echo "Maven: $(mvn --version | head -n 1)"');
  });

  it('should include environment variable setup', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    const text = result.content[0].text;
    
    // Check environment variables section
    expect(text).toContain('### Environment Variables:');
    expect(text).toContain('JAVA_HOME=C:\\Program Files\\Microsoft\\jdk-17.0.x.x-hotspot');
    expect(text).toContain('MAVEN_HOME=C:\\tools\\apache-maven-3.9.x');
    expect(text).toContain('%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin  # (Windows)');
    expect(text).toContain('$JAVA_HOME/bin:$MAVEN_HOME/bin    # (Linux/macOS)');
  });

  it('should include TypeSpec installation options', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    const text = result.content[0].text;
    
    // Check TypeSpec installation options
    expect(text).toContain('### Global Installation (Recommended):');
    expect(text).toContain('npm install -g @azure-tools/typespec-client-generator-cli');
    expect(text).toContain('npm install -g @typespec/compiler');
    
    expect(text).toContain('### Local Installation (Alternative):');
    expect(text).toContain('npm install @azure-tools/typespec-client-generator-cli');
    expect(text).toContain('npm install @typespec/compiler');
    
    // Check npx alternatives
    expect(text).toContain('npx tsp-client --version');
    expect(text).toContain('npx tsp --version');
  });

  it('should include troubleshooting section', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    const text = result.content[0].text;
    
    // Check troubleshooting sections
    expect(text).toContain('## 5. Troubleshooting Common Issues');
    expect(text).toContain('### Node.js/npm Issues:');
    expect(text).toContain('### Java Issues:');
    expect(text).toContain('### TypeSpec Issues:');
    expect(text).toContain('### Maven Issues:');
    
    // Check specific troubleshooting advice
    expect(text).toContain('Permission errors');
    expect(text).toContain('JAVA_HOME not set');
    expect(text).toContain('Command not found');
    expect(text).toContain('Network issues');
    expect(text).toContain('use nvm (Node Version Manager)');
    expect(text).toContain('update-alternatives');
    expect(text).toContain('Configure proxy settings');
  });

  it('should include quick setup scripts for all platforms', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd);

    // Assert
    const text = result.content[0].text;
    
    // Check quick setup scripts section
    expect(text).toContain('**Quick Setup Scripts:**');
    
    // Windows PowerShell script
    expect(text).toContain('### Windows (PowerShell):');
    expect(text).toContain('winget install OpenJS.NodeJS');
    expect(text).toContain('winget install Microsoft.OpenJDK.17');
    expect(text).toContain('winget install Apache.Maven');
    
    // macOS script
    expect(text).toContain('### macOS (Terminal):');
    expect(text).toContain('brew install node');
    expect(text).toContain('brew install openjdk@17');
    expect(text).toContain('brew install maven');
    
    // Linux script
    expect(text).toContain('### Linux (Ubuntu/Debian):');
    expect(text).toContain('sudo apt update');
    expect(text).toContain('sudo apt install nodejs npm openjdk-17-jdk maven');
  });

  it('should not include status checks when autoCheck is false', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd, false);

    // Assert
    const text = result.content[0].text;
    expect(text).not.toContain('## Current Environment Status');
    expect(text).not.toContain('🔍 **Checking your current environment...**');
  });

  it('should include status checks section when autoCheck is true', async () => {
    // Arrange
    const cwd = '/test/directory';

    // Act
    const result = await prepareJavaSdkEnvironmentCookbook(cwd, true);

    // Assert
    const text = result.content[0].text;
    expect(text).toContain('## Current Environment Status');
    expect(text).toContain('🔍 **Checking your current environment...**');
  });

  it('should handle errors in autoCheck gracefully', async () => {
    // Arrange
    const cwd = '/test/directory';
    
    // Mock an error in the status check logic (when implemented)
    // For now, just test that the function doesn't throw
    
    // Act & Assert
    await expect(prepareJavaSdkEnvironmentCookbook(cwd, true)).resolves.toBeDefined();
  });
});
