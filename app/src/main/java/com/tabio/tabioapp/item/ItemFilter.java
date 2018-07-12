package com.tabio.tabioapp.item;

import android.content.Context;
import android.os.Parcel;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.model.Filter;
import com.tabio.tabioapp.filter.FilterModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/16/15.
 */
public class ItemFilter extends Filter implements Serializable {
    public static final String TAG = makeLogTag(ItemFilter.class);

    public static final int ITEM_SEARCH_FREEWORD = 0;
    public static final int ITEM_SEARCH_GENDER = 1;
    public static final int ITEM_SEARCH_TYPE = 2;
    public static final int ITEM_SEARCH_LENGTH = 3;
    public static final int ITEM_SEARCH_SIZE = 4;
    public static final int ITEM_SEARCH_TOETYPE = 5;
    public static final int ITEM_SEARCH_MATERIAL = 6;
    public static final int ITEM_SEARCH_FUNCTION = 7;
    public static final int ITEM_SEARCH_COLOR = 8;
    public static final int ITEM_SEARCH_PATTERN = 9;
    public static final int ITEM_SEARCH_BRAND = 10;
    public static final int ITEM_SEARCH_PRICE = 11;

    public ItemFilter(Context c) {
        super(FILTER_TYPE_ITEM);

        titles = new String[]{c.getString(R.string.text_search_menu_freeword), c.getString(R.string.text_search_menu_gender),
                c.getString(R.string.text_search_menu_type),c.getString(R.string.text_search_menu_length),
                c.getString(R.string.text_search_menu_size),c.getString(R.string.text_search_menu_toeType),
                c.getString(R.string.text_item_title_material),c.getString(R.string.text_search_menu_function),
                c.getString(R.string.text_search_menu_color),c.getString(R.string.text_search_menu_pattern),
                c.getString(R.string.text_search_menu_brand),c.getString(R.string.text_search_menu_price)};
        listTitles = titles;

        List<FilterModel> freewordModels = new ArrayList<>();//フリーワード
        List<FilterModel> genderModels   = new ArrayList<>();//性別
        List<FilterModel> typeModels     = new ArrayList<>();//種類
        List<FilterModel> lengthModels   = new ArrayList<>();//丈
        List<FilterModel> sizeModels     = new ArrayList<>();//サイズ
        List<FilterModel> toeTypeModels  = new ArrayList<>();//足指タイプ
        List<FilterModel> materialModels = new ArrayList<>();//素材
        List<FilterModel> functionModels = new ArrayList<>();//機能
        List<FilterModel> colorModels    = new ArrayList<>();//色
        List<FilterModel> patternModels  = new ArrayList<>();//柄
        List<FilterModel> brandModels    = new ArrayList<>();//ブランド
        List<FilterModel> priceModels    = new ArrayList<>();//価格帯

        // フリーワード
        {
            FilterModel filterModel = new FilterModel("freeword", "", false);
            freewordModels.add(filterModel);
        }

        // 性別:gender
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_store_services_title_men));
            keyAndValue.put("02", c.getString(R.string.text_store_services_title_ladies));
            keyAndValue.put("03", c.getString(R.string.text_store_services_title_kids));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                genderModels.add(filterModel);
            }
        }

        // 種類:type
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_type_socks));
            keyAndValue.put("02", c.getString(R.string.text_item_type_footsies));
            keyAndValue.put("03", c.getString(R.string.text_item_type_sandalSocks));
            keyAndValue.put("04", c.getString(R.string.text_item_type_tights));
            keyAndValue.put("05", c.getString(R.string.text_item_type_stockings));
            keyAndValue.put("06", c.getString(R.string.text_item_type_leggings));
            keyAndValue.put("07", c.getString(R.string.text_item_type_stirrupLeggings));
            keyAndValue.put("08", c.getString(R.string.text_item_type_legWarmers));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                typeModels.add(filterModel);
            }
        }

        // 丈:length
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_type_footsies));
            keyAndValue.put("02", c.getString(R.string.text_item_length_sneakersSocks));
            keyAndValue.put("03", c.getString(R.string.text_item_length_shortAnkleLengthSocks));
            keyAndValue.put("04", c.getString(R.string.text_item_length_crewSocks));
            keyAndValue.put("05", c.getString(R.string.text_item_length_highCutHighSocks));
            keyAndValue.put("06", c.getString(R.string.text_item_length_kneeHighSocks));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                lengthModels.add(filterModel);
            }
        }

        // サイズ:size
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_size_0709));
            keyAndValue.put("02", c.getString(R.string.text_item_size_1012));
            keyAndValue.put("03", c.getString(R.string.text_item_size_1315));
            keyAndValue.put("04", c.getString(R.string.text_item_size_1618));
            keyAndValue.put("05", c.getString(R.string.text_item_size_1921));
            keyAndValue.put("06", c.getString(R.string.text_item_size_2223));
            keyAndValue.put("07", c.getString(R.string.text_item_size_2324));
            keyAndValue.put("08", c.getString(R.string.text_item_size_2425));
            keyAndValue.put("09", c.getString(R.string.text_item_size_2526));
            keyAndValue.put("10", c.getString(R.string.text_item_size_2627));
            keyAndValue.put("11", c.getString(R.string.text_item_size_2728));
            keyAndValue.put("12", c.getString(R.string.text_item_size_2829));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                sizeModels.add(filterModel);
            }
        }

        // 足指タイプ:toeType
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_toe_type_toeSocks));
            keyAndValue.put("02", c.getString(R.string.text_item_toe_type_tabiSocks));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                toeTypeModels.add(filterModel);
            }
        }

        // 素材:material
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_material_100Cotton));
            keyAndValue.put("02", c.getString(R.string.text_item_material_silk));
            keyAndValue.put("03", c.getString(R.string.text_item_material_linen));
            keyAndValue.put("04", c.getString(R.string.text_item_material_wool));
            keyAndValue.put("05", c.getString(R.string.text_item_material_cupra));
            keyAndValue.put("06", c.getString(R.string.text_item_material_cashmere));
            keyAndValue.put("07", c.getString(R.string.text_item_material_angora));
            keyAndValue.put("08", c.getString(R.string.text_item_material_organicCotton));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                materialModels.add(filterModel);
            }
        }

        // 機能:function
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_function_warm));
            keyAndValue.put("02", c.getString(R.string.text_item_function_compression));
            keyAndValue.put("03", c.getString(R.string.text_item_function_keepDryDeodorant));
            keyAndValue.put("04", c.getString(R.string.text_item_function_forDrySensitiveSkin));
            keyAndValue.put("05", c.getString(R.string.text_item_function_forTiredLegs));
            keyAndValue.put("06", c.getString(R.string.text_item_function_nonElasticSoftTop));
            keyAndValue.put("07", c.getString(R.string.text_store_services_title_cleat));
            keyAndValue.put("08", c.getString(R.string.text_item_function_sport));
            keyAndValue.put("09", c.getString(R.string.text_item_function_forBetterSleep));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                functionModels.add(filterModel);
            }
        }

        // 色
        {

            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_color_white));
            keyAndValue.put("02", c.getString(R.string.text_item_color_black));
            keyAndValue.put("03", c.getString(R.string.text_item_color_grey));
            keyAndValue.put("04", c.getString(R.string.text_item_color_beige));
            keyAndValue.put("05", c.getString(R.string.text_item_color_brown));
            keyAndValue.put("06", c.getString(R.string.text_item_color_pink));
            keyAndValue.put("07", c.getString(R.string.text_item_color_red));
            keyAndValue.put("08", c.getString(R.string.text_item_color_orange));
            keyAndValue.put("09", c.getString(R.string.text_item_color_yellow));
            keyAndValue.put("10", c.getString(R.string.text_item_color_green));
            keyAndValue.put("11", c.getString(R.string.text_item_color_blue));
            keyAndValue.put("12", c.getString(R.string.text_item_color_navy));
            keyAndValue.put("13", c.getString(R.string.text_item_color_purple));

            int[] colorIds = new int[]{
                    R.color.white,R.color.black,R.color.grayLight500,
                    R.color.beige,R.color.brown,R.color.pinkLight400,
                    R.color.redDark100,R.color.orangeLight100,R.color.yellowLight100,
                    R.color.greenLight100,R.color.blueDark100,R.color.navy,
                    R.color.purpleDark100
            };

            int i = 0;
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false, colorIds[i]);
                colorModels.add(filterModel);
                i++;
            }
        }

        // 柄
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_pattern_plain));
            keyAndValue.put("02", c.getString(R.string.text_item_pattern_glittery));
            keyAndValue.put("03", c.getString(R.string.text_item_pattern_ribbed));
            keyAndValue.put("04", c.getString(R.string.text_item_pattern_borders));
            keyAndValue.put("05", c.getString(R.string.text_item_pattern_argyleDiamonds));
            keyAndValue.put("06", c.getString(R.string.text_item_pattern_dotsPolkaDots));
            keyAndValue.put("07", c.getString(R.string.text_item_pattern_stars));
            keyAndValue.put("08", c.getString(R.string.text_item_pattern_floral));
            keyAndValue.put("09", c.getString(R.string.text_item_pattern_stripes));
            keyAndValue.put("10", c.getString(R.string.text_item_pattern_checks));
            keyAndValue.put("11", c.getString(R.string.text_item_pattern_ribbons));
            keyAndValue.put("12", c.getString(R.string.text_item_pattern_hearts));
            keyAndValue.put("13", c.getString(R.string.text_item_pattern_jacquard));
            keyAndValue.put("14", c.getString(R.string.text_item_pattern_meshRaschelLace));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                patternModels.add(filterModel);
            }
        }

        // ブランド
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_store_brand_kutsushitaya));
            keyAndValue.put("02", c.getString(R.string.text_store_brand_tabio));
            keyAndValue.put("03", c.getString(R.string.text_store_brand_tabio_men));
            keyAndValue.put("04", c.getString(R.string.text_item_brand_tabioSports));
            keyAndValue.put("05", c.getString(R.string.text_item_brand_tabioLegLabo));
            keyAndValue.put("06", c.getString(R.string.text_item_brand_tabioLuxe));
            keyAndValue.put("07", c.getString(R.string.text_item_brand_tabioArts));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                brandModels.add(filterModel);
            }
        }

        // 価格帯
        {
            LinkedHashMap<String, String> keyAndValue = new LinkedHashMap<>();
            keyAndValue.put("01", c.getString(R.string.text_item_price_500));
            keyAndValue.put("02", c.getString(R.string.text_item_price_5001000));
            keyAndValue.put("03", c.getString(R.string.text_item_price_10001500));
            keyAndValue.put("04", c.getString(R.string.text_item_price_15002000));
            keyAndValue.put("05", c.getString(R.string.text_item_price_20002500));
            keyAndValue.put("06", c.getString(R.string.text_item_price_25003000));
            keyAndValue.put("07", c.getString(R.string.text_item_price_30003500));
            keyAndValue.put("08", c.getString(R.string.text_item_price_35004000));
            keyAndValue.put("09", c.getString(R.string.text_item_price_40004500));
            keyAndValue.put("10", c.getString(R.string.text_item_price_45005000));
            keyAndValue.put("11", c.getString(R.string.text_item_price_5000));
            for (Map.Entry<String, String> entry : keyAndValue.entrySet()) {
                FilterModel filterModel = new FilterModel(entry.getKey(), entry.getValue(), false);
                priceModels.add(filterModel);
            }
        }

        filterModelsList.add(freewordModels);
        filterModelsList.add(genderModels);
        filterModelsList.add(typeModels);
        filterModelsList.add(lengthModels);
        filterModelsList.add(sizeModels);
        filterModelsList.add(toeTypeModels);
        filterModelsList.add(materialModels);
        filterModelsList.add(functionModels);
        filterModelsList.add(colorModels);
        filterModelsList.add(patternModels);
        filterModelsList.add(brandModels);
        filterModelsList.add(priceModels);


    }

    public ApiParams addFilterParams(ApiParams params) {
        if (getSortFilterModel() != null) {
            params.put("order", getSortFilterModel().getSelectedSortType());
        }
        for (int i=0; i<filterModelsList.size(); i++) {
            List<FilterModel> filterModels = getFilterModelsList().get(i);

            if (i==ITEM_SEARCH_FREEWORD) {
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
            HashMap<String, List> dict = new HashMap<>();
            switch (i) {
                case ITEM_SEARCH_GENDER:
                    dict.put("type", selectedKeys);
                    params.put("target", dict);
                    break;
                case ITEM_SEARCH_TYPE:
                    dict.put("type", selectedKeys);
                    params.put("classify", dict);
                    break;
                case ITEM_SEARCH_LENGTH:
                    dict.put("type", selectedKeys);
                    params.put("length", dict);
                    break;
                case ITEM_SEARCH_SIZE:
                    dict.put("type", selectedKeys);
                    params.put("size", dict);
                    break;
                case ITEM_SEARCH_TOETYPE:
                    dict.put("type", selectedKeys);
                    params.put("type", dict);
                    break;
                case ITEM_SEARCH_MATERIAL:
                    dict.put("type", selectedKeys);
                    params.put("material", dict);
                    break;
                case ITEM_SEARCH_FUNCTION:
                    dict.put("type", selectedKeys);
                    params.put("function", dict);
                    break;
                case ITEM_SEARCH_COLOR:
                    dict.put("type", selectedKeys);
                    params.put("color", dict);
                    break;
                case ITEM_SEARCH_PATTERN:
                    dict.put("type", selectedKeys);
                    params.put("pattern", dict);
                    break;
                case ITEM_SEARCH_BRAND:
                    dict.put("type", selectedKeys);
                    params.put("brand", dict);
                    break;
                case ITEM_SEARCH_PRICE:
                    dict.put("type", selectedKeys);
                    params.put("price", dict);
                    break;
            }
        }
        return params;
    }
}
