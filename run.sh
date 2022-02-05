#!/bin/bash
set -eou  pipefail
java -cp "./lib/jade.jar:./lib/commons-codec-1.15.jar:./classes" jade.Boot $@
