package com.iosharp.android.ssplayer.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.iosharp.android.ssplayer.PlayerApplication;
import com.iosharp.android.ssplayer.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class FetchLoginInfoTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = FetchLoginInfoTask.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Context mContext;

    private String mUsername;
    private String mPassword;
    private String mService;

    public FetchLoginInfoTask(Context context) {
        mContext = context;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mUsername = mSharedPreferences.getString(mContext.getString(R.string.pref_service_username_key), null);
        mPassword = mSharedPreferences.getString(mContext.getString(R.string.pref_service_password_key), null);
        mService = mSharedPreferences.getString(mContext.getString(R.string.pref_service_key), null);

        mUsername = mUsername.trim();
        mPassword = mPassword.trim();
        mService = mService.trim();
    }

    private String getServiceParam(String service) {
        if (service.equals("live247")){
            return "view247";
        }else if (service.equals("mystreams")){
            return "viewms";
        }else if (service.equals("starstreams")){
            return "viewss";
        }else if (service.equals("mma-tv")) {
            return "mma-tv";
        } else if (service.equals("mma-sr")) {
            return "mma-sr";
        } else if (service.equals("streamtvnow")){
            return "viewstvn";
        }else{
            return null;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final String USER_AGENT = PlayerApplication.getUserAgent(mContext);

        final OkHttpClient client = new OkHttpClient();
        String loginJsonStr = null;

        try {
            final String SMOOTHSTREAMS_BASE_URL = "http://smoothstreams.tv/schedule/admin/dash_new/hash_api.php"; //getServiceBaseUrl(mService);

            final String USERNAME_PARAM = "username";
            final String PASSWORD_PARAM = "password";
            final String SITE_PARAM = "site";

            /*  Uri.parse is stripping out characters like + signs. Since we're passing +'s we can't do that.
			Uri builtUri = Uri.parse(SMOOTHSTREAMS_BASE_URL).buildUpon()
					.appendQueryParameter(USERNAME_PARAM, mUsername)
					.appendQueryParameter(PASSWORD_PARAM, URLEncoder.encode(mPassword, "UTF-8"))
					.appendQueryParameter(SITE_PARAM, getServiceParam(mService))
					.build();

            */

            String builtUrl = SMOOTHSTREAMS_BASE_URL + "?" + USERNAME_PARAM + "=" + Uri.encode(mUsername) + "&" + PASSWORD_PARAM + "=" + Uri.encode(mPassword) + "&" + SITE_PARAM + "=" + getServiceParam(mService);

            URL url = new URL(builtUrl);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            loginJsonStr = response.body().string();

        } catch (MalformedURLException e) {
            Crashlytics.logException(e);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        parseLoginResponse(loginJsonStr);
        return null;
    }

    private void parseLoginResponse(String responseStr) {
        try {

            JSONObject response = new JSONObject(responseStr);
            if (response.has("error")) {
                String message = response.getString("error");
                showToastMethod("ERROR: " + message);
            }else if (response.has("hash")) {
                String password = response.getString("hash");
                setServiceCredentials(mUsername, password);
            }else{
                showToastMethod("ERROR: Unknown response!");
                Log.e(TAG, "Unknown response!");
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }
    }

    public void setServiceCredentials(String username, String password) {
        mEditor = mSharedPreferences.edit();
        mEditor.putString(mContext.getString(R.string.pref_ss_uid_key), mUsername);
        mEditor.putString(mContext.getString(R.string.pref_ss_password_key), password);
        mEditor.commit();

        showToastMethod("Saved service id and password");
        Log.i(TAG,
                "SUCCESS: serviceId: " + username
                        + ", servicePassword: "
                        + password.replaceAll(".", "*"));

    }

    private void showToastMethod(String text) {
        final String toastText = text;

        Handler handler = new Handler(mContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
            }
        });
    }

}