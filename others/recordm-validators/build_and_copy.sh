#!/bin/bash

set -e

mvn clean package
cp target/cob-customize-mandatoryif-validators-*-SNAPSHOT.jar ../../recordm/bundles/
