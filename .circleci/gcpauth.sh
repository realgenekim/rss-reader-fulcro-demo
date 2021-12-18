#!/bin/bash

# taken from booktracker2/makefile
# base64
# echo $(GCLOUD_SERVICE_KEY)
echo $GCLOUD_SERVICE_KEY | base64 --decode > /tmp/client-secret.json
# gcloud --quiet components update
gcloud auth activate-service-account --key-file /tmp/client-secret.json
# doesn't work, because we installed via yum
# gcloud components update kubectl
gcloud config set project booktracker-1208
# echo $(CREDSPY) | base64 --decode > creds.py

# gcloud components install kubectl

gcloud config set compute/zone us-west1-a
#gcloud config set container/cluster prod-cluster
#gcloud container clusters get-credentials prod-cluster
#kubectl get pods