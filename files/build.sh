#!/bin/bash

# cleanup previous build files/output

rm -rf $TALEND_WORKSPACE/.build-workspace
rm -rf $TALEND_WORKSPACE/.talend-build
mkdir $TALEND_WORKSPACE/.talend-build

# build specified job

cd /opt/TOS_DI-20181026_1147-V7.1.1

./TOS_DI-linux-gtk-x86_64 \
  -nosplash \
  --launcher.suppressErrors \
  -data $TALEND_WORKSPACE/.build-workspace \
  --clean_component_cache \
  -application au.org.emii.talend.codegen.Generator \
  -projectDir $TALEND_WORKSPACE/$1 \
  -jobName $2 \
  -targetDir $TALEND_WORKSPACE/.talend-build \
  -componentDir /opt/talend-components