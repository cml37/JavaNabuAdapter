# JavaNabuAdapter
Java version of https://github.com/hupshall/NabuAdaptor

# Building
Assuming you have Gradle and Java 11 installed, it's as easy as `gradlew jar`

# Running
After building, just run your built JAR, passing in your COM port (or tty port) and URL for your online server, e.g.:
* Real Nabu (Windows): `java -jar build\libs\JavaNabuAdapter.jar -Mode Serial -Port COM6 -url https://nabu.lenderman.com/cycle/`
* Real Nabu (Linux): `java -jar build\libs\JavaNabuAdapter.jar -Mode Serial -Port ttyUSB0 -url https://nabu.lenderman.com/cycle/`
* Emulator: `java -jar build\libs\JavaNabuAdapter.jar -Mode TCPIP -Port 5816 -url https://nabu.lenderman.com/cycle/`