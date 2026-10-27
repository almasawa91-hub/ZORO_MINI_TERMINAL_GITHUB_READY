package com.zoro.miniterminal;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_TEXT_FILE = 411;
    private final int bg = 0xFF050807, panel = 0xFF101815, green = 0xFF00FF88, white = 0xFFE5FFF1, muted = 0xFF8BB8A1;
    private LinearLayout root, outputBox;
    private EditText commandInput;
    private ScrollView scroll;
    private final List<String> history = new ArrayList<>();
    private final StringBuilder transcript = new StringBuilder();
    private File workDir;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        workDir = new File(getFilesDir(), "workspace");
        if (!workDir.exists()) workDir.mkdirs();
        buildUi();
        printLine("ZORO MINI TERMINAL PRO 2.5", green);
        printLine("Android • بدون Root • اكتب help لعرض الأوامر", muted);
        printLine("مساحة الملفات: " + workDir.getAbsolutePath(), muted);
    }

    private TextView text(String value, int color, float size) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextColor(color); t.setTextSize(size);
        t.setPadding(12, 7, 12, 7); t.setTextIsSelectable(true);
        return t;
    }
    private Button button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setTextColor(green);
        b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel));
        b.setOnClickListener(v -> action.run()); return b;
    }
    private void buildUi() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(12, 8, 12, 8); root.setBackgroundColor(bg);
        TextView title = text("⚡ ZORO  /  MINI TERMINAL PRO", green, 19); title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title);
        TextView subtitle = text("VERSION 2.5  |  LOCAL TOOLS", muted, 11); root.addView(subtitle);
        LinearLayout toolbar = new LinearLayout(this); toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.addView(button("نسخ النتائج", this::copyTranscript), new LinearLayout.LayoutParams(0, -2, 1));
        toolbar.addView(button("تصدير التقرير", this::exportTranscript), new LinearLayout.LayoutParams(0, -2, 1));
        toolbar.addView(button("مسح", () -> { outputBox.removeAllViews(); transcript.setLength(0); printLine("تم مسح الشاشة.", muted); }), new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(toolbar);
        scroll = new ScrollView(this); scroll.setFillViewport(false);
        outputBox = new LinearLayout(this); outputBox.setOrientation(LinearLayout.VERTICAL); outputBox.setPadding(2, 6, 2, 6); scroll.addView(outputBox);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout actions = new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(button("ملف نصي", this::pickTextFile), new LinearLayout.LayoutParams(0, -2, 1));
        actions.addView(button("عرض الملفات", () -> runCommand("ls")), new LinearLayout.LayoutParams(0, -2, 1));
        actions.addView(button("مساعدة", () -> runCommand("help")), new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(actions);
        commandInput = new EditText(this); commandInput.setSingleLine(true); commandInput.setTextColor(white); commandInput.setHintTextColor(muted);
        commandInput.setHint("اكتب أمرًا... مثل dns example.com"); commandInput.setTextSize(14);
        commandInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));
        root.addView(commandInput);
        root.addView(button("تنفيذ الأمر  ▶", () -> {
            String cmd = commandInput.getText().toString().trim();
            if (!cmd.isEmpty()) { commandInput.setText(""); runCommand(cmd); }
            ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(commandInput.getWindowToken(), 0);
        }));
        setContentView(root);
    }
    private void printLine(String line, int color) {
        TextView t = text(line, color, 13); outputBox.addView(t);
        transcript.append(line).append('\n');
        if (scroll != null) scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }
    private void print(String s) { printLine(s, white); }
    private void runCommand(String raw) {
        history.add(raw);
        printLine("zoro> " + raw, green);
        String[] parts = raw.trim().split("\\s+", 2);
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        String arg = parts.length > 1 ? parts[1].trim() : "";
        try {
            switch (cmd) {
                case "help":
                    print("الأوامر المتاحة:");
                    print("help | clear | about | history | sysinfo | ip | dns HOST | ping HOST");
                    print("hash TEXT | password [LENGTH] | strength TEXT | ls [PATH] | read PATH | export");
                    print("الملفات تقرأ من مساحة التطبيق الخاصة، أو اختر ملفًا عبر زر «ملف نصي».");
                    break;
                case "clear":
                    outputBox.removeAllViews(); transcript.setLength(0); printLine("تم مسح الشاشة.", muted); break;
                case "about":
                    print("ZORO MINI TERMINAL PRO 2.5 — أدوات محلية وتشخيص شبكة أساسي."); break;
                case "history":
                    if (history.isEmpty()) print("(لا يوجد سجل)");
                    else for (int i=0;i<history.size();i++) print((i+1)+". "+history.get(i));
                    break;
                case "sysinfo":
                    print("الجهاز: "+Build.MANUFACTURER+" "+Build.MODEL);
                    print("Android: "+Build.VERSION.RELEASE+" (SDK "+Build.VERSION.SDK_INT+")");
                    print("المعمارية: "+Build.SUPPORTED_ABIS[0]); print("مساحة التطبيق: "+workDir.getAbsolutePath()); break;
                case "ip": showIp(); break;
                case "dns":
                    if (arg.isEmpty()) { print("الاستخدام: dns example.com"); break; }
                    for (InetAddress a : InetAddress.getAllByName(arg)) print(arg+" → "+a.getHostAddress());
                    break;
                case "ping":
                    if (arg.isEmpty()) { print("الاستخدام: ping example.com"); break; }
                    print("محاولة وصول واحدة إلى "+arg+" (قد لا تعمل ICMP على كل الشبكات)...");
                    new Thread(() -> {
                        long start=System.currentTimeMillis();
                        try {
                            boolean ok=InetAddress.getByName(arg).isReachable(2500);
                            long ms=System.currentTimeMillis()-start;
                            runOnUiThread(() -> print(ok ? "يمكن الوصول إلى المضيف (تقريبًا "+ms+" ms)." : "لم يتأكد الوصول؛ قد تحجب الشبكة ICMP."));
                        } catch(Exception e) { runOnUiThread(() -> print("خطأ: "+e.getMessage())); }
                    }).start();
                    break;
                case "hash":
                    if (arg.isEmpty()) { print("الاستخدام: hash TEXT"); break; }
                    print("SHA-256: "+sha256(arg)); break;
                case "password":
                    int len=16;
                    if (!arg.isEmpty()) try { len=Integer.parseInt(arg); } catch(Exception ignored) {}
                    if (len<8 || len>64) { print("الطول المقبول من 8 إلى 64."); break; }
                    String pass=makePassword(len); print("كلمة المرور: "+pass); print("احفظها في مكان آمن."); break;
                case "strength":
                    if (arg.isEmpty()) { print("الاستخدام: strength TEXT"); break; }
                    print("التقييم المحلي: "+strength(arg)); print("لا يتم إرسال النص إلى الشبكة."); break;
                case "ls": listFiles(arg); break;
                case "read":
                    if (arg.isEmpty()) { print("الاستخدام: read filename.txt"); break; }
                    readLocalFile(arg); break;
                case "export": exportTranscript(); break;
                default: print("أمر غير معروف. اكتب help.");
            }
        } catch(Exception e) { print("خطأ: "+e.getClass().getSimpleName()+": "+e.getMessage()); }
    }
    private void showIp() {
        try {
            ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT>=23) {
                Network n=cm.getActiveNetwork(); NetworkCapabilities c=cm.getNetworkCapabilities(n);
                print("الشبكة متصلة: "+(c!=null));
                if(c!=null) {
                    print("Wi‑Fi: "+c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
                    print("خلوي: "+c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
                    print("Ethernet: "+c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
                    print("إنترنت متاح حسب النظام: "+c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
                }
            } else {
                NetworkInfo n=cm.getActiveNetworkInfo(); print("الشبكة متصلة: "+(n!=null && n.isConnected()));
                if(n!=null) print("النوع: "+n.getTypeName());
            }
            print("IP المحلي: راجع تفاصيل الشبكة في إعدادات Android؛ قد لا يكون متاحًا عبر API عام.");
        } catch(Exception e) { print("تعذر قراءة حالة الشبكة: "+e.getMessage()); }
    }
    private String sha256(String input) throws Exception {
        byte[] b=MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder s=new StringBuilder(); for(byte x:b) s.append(String.format(Locale.ROOT,"%02x",x)); return s.toString();
    }
    private String makePassword(int len) {
        final String chars="ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%*-_+?";
        SecureRandom r=new SecureRandom(); StringBuilder s=new StringBuilder();
        for(int i=0;i<len;i++) s.append(chars.charAt(r.nextInt(chars.length())));
        return s.toString();
    }
    private String strength(String s) {
        int score=0;
        if(s.length()>=12) score++; if(s.length()>=16) score++;
        if(s.matches(".*[a-z].*")) score++; if(s.matches(".*[A-Z].*")) score++;
        if(s.matches(".*[0-9].*")) score++; if(s.matches(".*[^A-Za-z0-9].*")) score++;
        if(s.matches("(?i).*(password|123456|qwerty|admin).*")) score=Math.min(score,1);
        if(score<=2) return "ضعيفة"; if(score<=4) return "متوسطة"; return "قوية";
    }
    private File resolveLocal(String path) throws Exception {
        File f=new File(workDir,path).getCanonicalFile();
        String base=workDir.getCanonicalPath() + File.separator;
        if(!f.getCanonicalPath().equals(workDir.getCanonicalPath()) && !f.getCanonicalPath().startsWith(base)) throw new SecurityException("المسار خارج مساحة العمل");
        return f;
    }
    private void listFiles(String path) throws Exception {
        File dir=path.isEmpty()?workDir:resolveLocal(path);
        if(!dir.exists()) { print("المسار غير موجود: "+path); return; }
        if(!dir.isDirectory()) { print("ليس مجلدًا: "+path); return; }
        File[] fs=dir.listFiles();
        if(fs==null || fs.length==0) { print("(المجلد فارغ)"); return; }
        for(File f:fs) print((f.isDirectory()?"[DIR] ":"[FILE] ")+f.getName()+(f.isFile()?" ("+f.length()+" bytes)":""));
    }
    private void readLocalFile(String path) throws Exception {
        File f=resolveLocal(path);
        if(!f.isFile()) { print("الملف غير موجود داخل مساحة التطبيق: "+path); return; }
        if(f.length()>256*1024) { print("الملف أكبر من 256 KB."); return; }
        BufferedReader br=new BufferedReader(new InputStreamReader(new java.io.FileInputStream(f), StandardCharsets.UTF_8));
        String line; int n=0; while((line=br.readLine())!=null && n++<500) print(line); br.close();
    }
    private void pickTextFile() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("text/*");
        startActivityForResult(i,PICK_TEXT_FILE);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_TEXT_FILE && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            Uri uri=data.getData();
            try(InputStream in=getContentResolver().openInputStream(uri);
                BufferedReader br=new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                printLine("----- محتوى الملف المختار -----", green);
                String line; int n=0; while((line=br.readLine())!=null && n++<300) print(line);
                if(n>=300) print("تم إيقاف العرض عند 300 سطر.");
                printLine("----- نهاية الملف -----", green);
            } catch(Exception e) { print("تعذر قراءة الملف: "+e.getMessage()); }
        }
    }
    private void copyTranscript() {
        ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("ZORO Terminal output",transcript.toString()));
        Toast.makeText(this,"تم نسخ النتائج",Toast.LENGTH_SHORT).show();
    }
    private void exportTranscript() {
        try {
            File f=new File(getCacheDir(),"zoro-report.txt");
            try(FileOutputStream out=new FileOutputStream(f)) { out.write(transcript.toString().getBytes(StandardCharsets.UTF_8)); }
            Intent send=new Intent(Intent.ACTION_SEND); send.setType("text/plain"); send.putExtra(Intent.EXTRA_SUBJECT,"ZORO MINI TERMINAL PRO report");
            send.putExtra(Intent.EXTRA_TEXT,transcript.toString());
            startActivity(Intent.createChooser(send,"تصدير التقرير"));
        } catch(Exception e) { print("تعذر تصدير التقرير: "+e.getMessage()); }
    }
}
