package com.tabio.tabioapp.coupon;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Coupon;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 2/23/16.
 */
public class CouponDetailDialog extends DialogFragment {
    public static final String TAG = makeLogTag(CouponDetailDialog.class);

    public CouponDetailDialog() {
    }

    public static CouponDetailDialog newInstance(Coupon coupon) {
        CouponDetailDialog fragment = new CouponDetailDialog();
        Bundle args = new Bundle();
//        args.putParcelable("coupon", coupon);
        args.putSerializable("coupon", coupon);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppController.getInstance().sendGAScreen("クーポン詳細ポップアップ");
        AppController.getInstance().decideTrack("570f2a1999c3634a425af4c0");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View content = inflater.inflate(R.layout.coupon_detail_dialog, null);

        Coupon coupon = (Coupon) getArguments().getSerializable("coupon");
        TextView description = (TextView) content.findViewById(R.id.description);
        TextView caution = (TextView) content.findViewById(R.id.caution);
        description.setText(coupon.getStores());
        caution.setText(coupon.getComment());

        if (coupon.getStores() != null && !coupon.getStores().isEmpty() &&
                coupon.getComment() != null && !coupon.getComment().isEmpty()) {
            View margin = (View) content.findViewById(R.id.margin);
            margin.setVisibility(View.VISIBLE);
        }

        content.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CouponDetailDialog.this.dismiss();
//                getActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .remove(CouponDetailDialog.this)
//                        .commit();
            }
        });
        builder.setView(content);
        return builder.create();
    }
}
