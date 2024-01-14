#!/usr/bin/env bash
mvn clean compile install
mvn -pl peer exec:java -Dexec.mainClass="com.gbeldilmi.lead_example.App"
