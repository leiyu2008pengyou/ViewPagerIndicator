package com.example.leiyu.viewpagerindicator;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.indicator.PagerSlidingTabStrip;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    PagerSlidingTabStrip mPagerSlidingTabStrip;
    PagerSlidingTabStrip mPagerSlidingTabStrip2;
    PagerSlidingTabStrip mPagerSlidingTabStrip3;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews(){
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.id_pager_s_t_s);
        mPagerSlidingTabStrip2 = (PagerSlidingTabStrip) findViewById(R.id.id_pager_s_t_s_2);
        mPagerSlidingTabStrip3 = (PagerSlidingTabStrip) findViewById(R.id.id_pager_s_t_s_3);
        mViewPager = (ViewPager) findViewById(R.id.id_vp);
        ArrayList<Fragment> fragments = new ArrayList<>();
        FirstFragment firstFragment = new FirstFragment();
        SecFragment secFragment = new SecFragment();
        ThdFragment thdFragment = new ThdFragment();
        FouthFragment fouthFragment = new FouthFragment();
        fragments.add(firstFragment);
        fragments.add(secFragment);
        fragments.add(thdFragment);
        fragments.add(fouthFragment);

        String[] array = new String[4];
        array[0] = "第一个";
        array[1] = "第二个";
        array[2] = "第三个";
        array[3] = "第四个";
        FragmentsViewPagerAdapter adapter = new FragmentsViewPagerAdapter(getSupportFragmentManager(), fragments, array);
        mViewPager.setAdapter(adapter);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip2.setViewPager(mViewPager);
        mPagerSlidingTabStrip2.setIsIndicatorTop(true);
        mPagerSlidingTabStrip3.setViewPager(mViewPager);
    }
}
