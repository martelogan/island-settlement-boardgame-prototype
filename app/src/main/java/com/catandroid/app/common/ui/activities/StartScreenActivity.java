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

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.catandroid.app.CatAndroidApp;
import com.catandroid.app.R;
import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.logistics.AppSettings;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;

public class StartScreenActivity extends Activity {

	private Vector<Runnable> actions;

	@Override
	public void onResume() {
		super.onResume();

		Board board = ((CatAndroidApp) getApplicationContext()).getBoardInstance();
		AppSettings appSettings = ((CatAndroidApp) getApplicationContext()).getAppSettingsInstance();

		Vector<String> labels = new Vector<String>();
		actions = new Vector<Runnable>();

		if (board != null && board.getWinner() == null) {
			//@TODO
			//ADD RESUME FUNCTIONALITY. HAPPENS WHEN YOU DO BACK ON SCREEN
			labels.add(getString(R.string.resume_button));
			actions.add(new Runnable() {
				@Override
				public void run() {
					StartScreenActivity.this.startActivity(new Intent(StartScreenActivity.this, ActiveGameFragment.class));
				}
			});
		}

		labels.add(getString(R.string.login_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				StartScreenActivity.this.startActivity(new Intent(StartScreenActivity.this, GameManagerActivity.class));
			}
		});


		labels.add(getString(R.string.rules_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				StartScreenActivity.this.startActivity(new Intent(StartScreenActivity.this, RulesActivity.class));
			}
		});

		labels.add(getString(R.string.about_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				final Builder aboutDialog = new AlertDialog.Builder(StartScreenActivity.this);
				aboutDialog.setTitle(R.string.app_name);
				aboutDialog.setIcon(R.drawable.logo);
				aboutDialog.setMessage(getString(R.string.about_text) + "\n\n"
						+ getString(R.string.acknowledgements));
				aboutDialog.show();
			}
		});

		String[] values = new String[labels.size()];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = labels.get(i);
		}

		int padding = (int) (10 * getResources().getDisplayMetrics().density);

		ListView view = new ListView(this);
		view.setPadding(padding, padding, padding, padding);

		view.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values));

		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				actions.get(position).run();
			}
		});

		setContentView(view);
	}
}
