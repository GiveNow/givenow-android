package org.onewarmcoat.onewarmcoat.app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import org.onewarmcoat.onewarmcoat.app.R;

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
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.pager, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) view
				.findViewById(R.id.tabs);
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager(), getTitles());
		pager.setAdapter(adapter);

        // Determines how many pages can be offscreen before the viewpager starts destroying fragments it's hosting.
        // http://stackoverflow.com/questions/11852604/why-is-my-fragment-oncreate-being-called-extensively-whenever-i-page-through-my
        pager.setOffscreenPageLimit(adapter.getCount() - 1);

		tabs.setViewPager(pager);

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

    public class MyPagerAdapter extends FragmentPagerAdapter {

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
            //TODO: is this fragment being created anew each time?
            //TODO: here is where i create the PickupFragment, DropOffFragment and CashFragment depending on position.
            return getFragmentForPosition(position);
        }

    }
}
