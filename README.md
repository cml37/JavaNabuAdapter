# JavaNabuAdapter
Java version of https://github.com/hupshall/NabuAdaptor

# Building
Assuming you have Gradle and Java 8 or higher installed, it's as easy as `gradlew jar`

# Running
After building, just run your built JAR, passing in your COM port (or tty port) and URL for your online server, e.g.:
* Real Nabu (Windows): `java -jar build\libs\JavaNabuAdapter.jar -Mode Serial -Port COM6 -source https://nabu.lenderman.com/cycle`
* Real Nabu (Linux): `java -jar build\libs\JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -source https://nabu.lenderman.com/cycle`
* Emulator: `java -jar build\libs\JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -source https://nabu.lenderman.com/cycle`