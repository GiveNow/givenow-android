package org.onewarmcoat.onewarmcoat.app.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.adapters.SmartFragmentStatePagerAdapter;
import org.onewarmcoat.onewarmcoat.app.interfaces.ViewPagerChangeListener;

import java.lang.reflect.Field;

public class PageSlidingTabStripFragment extends Fragment implements ViewPager.OnPageChangeListener {

	public final String TAG = this.getClass().getSimpleName();
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapter;
    private int currentPosition = 0;

    protected String[] getTitles() {
        return new String[] {"Categories", "Home", "Top Paid", "Top Free"};
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.pager, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) view
                .findViewById(R.id.tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mAdapter = new MyPagerAdapter(getChildFragmentManager(), getTitles());
        mViewPager.setAdapter(mAdapter);

        // Determines how many pages can be offscreen before the viewpager starts destroying fragments it's hosting.
        // http://stackoverflow.com/questions/11852604/why-is-my-fragment-oncreate-being-called-extensively-whenever-i-page-through-my
        mViewPager.setOffscreenPageLimit(mAdapter.getCount() - 1);

        tabStrip.setShouldExpand(true);
        tabStrip.setTabPaddingLeftRight(10); //10 is the magic number?
//        int padding = tabStrip.getDividerPadding();
//        tabStrip.setDividerPadding(padding - 1);
//        tabStrip.setBackgroundResource(R.drawable.tab);
//        tabStrip.setIndicatorColorResource(R.drawable.tab_selected_onewarmcoat);
        tabStrip.setIndicatorColor(getResources().getColor(R.color.accent));
        tabStrip.setTextColor(getResources().getColorStateList(R.color.tab_text));
        tabStrip.setBackgroundColor(Color.argb(0xFF, 0xdd, 0xe8, 0xed));
        tabStrip.setBackgroundResource(R.drawable.ab_background_textured_onewarmcoat);
        tabStrip.setOnPageChangeListener(this);

        tabStrip.setViewPager(mViewPager);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // When we get hidden or shown by the Main Activity navigation, pause and resume accordingly.
        super.onHiddenChanged(hidden);

        if (hidden) {
            onPause();
        } else {
            onResume();
        }
    }

    // PageChangeListeners:
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int newPosition) {
        callOnViewPagerHide(currentPosition);
        pauseFragment(currentPosition);

        callOnViewPagerShow(newPosition);
        resumeFragment(newPosition);

        currentPosition = newPosition;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }


    @Override
    public void onPause() {
        Log.e(getClass().getSimpleName(), "OnPause.");
        super.onPause();

        callOnViewPagerHide(mViewPager.getCurrentItem());
        pauseFragment(mViewPager.getCurrentItem());
    }

    @Override
    public void onResume() {
        Log.e(getClass().getSimpleName(), "OnResume.");
        super.onResume();

        callOnViewPagerShow(mViewPager.getCurrentItem());
        resumeFragment(mViewPager.getCurrentItem());
    }

    private void pauseFragment(int pos) {
        Fragment currentPositionFragment = mAdapter.getItem(pos);
        if (currentPositionFragment != null) {
            currentPositionFragment.onPause();
        }
    }

    private void resumeFragment(int pos) {
        Fragment newPositionFragment = mAdapter.getItem(pos);
        if (newPositionFragment != null) {
            newPositionFragment.onResume();
        }
    }

    private void callOnViewPagerHide(int i) {
        Log.e(getClass().getSimpleName(), "CallOnViewPagerHide(" + String.valueOf(i) + ")");
        try {
            ViewPagerChangeListener fragmentToShow = (ViewPagerChangeListener) mAdapter.getItem(i);
            fragmentToShow.onViewPagerHide();
        } catch (ClassCastException e) {
            Log.i(getClass().getSimpleName(), "Fragment at position " + String.valueOf(i) + " doesn't implement ViewPagerChangeListener.");
        }
    }

    private void callOnViewPagerShow(int i) {
        Log.e(getClass().getSimpleName(), "CallOnViewPagerShow(" + String.valueOf(i) + ")");
        try {
            ViewPagerChangeListener fragmentToShow = (ViewPagerChangeListener) mAdapter.getItem(i);
            fragmentToShow.onViewPagerShow();
        } catch (ClassCastException e) {
            Log.i(getClass().getSimpleName(), "Fragment at position " + String.valueOf(i) + " doesn't implement ViewPagerChangeListener.");
        }
    }

    protected Fragment getFragmentForPosition(int position) {
        return SuperAwesomeCardFragment.newInstance(position);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // http://stackoverflow.com/a/15656428/456568
        // Workaround for a bug in the support library related to support for nested fragments.
        // "Basically, the child FragmentManager ends up with a broken internal state when it is
        // detached from the activity. A short-term workaround that fixed it for me is to add the
        // following to onDetach() of every Fragment which you call getChildFragmentManager() on:"
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public class MyPagerAdapter extends SmartFragmentStatePagerAdapter {

        private String[] mtitles;

        public MyPagerAdapter(android.app.FragmentManager fm, String[] titles) {
            super(fm);
            mtitles = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mtitles[position];
        }

        @Override
        public int getCount() {
            return mtitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            return getFragmentForPosition(position);
        }

    }

}
