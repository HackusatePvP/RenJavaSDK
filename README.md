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
Then execute via shell or terminal command. `java -jar RenJavaSDK-1.0-SNAPSHOT.jar --color blue`.

There are a few command lines you can use to further customize your experience.
* --noclean - This will not remove the downloaded JDK once it's done downloading.
* --noconsole - Replaces the `java` command to use `javaw` instead.
* --color [color] - Set the default color.

# Colors
The following is the current supported colors.
* aqua
* blue
* light_blue
* orange

## Managing Assets
Managing and organizing assets is very important for RenJava. To keep things clean for debugging purposes all assets must be placed in the appropriate folders.
RenJava will not load images inside the 'audio' folder.

For videos place them in the 'media' folder. If you do not have a media folder create it inside the 'game' directory.

## Deployment
When the jar file is executed it will create three distributables (Windows, MacOS, Linux).
More instruction coming soon when the RenJava Framework enters public testing. I made a [video](https://youtu.be/gyZ_r8wtMEI) demonstrating this application.