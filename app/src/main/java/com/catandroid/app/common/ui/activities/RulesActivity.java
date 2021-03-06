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
package com.catandroid.app.common.ui.activities;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;

import com.catandroid.app.R;

public class RulesActivity extends Activity {
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.game_rules);
		setTitle(R.string.rules_button);

		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);

		String data = null;

		//TODO: add game_rules
		try {
			InputStream is = getResources().openRawResource(R.raw.rules);

			byte[] buffer = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			while (true) {
				int read = is.read(buffer);
				if (read == -1)
				{
					break;
				}

				baos.write(buffer, 0, read);
			}

			baos.close();
			is.close();

			data = baos.toString();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "failed to load resource to string", e);
		}

		final WebView rules = (WebView) findViewById(R.id.rules);
		rules.loadData(data != null ? data : getString(R.string.rules_failed), "text/html", "utf-8");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			}

		return super.onOptionsItemSelected(item);
	}
}
