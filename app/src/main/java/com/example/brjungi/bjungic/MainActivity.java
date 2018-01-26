package com.example.brjungi.bjungic;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private int notificationID = 1;
    private long time=0;
    private int MY_PERM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERM);

        DBA2 db = new DBA2(this);

        db.open();
        long id = db.insertSlika("Posljednja večera", "Leonardo DaVinci");
        id = db.insertSlika("Mona Lisa", "Leonardo DaVinci");
        id = db.insertSlika("Bogorodica na stijenama", "Leonardo DaVinci");
        id = db.insertSlika("Žena koja plače", "Pablo Picasso");
        id = db.insertSlika("Postojanost pamćenja (mekani satovi)", "Salvador Dali");

        id = db.insertPeriod("Ekspresionizam","Edvard Munch");
        id = db.insertPeriod("Kubizam","Pablo Picaso");
        id = db.insertPeriod("Surealizam","Salvador Dali");
        id = db.insertPeriod("Renesansa","Leonardo DaVinci");
        id = db.insertPeriod("Minimalizam","Frank Stella");
        db.close();

    }

    public void DisplayContact(Cursor c)
    {

        Toast.makeText(this,
                "id: " + c.getString(0) + "\n" +
                        "Name: " + c.getString(1) + "\n" +
                        "Email:  " + c.getString(2),
                Toast.LENGTH_LONG).show();
    }

    protected void displayNotification(Bitmap bmp)
    {
        //---PendingIntent to launch activity if the user selects
        // this notification---
        Intent i = new Intent(this, ImageActivity.class);

        i.putExtra("notificationID", notificationID);


        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, i, 0);

        long[] vibrate = new long[] { 100, 250, 100, 500};

//Notification Channel - novo od Android O

        String NOTIFICATION_CHANNEL_ID = "my_channel_01";
        CharSequence channelName = "hr.math.karga.MYNOTIF";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(vibrate);

//za sve verzije
        NotificationManager nm = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

// za Notification Chanel

        nm.createNotificationChannel(notificationChannel);




//ovako je i u starim verzijama, jedino dodano .setChannelId (za stare verzije to brisemo)

        Notification notif = new Notification.Builder(this)
                .setTicker("Download done")
                .setContentTitle("Preuzimanje završeno")
                .setContentText("i trajalo je " + time + " milisekundi")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setContentIntent(pendingIntent)
                .setVibrate(vibrate)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setStyle(new Notification.BigPictureStyle()
                .bigPicture(bmp))
                .build();
        //najnovije, od API level 26.1.0., .setWhen ide po defautlu ovdje na currentTimeMillis

/*        final NotificationCompat.Builder notif = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)

                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(vibrate)
                .setSound(null)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Meeting with customer at 3pm...")
                .setContentText("this is the second row")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTicker("Reminder: meeting starts in 5 minutes")
                .setContentIntent(pendingIntent)
                .setAutoCancel(false); */

// za sve verzije

        nm.notify(notificationID, notif);
    }

    public void preuzmiSliku(View view) {

        EditText text = (EditText) findViewById(R.id.webLokacija);
        String st = text.getText().toString();
        DownloadImageTask d = new DownloadImageTask();
        d.execute(st);
    }

    private InputStream OpenHttpConnection(String urlString)
            throws IOException
    {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }
        catch (Exception ex)
        {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }

    private Bitmap DownloadImage(String URL)
    {
        time = System.currentTimeMillis();
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }
        return bitmap;
    }

    /*
    private void spremiSliku(Bitmap bmp) {
        String iconsStoragePath = Environment.getExternalStorageDirectory()+ File.separator;
        File sdIconStorageDir = new File(iconsStoragePath);
        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();
        try {
            String filePath = null;
            filePath = Environment.getExternalStorageDirectory() + File.separator + "preuzetaSlika" + ".jpg";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Failed to Create folder",
                    Toast.LENGTH_SHORT).show();
        }
    }
    */

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            return DownloadImage(urls[0]);
        }

        protected void onPostExecute(Bitmap result) {
            time = System.currentTimeMillis()-time;
            ImageView img = (ImageView) findViewById(R.id.img);
            //spremiSliku(result);
            //img.setImageBitmap(result);

            Toast.makeText(MainActivity.this, "time: " + time, Toast.LENGTH_SHORT).show();
            displayNotification(result);

        }
    }

    public void ispisiSve(View view){
        String s="Slike:";
        DBA2 db = new DBA2(this);
        db.open();
        Cursor c = db.getAllSlika();
        if (c.moveToFirst())
        {
            do {
                s=s+"\n"+"id: " + c.getString(0) + "\n" +
                        "naziv: " + c.getString(1) + "\n" +
                        "autor:  " + c.getString(2);
            } while (c.moveToNext());
        }
        s=s+"\nPeriodi:";


        c = db.getAllPeriod();
        if (c.moveToFirst())
        {
            do {
                s=s+"\n"+"id: " + c.getString(0) + "\n" +
                        "razdoblje: " + c.getString(1) + "\n" +
                        "gl. predstavnik:  " + c.getString(2);
            } while (c.moveToNext());
        }


        db.close();

        TextView text = (TextView) findViewById(R.id.ispisBaze);
        text.setText(s);
    }
    public void brisiSliku(View view)  {
        DBA2 db = new DBA2(this);
        db.open();
        EditText text = (EditText) findViewById(R.id.brisiSlike) ;
        int indeks  = Integer.parseInt(text.getText().toString());
        if (db.deleteSlika(indeks))
            Toast.makeText(this, "Slika uspješno obrisana.", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Ne postoji slika s tim ID-jem.", Toast.LENGTH_LONG).show();
        db.close();

    }

    public void brisiRazdoblje (View view) {
        DBA2 db = new DBA2(this);
        db.open();
        EditText text = (EditText) findViewById(R.id.brisiPeriod) ;
        int indeks  = Integer.parseInt(text.getText().toString());
        if (db.deletePeriod(indeks))
            Toast.makeText(this, "Period uspješno obrisan.", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Ne postoji period s tim ID-jem.", Toast.LENGTH_LONG).show();
        db.close();
    }

    public void picasso (View view) {
        DBA2 db = new DBA2(this);
        db.open();
        TextView text = (TextView) findViewById(R.id.picasso);
        text.setText(db.getSlika("Pablo Picasso").getString(1));
        db.close();
    }
}
