#!/bin/sh
echo "Starting header footer service."
nohup java -jar ./header-footer/target/header-footer-0.0.1-SNAPSHOT.jar &> header-footer.out&
echo "Starting product detail page."
nohup java -jar ./product-detail-page/target/product-detail-page-0.0.1-SNAPSHOT.jar  &> product-detail.out&
echo "Starting composer."
nohup java -jar ./composer/target/composer.jar &> composer.out&

echo "Tailing all log files."
tail -f header-footer.out composer.out product-detail.out
