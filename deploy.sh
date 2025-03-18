#!/bin/bash

mvn clean package
mv target/main-jar-with-dependencies.jar target/resurface-exporter.jar
cloudsmith push raw resurfaceio/release target/resurface-exporter.jar --version $1
