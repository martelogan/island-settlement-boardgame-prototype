package com.catandroid.app.common.ui.fragments.interaction_fragments.trade;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.R;
import com.catandroid.app.common.components.TradeProposal;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;

public class TradeProposedFragment extends Fragment {

	private Resource.ResourceType resourceType;
	private int[] originalOffer;
	private Player currentPlayer, playerOffering;
	private int index;

	private Board board;

	private ActiveGameFragment activeGameFragment;

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5, R.id.trade_res6,
			R.id.trade_res7, R.id.trade_res8, R.id.trade_res9};

	private static final int[] OFFER = { R.id.trade_offer1, R.id.trade_offer2,
			R.id.trade_offer3, R.id.trade_offer4, R.id.trade_offer5, R.id.trade_offer6,
			R.id.trade_offer7, R.id.trade_offer8, R.id.trade_offer9};

	public void setBoard(Board board) {
		this.board = board;
	}

	public void setActiveGameFragment(ActiveGameFragment activeGameFragment) {
		this.activeGameFragment = activeGameFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		final TradeProposal tradeProposal = board.getTradeProposal();
		tradeProposal.setBoard(board);
		resourceType = tradeProposal.getTradeResource();
		originalOffer = tradeProposal.getOriginalOffer();
		playerOffering = board.getPlayerById(tradeProposal.getTradeCreatorPlayerId());
		currentPlayer = board.getPlayerFromParticipantId(activeGameFragment.myParticipantId);

		Log.d(getClass().getName(), playerOffering.getName() + " (index " + index
				+ ") considering originalOffer");

		final View counterOfferView = inflater.inflate(R.layout.trade_proposed, null, false);

		TextView wants = (TextView) counterOfferView.findViewById(R.id.trade_player_wants);
		wants.setText(String.format(getString(R.string.trade_player_wants),
				getString(Resource.toRString(resourceType))));

		final TextView playerOffer = (TextView) counterOfferView.findViewById(R.id.trade_player_offer);
		playerOffer.setText(String.format(
				getString(R.string.trade_player_offer), playerOffering.getName()));

		for (int i = 0; i < RESOURCES.length; i++) {
			TextView offer = (TextView) counterOfferView.findViewById(OFFER[i]);
			offer.setText(Integer.toString(this.originalOffer[i]));

			int count = currentPlayer.getResources(Resource.RESOURCE_TYPES[i]);

			TextView res = (TextView) counterOfferView.findViewById(RESOURCES[i]);
			res.setText(Integer.toString(count));
		}

		Button accept = (Button) counterOfferView.findViewById(R.id.trade_accept);
		accept.setEnabled(currentPlayer.getResources(resourceType) > 0);
		accept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playerOffering.trade(currentPlayer, resourceType, originalOffer);
				tradeProposal.setTradeReplied(true);
				board.nextPhase();
				getFragmentManager().popBackStack();
			}
		});

		Button decline = (Button) counterOfferView.findViewById(R.id.trade_decline);
		decline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				board.nextPhase();
				getFragmentManager().popBackStack();

			}
		});

		Button counterOffer = (Button) counterOfferView.findViewById(R.id.trade_counter);
		counterOffer.setEnabled(currentPlayer.getResources(resourceType) > 0);
		counterOffer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//do the exchange of resources
				TradeUnknownFragment tradeUnknownFragment = new TradeUnknownFragment();
				tradeUnknownFragment.setBoard(board);
				tradeUnknownFragment.setActiveGameFragment(activeGameFragment);

				FragmentTransaction tradeFragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
				tradeFragmentTransaction.replace(R.id.fragment_container, tradeUnknownFragment,"PROPOSE_COUNTERTRADE");
				tradeFragmentTransaction.addToBackStack("PROPOSE_COUNTERTRADE");
				tradeFragmentTransaction.commit();
			}
		});

		return counterOfferView;
	}
}
