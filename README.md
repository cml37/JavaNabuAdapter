# JavaNabuAdapter
Java 5 version of https://github.com/nabunetwork/Adaptor

WARNING: Java 5 is ancient, but so is Windows '98, which is the target OS for running this software version.  Proceed at your own risk!!

# Deficiencies 
* RetroNet File Store Extension mode is currently unimplemented (not ported from Java 8)
* NHACP Extension could use additional testing, especially for modes not executed by ISHKUR CP/M
* Better build process (i.e. maven, or perhaps even gradle) that pulls in libraries automatically
* Better deployment process (creating an executable JAR that extracts all libraries into it results in SSL handshake failures)

# Building
Use Eclipse to build against a Java 5 JDK.  

Then export a runnable JAR file called `JavaNabuAdapter.jar`. For library handling, copy required libraries into a sub-folder next to the generated JAR

Prior to doing the above, you will need to find the following libraries and place them in the lib directory:
* activation-1.1.1.jar (`javax.activation:activation:1.1.1`)
* bcprov-jdk15to18-173.jar (`org.bouncycastle:bcprov-jdk15to18:1.73`)
* bctls-jdk15to18-173.jar (`org.bouncycastle:bctls-jdk15to18:1.73`)
* bcutil-jdk15to18-173.jar (`org.bouncycastle:bcutil-jdk15to18:1.73`)
* commons-io-1.3.2.jar (`commons-io:commons-io:1.3.2`)
* javax.xml.stream-3.0.1.jar (http://www.java2s.com/Code/Jar/j/Downloadjavaxxmlstream301sourcesjar.htm)
* jaxb-api-2.2.jar (`javax.xml.bind:jaxb-api:2.2`)
* jaxb-impl-2.2.jar (`com.sun.xml.bind:jaxb-impl:2.2`)
* jssc-2.7.0.jar (https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/java-simple-serial-connector/jSSC-2.7.0-Release.zip)
* log4j-1.2.17.jar  (`log4j:log4j:1.2.17`)

# Running
After building, just run your built JAR, passing in your COM port (or tty port) and URL for your online server, e.g.:
* Real Nabu (Windows): `java -jar JavaNabuAdapter.jar -Mode Serial -Port COM6 -path https://nabu.lenderman.com/cycle`
* Real Nabu (Linux): `java -jar JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -path https://nabu.lenderman.com/cycle`
* Emulator: `java -jar JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -path https://nabu.lenderman.com/cycle`

NOTE: If you decide to move the JAR file to another directory, be sure to bring the JavaNabuAdapter_lib folder along for the ride!

You can also run in headless mode where you can choose which cycles to use on the NABU, courtesy of nabunetwork.com:
* Real Nabu (Windows): `java -jar JavaNabuAdapter.jar -Mode Serial -Port COM6 -path headless`
* Real Nabu (Linux): `java -jar JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -path headless`
* Emulator: `java -jar JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -path headless`