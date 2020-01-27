package com.bedrock.gaodedrawerdemo;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.BottomPopupView;

public class BottomDrawer extends BottomPopupView {
    public BottomDrawer(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_bottom_drawer;

    }

    private ScrollView scrollView;
    private LinearLayout ll_1,ll_2;
    private TextView tvUp1,tvUp2;
    private TextView tvBottom1,tvBottom2;

    @Override
    protected void onCreate() {
        super.onCreate();


        scrollView = findViewById(R.id.root_scroll_view);
        ll_1 = findViewById(R.id.ll_part_1);
        ll_2 = findViewById(R.id.ll_part_2);
        tvUp1 = findViewById(R.id.tv_up_1);
        tvUp2 = findViewById(R.id.tv_up_2);
        tvBottom1 = findViewById(R.id.tv_bottom_1);
        tvBottom2 = findViewById(R.id.tv_bottom_2);

    }


    @Override
    protected void onDismiss() {
        super.onDismiss();
    }
}





















