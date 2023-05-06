# JavaNabuAdapter
Java version of https://github.com/nabunetwork/Adaptor

# Deficiencies 
* RetroNet File Store Extension mode is completely untested
* NHACP Extension could use additional testing, especially for modes not executed by ISHKUR CP/M

# Building
Assuming you have Gradle and Java 8 or higher installed, it's as easy as `gradlew jar`

# Running
After building, just run your built JAR, passing in your COM port (or tty port) and URL for your online server, e.g.:
* Real Nabu (Windows): `java -jar build\libs\JavaNabuAdapter.jar -Mode Serial -Port COM6 -path https://nabu.lenderman.com/cycle`
* Real Nabu (Linux): `java -jar build/libs/JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -path https://nabu.lenderman.com/cycle`
* Emulator: `java -jar build\libs\JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -path https://nabu.lenderman.com/cycle`

You can also run in headless mode where you can choose which cycles to use on the NABU, courtesy of nabunetwork.com:
* Real Nabu (Windows): `java -jar build\libs\JavaNabuAdapter.jar -Mode Serial -Port COM6 -path headless`
* Real Nabu (Linux): `java -jar build/libs/JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -path headless`
* Emulator: `java -jar build\libs\JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -path headless`
