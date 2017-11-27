package com.quickblox.instadate.groupchatwebrtc.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.instadate.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import com.quickblox.instadate.groupchatwebrtc.utils.Consts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.conn.ClientConnectionManager;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.conn.tsccm.ThreadSafeClientConnManager;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by tereha on 26.04.16.
 */
public class QBResRequestExecutor {
    private String TAG = QBResRequestExecutor.class.getSimpleName();

    public static DefaultHttpClient getThreadSafeClient()  {

        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        return client;
    }

    public void signUpNewUser(final QBUser newQbUser, final QBEntityCallback<QBUser> callback) {
        QBUsers.signUp(newQbUser).performAsync(callback);
    }

    public void signInUser(final QBUser currentQbUser, final QBEntityCallback<QBUser> callback) {
        QBUsers.signIn(currentQbUser).performAsync(callback);
    }

    public void deleteCurrentUser(int currentQbUserID, QBEntityCallback<Void> callback) {
        QBUsers.deleteUser(currentQbUserID).performAsync(callback);
    }

    public void loadUsers( final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        QBUsers.getUsers(requestBuilder).performAsync(callback);
    }

    public void loadUsersByIds(final Collection<Integer> usersIDs, final QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(usersIDs, null).performAsync(callback);
    }

    public boolean postQbUserToInstadateAPISynchronous(
            Bitmap Image, String QbId, String AccessToken, String Birthday, String Title, String About, String Location) {

        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(Consts.INSTADATE_API_HOST + "/api/users");
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addTextBody("QbID", QbId);
            entityBuilder.addTextBody("AccessToken", AccessToken);
            entityBuilder.addTextBody("Birthday", Birthday);
            entityBuilder.addTextBody("Title", Title);
            entityBuilder.addTextBody("About", About);
            entityBuilder.addTextBody("Location", Location);

           if(Image != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bmData = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bmData);
                entityBuilder.addBinaryBody("UploadFile", bs, ContentType.DEFAULT_BINARY, AccessToken + ".jpeg");
            }

            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);

            client.execute(post);

            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public QBUser getQbUserByIdFromInstadateAPISynchronous(QBUser user) {
        try {

            HttpClient client = getThreadSafeClient();
            HttpGet get = new HttpGet(Consts.INSTADATE_API_HOST + "/api/users/" + user.getId().toString());
            client.execute(get);
            HttpResponse response = client.execute(get);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity);

            user.setCustomData(result);

            EntityUtils.consume(httpEntity);

            return user;

        } catch(Exception e) {

            e.printStackTrace();
            return null;
        }
    }
}