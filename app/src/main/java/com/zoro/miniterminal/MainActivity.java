package com.zoro.miniterminal;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;
import android.text.InputType;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(8, 12, 10);
    private final int PANEL = Color.rgb(15, 22, 18);
    private final int GREEN = Color.rgb(83, 255, 143);
    private final int MUTED = Color.rgb(139, 166, 148);
    private LinearLayout output;
    private EditText commandInput;
    private ScrollView scroll;
    private final ArrayList<String> history = new ArrayList<>();
    private int historyIndex = 0;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
        line("ZORO MINI TERMINAL v1.0.0", GREEN, true);
        line("Android local diagnostic console", MUTED, false);
        line("Type 'help' to list available commands.", MUTED, false);
        line("No root required. Commands run with Android app permissions.", MUTED, false);
        line("$ ready", GREEN, false);
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius); d.setStroke(1, Color.rgb(37, 68, 48));
        return d;
    }

    private TextView text(String s, int color, float size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextColor(color); t.setTextSize(size);
        t.setTypeface(Typeface.MONOSPACE, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(2, 4, 2, 4);
        return t;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(14, 14, 14, 10);
        root.setBackgroundColor(BG);
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL); header.setPadding(14, 12, 14, 12);
        header.setBackground(bg(PANEL, 18));
        TextView title = text("ZORO // MINI TERMINAL", GREEN, 19, true);
        TextView sub = text("LOCAL TOOLS  •  ANDROID  •  NO ROOT", MUTED, 10, false);
        header.addView(title); header.addView(sub);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        output = new LinearLayout(this); output.setOrientation(LinearLayout.VERTICAL);
        output.setPadding(10, 10, 10, 10);
        scroll.addView(output);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, 0, 1);
        sp.setMargins(0, 12, 0, 10); root.addView(scroll, sp);

        LinearLayout entry = new LinearLayout(this);
        entry.setGravity(Gravity.CENTER_VERTICAL); entry.setPadding(8, 5, 6, 5);
        entry.setBackground(bg(PANEL, 14));
        TextView prompt = text("›", GREEN, 22, true);
        entry.addView(prompt);
        commandInput = new EditText(this);
        commandInput.setSingleLine(true);
        commandInput.setTextColor(Color.WHITE); commandInput.setHintTextColor(MUTED);
        commandInput.setHint("enter command...");
        commandInput.setTextSize(14); commandInput.setTypeface(Typeface.MONOSPACE);
        commandInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        commandInput.setBackgroundColor(Color.TRANSPARENT);
        entry.addView(commandInput, new LinearLayout.LayoutParams(0, -2, 1));
        Button run = new Button(this); run.setText("RUN"); run.setTextColor(GREEN);
        run.setTextSize(12); run.setAllCaps(false); run.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(25, 54, 35)));
        entry.addView(run);
        root.addView(entry, new LinearLayout.LayoutParams(-1, -2));
        run.setOnClickListener(v -> executeInput());
        commandInput.setOnEditorActionListener((v, action, event) -> { executeInput(); return true; });
    }

    private void line(String s, int color, boolean bold) {
        output.addView(text(s, color, 12, bold));
        scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void executeInput() {
        String raw = commandInput.getText().toString().trim();
        if (raw.isEmpty()) return;
        commandInput.setText("");
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(commandInput.getWindowToken(), 0);
        history.add(raw); historyIndex = history.size();
        line("$ " + raw, GREEN, true);
        String[] parts = raw.split("\\s+", 2);
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        String arg = parts.length > 1 ? parts[1].trim() : "";
        switch (cmd) {
            case "help":
                line("help       show this command list", Color.WHITE, false);
                line("clear      clear terminal output", Color.WHITE, false);
                line("sysinfo    basic Android/device information", Color.WHITE, false);
                line("ip         network connection summary", Color.WHITE, false);
                line("dns HOST   resolve a hostname using system DNS", Color.WHITE, false);
                line("ping HOST  check hostname reachability (one attempt)", Color.WHITE, false);
                line("history    show commands entered this session", Color.WHITE, false);
                line("hash TEXT  SHA-256 hash of supplied text", Color.WHITE, false);
                line("about      app information and limitations", Color.WHITE, false);
                break;
            case "clear": output.removeAllViews(); break;
            case "sysinfo":
                line("Device: " + Build.MANUFACTURER + " " + Build.MODEL, Color.WHITE, false);
                line("Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")", Color.WHITE, false);
                line("App: " + getPackageName(), Color.WHITE, false);
                line("ABI: " + Build.SUPPORTED_ABIS[0], Color.WHITE, false);
                break;
            case "ip": networkInfo(); break;
            case "dns":
                if (arg.isEmpty()) { line("Usage: dns example.com", Color.YELLOW, false); break; }
                resolve(arg, false); break;
            case "ping":
                if (arg.isEmpty()) { line("Usage: ping example.com", Color.YELLOW, false); break; }
                resolve(arg, true); break;
            case "history":
                if (history.isEmpty()) line("(empty)", MUTED, false);
                else for (int i=0;i<history.size();i++) line((i+1)+": "+history.get(i), Color.WHITE, false);
                break;
            case "hash":
                if (arg.isEmpty()) { line("Usage: hash text-to-hash", Color.YELLOW, false); break; }
                line(sha256(arg), Color.WHITE, false); break;
            case "about":
                line("ZORO MINI TERMINAL v1.0.0", GREEN, true);
                line("Safe, app-scoped diagnostic commands; not a full Linux shell.", Color.WHITE, false);
                line("No root access, arbitrary shell execution, or device-wide file access.", MUTED, false);
                break;
            default:
                line("Unknown command: " + cmd + " — type help", Color.rgb(255, 125, 125), false);
        }
    }

    private void networkInfo() {
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network n = cm.getActiveNetwork();
            if (n == null) { line("No active network detected.", Color.YELLOW, false); return; }
            NetworkCapabilities c = cm.getNetworkCapabilities(n);
            if (c == null) { line("Network details unavailable.", Color.YELLOW, false); return; }
            line("Connected: yes", Color.WHITE, false);
            line("Transport Wi-Fi: " + c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI), Color.WHITE, false);
            line("Transport Cellular: " + c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR), Color.WHITE, false);
            line("Internet capability: " + c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET), Color.WHITE, false);
            line("Validated by Android: " + c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED), Color.WHITE, false);
        } catch (Exception e) { line("Network error: " + e.getMessage(), Color.YELLOW, false); }
    }

    private void resolve(String host, boolean checkReachability) {
        line("Working on " + host + " ...", MUTED, false);
        new Thread(() -> {
            String result;
            try {
                long start = System.currentTimeMillis();
                InetAddress address = InetAddress.getByName(host);
                long elapsed = System.currentTimeMillis() - start;
                boolean reachable = !checkReachability || address.isReachable(2500);
                StringBuilder b = new StringBuilder();
                b.append("Host: ").append(host).append("\nAddress: ").append(address.getHostAddress())
                 .append("\nDNS time: ").append(elapsed).append(" ms");
                if (checkReachability) b.append("\nReachable: ").append(reachable ? "yes" : "no / blocked");
                result = b.toString();
            } catch (Exception e) { result = "Lookup failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(); }
            final String r = result;
            runOnUiThread(() -> { for (String s : r.split("\n")) line(s, Color.WHITE, false); });
        }).start();
    }

    private String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format(Locale.ROOT, "%02x", b));
            return out.toString();
        } catch (Exception e) { return "SHA-256 error"; }
    }
}
