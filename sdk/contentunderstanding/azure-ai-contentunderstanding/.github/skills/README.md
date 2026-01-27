# Azure SDK Agent Skills

This directory contains GitHub Copilot Agent Skills for Azure SDK development. These skills are designed to be **cross-language compatible** and can be adapted for Java, Python, .NET, and JavaScript SDKs.

## 📐 Design Principles

### 1. Progressive Disclosure

Skills use a three-level loading system:

| Level | Content | When Loaded |
|-------|---------|-------------|
| Level 1 | `name` + `description` (YAML front matter) | Always visible for relevance matching |
| Level 2 | SKILL.md body | When request matches description |
| Level 3 | Scripts, templates, examples | Only when Copilot needs to reference them |

### 2. Naming Convention

```
sdk-{action}[-{qualifier}]
```

| Pattern | Examples |
|---------|----------|
| `sdk-{action}` | `sdk-compile`, `sdk-test`, `sdk-setup-env` |
| `sdk-{action}-{qualifier}` | `sdk-test-record`, `sdk-test-playback` |
| `{lang}-{service}-{action}` | `java-cu-create-async-sample` (language-specific) |

### 3. Directory Structure

```
skill-name/
├── SKILL.md           # Main skill file (required)
├── scripts/           # Executable scripts
│   ├── run.sh         # Unix/macOS
│   └── run.ps1        # Windows PowerShell (optional)
├── references/        # Reference documentation
│   └── patterns.md
└── templates/         # Code templates (optional)
    └── template.java
```

### 4. Cross-Language Support

Skills should define **what to do**, not **how to do it**:

| Abstraction | Content | Example |
|-------------|---------|---------|
| SKILL.md | Universal description, workflow, checklist | "Compile SDK" |
| scripts/ | Language-specific implementation | `compile.sh` (mvn/pip/dotnet/npm) |

## 📦 Available Skills

### Core SDK Skills

| Skill | Description | Priority |
|-------|-------------|----------|
| [`sdk-setup-env`](sdk-setup-env/) | Load environment variables from .env file | P0 |
| [`sdk-compile`](sdk-compile/) | Compile SDK source code | P0 |
| [`sdk-test-record`](sdk-test-record/) | Run tests in RECORD mode | P0 |
| [`sdk-test-playback`](sdk-test-playback/) | Run tests in PLAYBACK mode | P0 |
| [`sdk-push-recordings`](sdk-push-recordings/) | Push session recordings to assets repo | P1 |
| [`sdk-run-sample`](sdk-run-sample/) | Run a single sample | P1 |
| [`sdk-run-all-samples`](sdk-run-all-samples/) | Run all samples | P2 |

### Workflow Skills

| Skill | Description | Steps |
|-------|-------------|-------|
| [`sdk-workflow-record-push`](sdk-workflow-record-push/) | Complete RECORD and PUSH workflow | setup → compile → record → push → playback |

### Language-Specific Skills

| Skill | Language | Description |
|-------|----------|-------------|
| [`java-cu-create-async-sample`](java-cu-create-async-sample/) | Java | Create async samples with reactive patterns |

## 🚀 Quick Start

### 1. Setup Environment

```bash
# Use sdk-setup-env skill to load .env
source .github/skills/sdk-setup-env/scripts/load-env.sh
```

### 2. Compile SDK

```bash
# Use sdk-compile skill
.github/skills/sdk-compile/scripts/compile.sh
```

### 3. Run Tests

```bash
# RECORD mode (requires Azure credentials)
.github/skills/sdk-test-record/scripts/test-record.sh

# PLAYBACK mode (uses recorded responses)
.github/skills/sdk-test-playback/scripts/test-playback.sh
```

### 4. Push Recordings

```bash
# Push to Azure SDK Assets repo
.github/skills/sdk-push-recordings/scripts/push-recordings.sh
```

## 🔧 Language-Specific Commands

| Action | Java | Python | .NET | JavaScript |
|--------|------|--------|------|------------|
| Compile | `mvn compile` | `pip install -e .` | `dotnet build` | `npm run build` |
| Test Record | `mvn test -DAZURE_TEST_MODE=RECORD` | `pytest --azure-test-mode=record` | `dotnet test /p:TestMode=Record` | `npm test -- --test-mode=record` |
| Test Playback | `mvn test -DAZURE_TEST_MODE=PLAYBACK` | `pytest --azure-test-mode=playback` | `dotnet test /p:TestMode=Playback` | `npm test -- --test-mode=playback` |
| Push Recordings | `test-proxy push -a assets.json` | `test-proxy push -a assets.json` | `test-proxy push -a assets.json` | `test-proxy push -a assets.json` |

## 📝 Contributing

When creating new skills:

1. **Follow naming convention**: `sdk-{action}[-{qualifier}]`
2. **Include SKILL.md**: With YAML front matter (name, description)
3. **Keep description under 1024 chars**: Copilot uses it for relevance matching
4. **Single responsibility**: One skill does one thing
5. **Self-contained**: Include all needed scripts/templates
6. **Cross-language when possible**: Language-specific only when necessary
