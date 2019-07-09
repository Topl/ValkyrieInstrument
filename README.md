# ValkyrieInstrument

##Build process (Using Graal version > 19.0.2 as project SDK)
- Download maven command line tools
- Clone the repository
- Run mvn clean to remove all target/ folders and files
- Run mvn compile to compile all source classes and generate correct annotation classes
- Run mvn test-compile to compile test classes
- Run mvn assembly:single to package project with dependencies into jar

Drop jar into Graal's Contents/Home/jre/lib/boot folder (add to boot classpath)
This enables instrument registration and access by the option arguments when building a context, and service class loading and initialization within the instrument itself.
