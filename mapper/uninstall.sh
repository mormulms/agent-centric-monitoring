#!/bin/sh

#
# This script deletes the mapper
#

# delete artifacts using maven
mvn clean

# create  folder
rm -fr mapper