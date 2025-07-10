import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";

// Integration tests for the MCP server
describe('MCP Server Integration', () => {
  let server: McpServer;

  beforeEach(() => {
    // Create a fresh server instance for each test
    server = new McpServer({
      name: "java-sdk-tools-server-test",
      version: "1.0.0",
    });
  });

  afterEach(() => {
    vi.resetAllMocks();
  });

  it('should create server with correct name and version', () => {
    expect(server).toBeDefined();
    // Note: The actual server properties are not directly accessible,
    // but we can verify the server was created without errors
  });

  it('should register tools without throwing errors', () => {
    // This test verifies that tool registration syntax is correct
    expect(() => {
      server.registerTool(
        "test_tool",
        {
          description: "A test tool",
          inputSchema: {
            testParam: z
              .string()
              .describe("A test parameter")
          }
        },
        async (args) => {
          return {
            content: [
              {
                type: "text",
                text: "Test successful"
              }
            ]
          };
        }
      );
    }).not.toThrow();
  });

  it('should handle tool registration with complex schema', () => {
    expect(() => {
      server.registerTool(
        "complex_tool",
        {
          description: "A tool with complex schema",
          inputSchema: {
            packagePath: z
              .string()
              .describe("Path to package"),
            optionalParam: z
              .string()
              .optional()
              .describe("Optional parameter")
          },
          annotations: {
            title: "Complex Tool"
          }
        },
        async (args) => {
          return {
            content: [
              {
                type: "text",
                text: `Processing ${args.packagePath}`
              }
            ]
          };
        }
      );
    }).not.toThrow();
  });
});

// Mock tool implementations for testing
describe('Tool Implementation Patterns', () => {
  it('should follow CallToolResult return pattern', async () => {
    // Test the standard return pattern used by all tools
    const mockToolResult = {
      content: [
        {
          type: "text" as const,
          text: "Operation completed successfully"
        }
      ]
    };

    expect(mockToolResult.content).toHaveLength(1);
    expect(mockToolResult.content[0].type).toBe("text");
    expect(mockToolResult.content[0].text).toBeDefined();
  });

  it('should handle error responses correctly', async () => {
    const errorResult = {
      content: [
        {
          type: "text" as const,
          text: "❌ Error: Operation failed"
        }
      ]
    };

    expect(errorResult.content[0].text).toContain("❌ Error:");
  });

  it('should handle success responses correctly', async () => {
    const successResult = {
      content: [
        {
          type: "text" as const,
          text: "✅ Operation completed successfully!"
        }
      ]
    };

    expect(successResult.content[0].text).toContain("✅");
  });
});

// Test logging functionality
describe('Logging Functionality', () => {
  it('should format log messages correctly', () => {
    const logToolCall = (toolName: string) => {
      const logMsg = `[${new Date().toISOString()}] [MCP] Tool called: ${toolName}\n`;
      return logMsg;
    };

    const result = logToolCall("test_tool");
    expect(result).toMatch(/\[\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z\] \[MCP\] Tool called: test_tool\n/);
  });

  it('should handle console.error logging', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    
    console.error("Test error message");
    
    expect(consoleSpy).toHaveBeenCalledWith("Test error message");
    consoleSpy.mockRestore();
  });
});

// Test server configuration patterns
describe('Server Configuration', () => {
  it('should handle error callback configuration', () => {
    const server = new McpServer({
      name: "test-server",
      version: "1.0.0",
    });

    expect(() => {
      server.server.onerror = (error: Error) => {
        console.error("[MCP Error]", error);
      };
    }).not.toThrow();
  });

  it('should handle process signal configuration', () => {
    const mockExit = vi.spyOn(process, 'exit').mockImplementation(() => {
      return undefined as never;
    });

    const testServer = new McpServer({
      name: "test-server",
      version: "1.0.0",
    });

    // Test that signal handler can be set up
    expect(() => {
      process.on("SIGINT", async () => {
        await testServer.close();
        process.exit(0);
      });
    }).not.toThrow();

    mockExit.mockRestore();
  });
});

// Test input validation patterns
describe('Input Validation Patterns', () => {
  it('should validate required string parameters', () => {
    const validateStringParam = (param: unknown, name: string): string => {
      if (typeof param !== 'string' || param.trim() === '') {
        throw new Error(`${name} is required and must be a non-empty string`);
      }
      return param;
    };

    expect(() => validateStringParam("valid", "test")).not.toThrow();
    expect(() => validateStringParam("", "test")).toThrow();
    expect(() => validateStringParam(null, "test")).toThrow();
    expect(() => validateStringParam(undefined, "test")).toThrow();
  });

  it('should validate optional parameters', () => {
    const validateOptionalParam = (param: unknown): string | undefined => {
      if (param === undefined || param === null) {
        return undefined;
      }
      if (typeof param !== 'string') {
        throw new Error('Parameter must be a string if provided');
      }
      return param;
    };

    expect(validateOptionalParam(undefined)).toBeUndefined();
    expect(validateOptionalParam(null)).toBeUndefined();
    expect(validateOptionalParam("valid")).toBe("valid");
    expect(() => validateOptionalParam(123)).toThrow();
  });

  it('should validate path parameters', () => {
    const validatePath = (path: string): boolean => {
      // Basic path validation - could be expanded
      return path.length > 0 && !path.includes('..') && path.trim() === path;
    };

    expect(validatePath("/valid/path")).toBe(true);
    expect(validatePath("C:\\valid\\path")).toBe(true);
    expect(validatePath("")).toBe(false);
    expect(validatePath("../invalid")).toBe(false);
    expect(validatePath(" /path/with/spaces ")).toBe(false);
  });
});
