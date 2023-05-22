# grafana
Grafana + Prometheus
https://github.com/camunda-community-hub/camunda-8-benchmark#collect-and-inspect-metrics

# Camunda 8 performance related links
* https://camunda.com/blog/2022/05/how-to-benchmark-your-camunda-platform-8-cluster/
* https://github.com/camunda-community-hub/camunda-8-benchmark
* https://docs.camunda.io/docs/self-managed/zeebe-deployment/operations/backpressure/
* https://docs.camunda.io/docs/components/best-practices/architecture/sizing-your-environment
* https://camunda.com/blog/2019/10/0-21-release/#backpressure

# Collect and inspect metrics

The application provides some metrics via Spring Actuator that can be used via http://localhost:8090/actuator/prometheus for Prometheus.

To work locally, this project contains a docker-compose file that starts up a prometheus/grafana combo which scrapes the local Java application:

```
cd grafana
docker compose up
```

Now you can inspect metrics:

* via Prometheus, e.g. http://localhost:9090/graph?g0.expr=startedPi_total&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=1h
* via Grafana, e.g. http://localhost:3000/d/VEPGQXPnk/benchmark?orgId=1&from=now-15m&to=now