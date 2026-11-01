# Monitoring baseline

## Azure-native option (recommended for a small VM)
1. Enable Azure Monitor Agent on the VM.
2. Create a Data Collection Rule for syslog and Tomcat/application logs.
3. Send metrics and logs to a Log Analytics workspace.
4. Create alerts for VM CPU > 85% for 10 minutes, low free disk, VM unavailable, and repeated HTTP health-check failures.
5. Restrict access to the workspace; redact credentials and personal data in logs.

## Prometheus/Grafana option
- `prometheus-node-exporter` exposes host metrics on port 9100. Keep it private; do not expose it to the internet.
- Add a Prometheus server and Grafana on a separate monitoring host/container or use a managed service.
- The starter app logs to stdout/console. Ship logs using Azure Monitor Agent or an approved log forwarder.
- To add JVM metrics, install a reviewed JMX exporter Java agent and restrict its endpoint to the monitoring network.
- This starter does not expose an application `/metrics` endpoint; add Micrometer/OpenTelemetry instrumentation before creating application latency/error-rate dashboards.
