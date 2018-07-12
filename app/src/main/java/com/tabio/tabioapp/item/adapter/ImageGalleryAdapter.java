package com.tabio.tabioapp.item.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.R;

import java.util.List;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 1/28/16.
 */
public class ImageGalleryAdapter extends PagerAdapter {
    public static final String TAG = makeLogTag(ImageGalleryAdapter.class);

    private Context context;
    private List<String> imgUrls;

    private static final int GALLERY_IMG_HEIGHT = 340;

    public ImageGalleryAdapter(Context context, List<String> imgUrls) {
        this.context = context;
        this.imgUrls = imgUrls;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView img = new ImageView(this.context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, GALLERY_IMG_HEIGHT);
        img.setLayoutParams(params);
        Picasso.with(this.context)
                .load(imgUrls.get(position))
                .placeholder(R.drawable.placeholder_white)
                .error(R.drawable.placeholder_white)
                .fit()
                .centerCrop()
                .into(img);
        container.addView(img);
        return img;
    }

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public void add(String imgUrl) {
        this.imgUrls.add(imgUrl);
    }

    @Override
    public int getCount() {
        return imgUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
