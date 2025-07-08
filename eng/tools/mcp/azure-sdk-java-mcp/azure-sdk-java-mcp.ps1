# this script will run npm install, npm run build, and use node to start the mcp server

param(
    [switch]$SkipInstall,
    [switch]$SkipBuild,
    [switch]$Dev,
    [switch]$Help
)

function Show-Help {
    Write-Host "Azure SDK Java MCP Server Launcher" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage: .\azure-sdk-java-mcp.ps1 [options]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -SkipInstall    Skip npm install step"
    Write-Host "  -SkipBuild      Skip npm run build step"
    Write-Host "  -Dev            Run in development mode (npm run dev)"
    Write-Host "  -Help           Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\azure-sdk-java-mcp.ps1                    # Full build and start"
    Write-Host "  .\azure-sdk-java-mcp.ps1 -SkipInstall       # Skip install, build and start"
    Write-Host "  .\azure-sdk-java-mcp.ps1 -Dev               # Development mode with watch"
    Write-Host ""
}

if ($Help) {
    Show-Help
    exit 0
}

# Set error action preference
$ErrorActionPreference = "Stop"

try {
    # Get the script directory
    $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    Write-Host "Working directory: $ScriptDir" -ForegroundColor Cyan
    
    # Change to the script directory
    Set-Location $ScriptDir
    
    # Check if package.json exists
    if (-not (Test-Path "package.json")) {
        Write-Error "package.json not found in current directory: $ScriptDir"
        exit 1
    }
    
    # Check if Node.js is available
    try {
        $nodeVersion = node --version
        Write-Host "Node.js version: $nodeVersion" -ForegroundColor Green
    } catch {
        Write-Error "Node.js is not installed or not in PATH. Please install Node.js first."
        exit 1
    }
    
    # Check if npm is available
    try {
        $npmVersion = npm --version
        Write-Host "npm version: $npmVersion" -ForegroundColor Green
    } catch {
        Write-Error "npm is not installed or not in PATH. Please install npm first."
        exit 1
    }
    
    # Step 1: npm install (unless skipped)
    if (-not $SkipInstall) {
        Write-Host "`nRunning npm install..." -ForegroundColor Yellow
        npm install
        if ($LASTEXITCODE -ne 0) {
            Write-Error "npm install failed with exit code $LASTEXITCODE"
            exit $LASTEXITCODE
        }
        Write-Host "npm install completed successfully" -ForegroundColor Green
    } else {
        Write-Host "`nSkipping npm install..." -ForegroundColor Yellow
    }
    
    # Step 2: npm run clean and build (unless skipped or in dev mode)
    if (-not $SkipBuild -and -not $Dev) {
        Write-Host "`nRunning npm run clean..." -ForegroundColor Yellow
        npm run clean
        if ($LASTEXITCODE -ne 0) {
            Write-Error "npm run clean failed with exit code $LASTEXITCODE"
            exit $LASTEXITCODE
        }
        Write-Host "npm run clean completed successfully" -ForegroundColor Green
        
        Write-Host "`nRunning npm run build..." -ForegroundColor Yellow
        npm run build
        if ($LASTEXITCODE -ne 0) {
            Write-Error "npm run build failed with exit code $LASTEXITCODE"
            exit $LASTEXITCODE
        }
        Write-Host "npm run build completed successfully" -ForegroundColor Green
    } elseif ($Dev) {
        Write-Host "`nSkipping clean and build for development mode..." -ForegroundColor Yellow
    } else {
        Write-Host "`nSkipping npm run clean and build..." -ForegroundColor Yellow
    }
    
    # Step 3: Start the server
    if ($Dev) {
        Write-Host "`nStarting MCP server in development mode..." -ForegroundColor Yellow
        Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Cyan
        npm run start
    } else {
        # Check if dist directory exists
        if (-not (Test-Path "dist")) {
            Write-Error "dist directory not found. Please run build first or use -Dev flag."
            exit 1
        }
        
        # Check if main file exists
        if (-not (Test-Path "dist/index.js")) {
            Write-Error "dist/index.js not found. Please run build first."
            exit 1
        }
        
        Write-Host "`nStarting MCP server..." -ForegroundColor Yellow
        Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Cyan
        npm run start
    }
} catch {
    Write-Error "An error occurred: $($_.Exception.Message)"
    exit 1
} finally {
    # Restore original location
    Pop-Location -ErrorAction SilentlyContinue
}