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

public class PageSlidingTabStripFragment extends Fragment {

	public final String TAG = this.getClass().getSimpleName();

    protected String[] getTitles() {
        return new String[] {"Categories", "Home", "Top Paid", "Top Free"};
    }

    //this doesnt make sense, why not just instnatiate the fragment, why newinstance
//	public static PageSlidingTabStripFragment newInstance() {
//		return new PageSlidingTabStripFragment();
//	}

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
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        final MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager(), getTitles());
//        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//
//        });
        viewPager.setAdapter(adapter);

        // Determines how many pages can be offscreen before the viewpager starts destroying fragments it's hosting.
        // http://stackoverflow.com/questions/11852604/why-is-my-fragment-oncreate-being-called-extensively-whenever-i-page-through-my
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);

        tabStrip.setShouldExpand(true);
        tabStrip.setTabPaddingLeftRight(10); //10 is the magic number?
//        int padding = tabStrip.getDividerPadding();
//        tabStrip.setDividerPadding(padding - 1);
//        tabStrip.setBackgroundResource(R.drawable.tab);
//        tabStrip.setIndicatorColorResource(R.drawable.tab_selected_onewarmcoat);
        tabStrip.setIndicatorColor(Color.argb(0xFF, 0x24, 0x6D, 0x9E));
        tabStrip.setBackgroundColor(Color.argb(0xFF, 0xdd, 0xe8, 0xed));
        tabStrip.setBackgroundResource(R.drawable.ab_background_textured_onewarmcoat);
        tabStrip.setViewPager(viewPager);
        tabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int currentPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int newPosition) {
                try {
                    ViewPagerChangeListener fragmentToHide = (ViewPagerChangeListener) adapter.getItem(currentPosition);
                    fragmentToHide.onViewPagerHide();
                } catch (ClassCastException e) {
                    Log.i(getClass().getSimpleName(), "Fragment at position " + String.valueOf(currentPosition) + " doesn't implement ViewPagerChangeListener.");
                }

                try {
                    ViewPagerChangeListener fragmentToShow = (ViewPagerChangeListener) adapter.getItem(newPosition);
                    fragmentToShow.onViewPagerShow();
                } catch (ClassCastException e) {
                    Log.i(getClass().getSimpleName(), "Fragment at position " + String.valueOf(newPosition) + " doesn't implement ViewPagerChangeListener.");
                }

                currentPosition = newPosition;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

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
