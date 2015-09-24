package com.stoko.mappenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MessageDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerData", 0);
        if(pref.getString("MessageFoundPK", null) != null && pref.getString("MessageFoundRK", null) != null) {
            new AsyncMessageDetails().execute(getString(R.string.mappenger_ws_address), pref.getString("MessageFoundPK", null), pref.getString("MessageFoundRK", null));

            pref.edit().remove("MessageFoundPK");
            pref.edit().remove("MessageFoundRK");
            pref.edit().commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message_details, menu);
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

    public void BackToMessages(View v) {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    class AsyncMessageDetails extends AsyncTask<String, Void, UserMessage> {
        @Override
        protected UserMessage doInBackground(String... params) {
            try {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode

                HttpClient client = new HttpClient(params[0], pref.getString("access_token", null));

                UserMessage msg = client.sendGetMessageByPKRK(getString(R.string.ws_get_message_by_pkrk_endpoint), params[1], params[2]);
                client.sendPostFoundMessage(getString(R.string.ws_post_found_message_endpoint), msg);
                return msg;
            } catch (Exception ex) {
                return null;
            }
        }

        protected void onPostExecute(UserMessage msg) {
            ((TextView)findViewById(R.id.messageText)).setText(msg.Message);
        }
    }
}
