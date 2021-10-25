package com.ts.bindid.example.java.ui.main.token;

import android.content.Context;

import com.ts.bindid.example.java.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Ran Stone on 21/06/2021.
 *
 * TokenData parses the JWT json data into a readable format
 */
public class TokenData {

    private JSONObject json;

    private String userID;
    private String userAlias;
    private String phoneNumber;
    private String emailAddress;
    private String userRegisteredOn;
    private String userFirstSeen;
    private String userFirstConfirmed;
    private String userLastSeen;
    private String userLastSeenByNetwork;
    private String totalProvidersThatConfirmedUser;
    private String authenticatingDeviceRegistered;
    private String authenticatingDeviceFirstSeen;
    private String authenticatingDeviceConfirmed;
    private String authenticatingDeviceLastSeen;
    private String authenticatingDeviceLastSeenByNetwork;
    private String totalKnownDevices;

    public TokenData(String token){
        try {
            json = new JSONObject(token);

            userID = json.has("sub")? json.getString("sub") : null;
            userAlias = json.has("bindid_alias")? json.getString("bindid_alias") : "Not Set";
            phoneNumber = json.has("phone_number")? json.getString("phone_number") : null;
            emailAddress = json.has("email")? json.getString("email") : "Not Set";
            authenticatingDeviceConfirmed = json.has("acr.ts.bindid.app_bound_cred")?
                    json.getString("acr.ts.bindid.app_bound_cred") : "No";

            // Network info
            if(json.has("bindid_network_info")){
                JSONObject info = json.getJSONObject("bindid_network_info");
                userRegisteredOn = info.has("user_registration_time")?
                        info.getString("user_registration_time") : null;
                userLastSeenByNetwork = info.has("user_last_seen")?
                        info.getString("user_last_seen") : null;
                totalKnownDevices = info.has("device_count")?
                        info.getString("device_count") : "0";
                authenticatingDeviceLastSeenByNetwork =
                        info.has("authenticating_device_last_seen")?
                                info.getString("authenticating_device_last_seen").equals("null")?
                                    null : info.getString("authenticating_device_last_seen")
                                : null;
                totalProvidersThatConfirmedUser = info.has("confirmed_capp_count")?
                        info.getString("confirmed_capp_count") : "0";
                authenticatingDeviceRegistered =
                        info.has("authenticating_device_registration_time")?
                            info.getString("authenticating_device_registration_time") : null;

            }

            // BindID Info
            if(json.has("bindid_info")) {
                JSONObject info = json.getJSONObject("bindid_info");
                userFirstSeen = info.has("capp_first_login")?
                        toDateString(info.getString("capp_first_login")) : null;
                userFirstConfirmed = info.has("capp_first_confirmed_login")?
                        toDateString(info.getString("capp_first_confirmed_login")) : null;
                userLastSeen = info.has("capp_last_login")?
                        toDateString(info.getString("capp_last_login")) : null;
                authenticatingDeviceFirstSeen = info.has("capp_first_login_from_authenticating_device")?
                        toDateString(info.getString("capp_first_login_from_authenticating_device")) : null;
                authenticatingDeviceLastSeen = info.has("capp_last_login_from_authenticating_device")?
                        toDateString(info.getString("capp_last_login_from_authenticating_device")) : null;
            }
            
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public ArrayList<TokenItem> getTokens(Context context){
        ArrayList<TokenItem> list = new ArrayList<>();

        if (userID != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_user_id, userID));
        }
        if(userAlias != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_user_alias, userAlias));
        }
        if(phoneNumber != null) {
            list.add( new TokenItem(context, R.string.ts_bindid_passport_phone_number, phoneNumber));
        }
        if(emailAddress != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_email_address, emailAddress));
        }
        if(userRegisteredOn != null){
            list.add(new TokenItem(context, R.string.ts_bindid_passport_user_registered_on, userRegisteredOn));
        }
        if(userFirstSeen != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_user_first_seen, userFirstSeen));
        }
        if(userFirstConfirmed != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_user_first_confirmed, userFirstConfirmed));
        }
        if(userLastSeen != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_user_last_seen, userLastSeen));
        }
        if(userLastSeenByNetwork != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_user_last_seen_by_network, userLastSeenByNetwork));
        }
        if(totalProvidersThatConfirmedUser != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_total_providers_that_confirmed_user, totalProvidersThatConfirmedUser));
        }
        if(authenticatingDeviceRegistered != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_authenticating_device_registered, authenticatingDeviceRegistered));
        }
        if(authenticatingDeviceFirstSeen !=null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_authenticating_device_first_seen, authenticatingDeviceFirstSeen));
        }
        if(authenticatingDeviceConfirmed != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_authenticating_device_confirmed, authenticatingDeviceConfirmed));
        }
        if(authenticatingDeviceLastSeen != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_authenticating_device_last_seen, authenticatingDeviceLastSeen));
        }
        if(authenticatingDeviceLastSeenByNetwork != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_authenticating_device_last_seen_by_network, authenticatingDeviceLastSeenByNetwork));
        }
        if(totalKnownDevices != null) {
            list.add(new TokenItem(context, R.string.ts_bindid_passport_total_known_devices, totalKnownDevices));
        }

        return list;
    }

    private String toDateString(String date) {
        DateFormat format = new SimpleDateFormat("MMM d, yyyy HH:mm a");
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(date) * 1000);
        return format.format(cal.getTime());
    }

}
