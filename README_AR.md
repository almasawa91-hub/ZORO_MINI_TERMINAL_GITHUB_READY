# ZORO MINI TERMINAL — Android source

نسخة أولية قابلة للبناء لتطبيق طرفية تشخيصية لأندرويد، لا تحتاج إلى Root.

## المتطلبات
- AndroidIDE أو بيئة Android Gradle متوافقة على الهاتف.
- اتصال إنترنت أول مرة لتنزيل Gradle وAndroid Gradle Plugin وSDK المطلوبة.
- JDK 17 وAndroid SDK Platform 35.

## البناء على الهاتف
1. فك ضغط `ZORO_MINI_TERMINAL_SOURCE.zip`.
2. افتح مجلد المشروع في AndroidIDE.
3. اسمح للتطبيق بتنزيل متطلبات البناء إذا طلب ذلك.
4. نفّذ Sync/Build ثم `assembleDebug`.
5. ستجد APK التجريبي عادةً في `app/build/outputs/apk/debug/app-debug.apk`.

## الأوامر
`help`, `clear`, `sysinfo`, `ip`, `dns HOST`, `ping HOST`, `history`, `hash TEXT`, `about`.

## ملاحظات مهمة
- هذا مشروع مصدر، وليس APK مبنيًا أو مختبرًا في هذه البيئة.
- `hash` يحسب SHA-256 للنص المُدخل، وليس لملف. اختيار الملفات وحساب بصمتها ميزة لاحقة.
- `ping` يستخدم `InetAddress.isReachable` وقد لا يعمل مع بعض الشبكات أو الأجهزة؛ النتيجة ليست بديلًا عن ICMP ping الكامل.
- لا تنفذ هذه النسخة أوامر Linux عشوائية، ولا تملك صلاحيات Root أو وصولًا شاملًا لملفات الجهاز.
- لا تمنح صلاحيات غير مطلوبة. أذونات الشبكة مذكورة في manifest.

## البناء التلقائي عبر GitHub Actions

أصبح المشروع يحتوي على سير عمل في `.github/workflows/android.yml`.
بعد رفع الملفات إلى فرع `main` أو `master`، سيبدأ البناء تلقائيًا. ويمكن تشغيله يدويًا من تبويب **Actions** ثم **Build Android APK** ثم **Run workflow**.

لتنزيل التطبيق بعد نجاح البناء:
1. افتح المستودع على GitHub.
2. ادخل إلى **Actions** ثم افتح أحدث تشغيل ناجح.
3. من قسم **Artifacts** نزّل `ZORO-MINI-TERMINAL-debug-apk`.
4. فك ضغط الملف، ثم ثبّت `app-debug.apk` على هاتف Android.

ملاحظة: الناتج APK تجريبي موقّع بمفتاح debug، وليس إصدارًا نهائيًا للنشر في المتجر. لم يتم تأكيد نجاح البناء حتى يُشغّل سير العمل فعليًا على GitHub.
