package com.iosharp.android.ssplayer.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.iosharp.android.ssplayer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchLoginInfoTask extends AsyncTask<Void, Void, String> {

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
    }


    private String getServiceBaseUrl(String service) {
        if (service.equals("mma-tv")) {
            return "http://www.mma-tv.net/loginForm.php";
        } else if (service.equals("starstreams")) {
            return "http://starstreams.tv/t.php";
        } else {
            return "http://smoothstreams.tv/login.php";
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String loginJsonStr = null;

        try {
            final String SMOOTHSTREAMS_BASE_URL = getServiceBaseUrl(mService);

            final String USERNAME_PARAM = "username";
            final String PASSWORD_PARAM = "password";
            final String SITE_PARAM = "site";

            Uri builtUri = Uri.parse(SMOOTHSTREAMS_BASE_URL).buildUpon()
                    .appendQueryParameter(USERNAME_PARAM, mUsername)
                    .appendQueryParameter(PASSWORD_PARAM, mPassword)
                    .appendQueryParameter(SITE_PARAM, mService)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            loginJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        parseLoginResponse(loginJsonStr);

        return null;
    }

    private void parseLoginResponse (String responseStr) {
        try {

            JSONObject response = new JSONObject(responseStr);

            if (response.has("id")) {
                String username = response.getString("id");
                String password = response.getString("password");

                setServiceCredentials(username, password);
            } else if (response.has("error")) {
                String message = response.getString("error");
                showToastMethod("ERROR: " + message);
            } else {
                Log.e(TAG, "Unknown response!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setServiceCredentials(String username, String password) {
        mEditor = mSharedPreferences.edit();
        mEditor.putString(mContext.getString(R.string.pref_ss_uid_key), username);
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