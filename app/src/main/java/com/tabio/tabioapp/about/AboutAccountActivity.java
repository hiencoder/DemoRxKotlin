package com.tabio.tabioapp.about;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.ui.BaseActivity;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class AboutAccountActivity extends BaseActivity {
    private static final String TAG = makeLogTag(AboutAccountActivity.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_account);
        AppController.getInstance().sendGAScreen("Tabio ID, PINコードとは");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.button_aboutAccount);

        TextView title = (TextView)findViewById(R.id.about_title);
        title.setText(getString(R.string.text_about_example_title));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
