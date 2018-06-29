/*
 * island-settlement-boardgame-prototype
 * Copyright (C) 2017, Logan Martel, Frederick Parsons
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.catandroid.app.common.ui.fragments.static_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.catandroid.app.R;

public class CostsReferenceFragment extends Fragment {

	private static final int[] LAYOUTS = { R.layout.development_costs};

	private static final int[] NAMES = { R.string.costs_reference_title};

	private View[] views;

	public CostsReferenceFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreate(state);

		//getActivity().setContentView(R.layout.costs_reference);
		getActivity().setTitle(getString(R.string.costs_reference));
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

		//LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.costs_reference, null, false);

		views = new View[LAYOUTS.length];
		
		for (int i = 0; i < LAYOUTS.length; i++)
		{
			views[i] = inflater.inflate(LAYOUTS[i], null);
		}
		
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.costs_reference);
		viewPager.setAdapter(new ReferenceTabAdapter());
		viewPager.setCurrentItem(1);

		Log.d("costsView", "about to return costs view");
		return view;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				//finish();
				// must ask the activity to close this StartScreenActivity Fragment
				getActivity().getSupportFragmentManager().popBackStack();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public class ReferenceTabAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return views.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {
			collection.addView(views[position]);
			return views[position];
		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			collection.removeView((View) view);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getString(NAMES[position]);
		}
	}
}
