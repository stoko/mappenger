package com.stoko.mappenger;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        ((ListView)findViewById(R.id.listView)).setAdapter(adapter);

        new AsyncMessages().execute(getString(R.string.mappenger_ws_address));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messages, menu);
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

    public void BackToMap(View v) {
        startActivity(getParentActivityIntent());
    }

    class AsyncMessages extends AsyncTask<String, Void, List<FoundMessage>> {
        @Override
        protected List<FoundMessage> doInBackground(String... params) {
            try {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode

                HttpClient client = new HttpClient(params[0], pref.getString("access_token", null));

                return client.sendGetFoundMessages(getString(R.string.ws_get_found_messages));
            } catch (Exception ex) {
                return null;
            }
        }

        protected void onPostExecute(List<FoundMessage> foundMessages) {
            for(FoundMessage msg : foundMessages) {
                listItems.add("Message: "+msg.Message);
            }

            adapter.notifyDataSetChanged();
        }
    }
}
