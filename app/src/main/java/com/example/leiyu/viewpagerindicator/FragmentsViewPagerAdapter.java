package com.example.leiyu.viewpagerindicator;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by leiyu on 2018/6/4.
 */

public class FragmentsViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private FragmentManager fm;
    private String[] title;

    public FragmentsViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fm = fm;
        this.fragments = fragments;
    }

    public FragmentsViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments, String[] title) {
        super(fm);
        this.fm = fm;
        this.fragments = fragments;
        this.title = title;
    }

    public int getCount() {
        return this.fragments.size();
    }

    public Fragment getItem(int position) {
        return (Fragment)this.fragments.get(position);
    }

    public int getItemPosition(Object object) {
        return -2;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);
        return obj;
    }

    public CharSequence getPageTitle(int position) {
        return this.title[position];
    }
}
