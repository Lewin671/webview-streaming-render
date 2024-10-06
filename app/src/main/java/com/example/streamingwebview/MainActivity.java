package com.example.streamingwebview;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView.setWebContentsDebuggingEnabled(true);
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);

        StringBuilder chunkPrefixBuilder = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            chunkPrefixBuilder.append(" ");
        }
        final String chunkPrefix = chunkPrefixBuilder.toString();

        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request != null) {
                    Uri uri = request.getUrl();
                    if (uri != null) {
                        String url = uri.toString();
                        Log.e(TAG, url);
                        if ("https://www.baidu.com/".equals(url) && request.isForMainFrame()) {
                            Log.e(TAG, "url matched");
                            PipedInputStream pipedInputStream = new PipedInputStream() {
                                @Override
                                public byte[] readAllBytes() throws IOException {
                                    Log.e(TAG, "readAllBytes()");
                                    return super.readAllBytes();
                                }

                                @Override
                                public int read(byte[] b) throws IOException {
                                    Log.e(TAG, "read(byte[] b)");
                                    return super.read(b);
                                }

                                @Override
                                public synchronized int read() throws IOException {
                                    Log.e(TAG, "read");
                                    return super.read();
                                }

                                @Override
                                public synchronized int read(byte[] b, int off, int len) throws IOException {
                                    Log.e(TAG, "read(byte[] b, int off, int len)");
                                    return super.read(b, off, len);
                                }

                            };
                            PipedOutputStream pipedOutputStream = new PipedOutputStream();
                            try {
                                pipedOutputStream.connect(pipedInputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    int cnt = 0;
                                    String beforeBody = "<!DOCTYPE html>\n" +
                                            "<html lang=\"en\">\n" +
                                            "<head>\n" +
                                            "    <meta charset=\"UTF-8\">\n" +
                                            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                                            "    <title>Document</title>\n" +
                                            "</head>\n" +
                                            "<script>\n" +
                                            "    var cnt = 0;\n" +
                                            "    setInterval(() => {\n" +
                                            "        console.log(\"current: \" + cnt);\n" +
                                            "        cnt = cnt + 1;\n" +
                                            "    }, 1000);\n" +
                                            "</script>\n" +
                                            "<body> <p> This is the static part. </p>";
                                    String afterBody = "</body>\n" +
                                            "</html>";
                                    try {
                                        pipedOutputStream.write(beforeBody.getBytes(StandardCharsets.UTF_8));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
//                                    try {
//                                        pipedOutputStream.close();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
                                    while (cnt < 10) {
                                        Log.e(TAG, "cnt = " + cnt);
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        String chunk = (chunkPrefix + "<p> current： " + cnt + "</p>");
                                        try {
                                            pipedOutputStream.write(chunk.getBytes());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                        cnt++;
                                        try {
                                            pipedOutputStream.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    try {
                                        pipedOutputStream.write(afterBody.getBytes(StandardCharsets.UTF_8));
                                        pipedOutputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                            return new WebResourceResponse("text/html", StandardCharsets.UTF_8.name(), pipedInputStream);
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        webView.loadUrl("https://www.baidu.com/");

        setContentView(webView);
    }
}