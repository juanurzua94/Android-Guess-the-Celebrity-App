package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageView celebrityImage;
    private Button celebrity1;
    private Button celebrity2;
    private Button celebrity3;

    private URL url;
    private HttpURLConnection connection;
    private InputStream in;
    private InputStreamReader reader;
    private Bitmap celeb;
    private String code = "";

    final private String captureInfo = "<div class=\"image\">(\\r\\n|\\r|\\n)[\\t]*<img src=\"(?<imageURL>.+)\" alt=\"(?<name>.+)\"/>(\\r\\n|\\r|\\n)[\\t]*</div>";

    private String answer = "";
    private HashMap<String, String> celebrities;
    private Random rand;
    private int totalCelebs = 0;
    private Deque<String> recentAnswers;

    private class DownloadCelebrityImage extends AsyncTask<String, Void, Bitmap>{
        protected Bitmap doInBackground(String... urls){
            try {
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                in = connection.getInputStream();
                Bitmap celebrity = new BitmapFactory().decodeStream(in);
                return celebrity;

            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    private class DownloadCelebrityInfo extends AsyncTask<String, Void, StringBuilder>{
        protected StringBuilder doInBackground(String... urls){

            StringBuilder webPage = new StringBuilder();

            try{
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                in = connection.getInputStream();
                reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    webPage.append((char) data);
                    data = reader.read();
                }

            }
            catch(Exception e){
                e.printStackTrace();
                Log.i("ERROR", "Mission failed. We will get em next time");
            }


            return webPage;
        }
    }

    public void chosenCelebrity(View view){
        Button counter = (Button) view;
        if(counter.getText() == answer){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            nextCeleb();
        }
        else{
            Toast.makeText(this, "Wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebrityImage = (ImageView) findViewById(R.id.celebrityImage);
        celebrity1 = (Button) findViewById(R.id.celebrity1);
        celebrity2 = (Button) findViewById(R.id.celebrity2);
        celebrity3 = (Button) findViewById(R.id.celebrity3);

        celebrities = new HashMap<String, String>();

        try {
            code = new DownloadCelebrityInfo().execute("http://www.posh24.se/kandisar").get().toString();
            populateCelebrityMap();
            rand = new Random();
            recentAnswers = new ArrayDeque<>();
            nextCeleb();

        }
        catch (Exception e){
            e.printStackTrace();
            Log.i("ERROR", "PROGRAM FAILED");
        }

    }

    private void populateCelebrityMap(){
        Pattern info = Pattern.compile(captureInfo);
        Matcher data = info.matcher(code);


        while(data.find()){

            celebrities.put(data.group("name"), data.group("imageURL"));
            totalCelebs++;
        }

        code = "";
        if(celebrities.size() > 0) {
            int count  = 0;
            for (Map.Entry<String, String> entry : celebrities.entrySet()) {
                Log.i(entry.getKey(), entry.getValue());
                count++;
            }
            Log.i("TOTAL CELEBRITIES:", Integer.toString(count));
        }
        else {
            Log.i("ERROR", "ARRAY IS EMPTY. CHECK REGEX");
        }

    }

    private void nextCeleb(){

        List<String> keysAsArray = new ArrayList<>(celebrities.keySet());
        int option = rand.nextInt(totalCelebs);
        while(recentAnswers.contains(keysAsArray.get(option)) || keysAsArray.get(option).contentEquals("Kenza") || keysAsArray.get(option).contentEquals( "Halle Bailey")){
            option = rand.nextInt(totalCelebs);
        }

        try {


            celeb = new DownloadCelebrityImage().execute(celebrities.get(keysAsArray.get(option)).toString()).get();
            celebrityImage.setImageBitmap(celeb);
            recentAnswers.push(keysAsArray.get(option));
            answer = keysAsArray.get(option);


            // you neeed to randomize answers still
            String celebOption2 = "";
            String celebOption3 = "";

            int cel2 = rand.nextInt(totalCelebs);
            while(cel2 == option){
                cel2 = rand.nextInt(totalCelebs);
            }
            celebOption2 = keysAsArray.get(cel2);
            int cel3 = rand.nextInt(totalCelebs);
            while(cel3 == option || cel3 == cel2){
                cel3 = rand.nextInt(totalCelebs);
            }
            celebOption3 = keysAsArray.get(cel3);
            randomizeButtonChoices(celebOption2, celebOption3);

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    private void randomizeButtonChoices(String celebOption2, String celebOption3){
       Random randomizedOption = new Random();
       int correctAnswerButt = randomizedOption.nextInt(3);
       if(correctAnswerButt == 0) {
           celebrity1.setText(answer);
           celebrity2.setText(celebOption2);
           celebrity3.setText(celebOption3);
       }
       else if(correctAnswerButt == 1) {
           celebrity2.setText(answer);
           celebrity1.setText(celebOption2);
           celebrity3.setText(celebOption3);
       }
       else {
           celebrity3.setText(answer);
           celebrity1.setText(celebOption2);
           celebrity2.setText(celebOption3);
       }
    }
}
