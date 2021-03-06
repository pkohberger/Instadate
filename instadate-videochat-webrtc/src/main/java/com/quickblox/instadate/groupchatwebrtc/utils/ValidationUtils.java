package com.quickblox.instadate.groupchatwebrtc.utils;

import android.content.Context;
import android.widget.EditText;

import com.quickblox.instadate.groupchatwebrtc.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by tereha on 03.06.16.
 */
public class ValidationUtils {

    private static boolean isEnteredTextValid(Context context, EditText editText, int resFieldName, int minLength, int maxLength, boolean allowSpacesAndChars) {

        boolean isCorrect;
        Pattern p;
        String additionalBlurb;
        if (allowSpacesAndChars) {
            p = Pattern.compile("^[a-zA-Z 0-9\\\\,\\\\.\\\\!\\\\?\\\\:\\\\;\\\\(\\\\)]{"+String.valueOf(minLength-1)+"," + (maxLength - 1) + "}+$");
            additionalBlurb = " with the following special characters allowed: ( , . ! ? : ; ),";
        } else {
            p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{"+String.valueOf(minLength-1)+"," + (maxLength - 1) + "}+$");
            additionalBlurb = " without space,";
        }

        Matcher m = p.matcher(editText.getText().toString().trim());
        isCorrect = m.matches();

        if (!isCorrect) {
            editText.setError(String.format(context.getString(R.string.error_name_must_not_contain_special_characters_from_app),
                    context.getString(resFieldName),
                    String.valueOf(minLength),
                    String.valueOf(maxLength),
                    additionalBlurb));
            return false;
        } else {
            return true;
        }
    }

    public static boolean isUserNameValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_name,3, 20, false);
    }

    public static boolean isUserAboutValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_about, 15,200, true);
    }

    public static boolean isUserTitleValid(Context context, EditText editText) {
        return isEnteredTextValid(context, editText, R.string.field_name_user_title,5, 25, true);
    }

    public static boolean isUserPasswordValid(EditText editText) {

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
            editText.setError("Password must contain 8 characters, at least 2 must be digits and may contain only alpha and numeric characters");
        }

        return isCorrect;
    }
}
