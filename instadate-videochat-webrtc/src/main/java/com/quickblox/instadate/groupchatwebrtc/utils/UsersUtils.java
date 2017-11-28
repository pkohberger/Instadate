package com.quickblox.instadate.groupchatwebrtc.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.quickblox.instadate.core.utils.SharedPrefsHelper;
import com.quickblox.instadate.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.users.model.QBUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.quickblox.instadate.groupchatwebrtc.utils.Consts;

import java.security.MessageDigest;

/**
 * Created by tereha on 09.06.16.
 */
public class UsersUtils {

    private static SharedPrefsHelper sharedPrefsHelper;
    private static QbUsersDbManager dbManager;

    public static String getUserNameOrId(QBUser qbUser, Integer userId) {
        if (qbUser == null) {
            return String.valueOf(userId);
        }

        String fullName = qbUser.getFullName();

        return TextUtils.isEmpty(fullName) ? String.valueOf(userId) : fullName;
    }

    public static ArrayList<QBUser> getListAllUsersFromIds(ArrayList<QBUser> existedUsers, List<Integer> allIds) {
        ArrayList<QBUser> qbUsers = new ArrayList<>();


        for (Integer userId : allIds) {
            QBUser stubUser = createStubUserById(userId);
            if (!existedUsers.contains(stubUser)) {
                qbUsers.add(stubUser);
            }
        }

        qbUsers.addAll(existedUsers);

        return qbUsers;
    }

    private static QBUser createStubUserById(Integer userId) {
        QBUser stubUser = new QBUser(userId);
        stubUser.setFullName(String.valueOf(userId));
        return stubUser;
    }

    public static ArrayList<Integer> getIdsNotLoadedUsers(ArrayList<QBUser> existedUsers, List<Integer> allIds) {
        ArrayList<Integer> idsNotLoadedUsers = new ArrayList<>();

        for (Integer userId : allIds) {
            QBUser stubUser = createStubUserById(userId);
            if (!existedUsers.contains(stubUser)) {
                idsNotLoadedUsers.add(userId);
            }
        }

        return idsNotLoadedUsers;
    }

    public static void removeUserData(Context context) {
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }
        sharedPrefsHelper.clearAllData();
        if (dbManager == null) {
            dbManager = QbUsersDbManager.getInstance(context);
        }
        dbManager.clearDB();
    }

    public static String HashId(String pass)  {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] data = pass.getBytes();
            m.update(data,0,data.length);
            BigInteger i = new BigInteger(1,m.digest());
            return String.format("%1$032X", i);
        } catch(NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String getWebViewPortraitHTML(QBUser user)  {

        String url = extractInstadatePortraitUrl(user);

        if(url.isEmpty()) {
            return null;
        }

        String html = "";
        html += "<html style='overflow:hidden;'>";
        html +=     "<head><meta name='viewport' content='width=device-width, initial-scale=1' /></head>";
        html +=     "<body>";
        html +=         "<img style='height:auto;width:100%;' src='" + url + "' />";
        html +=     "</body>";
        html += "</html>";

        return html;
    }

    public static String extractInstadatePortraitUrl(QBUser user)  {
        /**
         * @Todo Add Default random colored image if none
         * @Author Phil Kohberger
         */
        try {
            JSONArray instadateApiData = new JSONArray(user.getCustomData());

            for(int i = 0; i < instadateApiData.length(); i++) {

                JSONObject record = instadateApiData.getJSONObject(i);
                JSONArray filePaths = record.getJSONArray("filePaths");

                for(int j = 0; j < filePaths.length(); j++) {

                    JSONObject filePath = filePaths.getJSONObject(i);
                    String image = filePath.getString("fileName")+filePath.getString("fileType");
                    return Consts.INSTADATE_API_HOST + "/Uploads/" + image;

                }
            }
            return null;
        } catch(JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}