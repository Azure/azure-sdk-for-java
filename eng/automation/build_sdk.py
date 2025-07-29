#!/usr/bin/env python3
"""
Build SDK script for Azure SDK for Java.
This script builds a specific SDK module given its absolute path.
"""

import os
import sys
import argparse
import subprocess
import logging
from pathlib import Path
from typing import Optional

# Setup logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


def validate_module_directory(module_dir: str) -> Path:
    """
    Validate that the provided module directory exists and contains tsp-location.yaml.
    
    Args:
        module_dir: Absolute path to the module directory
        
    Returns:
        Path object of the validated module directory
        
    Raises:
        ValueError: If the directory is invalid
    """
    module_path = Path(module_dir)
    
    if not module_path.exists():
        raise ValueError(f"Module directory does not exist: {module_dir}")
    
    if not module_path.is_dir():
        raise ValueError(f"Path is not a directory: {module_dir}")
    
    # Check for pom.xml file
    pom_file = module_path / "pom.xml"
    if not pom_file.exists():
        raise ValueError(f"No pom.xml found in module directory: {module_dir}")
    
    # Check for tsp-location.yaml file
    tsp_location_file = module_path / "tsp-location.yaml"
    if not tsp_location_file.exists():
        raise ValueError(f"No tsp-location.yaml found in module directory: {module_dir}")
    
    
    return module_path

def run_maven_build(module_path: Path) -> bool:
    """
    Run Maven build for the specified module.
    
    Args:
        module_path: Path to the module directory
        
    Returns:
        True if build succeeded, False otherwise
    """
    try:
        # Change to module directory
        original_cwd = os.getcwd()
        os.chdir(module_path)
        
        # Prepare Maven command
        mvn_cmd = ["mvn", "clean", "package"]
        
        # Add common Maven options
        mvn_cmd.extend([
            "-Dmaven.javadoc.skip=true",
            "-Dcodesnippet.skip=true",
            "-Dgpg.skip=true",
            "-Drevapi.skip=true",
        ])

        command = "mvn clean package -Dmaven.javadoc.skip -Dgpg.skip -DskipTestCompile -Djacoco.skip -Drevapi.skip"
        logging.info(command)
        if os.system(command) == 0:
            logger.info("✅ Maven build completed successfully!")
            
            # Check if JAR was created
            target_dir = module_path / "target"
            jar_files = list(target_dir.glob("*.jar"))
            if jar_files:
                logger.info(f"Generated JAR files:")
                for jar_file in jar_files:
                    logger.info(f"  - {jar_file}")
            
            return True
        else:
            logger.error("❌ Maven build failed!")
            logger.error(f"Exit code: {result.returncode}")
            logger.error(f"STDOUT:\n{result.stdout}")
            logger.error(f"STDERR:\n{result.stderr}")
            return False
    finally:
        os.chdir(original_cwd)


def parse_args() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Build Azure SDK for Java module",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python build_sdk.py --module-dir "C:\\workspace\\azure-sdk-for-java\\sdk\\communication\\azure-communication-messages"
  python build_sdk.py --module-dir "C:\\workspace\\azure-sdk-for-java\\sdk\\dnsresolver\\azure-resourcemanager-dnsresolver"
        """
    )
    
    parser.add_argument(
        "--module-dir",
        required=True,
        help="Absolute path to the SDK module directory (e.g., C:\\workspace\\azure-sdk-for-java\\sdk\\communication\\azure-communication-messages)"
    )
    
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Enable verbose logging"
    )
    
    return parser.parse_args()


def main():
    """Main entry point."""
    args = parse_args()
    
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    try:
        # Validate module directory
        logger.info(f"Validating module directory: {args.module_dir}")
        module_path = validate_module_directory(args.module_dir)
        
        # Run Maven build
        logger.info("Starting Maven build...")
        success = run_maven_build(module_path=module_path)
        
        if success:
            logger.info("🎉 SDK build completed successfully!")
            sys.exit(0)
        else:
            logger.error("💥 SDK build failed!")
            sys.exit(1)
            
    except Exception as e:
        logger.error(f"Build failed with error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()