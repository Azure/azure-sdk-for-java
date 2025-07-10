import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { buildJavaSdk } from './build-java-sdk.js';

// Mock dependencies
vi.mock('./utils/index.js', () => ({
  findAzureSdkRoot: vi.fn(),
  spawnAsync: vi.fn(),
}));

// Import mocked functions
import { findAzureSdkRoot, spawnAsync } from './utils/index.js';

describe('buildJavaSdk', () => {
  const originalPlatform = process.platform;
  const originalChdir = process.chdir;
  const originalCwd = process.cwd;

  beforeEach(() => {
    vi.clearAllMocks();
    process.chdir = vi.fn();
    process.cwd = vi.fn().mockReturnValue('/mocked/cwd');
  });

  afterEach(() => {
    vi.resetAllMocks();
    process.chdir = originalChdir;
    process.cwd = originalCwd;
    Object.defineProperty(process, 'platform', {
      value: originalPlatform,
    });
  });

  it('should successfully build Java SDK on Linux/macOS', async () => {
    // Arrange
    Object.defineProperty(process, 'platform', {
      value: 'linux',
    });

    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'BUILD SUCCESS\nTotal time: 2:30 min',
      stderr: '',
      exitCode: 0
    });

    // Act
    const result = await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(findAzureSdkRoot).toHaveBeenCalledWith(rootDir);
    expect(process.chdir).toHaveBeenCalledWith(rootDir);
    expect(spawnAsync).toHaveBeenCalledWith(
      'mvn',
      [
        '--no-transfer-progress',
        'clean',
        'package',
        '-f',
        `${moduleDirectory}/pom.xml`,
        '-Dmaven.javadoc.skip',
        '-Dcodesnippet.skip',
        '-Dgpg.skip',
        '-Drevapi.skip',
        '-pl',
        `${groupId}:${artifactId}`,
        '-am'
      ],
      {
        cwd: '/mocked/cwd',
        shell: true,
        timeout: 600000
      }
    );

    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('✅ SDK build completed successfully!');
  });

  it('should use mvn.cmd on Windows', async () => {
    // Arrange
    Object.defineProperty(process, 'platform', {
      value: 'win32',
    });

    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'BUILD SUCCESS',
      stderr: '',
      exitCode: 0
    });

    // Act
    await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(spawnAsync).toHaveBeenCalledWith(
      'mvn.cmd',
      expect.any(Array),
      expect.any(Object)
    );
  });

  it('should handle build failure with error output', async () => {
    // Arrange
    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: false,
      stdout: 'Some compilation output',
      stderr: 'Compilation error: Cannot find symbol',
      exitCode: 1
    });

    // Act
    const result = await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('❌ SDK build failed with exit code 1');
    expect(text).toContain('Output:\nSome compilation output');
    expect(text).toContain('Errors:\nCompilation error: Cannot find symbol');
  });

  it('should handle Maven command arguments correctly', async () => {
    // Arrange
    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/documentintelligence/azure-ai-documentintelligence';
    const groupId = 'com.azure';
    const artifactId = 'azure-ai-documentintelligence';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'BUILD SUCCESS',
      stderr: '',
      exitCode: 0
    });

    // Act
    await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    const expectedArgs = [
      '--no-transfer-progress',
      'clean',
      'package',
      '-f',
      `${moduleDirectory}/pom.xml`,
      '-Dmaven.javadoc.skip',
      '-Dcodesnippet.skip',
      '-Dgpg.skip',
      '-Drevapi.skip',
      '-pl',
      `${groupId}:${artifactId}`,
      '-am'
    ];

    expect(spawnAsync).toHaveBeenCalledWith(
      expect.any(String),
      expectedArgs,
      expect.any(Object)
    );
  });

  it('should handle findAzureSdkRoot failure', async () => {
    // Arrange
    const rootDir = '/invalid/path';
    const moduleDirectory = '/invalid/path/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockRejectedValue(new Error('Cannot find Azure SDK root'));

    // Act
    const result = await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('Unexpected error during SDK build');
    expect(result.content[0].text).toContain('Cannot find Azure SDK root');
  });

  it('should handle spawnAsync throwing an exception', async () => {
    // Arrange
    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockRejectedValue(new Error('Process spawn failed'));

    // Act
    const result = await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toContain('Unexpected error during SDK build');
    expect(result.content[0].text).toContain('Process spawn failed');
  });

  it('should handle build with only stdout output', async () => {
    // Arrange
    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: false,
      stdout: 'Build output with warnings',
      stderr: '',
      exitCode: 1
    });

    // Act
    const result = await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('❌ SDK build failed with exit code 1');
    expect(text).toContain('Output:\nBuild output with warnings');
    expect(text).not.toContain('Errors:');
  });

  it('should handle build with only stderr output', async () => {
    // Arrange
    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: false,
      stdout: '',
      stderr: 'Critical build error',
      exitCode: 1
    });

    // Act
    const result = await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(result.content[0].type).toBe('text');
    const text = result.content[0].text;
    expect(text).toContain('❌ SDK build failed with exit code 1');
    expect(text).toContain('Errors:\nCritical build error');
    expect(text).not.toContain('Output:');
  });

  it('should use correct timeout value', async () => {
    // Arrange
    const rootDir = '/azure-sdk-for-java';
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const groupId = 'com.azure';
    const artifactId = 'azure-compute-batch';

    vi.mocked(findAzureSdkRoot).mockResolvedValue(rootDir);
    vi.mocked(spawnAsync).mockResolvedValue({
      success: true,
      stdout: 'BUILD SUCCESS',
      stderr: '',
      exitCode: 0
    });

    // Act
    await buildJavaSdk(rootDir, moduleDirectory, groupId, artifactId);

    // Assert
    expect(spawnAsync).toHaveBeenCalledWith(
      expect.any(String),
      expect.any(Array),
      expect.objectContaining({
        timeout: 600000 // 10 minutes
      })
    );
  });
});
