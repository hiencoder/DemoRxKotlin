package com.tabio.tabioapp.preference;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.main.MainActivity;
import com.tabio.tabioapp.me.MyBaseActivity;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.top.TopActivity;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.util.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class LanguageSettingsActivity extends MyBaseActivity {
    private static final String TAG = makeLogTag(LanguageSettingsActivity.class);

    private LanguageDataAdapter adapter;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LANG_JA, LANG_EN, LANG_FR, LANG_ZH, LANG_KO})
    public @interface LANG {
    }

    public static final String LANG_JA = "ja";
    public static final String LANG_EN = "en";
    public static final String LANG_FR = "fr";
    public static final String LANG_ZH = "zh";
    public static final String LANG_KO = "ko";

    public static final String[] LANGS = new String[]{
            LANG_JA, LANG_EN, LANG_FR, LANG_ZH, LANG_KO
    };
    public static final String[] DISPLAY_LANGUAGES = new String[]{
            "日本語", "English", "Français", "中文", "한국어"
    };
    public static final String[] ALERTS = new String[]{
            "日本語に設定しますか？",
            "Translate to English?\n" + " *Automated translation.",
            "Vouz voulez afficher la page en français?\n" + "(*Traduction automatique. Il y aura peu être des difficultés)",
            "设置为中文吗?可使自动翻译功能。 \n" + "※但有一部分没有设置自动翻译。",
            "한국어로 설정하시겠습니까? 기계번역을 이용하여 번역합니다. ※일부 번역되지 않는 페이지도 있습니다."
    };

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);
        AppController.getInstance().sendGAScreen("言語設定");
        AppController.getInstance().decideTrack("570f30d699c3634a425af51b");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.text_preference_title_settings_language);
        ButterKnife.bind(this);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setHasFixedSize(true);
        this.adapter = new LanguageDataAdapter(LANGS, self.getLanguage());
        this.recyclerView.setAdapter(this.adapter);
    }

    @Override
    protected void changeLanguage(String lang) {
        super.changeLanguage(lang);
    }

    private void confirm(final int position) {
        if (self.getLanguage().equals(LANGS[position])) {
            return;
        }
        AppController.getInstance().showAlert(this, null, ALERTS[position], "YES", "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeLanguage(LANGS[position]);
                Intent view = new Intent(LanguageSettingsActivity.this, TopActivity.class);
                view.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(view);
                finish();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class LanguageDataAdapter extends RecyclerView.Adapter<LanguageDataAdapter.ViewHolder> {

        private String[] langs;
        private String myLang;

        public LanguageDataAdapter(String[] langs, String myLang) {
            this.langs = langs;
            this.myLang = myLang;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.title.setText(DISPLAY_LANGUAGES[position]);
            holder.check.setVisibility(this.langs[position].equals(this.myLang) ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirm(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.langs.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.check)
            ImageView check;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
