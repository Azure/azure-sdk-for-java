import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { updateJavaSdk } from './update-java-sdk.js';
import * as fs from 'fs';
import * as path from 'path';

// Mock dependencies
vi.mock('fs', () => ({
  existsSync: vi.fn(),
}));

vi.mock('path', () => ({
  join: vi.fn(),
}));

vi.mock('./utils/index.js', () => ({
  spawnAsync: vi.fn(),
}));

// Import the mocked function
import { spawnAsync } from './utils/index.js';

describe('updateJavaSdk', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should return error when package path does not exist', async () => {
    // Arrange
    const packagePath = '/non/existent/path';
    vi.mocked(fs.existsSync).mockReturnValue(false);

    // Act
    const result = await updateJavaSdk(packagePath);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('Error: Package path does not exist');
    expect(result.content[0].text).toContain(packagePath);
  });

  it('should return error when tsp-location.yaml not found', async () => {
    // Arrange
    const packagePath = '/existing/path';
    const tspLocationPath = '/existing/path/tsp-location.yaml';
    
    vi.mocked(path.join).mockReturnValue(tspLocationPath);
    vi.mocked(fs.existsSync)
      .mockReturnValueOnce(true)  // packagePath exists
      .mockReturnValueOnce(false); // tsp-location.yaml doesn't exist

    // Act
    const result = await updateJavaSdk(packagePath);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('Error: tsp-location.yaml not found');
    expect(result.content[0].text).toContain('use sync_java_sdk instead');
  });

  it('should successfully update Java SDK with minimal parameters', async () => {
    // Arrange
    const packagePath = '/existing/typespec/project';
    const tspLocationPath = '/existing/typespec/project/tsp-location.yaml';
    
    vi.mocked(path.join).mockReturnValue(tspLocationPath);
    vi.mocked(fs.existsSync).mockReturnValue(true);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'SDK update completed successfully',
      stderr: '',
      exitCode: 0
    });

    // Act
    const result = await updateJavaSdk(packagePath);

    // Assert
    expect(vi.mocked(spawnAsync)).toHaveBeenCalledWith(
      'tsp-client',
      ['update', '--debug', '--output-dir', packagePath, '--save-inputs'],
      {
        cwd: packagePath,
        shell: true,
        timeout: 600000
      }
    );
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('✅ Java SDK update completed successfully!');
    expect(result.content[0].text).toContain('SDK update completed successfully');
  });

  it('should include all optional parameters when provided', async () => {
    // Arrange
    const packagePath = '/existing/typespec/project';
    const commitHash = 'abc123def456';
    const repo = 'Azure/azure-rest-api-specs';
    const tspConfig = '/path/to/tspconfig.yaml';
    const localSpec = '/local/spec/path';
    
    vi.mocked(path.join).mockReturnValue('/existing/typespec/project/tsp-location.yaml');
    vi.mocked(fs.existsSync).mockReturnValue(true);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'SDK update completed with all parameters',
      stderr: '',
      exitCode: 0
    });

    // Act
    const result = await updateJavaSdk(packagePath, commitHash, repo, tspConfig, localSpec);

    // Assert
    expect(vi.mocked(spawnAsync)).toHaveBeenCalledWith(
      'tsp-client',
      [
        'update',
        '--debug',
        '--output-dir', packagePath,
        '--commit', commitHash,
        '--repo', repo,
        '--tsp-config', tspConfig,
        '--local-spec-repo', localSpec,
        '--save-inputs'
      ],
      {
        cwd: packagePath,
        shell: true,
        timeout: 600000
      }
    );
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('✅ Java SDK update completed successfully!');
  });

  it('should handle partial optional parameters', async () => {
    // Arrange
    const packagePath = '/existing/typespec/project';
    const commitHash = 'abc123def456';
    // Only providing commitHash, not other parameters
    
    vi.mocked(path.join).mockReturnValue('/existing/typespec/project/tsp-location.yaml');
    vi.mocked(fs.existsSync).mockReturnValue(true);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'SDK update completed with commit hash',
      stderr: '',
      exitCode: 0
    });

    // Act
    const result = await updateJavaSdk(packagePath, commitHash);

    // Assert
    expect(vi.mocked(spawnAsync)).toHaveBeenCalledWith(
      'tsp-client',
      [
        'update',
        '--debug',
        '--output-dir', packagePath,
        '--commit', commitHash,
        '--save-inputs'
      ],
      {
        cwd: packagePath,
        shell: true,
        timeout: 600000
      }
    );
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('✅ Java SDK update completed successfully!');
  });

  it('should handle tsp-client update failure', async () => {
    // Arrange
    const packagePath = '/existing/typespec/project';
    
    vi.mocked(path.join).mockReturnValue('/existing/typespec/project/tsp-location.yaml');
    vi.mocked(fs.existsSync).mockReturnValue(true);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: false,
      stdout: 'Some output before failure',
      stderr: 'TypeSpec compilation failed',
      exitCode: 1
    });

    // Act
    const result = await updateJavaSdk(packagePath);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('❌ Java SDK update failed!');
    expect(result.content[0].text).toContain('TypeSpec compilation failed');
    expect(result.content[0].text).toContain('Some output before failure');
  });

  it('should handle unexpected errors', async () => {
    // Arrange
    const packagePath = '/existing/typespec/project';
    
    vi.mocked(path.join).mockReturnValue('/existing/typespec/project/tsp-location.yaml');
    vi.mocked(fs.existsSync).mockReturnValue(true);
    vi.mocked(spawnAsync).mockRejectedValue(new Error('Network timeout'));

    // Act
    const result = await updateJavaSdk(packagePath);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('❌ Error updating Java SDK');
    expect(result.content[0].text).toContain('Network timeout');
  });

  it('should log the command being executed', async () => {
    // Arrange
    const packagePath = '/existing/typespec/project';
    const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {});
    
    vi.mocked(path.join).mockReturnValue('/existing/typespec/project/tsp-location.yaml');
    vi.mocked(fs.existsSync).mockReturnValue(true);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'Success',
      stderr: '',
      exitCode: 0
    });

    // Act
    await updateJavaSdk(packagePath);

    // Assert
    expect(consoleSpy).toHaveBeenCalledWith(
      expect.stringContaining('Running tsp-client update with args:')
    );
    expect(consoleSpy).toHaveBeenCalledWith(
      expect.stringContaining('update --debug --output-dir')
    );
    
    consoleSpy.mockRestore();
  });
});
