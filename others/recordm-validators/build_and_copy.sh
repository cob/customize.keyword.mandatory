#!/bin/bash

set -e

mvn clean package
cp target/cob-customize-mandatoryif-*-SNAPSHOT.jar ../../recordm/bundles/
