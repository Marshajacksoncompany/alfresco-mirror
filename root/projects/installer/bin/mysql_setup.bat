@echo off
rem ---------------------------------------
rem MySQL create DB command
rem ---------------------------------------

echo Creating Alfresco database and user...
mysql -u root -p < mysql_setup.sql

echo Database prepared.

