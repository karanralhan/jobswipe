package com.jsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DashBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
    }

    public void editinfo(View view) {
        Intent intent = new Intent(getApplicationContext(), EditInfo.class);
        startActivity(intent);
        return;
    }
}
