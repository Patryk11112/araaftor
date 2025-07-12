# ARAAFTOR - Using ATAK for the first time

Application documentation can be found on the [official website](https://www.civtak.org/documentation/).

**Instructions on how to run the application extension**

1. Download the prepared plugin schema *atak-civ-sdk-4.5.1.13.zip* from the official [GitHub](https://github.com/deptofdefense/AndroidTacticalAssaultKit-CIV/releases/tag/4.5.1.13) repository.

2. Open Android Studio and click the *Open* button. Navigate to the location where you unzipped the plugin folder and expand the *plugin-examples* folder.

3. We select the extension we are interested in, which should have a small Android icon in front of the root folder name to open the plugin as a project.

4. Gradle build will fail the first time you run it and you will get the following error.
```
Caused by: org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed
```

5. To fix the error, open the <PLUGIN_NAME>/app/build.gradle file, then find the following line of code: 
```
def getValueFromPropertiesFile = { propFile, key ->​
```
and change it as follows:
```
exp.getValueFromPropertiesFile = { propFile, key ->​
```
6. We need to build the application signing keys required by Android. So, we launch a terminal in the root folder of our plugin, e.g., "...\atak-civ\plugin-examples\ <PLUGIN_NAME>" and execute two commands:
```
keytool -genkeypair -dname "CN=Android Debug,O=Android,C=US" -validity 9999 -keystore debug.keystore -alias androiddebugkey -keypass android -storepass android
keytool -genkeypair -dname "CN=Android Release,O=Android,C=US" -validity 9999 -keystore release.keystore -alias androidreleasekey -keypass android -storepass android
```
7. Next, you need to place the following lines in the <PLUGIN_NAME>/local.properties file, as shown in the code below. The sdk.dir path should already be filled in by the IDE with the default Android SDK file path.
```
sdk.dir=C\:\\...\\AppData\\Local\\Android\\Sdk​
takDebugKeyFile=C\:\\<your_path>\\atak-civ\\plugin-examples\\<PLUGIN_NAME>\\debug.keystore​
takDebugKeyFilePassword=android​
takDebugKeyAlias=androiddebugkey​
takDebugKeyPassword=android​
​
takReleaseKeyFile=C\:\\<your_path>\\atak-civ\\plugin-examples\\<PLUGIN_NAME>\\release.keystore​
takReleaseKeyFilePassword=android​
takReleaseKeyAlias=androidreleasekey​
takReleaseKeyPassword=android
```
8. ​In the upper left corner, go to File > Settings > Build, Execution, Deployment > Build Tools > Gradle. Make sure your Gradle JDK is Java version 11. If not, download Java 11 to use it. If your Java version is too new or too old, the build will fail.

9. After all these steps, go to the top left corner and click File and click Sync Project with Gradle Files. Gradle should compile without errors.

10. Next, open the Run Configurations drop-down menu and select "Edit Configurations". Set Launch Options > Launch to "Nothing" and press Apply.

11. In the top right corner, click the Build Variants tab and change the Active Build Variants from milDebug to civDebug.

12. Next, if you've installed the ATAK app (on a phone connected via USB with the "USB for file transfer" option enabled, or on an emulator in Android Studio), simply click Run, and the app will ask if you want to install the plugin.