package com.catandroid.app.common.ui.fragments.static_fragments;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.CityImprovement;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.ui.graphics_controllers.TextureManager;
import com.catandroid.app.R;
import com.catandroid.app.common.players.Player;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayerStatsFragment extends Fragment {
	
	private View[] views;
	String myParticipantId;

	public void setBoard(Board board) {
		this.board = board;
	}

	Board board;

	public void setMyPlayerId(String myParticipantId){
		this.myParticipantId = myParticipantId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreate(state);
		
		getActivity().setTitle(getString(R.string.player_status));

		if (board == null) {
			//finish();
			return null;
		}

		int numPlayers = board.getNumPlayers();

		views = new View[numPlayers];
		
		for (int i = 0; i < numPlayers; i++) {
			views[i] = inflater.inflate(R.layout.player_stats, null);
			
			Player player = board.getPlayerById(i);

			boolean showAll = ((myParticipantId).equals(player.getGooglePlayParticipantId())
					&& player.isHuman()
					|| board.getWinner() != null);
			int points;
			if (showAll)
				points = player.getVictoryPoints();
			else
				points = player.getPublicVictoryPoints();

			String message = "\n";

			if(board.playerNumBootOwner == player.getPlayerNumber()){
				message += "Has the boot! (+1 VP required to win)\n\n";
			}

			message += getString(R.string.player_status_resources) + ": "
					+ player.getTotalNumOwnedResources() + "\n";
			if(showAll) {
				message += "\t\t" + getString(R.string.wool) + ": "
						+ player.getResources(Resource.ResourceType.WOOL) + "\n";
				message += "\t\t" + getString(R.string.lumber) + ": "
						+ player.getResources(Resource.ResourceType.LUMBER) + "\n";
				message += "\t\t" + getString(R.string.brick) + ": "
						+ player.getResources(Resource.ResourceType.BRICK) + "\n";
				message += "\t\t" + getString(R.string.ore) + ": "
						+ player.getResources(Resource.ResourceType.ORE) + "\n";
				message += "\t\t" + getString(R.string.gold) + ": "
						+ player.getResources(Resource.ResourceType.GOLD) + "\n";
				message += "\t\t" + getString(R.string.grain) + ": "
						+ player.getResources(Resource.ResourceType.GRAIN) + "\n";
				message += "\t\t" + getString(R.string.paper) + ": "
						+ player.getResources(Resource.ResourceType.PAPER) + "\n";
				message += "\t\t" + getString(R.string.coin) + ": "
						+ player.getResources(Resource.ResourceType.COIN) + "\n";
				message += "\t\t" + getString(R.string.cloth) + ": "
						+ player.getResources(Resource.ResourceType.CLOTH) + "\n\n";
				message += getString(R.string.fish) + ": "
						+ player.getNumFishOwned() + "\n";
			}

			message += getString(R.string.player_status_progress_cards) + ": "
					+ player.getNumProgressCards();

			message += "\n";

			message += getString(R.string.player_status_total_settlements) + ": "
					+ player.getNumOwnedSettlements() + " / " + Player.MAX_SETTLEMENTS + "\n";
			message += getString(R.string.player_status_total_cities) + ": "
					+ player.getNumOwnedCities() + " / " + Player.MAX_CITIES + "\n";
			message += getString(R.string.player_status_total_roads) + ": "
					+ player.getNumRoads() + " / " + Player.MAX_ROADS + "\n";
			message += getString(R.string.player_status_total_ships) + ": "
					+ player.getNumShips() + " / " + Player.MAX_SHIPS+ "\n";
			message += getString(R.string.player_status_total_city_walls) + ": "
					+ player.getNumOwnedCityWalls() + " / " + Player.MAX_CITY_WALLS + "\n";
			message += getString(R.string.player_status_total_basic_knights) + ": "
					+ player.getNumOwnedBasicKnights() + " / " + Player.MAX_BASIC_KNIGHTS + "\n";
			message += getString(R.string.player_status_total_strong_knights) + ": "
					+ player.getNumOwnedStrongKnights() + " / " + Player.MAX_STRONG_KNIGHTS + "\n";
			message += getString(R.string.player_status_total_mighty_knights) + ": "
					+ player.getNumOwnedMightyKnights() + " / " + Player.MAX_MIGHTY_KNIGHTS+ "\n";
			if(board.getMetropolisOwners()[0] == player.getPlayerNumber()){
				message += "Trade Metropolis (2 points): 1/1\n";
			}
			if(board.getMetropolisOwners()[1] == player.getPlayerNumber()){
				message += "Science Metropolis (2 points): 1/1\n";
			}
			if(board.getMetropolisOwners()[2] == player.getPlayerNumber()){
				message += "Politics Metropolis (2 points): 1/1\n";
			}

			message += "\nDefender Of Catan: " + player.getDefenderOfCatan();

			message += "\n";

			message += getString(R.string.player_status_best_road_length) + ": "
					+ player.getMyLongestTradeRouteLength() + "\n";
			if (player == board.getLongestTradeRouteOwner())
				message += getString(R.string.player_status_has_longest_road) + ": "
						+ "2 " + getString(R.string.player_status_points_str) + "\n";

			message += "\n";


			//show city improvement status
			int[] playerCityImprovementLevels = player.getCityImprovementLevels();
			int playerTradeLevel = playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)];
			int playerScienceLevel = playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)];
			int playerPoliticsLevel = playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)];

			message += "Trade Improvement Level: " + playerTradeLevel + "\n";
			message += "Science Improvement Level: " + playerScienceLevel + "\n";
			message += "Politics Improvement Level: " + playerPoliticsLevel + "\n\n";

			if(board.getMerchantOwner() == player.getPlayerNumber()){

				message += "Owns Merchant (1 VP): " + getString(Resource.toRString(board.getMerchantType())) + " harbour\n\n";
			}

			boolean hasHarbor = false;
			if (player.hasHarbor(null)) {
				message += "3:1 " + getString(R.string.player_status_harbor) + "\n";
				hasHarbor = true;
			}

			for (int j = 0; j < Resource.RESOURCE_TYPES.length; j++) {
				if (player.hasHarbor(Resource.RESOURCE_TYPES[j])
						&& Resource.RESOURCE_TYPES[j] != Resource.ResourceType.GOLD) {
					message += getString(Resource
							.toRString(Resource.RESOURCE_TYPES[j]))
							+ " " + getString(R.string.player_status_harbor) + "\n";
					hasHarbor = true;
				}
			}

			if (hasHarbor)
				message += "\n";

			String turn = player.getActionLog();
			if (player == board.getPlayerOfCurrentGameTurn() && turn != "")
			{
				message += getString(R.string.player_status_this_turn) + ":\n" + turn;
			}
			else if (turn != "")
			{
				message += getString(R.string.player_status_last_turn) + ":\n" + turn;
			}

			TextView text = (TextView) views[i].findViewById(R.id.status_text);
			text.setText(message);

			TextView point = (TextView) views[i].findViewById(R.id.status_points);
			int bootOwner = 0;
			if(board.playerNumBootOwner == player.getPlayerNumber()) bootOwner = 1;
			point.setText(getString(R.string.player_status_victory_points) + ": "
					+ points + " / " + (board.getMaxPoints() + bootOwner));

			ProgressBar progress = (ProgressBar) views[i].findViewById(R.id.status_progress);
			progress.setMax(board.getMaxPoints() + bootOwner);
			progress.setProgress(points);
		}

		final View view = inflater.inflate(R.layout.pager_title_strip, null, false);

		ViewPager viewPager = (ViewPager) view.findViewById(R.id.status);
		viewPager.setAdapter(new StatusTabAdapter());
		viewPager.setCurrentItem(board.getPlayerOfCurrentGameTurn().getPlayerNumber());
		
		PagerTitleStrip titleStrip = (PagerTitleStrip) view.findViewById(R.id.status_title_strip);
		titleStrip.setBackgroundColor(TextureManager.darken(TextureManager.getColor(
				board.getPlayerById(board.getPlayerOfCurrentGameTurn().getPlayerNumber()).getColor()), 0.35));
		
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float offset, int offsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				int color = TextureManager.getColor(board.getPlayerById(position).getColor());
				color = TextureManager.darken(color, 0.35);
				
				PagerTitleStrip titleStrip = (PagerTitleStrip) view.findViewById(R.id.status_title_strip);
				titleStrip.setBackgroundColor(color);
			}
		});

		return view;

	}

	public class StatusTabAdapter extends PagerAdapter {

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
			return board.getPlayerById(position).getPlayerName();
		}
	}
}
