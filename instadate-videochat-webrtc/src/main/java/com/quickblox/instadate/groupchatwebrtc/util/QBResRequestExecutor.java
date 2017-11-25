package com.quickblox.instadate.groupchatwebrtc.util;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import com.quickblox.instadate.groupchatwebrtc.utils.Consts;

import java.util.ArrayList;
import java.util.Collection;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

/**
 * Created by tereha on 26.04.16.
 */
public class QBResRequestExecutor {
    private String TAG = QBResRequestExecutor.class.getSimpleName();

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

    public void postQbUserToInstadateAPI(String QbId,String AccessToken,String Birthday,String Title,String About,String Location) {

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

           //if(file != null)
            {
                //entityBuilder.addBinaryBody(IMAGE, file);
            }

            HttpEntity entity = entityBuilder.build();

            post.setEntity(entity);

            HttpResponse response = client.execute(post);

            HttpEntity httpEntity = response.getEntity();

            //result = EntityUtils.toString(httpEntity);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}