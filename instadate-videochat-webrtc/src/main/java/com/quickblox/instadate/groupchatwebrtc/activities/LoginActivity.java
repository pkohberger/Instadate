package com.quickblox.instadate.groupchatwebrtc.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Button;
import android.graphics.Bitmap;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.instadate.core.utils.KeyboardUtils;
import com.quickblox.instadate.core.utils.SharedPrefsHelper;
import com.quickblox.instadate.core.utils.Toaster;
import com.quickblox.instadate.groupchatwebrtc.R;
import com.quickblox.instadate.groupchatwebrtc.services.CallService;
import com.quickblox.instadate.groupchatwebrtc.utils.Consts;
import com.quickblox.instadate.groupchatwebrtc.utils.QBEntityCallbackImpl;
import com.quickblox.instadate.groupchatwebrtc.utils.UsersUtils;
import com.quickblox.instadate.groupchatwebrtc.utils.ValidationUtils;
import com.quickblox.users.model.QBUser;

import java.io.IOException;
import java.util.Calendar;

public class LoginActivity extends BaseActivity {

    private String userPassword;

    private EditText userNameEditText;

    private EditText userAboutEditText;

    private String userAboutEditTextString;

    private EditText userTitleEditText;

    private String userTitleEditTextString;

    private EditText userPasswordEditText;

    private Button SelectPortrait;

    private QBUser userForSave;

    private CircleImageView imageView = null;

    protected static TextView BirthdayLabel;

    protected static String birthDate = null;

    private Button TakePortrait;

    private Bitmap PortraitImage = null;


    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUI();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.root_view_login_activity);
    }

    private void initUI() {
        setActionBarTitle(R.string.title_login_activity);
        userNameEditText = (EditText) findViewById(R.id.user_name);
        userNameEditText.addTextChangedListener(new LoginEditTextWatcher(userNameEditText));

        userPasswordEditText = (EditText) findViewById(R.id.user_password);
        userPasswordEditText.addTextChangedListener(new LoginEditTextWatcher(userPasswordEditText));

        userAboutEditText = (EditText) findViewById(R.id.user_about);
        userAboutEditText.addTextChangedListener(new LoginEditTextWatcher(userAboutEditText));

        userTitleEditText = (EditText) findViewById(R.id.user_title);
        userTitleEditText.addTextChangedListener(new LoginEditTextWatcher(userTitleEditText));

        BirthdayLabel = (TextView)findViewById(R.id.BirthdayLabel);

        TakePortrait = (Button)findViewById(R.id.TakePortrait);

        SelectPortrait = (Button)findViewById(R.id.SelectPortrait);

        TakePortrait.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, Consts.CAMERA_PIC_REQUEST);
            }
        });

        SelectPortrait.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , Consts.PICK_PHOTO_FOR_PORTRAIT);
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
                if (areEnteredUserInputsForSignUpValid()) {
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
        return ValidationUtils.isUserPasswordValid(userPasswordEditText);
    }

    private boolean isEnteredUserTitleValid() {
        return ValidationUtils.isUserTitleValid(this, userTitleEditText);
    }

    private boolean isEnteredUserAboutValid() {
        return ValidationUtils.isUserAboutValid(this, userAboutEditText);
    }

    private boolean isEnteredUserBirthdayValid() {
        /**
         * not using validation class because using toast for message
         */
        if(birthDate == null) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isEnteredUserPicValid() {
        /**
         * @Todo Add Image Size Validation
         * @Author Phil Kohberger
         * not using validation class because using toast for message
         */
        if(imageView == null) {
            return false;
        } else {
            return true;
        }
    }
    private void showPictureAndOrBirthdayToastIfError() {
        if(!isEnteredUserPicValid() && !isEnteredUserBirthdayValid()) {
            Toaster.longToast("User Picture and Birthday are Required");
        } else if(!isEnteredUserPicValid()) {
            Toaster.longToast("User Picture is Required");
        } else if(!isEnteredUserBirthdayValid()) {
            Toaster.longToast("User Birthday is Required");
        }
    }

    private boolean areEnteredUserInputsForSignUpValid() {
        /**
         * we want to run ALL validating methods and set errors
         * so the user sees what inputs are wrong at once
         * instead of one at a time
         */
        Boolean isValidName = isEnteredUserNameValid();
        Boolean isValidPass = isEnteredUserPasswordValid();
        Boolean isValidTitle = isEnteredUserTitleValid();
        Boolean isValidAbout = isEnteredUserAboutValid();
        Boolean isValidBirth = isEnteredUserBirthdayValid();
        Boolean isValidPic = isEnteredUserPicValid();

        showPictureAndOrBirthdayToastIfError();

        if(!isValidName || !isValidPass || !isValidTitle || !isValidAbout || !isValidBirth || !isValidPic) {
            return false;
        } else {
            userTitleEditTextString = userTitleEditText.getText().toString().trim();
            userAboutEditTextString = userAboutEditText.getText().toString().trim();
            return true;
        }
    }

    private void hideKeyboard() {
        KeyboardUtils.hideKeyboard(userNameEditText);
    }

    private class InstadateAsyncTask extends AsyncTask<String, Integer, Double> {

        private QBUser qbUser;

        public InstadateAsyncTask (QBUser result){
            qbUser = result;
        }

        @Override
        protected Double doInBackground(String... params) {
            /**
             * @Todo Make this method Atomic, delete QBUser if Instadate api failed
             * @Author Phil Kohberger
             */
            if(requestExecutor.postQbUserToInstadateAPI(
                PortraitImage,
                qbUser.getId().toString(),
                UsersUtils.HashId(qbUser.getId().toString()),
                birthDate,
                userTitleEditTextString,
                userAboutEditTextString,
                Location
            )) {
                loginToChat(this.qbUser);
            }
            return null;
        }
    }

    private void startSignUpNewUser(final QBUser newUser) {
        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser result, Bundle params) {
                    new InstadateAsyncTask(result).execute();
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

        if(requestCode == Consts.CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            PortraitImage = image;
            imageView = (CircleImageView) findViewById(R.id.SelectedImage);
            imageView.setImageBitmap(image);
        }

        if(requestCode == Consts.PICK_PHOTO_FOR_PORTRAIT && resultCode == RESULT_OK) {
            try {
                final Uri uri = data.getData();
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                String imageAbs = getRealPathFromURI(this,uri);
                image = modifyOrientation(image,imageAbs);
                PortraitImage = (Bitmap) image;
                imageView = (CircleImageView) findViewById(R.id.SelectedImage);
                imageView.setImageBitmap(image);
            } catch (IOException ex) {
               Log.d("",ex.getMessage());
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        try {
            ExifInterface ei = new ExifInterface(image_absolute_path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotate(bitmap, 90);

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotate(bitmap, 180);

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotate(bitmap, 270);

                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    return flip(bitmap, true, false);

                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    return flip(bitmap, false, true);

                default:
                    return bitmap;
            }
        } catch (IOException ex) {
            String message = ex.getMessage();
            Log.d("",message);
            return null;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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
