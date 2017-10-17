package edu.mit.prg.soroprogo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import prg.innards.appcore.android.AndroidSpecificResourceLocator;
import prg.innards.network.ircp.IRCPConstants;
import prg.innards.network.ircp.IRCPUDPManager;
import prg.innards.network.ircp.IRCPUDPSender;

public class SoroProgo extends Activity {

    IRCPUDPManager man = null;
    byte robotID;
    String ID = "1";
    String TAG = "Soro progo";
    String logfile = "devicelog_";
    String savefile = "prog_";
    String soundfile = "record_";
    File dir;
    Integer logCount = 1;
    MediaRecorder soundRecorder;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    public enum TRIGGERS {
        START, STOP;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        WebView browser;
        browser=(WebView)findViewById(R.id.webkit);
        browser.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("SoroProgo", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId() );
                return true;
            }
        });
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://web.media.mit.edu/~randiw12/popr/scratch-blocks-develop/pop")) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }
        });
        //Enable Javascript
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);
        //Inject WebAppInterface methods into Web page by having Interface 'Android'
        browser.addJavascriptInterface(new WebAppInterface(this), "Android");
        browser.clearCache(false); // usually true
        browser.loadUrl("http://web.media.mit.edu/~randiw12/popr/scratch-blocks-develop/pop/index.html");

        // Get the directory for the user's public pictures directory.
        dir = new File(Environment.getExternalStorageDirectory(), "soroprogo/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith(logfile)) {
                logCount += 1;
            }
        }
        soundRecorder = new MediaRecorder();

        launch();
    }

    public void launch() {
        AndroidSpecificResourceLocator.setActivity(this);
        if (man == null) {
            /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter ID:");

            // Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    robotID = Byte.valueOf(input.getText().toString());

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();*/
            robotID = Byte.valueOf(ID);
            man = IRCPUDPManager.createWithSuggestedID(robotID, IRCPConstants.DRAGONBOT_TELEOP_ID);
            man.launch((int)Math.round(1/60.0 * 1000f));
        }
    }

    public void sendSpeech(String speech) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.SPEECH_SYNTH, speech);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void sendRule(String rule) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.ADD_RULE, rule);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void triggerTegaCommand (String command) {
        command = command.toUpperCase();
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.MOTION_TRIGGER , command);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void triggerTegaSpeech (String speech) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.AUDIO_TRIGGER , speech);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void sendNewCommand(String rule) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.EXECUTE_RULE, rule);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void sendDeleteRule(String rule) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.DELETE_RULE, rule);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void clearRules() {
        Log.e(TAG, "Clear rules");
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.CLEAR_RULES);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void sendNewRules( String rules ) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.CLEAR_RULES);
            String[] ruleList = rules.split(";");
            for (String rule : ruleList) {
                sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.ADD_RULE, rule);
            }
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void sendTrigger ( TRIGGERS num ) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            switch(num) {
                case START:
                    sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.START_TRIGGER);
                    break;
                case STOP:
                    sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.STOP_TRIGGER);
                    break;
                default: break;
            }
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void setPolicy ( String policy ) {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.AI_POLICY, policy);
            Log.d(TAG, "sending policy: " + policy);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void logInteraction(String interaction) {
        try {
            File f = new File(dir, logfile + logCount.toString() + ".txt");
            FileOutputStream outputStream = new FileOutputStream(f, true);
            outputStream.write(interaction.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer countSavedPrograms() {
        Integer progCount = 0;
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith(savefile)) {
                progCount += 1;
            }
        }
        Log.d(TAG, "Saved programs " + progCount.toString());
        return progCount;
    }

    public void saveProgram(String xml) {
        Integer count = countSavedPrograms() + 1;
        String filename = savefile + count.toString() + ".txt";
        saveProgram(xml, filename);
    }

    public void saveProgram(String xml, String name) {
        File f;
        try {
            Integer count = Integer.valueOf(name);
            f = new File(dir, savefile + count.toString()+".txt");
        } catch (Exception e) { // we have a full filename
            f = new File(dir, name+".txt");
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(f);
            outputStream.write(xml.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadProgram(String name) {
        File f;
        Log.d(TAG, "loading a program");
        try {
            Integer count = Integer.valueOf(name);
            f = new File(dir, savefile + count.toString()+".txt");
        } catch (Exception e) { // we have a full filename
            f = new File(dir, name+".txt");
        }

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text.toString();
    }

    public void addWedoBrick() {
        IRCPUDPSender sender = IRCPUDPManager.getSenderForID(IRCPConstants.BEHAVIOR_MODULE_ID);
        if (sender != null) {
            sender.send(IRCPConstants.ANDROID.MAJOR_TYPE, IRCPConstants.ANDROID.ADD_WEDO);
        } else {
            Log.e(TAG, "Sender equals null...did not send");
        }
    }

    public void connectR1d1(int id) {
        if(id <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter ID:");

            // Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    robotID = Byte.valueOf(input.getText().toString());
                    man.switchRobotID(robotID);
                    sendNewCommand("u01_3+100");

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        } else {
            robotID = (byte) id;
            man.switchRobotID(robotID);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                sendNewCommand("u01_1+100 u01_3+100");
            }
        }, 2700); // found empirically, why so long?
    }

    public Integer countSavedRecordings() {
        Integer progCount = 0;
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith(savefile)) {
                progCount += 1;
            }
        }
        Log.d(TAG, "Saved programs " + progCount.toString());
        return progCount;
    }

    public void update() {

    }

    //Class to be injected in Web page
    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        public void triggerCommand(String command) {
            if (command.startsWith("motion_")) {
                // Len of motion_tega_ = 12
                triggerTegaCommand(command.substring(12));
            } else if (command.startsWith("speech_")){
                // Len of speech_tega_ = 12
                triggerTegaSpeech(command.substring(12));
            }
        }

        public void triggerSpeech(String speech) { triggerTegaSpeech(speech); }

        public void generateSpeech(String speech) {
            sendSpeech(speech);
        }

        public void sendCommand(String command) {
            sendNewCommand(command);
        }

        public void saveRule(String rule) {
            sendRule(rule);
        }

        public void resetRules(String rules) { sendNewRules(rules); }

        public void deleteRule(String rule) { sendDeleteRule(rule); }

        public void deleteAllRules() { clearRules(); }

        public void stopTrigger() { sendTrigger(TRIGGERS.STOP); }

        public void startTrigger() { sendTrigger(TRIGGERS.START); }

        public void log(String text) { logInteraction(text); }

        public void saveNewWorkspace(String xml) { saveProgram(xml); }

        public void saveWorkspace(String xml, String name) { saveProgram(xml, name); }

        public String loadWorkspace(String name) { return loadProgram(name); }

        public void addWedo() { addWedoBrick(); }

        public void addRobot(int id) { connectR1d1(id); }

        public int numPrograms() { return countSavedPrograms(); }

        public int numRecords() { return countSavedRecordings(); }

        public void setAIPolicy(String policy) { setPolicy(policy); }

    }
}
