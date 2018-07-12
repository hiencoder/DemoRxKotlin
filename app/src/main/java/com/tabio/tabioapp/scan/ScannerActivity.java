package com.tabio.tabioapp.scan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.BuildConfig;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.coordinate.VerticalCoordinatesActivity;
import com.tabio.tabioapp.ui.BaseActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class ScannerActivity extends BaseActivity {
    public static final String TAG = makeLogTag(ScannerActivity.class);

    @BindView(R.id.scanner_view)
    CompoundBarcodeView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        AppController.getInstance().sendGAScreen("スキャン");
        AppController.getInstance().decideTrack("570f2d0399c3634a425af4c8");
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(getString(R.string.text_scan_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scannerView.setStatusText("");
        scannerView.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));

        scannerView.decodeContinuous(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        scannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        scannerView.pause();
    }


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
//                String text = result.getText();
//                Intent view = new Intent(ScannerActivity.this, VerticalCoordinatesActivity.class);
//                view.putExtra("classId", text);
//                ScannerActivity.this.startActivity(view);
//                ScannerActivity.this.finish();
                Intent data = new Intent();
                data.putExtra("text", result.getText());
                ScannerActivity.this.setResult(RESULT_OK, data);
                finish();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
