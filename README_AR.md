# سبّح - عدّاد الأذكار الرقمي

<div align="center">

![شعار سبّح](./app/src/main/res/drawable/app_icon.png)

**عدّاد أندرويد جميل لأذكارك وصلواتك.**

*مجاني وبدون إعلانات أو متتبعات. صُنع لوجه الله.*

[![GitHub](https://img.shields.io/badge/GitHub-SlaveOfGod1-blue?logo=github)](https://github.com/SlaveOfGod1/Sabbeh)
[![License](https://img.shields.io/badge/License-Personal%20Use%20Only-red)](./LICENSE.md)

[English](./README.md)

</div>

---

## ✨ المميزات

### 🔢 العدّاد الرقمي
- شاشة عرض كبيرة وسهلة القراءة
- تتبع الجولات تلقائياً عند بلوغ الهدف
- تصفير العدّاد بلمسة واحدة
- **الاهتزاز**: اهتزاز اختياري عند إكمال الجولة (يُفعّل من الإعدادات)

### 📿 جلسات الذكر
- **أذكار مخصصة**: أضف أذكارك الخاصة بعناوين وأهداف مخصصة
- **تعديل وحذف**: اضغط مطولاً على أي ذكر لتعديله أو حذفه
- حفظ التقدم لكل جلسة ذكر
- **الانتقال التلقائي**: الانتقال للذكر التالي تلقائياً بعد إكمال الجولة (اختياري)

### 🎨 المظاهر والتخصيص
- **4 مظاهر جاهزة**: فيروزي، بنفسجي، أزرق، وردي
- **لون مخصص**: اختر أي لون يعجبك ويتذكره التطبيق
- **الوضع الليلي**: مظهر داكن مريح للاستخدام ليلاً
- **أيقونة التطبيق**: بدّل أيقونة المشغّل (الإعدادات > أيقونة التطبيق)

### 🌍 دعم متعدد اللغات
مترجم بالكامل إلى 7 لغات:
- English
- العربية
- Türkçe
- 中文
- Melayu
- اردو
- 日本語

### 💾 إدارة البيانات
- **تصدير البيانات**: احفظ بياناتك كملف JSON
- **استيراد البيانات**: استعد بياناتك من نسخة احتياطية
- **إعادة ضبط البيانات**: امسح كل البيانات وابدأ من جديد

### 🛡️ الخصوصية أولاً
- ✅ بدون إعلانات
- ✅ بدون متتبعات
- ✅ بدون جمع بيانات
- ✅ يعمل بدون إنترنت

---

## 🚀 البدء

### المتطلبات
- [JDK](https://adoptium.net/) إصدار 17 أو أحدث
- حزمة Android SDK مع: `cmdline-tools` و `platforms;android-34` و `build-tools;34.0.0` و `platform-tools`
- لا حاجة لـ Android Studio أو Gradle — بدون أي مكتبات خارجية

### خطوات البناء

1. **ثبّت حزم SDK (مرة واحدة)**
   ```bat
   sdkmanager "platforms;android-34" "build-tools;34.0.0"
   ```

2. **حدّد المسارات واجمع الموارد**
   ```bat
   set SDK=<path-to-android-sdk>
   set JDK=<path-to-jdk>
   set PROJ=%CD%
   set BT=%SDK%\build-tools\34.0.0
   set AJAR=%SDK%\platforms\android-34\android.jar
   "%BT%\aapt2.exe" compile --dir "%PROJ%\app\src\main\res" -o "%PROJ%\build\flats"
   "%BT%\aapt2.exe" link -o "%PROJ%\build\apk\app-unaligned.apk" -I "%AJAR%" --manifest "%PROJ%\app\src\main\AndroidManifest.xml" --java "%PROJ%\build\gen" --auto-add-overlay --min-sdk-version 24 --target-sdk-version 34 --version-code 3 --version-name 1.0.2 -R "%PROJ%\build\flats\values_colors.arsc.flat" -R "%PROJ%\build\flats\values_strings.arsc.flat" -R "%PROJ%\build\flats\values_styles.arsc.flat"
   ```

3. **الترجمة والتوقيع**
   ```bat
   "%JDK%\bin\javac.exe" --release 8 -classpath "%AJAR%" -d "%PROJ%\build\obj" <your-sources>
   "%JDK%\bin\jar.exe" cf "%PROJ%\build\classes.jar" -C "%PROJ%\build\obj" .
   "%JDK%\bin\java.exe" -classpath "%SDK%\cmdline-tools\latest\lib\d8-classpath.jar" com.android.tools.r8.D8 --lib "%AJAR%" --min-api 24 --output "%PROJ%\build\dex" "%PROJ%\build\classes.jar"
   "%BT%\zipalign.exe" -f 4 "%PROJ%\build\apk\app-unaligned.apk" "%PROJ%\build\apk\app-aligned.apk"
   "%JDK%\bin\java.exe" -jar "%BT%\lib\apksigner.jar" sign --ks <your.keystore> --ks-pass pass:<password> --key-pass pass:<password> --out "%PROJ%\build\Sabbeh.apk" "%PROJ%\build\apk\app-aligned.apk"
   ```

4. **التشغيل على جهازك** (مع تفعيل تصحيح USB)
   ```bat
   "%SDK%\platform-tools\adb.exe" install -r "%PROJ%\build\Sabbeh.apk"
   ```

---

## 🛠️ التقنيات المستخدمة

- **Java** - تطبيق أندرويد أصيل بدون أطر عمل
- **Android SDK فقط** - ‏aapt2 و d8 و apksigner و adb مباشرة
- **SharedPreferences** - حفظ البيانات محلياً
- **رسم Canvas** - خلفيات المظاهر أثناء التشغيل

---

## 🤝 المساهمة

المساهمات مرحب بها! يمكنك:
- الإبلاغ عن الأخطاء عبر [GitHub Issues](https://github.com/SlaveOfGod1/Sabbeh/issues)
- إرسال طلبات السحب
- اقتراح مميزات جديدة

---

## 📄 الرخصة

هذا المشروع **مجاني للاستخدام الشخصي فقط**.

> ⚠️ **تنبيه مهم**
>
> - ❌ **الاستخدام التجاري ممنوع منعاً باتاً**
> - ❌ **بيع التطبيق أو إعادة توزيعه للربح غير مسموح**
> - ❌ **رفعه على المتاجر لأغراض تجارية ممنوع**
> - ✅ الاستخدام الشخصي فقط
>
> **مخالفة هذه الشروط ستؤدي إلى إشعار إزالة DMCA.**

---

## 🤲 دعاء

تقبل الله منا هذا العمل وجعله نافعاً للأمة.

*اللهم تقبل منا إنك أنت السميع العليم*

---

<div align="center">

**صُنع لوجه الله**

</div>
