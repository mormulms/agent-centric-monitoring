#!/bin/sh

#
# This script creates the mapper
#

# create artifacts using maven
mvn clean package -DskipTests

# create  folder
rm -fr mapper
mkdir -p mapper/plugins
mkdir -p mapper/agent-configs

# copy artifacts to mapper folder
cp mapper-core/target/mapper-core.jar mapper
cp mapper-core/*.toml mapper
cp mapper-plugins/*/target/*.jar mapper/plugins
cp mapper-plugins/*.txt mapper/plugins
