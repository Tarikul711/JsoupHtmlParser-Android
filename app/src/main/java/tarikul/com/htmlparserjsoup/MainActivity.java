package tarikul.com.htmlparserjsoup;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Context context;
    public ProgressDialog progressDialog;
    static ArrayList<JSONObject> jsonObjects;
    Elements detailsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        findViewById(R.id.btnClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Online data loading ....");
                progressDialog.setCancelable(false);
                progressDialog.show();
                new webDataScraping().execute();
            }
        });
    }


    @SuppressLint("StaticFieldLeak")
    public class webDataScraping extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            jsonObjects = new ArrayList<>();

            try {
                Document doc = Jsoup.connect("https://www.ielts-mentor.com/cue-card-sample?start=1").get();
                Elements links = doc.getElementsByClass("list-title");
                for (Element link : links) {
                    Element detailContentLink = link.select("a").first(); // get details Content <a> tag
                    String detailsUrl = detailContentLink.attr("abs:href"); // get details content href link


                    Document docText = Jsoup.connect(detailsUrl).get(); // for connection detail url
                    docText.getElementsByClass("article-info").remove(); // remove some content from html content
                    docText.getElementsByClass("size-1 extravote").remove();
                    docText.getElementsByClass("pager pagenav").remove();
                    docText.getElementsByClass("tags inline").remove();
                    docText.select("div#jc").remove();
                    detailsContent = docText.select("div#wrapper2");// get whole html content
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("title", "Cue Card");
                        jsonObject.put("content", detailsContent.html());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonObjects.add(jsonObject);

                }
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (IOException ignored) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            return "Execute";
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            saveData();

        }
    }

    public static void saveData() {

        File myFile = new File("/sdcard/webDataScrapingData.json");
        try {
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(jsonObjects.toString());
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
