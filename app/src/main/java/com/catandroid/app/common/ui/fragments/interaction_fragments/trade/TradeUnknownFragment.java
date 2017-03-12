package com.catandroid.app.common.ui.fragments.interaction_fragments.trade;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.R;
import com.catandroid.app.common.components.TradeProposal;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;

import java.util.ArrayList;
import java.util.Collections;

public class TradeUnknownFragment extends Fragment {

	private Resource.ResourceType resourceType;
	private int[] originalOffer;
	private Player currentPlayer, playerOffering;
	private int index;

	private Board board;

	public static final int REQUEST_TRADE_COMPLETED = 0;

	public static final String TYPE_KEY = "com.catandroid.app.TradeType";

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5, R.id.trade_res6,
			R.id.trade_res7, R.id.trade_res8, R.id.trade_res9};

	private static final int[] OFFERS = { R.id.trade_offer1, R.id.trade_offer2,
			R.id.trade_offer3, R.id.trade_offer4, R.id.trade_offer5, R.id.trade_offer6,
			R.id.trade_offer7, R.id.trade_offer8, R.id.trade_offer9};

	private static final int[] PLUS = { R.id.trade_plus1, R.id.trade_plus2,
			R.id.trade_plus3, R.id.trade_plus4, R.id.trade_plus5, R.id.trade_plus6,
			R.id.trade_plus7, R.id.trade_plus8, R.id.trade_plus9};

	private static final int[] MINUS = { R.id.trade_minus1, R.id.trade_minus2,
			R.id.trade_minus3, R.id.trade_minus4, R.id.trade_minus5, R.id.trade_minus6,
			R.id.trade_minus7, R.id.trade_minus8, R.id.trade_minus9};

	private static final int[] RES_VIEW = { R.id.resource1, R.id.resource2,
			R.id.resource3, R.id.resource4, R.id.resource5, R.id.resource6,
			R.id.resource7, R.id.resource8, R.id.resource9};

	private static final int[] RES_STRING = { R.string.lumber, R.string.wool,
			R.string.grain, R.string.brick, R.string.ore, R.string.gold,
			R.string.paper, R.string.coin, R.string.cloth};

	private int selected;

	public void setActiveGameFragment(ActiveGameFragment activeGameFragment) {
		this.activeGameFragment = activeGameFragment;
	}

	private ActiveGameFragment activeGameFragment;


	public void setBoard(Board board) {
		this.board = board;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final TradeProposal tradeProposal = board.getTradeProposal();
		tradeProposal.setBoard(board);
		resourceType = tradeProposal.getTradeResource();
		originalOffer = tradeProposal.getOriginalOffer();
		playerOffering = board.getPlayerById(tradeProposal.getTradeCreatorPlayerId());
		currentPlayer = board.getPlayerFromParticipantId(activeGameFragment.myParticipantId);


		selected = 0;

		getActivity().setTitle(R.string.trade);

		if (board == null) {
			//finish();
			return null;
		}

		final View counterOfferView = inflater.inflate(R.layout.trade_counter_proposal, null, false);

		TextView wants = (TextView) counterOfferView.findViewById(R.id.trade_player_wants);
		wants.setText(String.format(getString(R.string.trade_player_wants),
				getString(Resource.toRString(resourceType))));

		for (int i = 0; i < RESOURCES.length; i++) {

			int count = currentPlayer.getResources(Resource.RESOURCE_TYPES[i]);

			TextView text = (TextView) counterOfferView.findViewById(RESOURCES[i]);
			text.setText(Integer.toString(count));

			Button plus = (Button) counterOfferView.findViewById(PLUS[i]);
			Button minus = (Button) counterOfferView.findViewById(MINUS[i]);

			plus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (int i = 0; i < PLUS.length; i++) {
						if (v == counterOfferView.findViewById(PLUS[i])) {
							TextView offer = (TextView) counterOfferView.findViewById(OFFERS[i]);
							String str = offer.getText().toString();
							int value = Integer.parseInt(str, 10) + 1;
							offer.setText(Integer.toString(value));

							Button minus = (Button) counterOfferView.findViewById(MINUS[i]);
							minus.setEnabled(true);

							break;
						}
					}

					Button propose = (Button) counterOfferView.findViewById(R.id.trade_propose);
					propose.setEnabled(true);

				}
			});

			minus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean valid = false;
					for (int i = 0; i < MINUS.length; i++) {
						TextView offer = (TextView) counterOfferView.findViewById(OFFERS[i]);
						String str = offer.getText().toString();
						int value = Integer.parseInt(str, 10);

						if (v == counterOfferView.findViewById(MINUS[i])) {
							value -= 1;
							offer.setText(Integer.toString(value));

							Button plus = (Button) counterOfferView.findViewById(PLUS[i]);
							plus.setEnabled(true);

							if (value == 0)
								v.setEnabled(false);
						}

						if (value > 0)
							valid = true;
					}

					Button propose = (Button) counterOfferView.findViewById(R.id.trade_propose);
					if (!valid)
						propose.setEnabled(false);

				}
			});
		}

		Button propose = (Button) counterOfferView.findViewById(R.id.trade_propose);
		propose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int[] trade = new int[Resource.RESOURCE_TYPES.length];
				for (int i = 0; i < trade.length; i++) {
					TextView offer = (TextView) counterOfferView.findViewById(OFFERS[i]);
					trade[i] = Integer.parseInt((String) offer.getText(), 10);
				}

				tradeProposal.setCounterOffer(trade);
				tradeProposal.setTradeReplied(true);
				board.nextPhase();

				getFragmentManager().popBackStack();
				getFragmentManager().popBackStack();

			}
		});

		return counterOfferView;
	}

	private void toast(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,
								 Intent intent) {
		if (requestCode == REQUEST_TRADE_COMPLETED
				&& resultCode == Activity.RESULT_OK)
			return;
	}
}
