import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    // Test files pattern
    include: ['src/**/*.{test,spec}.{js,ts}'],
    
    // Environment
    environment: 'node',
    
    // TypeScript support
    globals: true,
    
    // Coverage (optional)
    coverage: {
      provider: 'v8',
      include: ['src/**/*.ts'],
      exclude: ['src/**/*.{test,spec}.ts']
    }
  }
});
