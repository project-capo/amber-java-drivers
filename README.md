amber-java-drivers
=================

[![Build Status](https://travis-ci.org/project-capo/amber-java-drivers.svg?branch=master)](https://travis-ci.org/project-capo/amber-java-drivers)

[![Coverity Scan Build Status](https://scan.coverity.com/projects/4014/badge.svg?style=flat)](https://scan.coverity.com/projects/4014)

Robot devices drivers for Amber platform in Java.

Requirements
------------

* `jdk7` with `maven`
* `protobuf` and `protoc` from `protobuf-compiler`

How to deploy
-------------

* Clone this project.
* Execute `protoc.sh` script to generate classes for Protobuf.
* `mvn install` inside project.
* Import project to your favorite IDE.

If you want to use packages, run `mvn install` ar `mvn package` inside project.

If project cannot be build in IDE due to import errors, check if `target/generated-sources/java` is selected as *source* in every module (if exists).

How to use (maven)
------------------

Simply. Add following lines to your projects `pom.xml`:

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

Next, add following selected dependencies:

    <dependencies>
        <groupId>com.github.project-capo</groupId>
        <artifactId>amber-java-drivers</artifactId>
        <version>0.7</version>
    </dependencies>

How to contribute
-----------------

Clone this repo and setup your enviroment. Next, change what you want and make pull request.
