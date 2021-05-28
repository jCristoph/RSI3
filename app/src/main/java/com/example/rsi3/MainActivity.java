package com.example.rsi3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.MainThread;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private ListView lv;

    String namey, age;

    private static String JSON_URL = "https://skil-pc.pl/RSI.php";



    ArrayList<HashMap<String, String>> friendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        friendsList = new ArrayList<>();
        lv = findViewById(R.id.listview);
        Thread thread = new Thread(){
            public void run(){
                while (true) {
                    GetData getData = new GetData();
                    getData.execute();
                    try {
                        Thread.sleep(60* 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    public class GetData extends AsyncTask<String, String, String> {

        private NotificationManager notifManager;

        @Override
        protected String doInBackground(String... strings) {


            String current = "";

            try {
                URL url;
                HttpURLConnection urlConnection = null;

                try {


                    url = new URL(JSON_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(in);

                    int data = isr.read();
                    while (data != -1) {

                        current += (char) data;
                        data = isr.read();
                    }

                    return current;



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return current;

        }

        public void createNotification(String aMessage, Context context, int noti_id) {
            final int NOTIFY_ID = noti_id; // ID of notification
            String id = "default_notification_channel_id"; // default_channel_id
            String title = "Rdefault_notification_channel_title"; // Default Channel
            Intent intent;
            PendingIntent pendingIntent;
            NotificationCompat.Builder builder;
            if (notifManager == null) {
                notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notifManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, title, importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(context, id);
                intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                builder.setContentTitle("RSI ALERT")                            // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                        .setContentText(context.getString(R.string.app_name)) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(aMessage))
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            }
            else {
                builder = new NotificationCompat.Builder(context, id);
                intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                builder.setContentTitle("RSI ALERT")                            // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                        .setContentText(context.getString(R.string.app_name)) // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(aMessage))
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH);
            }
            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("RSI");

                friendsList.clear();

                ArrayList<String[]> coin_high_rsi = new ArrayList<>();
                ArrayList<String[]> coin_low_rsi = new ArrayList<>();




                for (int i = 0; i< jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    namey = jsonObject1.getString("coin");
                    age = jsonObject1.getString("rsi");
                    double val = Double.parseDouble(age);
                    //TODO powiadomienie
                    if(val >= 70.0) {
                        String[] temp = new String[2];
                        temp[0]= namey;
                        temp[1]= age;
                        coin_high_rsi.add(temp);
                    }
                    else if (val <= 30)

                    {
                        String[] temp = new String[2];
                        temp[0]= namey;
                        temp[1]= age;
                        coin_low_rsi.add(temp);


                    }



                    HashMap<String, String> friends = new HashMap<>();
                    friends.put("name", namey);
                    friends.put("age", age);

                    friendsList.add(friends);

                }

                String wiadomosc = "";

                if(!coin_high_rsi.isEmpty()) {

                    for (int i=0; i<coin_high_rsi.size(); i++) {

                        wiadomosc += "Sprzedaj " + coin_high_rsi.get(i)[0] + " RSI: "+ coin_high_rsi.get(i)[1].substring(0,5)+"\n";

                    }
                    createNotification(wiadomosc, MainActivity.this, 0);
                    wiadomosc = "";
                }


                if(!coin_low_rsi.isEmpty()) {

                    for (int i=0; i<coin_low_rsi.size(); i++) {

                        wiadomosc += "Kup " + coin_low_rsi.get(i)[0] + " RSI: "+ coin_low_rsi.get(i)[1].substring(0,5)+"\n";

                    }
                    createNotification(wiadomosc, MainActivity.this, 1);
                    wiadomosc = "";
                }




            } catch (JSONException e) {
                e.printStackTrace();
            }

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this,
                    friendsList,
                    R.layout.row_layout,
                    new String[] {"name", "age"},
                    new int[]{R.id.textView, R.id.textView2});

            lv.setAdapter(adapter);

        }
    }
}