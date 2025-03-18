# resurfaceio-exporter
Export NDJSON from Resurface database

This open-source Java utility exports API calls (stored in [NDJSON format](https://resurface.io/json.html)) stored
in a remote Resurface database to a local NDJSON file.

[![CodeFactor](https://www.codefactor.io/repository/github/resurfaceio/exporter/badge)](https://www.codefactor.io/repository/github/resurfaceio/exporter)
[![Contributing](https://img.shields.io/badge/contributions-welcome-green.svg)](https://github.com/resurfaceio/exporter/blob/v3.7.x/CONTRIBUTING.md)
[![License](https://img.shields.io/github/license/resurfaceio/exporter)](https://github.com/resurfaceio/exporter/blob/v3.7.x/LICENSE)
[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=flat-square)](https://cloudsmith.io/~resurfaceio/repos/public/packages/)

## Usage

Download executable jar:
```
wget https://dl.cloudsmith.io/public/resurfaceio/public/maven/io/resurface/resurfaceio-exporter/3.7.1/resurfaceio-exporter-3.7.1.jar
```

Exporting from a remote database: (first 1000 rows)
```
java -DFILE=target/test-$(date +%F).ndjson.gz -DHOST=neptune -DPORT=443 -DLIMIT_MESSAGES=1000 -DMAX_CALL_AGE_IN_DAYS=14 -DUSER=rob -DPASSWORD=**** -Xmx512M -jar resurfaceio-exporter-3.7.1.jar
```

⚠️ This utility creates files in [.ndjson.gz](https://github.com/resurfaceio/ndjson) format exclusively.

## Parameters

```
FILE: local .ndjson.gz file to create
HOST: machine name for remote database
PORT: network port for remote database (80 or 443 for Kubernetes, 7701 for Docker)
LIMIT_MESSAGES: default is '0' (unlimited), quit after this many messages
USER: Trino user name (required)
PASSWORD: Trino user password (required)
URL: override HOST and PORT with custom URL for remote database
```

## Dependencies

* Java 17
* Trino 470
* [resurfaceio/ndjson](https://github.com/resurfaceio/ndjson)

## Installing with Maven

⚠️ We publish our official binaries on [CloudSmith](https://cloudsmith.io/~resurfaceio/repos/public/packages/) rather than Maven Central,
because CloudSmith is awesome and **free** for open-source projects.

If you want to call this utility from your own Java application, add these sections to `pom.xml` to install:

```xml
<dependency>
    <groupId>io.resurface</groupId>
    <artifactId>resurfaceio-exporter</artifactId>
    <version>3.7.1</version>
</dependency>
```

```xml
<repositories>
    <repository>
        <id>resurfaceio-public</id>
        <url>https://dl.cloudsmith.io/public/resurfaceio/public/maven/</url>
        <releases>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </releases>
    </repository>
</repositories>
```

---
<small>&copy; 2016-2025 <a href="https://resurface.io">Graylog, Inc.</a></small>
