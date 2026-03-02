#!/bin/bash
# provision-benchmark-vm.sh — Create a new Azure VM or connect to an existing one
# See §6.5 of the test plan for full details.
#
# Usage:
#   ./provision-benchmark-vm.sh --new --location westus2 [--create-key] [--ssh-key <pub>] [--config-dir <path>] [options]
#   ./provision-benchmark-vm.sh --existing --ip <ip> --user <user> --key <private-key> [--config-dir <path>]
#   ./provision-benchmark-vm.sh --existing --rg <rg> --vm-name <name> --key <private-key> [--config-dir <path>]
#
# Config directory: where VM connection info is saved. Required.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MODE=""
LOCATION="westus2"
RG="rg-cosmos-benchmark-$(date +%Y%m%d)"
VM_NAME="vm-benchmark-01"
VM_SIZE="Standard_D16s_v5"
VM_IP=""
SSH_USER="benchuser"
SSH_PRIVATE_KEY=""
SSH_PUBLIC_KEY=""
CREATE_KEY=false
CREATE_KEY_PATH=""
DISK_SIZE=256
SETUP_AFTER_CREATE=true
CONFIG_DIR=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --new)          MODE="new" ;;
        --existing)     MODE="existing" ;;
        --location)     LOCATION="$2"; shift ;;
        --rg)           RG="$2"; shift ;;
        --vm-name)      VM_NAME="$2"; shift ;;
        --size)         VM_SIZE="$2"; shift ;;
        --ip)           VM_IP="$2"; shift ;;
        --user)         SSH_USER="$2"; shift ;;
        --key)          SSH_PRIVATE_KEY="$2"; shift ;;
        --ssh-key)      SSH_PUBLIC_KEY="$2"; shift ;;
        --create-key)
            CREATE_KEY=true
            if [[ $# -gt 1 && ! "$2" =~ ^-- ]]; then
                CREATE_KEY_PATH="$2"; shift
            fi
            ;;
        --disk-size)    DISK_SIZE="$2"; shift ;;
        --skip-setup)   SETUP_AFTER_CREATE=false ;;
        --config-dir)   CONFIG_DIR="$2"; shift ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
    shift
done

ssh_cmd() {
    local cmd="ssh"
    [[ -n "$SSH_PRIVATE_KEY" ]] && cmd="$cmd -i $SSH_PRIVATE_KEY"
    echo "$cmd"
}

generate_ssh_key() {
    local key_path="$1"
    mkdir -p "$(dirname "$key_path")"
    if [[ -f "$key_path" ]]; then
        echo "  SSH key already exists at $key_path — reusing."
    else
        echo "  Generating new SSH key pair: $key_path"
        ssh-keygen -t rsa -b 4096 -f "$key_path" -N "" -C "cosmos-benchmark-${VM_NAME}" -q
    fi
    chmod 600 "$key_path"
    chmod 644 "${key_path}.pub"
    SSH_PRIVATE_KEY="$key_path"
    SSH_PUBLIC_KEY="${key_path}.pub"
}

if [[ "$MODE" == "new" ]]; then
    echo "=== Creating VM: $VM_NAME in $RG ($LOCATION) ==="
    az group create --name "$RG" --location "$LOCATION" 2>/dev/null || true

    SSH_KEY_ARGS=""
    if [[ "$CREATE_KEY" == "true" ]]; then
        [[ -z "$CREATE_KEY_PATH" ]] && CREATE_KEY_PATH="$HOME/.ssh/cosmos-bench-${VM_NAME}"
        generate_ssh_key "$CREATE_KEY_PATH"
        SSH_KEY_ARGS="--ssh-key-value $SSH_PUBLIC_KEY"
    elif [[ -n "$SSH_PUBLIC_KEY" ]]; then
        SSH_KEY_ARGS="--ssh-key-value $SSH_PUBLIC_KEY"
        [[ -z "$SSH_PRIVATE_KEY" && "$SSH_PUBLIC_KEY" == *.pub ]] && SSH_PRIVATE_KEY="${SSH_PUBLIC_KEY%.pub}"
    else
        SSH_KEY_ARGS="--generate-ssh-keys"
        [[ -z "$SSH_PRIVATE_KEY" ]] && SSH_PRIVATE_KEY="$HOME/.ssh/id_rsa"
    fi

    az vm create --resource-group "$RG" --name "$VM_NAME" --image Ubuntu2204 \
      --size "$VM_SIZE" --accelerated-networking true --admin-username "$SSH_USER" \
      $SSH_KEY_ARGS --authentication-type ssh --os-disk-size-gb "$DISK_SIZE" --storage-sku Premium_LRS

    az vm open-port --resource-group "$RG" --name "$VM_NAME" --port 22
    VM_IP=$(az vm show -g "$RG" -n "$VM_NAME" -d --query publicIps -o tsv)
    echo "VM created. IP: $VM_IP"

    if [[ "$SETUP_AFTER_CREATE" == "true" ]]; then
        echo "=== Installing tools on VM ==="
        $(ssh_cmd) -o StrictHostKeyChecking=no "${SSH_USER}@${VM_IP}" 'bash -s' << 'SETUP_SCRIPT'
set -euo pipefail
echo "=== Installing JDK, Maven, tools ==="
sudo apt-get update && sudo apt-get install -y openjdk-21-jdk git net-tools iproute2 sysstat procps tmux

wget -q https://dlcdn.apache.org/maven/maven-3/3.9.12/binaries/apache-maven-3.9.12-bin.tar.gz -O /tmp/maven.tar.gz
sudo tar -xzf /tmp/maven.tar.gz -C /opt/
sudo ln -sf /opt/apache-maven-3.9.12/bin/mvn /usr/local/bin/mvn

wget -qO /tmp/async-profiler.tar.gz \
  https://github.com/async-profiler/async-profiler/releases/download/v3.0/async-profiler-3.0-linux-x64.tar.gz
sudo tar -xzf /tmp/async-profiler.tar.gz -C /opt/
echo 'export PATH=$PATH:/opt/apache-maven-3.9.12/bin:/opt/async-profiler-3.0-linux-x64/bin' >> ~/.bashrc

echo "=== VM tool setup complete ==="
SETUP_SCRIPT
    fi

elif [[ "$MODE" == "existing" ]]; then
    [[ -z "$SSH_PRIVATE_KEY" && -f "$HOME/.ssh/id_rsa" ]] && SSH_PRIVATE_KEY="$HOME/.ssh/id_rsa"
    [[ -z "$SSH_PRIVATE_KEY" ]] && { echo "ERROR: --key <private-key-path> required"; exit 1; }
    [[ -z "$VM_IP" ]] && VM_IP=$(az vm show -g "$RG" -n "$VM_NAME" -d --query publicIps -o tsv)
    echo "=== Connecting to ${SSH_USER}@${VM_IP} ==="
    $(ssh_cmd) -o ConnectTimeout=10 "${SSH_USER}@${VM_IP}" 'echo "VM reachable. JDK: $(java -version 2>&1 | head -1)"'
else
    echo "Usage: $0 --new --location <region> | --existing --ip <ip> --key <key>"
    exit 1
fi

if [[ -z "$CONFIG_DIR" ]]; then
    echo "ERROR: --config-dir <path> is required" >&2
    exit 1
fi

mkdir -p "$CONFIG_DIR"
echo "$VM_IP" > "$CONFIG_DIR/vm-ip"
echo "$SSH_USER" > "$CONFIG_DIR/vm-user"
echo "$SSH_PRIVATE_KEY" > "$CONFIG_DIR/vm-key"

echo "VM_IP=$VM_IP" > "$CONFIG_DIR/vm-config.env"
echo "VM_USER=$SSH_USER" >> "$CONFIG_DIR/vm-config.env"
echo "VM_KEY_PATH=$SSH_PRIVATE_KEY" >> "$CONFIG_DIR/vm-config.env"
echo "=== Ready: $(ssh_cmd) ${SSH_USER}@${VM_IP} ==="
echo "=== Config saved to: $CONFIG_DIR ==="
