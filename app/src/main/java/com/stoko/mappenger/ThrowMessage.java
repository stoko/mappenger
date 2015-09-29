package com.stoko.mappenger;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.List;

public class ThrowMessage extends AppCompatActivity {

    private MappengerService mpService = null;
    private ProgressDialog _prSend = null;

    private int _speedId = 1;
    private boolean _static = false;
    private String _iconType = "snail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throw_message);
        mpService = MappengerService.GetInstance();
        _prSend = new ProgressDialog(this);
        _prSend.setMessage(getString(R.string.throwing_progress_message));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_throw_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        _static = false;

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioSpeed1:
                if (checked) {
                    _speedId = 1;
                    _iconType = getString(R.string.speed_1_icon);
                }
                break;
            case R.id.radioSpeed2:
                if (checked) {
                    _speedId = 2;
                    _iconType = getString(R.string.speed_2_icon);
                }
                break;
            case R.id.radioSpeed3:
                if (checked) {
                    _speedId = 3;
                    _iconType = getString(R.string.speed_3_icon);
                }
                break;
            case R.id.radioStatic:
                if(checked) {
                    _static = true;
                }
                break;
        }
    }

    public void ThrowMessage(View v) {
        Double lat = mpService.GetLat();
        Double lon = mpService.GetLong();
        String msg = ((EditText)findViewById(R.id.txtMessage)).getText().toString();

        _prSend.show();
        new AsyncThrowMessage().execute(getString(R.string.mappenger_ws_address), String.valueOf(lat), String.valueOf(lon), msg, String.valueOf(_speedId), _iconType, String.valueOf(_static));
    }

    public void BackToHome() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    class AsyncThrowMessage extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode

                HttpClient client = new HttpClient(params[0], pref.getString("access_token", null));

                client.sendPostThrowMessage(getString(R.string.ws_throw_message_endpoint), params[1], params[2], params[3], params[4], params[5], params[6]);

                return null;
            } catch (Exception ex) {
                return null;
            }
        }

        protected void onPostExecute(Void param) {
            _prSend.hide();
            ((EditText)findViewById(R.id.txtMessage)).setText("");
            BackToHome();
        }
    }
}
