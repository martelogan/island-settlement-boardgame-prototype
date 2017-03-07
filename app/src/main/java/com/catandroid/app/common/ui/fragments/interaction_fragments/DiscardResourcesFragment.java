package com.catandroid.app.common.ui.fragments.interaction_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.catandroid.app.R;
import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.Player;

public class DiscardResourcesFragment extends Fragment {

	public static final String QUANTITY_KEY = "com.catandroid.app.DiscardQuantity";
	public static final String PLAYER_KEY = "com.catandroid.app.DiscardPlayer";

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5 };

	private static final int[] SELECTIONS = { R.id.trade_offer1,
			R.id.trade_offer2, R.id.trade_offer3, R.id.trade_offer4,
			R.id.trade_offer5 };

	private static final int[] PLUS = { R.id.trade_plus1, R.id.trade_plus2,
			R.id.trade_plus3, R.id.trade_plus4, R.id.trade_plus5 };

	private static final int[] MINUS = { R.id.trade_minus1, R.id.trade_minus2,
			R.id.trade_minus3, R.id.trade_minus4, R.id.trade_minus5 };

	private Player player;
	private int quantity;

	private Board board;

	public void setBoard(Board board) {
		this.board = board;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreate(bundle);
		
		//getActivity().ActivsetFinishOnTouchOutside(false);

		//setContentView(R.layout.discard_resources);
		final View discardView = inflater.inflate(R.layout.discard_resources, null, false);

		quantity = 0;
		player = null;


		if (board == null) {
			//finish();
			return null;
		}

		Bundle extras = this.getArguments();
		if (extras != null) {
			quantity = extras.getInt(QUANTITY_KEY);
			player = board.getPlayer(extras.getInt(PLAYER_KEY));
		}
		
		if (extras == null || quantity == 0) {
			//finish();
			return null;
		}

		getActivity().setTitle(String.format(getString(R.string.discard_reason), board.getCurrentPlayer().getName()));

		String instructionText = getString(R.string.discard_instruction);
		TextView instruction = (TextView) discardView.findViewById(R.id.discard_instruction);
		instruction.setText(player.getName() + ": "
				+ String.format(instructionText, quantity));

		for (int i = 0; i < RESOURCES.length; i++) {
			int count = player.getResources(Resource.RESOURCE_TYPES[i]);

			TextView text = (TextView) discardView.findViewById(RESOURCES[i]);
			text.setText(Integer.toString(count));

			Button plus = (Button) discardView.findViewById(PLUS[i]);
			Button minus = (Button) discardView.findViewById(MINUS[i]);

			plus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int total = 0;

					for (int i = 0; i < PLUS.length; i++) {
						TextView offer = (TextView) discardView.findViewById(SELECTIONS[i]);
						String str = offer.getText().toString();
						int value = Integer.parseInt(str, 10);

						if (v == discardView.findViewById(PLUS[i])) {
							value += 1;
							offer.setText(Integer.toString(value));

							discardView.findViewById(MINUS[i]).setEnabled(true);

							if (value >= player.getResources(Resource.ResourceType
									.values()[i]))
								v.setEnabled(false);
						}

						total += value;
					}

					if (total == quantity) {
						for (int i = 0; i < PLUS.length; i++)
							discardView.findViewById(PLUS[i]).setEnabled(false);

						discardView.findViewById(R.id.discard_button).setEnabled(true);
					}
				}
			});

			minus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (int i = 0; i < MINUS.length; i++) {
						TextView offer = (TextView) discardView.findViewById(SELECTIONS[i]);
						String str = offer.getText().toString();
						int value = Integer.parseInt(str, 10);

						if (v == discardView.findViewById(MINUS[i])) {
							value -= 1;
							offer.setText(Integer.toString(value));

							if (value == 0)
								v.setEnabled(false);
						}

						int count = player.getResources(Resource.RESOURCE_TYPES[i]);
						if (value < count)
							discardView.findViewById(PLUS[i]).setEnabled(true);
					}

					discardView.findViewById(R.id.discard_button).setEnabled(false);
				}
			});

			plus.setEnabled(count > 0);
		}

		Button discard = (Button) discardView.findViewById(R.id.discard_button);
		discard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < RESOURCES.length; i++) {
					TextView number = (TextView) discardView.findViewById(SELECTIONS[i]);
					int count = Integer.parseInt((String) number.getText(), 10);

					for (int j = 0; j < count; j++)
						player.discard(Resource.RESOURCE_TYPES[i]);
				}

				//finish();
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});

	return discardView;
	}
}
