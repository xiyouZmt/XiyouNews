package com.example.android.xiyounews;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsActivity extends Activity {

    private TextView textTitle;
    private TextView textEdit;
    private TextView textDetail;
    private String title;
    private String edit;
    private StringBuilder text;
    private Document document;
    private String prefix = "http://news.xupt.edu.cn/system/resource/code/news/click/dynclicks.jsp?clickid=";
    private String clickId;
    private String suffix = "&owner=1153721284&clicktype=wbnews";
    private String clickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        textTitle = (TextView) findViewById(R.id.textTitle);
        textEdit = (TextView) findViewById(R.id.textEdit);
        textDetail = (TextView) findViewById(R.id.textDetail);
        NewsThread nt = new NewsThread(getIntent().getStringExtra("url"));
        Thread t = new Thread(nt, "NewsThread");
        t.start();
    }

    public final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x000 :
                    analyseHTML(document);
                    textTitle.setText(title);
                    textDetail.setText(text);
                    break;
                case 0x111 :
                    edit = edit.substring(0, edit.length() - 2) + clickCount + "次";
                    textEdit.setText(edit);
                    break;
            }
        }
    };

    public class NewsThread implements Runnable{

        private String url;

        public NewsThread(String url){
            this.url = url;
        }

        @Override
        public void run() {
            if(url.equals(prefix + clickId + suffix)){
                NetWorkClass netWorkClass = new NetWorkClass();
                clickCount = netWorkClass.getDataByGet(url);
                handler.sendEmptyMessage(0x111);
            } else {
                getHTML(url);
                handler.sendEmptyMessage(0x000);
            }
        }
    }

    public void getHTML(String url){
        NetWorkClass netWorkClass = new NetWorkClass();
        document = Jsoup.parse(netWorkClass.getDataByGet(url));
    }

    public void analyseHTML(Document document){
        if(document != null) {
            Elements elements = document.getElementById("nrys").getAllElements();
            title = elements.get(1).text();

            edit = elements.get(4).text();
            String html = elements.get(4).getAllElements().get(1).html();
            clickId = html.substring(html.lastIndexOf(' ') + 1, html.lastIndexOf(')'));
            NewsThread nt = new NewsThread(prefix + clickId + suffix);
            Thread t = new Thread(nt, "NewsThread");
            t.start();

            Element mElement = document.getElementById("vsb_content_1031");
            if(mElement != null) {
                Elements mElements = mElement.getAllElements();
                text = new StringBuilder();
                for (Element element : mElements) {
                    if(element.className().equals("nrzwys") || element.tagName().equals("strong")){
                        continue;
                    }
                    String tag = element.tagName();
                    String str = element.text();
                    if(!element.text().equals(" ") && !element.text().equals(""));{
                        text.append("  ").append(element.text()).append("\n");
                    }
                    if (element.className().equals("vsbcontent_end")) {
                        break;
                    }
                }
            }
        }
    }
}