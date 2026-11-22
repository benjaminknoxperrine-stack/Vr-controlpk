Quest Control - build & use

BUILD
1. Open this folder in Android Studio (Hedgehog or newer, JDK 17). Let it sync.
   If a dependency version isn't found, check Maven Central for the latest
   io.github.muntashirakon:libadb-android and update app/build.gradle.kts.
2. Build > Build APK(s). Find it in app/build/outputs/apk/debug/.
3. Sideload:  adb install app-debug.apk   (or use SideQuest).

USE (pick ONE connection method)
A) Classic ADB over TCP (most likely to work on Quest)
   - Once per headset boot, with USB plugged in to a computer: adb tcpip 5555
   - In the app: Host 127.0.0.1, port 5555, tap Connect.
   - Accept the "Allow USB debugging?" prompt in the headset (tick Always allow).
B) Wireless debugging (only if your Horizon OS exposes it)
   - Enter the pairing port + code and tap Pair, then enter the connect port
     and tap Connect.
Open the app from Library > Unknown Sources.

If libadb-android's API differs from AdbManager.kt/MainActivity.kt in your
version (connect/pair/openStream), adjust those few calls to match its README.
