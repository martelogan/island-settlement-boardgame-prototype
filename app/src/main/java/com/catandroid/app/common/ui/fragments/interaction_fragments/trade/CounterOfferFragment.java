package com.catandroid.app.common.ui.fragments.interaction_fragments.trade;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.R;
import com.catandroid.app.CatAndroidApp;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.Player;

public class CounterOfferFragment extends Activity {

	private Resource.ResourceType resourceType;
	private int[] trade;
	private Player current, player;
	private int index;

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5 };

	private static final int[] OFFER = { R.id.trade_offer1, R.id.trade_offer2,
			R.id.trade_offer3, R.id.trade_offer4, R.id.trade_offer5 };

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Board board = ((CatAndroidApp) getApplicationContext()).getBoardInstance();

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}

		resourceType = Resource.RESOURCE_TYPES[extras.getInt(TradeRequestFragment.TYPE_KEY)];
		trade = extras.getIntArray(TradeRequestFragment.OFFER_KEY);
		player = board.getPlayer(extras.getInt(TradeRequestFragment.PLAYER_KEY));
		current = board.getCurrentPlayer();
		index = extras.getInt(TradeRequestFragment.INDEX_KEY);

		Log.d(getClass().getName(), player.getName() + " (index " + index
				+ ") considering trade");

//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.counter_offer);

		TextView wants = (TextView) findViewById(R.id.trade_player_wants);
		wants.setText(String.format(getString(R.string.trade_player_wants),
				getString(Resource.toRString(resourceType))));

		TextView playerOffer = (TextView) findViewById(R.id.trade_player_offer);
		playerOffer.setText(String.format(
				getString(R.string.trade_player_offer), current.getName()));

		for (int i = 0; i < RESOURCES.length; i++) {
			int count = player.getResources(Resource.RESOURCE_TYPES[i]);

			TextView res = (TextView) findViewById(RESOURCES[i]);
			res.setText(Integer.toString(count));

			TextView offer = (TextView) findViewById(OFFER[i]);
			offer.setText(Integer.toString(trade[i]));
		}

		Button accept = (Button) findViewById(R.id.trade_accept);
		accept.setEnabled(player.getResources(resourceType) > 0);
		accept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(TradeRequestFragment.INDEX_KEY, index);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}
}
