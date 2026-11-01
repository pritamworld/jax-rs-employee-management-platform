#!/usr/bin/env bash
# Ubuntu 22.04/24.04 VM bootstrap. Run as a sudo-capable admin.
set -euo pipefail
sudo apt-get update
sudo apt-get install -y temurin-21-jdk maven tomcat10 curl unzip jq prometheus-node-exporter
sudo systemctl enable --now tomcat10
sudo systemctl enable --now prometheus-node-exporter
sudo install -d -o root -g root -m 0755 /var/backups/employee-management
sudo install -d -o root -g root -m 0755 /usr/local/sbin
sudo install -o root -g root -m 0750 ops/deploy-employee-management.sh /usr/local/sbin/deploy-employee-management
echo "Bootstrap complete. Configure secrets/environment before deployment."
