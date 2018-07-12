package com.tabio.tabioapp.me;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tabio.tabioapp.AppController;
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

import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 12/21/15.
 */
public class MyMenuPagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = makeLogTag(MyMenuPagerAdapter.class);

    private Context context;
    private List<MyMenu> myMenus;
    private Me me;

    public MyMenuPagerAdapter(FragmentManager fm, Context c, List<MyMenu> myMenus) {
        super(fm);
        this.myMenus = myMenus;
        this.context = c;
        this.me = AppController.getInstance().getSelf(false);
    }

    @Override
    public Fragment getItem(int position) {
        return getFragment(position);
    }

    @Override
    public int getCount() {
        return myMenus.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return myMenus.get(position).getName();
    }

    public Fragment getFragment(int position) {
        ApiParams params = null;
        MyMenu myMenu = this.myMenus.get(position);
        switch (position) {
            case MyMenu.MYMENU_ID_FAVORITE_ITEMS:
                AppController.getInstance().sendGAScreen("お気に入り商品一覧");
                params = new ApiParams(this.me, true, ApiRoute.GET_ITEMS);
                params.put("search", 1);
                return ItemsFragment.newInstance(params, myMenu,
                        this.context.getString(R.string.error_text_nodata,this.context.getString(R.string.text_mypage_menu_title_favoriteItems)), null);
            case MyMenu.MYMENU_ID_FAVORITE_COORDINATES:
                AppController.getInstance().sendGAScreen("お気に入りコーデ一覧");
                params = new ApiParams(me, true, ApiRoute.GET_COORDINATE);
                params.put("search", 1);
                return CoordinatesFragment.newInstance(params, myMenu);
            case MyMenu.MYMENU_ID_FAVORITE_STORES:
                AppController.getInstance().sendGAScreen("お気に入り店舗一覧");
                params = new ApiParams(me, true, ApiRoute.GET_STORES);
                params.put("search", 1);
                return StoresFragment.newInstance(params, false, false, myMenu, null);
            case MyMenu.MYMENU_ID_REVIEWS:
                AppController.getInstance().sendGAScreen("レビュー一覧");
                params = new ApiParams(me, true, ApiRoute.GET_REVIEWS);
                params.put("search", 1);
                return ReviewsFragment.newInstance(true, false, params, myMenu);
            case MyMenu.MYMENU_ID_ITEM_READ_HISTORIES:
                AppController.getInstance().sendGAScreen("閲覧履歴一覧");
                params = new ApiParams(me, true, ApiRoute.GET_ITEMS);
                params.put("search", 2);
                return ItemsFragment.newInstance(params, myMenu, this.context.getString(R.string.error_text_nodata, this.context.getString(R.string.text_readHistory)), null);
            case MyMenu.MYMENU_ID_ITEM_ORDER_HISTORIES:
                AppController.getInstance().sendGAScreen("購入履歴一覧");
                params = new ApiParams(me, true, ApiRoute.GET_ORDER_HISTORIES);
                params.put("url", ApiRoute.GET_ORDER_HISTORIES);
                return OrderHistoriesFragment.newInstance(params, myMenu);
            case MyMenu.MYMENU_ID_NEWS:
                AppController.getInstance().sendGAScreen("お知らせ一覧");
                params = new ApiParams(me, true, ApiRoute.GET_NEWS);
                return NewsListFragment.newInstance(params, myMenu);
            case MyMenu.MYMENU_ID_TOPICS:
                AppController.getInstance().sendGAScreen("トピックス一覧");
                return TopicsFragment.newInstance(myMenu);
            case MyMenu.MYMENU_ID_PIECE_GET_HISTORIES:
                AppController.getInstance().sendGAScreen("ピース取得履歴一覧");
                return PieceGotHistoriesFragment.newInstance(myMenu);
            case MyMenu.MYMENU_ID_POINT_GET_HISTORIES:
                AppController.getInstance().sendGAScreen("ポイント取得履歴一覧");
                return PointGotHistoriesFragment.newInstance(myMenu);
            default:
                return null;
        }
    }

}