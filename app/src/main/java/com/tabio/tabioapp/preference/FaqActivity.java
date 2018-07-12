package com.tabio.tabioapp.preference;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.tutorial.TutorialActivity;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.web.WebActivity;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class FaqActivity extends BaseActivity {
    private static final String TAG = makeLogTag(FaqActivity.class);

    public static final int[] faqTitleStringIds = new int[]{
            R.string.text_faq_title_1,
            R.string.text_faq_title_2,
            R.string.text_faq_title_3,
            R.string.text_faq_title_4,
            R.string.text_faq_title_5,
            R.string.text_faq_title_6,
            R.string.text_faq_title_7,
            R.string.text_faq_title_8,
            R.string.text_faq_title_9
    };

    private FaqDataAdapter adapter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.text_preference_title_help_faq);
        ButterKnife.bind(this);

        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new FaqDataAdapter(this.faqTitleStringIds);
        this.recyclerView.setAdapter(this.adapter);
    }

    public void onTutorialButtonClicked() {
        Intent view = new Intent(this, TutorialActivity.class);
        view.putExtra("nocheck", true);
        AppController.getInstance().sendGAScreen("使い方が分かるチュートリアル");
        AppController.getInstance().decideTrack("570f30e199c3634a425af51d");
        view.putExtra("showLogo", false);
        startActivity(view);
    }

    public void onContactButtonClicked() {
        AppController.getInstance().sendGAScreen("お問い合わせフォーム");
        AppController.getInstance().decideTrack("570f32c599c3634a425af52c");
        Intent view = new Intent(this, WebActivity.class);
        String url = BuildConfig.BASE_URL + ApiRoute.WV_CONTACT + "?";
        HashMap<String, String> params = new HashMap<>();
        //OSとOSバージョン,端末機種,TabioID,アプリのバージョンの4項目を送る
        params.put("os", "android");
        params.put("model", Build.MODEL);
        params.put("os_version", String.valueOf(Build.VERSION.RELEASE));
        params.put("tabio_id", self.getTabioId());
        PackageManager pm = this.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        params.put("app_version", versionName);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url += "&";
            url += entry.getKey();
            url += "=";
            url += entry.getValue();
        }
        view.putExtra("url", url);
        startActivity(view);
    }

    public void leave() {
        AppController.getInstance().sendGAScreen("退会");
        AppController.getInstance().decideTrack("570f32df99c3634a425af52f");
        Intent view = new Intent(this, WebActivity.class);
        view.putExtra("url", BuildConfig.BASE_URL + ApiRoute.WV_LEAVE + "?tabio_id=" + self.getTabioId() + "&token=" + self.getToken());
        view.putExtra("leave", true);
        startActivity(view);
    }

    public void onFaqListItemClicked(int stringId) {
        String url = "";
        switch (stringId) {
            case R.string.text_faq_title_1:
                url = ApiRoute.WV_FAQ_1;
                break;
            case R.string.text_faq_title_2:
                url = ApiRoute.WV_FAQ_2;
                break;
            case R.string.text_faq_title_3:
                url = ApiRoute.WV_FAQ_3;
                break;
            case R.string.text_faq_title_4:
                url = ApiRoute.WV_FAQ_4;
                break;
            case R.string.text_faq_title_5:
                url = ApiRoute.WV_FAQ_5;
                break;
            case R.string.text_faq_title_6:
                url = ApiRoute.WV_FAQ_6;
                break;
            case R.string.text_faq_title_7:
                leave();
                return;
            case R.string.text_faq_title_8:
                url = ApiRoute.WV_FAQ_8;
                break;
            case R.string.text_faq_title_9:
                url = ApiRoute.WV_FAQ_9;
                break;
        }
        Intent view = new Intent(this, WebActivity.class);
        view.putExtra("url", BuildConfig.EC_URL + url);
        startActivity(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class FaqDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private int[] faqListStringIds;

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_TITLE = 1;
        private static final int VIEW_TYPE_LIST_ITEM = 2;
        private static final int VIEW_TYPE_FOOTER = 3;

        public FaqDataAdapter(int[] faqListStringIds) {
            this.faqListStringIds = faqListStringIds;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    return new HeaderHolder(inflater.inflate(R.layout.faq_header, parent, false));
                case VIEW_TYPE_TITLE:
                    return new TitleHolder(inflater.inflate(R.layout.title_item, parent, false));
                case VIEW_TYPE_LIST_ITEM:
                    return new ListItemHolder(inflater.inflate(R.layout.faq_list_item, parent, false));
                case VIEW_TYPE_FOOTER:
                    return new FooterHolder(inflater.inflate(R.layout.faq_footer, parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_HEADER: {
                    HeaderHolder vh = (HeaderHolder) holder;
                    vh.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onTutorialButtonClicked();
                        }
                    });
                    break;
                }
                case VIEW_TYPE_TITLE: {
                    ((TextView) ((TitleHolder) holder).itemView).setText(getString(R.string.text_faq_title));
                    break;
                }
                case VIEW_TYPE_LIST_ITEM: {
                    ListItemHolder vh = (ListItemHolder) holder;
                    vh.title.setText(this.faqListStringIds[position - 1]);
                    vh.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onFaqListItemClicked(faqListStringIds[position - 1]);
                        }
                    });
                    break;
                }
                case VIEW_TYPE_FOOTER: {
                    FooterHolder vh = (FooterHolder) holder;
                    vh.contactButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onContactButtonClicked();
                        }
                    });
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TYPE_HEADER;
            } else if (position == 1) {
                return VIEW_TYPE_TITLE;
            } else if (position == this.getItemCount() - 1) {
                return VIEW_TYPE_FOOTER;
            } else {
                return VIEW_TYPE_LIST_ITEM;
            }
        }

        @Override
        public int getItemCount() {
            return this.faqListStringIds.length + 2;
        }

        class HeaderHolder extends RecyclerView.ViewHolder {

            public HeaderHolder(View itemView) {
                super(itemView);
            }
        }

        class TitleHolder extends RecyclerView.ViewHolder {
            public TitleHolder(View itemView) {
                super(itemView);
            }
        }


        class FooterHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.contact_button)
            Button contactButton;

            public FooterHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class ListItemHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.title)
            TextView title;

            public ListItemHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
