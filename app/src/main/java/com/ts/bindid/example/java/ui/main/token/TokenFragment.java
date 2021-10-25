package com.ts.bindid.example.java.ui.main.token;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nimbusds.jwt.SignedJWT;
import com.ts.bindid.example.java.R;

import java.text.ParseException;

public class TokenFragment extends Fragment {

    private static final String RESPONSE_ID_TOKEN = "id_token";

    private String mIdToken;
    private RecyclerView tokenRecyclerView;

    public static TokenFragment newInstance(String param) {
        TokenFragment fragment =  new TokenFragment();
        Bundle args = new Bundle();
        args.putString(RESPONSE_ID_TOKEN, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIdToken = getArguments().getString(RESPONSE_ID_TOKEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.token_fragment, container, false);
        tokenRecyclerView = view.findViewById(R.id.token_values_rv);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            // Get the JWT token and display it in a user friendly format
            SignedJWT jwt = SignedJWT.parse(mIdToken);
            TokenData tokenData = new TokenData(jwt.getPayload().toString());
            tokenRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            tokenRecyclerView.setAdapter(new PassportAdapter(tokenData.getTokens(requireContext())));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}