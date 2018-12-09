[GitHub](https://github.com/PdKingLiu/WebSpider)
### 我用的是嗅事百科做的例子
### 这里我把网页趴下来，然后用了一个listview展示出来，实现了刷新，加载更多，还有点击事件
![在这里插入图片描述](https://img-blog.csdn.net/20180918193110139?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0NvZGVGYXJtZXJfXw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
打开[嗅事百科](https://www.qiushibaike.com/) 按 f12 查看源码


![在这里插入图片描述](https://img-blog.csdn.net/2018091819343837?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0NvZGVGYXJtZXJfXw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

我对网页了解也不是太多，在网上查了一些资料，然后就开始上手了，对于网址的处理可以使用开源库，我使用了Jsoup
首先添加依赖库`implementation 'org.jsoup:jsoup:1.11.3'`
具体的内容请查看[Jsoup](http://www.open-open.com/jsoup/parsing-a-document.htm)
下面看我的主活动
很显然我首先调用了方法
`init((++id) % 13);`
参数其实不重要，这是我为了给listview刷新用的，因为我发现嗅事百科的下一页内容就是网址里面的一个数字加了一，但是最多十三页，所以我就取余了，看这个方法，实际上是另开了一个线程去进行网络请求，当请求完后就通知Handler更新UI，最初做的时候，我切换线程加载HTML后，不知道该如何处理网络请求和UI更新，因为，当网络请求没有结束时是不能更新UI的，就算是更新了但也存在一个问题，我在这个线程请求网络，如果适配器不为空了便可以更新UI了，但是更新完后，网络请求的这个线程还没结束，所以适配器内容就和UI上显示的不一样，然后你要是处理listview的点击事件就会崩掉，最后我使用了Handler，我让线程去进行网络请求，当线程结束时再通知Handler进行UI更新。

方法里面 
Jsoup.connect(); 
是为了把源码解析出来
document.select("a.contentHerf"); 
可以由我截图圈的发现正文正是`a.contentHerf`下的 ，所以把他当做条件筛选即可，然后会返回多个数据，逐个遍历即可，看代码里面的循环
element.text() 便是得到正文部分了
另外一个
element.attr("href") 是筛选`href`下的内容了，这个是什么呢
我们把嗅事百科这个正文点击一下进入详情页，观察网址，是不是
`https://www.qiushibaike.com`+`href`呢？之所以弄这个原因是有的文章太长，不能一次性显示完，所以就要进入详情页继续爬，我这里的实现是点击点击这个listview子项就重新启动一个活动，访问这个详情页的网址，然后展示出来，其实这个网页里面还会有图片，或者视频，大家看我圈的一个内容，`src`便签下的东西便是图片的地址了，大家可以自行拓展，把这些视频图片和头像都可以趴下来，展示出来，还有一个要说的事适配器的`Thing`这个有两个成员变量，一个是解析出的正文，还有就是详情页的地址了，地址传给另一个活动，即可把所有的内容爬下来。
#### 当然，我用了SmartRefreshLayout 实现了炫酷的刷新，还有卡片布局，详细内容大家有兴趣的可以查一查，下面附上代码
### 主活动 Mainactivity
```
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

```

### Thing类
```
package com.example.webspider;

public class Thing {

    public Thing(String item, String address) {
        this.item = item;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String item;

    private String address;

    public void setItem(String item) {
        this.item = item;
    }

    public String getItem() {

        return item;
    }
}
```
### 适配器
```
package com.example.webspider;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class XiuAdapter extends ArrayAdapter<Thing> {

    private int id;

    public XiuAdapter(@NonNull Context context, int resource, @NonNull List<Thing> objects) {
        super(context, resource, objects);
        id = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(id, parent, false);
        Thing thing = getItem(position);
        TextView textView = view.findViewById(R.id.thing_name);
        textView.setText(thing.getItem());
        return view;
    }
}

```
### 详细内容的活动
```
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

```
### 主活动布局
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.scwang.smartrefresh.layout.SmartRefreshLayout
        android:id="@+id/smart_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:srlAccentColor="#00000000"
        app:srlEnablePreviewInEditMode="true"
        app:srlPrimaryColor="#00000000">

        <ListView
            android:id="@+id/xiuShi"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_marginLeft="20dp"
            android:layout_marginRight="20dp"
            >

        </ListView>
    </com.scwang.smartrefresh.layout.SmartRefreshLayout>


</android.support.constraint.ConstraintLayout>
```
### 详细内容布局
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"

    tools:context=".ContentActivity">

    <ScrollView xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="20dp"
        android:layout_marginBottom="20dp">


        <android.support.v7.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_margin="20dp"
            app:cardUseCompatPadding="true"
            app:cardCornerRadius="10dp"
            app:cardBackgroundColor="#FFF8DC">

            <TextView
                android:id="@+id/content_thing"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_margin="10dp" />

        </android.support.v7.widget.CardView>
    </ScrollView>

</LinearLayout>
```
### listview的内容
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.v7.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="10dp"
    android:layout_marginTop="10dp"
    app:cardBackgroundColor="#FFF8DC"
    app:cardCornerRadius="10dp"
    app:cardUseCompatPadding="true">

    <TextView
        android:id="@+id/thing_name"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="10dp" />

</android.support.v7.widget.CardView>
```
### 还有SmartRefreshLayout和CardView的依赖库
```
    implementation 'com.android.support:cardview-v7:28.0.0-rc02'
    implementation 'com.scwang.smartrefresh:SmartRefreshLayout:1.0.4-7'
    implementation 'com.scwang.smartrefresh:SmartRefreshHeader:1.0.4-7'
```
### 最后记得加上联网权限
```
<uses-permission android:name="android.permission.INTERNET" />
```
