package com.quickblox.sample.groupchatwebrtc.utils;

import android.content.Context;
import android.widget.EditText;

import com.quickblox.sample.groupchatwebrtc.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by tereha on 03.06.16.
 */
public class ValidationUtils {

    private static boolean isEnteredTextValid(Context context, EditText editText, int resFieldName, int maxLength, boolean checkName) {

        boolean isCorrect;
        Pattern p;
        if (checkName) {
            p = Pattern.compile("^[a-zA-Z][a-zA-Z 0-9]{2," + (maxLength - 1) + "}+$");
        } else {
            p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{2," + (maxLength - 1) + "}+$");
        }

        Matcher m = p.matcher(editText.getText().toString().trim());
        isCorrect = m.matches();

        if (!isCorrect) {
            editText.setError(String.format(context.getString(R.string.error_name_must_not_contain_special_characters_from_app),
                    context.getString(resFieldName),
                    maxLength));
            return false;
        } else {
            return true;
        }
    }

    public static boolean isUserNameValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_name, 20, true);
    }

    public static boolean isUserAboutValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_about, 200, true);
    }
    public static boolean isUserTitleValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_title, 25, true);
    }

    public static boolean isUserBirthdayValid(Context context) {
        return true;
    }

    public static boolean isUserPasswordValid(Context context, EditText editText) {

        String password = editText.getText().toString().trim();
        Boolean isCorrect = true;

        if (password.length() < 8) {
            isCorrect = false;
        } else {
            char c;
            int count = 0;
            for (int i = 0; i < password.length(); i++) {
                c = password.charAt(i);
                if (!Character.isLetterOrDigit(c)) {
                    isCorrect = false;
                } else if (Character.isDigit(c)) {
                    count++;
                }
            }
            if (count < 2)   {
                isCorrect = false;
            }
        }

        if(isCorrect == false) {
            editText.setError("There was a password error, password must contain 8 characters, at least 2 digits and may contain only letters and digits");
        }

        return isCorrect;
    }
}
