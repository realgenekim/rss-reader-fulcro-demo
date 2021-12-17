#!/bin/bash

# https://cloud.google.com/sdk/docs/downloads-apt-get

# cat /etc/os-release
export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list

# https://stackoverflow.com/questions/49582490/gpg-error-http-packages-cloud-google-com-apt-expkeysig-3746c208a7317b0f
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -

sudo apt-get update && sudo apt-get install --allow-unauthenticated -y google-cloud-sdk
