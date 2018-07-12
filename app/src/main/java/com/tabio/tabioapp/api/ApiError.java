package com.tabio.tabioapp.api;

import android.support.annotation.IntDef;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 3/20/16.
 */
public class ApiError {
    public static final String TAG = makeLogTag(ApiError.class);

    /*
    1:不正なフォーマットです。パラメータ書式が仕様外。
    2:不正な値です。指定された値が範囲外。
    3:未登録の Tabio ID です。指定されたTabio IDが登録されていいない。
    4:無効なトークンです。指定されたトークンの有効期間が過ぎている。
    5:未登録のトークンです。Tabio IDは合致しているがトークンが不一致である。
    6:無効な会員です。会員状態が「停止」になっている。
    7:無効な会員です。会員状態が「退会」になっている。
    91:データの取得に失敗しました。
    92:データの登録に失敗しました。
    93:データの更新に失敗しました。
    94:処理がタイムアウトしました。
    99:メンテナンス中です。
     */

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CODE_1, CODE_2, CODE_3, CODE_4, CODE_5, CODE_6, CODE_7, CODE_40X, CODE_50X, UNDEFINED, CONNECT_ERROR,
    CODE_91, CODE_92, CODE_93, CODE_94, CODE_99})
    public @interface ErrorCode {}
    public static final int CODE_1 = 1;
    public static final int CODE_2 = 2;
    public static final int CODE_3 = 3;
    public static final int CODE_4 = 4;
    public static final int CODE_5 = 5;
    public static final int CODE_6 = 6;
    public static final int CODE_7 = 7;
    public static final int CODE_91 = 91;
    public static final int CODE_92 = 92;
    public static final int CODE_93 = 93;
    public static final int CODE_94 = 94;
    public static final int CODE_99 = 99;
    public static final int CODE_40X = 404;
    public static final int CODE_50X = 500;
    public static final int UNDEFINED = 1000;
    public static final int CONNECT_ERROR = 404;

    private boolean error = false;
    private int code;
    private String message;

    public static final ApiError newNetworkErrorApiError() {
        ApiError networkError = new ApiError(null);
        networkError.setCode(CONNECT_ERROR);
        networkError.setError(true);
        networkError.setMessage(AppController.getInstance().getString(R.string.error_text_connection));
        return networkError;
    }

    public static final ApiError newUndefinedApiError() {
        ApiError error = new ApiError(null);
        error.setCode(UNDEFINED);
        error.setError(true);
        error.setMessage(AppController.getInstance().getString(R.string.error_text_undefined));
        return error;
    }

    public ApiError(JSONObject json) {
        setMessage(AppController.getInstance().getApplicationContext().getString(R.string.error_text_undefined));
        if (json == null) {
            this.error = true;
            this.code = UNDEFINED;
            return;
        }
        if (json.has("errors")) {
            try {
                JSONArray errors = json.getJSONArray("errors");
                if (errors.length() > 0) {
                    this.error = true;
                    JSONObject errorJson = errors.getJSONObject(0);
                    int code = errorJson.getInt("code");
                    switch (code) {
                        case CODE_1:setCode(CODE_1);break;
                        case CODE_2:setCode(CODE_2);break;
                        case CODE_3:setCode(CODE_3);break;
                        case CODE_4:setCode(CODE_4);break;
                        case CODE_5:setCode(CODE_5);break;
                        case CODE_6:setCode(CODE_6);setMessage(
                                AppController.getInstance().getString(R.string.error_suspend)
                        );break;
                        case CODE_7:setCode(CODE_7);break;
                        case CODE_91:setCode(CODE_91);break;
                        case CODE_92:setCode(CODE_92);break;
                        case CODE_93:setCode(CODE_93);break;
                        case CODE_94:setCode(CODE_94);break;
                        case CODE_99:setCode(CODE_99);break;
                        default:
                            setCode(code);
                            break;
                    }
//                    setMessage(errorJson.getString("message"));
                    LOGE(TAG, "ERROR CODE:"+code);
                    LOGE(TAG, "ERROR MESSAGE:"+getMessage());
                    if (errorJson.has("info")) {
                        LOGE(TAG, "ERROR INFO:" + errorJson.getString("info"));
                    }
                } else {
                    this.error = false;
                }
            } catch (JSONException e) {
                LOGE(TAG, e.getLocalizedMessage());
                e.printStackTrace();
                this.error = true;
                setCode(UNDEFINED);
            }
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
