#!/usr/bin/env bash
set -euo pipefail
WAR="${1:-/tmp/employee-management.war}"
APP="/var/lib/tomcat10/webapps/ROOT.war"
BACKUP="/var/backups/employee-management"
sudo install -d -o tomcat -g tomcat "$BACKUP"
if [[ -f "$APP" ]]; then sudo cp "$APP" "$BACKUP/ROOT-$(date +%Y%m%d%H%M%S).war"; fi
sudo install -o tomcat -g tomcat -m 0640 "$WAR" "$APP"
sudo systemctl restart tomcat10
for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/dev/null; then
    echo "Deployment health check passed"; exit 0
  fi
  sleep 5
done
echo "Health check failed. Review logs and restore a prior WAR from $BACKUP" >&2
exit 1
