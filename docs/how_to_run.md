# ARAAFTOR - How to start

To run the extension, you need a mobile device or emulator with the ATAK application installed in version 4.6.0.5 (the ATAK.apk file is located in the `docs` folder).

**Android Studio** is recommended for running the extension.

To ensure smooth operation, it is recommended to place the repository within the prepared plugin structure available on [GitHub](https://github.com/deptofdefense/AndroidTacticalAssaultKit-CIV/releases/tag/4.5.1.13), inside the *plugintemplate* folder.

You are required to generate application signing keys, which are necessary for Android. In the extension's root directory, run the following commands:

```
keytool -genkeypair -dname "CN=Android Debug,O=Android,C=US" -validity 9999 -keystore debug.keystore -alias androiddebugkey -keypass android -storepass android
keytool -genkeypair -dname "CN=Android Release,O=Android,C=US" -validity 9999 -keystore release.keystore -alias androidreleasekey -keypass android -storepass android
```

Then, add the paths to the appropriate signing keys in the *local.properties* file.
​