# RenJava SDK
Tool used to start development and deployment of RenJava projects.

## To use
Place RenJavaSDK-1.0-SNAPSHOT.jar inside of the root directory of your RenJava project.
```
Hero-Adventure
  src
  target
  pom.xml
  RenJavaSDK-1.0-SNAPSHOT.jar
```
Then execute via shell or terminal command. `java -jar RenJavaSDK-1.0-SNAPSHOT.jar -- color blue`.

There are a few command lines you can use to further customize your experience.
* --noclean - This will not remove the downloaded JDK once it's done downloading.
* --noconsole - Replaces the `java` command to use `javaw` instead.
* --color [color] - Set the default color.

## Deployment
When the jar file is executed it will create three distributables (Windows, MacOS, Linux).
More instruction coming soon when the RenJava Framework enters public testing.