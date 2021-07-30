package com.verify.docverify;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentTypeFragment extends Fragment {
    private ImageView goBackType;
    private CardView cardViewPass, cardViewLicense, cardViewAadhaar, cardViewRation, cardViewPan;

    public DocumentTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document_type, container, false);

        goBackType = view.findViewById(R.id.goBackType);
        cardViewPass = view.findViewById(R.id.cardViewPass);
        cardViewLicense = view.findViewById(R.id.cardViewLicense);
        cardViewAadhaar = view.findViewById(R.id.cardViewAadhaar);
        cardViewRation = view.findViewById(R.id.cardViewRation);
        cardViewPan = view.findViewById(R.id.cardViewPan);

        goBack();
        docType();

        return view;
    }

    private void goBack() {
        goBackType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    private void docType() {
        cardViewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDoc("Passport");
            }
        });
        cardViewLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDoc("Driver License");
            }
        });
        cardViewAadhaar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDoc("Aadhaar Card");
            }
        });
        cardViewRation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDoc("Ration Card");
            }
        });
        cardViewPan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDoc("Pan Card");
            }
        });
    }

    private void selectDoc(String type) {
        Bundle typeBundle = new Bundle();
        typeBundle.putString("docType", type);
        SelectFragment selectFragment = new SelectFragment();
        selectFragment.setArguments(typeBundle);
        getParentFragmentManager().beginTransaction().replace(R.id.libActivity, selectFragment).addToBackStack("").commit();
    }

}