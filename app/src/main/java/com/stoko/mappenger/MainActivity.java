package com.stoko.mappenger;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog _prDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, MappengerService.class));

        try {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode
            if(pref.getString("access_token", "none") != "none" && pref.getString("userName", "none") != "none") {
                SwitchToHome();
            }
        }
        catch (Exception ex) {
            return;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.quit) {
            this.finishAffinity();
        }

        return super.onOptionsItemSelected(item);
    }

    public void CallAPI(View v) {
        _prDialog = new ProgressDialog(this);
        _prDialog.setMessage(getString(R.string.logging_in_progress_message));
        _prDialog.setCancelable(false);
        _prDialog.setInverseBackgroundForced(false);
        _prDialog.show();
        String userName = ((EditText)findViewById(R.id.txtUsername)).getText().toString();
        String password = ((EditText)findViewById(R.id.txtPassword)).getText().toString();
        new AsyncLogin().execute(getString(R.string.mappenger_ws_address), userName, password);
    }

    public void SwitchToHome() {
        // redirect to home activity
        Intent myIntent = new Intent(this, MapActivity.class);
        startActivity(myIntent);

        if(_prDialog != null)
            _prDialog.hide();
    }

    public void GoSignUp(View v) {
        Intent myIntent = new Intent(this, Register.class);
        startActivity(myIntent);
    }

    class AsyncLogin extends AsyncTask<String, Void, LoginResult> {
        @Override
        protected LoginResult doInBackground(String... params) {
            try {
                HttpClient client = new HttpClient(params[0]);

                return client.sendPostLogin("Token", params[1], params[2]);
            } catch (Exception ex) {
                LoginResult lr = new LoginResult();
                lr.error = true;
                lr.errorMessage = getString(R.string.exception_on_login);
                return lr;
            }

        }

        protected void onPostExecute(LoginResult result) {
            if(android.os.Build.VERSION.SDK_INT > 22 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1337);
                _prDialog.hide();
                return;
            }
            if(result.error != null && result.error == true) {
                ((EditText)findViewById(R.id.txtUsername)).setText("");
                ((EditText)findViewById(R.id.txtPassword)).setText("");
                ((TextView)findViewById(R.id.lblErrorMessage)).setText(result.errorMessage);
                _prDialog.hide();
            } else {
                // login successfull
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("userName", result.userName);
                editor.putString("access_token", result.access_token);
                editor.commit();

                SwitchToHome();
            }
        }
    }
}
