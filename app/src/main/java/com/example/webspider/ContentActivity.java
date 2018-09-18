package com.example.webspider;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ContentActivity extends AppCompatActivity {

    private String url;

    private String content = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        final TextView textView = findViewById(R.id.content_thing);
        url = getIntent().getStringExtra("url");

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 111) {
                    textView.setText("        " + msg.obj.toString());
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(url).get();
                    Elements elements = document.select("div.content");
                    for (int i = 0; i < elements.size(); i++) {
                        Element element = elements.get(i);
                        Log.d("wds", element.text());
                        Message msg = new Message();
                        msg.obj = element.text();
                        msg.what = 111;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }


}
