package id.or.radiokita.mobile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadioService extends Service {

    private static final int HELLO_ID = 1;
    public static boolean isPlay;
    public static MediaPlayer stream;
    public static String url;
    public static int serviceAvailable;
    public static NotificationManager mNotificationManager;
    public static PowerManager.WakeLock wl;
    public static Boolean bgPlay = false;
    public static Intent intent;
    private PhoneStateListener mPhoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
//					toggleControlButton(streamHD1.controlButton, "play");
//					toggleControlButton(streamHD2.controlButton, "play");
                        stopService(RadioService.intent);
//					stopService(streamHD2.svc);

                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                    default:
                }
            } catch (Exception e) {
            }
        }
    };
    private static String radioName;
    TelephonyManager tm;

    public void toggleButtons(Button button, String state) {
//		Main.toggleControlButton(button, state);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void onStart(Intent intent, int startid) {
        if (intent == null) {
            stopSelf();
        } else {
            // Restore preferences
//		SharedPreferences settings = getSharedPreferences(
//				infoScreen.PREFS_NAME, 0);
//		bgPlay = settings.getBoolean("bgPlay", false);
            RadioService.intent = intent;
            stream = new MediaPlayer();

            Bundle extras = intent.getExtras();
            url = extras.getString("url");
            radioName = extras.getString("name");
            try {
                stream.setDataSource(url);
            } catch (IllegalArgumentException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                streamErrorHandler();
            } catch (IllegalStateException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                streamErrorHandler();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                streamErrorHandler();
            }
            stream.setAudioStreamType(AudioManager.STREAM_MUSIC);

            stream.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer stream) {

                    tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

                    makeNotification();
                    // Set the Wake Lock - CPU on, keyboard and screen off
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                            getString(R.string.app_name));
                    wl.acquire();
                    serviceAvailable = 1;

                    stream.start();

//				if (Main.activeStation == 1) {
//					toggleControlButton(streamHD1.controlButton, "stop");
//					toggleControlButton(streamHD2.controlButton, "play");
//
//				} else {
//					toggleControlButton(streamHD1.controlButton, "play");
//					toggleControlButton(streamHD2.controlButton, "stop");
//				}

                }
            });
            try {
                stream.prepare();
                isPlay = true;
            } catch (IllegalStateException e1) {
                e1.printStackTrace();
                streamErrorHandler();
            } catch (IOException e1) {
                e1.printStackTrace();
                streamErrorHandler();
            }
        }
    }

    @Override
    public void onDestroy() {
        // kill the stream
        serviceAvailable = 0;
        if (stream != null) {
            stream.stop();
            stream.release();
            isPlay = false;
            stream = null;
            // kill the status notification
            mNotificationManager.cancel(HELLO_ID);
            intent = null;
            if (wl.isHeld()) {
                // kill the wake lock
                wl.release();
                wl = null;
            }
        }
    }

    public void toggleControlButton(Button button, String state) {
//		Main.toggleControlButton(button, state);
    }

    public String getStreamingURL(String string) {

        String html = "";
        String result = "";
        try {
            URL updateURL = new URL(string);
            URLConnection conn = updateURL.openConnection();
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);

            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            html = new String(baf.toByteArray());

            String re1 = ".*?"; // Non-greedy match on filler
            String re2 = "((?:http|https)(?::\\/{2}[\\w]+)(?:[\\/|\\.]?)(?:[^\\s\"]*))"; // HTTP
            // URL
            // 1

            Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
            Matcher m = p.matcher(html);
            if (m.find()) {
                String httpurl1 = m.group(1);
                result = httpurl1.toString();
            } else {
//				if (Main.activeStation == 1) {
//					result = getString(R.string.stream_url_HD1);
//				} else {
//					result = getString(R.string.stream_url_HD2);
//				}
            }

        } catch (Exception e) {
//			if (Main.activeStation == 1) {
//				result = getString(R.string.stream_url_HD1);
//			} else {
//				result = getString(R.string.stream_url_HD2);
//			}
        }
        return result;
    }

    public void streamErrorHandler() {

        Toast t = Toast.makeText(this, "Network error... Check Internet Connection",
                Toast.LENGTH_LONG);
        t.setGravity(Gravity.TOP | Gravity.CENTER, 0, 100);
        t.show();

        isPlay = false;

//		toggleButtons(streamHD1.controlButton, "play");
//		toggleButtons(streamHD2.controlButton, "play");

        serviceAvailable = 0;
        stream.stop();
        stream.release();
        stream = null;
    }

    public void makeNotification() {
        // Create the Status Notifcation
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.logo_radio_kita; // icon from resources
        CharSequence tickerText = getString(R.string.app_name); // ticker-text
        long when = System.currentTimeMillis(); // notification time
        Context context = getApplicationContext(); // application Context
        CharSequence contentTitle = radioName; // expanded
        // message
        // title
        CharSequence contentText = getString(R.string.app_name); // expanded
        // message
        // text

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new Notification(icon, tickerText, when);
        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);
        mNotificationManager.notify(HELLO_ID, notification);
        notification.flags |= Notification.FLAG_NO_CLEAR;
    }
}
