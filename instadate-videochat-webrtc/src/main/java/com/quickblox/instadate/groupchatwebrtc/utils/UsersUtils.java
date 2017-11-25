package com.quickblox.instadate.groupchatwebrtc.utils;

import android.content.Context;
import android.text.TextUtils;

import com.quickblox.instadate.core.utils.SharedPrefsHelper;
import com.quickblox.instadate.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.users.model.QBUser;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
}