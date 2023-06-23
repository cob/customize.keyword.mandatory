#!/bin/bash

set -e

mvn clean package
cp target/cob-customize-mandatory-*-SNAPSHOT.jar ../../recordm/bundles/
