package edu.mit.prg.soroprogo;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by randi on 2/1/18.
 */

public class LoggingService extends IntentService {
    public static final String IP = "edu.mit.prg.soroproro.IP_ADDRESS";
    public static final String MESSAGE = "edu.mit.prg.soroprogo.MESSAGE_TO_LOG";

    private String TAG = "Soro progo log";
    private String logfile = "devicelog_";
    private File dir;
    private Integer logCount = 0;

    public LoggingService() {
        super("LoggingService");
    }

    @Override
    public void onCreate() {
        // Get the directory for the user's public directory.
        dir = new File(Environment.getExternalStorageDirectory(), "soroprogo/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith(logfile)) {
                logCount += 1;
            }
        }
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String interaction = workIntent.getStringExtra(MESSAGE);
        String ip = workIntent.getStringExtra(IP);

        Log.d(TAG, "received interaction sending to: " + ip);
        BufferedReader reader = null;
        String text = "no response";
        try {
            // write to file on device
            File f = new File(dir, logfile + logCount.toString() + ".txt");
            FileOutputStream outputStream = new FileOutputStream(f, true);
            outputStream.write(interaction.getBytes());
            outputStream.close();

            // write to server
            URL url = new URL(ip);
            URLConnection conn = url.openConnection();
            Log.d(TAG, "writing to server");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(interaction);
            wr.flush();

            // Read Server Response

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }
            text = sb.toString();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        finally {
            try {
                reader.close();
            }

            catch(Exception ex) {}
        }
        Log.d(TAG, text);
    }
}
