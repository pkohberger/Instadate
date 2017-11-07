package com.quickblox.sample.groupchatwebrtc.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.sample.core.utils.KeyboardUtils;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.services.CallService;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.QBEntityCallbackImpl;
import com.quickblox.sample.groupchatwebrtc.utils.UsersUtils;
import com.quickblox.sample.groupchatwebrtc.utils.ValidationUtils;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends BaseActivity {

    private String userPassword;
    private EditText userNameEditText;
    private EditText userPasswordEditText;
    private Button TakePortrait;
    private QBUser userForSave;
    private CircleImageView imageView;
    protected static TextView BirthdayLabel;
    protected static String birthDate;
    private static final int REQUEST_EXTERNAL_PERMISSIONS = 1;
    String mCurrentPhotoPath;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        verifyPermissions(this);
        initUI();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.root_view_login_activity);
    }

    public static void verifyPermissions(Activity activity) {

        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (writePermission != PackageManager.PERMISSION_GRANTED
        ||  readPermission != PackageManager.PERMISSION_GRANTED
        ||  cameraPermission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS,
                    REQUEST_EXTERNAL_PERMISSIONS
            );
        }
    }

    private void initUI() {
        setActionBarTitle(R.string.title_login_activity);
        userNameEditText = (EditText) findViewById(R.id.user_name);
        userNameEditText.addTextChangedListener(new LoginEditTextWatcher(userNameEditText));

        userPasswordEditText = (EditText) findViewById(R.id.user_password);
        userPasswordEditText.addTextChangedListener(new LoginEditTextWatcher(userPasswordEditText));

        BirthdayLabel = (TextView)findViewById(R.id.BirthdayLabel);

        TakePortrait = (Button)findViewById(R.id.TakePortrait);

        TakePortrait.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        photoFile = null;
                    }
                    if (photoFile != null) {
                        /**
                         * this stores the file on the phone in internal storage pictures directory:
                         * cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                         **/
                        startActivityForResult(cameraIntent, Consts.CAMERA_PIC_REQUEST);
                    }
                }
            }

            private File createImageFile() throws IOException {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
                );

                mCurrentPhotoPath = "file:" + image.getAbsolutePath();
                return image;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_login_user_done:
                if (isEnteredUserNameValid() && isEnteredUserPasswordValid()) {
                    hideKeyboard();
                    startSignUpNewUser(createUserWithEnteredData());
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isEnteredUserNameValid() {
        return ValidationUtils.isUserNameValid(this, userNameEditText);
    }

    private boolean isEnteredUserPasswordValid() {
        return ValidationUtils.isUserPasswordValid(this, userPasswordEditText);
    }

    private void hideKeyboard() {
        KeyboardUtils.hideKeyboard(userNameEditText);
    }

    private void startSignUpNewUser(final QBUser newUser) {
        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser result, Bundle params) {
                    loginToChat(result);
                }

                @Override
                public void onError(QBResponseException e) {
                    if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                        signInCreatedUser(newUser, true);
                    } else {
                        hideProgressDialog();
                        Toaster.longToast(R.string.sign_up_error);
                    }
                }
            }
        );
    }

    private void loginToChat(final QBUser qbUser) {
        qbUser.setPassword(this.userPassword);

        userForSave = qbUser;
        startLoginService(qbUser);
    }

    private void startOpponentsActivity() {
        OpponentsActivity.start(LoginActivity.this, false);
        finish();
    }

    private void saveUserData(QBUser qbUser) {
        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        sharedPrefsHelper.save(Consts.PREF_CURREN_ROOM_NAME, qbUser.getTags().get(0));
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private QBUser createUserWithEnteredData() {
        return createQBUserWithCurrentData(String.valueOf(userNameEditText.getText()),String.valueOf(userPasswordEditText.getText()));
    }

    private QBUser createQBUserWithCurrentData(String userName, String userPassword) {
        QBUser qbUser = null;
        this.userPassword = userPassword;
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPassword)) {
            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add("Tag");

            qbUser = new QBUser();
            qbUser.setFullName(userName);
            qbUser.setLogin(getCurrentDeviceId());
            qbUser.setPassword(userPassword);
            qbUser.setTags(userTags);
        }

        return qbUser;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
            hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {
                saveUserData(userForSave);

                signInCreatedUser(userForSave, false);
            } else {
                Toaster.longToast(getString(R.string.login_chat_login_error) + errorMessage);
                userNameEditText.setText(userForSave.getFullName());
            }
        }
        if(requestCode == Consts.CAMERA_PIC_REQUEST) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            imageView = (CircleImageView) findViewById(R.id.SelectedImage);
            imageView.setImageBitmap(image);
        }
    }

    private void signInCreatedUser(final QBUser user, final boolean deleteCurrentUser) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                if (deleteCurrentUser) {
                    removeAllUserData(result);
                } else {
                    startOpponentsActivity();
                }
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    private void removeAllUserData(final QBUser user) {
        requestExecutor.deleteCurrentUser(user.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                UsersUtils.removeUserData(getApplicationContext());
                startSignUpNewUser(createUserWithEnteredData());
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    private void startLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, CallService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(this, qbUser, pendingIntent);
    }

    private String getCurrentDeviceId() {
        return Utils.generateDeviceId(this);
    }

    private class LoginEditTextWatcher implements TextWatcher {
        private EditText editText;

        private LoginEditTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editText.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //change month zero based to 1 based
            month++;
            String yyyy = String.valueOf(year);
            String mm = (String.valueOf(month).length() == 1 ? '0'+String.valueOf(month) : String.valueOf(month));
            String dd = (String.valueOf(day).length() == 1 ? '0'+String.valueOf(day) : String.valueOf(day));
            birthDate = yyyy+mm+dd;
            BirthdayLabel.setText(mm+"/"+dd+"/"+yyyy);
        }
    }
}
