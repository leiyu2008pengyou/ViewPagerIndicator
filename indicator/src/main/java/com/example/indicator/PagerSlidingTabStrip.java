package com.example.indicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by leiyu on 2018/6/4.
 */

public class PagerSlidingTabStrip extends HorizontalScrollView {
    private static final String TAG = "lf_pager_sliding_tabstrip";

    public interface IconTabProvider {
        int getPageIconResId(int position);
    }

    // @formatter:off
    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textColor
    };
    // @formatter:on

    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams matchparentTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;

    private final PageListener pageListener;
    public ViewPager.OnPageChangeListener delegatePageListener;

    private LinearLayout tabsContainer;
    private ViewPager pager;

    private int tabCount;

    private int selectedPosition = 0;
    private int currentPosition = 0;
    private float currentPositionOffset = 0f;

    private Paint rectPaint;
    private Paint dividerPaint;

    private int indicatorColor = 0xFF666666;
    private int underlineColor = 0x1A000000;
    private int dividerColor = 0x1A000000;

    private boolean shouldExpand = false;
    private boolean textAllCaps = true;
    private boolean indicatorWrap = false;//导航线的宽是否和字体的宽一样
    private boolean selectedTabTextBold;
    private boolean indicatorCircle;//下方导航条是圆形
    private int indicatorCircleRadius = 3;//圆形半径

    private int scrollOffset = 52;
    private boolean isIndicatorTop = false;    // 指示器线在顶部，特殊需求可以使用set方法设置
    private int indicatorHeight = 8;
    private int underlineHeight = 2;
    private int dividerPadding = 12;
    private int tabPadding = 24;
    private int dividerWidth = 1;

    private int tabTextSize = 12;
    private int tabTextColor = 0xFF666666;
    private int selectedTabTextColor = 0xFF45c01a;
    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.NORMAL;

    private int lastScrollX = 0;

    private int tabBackgroundResId = 0;

    private Locale locale;

    private float zoomMax = 0.0f;
    private int mState;
    private static final int IDLE = 0;
    private static final int GOING_LEFT = 1;
    private static final int GOING_RIGHT = 2;
    private boolean mFadeEnabled = false;
    private List<Map<String, View>> tabViews = new ArrayList<Map<String, View>>();

    private int oldPage;

    private Rect mRectTabText = new Rect();
    private Paint mPaintTabText = new Paint();
    private int mTabsTextWidth;//所有tab的实际字体的宽
    private String mPageTitle;
    private int mPagePosition = -1;

    public PagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ResourceType")
    public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(tabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
        indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
        underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
        dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
        tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
        tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);
        indicatorCircleRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorCircleRadius, dm);

        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColor(1, tabTextColor);

        a.recycle();

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.lf_pager_sliding_tabstrip);

        indicatorColor = a.getColor(R.styleable.lf_pager_sliding_tabstrip_pstsIndicatorColor, indicatorColor);
        underlineColor = a.getColor(R.styleable.lf_pager_sliding_tabstrip_pstsUnderlineColor, underlineColor);
        dividerColor = a.getColor(R.styleable.lf_pager_sliding_tabstrip_pstsDividerColor, dividerColor);
        indicatorHeight = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsIndicatorHeight, indicatorHeight);
        underlineHeight = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsUnderlineHeight, underlineHeight);
        dividerWidth = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsDividerWidth, dividerWidth);
        dividerPadding = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsDividerPadding, dividerPadding);
        tabPadding = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsTabPaddingLeftRight, tabPadding);
        tabBackgroundResId = a.getResourceId(R.styleable.lf_pager_sliding_tabstrip_pstsTabBackground, tabBackgroundResId);
        shouldExpand = a.getBoolean(R.styleable.lf_pager_sliding_tabstrip_pstsShouldExpand, shouldExpand);
        scrollOffset = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsScrollOffset, scrollOffset);
        textAllCaps = a.getBoolean(R.styleable.lf_pager_sliding_tabstrip_pstsTextAllCaps, textAllCaps);
        selectedTabTextColor = a.getColor(R.styleable.lf_pager_sliding_tabstrip_pstsSelectedTextColor, selectedTabTextColor);
        tabTextColor = a.getColor(R.styleable.lf_pager_sliding_tabstrip_pstsDefalutTextColor, tabTextColor);
        tabTextSize = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsTextSize, tabTextSize);
        zoomMax = a.getFloat(R.styleable.lf_pager_sliding_tabstrip_pstsScaleZoomMax, zoomMax);
        //宽为match_parent ，指示器需手动计算的问题,这个属性设置为true,则指示器的位置和字体的宽一样
        indicatorWrap = a.getBoolean(R.styleable.lf_pager_sliding_tabstrip_pstsIndicatorWrap, indicatorWrap);
        selectedTabTextBold = a.getBoolean(R.styleable.lf_pager_sliding_tabstrip_pstsSelectedTextBold, selectedTabTextBold);
        indicatorCircle = a.getBoolean(R.styleable.lf_pager_sliding_tabstrip_pstsIndicatorCircle, indicatorCircle);
        indicatorCircleRadius = a.getDimensionPixelSize(R.styleable.lf_pager_sliding_tabstrip_pstsIndicatorCircleRadius, indicatorCircleRadius);

        a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);

        defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        matchparentTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if (locale == null) {
            locale = getResources().getConfiguration().locale;
        }
        pageListener = new PageListener();

        mPaintTabText.setAntiAlias(true);
    }

    public void setViewPagerAndTitles(ViewPager pager, ArrayList<String> titles) {
        this.pager = pager;
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        this.pager.addOnPageChangeListener(pageListener);
        notifyDataSetChanged(titles);
    }

    public void setViewPager(ViewPager pager) {
        this.pager = pager;
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        pager.addOnPageChangeListener(pageListener);
        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged() {

        tabsContainer.removeAllViews();
        mTabsTextWidth = 10;

        tabCount = pager.getAdapter().getCount();

        if (indicatorWrap) {
            for (int i = 0; i < tabCount; i++) {
                String s = pager.getAdapter().getPageTitle(i).toString();
                //计算所有tab的实际字体的宽
                mPaintTabText.setTextSize(tabTextSize);
                mPaintTabText.getTextBounds(s, 0, s.length(), mRectTabText);
                mTabsTextWidth += mRectTabText.width();
            }
            //如果底部导航是wrap,则重新计算tabPadding
            mTabsTextWidth = mTabsTextWidth + tabCount * 5 * 2;
            if (mTabsTextWidth > 0) {
                tabPadding = (getResources().getDisplayMetrics().widthPixels - mTabsTextWidth) / tabCount / 2;
            }
        }

        for (int i = 0; i < tabCount; i++) {

            if (pager.getAdapter() instanceof IconTabProvider) {
                addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
            } else {
                if(mPagePosition >= 0 && i == mPagePosition){
                    if(mPageTitle != null && !"".equals(mPageTitle)){
                        addTextTab(i, mPageTitle);
                    }else{
                        addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
                    }
                }else{
                    addTextTab(i, pager.getAdapter().getPageTitle(i).toString());
                }
            }

        }

        updateTabStyles();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                currentPosition = pager.getCurrentItem();
                scrollToChild(currentPosition, 0);
            }
        });

    }

    public void setPageTitle(String pageTitle){
        mPageTitle = pageTitle;
    }

    public void setmPagePosition(int pagePosition){
        mPagePosition = pagePosition;
    }

    public void notifyDataSetChanged(ArrayList<String> titles) {
        mTabsTextWidth = 10;
        tabsContainer.removeAllViews();

        tabCount = pager.getAdapter().getCount();

        if (indicatorWrap) {
            for (int i = 0; i < tabCount; i++) {
                String s = pager.getAdapter().getPageTitle(i).toString();
                //计算所有tab的实际字体的宽
                mPaintTabText.setTextSize(tabTextSize);
                mPaintTabText.getTextBounds(s, 0, s.length(), mRectTabText);
                mTabsTextWidth += mRectTabText.width();
            }
            //如果底部导航是wrap,则重新计算tabPadding
            mTabsTextWidth = mTabsTextWidth + tabCount * 5 * 2;
            if (mTabsTextWidth > 0) {
                tabPadding = (getResources().getDisplayMetrics().widthPixels - mTabsTextWidth) / tabCount / 2;
            }
        }

        for (int i = 0; i < tabCount; i++) {

            if (pager.getAdapter() instanceof IconTabProvider) {
                addIconTab(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
            } else {
                addTextTab(i, titles.get(i));
            }

        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                currentPosition = pager.getCurrentItem();
                scrollToChild(currentPosition, 0);
                updateTabStyles();
            }
        });

    }


    private void addTextTab(final int position, String title) {

        TextView tab = new TextView(getContext());
        tab.setText(title);
        tab.setIncludeFontPadding(false);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
//        tab.setTextColor(tabTextColor);
//        tab.setTextSize(14);

        TextView tab2 = new TextView(getContext());
        tab2.setText(title);
        tab2.setIncludeFontPadding(false);
        tab2.setGravity(Gravity.CENTER);
        tab2.setSingleLine();
//        tab2.setTextColor(selectedTabTextColor);
//        tab2.setTextSize(14);

        addTab(position, tab, tab2);

    }

    private void addIconTab(final int position, int resId) {

        ImageButton tab = new ImageButton(getContext());
        tab.setImageResource(resId);

        addTab(position, tab, null);

    }

    private void addTab(final int position, View tab, View tab2) {
        tab.setFocusable(true);
        tab.setPadding(tabPadding, 0, tabPadding, 0);
        tab2.setFocusable(true);
        tab2.setPadding(tabPadding, 0, tabPadding, 0);
        TitleClickView titleClickView = new TitleClickView(getContext());
        titleClickView.addView(tab, 0, matchparentTabLayoutParams);
        titleClickView.addView(tab2, 1, matchparentTabLayoutParams);
        tabsContainer.addView(titleClickView, position, shouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
        titleClickView.setDoubleSingleClickListener(new TitleClickView.DoubleSingleClickListener() {
            @Override
            public void onDoubleTap(MotionEvent e) {
                //cb
                if (mOnPagerTitleItemClickListener != null) {
                    mOnPagerTitleItemClickListener.onDoubleClickItem(position);
                }
            }

            @Override
            public void onSingleTapConfirmed(MotionEvent e) {
                mFadeEnabled = false;//点击时没有文字颜色渐变效果
                //点击时没有切换的动画
                pager.setCurrentItem(position, false);
                currentPosition = position;
                scrollToChild(position, 0);//滚动HorizontalScrollView
//				invalidate();//更新线的位置
                //cb
                if (mOnPagerTitleItemClickListener != null) {
                    mOnPagerTitleItemClickListener.onSingleClickItem(position);
                }
            }
        });

        Map<String, View> map = new HashMap<>();


        tab.setAlpha(1);
        map.put("normal", tab);

        tab2.setAlpha(0);
        map.put("selected", tab2);

        tabViews.add(position, map);


    }

    public void updateTabStyles() {

//		for (int i = 0; i < tabCount; i++) {
//
//			View v = tabsContainer.getChildAt(i);
//
//			v.setBackgroundResource(tabBackgroundResId);
//
//			if (v instanceof TextView) {
//
//				TextView tab = (TextView) v;
//				tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
//				tab.setTypeface(tabTypeface, tabTypefaceStyle);
//				tab.setTextColor(tabTextColor);
//
//				// setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
//				// pre-ICS-build
//				if (textAllCaps) {
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//						tab.setAllCaps(true);
//					} else {
//						tab.setText(tab.getText().toString().toUpperCase(locale));
//					}
//				}
//				if (i == selectedPosition) {
//					tab.setTextColor(selectedTabTextColor);
//				}
//			}
//		}


        for (int i = 0; i < tabCount; i++) {
            FrameLayout frameLayout = (FrameLayout) tabsContainer.getChildAt(i);
            frameLayout.setBackgroundResource(tabBackgroundResId);
            for (int j = 0; j < frameLayout.getChildCount(); j++) {
                View v = frameLayout.getChildAt(j);
                if (v instanceof TextView) {
                    TextView tab = (TextView) v;
                    tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
                    tab.setTypeface(tabTypeface, tabTypefaceStyle);
                    tab.setPadding(tabPadding, 0, tabPadding, 0);
                    if (j == 0) {
                        tab.setTextColor(tabTextColor);//normal
                    } else {
                        tab.setTextColor(selectedTabTextColor);//selected
                    }
                    tabViews.get(i).get("normal").setAlpha(1);
                    tabViews.get(i).get("selected").setAlpha(0);

                    //set normal  Scale

                    frameLayout.setPivotX(frameLayout.getMeasuredWidth() * 0.5f);
                    frameLayout.setPivotY(frameLayout.getMeasuredHeight() * 0.5f);
                    frameLayout.setScaleX(1f);
                    frameLayout.setScaleY(1f);

                    // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
                    // pre-ICS-build
                    if (textAllCaps) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            tab.setAllCaps(true);
                        } else {
                            tab.setText(tab.getText().toString().toUpperCase(locale));
                        }
                    }
                    if (i == selectedPosition) {
                        tabViews.get(i).get("normal").setAlpha(0);
                        tabViews.get(i).get("selected").setAlpha(1);
                        //set select  Scale
                        frameLayout.setPivotX(frameLayout.getMeasuredWidth() * 0.5f);
                        frameLayout.setPivotY(frameLayout.getMeasuredHeight() * 0.5f);
                        frameLayout.setScaleX(1 + zoomMax);
                        frameLayout.setScaleY(1 + zoomMax);
                        if (selectedTabTextBold) {
                            TextView oldTV_1 = (TextView) tabViews.get(i).get("normal");
                            oldTV_1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            TextView oldTV_2 = (TextView) tabViews.get(i).get("selected");
                            oldTV_2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        }
                    }
                }
            }
        }
    }

    private void scrollToChild(int position, int offset) {

        if (tabCount == 0) {
            return;
        }

        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            //不居中的
            // smoothScrollTo(newScrollX, 0);

            //以下是当tab很多时，点击屏幕右边的，点击的那个居中!!!
            int k = tabsContainer.getChildAt(position).getMeasuredWidth();
            int l = tabsContainer.getChildAt(position).getLeft() + offset;
            int i2 = l + k / 2 - this.getMeasuredWidth() / 2;
            smoothScrollTo(i2, 0);

        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || tabCount == 0) {
            return;
        }

        final int height = getHeight();

        // draw underline
        rectPaint.setColor(underlineColor);
        canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);

        // draw indicator line
        rectPaint.setColor(indicatorColor);

        // default: line below current tab
        View currentTab = tabsContainer.getChildAt(currentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();
        float lineWidth = currentTab.getWidth();

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

            View nextTab = tabsContainer.getChildAt(currentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
            lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
        }

//        canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);

        if (isIndicatorTop) {
            if(indicatorCircle){
                canvas.drawCircle(lineLeft + lineWidth / 2, height - indicatorCircleRadius, indicatorCircleRadius, rectPaint);
            }else{
                canvas.drawRect(lineLeft + tabPadding - 5 + getPaddingLeft(), currentTab.getTop(), lineRight - tabPadding + 5 + getPaddingLeft(), indicatorHeight, rectPaint);
            }
        } else {
            if(indicatorCircle){
                canvas.drawCircle(lineLeft + lineWidth / 2, height - indicatorCircleRadius, indicatorCircleRadius, rectPaint);
            }else{
                canvas.drawRect(lineLeft + tabPadding - 5 + getPaddingLeft(), height - indicatorHeight, lineRight - tabPadding + 5 + getPaddingLeft(), height, rectPaint);
            }
        }

        // draw divider

        dividerPaint.setColor(dividerColor);
        for (int i = 0; i < tabCount - 1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
        }
    }

    public int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }


    public void setFadeEnabled(boolean enabled) {
        mFadeEnabled = enabled;
    }

    protected void animateFadeScale(View left, View right, float positionOffset, int position) {
        if (mState != IDLE) {
            if (left != null) {
                tabViews.get(position).get("normal").setAlpha(positionOffset);
                tabViews.get(position).get("selected").setAlpha(1 - positionOffset);

                float mScale = 1 + zoomMax - zoomMax * positionOffset;


                left.setPivotX(left.getMeasuredWidth() * 0.5f);
                left.setPivotY(left.getMeasuredHeight() * 0.5f);
                left.setScaleX(mScale);
                left.setScaleY(mScale);
            }
            if (right != null) {

                tabViews.get(position + 1).get("normal").setAlpha(1 - positionOffset);
                tabViews.get(position + 1).get("selected").setAlpha(positionOffset);
                float mScale = 1 + zoomMax * positionOffset;

                right.setPivotX(right.getMeasuredWidth() * 0.5f);
                right.setPivotY(right.getMeasuredHeight() * 0.5f);
                right.setScaleX(mScale);
                right.setScaleY(mScale);

            }
        }
    }

    private class PageListener implements ViewPager.OnPageChangeListener {
        private int oldPosition = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentPosition = position;
            currentPositionOffset = positionOffset;

            if (tabsContainer != null && tabsContainer.getChildAt(position) != null) {
                scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
            }

            invalidate();

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            if (mState == IDLE && positionOffset > 0) {
                oldPage = pager.getCurrentItem();
                mState = position == oldPage ? GOING_RIGHT : GOING_LEFT;
            }
            boolean goingRight = position == oldPage;
            if (mState == GOING_RIGHT && !goingRight)
                mState = GOING_LEFT;
            else if (mState == GOING_LEFT && goingRight)
                mState = GOING_RIGHT;


            float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;

            if (tabsContainer != null) {
                View mLeft = tabsContainer.getChildAt(position);
                View mRight = tabsContainer.getChildAt(position + 1);
                if (effectOffset == 0) {
                    mState = IDLE;
                }
                if (mFadeEnabled)
                    animateFadeScale(mLeft, mRight, effectOffset, position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(pager.getCurrentItem(), 0);
                mFadeEnabled = true;
            }

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            selectedPosition = position;
//			updateTabStyles();
//            currentPosition=position;


            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
            //set old view statue
            tabViews.get(oldPosition).get("normal").setAlpha(1);
            tabViews.get(oldPosition).get("selected").setAlpha(0);
            View v_old = tabsContainer.getChildAt(oldPosition);

            v_old.setPivotX(v_old.getMeasuredWidth() * 0.5f);
            v_old.setPivotY(v_old.getMeasuredHeight() * 0.5f);
            v_old.setScaleX(1f);
            v_old.setScaleY(1f);
            if (selectedTabTextBold) {
                TextView oldTV_1 = (TextView) tabViews.get(oldPosition).get("normal");
                oldTV_1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                TextView oldTV_2 = (TextView) tabViews.get(oldPosition).get("selected");
                oldTV_2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            //set new view statue
            tabViews.get(position).get("normal").setAlpha(0);
            tabViews.get(position).get("selected").setAlpha(1);
            View v_new = tabsContainer.getChildAt(position);
            v_new.setPivotX(v_new.getMeasuredWidth() * 0.5f);
            v_new.setPivotY(v_new.getMeasuredHeight() * 0.5f);
            v_new.setScaleX(1 + zoomMax);
            v_new.setScaleY(1 + zoomMax);
            if (selectedTabTextBold) {
                TextView newTV_1 = (TextView) tabViews.get(position).get("normal");
                newTV_1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                TextView newTV_2 = (TextView) tabViews.get(position).get("selected");
                newTV_2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }

            oldPosition = selectedPosition;
//             oldPosition = currentPosition;
        }
    }

    private boolean isSmall(float positionOffset) {
        return Math.abs(positionOffset) < 0.0001;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.indicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getIndicatorColor() {
        return this.indicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.indicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public void setUnderlineColor(int underlineColor) {
        this.underlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.underlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.dividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.underlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getDividerWidth() {
        return dividerWidth;
    }

    public void setDividerWidth(int dividerWidth) {
        this.dividerWidth = dividerWidth;
    }

    public int getUnderlineHeight() {
        return underlineHeight;
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.dividerPadding = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPadding() {
        return dividerPadding;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
        notifyDataSetChanged();
    }

    public void setShouldExpand_2(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
//        notifyDataSetChanged(titles);
    }

    public boolean getShouldExpand() {
        return shouldExpand;
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.textAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
        this.tabTextSize = textSizePx;
//		updateTabStyles();
    }

    public int getTextSize() {
        return tabTextSize;
    }

    public void setTextColor(int textColor) {
        this.tabTextColor = textColor;
//		updateTabStyles();
    }

    public void setTextColorResource(int resId) {
        this.tabTextColor = getResources().getColor(resId);
//		updateTabStyles();
    }

    public int getTextColor() {
        return tabTextColor;
    }

    public void setSelectedTextColor(int textColor) {
        this.selectedTabTextColor = textColor;
//		updateTabStyles();
    }

    public void setSelectedTextColorResource(int resId) {
        this.selectedTabTextColor = getResources().getColor(resId);
//		updateTabStyles();
    }

    public int getSelectedTextColor() {
        return selectedTabTextColor;
    }

    public void setTypeface(Typeface typeface, int style) {
        this.tabTypeface = typeface;
        this.tabTypefaceStyle = style;
//		updateTabStyles();
    }

    public void setTabBackground(int resId) {
        this.tabBackgroundResId = resId;
//		updateTabStyles();
    }

    public int getTabBackground() {
        return tabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.tabPadding = paddingPx;
//		updateTabStyles();
    }

    public int getTabPaddingLeftRight() {
        return tabPadding;
    }

    public void setIndicatorLineWrap() {

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public LinearLayout getTabsContainer(){
        return tabsContainer;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//		if(null!=pageListener&&this.pager!=null)
//		   this.pager.removeOnPageChangeListener(pageListener);
    }

    public boolean isIndicatorTop() {
        return isIndicatorTop;
    }

    public void setIsIndicatorTop(boolean isIndicatorTop) {
        this.isIndicatorTop = isIndicatorTop;
    }

    //-------------CB--title-click-Single-Double---------
    private OnPagerTitleItemClickListener mOnPagerTitleItemClickListener;

    public interface OnPagerTitleItemClickListener {
        /**
         * 点击,不需做切换处理了,内部已经处理
         *
         * @param position position
         */
        void onSingleClickItem(int position);

        /**
         * 双击
         *
         * @param position position
         */
        void onDoubleClickItem(int position);
    }

    public void setOnPagerTitleItemClickListener(OnPagerTitleItemClickListener mOnPagerTitleItemClickListener) {
        this.mOnPagerTitleItemClickListener = mOnPagerTitleItemClickListener;
    }
    //-------------CB--title-click-Single-Double---------
}
