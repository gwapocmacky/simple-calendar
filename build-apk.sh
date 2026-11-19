#!/data/user/0/com.agy/files/usr/bin/bash
set -eu

export PATH="/data/user/0/com.agy/files/usr/lib/jvm/java-21-openjdk/bin:/data/user/0/com.agy/files/usr/bin:$PATH"
export JAVA_HOME="/data/user/0/com.agy/files/usr/lib/jvm/java-21-openjdk"

FRAMEWORK_RES="/data/user/0/com.agy/files/.gemini/antigravity-app/scratch/elegant-calendar/framework-res.apk"
ANDROID_JAR="/data/user/0/com.agy/files/usr/share/java/android.jar"
KEYSTORE="$HOME/.android/debug.keystore"
KS_PASS="android"
KS_ALIAS="androiddebugkey"

cd /data/user/0/com.agy/files/.gemini/antigravity-app/scratch/elegant-calendar-v2

PACKAGE="com.elegantcal.app"
echo "=== Building $PACKAGE ==="

rm -rf build
mkdir -p build/classes build/dex build/gen

echo "1. Compiling resources..."
aapt2 compile --dir res -o build/res.zip

echo "2. Linking resources..."
aapt2 link -I "$FRAMEWORK_RES" --manifest AndroidManifest.xml \
    --java build/gen --min-sdk-version 21 --target-sdk-version 33 \
    --version-code 2 --version-name "2.0" \
    -o build/base.apk build/res.zip

echo "3. Compiling Java..."
find src build/gen -name '*.java' > build/sources.txt
javac -source 8 -target 8 -classpath "$ANDROID_JAR" -Xlint:-options \
    -d build/classes @build/sources.txt

echo "4. Dexing classes..."
jar cf build/classes.jar -C build/classes .
d8 --min-api 21 --lib "$ANDROID_JAR" --output build/dex build/classes.jar

echo "5. Assembling APK..."
cp build/base.apk build/unsigned.apk
for dex in build/dex/classes*.dex; do
    jar --update --file build/unsigned.apk -C build/dex "$(basename "$dex")"
done

echo "6. Signing APK..."
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$(dirname "$KEYSTORE")"
    keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KS_PASS" \
        -alias "$KS_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
fi

OUT="build/$PACKAGE.apk"
apksigner sign --ks "$KEYSTORE" --ks-key-alias "$KS_ALIAS" \
    --ks-pass pass:"$KS_PASS" --key-pass pass:"$KS_PASS" --out "$OUT" build/unsigned.apk

apksigner verify "$OUT"

cp "$OUT" /storage/emulated/0/ElegantCalendar.apk
echo "Copied APK to /storage/emulated/0/ElegantCalendar.apk"
echo "✅ SUCCESS: $OUT"
