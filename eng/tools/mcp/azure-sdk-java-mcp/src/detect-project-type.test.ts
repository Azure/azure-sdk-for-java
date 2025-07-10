import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { detectProjectType } from './detect-project-type.js';
import * as fs from 'fs';
import * as path from 'path';

// Mock fs module
vi.mock('fs', () => ({
  existsSync: vi.fn(),
}));

// Mock path module  
vi.mock('path', () => ({
  join: vi.fn(),
}));

describe('detectProjectType', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should return error when project path does not exist', async () => {
    // Arrange
    const projectPath = '/non/existent/path';
    vi.mocked(fs.existsSync).mockReturnValue(false);

    // Act
    const result = await detectProjectType(projectPath);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('❌ Error: Project path does not exist');
    expect(result.content[0].text).toContain(projectPath);
  });

  it('should detect existing TypeSpec project', async () => {
    // Arrange
    const projectPath = '/sdk/documentintelligence/azure-ai-documentintelligence';
    const tspLocationPath = path.join(projectPath, 'tsp-location.yaml');
    const pomXmlPath = path.join(projectPath, 'pom.xml');
    const srcPath = path.join(projectPath, 'src');

    vi.mocked(path.join)
      .mockReturnValueOnce(tspLocationPath)
      .mockReturnValueOnce(pomXmlPath)
      .mockReturnValueOnce(srcPath)
      .mockReturnValueOnce(path.join(projectPath, 'TempTypeSpecFiles'));

    vi.mocked(fs.existsSync)
      .mockImplementation((filePath) => {
        if (filePath === projectPath) return true;
        if (filePath === tspLocationPath) return true;
        if (filePath === pomXmlPath) return true;
        if (filePath === srcPath) return true;
        return false;
      });

    // Act
    const result = await detectProjectType(projectPath);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('existing-typespec');
    expect(text).toContain('🔄 This is an EXISTING TypeSpec-based Java SDK project');
    expect(text).toContain("✅ Use 'update_java_sdk'");
    expect(text).toContain("⚠️  Avoid using 'sync_java_sdk'");
    expect(text).toContain('✓ tsp-location.yaml');
    expect(text).toContain('✓ pom.xml');
    expect(text).toContain('✓ src/ directory');
  });

  it('should detect existing Maven project without TypeSpec', async () => {
    // Arrange
    const projectPath = '/existing/maven/project';
    const tspLocationPath = path.join(projectPath, 'tsp-location.yaml');
    const pomXmlPath = path.join(projectPath, 'pom.xml');
    const srcPath = path.join(projectPath, 'src');

    vi.mocked(path.join)
      .mockReturnValueOnce(tspLocationPath)
      .mockReturnValueOnce(pomXmlPath)
      .mockReturnValueOnce(srcPath)
      .mockReturnValueOnce(path.join(projectPath, 'TempTypeSpecFiles'));

    vi.mocked(fs.existsSync)
      .mockImplementation((filePath) => {
        if (filePath === projectPath) return true;
        if (filePath === tspLocationPath) return false; // No tsp-location.yaml
        if (filePath === pomXmlPath) return true;
        if (filePath === srcPath) return true;
        return false;
      });

    // Act
    const result = await detectProjectType(projectPath);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('existing-maven');
    expect(text).toContain('📦 This appears to be an existing Maven project without TypeSpec integration');
    expect(text).toContain("🆕 Use 'sync_java_sdk' to initialize TypeSpec-based SDK generation");
    expect(text).toContain("📋 Consider reviewing 'instruction_migrate_typespec'");
  });

  it('should detect ready-for-generation project', async () => {
    // Arrange
    const projectPath = '/ready/for/generation';
    const tempTypeSpecPath = path.join(projectPath, 'TempTypeSpecFiles');

    vi.mocked(path.join)
      .mockReturnValueOnce(path.join(projectPath, 'tsp-location.yaml'))
      .mockReturnValueOnce(path.join(projectPath, 'pom.xml'))
      .mockReturnValueOnce(path.join(projectPath, 'src'))
      .mockReturnValueOnce(tempTypeSpecPath);

    vi.mocked(fs.existsSync)
      .mockImplementation((filePath) => {
        if (filePath === projectPath) return true;
        if (filePath === tempTypeSpecPath) return true;
        return false;
      });

    // Act
    const result = await detectProjectType(projectPath);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('ready-for-generation');
    expect(text).toContain('🎯 This directory contains TypeSpec source files ready for generation');
    expect(text).toContain("✅ Use 'generate_java_sdk'");
  });

  it('should detect new project', async () => {
    // Arrange
    const projectPath = '/new/empty/project';

    vi.mocked(path.join)
      .mockReturnValueOnce(path.join(projectPath, 'tsp-location.yaml'))
      .mockReturnValueOnce(path.join(projectPath, 'pom.xml'))
      .mockReturnValueOnce(path.join(projectPath, 'src'))
      .mockReturnValueOnce(path.join(projectPath, 'TempTypeSpecFiles'));

    vi.mocked(fs.existsSync)
      .mockImplementation((filePath) => {
        if (filePath === projectPath) return true;
        return false; // No project files exist
      });

    // Act
    const result = await detectProjectType(projectPath);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('new-project');
    expect(text).toContain('🆕 This appears to be a new/empty project directory');
    expect(text).toContain("✅ Use 'sync_java_sdk' to initialize");
    expect(text).toContain('❌ No key project files detected');
  });

  it('should handle errors gracefully', async () => {
    // Arrange
    const projectPath = '/error/path';
    vi.mocked(fs.existsSync).mockImplementation(() => {
      throw new Error('File system error');
    });

    // Act
    const result = await detectProjectType(projectPath);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('❌ Error detecting project type');
    expect(result.content[0].text).toContain('File system error');
  });

  it('should detect TypeSpec project with TempTypeSpecFiles', async () => {
    // Arrange
    const projectPath = '/typespec/with/temp';
    const tspLocationPath = path.join(projectPath, 'tsp-location.yaml');
    const tempTypeSpecPath = path.join(projectPath, 'TempTypeSpecFiles');

    vi.mocked(path.join)
      .mockReturnValueOnce(tspLocationPath)
      .mockReturnValueOnce(path.join(projectPath, 'pom.xml'))
      .mockReturnValueOnce(path.join(projectPath, 'src'))
      .mockReturnValueOnce(tempTypeSpecPath);

    vi.mocked(fs.existsSync)
      .mockImplementation((filePath) => {
        if (filePath === projectPath) return true;
        if (filePath === tspLocationPath) return true;
        if (filePath === tempTypeSpecPath) return true;
        return false;
      });

    // Act
    const result = await detectProjectType(projectPath);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('existing-typespec');
    expect(text).toContain("📁 Found 'TempTypeSpecFiles' directory");
    expect(text).toContain("you can also use 'generate_java_sdk' directly");
  });
});
