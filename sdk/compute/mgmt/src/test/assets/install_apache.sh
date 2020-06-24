#!/bin/bash

sudo apt-get update

# install apache
sudo apt-get -y install apache2

# restart Apache
sudo service apache2 restart