package com.example.android.xiyounews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private PullListView newsList;
    private List<News> list;
    private MainAdapter adapter;
    private String content;
    private String newsUrl;
    private String nextPage;
    private String url = "http://www.xiyou.edu.cn/index/xy21";
    private boolean isGetMore = false;
    private int num ;      //总页数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        MainThread mt = new MainThread(newsUrl);
        Thread t = new Thread(mt, "MainThread");
        t.start();
        /**
         * 下拉刷新
         */
        newsList.setOnRefreshListener(new PullListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isGetMore = false;
                MainThread mt = new MainThread(newsUrl);
                Thread t = new Thread(mt, "MainThread");
                t.start();
            }
        });
        /**
         * 上拉加载
         */
        newsList.setOnGetMoreListener(new PullListView.OnGetMoreListener() {
            @Override
            public void onGetMore() {
                isGetMore = true;
                if(num > 1) {
                    MainThread mt = new MainThread(nextPage);
                    Thread t = new Thread(mt, "MainThread");
                    t.start();
                }
            }
        });
        /**
         * 点击事件
         */
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                intent.putExtra("url", list.get(position-1).getUrl());
                startActivity(intent);
            }
        });
    }

    private final android.os.Handler handler = new android.os.Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0x000 :
//                    analyseNewsData();
                    analyseHTML();
                    if(isGetMore){
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter = new MainAdapter(MainActivity.this, list);
                        newsList.setAdapter(adapter);
                    }
                    newsList.refreshComplete();
                    newsList.getMoreComplete();
                    break;
            }
        }
    };

    public class MainThread implements Runnable{

        private String url;

        public MainThread(String url){
            this.url = url;
        }

        @Override
        public void run() {
            NetWorkClass netWorkClass = new NetWorkClass();
            content = netWorkClass.getDataByGet(url);
            handler.sendEmptyMessage(0x000);
        }
    }

    public class MainAdapter extends BaseAdapter{

        private LayoutInflater layoutInflater;
        private List<News> list;

        public MainAdapter(Context context, List<News> list){
            layoutInflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout linear;
            ViewHolder viewHolder;
            if(convertView == null){
                linear = (LinearLayout)layoutInflater.inflate(R.layout.news_item, null);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView) linear.findViewById(R.id.newsTitle);
                linear.setTag(viewHolder);
            } else {
                linear = (LinearLayout)convertView;
                viewHolder = (ViewHolder)linear.getTag();
            }
            viewHolder.title.setText(list.get(position).getTitle());
            return linear;
        }

        class ViewHolder{
            TextView title;
        }
    }

    public void analyseHTML(){
        if(content != null){
            int x = 0;
            Document document = Jsoup.parse(content);
            if (!isGetMore) {
                list.clear();
                Element element = document.getElementById("fanye3942");
                String text = element.text();
                num = Integer.parseInt(text.substring(text.lastIndexOf('/') + 1, text.length()-1));
                Log.w("num--->", String.valueOf(num));
            }
            Elements elements = document.getElementsByClass("c3942");
            while (true) {
                if(x == elements.size()){
                    break;
                }
                News news = new News();
                news.setTitle(elements.get(x).attr("title"));
                news.setUrl(elements.get(x).attr("href"));
                if (!isGetMore || x >= 4) {
                    list.add(news);
                    if (x >= 18) {
                        break;
                    }
                }
                x++;
            }
            if (num > 1) {
                nextPage = url + "/" + --num + ".htm";
            }
        }
    }

    private void initViews(){
        newsList = (PullListView) findViewById(R.id.newsList);
        list = new ArrayList<>();
        newsUrl = url + ".htm";
    }
}
