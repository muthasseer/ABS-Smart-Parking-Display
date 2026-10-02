package com.absecuritas.smartparkingdisplay;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String PREFS = "display_prefs";
    private static final String KEY_SERVER = "server";
    private static final String KEY_GATE = "gate";
    private static final String KEY_SIDE = "side";
    private final Handler handler = new Handler();
    private SharedPreferences prefs;
    private WebView webView;
    private FrameLayout root;
    private EditText serverInput;
    private EditText portInput;
    private Spinner gateSpinner;
    private Spinner sideSpinner;
    private TextView statusText;
    private final List<String> gateIds = new ArrayList<>();
    private final List<String> gateNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        enterImmersive();
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        showSetup();
    }

    private void enterImmersive() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView label(String text, float size) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(21,32,58));
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(dp(2), dp(10), dp(2), dp(4));
        return t;
    }

    private EditText input(String value, String hint) {
        EditText e = new EditText(this);
        e.setText(value);
        e.setHint(hint);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(dp(12), dp(8), dp(12), dp(8));
        return e;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setPadding(dp(12), dp(5), dp(12), dp(5));
        return b;
    }

    private void showSetup() {
        enterImmersive();
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(24), dp(18), dp(24), dp(18));
        outer.setBackgroundColor(Color.rgb(245,247,251));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(com.absecuritas.smartparkingdisplay.R.drawable.company_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        header.addView(logo, new LinearLayout.LayoutParams(dp(64), dp(64)));
        LinearLayout headText = new LinearLayout(this);
        headText.setOrientation(LinearLayout.VERTICAL);
        TextView title = label("ABS SMART PARKING DISPLAY", 22);
        headText.addView(title);
        TextView sub = label("Android / Android TV • Gate vehicle display", 12);
        sub.setTextColor(Color.rgb(93,107,132));
        headText.addView(sub);
        header.addView(headText, new LinearLayout.LayoutParams(0, -2, 1));
        outer.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(4), dp(8), dp(4), dp(8));

        serverInput = input(prefs.getString(KEY_SERVER, "192.168.1.7"), "Gate computer IP or server URL");
        form.addView(label("Gate Computer IP / Server", 13));
        form.addView(serverInput, new LinearLayout.LayoutParams(-1, dp(52)));

        portInput = input("5000", "5000");
        form.addView(label("Web Port", 13));
        form.addView(portInput, new LinearLayout.LayoutParams(-1, dp(52)));

        gateSpinner = new Spinner(this);
        form.addView(label("Gate", 13));
        form.addView(gateSpinner, new LinearLayout.LayoutParams(-1, dp(52)));

        sideSpinner = new Spinner(this);
        String[] sides = new String[]{"ENTRY", "EXIT"};
        sideSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sides));
        sideSpinner.setSelection("exit".equalsIgnoreCase(prefs.getString(KEY_SIDE, "entry")) ? 1 : 0);
        form.addView(label("Display Side", 13));
        form.addView(sideSpinner, new LinearLayout.LayoutParams(-1, dp(52)));

        statusText = new TextView(this);
        statusText.setText("Same LAN / Wi‑Fi is required. Example: 192.168.1.7:5000");
        statusText.setTextSize(12);
        statusText.setTextColor(Color.rgb(93,107,132));
        statusText.setPadding(0, dp(10), 0, dp(10));
        form.addView(statusText);

        Button loadGates = button("LOAD GATES");
        form.addView(loadGates, new LinearLayout.LayoutParams(-1, dp(48)));
        loadGates.setOnClickListener(v -> loadGateList());

        Button connect = button("CONNECT DISPLAY");
        connect.setTextColor(Color.WHITE);
        connect.setBackgroundColor(Color.rgb(25,118,243));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(56));
        cp.topMargin = dp(10);
        form.addView(connect, cp);
        connect.setOnClickListener(v -> connectDisplay());

        scroll.addView(form);
        outer.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(outer);
        loadGateList();
    }

    private String baseUrl() {
        String s = serverInput.getText().toString().trim();
        String port = portInput.getText().toString().trim();
        if (s.isEmpty()) s = "192.168.1.7";
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://" + s;
        Uri u = Uri.parse(s);
        if (u.getPort() == -1 && !port.isEmpty()) s = s + ":" + port;
        return s.replaceAll("/$", "");
    }

    private void loadGateList() {
        final String base = baseUrl();
        statusText.setText("Connecting to " + base + " …");
        new Thread(() -> {
            try {
                String body = httpGet(base + "/api/display/gates");
                JSONObject root = new JSONObject(body);
                JSONArray arr = root.optJSONArray("gates");
                List<String> ids = new ArrayList<>();
                List<String> names = new ArrayList<>();
                if (arr != null) {
                    for (int i=0;i<arr.length();i++) {
                        JSONObject g = arr.getJSONObject(i);
                        ids.add(g.optString("id"));
                        names.add(g.optString("name", "GATE " + g.optString("id")));
                    }
                }
                runOnUiThread(() -> {
                    gateIds.clear(); gateIds.addAll(ids);
                    gateNames.clear(); gateNames.addAll(names);
                    if (gateIds.isEmpty()) {
                        for(int i=1;i<=8;i++){ gateIds.add(String.valueOf(i)); gateNames.add("GATE "+i); }
                    }
                    gateSpinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, gateNames));
                    String saved = prefs.getString(KEY_GATE, "1");
                    int idx = gateIds.indexOf(saved);
                    gateSpinner.setSelection(Math.max(0, idx));
                    statusText.setText("Connected. Select ENTRY or EXIT and connect.");
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    gateIds.clear(); gateNames.clear();
                    for(int i=1;i<=8;i++){ gateIds.add(String.valueOf(i)); gateNames.add("GATE "+i); }
                    gateSpinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, gateNames));
                    statusText.setText("Could not load gates. You can still select a Gate and connect.");
                });
            }
        }).start();
    }

    private void connectDisplay() {
        if (gateIds.isEmpty()) loadGateList();
        int pos = gateSpinner.getSelectedItemPosition();
        if (pos < 0 || pos >= gateIds.size()) pos = 0;
        String gate = gateIds.get(pos);
        String side = sideSpinner.getSelectedItemPosition() == 1 ? "exit" : "entry";
        String base = baseUrl();
        prefs.edit().putString(KEY_SERVER, base).putString(KEY_GATE, gate).putString(KEY_SIDE, side).apply();
        showWebDisplay(base + "/display?gate=" + Uri.encode(gate) + "&side=" + Uri.encode(side));
    }

    private void showWebDisplay(String url) {
        enterImmersive();
        root = new FrameLayout(this);
        webView = new WebView(this);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadsImagesAutomatically(true);
        ws.setAllowFileAccess(false);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setBackgroundColor(Color.rgb(246,248,251));
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String u) {
                statusText = null;
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError error) {
                Toast.makeText(MainActivity.this, "Display connection error", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(webView, new FrameLayout.LayoutParams(-1,-1));

        Button settings = new Button(this);
        settings.setText("⚙");
        settings.setTextSize(16);
        settings.setAlpha(.45f);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(dp(46),dp(46),Gravity.TOP|Gravity.END);
        sp.setMargins(0,dp(8),dp(8),0);
        root.addView(settings, sp);
        settings.setOnClickListener(v -> showSetup());
        setContentView(root);
        webView.loadUrl(url);
    }

    private String httpGet(String target) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(target).openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(5000);
        c.setReadTimeout(7000);
        c.setUseCaches(false);
        int code = c.getResponseCode();
        BufferedReader br = new BufferedReader(new InputStreamReader(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(); String line;
        while((line=br.readLine())!=null) sb.append(line);
        br.close(); c.disconnect();
        if(code < 200 || code >= 300) throw new RuntimeException("HTTP "+code);
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else showSetup();
    }
}
