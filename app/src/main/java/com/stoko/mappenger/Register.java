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

public class Register extends AppCompatActivity {

    ProgressDialog _prDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        _prDialog = new ProgressDialog(this);
        _prDialog.setMessage(getString(R.string.signing_up_progress_message));
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

    public void SignUp(View v) {
        _prDialog.show();
        String email = ((EditText)findViewById(R.id.txtEmail)).getText().toString();
        String pass = ((EditText)findViewById(R.id.txtPassword)).getText().toString();
        String cPass = ((EditText)findViewById(R.id.txtConfirmPassword)).getText().toString();

        new AsyncSignUp().execute(getString(R.string.mappenger_ws_address), email, pass, cPass);
    }

    public void BackToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    class AsyncSignUp extends AsyncTask<String, Void, SignUpResponse> {
        @Override
        protected SignUpResponse doInBackground(String... params) {
            try {
                HttpClient client = new HttpClient(params[0]);

                return client.sendPostSignUp(getString(R.string.ws_sign_up_endpoint), params[1], params[2], params[3]);
            } catch (Exception ex) {
                SignUpResponse sur = new SignUpResponse();
                sur.Errors = getString(R.string.exception_on_login);
                return sur;
            }

        }

        protected void onPostExecute(SignUpResponse result) {
            _prDialog.hide();

            if(result != null) {
                ((TextView)findViewById(R.id.txtErrors)).setText(result.Errors);
            } else {
                BackToLogin();
            }
        }
    }
}
