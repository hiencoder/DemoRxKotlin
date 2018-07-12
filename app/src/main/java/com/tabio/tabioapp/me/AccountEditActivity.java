package com.tabio.tabioapp.me;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiParams;
import com.tabio.tabioapp.login.BaseLoginActivity;
import com.tabio.tabioapp.model.Me;
import com.tabio.tabioapp.model.Profile;
import com.tabio.tabioapp.model.Route;
import com.tabio.tabioapp.ui.RecyclerItemDividerDecoration;
import com.tabio.tabioapp.util.BitmapResizer;
import com.tabio.tabioapp.util.CameraUtils;
import com.tabio.tabioapp.util.DateUtils;
import com.tabio.tabioapp.util.ImageUtils;
import com.tabio.tabioapp.util.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.mixi.compatibility.android.media.ExifInterfaceCompat;
import jp.mixi.compatibility.android.provider.MediaStoreCompat;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class AccountEditActivity extends BaseLoginActivity {
    private static final String TAG = makeLogTag(AccountEditActivity.class);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACCOUNT_EDIT_PREVIEW, ACCOUNT_EDIT})
    public @interface AccountEditMode {
    }

    public static final int ACCOUNT_EDIT_PREVIEW = 0;
    public static final int ACCOUNT_EDIT = 1;

    public int accountEditMode;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private InputMethodManager inputMethodManager;
    private static MediaStoreCompat mediaStoreCompat;
    private String preparedUri = "";

    private AccountEditBaseAdapter adapter;
    private String tmpNickname;
    private String tmpBirthday;
    private String tmpGender;

    private static final int ICON_CHOOSER_REQUEST_CODE = 1011;
    private static final int COVER_CHOOSER_REQUEST_CODE = 1012;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.button_editAccount);
        ButterKnife.bind(this);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        accountEditMode = getIntent().getIntExtra("mode", ACCOUNT_EDIT_PREVIEW);

        this.adapter = new AccountEditBaseAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (this.accountEditMode == ACCOUNT_EDIT) {
            AppController.getInstance().sendGAScreen("アカウント編集詳細");
            AppController.getInstance().decideTrack("570f305499c3634a425af509");
            recyclerView.addItemDecoration(new RecyclerItemDividerDecoration(this, R.drawable.line_divider));
        } else {
            AppController.getInstance().sendGAScreen("アカウント編集");
            AppController.getInstance().decideTrack("570f306699c3634a425af50b");
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("preparedUri", this.preparedUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.preparedUri = savedInstanceState.getString("preparedUri");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getUser();
        isLoading = false;
        reset();
    }

    private void showBirthdayDialog() {
        if (accountEditMode == ACCOUNT_EDIT_PREVIEW) {
            return;
        }
        int year = 0;
        int monthOfYear = 0;
        int dayOfMonth = 0;

        LOGD(TAG, "tmp bithday:" + tmpBirthday);
        Calendar cal = Calendar.getInstance(Locale.JAPAN);
        cal.setTime(DateUtils.getDateFromString("yyyy/MM/dd HH:mm:ss", tmpBirthday));
        year = cal.get(Calendar.YEAR);
        monthOfYear = cal.get(Calendar.MONTH);
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dialog = new DatePickerDialog(this, R.style.DatePickerDialogStyle, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                LOGD(TAG, "birthday data did set:"+ year+"/"+(monthOfYear+1)+"/"+dayOfMonth);
                String birthdayStr = year + "/" + (monthOfYear + 1) + "/" + dayOfMonth + " 00:00:00";
                LOGD(TAG, "done birthday:" + birthdayStr);
                tmpBirthday = birthdayStr;
                adapter.notifyDataSetChanged();
            }
        }, year, monthOfYear, dayOfMonth);

        // TODO:APIレベルによって変える必要あり？
        LOGD(TAG, "SDK INT:" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.transparent));
        }


        dialog.getDatePicker().setMaxDate(new Date().getTime());
//        dialog = Dialog.restyleDatePickerDialog(this, dialog);
        dialog.show();
    }

    private void showGenderDialog() {
        if (accountEditMode == ACCOUNT_EDIT_PREVIEW) {
            return;
        }
        int checkedPosition = -1;
        if (tmpGender.equals(Profile.MAN)) {
            checkedPosition = 0;
        } else if (tmpGender.equals(Profile.WOMAN)) {
            checkedPosition = 1;
        }
        LOGD(TAG, "gender:" + tmpGender);
        new AlertDialog.Builder(this, R.style.ActionDialogStyle)
                .setSingleChoiceItems(new String[]{getString(R.string.text_gender_man), getString(R.string.text_gender_woman)}, checkedPosition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LOGD(TAG, "gender which:" + which);
                        if (which >= 0) {
                            tmpGender = which == 0 ? Profile.MAN : Profile.WOMAN;
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                })
                .show();
    }

    private void saveButtonClicked() {
        View view = this.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        self = AppController.getInstance().getSelf(false);
        self.getProfile().setNickname(this.tmpNickname);

        if (isLoading) {
            return;
        }
        isLoading = true;
        {
            if (!this.tmpBirthday.isEmpty()) {
                String birth = this.tmpBirthday;
                Date date = DateUtils.getDateFromString("yyyy/MM/dd HH:mm:ss", birth);
                Calendar cal = Calendar.getInstance(Locale.JAPAN);
                cal.setTime(date);
                cal.add(Calendar.MONTH, 1);
                String birthday = cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " 00:00:00";
                self.getProfile().setBirthday(birthday);
            }
        }
        self.getProfile().setGender(this.tmpGender);

        try {
            if (self.getProfile().getManager().save()) {
                Bundle args = new Bundle();
                args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
                updateProfile(null, args);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGE(TAG, e.getMessage());
        }
    }

    private int tmpMediaRequestCode = 0;

    private void launchCamera(int requestCode) {
        this.mediaStoreCompat = new MediaStoreCompat(this, handler);
        this.mediaStoreCompat.cleanUp(this.preparedUri);
        this.preparedUri = mediaStoreCompat.invokeCameraCapture(this, requestCode);
    }

    private void launchGallery(int requestCode) {
        this.mediaStoreCompat = null;
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(
                        intent, ""),
                requestCode);
    }

    private void showMediaChooserDialog(final int requestCode) {
        if (!allowUseCamera) {
            tmpMediaRequestCode = requestCode;
            requestCameraPermission();
            return;
        }
        new AlertDialog.Builder(this, R.style.ActionDialogStyle)
                .setSingleChoiceItems(new String[]{getString(R.string.text_actionsheet_camera),
                        getString(R.string.text_actionsheet_cameraroll)}, 0, null)
                .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView lw = ((AlertDialog) dialog).getListView();
                        int checkedPosition = lw.getCheckedItemPosition();
                        LOGD(TAG, "checkedItemPosition:" + checkedPosition);
                        if (checkedPosition == 0) /*camera*/ {
                            launchCamera(requestCode);
                        } else {/* gallery */
                            launchGallery(requestCode);
                        }
                    }
                })
                .show();
    }

    private void resetButtonClicked() {
        View view = this.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        finish();
//        reset();
    }

    private void reset() {
        this.tmpNickname = self.getProfile().getNickname();
        this.tmpBirthday = self.getProfile().getBirthday();
        this.tmpGender = self.getProfile().getGender();
        this.adapter.notifyDataSetChanged();
    }

    private void onEditButtonClicked() {
        Intent view = new Intent(this, AccountEditActivity.class);
        view.putExtra("mode", ACCOUNT_EDIT);
        startActivity(view);
    }

    @Override
    protected void onUpdateProfileSuccessed(@Nullable @Route.From String from, @Nullable Bundle args) {
        adapter.notifyDataSetChanged();
        isLoading = false;
        // アカウント連携完了
        if (from == null) {
            AppController.getInstance().showAlert(this, getString(R.string.text_account_edit_success), null);
            return;
        }
        if (from == Route.FACEBOOK) {
            AppController.getInstance().showAlert(this, getString(R.string.text_account_login_facebook_success), null);
        } else if (from == Route.TWITTER) {
            AppController.getInstance().showAlert(this, getString(R.string.text_account_login_twitter_success), null);
        } else if (from == Route.EMAIL) {
            AppController.getInstance().showAlert(this, getString(R.string.text_account_login_email_success), null);
        }
    }

    @Override
    protected void onUpdateProfileFailed(@Nullable @Route.From String from, @Nullable Bundle args) {
        isLoading = false;
    }

    @Override
    protected void onRefreshedMyInfo(int oldPiece, int newPiece, int oldPoint, int newPoint, int oldRank, int newRank) {
        reset();
        isLoading = false;
    }

    @Override
    protected void onRefreshedMyInfoFailed() {
    }

    @Override
    protected void onMigrateSuccess(@Route.From String from) {
        isLoading = false;
    }

    @Override
    protected void onMigrateFail(@Route.From String from, @Nullable String message, ApiError error) {
        isLoading = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            LOGD(TAG, "clicked home button");
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        Bitmap iconOrCoverData = null;
        if (requestCode == ICON_CHOOSER_REQUEST_CODE || requestCode == COVER_CHOOSER_REQUEST_CODE) {
            if (mediaStoreCompat != null) {/* from gallery */
                if (preparedUri.isEmpty()) {
                    AppController.getInstance().showApiErrorAlert(AccountEditActivity.this, null);
                    return;
                }
            }

            final Uri uri;
            if (mediaStoreCompat != null) {
                uri = mediaStoreCompat.getCapturedPhotoUri(data, preparedUri);
            } else {
                uri = data.getData();
            }

            Matrix matrix = new Matrix();
            int orientation = ExifInterfaceCompat.getExifOrientation(
                    MediaStoreCompat.getPathFromUri(getContentResolver(), uri)
            );
            LOGD(TAG, "orientation:" + orientation);
            matrix.postRotate(orientation);
            BitmapResizer bitmapResizer = new BitmapResizer(this);
            try {
                if (requestCode == COVER_CHOOSER_REQUEST_CODE) {
                    int width = recyclerView.getMeasuredWidth() / 2;
                    int height = ViewUtils.getPixelFromDp(this, getResources().getDimensionPixelSize(R.dimen.account_profile_cover_size_height) / 2);
                    iconOrCoverData = bitmapResizer.resize(uri, width, height);
                } else {
                    int iconWH = 200;
                    iconOrCoverData = bitmapResizer.resize(uri,
                            iconWH, iconWH
                    );
                }
            } catch (IOException e) {
                LOGE(TAG, e.getMessage());
                e.printStackTrace();
            }
            if (iconOrCoverData == null) {
                LOGE(TAG, "iconOrCoverData is null");
                return;
            }
            iconOrCoverData = Bitmap.createBitmap(iconOrCoverData, 0, 0, iconOrCoverData.getWidth(), iconOrCoverData.getHeight(), matrix, true);
        }
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (allowUseCamera) {
                    showMediaChooserDialog(tmpMediaRequestCode);
                }
                break;
            }
            case ICON_CHOOSER_REQUEST_CODE: {
                LOGD(TAG, "icon chooser request code");
                try {
                    final Bitmap fitBp = iconOrCoverData;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            self = AppController.getInstance().getSelf(true);
                            String encodedBp = ImageUtils.getBase64FromBitmap(fitBp);
                            LOGD(TAG, "encodedBp isEmpty?:" + encodedBp.isEmpty());
                            ApiParams params = self.getManager().getUpdateProfileParams();
                            params.put("icon", encodedBp);
                            params.put("cover", self.getProfile().getCoverImgBlob());
                            Bundle args = new Bundle();
                            args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
                            updateProfile(null, args, params);
                        }
                    });
                } catch (Exception e) {
                    LOGE(TAG, e.getMessage());
                    e.printStackTrace();
                }
                break;
            }
            case COVER_CHOOSER_REQUEST_CODE: {
                LOGD(TAG, "cover chooser request code");
                try {
                    final Bitmap fitBp = iconOrCoverData;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            self = AppController.getInstance().getSelf(true);
                            String encodedBp = ImageUtils.getBase64FromBitmap(fitBp);
                            LOGD(TAG, "encodedBp isEmpty?:" + encodedBp.isEmpty());
                            ApiParams params = self.getManager().getUpdateProfileParams();
                            params.put("icon", self.getProfile().getIconImgBlob());
                            params.put("cover", encodedBp);
                            Bundle args = new Bundle();
                            args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
                            updateProfile(null, args, params);
                        }
                    });
                } catch (Exception e) {
                    LOGE(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    class AccountEditBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context = null;
        private LayoutInflater inflater = null;

        private static final int PROFILE_LIST_SIZE = 7;

        private static final int HEADER = 0;
        private static final int TABIO_ID = 1;
        private static final int PIN_CODE = 2;
        private static final int NICKNAME = 3;
        private static final int BIRTHDAY = 4;
        private static final int GENDER = 5;
        private static final int FOOTER = 6;

        public AccountEditBaseAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == 0) {
                view = inflater.inflate(R.layout.profile_header_view, parent, false);
                return new HeaderViewHolder(view);
            } else if (viewType == getItemCount() - 1) {
                view = inflater.inflate(R.layout.profile_footer_view, parent, false);
                return new FooterViewHolder(view);
            } else {
                view = inflater.inflate(R.layout.profile_list_item, parent, false);
                return new ProfileViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (position) {
                case HEADER: {
                    HeaderViewHolder h = (HeaderViewHolder) holder;
                    if (accountEditMode == ACCOUNT_EDIT) {
                        h.iconEditButton.setVisibility(View.GONE);
                        h.coverEditButton.setVisibility(View.GONE);
                    }
                    setProfileIcon(AccountEditActivity.this, self.getProfile().getIconImgUrl(), h.icon);
                    setProfileCover(AccountEditActivity.this, self.getProfile().getCoverImgUrl(), h.cover);
                    h.coverEditButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            pictureUri = CameraUtils.showPictureChooser(COVER_CHOOSER_REQUEST_CODE, AccountEditActivity.this, "cover");
                            showMediaChooserDialog(COVER_CHOOSER_REQUEST_CODE);
                        }
                    });
                    h.iconEditButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            pictureUri = CameraUtils.showPictureChooser(ICON_CHOOSER_REQUEST_CODE, AccountEditActivity.this, "icon");
                            showMediaChooserDialog(ICON_CHOOSER_REQUEST_CODE);
                        }
                    });
                    break;
                }
                case TABIO_ID: {
                    ProfileViewHolder h = (ProfileViewHolder) holder;
                    h.title.setText(context.getString(R.string.text_account_my_id));
                    h.value.setText(self.getTabioId());
                    break;
                }
                case PIN_CODE: {
                    ProfileViewHolder h = (ProfileViewHolder) holder;
                    h.title.setText(context.getString(R.string.text_account_my_pin));
                    h.value.setText(self.getPinCode());
                    break;
                }
                case NICKNAME: {
                    ProfileViewHolder h = (ProfileViewHolder) holder;
                    h.title.setText(context.getString(R.string.text_account_profile_title_nickname));

                    if (accountEditMode == ACCOUNT_EDIT_PREVIEW) {
                        h.editValue.setEnabled(false);
                        h.editValue.setVisibility(View.GONE);
                        h.value.setVisibility(View.VISIBLE);
                        h.value.setText(tmpNickname);
                    } else {
                        h.editValue.setEnabled(true);
                        h.editValue.setVisibility(View.VISIBLE);
                        h.value.setVisibility(View.GONE);
                        if (tmpNickname == null || tmpNickname.length() < 1) {
                            h.editValue.setHint(context.getString(R.string.text_account_profile_title_nickname_placeholder));
                        } else {
                            h.editValue.setText(tmpNickname);
                        }
                        h.editValue.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                    recyclerView.requestFocus();// out of focus editValue
                                    return true;
                                }
                                return false;
                            }
                        });
                        h.editValue.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                tmpNickname = s.toString();
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }
                    break;
                }
                case BIRTHDAY: {
                    ProfileViewHolder h = (ProfileViewHolder) holder;
                    h.title.setText(context.getString(R.string.text_account_profile_title_birthday));
                    h.value.setText(DateUtils.getDateFromFormat2(AccountEditActivity.this, "yyyy/MM/dd HH:mm:ss", tmpBirthday, self.getLanguage()));

                    if (accountEditMode == ACCOUNT_EDIT_PREVIEW) {
                        h.itemView.setEnabled(false);
                    } else {
                        h.value.setHint(context.getString(R.string.text_account_profile_title_birthday_placeholder));
                        h.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showBirthdayDialog();
                            }
                        });
                    }
                    break;
                }
                case GENDER: {
                    ProfileViewHolder h = (ProfileViewHolder) holder;
                    h.title.setText(context.getString(R.string.text_search_menu_gender));
                    LOGD(TAG, "tmpGender:" + tmpGender);
                    h.value.setText(Profile.getGenderForDisplay(AccountEditActivity.this, tmpGender));

                    if (accountEditMode == ACCOUNT_EDIT_PREVIEW) {
                        h.itemView.setEnabled(false);
                    } else {
                        h.value.setHint(context.getString(R.string.text_account_profile_title_gender_placeholder));
                        h.value.setText(Profile.getGenderForDisplay(AccountEditActivity.this, tmpGender));
                        h.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showGenderDialog();
                            }
                        });
                    }

                    break;
                }
                case FOOTER:
                    final FooterViewHolder h = (FooterViewHolder) holder;
                    if (accountEditMode == ACCOUNT_EDIT_PREVIEW) {
                        h.inputResetButton.setVisibility(View.GONE);
                        h.connectTitle.setText(context.getString(R.string.text_account_connect_title));
                        h.profileEditButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onEditButtonClicked();
                            }
                        });

                        LOGD(TAG, "fb:" + self.getManager().getRoute(Route.FACEBOOK));
                        LOGD(TAG, "tw:" + self.getManager().getRoute(Route.TWITTER));
                        LOGD(TAG, "mail:" + self.getManager().getRoute(Route.EMAIL));
                        if (self.getManager().getRoute(Route.FACEBOOK) == null) {
                            h.facebookLoginButtonDisableView.setVisibility(View.GONE);
                        } else {
                            h.facebookLoginButtonDisableView.setVisibility(View.VISIBLE);
                        }
                        if (self.getManager().getRoute(Route.TWITTER) == null) {
                            h.twitterLoginButtonDisableView.setVisibility(View.GONE);
                        } else {
                            h.twitterLoginButtonDisableView.setVisibility(View.VISIBLE);
                        }
                        h.emailLoginButtonDisableView.setVisibility(View.GONE);
                        h.idPinSaveButtonDisableView.setVisibility(View.GONE);

                        h.registerEmailPasswordButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                toEmailPasswordRegisterScreen();
                            }
                        });
                        h.facebookConnectButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (h.facebookLoginButtonDisableView.getVisibility() == View.VISIBLE) {
                                    return;
                                }
                                Bundle args = new Bundle();
                                args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
                                facebookAuthentication(args, true);
                            }
                        });
                        h.twitterConnectButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (h.twitterLoginButtonDisableView.getVisibility() == View.VISIBLE) {
                                    return;
                                }
                                Bundle args = new Bundle();
                                args.putInt(NEXT_ACTION, NEXT_ACTION_UPDATE_PROFILE);
                                twitterAuthentication(args, true);
                            }
                        });
                        h.idPinSaveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent view = new Intent(AccountEditActivity.this, MyIdActivity.class);
                                startActivity(view);
                            }
                        });

                    } else {
                        h.profileEditButton.setText(getString(R.string.button_save));
                        h.profileEditButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveButtonClicked();
                            }
                        });
                        h.inputResetButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                resetButtonClicked();
                            }
                        });
                        h.connectTitle.setVisibility(View.GONE);
                        h.idPinSaveButton.setVisibility(View.GONE);
                        h.facebookConnectButton.setVisibility(View.GONE);
                        h.twitterConnectButton.setVisibility(View.GONE);
                        h.registerEmailPasswordButton.setVisibility(View.GONE);
                        h.footerDescription.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }


        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return PROFILE_LIST_SIZE;
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.cover)
            ImageView cover;
            @BindView(R.id.cover_edit_button)
            Button coverEditButton;
            @BindView(R.id.icon)
            CircleImageView icon;
            @BindView(R.id.icon_edit_button)
            ImageButton iconEditButton;

            public HeaderViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class ProfileViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.value)
            TextView value;
            @BindView(R.id.edit_value)
            EditText editValue;

            public ProfileViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tabioidpin_save_button)
            View idPinSaveButton;
            @BindView(R.id.profile_edit_button)
            Button profileEditButton;
            @BindView(R.id.input_reset_button)
            Button inputResetButton;
            @BindView(R.id.connect_title)
            TextView connectTitle;
            @BindView(R.id.facebook_login_button)
            View facebookConnectButton;
            @BindView(R.id.twitter_login_button)
            View twitterConnectButton;
            @BindView(R.id.email_login_button)
            View registerEmailPasswordButton;
            @BindView(R.id.footer_description)
            TextView footerDescription;

            @BindView(R.id.tabioidpin_button_disable)
            View idPinSaveButtonDisableView;
            @BindView(R.id.facebook_login_button_disable)
            View facebookLoginButtonDisableView;
            @BindView(R.id.twitter_login_button_disable)
            View twitterLoginButtonDisableView;
            @BindView(R.id.email_login_button_disable)
            View emailLoginButtonDisableView;


            public FooterViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
