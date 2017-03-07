package com.catandroid.app.common.ui.fragments.interaction_fragments.trade;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.R;
import com.catandroid.app.CatAndroidApp;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.AutomatedPlayer;
import com.catandroid.app.common.players.Player;

public class TradeResponseFragment extends Activity {

	public static final int REQUEST_TRADE_ACCEPTED = 0;

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5 };

	private static final int[] BUTTONS = { R.id.trade_player1,
			R.id.trade_player2, R.id.trade_player3 };

	private static final int[] OFFER_BUTTONS = { R.id.trade_player1_offer,
			R.id.trade_player2_offer, R.id.trade_player3_offer };

	private Board board;
	private Player current;
	private int[] trade;
	private Resource.ResourceType resourceType;
	private boolean[] accepted;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}

		resourceType = Resource.RESOURCE_TYPES[extras.getInt(TradeRequestFragment.TYPE_KEY)];
		trade = extras.getIntArray(TradeRequestFragment.OFFER_KEY);

		setContentView(R.layout.trade_response);
		setTitle(R.string.trade);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.dimAmount = 1.0f;
		getWindow().setAttributes(lp);

		for (int i = 0; i < RESOURCES.length; i++) {
			TextView text = (TextView) findViewById(RESOURCES[i]);
			text.setText(Integer.toString(trade[i]));
		}

		board = ((CatAndroidApp) getApplicationContext()).getBoardInstance();
		if (board == null) {
			finish();
			return;
		}

		current = board.getCurrentPlayer();

		accepted = new boolean[3];
		for (int i = 0; i < 3; i++)
			accepted[i] = false;

		TextView wants = (TextView) findViewById(R.id.trade_player_wants);
		wants.setText(String.format(getString(R.string.trade_player_wants),
				getString(Resource.toRString(resourceType))));

		TextView playerOffer = (TextView) findViewById(R.id.trade_player_offer);
		playerOffer.setText(String.format(
				getString(R.string.trade_player_offer), current.getName()));

		int index = 0;
		for (int i = 0; i < 4; i++) {
			Player player = board.getPlayer(i);
			if (player == current)
				continue;

			boolean accepted = false;
			int[] counter = null;

			if (player.isBot()) {
				// offer to AI player automatically
				AutomatedPlayer bot = (AutomatedPlayer) player;
				int[] offer = bot.offerTrade(current, resourceType, trade);
				if (offer == trade)
					accepted = true;
				else if (offer != null)
					counter = offer;
			} else if (player.isHuman()) {
				// addCubic button on human players
				Button make = (Button) findViewById(OFFER_BUTTONS[index]);
				make.setVisibility(View.VISIBLE);

				make.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Player player = null;
						int index = 0;
						for (int i = 0; i < 4; i++) {
							player = board.getPlayer(i);
							if (player == current)
								continue;

							if (findViewById(OFFER_BUTTONS[index]) == v)
								break;

							index++;
						}

						Intent intent = new Intent(TradeResponseFragment.this,
								CounterOfferFragment.class);
						intent.setClassName("com.catandroid.app",
								"com.catandroid.app.activities.trade.CounterOfferFragment");
						intent.putExtra(TradeRequestFragment.TYPE_KEY, resourceType.ordinal());
						intent.putExtra(TradeRequestFragment.OFFER_KEY, trade);
						intent.putExtra(TradeRequestFragment.PLAYER_KEY, player
								.getPlayerNumber());
						intent.putExtra(TradeRequestFragment.INDEX_KEY, index);
						TradeResponseFragment.this.startActivityForResult(intent,
								REQUEST_TRADE_ACCEPTED);
					}
				});
			}

			String text;
			if (accepted)
				text = getString(R.string.trade_accepted);
			else if (counter != null)
				text = getString(R.string.trade_counter);
			else
				text = getString(R.string.trade_rejected);

			Button button = (Button) findViewById(BUTTONS[index]);
			button.setText(String.format(text, player.getName()));
			button.setEnabled((accepted || counter != null)
					&& board.getCurrentPlayer().isHuman());

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// determine the player
					Player player = null;
					int index = 0;
					for (int i = 0; i < 4; i++) {
						player = board.getPlayer(i);
						if (player == current)
							continue;

						if (findViewById(BUTTONS[index]) == v)
							break;

						index++;
					}

					// swap resources
					current.trade(player, resourceType, trade);

					setResult(Activity.RESULT_OK);
					finish();
				}
			});

			index += 1;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == REQUEST_TRADE_ACCEPTED
				&& resultCode == Activity.RESULT_OK && intent != null) {
			int acceptedIndex = intent.getIntExtra(TradeRequestFragment.INDEX_KEY, -1);

			Log.d(getClass().getName(), "offer accepted by player with index "
					+ acceptedIndex);

			if (acceptedIndex < 0 || !board.getCurrentPlayer().isHuman())
				return;

			accepted[acceptedIndex] = true;

			int index = 0;
			for (int i = 0; i < 4; i++) {
				Player player = board.getPlayer(i);
				if (player == current)
					continue;

				if (player.isHuman()) {
					String text;
					if (accepted[index])
						text = getString(R.string.trade_accepted);
					else
						text = getString(R.string.trade_rejected);

					Button button = (Button) findViewById(BUTTONS[index]);
					button.setEnabled(accepted[index]);
					button.setText(String.format(text, player.getName()));
				}

				index++;
			}
		}
	}
}
