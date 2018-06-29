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
package com.catandroid.app.common.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.R;
import com.catandroid.app.common.players.Player;

public class ResourceView extends LinearLayout {

	private static final int[] RESOURCES = { R.drawable.resource_lumber_medium, R.drawable.resource_wool_medium,
			R.drawable.resource_grain_medium, R.drawable.resource_brick_medium, R.drawable.resource_ore_medium, };

	private TextView[] views;

	private Context context;

	public ResourceView(Context context) {
		super(context);
		this.context = context;

		int padding = (int) (10 * context.getResources().getDisplayMetrics().density);

		setVisibility(View.INVISIBLE);
		setGravity(Gravity.CENTER);

		views = new TextView[RESOURCES.length];

		for (int i = 0; i < RESOURCES.length; i++) {
			final int resource = i;

			ImageView image = new ImageView(context);
			image.setImageResource(RESOURCES[i]);

			TextView text = new TextView(context);
			text.setText("");
			text.setTextColor(Color.WHITE);
			text.setTextSize(24);
			text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			text.setGravity(Gravity.BOTTOM);
			views[i] = text;

			LinearLayout row = new LinearLayout(context);
			row.setOrientation(LinearLayout.HORIZONTAL);
			row.setPadding(padding, padding, padding, padding);
			row.addView(image);
			row.addView(text);


			addView(row);
		}
	}

	public void setValues(Player player) {
		if (player == null || !player.isHuman()) {
			setVisibility(View.INVISIBLE);
			return;
		}

		setVisibility(View.VISIBLE);
		setBackgroundColor(Color.argb(200, 0, 0, 0));

		int[] resources = player.getCountPerResource();
		for (int i = 0; i < Resource.RESOURCE_TYPES.length; i++)
		{
			views[i].setText(String.valueOf(resources[i]));
		}
	}
}
