# JavaNabuAdapter
Java 5 version of https://github.com/nabunetwork/Adaptor

WARNING: Java 5 is ancient, but so is Windows '98, which is the target OS for running this software version.  Proceed at your own risk!!

# Deficiencies 
* RetroNet File Store Extension mode is currently unimplemented (not ported from Java 8)
* NHACP Extension could use additional testing, especially for modes not executed by ISHKUR CP/M

# Building
* Set your JAVA_HOME to a Java 8 JDK (later JDKs won't build Java 5 classes)
* mvn install 


# Running
After building, just run your built JAR, passing in your COM port (or tty port) and URL for your online server, e.g.:
* Real Nabu (Windows): `java -jar target\JavaNabuAdapter.jar -Mode Serial -Port COM6 -path https://nabu.lenderman.com/cycle`
* Real Nabu (Linux): `java -jar target/JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -path https://nabu.lenderman.com/cycle`
* Emulator: `java -jar target\JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -path https://nabu.lenderman.com/cycle`

NOTE: If you decide to move the JAR file to another directory, be sure to bring the JavaNabuAdapter_lib folder along for the ride!

You can also run in headless mode where you can choose which cycles to use on the NABU, courtesy of nabunetwork.com:
* Real Nabu (Windows): `java -jar target\JavaNabuAdapter.jar -Mode Serial -Port COM6 -path headless`
* Real Nabu (Linux): `java -jar target/JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -path headless`
* Emulator: `java -jar target\JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -path headless`