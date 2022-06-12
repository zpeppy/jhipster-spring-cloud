# 组件及服务

### 基于组件

- jhipster:v6.10.5, jhipster/jhipster-registry:v6.8.0

- spring-boot:2.2.7.RELEASE, spring-cloud:Hoxton.SR4, spring-cloud-alibaba:2.2.6.RELEASE

- nacos/nacos-server:v2.1.0

- openzipkin/zipkin:2

- prom/prometheus:v2.35.0, grafana/grafana:8.5.2

### 启动服务

##### mysql

```docker
docker run -d --name mysql -v /docker/mysql/data:/var/lib/mysql -v /docker/mysql/conf:/etc/mysql/conf.d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0.27 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```

##### postgresql

```docker
docker run --name postgresql -e POSTGRES_PASSWORD=postgres -e PGDATA=/var/lib/postgresql/data/pgdata -v /docker/postgres/data:/var/lib/postgresql/data -p 5432:5432 -d postgres:14.2
```

##### redis

```docker
docker run -v /docker/redis/config:/usr/local/etc/redis -v D/docker/redis/data:/data -p 6379:6379 --name redis -d redis:7.0.0 redis-server /usr/local/etc/redis/redis.conf
```

##### rabbitmq

```docker
docker run --name rabbitmq -v /docker/rabbitmq/data:/var/lib/rabbitmq -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest -p 15672:15672 -p 5672:5672 -d rabbitmq:3.9.17-management
```

##### elasticsearch

```docker
docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -v /docker/elasticsearch/config:/usr/share/elasticsearch/config -v /docker/elasticsearch/data:/usr/share/elasticsearch/data -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms1g -Xmx1g" -d elasticsearch:6.8.16
```

- 在 Windows 的 Docker desktop 下使用 ES，通常会遇到内存不足的问题。
  ```shell
  max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
  ```
- 使用 WSL 作为后端，调整的方式是通过命令行 wsl 进入 docker-desktop 的终端，然后通过 Linux 的 sysctl 命令调整系统参数。
  ```shell
  wsl -d docker-desktop
  sysctl -w vm.max_map_count=262144
  ```
- 退出后重启 docker-desktop，再次启动 es，成功！

---

- 访问拒绝错误
  ```shell
  ERROR Could not register mbeans java.security.AccessControlException: access denied ("
    javax.management.MBeanTrustPermission" "register")
  ERROR Unable to unregister MBeans java.security.AccessControlException: access denied ("
    javax.management.MBeanServerPermission" "createMBeanServer")
  ```
- 原因是 elasticsearch 使用了 SecurityManager。解决的办法有两种：
- 1.根据错误提示，在 java security manager 配置白名单。access denied ("javax.management.MBeanTrustPermission" "register")。 在 JAVA_HOME
  的 conf/security 目录下的 java.policy 中添加如下一行：
  ```shell
  permission javax.management.MBeanTrustPermission "register"
  ```
- 2.添加启动 JVM 参数。
  ```shell
  -Dlog4j2.disable.jmx=true
  ```

##### clickhouse

```docker
docker run -d --name clickhouse-server -p 8123:8123 -p 9009:9009 -p 9000:9000 --ulimit nofile=262144:262144 -v /docker/clickhouse/data:/var/lib/clickhouse -v /docker/clickhouse/config:/etc/clickhouse-server -v /docker/clickhouse/logs:/var/log/clickhouse-server yandex/clickhouse-server:22.4.5.9
```

- 先不使用配置文件挂载，直接使用 docker run 查看环境变量及挂载路径

- 错误信息
  ```shell
  <Warning> Application: Listen [::1]:8123 failed: Poco::Exception. Code: 1000, e.code() = 99, e.displayText() = Net
  Exception: Cannot assign requested address: [::1]:8123 (version 21.6.3.14 (official build)). If it is an IPv6 or IPv4
  address and your host has disabled IPv6 or IPv4, then consider to specify not disabled IPv4 or IPv6 address to listen
  in <listen_host> element of configuration file. Example for disabled IPv6: <listen_host>0.0.0.0</listen_host> .
  Example for disabled IPv4: <listen_host>::</listen_host>
  
  <Error> Application: DB::Exception: Listen [::]:8123 failed: Poco::Exception. Code: 1000, e.code() = 0,
  e.displayText() = DNS error: EAI: Address family for hostname not supported (version 21.6.3.14 (official build))
  ```
- 原因：本机没有开放 ipv6，只能对 ipv4生效。在 /etc/clickhouse-server/config.xml中，把 <listen_host> 改成 0.0.0.0 或者 ::

##### seata

```docker
docker run --name seata-server -p 8091:8091 -v /docker/seata-server/config:/seata-server/resources seataio/seata-server:1.4.2
```

- 修改环境变量：seata-server -e SEATA_CONFIG_NAME=file:/seata-server/resources

##### nacos

```docker
docker run --name nacos -e MODE=standalone -p 8848:8848 -d nacos/nacos-server:v2.1.0
```

##### zipkin

```docker
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin:2
```

##### prometheus

```docker
docker run -d --name=prometheus -p 9090:9090 -v /docker/prometheus/config:/etc/prometheus prom/prometheus:v2.35.0
```

- 创建 /docker/prometheus/config/prometheus.yml

```yaml
# global config
global:
  scrape_interval: 15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          # - alertmanager:9093

rule_files:
# - "first_rules.yml"
# - "second_rules.yml"

scrape_configs:
  # 配置监控的 Job
  - job_name: 'prometheus'
    # 拉取监控数据的地址 --> 完整路径即为 targets[i]/metrics_path, 本 Job 即为 http://localhost:9090/metrics
    metrics_path: '/metrics'
    static_configs:
      # 监控的目标 -- 这里配置的是监控 prometheus 自身
      - targets: [ 'localhost:9090' ]
        # 添加一个标识
        labels:
          instance: prometheus
  # 监控其他微服务
  - job_name: 'microservice'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: [ 'host.docker.internal:8081' ]
        labels:
          instance: microservice
```

##### grafana

```docker
docker run -d --name=grafana -p 3000:3000 grafana/grafana:8.5.2
```

### hosts 文件配置

```shell
# /etc/hosts
127.0.0.1 registry
127.0.0.1 mysql
127.0.0.1 redis
127.0.0.1 nacos
127.0.0.1 zipkin
```

### 通过二进制包方式安装服务

##### jhipster-registry：

点击下载 [jhipster-registry-6.8.0.jar](https://github.com/jhipster/jhipster-registry/releases/download/v6.8.0/jhipster-registry-6.8.0.jar)

##### mysql：

点击下载 [mysql-8.0.29-winx64.zip](https://dev.mysql.com/downloads/file/?id=511178)

##### redis：

点击下载 [redis-7.0.1.tar.gz](https://github.com/redis/redis/archive/7.0.1.tar.gz)

##### nacos：

点击下载 [nacos-server-2.1.0.zip](https://github.com/alibaba/nacos/releases/download/2.1.0/nacos-server-2.1.0.zip)
或者 [nacos-server-2.1.0.tar.gz](https://github.com/alibaba/nacos/releases/download/2.1.0/nacos-server-2.1.0.tar.gz)

##### zipkin：

点击下载 [zipkin-server-2.23.16-exec.jar](https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=2.23.16&c=exec)

##### prometheus:

点击下载 [prometheus-2.36.1.windows-amd64.zip](https://github.com/prometheus/prometheus/releases/download/v2.36.1/prometheus-2.36.1.windows-amd64.zip)
或者 [prometheus-2.36.1.linux-amd64.tar.gz](https://github.com/prometheus/prometheus/releases/download/v2.36.1/prometheus-2.36.1.linux-amd64.tar.gz)

##### grafana:

点击下载 [grafana-enterprise-8.5.5.windows-amd64.zip](https://dl.grafana.com/enterprise/release/grafana-enterprise-8.5.5.windows-amd64.zip)
或者点击进入官网下载页面 [grafana download](https://grafana.com/grafana/download)
