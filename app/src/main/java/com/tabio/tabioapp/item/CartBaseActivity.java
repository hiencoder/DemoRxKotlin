package com.tabio.tabioapp.item;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.api.cart.CartApiParams;
import com.tabio.tabioapp.api.cart.CartApiRequest;
import com.tabio.tabioapp.api.cart.CartApiResponse;
import com.tabio.tabioapp.model.Item;
import com.tabio.tabioapp.ui.BaseActivity;
import com.tabio.tabioapp.util.ViewUtils;
import com.tabio.tabioapp.util.animation.EaseInSineInterpolator;
import com.tabio.tabioapp.web.WebActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observer;
import rx.schedulers.Schedulers;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 4/12/16.
 */
public abstract class CartBaseActivity extends BaseActivity {
    public static final String TAG = makeLogTag(CartBaseActivity.class);

    private CartApiRequest cRequest;

    protected TextView cartCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cRequest = new CartApiRequest(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProductsCountInCart();
    }

    protected void getProductsCountInCart() {
        CartApiParams params = new CartApiParams(CartApiParams.GET_CART, null);
        this.cRequest.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<CartApiResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(final CartApiResponse cartApiResponse) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                LOGD(TAG, "products count in cart"+cartApiResponse.getProductsCount());
                                onUpdatedCart(cartApiResponse.getProductsCount());
                            }
                        });
                    }
                });
    }

    protected void animateViewBottomUp(final View targetView) {
        LOGD(TAG, "animateViewBottomUp");
        View contentView = (View) findViewById(R.id.main_content);
        View oldView = contentView.findViewById(targetView.getId());
        if (oldView != null) {
            animateViewBottomOut(oldView, true);
        }
        targetView.setTranslationY(ViewUtils.getPixelFromDp(this, 64));
        ((ViewGroup) contentView).addView(targetView);

        ObjectAnimator anim = ObjectAnimator.ofFloat(targetView, "translationY", targetView.getTranslationY(), 0);
        anim.setDuration(500);
        anim.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateViewBottomOut(targetView, false);
            }
        }, 2000);
    }

    private void animateViewBottomOut(final View targetView, boolean now) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(targetView, "translationY", 0, ViewUtils.getPixelFromDp(this, 64));
        anim.setDuration(now?0:300);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                if (targetView != null && targetView.getParent() != null) {
                    ((ViewManager) targetView.getParent()).removeView(targetView);
                }
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
        anim.start();
    }

    protected void onAddCartSuccess(Item item){
        View contentView = (View) findViewById(R.id.main_content);
        final View targetView = (View) LayoutInflater.from(this).inflate(R.layout.add_cart_success_view, (ViewGroup) contentView, false);
        CircleImageView itemImg = (CircleImageView) targetView.findViewById(R.id.addcart_success_item_img);
        Picasso.with(this)
                .load(item.getAsset().getCurrentLineup().getImgUrl())
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .into(itemImg);
        animateViewBottomUp(targetView);
        Button checkCartButton = (Button) targetView.findViewById(R.id.addcart_success_check_button);
        checkCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCart();
            }
        });
    }

    protected void addCart(final Item item) {
        CartApiParams params = new CartApiParams(CartApiParams.ADD_CART, item);
        showNetworkProgress();
        this.cRequest.run(params)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<CartApiResponse>() {
                    @Override
                    public void onCompleted() {
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        AppController.getInstance().showApiErrorAlert(CartBaseActivity.this, null);
                        hideProgress();
                    }

                    @Override
                    public void onNext(final CartApiResponse cartApiResponse) {
                        if (cartApiResponse.hasError()) {
                            LOGE(TAG, cartApiResponse.getErrorMessage());
                            AppController.getInstance().showApiErrorAlert(CartBaseActivity.this, cartApiResponse.getError());
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                getProductsCountInCart();
                                onAddCartSuccess(item);
                            }
                        });

                    }
                });
    }

    protected void onUpdatedCart(int productsCountInCart) {
        if (this.cartCount == null) {
            return;
        }
        this.cartCount.setVisibility(productsCountInCart > 0 ? View.VISIBLE : View.GONE);
        this.cartCount.setText(String.valueOf(productsCountInCart));
    }

    protected void checkCart() {
        Intent view = new Intent(this, WebActivity.class);
        view.putExtra("url", BuildConfig.CART_BASE_URL+ApiRoute.WV_CART+"?token="+self.getToken());
        view.putExtra("cart", true);
        AppController.getInstance().sendGAScreen("カート");
        AppController.getInstance().decideTrack("570f2fef99c3634a425af500");
        startActivity(view);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_cart);
        MenuItemCompat.setActionView(item, R.layout.menu_cart);
        this.cartCount = (TextView) item.getActionView().findViewById(R.id.cart_count);
        item.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCart();
            }
        });
        return true;
    }
}
