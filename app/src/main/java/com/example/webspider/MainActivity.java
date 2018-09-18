package com.example.webspider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SmartRefreshLayout smartRefreshLayout;
    private String TAG = "MainActivity";
    private List<Thing> things = new ArrayList<>();
    ListView listView;
    XiuAdapter adapter;
    private int id = 0;
    private Handler handler;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.xiuShi);
        smartRefreshLayout = findViewById(R.id.smart_layout);
        smartRefreshLayout.setEnableRefresh(true);//启用刷新
        smartRefreshLayout.setEnableLoadmore(true);//启用加载
        init((++id) % 13);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 111) {
                    adapter = new XiuAdapter(MainActivity.this, R.layout.thing_item, things);
                    listView.setAdapter(adapter);
                }
            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Thing thing = things.get(position);
                Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                intent.putExtra("url", thing.getAddress());
                startActivity(intent);
            }
        });

        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refresh((++id) % 13);
            }
        });

        smartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                moreContent((++id) % 13);
            }
        });
    }

    private void moreContent(int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(
                            "https://www.qiushibaike.com/8hr/page/" + id + "/").get();
                    Elements elements = document.select("a.contentHerf");
                    for (int i = 0; i < elements.size(); i++) {
                        Element element = elements.get(i);
                        Thing thing = new Thing("        " + element.text() + "",
                                "https://www.qiushibaike.com" + element.attr("href"));
                        things.add(thing);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            smartRefreshLayout.finishLoadmore();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void refresh(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    things.clear();
                    Document document = Jsoup.connect(
                            "https://www.qiushibaike.com/8hr/page/" + id + "/").get();
                    Elements elements = document.select("a.contentHerf");
                    for (int i = 0; i < elements.size(); i++) {
                        Element element = elements.get(i);
                        Thing thing = new Thing("        " + element.text() + "",
                                "https://www.qiushibaike.com" + element.attr("href"));
                        things.add(thing);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            smartRefreshLayout.finishRefresh();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void init(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("HTML", "-----------------------");
                    Document document = Jsoup.connect("https://www.qiushibaike.com/8hr/page/" + id + "/").get();
                    Elements elements = document.select("a.contentHerf");
                    for (int i = 0; i < elements.size(); i++) {
                        Element element = elements.get(i);
                        Thing thing = new Thing("        " + element.text() + "",
                                "https://www.qiushibaike.com" + element.attr("href"));
                        things.add(thing);
                        Log.d("HTML", thing.getAddress());
                        Log.d("HTML", element.text());
                    }

                    Log.d("HTML", "-----------------------");
                    Message msg = new Message();
                    msg.what = 111;
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
