package com.tabio.tabioapp.store;

import android.content.Context;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.filter.FilterModel;
import com.tabio.tabioapp.model.Filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/16/15.
 */
public class StoreFilter extends Filter implements Serializable {
    public static final String TAG = makeLogTag(StoreFilter.class);

    public static final int STORE_SEARCH_FREEWORD = 0;
    public static final int STORE_SEARCH_PREFECTURE = 1;
    public static final int STORE_SEARCH_BRAND = 2;
    public static final int STORE_SEARCH_SERVICE = 3;

    public StoreFilter(Context c) {
        super(FILTER_TYPE_STORE);

        titles = new String[]{
                c.getString(R.string.text_search_menu_freeword),
                c.getString(R.string.text_search_menu_searchByPrefectures),
                c.getString(R.string.text_search_menu_searchByBrands),
                c.getString(R.string.text_search_menu_searchByServices)};
        listTitles = new String[]{
                c.getString(R.string.text_search_menu_freeword),
                c.getString(R.string.text_search_menu_prefectures),
                c.getString(R.string.text_search_menu_brand),
                c.getString(R.string.text_search_menu_itemServices)};

        List<FilterModel> freewordModels = new ArrayList<>();
        List<FilterModel> prefectureModels = new ArrayList<>();
        List<FilterModel> brandModels = new ArrayList<>();
        List<FilterModel> serviceModels = new ArrayList<>();

        {
            // フリーワード
            FilterModel filterModel = new FilterModel("free_word", "", false);
            freewordModels.add(filterModel);
        }

        {
            // 都道府県
            String[] prefectureStrings = c.getResources().getStringArray(R.array.prefectures);
            for (int i=0; i<prefectureStrings.length; i++) {
                String key = String.valueOf(i+1);
                String displayName = prefectureStrings[i];
                FilterModel filterModel = new FilterModel(key, displayName, false);
                prefectureModels.add(filterModel);
            }
        }

        {
            // ブランド
            // TODO brand keyが決まったら埋める
            LinkedHashMap<String, String> brandKeyAndValue = new LinkedHashMap<>();
            brandKeyAndValue.put("01", c.getString(R.string.text_store_brand_kutsushitaya));
            brandKeyAndValue.put("02", c.getString(R.string.text_store_brand_tabio));
            brandKeyAndValue.put("03", c.getString(R.string.text_store_brand_tabio_men));

            for (Map.Entry<String, String> entry : brandKeyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                brandModels.add(filterModel);
            }
        }

        {
            // サービス
            LinkedHashMap<String, String> serviceKeyAndValue = new LinkedHashMap<>();
            serviceKeyAndValue.put("02", c.getString(R.string.text_store_services_title_ladies));
            serviceKeyAndValue.put("01", c.getString(R.string.text_store_services_title_men));
            serviceKeyAndValue.put("03", c.getString(R.string.text_store_services_title_kids));
            serviceKeyAndValue.put("11", c.getString(R.string.text_store_services_title_embroidery));
            serviceKeyAndValue.put("12", c.getString(R.string.text_store_services_title_printing));
            serviceKeyAndValue.put("13", c.getString(R.string.text_store_services_title_cleat)+"・"+c.getString(R.string.text_store_services_title_nonskid));
            serviceKeyAndValue.put("22", c.getString(R.string.text_store_services_title_chinaUnionpay));
            serviceKeyAndValue.put("23", c.getString(R.string.text_store_services_title_dutyFree));
            serviceKeyAndValue.put("91", c.getString(R.string.text_store_services_title_membershipPiece));
            serviceKeyAndValue.put("92", c.getString(R.string.text_store_services_title_membershipCert));

            for (Map.Entry<String, String> entry : serviceKeyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                serviceModels.add(filterModel);
            }
        }

        filterModelsList.add(STORE_SEARCH_FREEWORD, freewordModels);
        filterModelsList.add(STORE_SEARCH_PREFECTURE, prefectureModels);
        filterModelsList.add(STORE_SEARCH_BRAND, brandModels);
        filterModelsList.add(STORE_SEARCH_SERVICE, serviceModels);
    }

    public ApiParams addFilterParams(ApiParams params) {
        for (int i=0; i<4; i++) {
            List<FilterModel> filterModels = getFilterModelsList().get(i);
            if (i==STORE_SEARCH_FREEWORD) {
                FilterModel freewordFilterModel = filterModels.get(0);
                if (freewordFilterModel.isSelect()) {
                    params.put("free_word", freewordFilterModel.getValue());
                }
                continue;
            }
            List<Object> selectedKeys = FilterModel.getSelectedKeys(filterModels);
            if (selectedKeys.size() < 1) {
                continue;
            }
            LOGD(TAG, "selected:"+i+ Arrays.toString(selectedKeys.toArray()));
            HashMap<String, List> dict = new HashMap<>();
            switch (i) {
                case StoreFilter.STORE_SEARCH_PREFECTURE:
                    dict.put("code", selectedKeys);
                    params.put("pref", dict);
                    break;
                case StoreFilter.STORE_SEARCH_BRAND:
                    dict.put("code", selectedKeys);
                    params.put("brand", dict);
                    break;
                case StoreFilter.STORE_SEARCH_SERVICE:
                    dict.put("class", selectedKeys);
                    params.put("service", dict);
                    break;
            }
        }
        return params;
    }
}
