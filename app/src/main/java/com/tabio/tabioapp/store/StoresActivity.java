package com.tabio.tabioapp.store;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiResponse;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Store;
import com.tabio.tabioapp.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class StoresActivity extends BaseActivity {
    public static final String TAG = makeLogTag(StoresActivity.class);

    private StoresFragment storesFragment;

    private ApiRequest request;
    private ApiParams params;


    public StoresActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().getStringExtra("title") != null) {
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        }
        ButterKnife.bind(this);
        this.request = new ApiRequest(this);

        HashMap<String, Object> map = (HashMap)getIntent().getSerializableExtra("params");
        this.params = new ApiParams(self, map, getIntent().getStringExtra("url"));

        boolean showMap = getIntent().getBooleanExtra("showMap", false);
        boolean checkin = getIntent().getBooleanExtra("checkin", false);
        this.storesFragment = StoresFragment.newInstance(this.params, showMap, checkin, getIntent().getStringExtra("noDataTxt"));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.stores_fragment, this.storesFragment, StoresFragment.TAG)
                .commit();
    }

//                            JSONObject result = apiResponse.getBody().getJSONObject("result");
//                            total = result.getInt("total");
//                            JSONArray jStores = result.getJSONArray("store");
//                            LOGD(TAG, "length:"+jStores.length());
//                            for (int i=0; i<jStores.length(); i++) {
//                                stores.add(new Store(jStores.getJSONObject(i)));
//                            }
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    storesFragment.addAll(stores);
//                                }
//                            });
//
//                        } catch (JSONException e) {
//                            LOGE(TAG, e.getMessage());
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
