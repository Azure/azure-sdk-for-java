#!/bin/bash
# Bash script to load .env file
# Usage: source load-env.sh
#        or: . load-env.sh

env_file=".env"
if [ ! -f "$env_file" ]; then
    echo "Error: .env file not found at $env_file" >&2
    return 1 2>/dev/null || exit 1
fi

while IFS= read -r line || [ -n "$line" ]; do
    # Skip empty lines and comments
    if [[ "$line" =~ ^[[:space:]]*# ]] || [[ -z "${line// }" ]]; then
        continue
    fi
    
    # Parse KEY=value format
    if [[ "$line" =~ ^[[:space:]]*([^#][^=]+)=(.*)$ ]]; then
        name="${BASH_REMATCH[1]}"
        name="${name%"${name##*[![:space:]]}"}"  # trim trailing whitespace
        name="${name#"${name%%[![:space:]]*}"}"  # trim leading whitespace
        
        value="${BASH_REMATCH[2]}"
        value="${value%"${value##*[![:space:]]}"}"  # trim trailing whitespace
        value="${value#"${value%%[![:space:]]*}"}"  # trim leading whitespace
        
        # Remove quotes if present (both single and double)
        if [[ "$value" =~ ^[\"'](.*)[\"']$ ]]; then
            value="${BASH_REMATCH[1]}"
        fi
        
        # Export the variable
        export "$name=$value"
        echo "Exported: $name"
    fi
done < "$env_file"

echo "Environment variables loaded from .env file"
