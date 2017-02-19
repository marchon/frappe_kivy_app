package io.frappe.frappe_authenticator.authentication;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import io.frappe.frappe_authenticator.R;
import org.json.JSONObject;
import org.json.JSONException;

import android.net.Uri;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import static io.frappe.frappe_authenticator.authentication.AccountGeneral.sServerAuthenticate;

/**
 * The Authenticator activity.
 *
 * Called by the Authenticator and in charge of identifing the user.
 *
 * It sends back to the Authenticator the result.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    private final String TAG = this.getClass().getSimpleName();

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    /**
     * Called when the activity is first created.
     */
    private static String CLIENT_ID = "5647445673";
    //Use your own client id
    private static String CLIENT_SECRET ="b1319acb4d";
    //Use your own client secret
    private static String REDIRECT_URI="http://localhost";
    private static String GRANT_TYPE="authorization_code";
    private static String TOKEN_URL ="http://192.168.0.107:8000/api/method/frappe.integration_broker.oauth2.get_token";
    private static String OAUTH_URL ="http://192.168.0.107:8000/api/method/frappe.integration_broker.oauth2.authorize";
    private static String OAUTH_SCOPE="all openid";
    //Change the Scope as you need
    WebView web;
    Button auth;
    SharedPreferences pref;
    TextView Access;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        Access =(TextView)findViewById(R.id.Access);
        auth = (Button)findViewById(R.id.auth);
        auth.setOnClickListener(new View.OnClickListener() {
            Dialog auth_dialog;
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                auth_dialog = new Dialog(AuthenticatorActivity.this);
                auth_dialog.setContentView(R.layout.auth_dialog);
                web = (WebView)auth_dialog.findViewById(R.id.webv);
                web.getSettings().setJavaScriptEnabled(true);
                web.loadUrl(OAUTH_URL+"?redirect_uri="+REDIRECT_URI+"&response_type=code&client_id="+CLIENT_ID+"&scope="+OAUTH_SCOPE);
                web.setWebViewClient(new WebViewClient() {
 
                    boolean authComplete = false;
                    Intent resultIntent = new Intent();
 
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon){
                     super.onPageStarted(view, url, favicon);
 
                    }
                 String authCode;
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
 
                        if (url.contains("?code=") && authComplete != true) {
                            Uri uri = Uri.parse(url);
                            authCode = uri.getQueryParameter("code");
                            Log.i("", "CODE : " + authCode);
                            authComplete = true;
                            resultIntent.putExtra("code", authCode);
                            AuthenticatorActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                            setResult(Activity.RESULT_CANCELED, resultIntent);
 
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("Code", authCode);
                            edit.commit();
                            auth_dialog.dismiss();
                            new TokenGet().execute();
                           Toast.makeText(getApplicationContext(),"Authorization Code is: " +authCode, Toast.LENGTH_SHORT).show();
                        }else if(url.contains("error=access_denied")){
                            Log.i("", "ACCESS_DENIED_HERE");
                            resultIntent.putExtra("code", authCode);
                            authComplete = true;
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
 
                            auth_dialog.dismiss();
                        }
                    }
                });
                auth_dialog.show();
                auth_dialog.setTitle("Authorize Frappe");
                auth_dialog.setCancelable(true);
            }
        });
    }
 
    private class TokenGet extends AsyncTask<String, String, JSONObject> {
            private ProgressDialog pDialog;
            String Code;
           @Override
           protected void onPreExecute() {
               super.onPreExecute();
               pDialog = new ProgressDialog(AuthenticatorActivity.this);
               pDialog.setMessage("Contacting Google ...");
               pDialog.setIndeterminate(false);
               pDialog.setCancelable(true);
               Code = pref.getString("Code", "");
               pDialog.show();
           }
 
           @Override
           protected JSONObject doInBackground(String... args) {
               GetAccessToken jParser = new GetAccessToken();
               JSONObject json = jParser.gettoken(TOKEN_URL,Code,CLIENT_ID,CLIENT_SECRET,REDIRECT_URI,GRANT_TYPE);
               return json;
           }
 
            @Override
            protected void onPostExecute(JSONObject json) {
                pDialog.dismiss();
                if (json != null){
 
                       try {
 
                        String tok = json.getString("access_token");
                        String expire = json.getString("expires_in");
                        String refresh = json.getString("refresh_token");
 
                           Log.d("Token Access", tok);
                           Log.d("Expire", expire);
                           Log.d("Refresh", refresh);
                           auth.setText("Authenticated");
                           Access.setText("Access Token:"+tok+"nExpires:"+expire+"nRefresh Token:"+refresh);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
 
                        }else{
                       Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                       pDialog.dismiss();
                   }
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    // public void submit() {

    //     final String userName = ((TextView) findViewById(R.id.accountName)).getText().toString();
    //     final String userPass = ((TextView) findViewById(R.id.accountPassword)).getText().toString();

    //     final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

    //     new AsyncTask<String, Void, Intent>() {

    //         @Override
    //         protected Intent doInBackground(String... params) {

    //             Log.d("frappe", TAG + "> Started authenticating");

    //             String authtoken = null;
    //             Bundle data = new Bundle();
    //             try {
    //                 authtoken = sServerAuthenticate.userSignIn(userName, userPass, mAuthTokenType);

    //                 data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
    //                 data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
    //                 data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
    //                 data.putString(PARAM_USER_PASS, userPass);

    //             } catch (Exception e) {
    //                 data.putString(KEY_ERROR_MESSAGE, e.getMessage());
    //             }

    //             final Intent res = new Intent();
    //             res.putExtras(data);
    //             return res;
    //         }

    //         @Override
    //         protected void onPostExecute(Intent intent) {
    //             if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
    //                 Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
    //             } else {
    //                 finishLogin(intent);
    //             }
    //         }
    //     }.execute

    // }

    private void finishLogin(Intent intent) {
        Log.d("frappe", TAG + "> finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d("frappe", TAG + "> finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d("frappe", TAG + "> finishLogin > setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

}
