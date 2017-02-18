package io.frappe.frappe_authenticator.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FrappeAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        FrappeAuthenticator authenticator = new FrappeAuthenticator(this);
        return authenticator.getIBinder();
    }
}
