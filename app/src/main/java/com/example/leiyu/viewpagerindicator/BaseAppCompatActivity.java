package com.example.leiyu.viewpagerindicator;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.example.leiyu.utils.StatusBarUtil;


/**
 * 状态栏沉浸
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    public static final int STATEBAR_DEFAULT_ALPHA = 90;
    private long lastClickTime;
    private int res;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtra();
        res = StatusBarUtil.StatusBarLightMode(this);
        setContentView();
        initView();
        setStatusBar();
        initToolBar();
        queryData();

    }

    protected void setContentView() {
        setContentView(bindLayout());
    }


    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), res == -1 ? STATEBAR_DEFAULT_ALPHA : 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected abstract void getExtra();

    protected abstract int bindLayout();

    protected void initView() {
    }


    protected abstract void queryData();


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isFastDoubleClick()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (timeD < 250) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    private void initToolBar() {

    }

    public abstract void goBack();

    public abstract String setTitle();

    protected void setTitle(String title) {

    }

    /**
     * 处理back键盘
     * @return 处理完后是否finish
     */
    protected boolean handleBackPressed() {
        return true;
    }

    @Override
    public void onBackPressed() {
        if(handleBackPressed()) {
            super.onBackPressed();
        }
    }
}
