# ZORO MINI TERMINAL PRO 2.5

تطبيق Android خفيف بدون Root، مع بناء APK تلقائي عبر GitHub Actions.

## الميزات
- أوامر طرفية داخل التطبيق: `help`, `clear`, `sysinfo`, `ip`, `dns HOST`, `ping HOST`, `hash TEXT`, `password [LENGTH]`, `strength TEXT`, `ls [PATH]`, `read PATH`, `history`, `export`, `about`.
- DNS وIP وPing: يستخدم DNS وواجهات Java القياسية. `ping` يعتمد `InetAddress.isReachable` وقد لا يعكس ICMP الحقيقي على كل الشبكات.
- SHA-256 للنص المدخل.
- توليد كلمة مرور عشوائية محليًا باستخدام SecureRandom.
- تقييم محلي تقريبي لقوة كلمة المرور؛ لا يتم إرسالها إلى الشبكة.
- إدارة الملفات داخل مجلد التطبيق الخاص، مع إمكانية اختيار ملف نصي من منتقي ملفات أندرويد.
- سجل أوامر، نسخ النتائج، تصدير تقرير نصي عبر مشاركة أندرويد.
- واجهة داكنة خضراء.

## بناء APK
ارفع المشروع إلى GitHub. كل Push إلى `main` أو `master` يشغّل GitHub Actions، كما يمكن تشغيله يدويًا من Actions عبر `workflow_dispatch`.
بعد نجاح البناء، نزّل artifact المسمى `ZORO-MINI-TERMINAL-PRO-2.5-APK`. يحتوي على `app-debug.apk`.

## رفع المشروع من Termux
فك الضغط، ثم ادخل إلى المجلد الذي يحتوي `settings.gradle`:
```bash
pkg update -y
pkg install git gh unzip -y
cd ~/storage/downloads
unzip ZORO_MINI_TERMINAL_PRO_2_5.zip -d ~/ZORO_MINI_TERMINAL_PRO_2_5
cd ~/ZORO_MINI_TERMINAL_PRO_2_5
git init
git add .
git commit -m "ZORO MINI TERMINAL PRO 2.5"
git branch -M main
gh auth login
gh repo create ZORO_MINI_TERMINAL_PRO_2_5 --public --source=. --remote=origin --push
```
إذا كان ZIP قد فكّ داخل مجلد فرعي، استخدم `ls` وتأكد من وجود `settings.gradle` قبل `git init`.

## حدود الأمان والخصوصية
- لا يتطلب Root.
- لا يملك صلاحيات قراءة شاملة للملفات؛ يقرأ ملفات من مساحة التطبيق الخاصة، أو ملفًا نصيًا يختاره المستخدم عبر منتقي أندرويد.
- أدوات الشبكة مخصصة للتشخيص الأساسي. لا تتضمن فحص منافذ أو استغلال أو تجاوز حماية.
