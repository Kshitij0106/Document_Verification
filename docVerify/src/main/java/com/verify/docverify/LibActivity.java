package com.verify.docverify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class LibActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib);

        getSupportFragmentManager().beginTransaction().add(R.id.libActivity, new WelcomeFragment()).commit();

    }
}