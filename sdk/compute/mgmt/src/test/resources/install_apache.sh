#!/bin/bash

sudo apt-get install -f

sudo apt-get update

# install apache
sudo apt-get -y install apache2

# restart Apache
sudo apachectl restart