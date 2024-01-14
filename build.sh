#!/usr/bin/env bash
mvn clean compile install
mvn -pl server exec:java -Dexec.mainClass="com.gbeldilmi.lead_example.App" &
mvn -pl client exec:java -Dexec.mainClass="com.gbeldilmi.lead_example.App"

