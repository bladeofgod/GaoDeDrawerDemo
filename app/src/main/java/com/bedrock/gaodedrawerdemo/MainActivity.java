package com.bedrock.gaodedrawerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.lxj.xpopup.XPopup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test();

    }

    private void test(){
        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void showBottomDrawer(){
        new XPopup.Builder(this)
                .offsetY(-200)
                .asCustom(new BottomDrawer(this))
                .show();
    }
}



















