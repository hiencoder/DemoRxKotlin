package com.tabio.tabioapp.me;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.preference.EmailPasswordRegisterActivity;
import com.tabio.tabioapp.preference.EmailPasswordUpdateActivity;
import com.tabio.tabioapp.util.StringUtils;

import org.w3c.dom.Text;

import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

/**
 * Created by san on 5/16/16.
 */
public class MyAccountsDialog extends DialogFragment {
    public static final String TAG = makeLogTag(MyAccountsDialog.class);

    private OnMyAccountsDialogCallbacks callbacks;

    public MyAccountsDialog() {
    }

    public interface OnMyAccountsDialogCallbacks {
        void onFacebookLoginButtonClicked();
        void onTwitterLoginButtonClicked();
    }

    public static MyAccountsDialog newInstance() {
        Bundle args = new Bundle();
        Me self = AppController.getInstance().getSelf(false);
        args.putSerializable("self", self);
        MyAccountsDialog fragment = new MyAccountsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnMyAccountsDialogCallbacks) {
            callbacks = (OnMyAccountsDialogCallbacks) activity;
        } else {
            LOGE(TAG, "must implement OnMyAccountsDialogCallbacks");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppController.getInstance().sendGAScreen("会員ID確認ポップアップ");
        AppController.getInstance().decideTrack("570f29c899c3634a425af4b6");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View content = inflater.inflate(R.layout.my_accounts_dialog, null);

        Me self = AppController.getInstance().getSelf(false);
        TextView myId = (TextView) content.findViewById(R.id.my_id);
        TextView pin  = (TextView) content.findViewById(R.id.my_pin);
        myId.setText(self.getTabioId());
        pin.setText(self.getPinCode());

        View tabioIdPinSaveButton = (View) content.findViewById(R.id.tabioidpin_save_button);
        tabioIdPinSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view = new Intent(getActivity(), MyIdActivity.class);
                startActivity(view);
            }
        });

        View facebookLoginButton = (View) content.findViewById(R.id.facebook_login_button);
        Route fbRoute = self.getManager().getRoute(Route.FACEBOOK);
        if (fbRoute != null) {
            facebookLoginButton.setVisibility(View.GONE);
        } else {
            facebookLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callbacks != null) {
                        dismiss();
                        callbacks.onFacebookLoginButtonClicked();
                    }
                }
            });
        }
        View twitterLoginButton = (View) content.findViewById(R.id.twitter_login_button);
        Route twRoute = self.getManager().getRoute(Route.TWITTER);
        if (twRoute != null) {
            twitterLoginButton.setVisibility(View.GONE);
        } else {
            twitterLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callbacks != null) {
                        dismiss();
                        callbacks.onTwitterLoginButtonClicked();
                    }
                }
            });
        }
        View emailLoginButton = (View) content.findViewById(R.id.email_login_button);
        final Route emailRoute = self.getManager().getRoute(Route.EMAIL);
        emailLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailRoute != null) {
                    Intent view = new Intent(getActivity(), EmailPasswordUpdateActivity.class);
                    startActivity(view);
                } else {
                    AppController.getInstance().sendGAScreen("メールアドレス・パスワードでログイン");
                    AppController.getInstance().decideTrack("570f30c299c3634a425af517");
                    Intent view = new Intent(getActivity(), EmailPasswordRegisterActivity.class);
                    startActivity(view);
                }
            }
        });



        View okButton = (View) content.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        View closeButton = (View) content.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(content);
        return builder.create();
    }
}
