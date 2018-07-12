package com.tabio.tabioapp.api;

import com.tabio.tabioapp.util.StringUtils;

/**
 * Created by san on 3/7/16.
 */
public final class ApiRoute {
    public static final String REGISTER = "apps/registrate_customer.php";
    public static final String REFRESH_TOKEN = "apps/issue_new_token.php";
    public static final String MIGRATION = "apps/member_migration.php";
    public static final String GET_USER = "apps/get_member_detail.php";
    public static final String UPDATE_USER = "apps/update_member_detail.php";
    public static final String GET_COUPON = "apps/get_coupon_info.php";
    public static final String GET_COORDINATE = "apps/search_coordinate_info.php";
    public static final String DEVICE_REGISTER = "apps/registrate_device_info.php";
    public static final String GET_HAS_UPDATE_STORES = "apps/get_store_update_info.php";
    public static final String GET_STORES = "apps/search_store_info.php";
    public static final String GET_STORE = "apps/get_store_detail_info.php";
    public static final String FAVORITE_STORE = "apps/registrate_favorite_store.php";
    public static final String GET_CHECKIN_HISTORIES = "apps/get_checkin_history.php";
    public static final String FAVORITE_COORDINATE = "apps/registrate_favorite_coordination.php";
    public static final String GET_ITEMS = "apps/search_products.php";
    public static final String FAVORITE_ITEM = "apps/registrate_favorite_products.php";
    public static final String GET_ITEM = "apps/get_product_info.php";
    public static final String GET_REVIEWS = "apps/get_review_info.php";
    public static final String CREATE_REVIEW = "apps/registrate_review_info.php";
    public static final String RESTOCK_REQUEST = "apps/restock_request.php";
    public static final String GET_ORDER_HISTORIES = "apps/get_order_list.php";
    public static final String GET_NEWS = "apps/get_notice.php";
    public static final String GET_TOPICS = "http://www.tabio.com/jp/corporate/news/topics/feed?media_type=app";
    public static final String GET_PIECE_GOT_HISTORIES = "apps/get_piece_history.php";
    public static final String GET_POINT_GOT_HISTORIES = "apps/get_point_history.php";
    public static final String GET_ORDER = "apps/get_order_detail.php";
    public static final String SEND_READ_ITEM = "apps/registrate_reading.php";
    public static final String SEND_CHECKIN = "apps/registrate_checkin_history.php";
    public static final String FORCE_UPDATE = "updates.json";

    public static final String CART_CRUD = "cartlink.php";

    public static final String DECIDE_GET_TIME = "time";
    public static final String DECIDE_LOGIN = "user/login/";
    public static final String DECIDE_TRACKING = "user/tracking";
    public static final String DECIDE_SHOP_VISIT = "scenario/event/shop-visit/fire/";
    public static final String DECIDE_NOTIFY = "user/permission";
    public static final String DECIDE_PUSH_OPEN = "notification/push/open/";

    public static final String WV_RECEIPT = "receipt/receipt_app.php";
    public static final String WV_RESET_PASSWORD = "jp/entry/reset_sendmail.php";
    public static final String WV_TERMS = "jp/app/membership/index.html";
    public static final String WV_CART = "jp/cart/index.php";
    public static final String WV_UPDATE_EMAIL = "jp/entry/reset_sendmail.php";
    public static final String WV_SECURITY_POLICY = "jp/app/privacy/index.html";
    public static final String WV_SPECIFIC = "jp/app/specific/index.html";
    public static final String WV_CONTACT = "jp/consultation/form/";
    public static final String WV_ITEM_RETURN = "/jp/order/return_index.php";
    public static final String WV_ITEM_CANCEL = "/jp/order/cancel_index.php";

    public static final String WV_FAQ_1 = "jp/app/faq/item/index.html"; // 商品について
    public static final String WV_FAQ_2 = "jp/app/faq/member/index.html";// Tabio会員について
    public static final String WV_FAQ_3 = "jp/app/faq/application/index.html";// Tabioアプリのご利用について
    public static final String WV_FAQ_4 = "jp/app/faq/store/index.html";// 店舗のご利用について
    public static final String WV_FAQ_5 = "jp/app/faq/online/index.html"; // Tabioオンラインストアのご利用について
    public static final String WV_FAQ_6 = "jp/app/faq/trouble/index.html"; // 不具合・トラブル
    public static final String WV_FAQ_8 = "jp/app/faq/company/index.html"; // 企業情報
    public static final String WV_FAQ_9 = "jp/app/faq/other/index.html"; // その他

    public static final String WV_LEAVE = "jp/mypage/login.php"; // 退会
    public static final String WV_MYPAGE = "jp/mypage/login.php"; // マイページ
}
