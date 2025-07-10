import { vi } from 'vitest';

// Mock utility functions for testing
export const createMockSpawnResult = (success: boolean, stdout = '', stderr = '', exitCode = 0) => ({
  success,
  stdout,
  stderr,
  exitCode
});

export const mockFsExists = (existingPaths: string[]) => {
  return vi.fn((filePath: string) => existingPaths.includes(filePath));
};

export const mockPathJoin = () => {
  return vi.fn((...segments: string[]) => segments.join('/'));
};

// Common test data
export const testPaths = {
  azureSdkRoot: '/azure-sdk-for-java',
  documentIntelligence: '/azure-sdk-for-java/sdk/documentintelligence/azure-ai-documentintelligence',
  batch: '/azure-sdk-for-java/sdk/batch/azure-compute-batch',
  tspLocation: 'tsp-location.yaml',
  pomXml: 'pom.xml',
  srcDir: 'src',
  tempTypeSpecFiles: 'TempTypeSpecFiles'
};

export const testGroupIds = {
  azure: 'com.azure',
  azureResourceManager: 'com.azure.resourcemanager'
};

export const testArtifactIds = {
  documentIntelligence: 'azure-ai-documentintelligence',
  batch: 'azure-compute-batch'
};

// Mock successful Maven output
export const mockMavenSuccess = `
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] Building Azure AI Document Intelligence SDK 1.1.0-beta.1
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ azure-ai-documentintelligence ---
[INFO] Compiling 150 source files to /target/classes
[INFO] 
[INFO] --- maven-jar-plugin:3.2.0:jar (default-jar) @ azure-ai-documentintelligence ---
[INFO] Building jar: /target/azure-ai-documentintelligence-1.1.0-beta.1.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 2:30 min
[INFO] Finished at: 2025-01-09T10:30:00Z
[INFO] ------------------------------------------------------------------------
`;

// Mock TypeSpec update output
export const mockTypeSpecUpdateSuccess = `
Using output directory 'C:/azure-sdk-for-java/sdk/documentintelligence/azure-ai-documentintelligence'

888                                      888 d8b                   888    
888                                      888 Y8P                   888    
888                                      888                       888    
888888 .d8888b  88888b.          .d8888b 888 888  .d88b.  88888b.  888888 
888    88K      888 "88b        d88P"    888 888 d8P  Y8b 888 "88b 888    
888    "Y8888b. 888  888 888888 888      888 888 88888888 888  888 888    
Y88b.       X88 888 d88P        Y88b.    888 888 Y8b.     888  888 Y88b.  
 "Y888  88888P' 88888P"          "Y8888P 888 888  "Y8888  888  888  "Y888 
                888                                                       
                888                                                       
                888                                                       

0.24.0
Created temporary working directory
Using project name: DocumentIntelligence
Found emitter package @azure-tools/typespec-java@0.31.6
Compiling tsp using @azure-tools/typespec-java...
generation complete
`;

// Mock compilation error
export const mockCompilationError = `
[ERROR] COMPILATION ERROR :
[INFO] -------------------------------------------------------------
[ERROR] /src/main/java/com/azure/ai/documentintelligence/DocumentIntelligenceClient.java:[42,8] error: cannot find symbol
  symbol:   class UndefinedClass
  location: class DocumentIntelligenceClient
[INFO] 1 error
[INFO] -------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.8.1:compile (default-compile) on project azure-ai-documentintelligence: Compilation failure
[ERROR] /src/main/java/com/azure/ai/documentintelligence/DocumentIntelligenceClient.java:[42,8] error: cannot find symbol
[ERROR] -> [Help 1]
`;

// Test utilities for creating mock file systems
export class MockFileSystem {
  private existingFiles: Set<string> = new Set();
  private existingDirs: Set<string> = new Set();

  addFile(path: string) {
    this.existingFiles.add(path);
    return this;
  }

  addDir(path: string) {
    this.existingDirs.add(path);
    return this;
  }

  addTypeSpecProject(basePath: string) {
    this.addFile(`${basePath}/tsp-location.yaml`);
    this.addFile(`${basePath}/pom.xml`);
    this.addDir(`${basePath}/src`);
    return this;
  }

  addMavenProject(basePath: string) {
    this.addFile(`${basePath}/pom.xml`);
    this.addDir(`${basePath}/src`);
    return this;
  }

  addTempTypeSpecFiles(basePath: string) {
    this.addDir(`${basePath}/TempTypeSpecFiles`);
    return this;
  }

  exists(path: string): boolean {
    return this.existingFiles.has(path) || this.existingDirs.has(path);
  }

  getMockFn() {
    return vi.fn((path: string) => this.exists(path));
  }
}
