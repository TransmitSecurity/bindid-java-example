package com.ts.bindid.example.java.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.ts.bindid.XmBindIdAuthenticationRequest;
import com.ts.bindid.XmBindIdConfig;
import com.ts.bindid.XmBindIdError;
import com.ts.bindid.XmBindIdErrorCode;
import com.ts.bindid.XmBindIdExchangeTokenRequest;
import com.ts.bindid.XmBindIdExchangeTokenResponse;
import com.ts.bindid.XmBindIdResponse;
import com.ts.bindid.XmBindIdScopeType;
import com.ts.bindid.XmBindIdSdk;
import com.ts.bindid.XmBindIdServerEnvironment;
import com.ts.bindid.XmBindIdServerEnvironmentMode;
import com.ts.bindid.example.java.R;
import com.ts.bindid.example.java.ui.main.token.TokenFragment;
import com.ts.bindid.impl.XmBindIdErrorImpl;
import com.ts.bindid.util.ObservableFuture;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class MainFragment extends Fragment {

    Button loginBtn;
    ProgressBar progressBar;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.main_fragment, container, false);
        progressBar = view.findViewById(R.id.main_progress_bar);
        loginBtn = view.findViewById(R.id.main_login_btn);
        loginBtn.setEnabled(false);
        loginBtn.setOnClickListener(view1 -> {
            authenticate(requireContext());
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure and initialise the BindID SDK
        initBindId(requireActivity().getApplicationContext());
    }

    /**
     * Configure the BindID SDK with your client ID, and to work with the BindID sandbox environment
     */
    public void initBindId(Context applicationContext){
        XmBindIdSdk.getInstance().initialize(
                XmBindIdConfig.create(
                        applicationContext,
                        XmBindIdServerEnvironment.createWithMode(XmBindIdServerEnvironmentMode.Sandbox),
                        applicationContext.getString(R.string.bindid_client_id)
                ))
                .addListener(new ObservableFuture.Listener<Boolean, XmBindIdError>(){

                    @Override
                    public void onComplete(@NotNull Boolean aBoolean) {
                        Timber.i("SDK initialized");
                        loginBtn.setEnabled(true);
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onReject(@NotNull XmBindIdError xmBindIdError) {
                        Timber.e("SDK failed to initialize: " + xmBindIdError.getCode() + "\n" + xmBindIdError.getMessage());
                    }
                });
    }

    /**
     * Authenticate the user
     */
    public void authenticate(Context context) {
        XmBindIdAuthenticationRequest request =
                XmBindIdAuthenticationRequest.create(context.getString(R.string.bindid_redirect_uri));
        request.setUsePkce(true);
        request.setScope(Arrays.asList(XmBindIdScopeType.OpenId, XmBindIdScopeType.Email, XmBindIdScopeType.NetworkInfo));
        XmBindIdSdk.getInstance().authenticate(request)
                .addListener(new ObservableFuture.Listener<XmBindIdResponse, XmBindIdError>() {
                    @Override
                    public void onComplete(XmBindIdResponse xmBindIdResponse) {
                        // Do when using PKCE
                        exchange(xmBindIdResponse);
                    }

                    @Override
                    public void onReject(XmBindIdError xmBindIdError) {
                        onError(xmBindIdError);
                    }
                });
    }

    /**
     * Exchange the authentication response for the ID and access token using a PKCE token exchange
     */
    public void exchange(XmBindIdResponse response) {
        XmBindIdSdk.getInstance().exchangeToken(
                XmBindIdExchangeTokenRequest.create(response)
        ).addListener(new ObservableFuture.Listener<XmBindIdExchangeTokenResponse, XmBindIdError>() {
            @Override
            public void onComplete(XmBindIdExchangeTokenResponse tokenResponse) {

                // Validate the tokenResponse
                // 1. get publicKey from BindID server
                // 2. validate JWT
                fetchBindIDPublicKey(new fetchBindIDPublicKeyListener() {
                    @Override
                    public void onResponse(String publicKey) {
                        try {
                            boolean isValid = SignedJWT.parse(tokenResponse.getIdToken())
                                    .verify(new RSASSAVerifier(RSAKey.parse(publicKey)));

                            if(isValid){
                                // When connected to your company's backend, send the ID and access tokens
                                // to be processed
                                sendTokenToServer(tokenResponse.getAccessToken(), tokenResponse.getIdToken());

                                // Once authentication and token exchange are done go to
                                // TokenFragment to display the BindID token parameters
                                MainFragment.this.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getActivity().getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.container, TokenFragment.newInstance(tokenResponse.getIdToken()))
                                                .commitNow();
                                    }
                                });

                            } else {
                                XmBindIdError xmBindIdError = new XmBindIdErrorImpl(
                                        XmBindIdErrorCode.InvalidResponse, "Invalid JWT signature");
                                onError(xmBindIdError);
                            }

                        } catch (Exception e) {
                            XmBindIdError xmBindIdError = new XmBindIdErrorImpl(
                                    XmBindIdErrorCode.InvalidResponse, e.getMessage());
                            onError(xmBindIdError);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        XmBindIdError xmBindIdError = new XmBindIdErrorImpl(
                                XmBindIdErrorCode.InvalidResponse, "Invalid JWT signature");
                        onError(xmBindIdError);
                    }
                });
            }

            @Override
            public void onReject(XmBindIdError xmBindIdError) {
                onError(xmBindIdError);
            }
        });
    }

    // sendTokenToServer should send the ID and access tokens received upon successful authentication
    // to your backend server, where it will be processed
    public void sendTokenToServer(String one, String two) {
        // Add code to send the ID and access token to your application server here
    }

    private interface fetchBindIDPublicKeyListener {
        void onResponse(String publicKey);
        void onFailure(String error);
    }
    /**
     * Fetch the public key from the BindID jwks endpoint
     * @param listener
     */
    private void fetchBindIDPublicKey(fetchBindIDPublicKeyListener listener) {

        OkHttpClient client = new OkHttpClient();

        String url = MainFragment.this.getString(R.string.bindid_host) + "/jwks";
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    listener.onFailure("Unexpected code " + response);
                } else {
                    // Serialize the response and convert it to an array of key objects
                    String responseData = Objects.requireNonNull(response.body()).string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray keys = json.has("keys") ? json.getJSONArray("keys") : null;

                        // Find the key that contains the "sig" value in the "use" key. Return the publicKey in it
                        for (int i = 0; i < keys.length(); i++) {
                            JSONObject key = keys.getJSONObject(i);
                            if (key.get("use").equals("sig")) {
                                listener.onResponse(key.toString());
                                return;
                            }
                        }
                        listener.onFailure("No signature key in publicKey");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void onError(XmBindIdError bindIdError){
        String err = bindIdError.getMessage().equals("") ?  bindIdError.getCode().name() :
                bindIdError.getMessage() + ": " + bindIdError.getCode().name();
        Timber.e(err);
        Snackbar.make(getView(),  err, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorError))
                .show();
    }

}