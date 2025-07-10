import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { cleanJavaSource } from './clean-java-source.js';
import fs from 'fs';

// Mock fs module
vi.mock('fs', () => ({
  default: {
    promises: {
      rmdir: vi.fn(),
    },
  },
}));

describe('cleanJavaSource', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should successfully clean all Java source directories', async () => {
    // Arrange
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    vi.mocked(fs.promises.rmdir).mockResolvedValue(undefined);

    // Act
    const result = await cleanJavaSource(moduleDirectory);

    // Assert
    expect(fs.promises.rmdir).toHaveBeenCalledTimes(3);
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      `${moduleDirectory}/src/main`,
      { recursive: true }
    );
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      `${moduleDirectory}/src/samples`,
      { recursive: true }
    );
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      `${moduleDirectory}/src/test`,
      { recursive: true }
    );

    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toBe('Java source cleaned successfully.');
  });

  it('should handle rmdir failures gracefully', async () => {
    // Arrange
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    const error = new Error('Permission denied');
    vi.mocked(fs.promises.rmdir).mockRejectedValue(error);

    // Act & Assert
    await expect(cleanJavaSource(moduleDirectory)).rejects.toThrow('Permission denied');
    
    // Verify that the first rmdir call was made
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      `${moduleDirectory}/src/main`,
      { recursive: true }
    );
  });

  it('should handle partial failures', async () => {
    // Arrange
    const moduleDirectory = '/azure-sdk-for-java/sdk/batch/azure-compute-batch';
    vi.mocked(fs.promises.rmdir)
      .mockResolvedValueOnce(undefined) // src/main succeeds
      .mockRejectedValueOnce(new Error('Directory not found')) // src/samples fails
      .mockResolvedValueOnce(undefined); // src/test would succeed if reached

    // Act & Assert
    await expect(cleanJavaSource(moduleDirectory)).rejects.toThrow('Directory not found');
    
    // Verify that rmdir was called for src/main and src/samples
    expect(fs.promises.rmdir).toHaveBeenCalledTimes(2);
    expect(fs.promises.rmdir).toHaveBeenNthCalledWith(1,
      `${moduleDirectory}/src/main`,
      { recursive: true }
    );
    expect(fs.promises.rmdir).toHaveBeenNthCalledWith(2,
      `${moduleDirectory}/src/samples`,
      { recursive: true }
    );
  });

  it('should work with Windows-style paths', async () => {
    // Arrange
    const moduleDirectory = 'C:\\azure-sdk-for-java\\sdk\\batch\\azure-compute-batch';
    vi.mocked(fs.promises.rmdir).mockResolvedValue(undefined);

    // Act
    const result = await cleanJavaSource(moduleDirectory);

    // Assert
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      `${moduleDirectory}/src/main`,
      { recursive: true }
    );
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      `${moduleDirectory}/src/samples`,
      { recursive: true }
    );
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      `${moduleDirectory}/src/test`,
      { recursive: true }
    );

    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toBe('Java source cleaned successfully.');
  });

  it('should handle empty module directory path', async () => {
    // Arrange
    const moduleDirectory = '';
    vi.mocked(fs.promises.rmdir).mockResolvedValue(undefined);

    // Act
    const result = await cleanJavaSource(moduleDirectory);

    // Assert
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      '/src/main',
      { recursive: true }
    );
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      '/src/samples',
      { recursive: true }
    );
    expect(fs.promises.rmdir).toHaveBeenCalledWith(
      '/src/test',
      { recursive: true }
    );

    expect(result.content[0].type).toBe('text');
    expect(result.content[0].text).toBe('Java source cleaned successfully.');
  });

  it('should use recursive option for all rmdir calls', async () => {
    // Arrange
    const moduleDirectory = '/test/module';
    vi.mocked(fs.promises.rmdir).mockResolvedValue(undefined);

    // Act
    await cleanJavaSource(moduleDirectory);

    // Assert
    expect(fs.promises.rmdir).toHaveBeenCalledTimes(3);
    
    // Check that all calls include recursive: true
    const calls = vi.mocked(fs.promises.rmdir).mock.calls;
    calls.forEach(call => {
      expect(call[1]).toEqual({ recursive: true });
    });
  });

  it('should clean directories in the correct order', async () => {
    // Arrange
    const moduleDirectory = '/test/module';
    vi.mocked(fs.promises.rmdir).mockResolvedValue(undefined);

    // Act
    await cleanJavaSource(moduleDirectory);

    // Assert
    const calls = vi.mocked(fs.promises.rmdir).mock.calls;
    expect(calls[0][0]).toBe(`${moduleDirectory}/src/main`);
    expect(calls[1][0]).toBe(`${moduleDirectory}/src/samples`);
    expect(calls[2][0]).toBe(`${moduleDirectory}/src/test`);
  });
});
