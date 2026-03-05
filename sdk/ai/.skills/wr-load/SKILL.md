---
name: wr-load
description: Manage Azure KeyVault work resources via the work-resources CLI (wr-setup, wr-save, wr-update, wr-load, wr-list, wr-delete, wr-clear, wr-migrate). Use for provisioning, listing, loading, saving, updating, deleting, or clearing secrets.
---

# Work Resources (Azure KeyVault)

Use the `wr-*` CLI commands from the **work-resources** project to manage secrets for test/dev resources.

## Prerequisite
The `wr-*` commands must be installed and available on PATH. If they are missing, instruct the user to install them from the work-resources repo:
- Repo: https://github.com/jpalvarezl/work-resources
- Install: follow the repo README (install.sh / install.ps1)

## Key rule: minimize wr-load calls

`wr-load` fetches secrets from Azure KeyVault over the network. **Call it at most once per session.** After the first call, cache the values you received and reuse them directly in subsequent commands. Do NOT call `wr-load` again for the same resource.

## Commands

### `wr-load`
Load secrets into the current shell session. The script detects your shell automatically; use `-Export` to control the output format.

```bash
wr-load -Resource <resource>
wr-load -Resource "res1,res2"
wr-load -SpawnShell
```

To combine with another command (env vars persist within the same shell process):
```bash
wr-load -Resource <resource>; <your-command>
```

### `wr-list`
List secrets in the vault (optionally by resource prefix).
```bash
wr-list
wr-list -Resource <resource>
```

### `wr-save`
Save a new secret. Always pass `-Value` to avoid interactive prompts that hang in pi.
```bash
wr-save -Resource <resource> -Name <secret-name> -EnvVarName <ENV_VAR> -Value <value>
```

### `wr-update`
Update an existing secret. Always pass `-Value` to avoid interactive prompts that hang in pi.
```bash
wr-update -Resource <resource> -Name <secret-name> -EnvVarName <ENV_VAR> -Value <value>
```

### `wr-delete`
Delete secrets from the vault.
```bash
wr-delete -Resource <resource> -Name <secret-name> -Force
wr-delete -Resource <resource> -All -Force
```

### `wr-clear`
Clear loaded secrets from the current session.
```bash
wr-clear
wr-clear -Resource <resource>
```

### `wr-setup`
Initial KeyVault setup (creates resource group/vault and assigns permissions).
```bash
wr-setup
```

### `wr-migrate`
Maintenance tool to add missing tags.
```bash
wr-migrate -DryRun
wr-migrate -Force
```

## Steps
1. Determine which `wr-*` command matches the user request.
2. If the request requires a resource name and it's missing, run `wr-list` and ask the user to pick one.
3. Execute the appropriate `wr-*` command.
4. For `wr-save` and `wr-update`, always include `-Value` to prevent interactive prompts.
5. After loading secrets with `wr-load`, cache the values. Do not call `wr-load` again for the same resource.
