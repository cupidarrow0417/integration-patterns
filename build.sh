#!/bin/sh
echo "Building all procjects.."
cd composer
mvn clean verify -DskipTests
cd ..
cd header-footer
mvn clean verify -DskipTests
cd ..
cd product-detail-page
mvn clean verify -DskipTests
cd ..
