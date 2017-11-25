
package com.quickblox.instadate.groupchatwebrtc.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import com.quickblox.instadate.core.gcm.GooglePlayServicesHelper;
import com.quickblox.instadate.core.ui.activity.CoreBaseActivity;
import com.quickblox.instadate.core.utils.ErrorUtils;
import com.quickblox.instadate.core.utils.SharedPrefsHelper;
import com.quickblox.instadate.groupchatwebrtc.App;
import com.quickblox.instadate.groupchatwebrtc.R;
import com.quickblox.instadate.groupchatwebrtc.util.QBResRequestExecutor;
import com.quickblox.instadate.groupchatwebrtc.utils.Consts;

/**
 * QuickBlox team
 */
public abstract class BaseActivity extends CoreBaseActivity implements LocationListener {

    SharedPrefsHelper sharedPrefsHelper;

    private ProgressDialog progressDialog;

    protected GooglePlayServicesHelper googlePlayServicesHelper;

    protected QBResRequestExecutor requestExecutor;

    protected Context context;

    protected String Location;

    private static final int REQUEST_EXTERNAL_PERMISSIONS = 1;

    private static String[] PERMISSIONS = {

        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestExecutor = App.getInstance().getQbResRequestExecutor();
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        googlePlayServicesHelper = new GooglePlayServicesHelper();

        checkAndRequestPermissions();

    }

    public void checkAndRequestPermissions() {

        int locationFinePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int locationCoarsePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (writePermission != PackageManager.PERMISSION_GRANTED
                ||  readPermission != PackageManager.PERMISSION_GRANTED
                ||  cameraPermission != PackageManager.PERMISSION_GRANTED
                ||  locationFinePermission != PackageManager.PERMISSION_GRANTED
                ||  locationCoarsePermission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_EXTERNAL_PERMISSIONS
            );

        }

    }

    @Override
    public void onLocationChanged(final Location location) {
        Location = location.getLatitude() +"|"+ location.getLongitude();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String bestProvider =locationManager.getBestProvider(criteria,true);
                    try {
                        Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);
                        Location = lastKnownLocation.getLatitude() + "|" + lastKnownLocation.getLongitude();
                    }catch(SecurityException e) {
                        Log.i("",e.getMessage());
                    }
                }
                return;
            }
        }
    }

    public void initDefaultActionBar() {
        String currentUserFullName = "";
        String currentRoomName = sharedPrefsHelper.get(Consts.PREF_CURREN_ROOM_NAME, "");

        if (sharedPrefsHelper.getQbUser() != null) {
            currentUserFullName = sharedPrefsHelper.getQbUser().getFullName();
        }

        setActionBarTitle(currentRoomName);
        //setActionbarSubTitle(String.format(getString(R.string.subtitle_text_logged_in_as), currentUserFullName));
    }


    public void setActionbarSubTitle(String subTitle) {
        if (actionBar != null)
            actionBar.setSubtitle(subTitle);
    }

    public void removeActionbarSubTitle() {
        if (actionBar != null)
            actionBar.setSubtitle(null);
    }

    void showProgressDialog(@StringRes int messageId) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            // Disable the back button
            DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            };
            progressDialog.setOnKeyListener(keyListener);
        }

        progressDialog.setMessage(getString(messageId));

        progressDialog.show();

    }

    void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected void showErrorSnackbar(@StringRes int resId, Exception e,
                                     View.OnClickListener clickListener) {
        if (getSnackbarAnchorView() != null) {
            ErrorUtils.showSnackbar(getSnackbarAnchorView(), resId, e,
                    com.quickblox.instadate.core.R.string.dlg_retry, clickListener);
        }
    }

    protected abstract View getSnackbarAnchorView();
}




