# ViewPagerIndicator
viewpager,indicator,自定义指示器，支持圆形，指示器顶部展示，展示图片等功能
```
<com.example.indicator.PagerSlidingTabStrip
  android:id="@+id/id_pager_s_t_s"
  android:layout_width="match_parent"
  android:layout_height="30dp"
  android:layout_marginLeft="10dp"
  app:pstsDefalutTextColor="@color/lf_color_80ffffff"
  app:pstsDividerColor="@color/lf_color_transparent"
  app:pstsDividerPadding="0dp"
  app:pstsDividerWidth="0dp"
  app:pstsIndicatorColor="@color/lf_color_ffffff"
  app:pstsIndicatorHeight="3dp"
  app:pstsIndicatorWrap="false"
  app:pstsSelectedTextColor="@color/lf_color_ffffff"
  app:pstsShouldExpand="false"
  app:pstsTextSize="13dp"
  app:pstsUnderlineColor="@color/lf_color_transparent"
  app:pstsUnderlineHeight="1px"
  app:pstsTabPaddingLeftRight="12dp"
  app:pstsIndicatorCircle="true"
  app:pstsIndicatorCircleRadius="1.5dp"
  app:pstsSelectedTextBold="true"/>
  
  pstsDefalutTextColor:默认字体颜色
  pstsDividerColor：tab间隔线
  pstsDividerPadding：上下留白padding距离
  pstsDividerWidth：间隔线高度
  pstsIndicatorColor：下划线或圆点的颜色
  pstsIndicatorHeight：下划线高度
  pstsIndicatorWrap：false左对齐true居中
  pstsSelectedTextColor：选中后颜色
  pstsShouldExpand：true模式为WRAP_CONTENT当tab超过屏幕时可滚动，false模式为MATCH_PARENT
  pstsTextSize：字体大小
  pstsUnderlineColor：下划线颜色
  pstsUnderlineHeight：下划线高度
  pstsTabPaddingLeftRight：tab之间距离（当pstsIndicatorWrap=false时适用）
  pstsIndicatorCircle：圆点
  代码里加Title
```