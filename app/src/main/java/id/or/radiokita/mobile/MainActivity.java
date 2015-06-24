package id.or.radiokita.mobile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import at.markushi.ui.CircleButton;
import es.claucookie.miniequalizerlibrary.EqualizerView;

/**
 * Created by Rio Rizky Rainey on 01/06/2015.
 * rizkyrainey@gmail.com
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private EqualizerView equalizerView;
    private CircleButton circleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        equalizerView = (EqualizerView) findViewById(R.id.equalizer_view);
        circleButton = (CircleButton) findViewById(R.id.button_play);
        circleButton.setOnClickListener(this);

        if (RadioService.isPlay) playRadio();
        else stopRadio();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            TextView msg = new TextView(this);
            msg.setMovementMethod(LinkMovementMethod.getInstance());
            msg.setClickable(true);
            msg.setTextSize(18);
            msg.setPadding(20, 20, 20, 20);
            msg.setText(Html.fromHtml("Radio Kita " +
                    "adalah aplikasi radio streaming untuk Radio Kita FM 105.2 FM Madiun" +
                    "<br/><br/>Aplikasi ini dikembangkan oleh <a href=\"http://firzil.co.id/\">Firzil Tech</a>" +
                    "<br/><br/>Untuk menghubungi developer atau melaporkan bug silahkan email ke <a href=\"mailto:support@firzil.co.id\">" +
                    "support@firzil.co.id</a>" +
                    "<br/><br/>Semoga aplikasi ini bermanfaat dan yang membantu menyebarkan aplikasi ini mendapatkan Rahmat " +
                    "dan selalu dalam lindungan Allah. Amiin."));
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("About us");
            alertDialogBuilder.setView(msg);
            alertDialogBuilder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, RadioService.class);
        if (equalizerView.isAnimating()) {
            stopRadio();
            stopService(intent);
        } else {
            Bundle extras = new Bundle();
            extras.putString("url", "http://live.radiokita.or.id");
            extras.putString("name", "Radio Kita");
            intent.putExtras(extras);
            startService(intent);
            playRadio();
        }
    }

    private void stopRadio() {
        circleButton.post(new Runnable() {
            @Override
            public void run() {
                circleButton.setImageResource(R.drawable.play);
            }
        });
        equalizerView.stopBars(); // When you want equalizer stops animating
    }

    private void playRadio() {
        circleButton.post(new Runnable() {
            @Override
            public void run() {
                circleButton.setImageResource(R.drawable.pause);
            }
        });
        equalizerView.animateBars();
    }
}
