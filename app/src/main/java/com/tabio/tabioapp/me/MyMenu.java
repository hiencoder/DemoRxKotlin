package com.tabio.tabioapp.me;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;

import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.coordinate.CoordinatesFragment;
import com.tabio.tabioapp.item.ItemsFragment;
import com.tabio.tabioapp.item.review.ReviewsFragment;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.news.NewsListFragment;
import com.tabio.tabioapp.order.OrderHistoriesFragment;
import com.tabio.tabioapp.piece.PieceGotHistoriesFragment;
import com.tabio.tabioapp.piece.PointGotHistoriesFragment;
import com.tabio.tabioapp.store.StoresFragment;
import com.tabio.tabioapp.topics.TopicsFragment;

import java.io.Serializable;

/**
 * Created by pixie3 on 3/12/16.
 */
public class MyMenu implements Serializable {

    private int myMenuId;
    private String name;
    private int objectsCount = 0;

    public static final int MYMENU_ID_TOPICS                   = 0; //トピックス
    public static final int MYMENU_ID_NEWS                     = 1;  //お知らせ
    public static final int MYMENU_ID_FAVORITE_COORDINATES     = 2;  //お気に入りコーデ
    public static final int MYMENU_ID_FAVORITE_ITEMS           = 3;  //お気に入り商品
    public static final int MYMENU_ID_FAVORITE_STORES          = 4;  //お気に入り店舗
    public static final int MYMENU_ID_REVIEWS                  = 5;  //レビュー
    public static final int MYMENU_ID_ITEM_ORDER_HISTORIES     = 6;  //購入履歴
    public static final int MYMENU_ID_ITEM_READ_HISTORIES      = 7;  //閲覧履歴
    public static final int MYMENU_ID_PIECE_GET_HISTORIES      = 8;  //ピース取得履歴
    public static final int MYMENU_ID_POINT_GET_HISTORIES      = 9;  //ポイント取得履歴

    public static final int[] MYMENU_TAB_IDS = {
            MYMENU_ID_TOPICS, MYMENU_ID_NEWS,
            MYMENU_ID_FAVORITE_COORDINATES, MYMENU_ID_FAVORITE_ITEMS,
            MYMENU_ID_FAVORITE_STORES, MYMENU_ID_REVIEWS,
            MYMENU_ID_ITEM_ORDER_HISTORIES, MYMENU_ID_ITEM_READ_HISTORIES
            /*MYMENU_ID_COUPONS*/, MYMENU_ID_PIECE_GET_HISTORIES,
            MYMENU_ID_POINT_GET_HISTORIES
    };
    @IntDef({
            MYMENU_ID_FAVORITE_ITEMS, MYMENU_ID_FAVORITE_COORDINATES,
            MYMENU_ID_FAVORITE_STORES, MYMENU_ID_REVIEWS,
            MYMENU_ID_ITEM_READ_HISTORIES, MYMENU_ID_ITEM_ORDER_HISTORIES,
            MYMENU_ID_NEWS, MYMENU_ID_TOPICS,
            /*MYMENU_ID_COUPONS,*/ MYMENU_ID_PIECE_GET_HISTORIES,
            MYMENU_ID_POINT_GET_HISTORIES
    })
    public @interface MyMenuId{}


    public MyMenu(@MyMenuId int myMenuId, Context c, Me me, int objectsCount) {
        this.myMenuId = myMenuId;
        this.objectsCount = objectsCount;
        Resources resources = c.getResources();
        ApiParams params = null;

        // お気に入り商品
        if (myMenuId == MYMENU_ID_FAVORITE_ITEMS) {
            setName(resources.getString(R.string.text_mypage_menu_title_favoriteItems));
        }

        // お気に入りコーデ
        else if (myMenuId == MYMENU_ID_FAVORITE_COORDINATES) {
            setName(resources.getString(R.string.text_mypage_menu_title_favoriteCoordinates));
        }

        // お気に入り店舗
        else if (myMenuId == MYMENU_ID_FAVORITE_STORES) {
            setName(resources.getString(R.string.text_mypage_menu_title_favoriteStores));
        }

        // レビュー
        else if (myMenuId == MYMENU_ID_REVIEWS) {
            setName(resources.getString(R.string.text_mypage_menu_title_reviews));
        }

        // 閲覧履歴
        else if (myMenuId == MYMENU_ID_ITEM_READ_HISTORIES) {
            setName(resources.getString(R.string.text_readHistory));
        }

        // 購入履歴
        else if (myMenuId == MYMENU_ID_ITEM_ORDER_HISTORIES) {
            setName(resources.getString(R.string.text_orderHistory));
        }

        // お知らせ
        else if (myMenuId == MYMENU_ID_NEWS) {
            setName(resources.getString(R.string.text_news));
        }

        // トピックス
        else if (myMenuId == MYMENU_ID_TOPICS) {
            setName(resources.getString(R.string.text_topics));
        }

        // ピース取得履歴
        else if (myMenuId == MYMENU_ID_PIECE_GET_HISTORIES) {
            setName(resources.getString(R.string.text_mypage_menu_title_pieceGetHistories));
        }

        // ポイント取得履歴
        else if (myMenuId == MYMENU_ID_POINT_GET_HISTORIES) {
            setName(resources.getString(R.string.text_mypage_menu_title_pointGetHistories));
        }
    }


    @MyMenuId
    public int getMyMenuId() {
        return myMenuId;
    }

    public void setMyMenuId(@MyMenuId int myMenuId) {
        this.myMenuId = myMenuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getObjectsCount() {
        return objectsCount;
    }

    public void setObjectsCount(int objectsCount) {
        this.objectsCount = objectsCount;
    }
}
