# JavaNabuAdapter
Java version of https://github.com/hupshall/NabuAdaptor

# Building
Assuming you have Gradle and Java 15 installed, it's as easy as `gradlew jar`

# Running
After building, just run your built JAR, passing in your COM port and URL for your online server:
* `java -jar build\libs\JavaNabuAdapter.jar -Mode Serial -Port COM6 -url https://nabu.lenderman.com/cycle/`