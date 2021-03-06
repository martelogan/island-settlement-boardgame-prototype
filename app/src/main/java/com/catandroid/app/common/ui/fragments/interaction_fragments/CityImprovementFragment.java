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
package com.catandroid.app.common.ui.fragments.interaction_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.catandroid.app.R;
import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.CityImprovement;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;

public class CityImprovementFragment extends Fragment {

	private static final int[] LAYOUTS = { R.layout.city_improvement_purchased, R.layout.city_improvement_shop};

	private static final int[] NAMES = { R.string.current_improvements, R.string.shop_improvements};

	private View[] views;

	private Board board;

	private ActiveGameFragment activeGameFragment;

	private int[] playerCityImprovementLevels;

	Player currentPlayer;

	int playerTradeLevel;
	int playerScienceLevel;
	int playerPoliticsLevel;

	public CityImprovementFragment(){

	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public void setActiveGameFragment(ActiveGameFragment activeGameFragment) {
		this.activeGameFragment = activeGameFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		currentPlayer = board.getPlayerFromParticipantId(activeGameFragment.myParticipantId);

		playerCityImprovementLevels = currentPlayer.getCityImprovementLevels();

		playerTradeLevel = playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)];
		playerScienceLevel = playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)];
		playerPoliticsLevel = playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)];


		getActivity().setTitle(getString(R.string.city_improvements));
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

		View view = inflater.inflate(R.layout.costs_reference, null, false);

		views = new View[LAYOUTS.length];
		
		for (int i = 0; i < LAYOUTS.length; i++){
			views[i] = inflater.inflate(LAYOUTS[i], null);
		}

		//Setup the city_improvement_purchased based on the player's level
		View city_improvement_purchased = views[0];
		setPurchasedViewVisibility(city_improvement_purchased);

		//setup the city_improvement_shop based on the current players city count and resources
		View city_improvement_shop = views[1];
		setShopViewVisibility(city_improvement_shop);

		
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.costs_reference);
		viewPager.setAdapter(new ReferenceTabAdapter());
		viewPager.setCurrentItem(0);

		Log.d("myTag", "about to return city improvement view");
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

	private boolean canPurchaseCityImprovement(CityImprovement.CityImprovementType cityImprovementType, int levelDesired){
		//check has at least one city
		boolean hasNonMetropolisCity = currentPlayer.getNonMetropolisCities() > 0;
		boolean hasCity = currentPlayer.getNumOwnedCities() > 0;

		//check if have enough resources
		int clothCount = currentPlayer.getResources(Resource.ResourceType.CLOTH);
		int coinCount = currentPlayer.getResources(Resource.ResourceType.COIN);
		int paperCount = currentPlayer.getResources(Resource.ResourceType.PAPER);

		boolean enoughResources = false;

		switch(cityImprovementType){
			case POLITICS:
				if(coinCount >= levelDesired){
					enoughResources = true;
				}
				break;
			case SCIENCE:
				if(paperCount >= levelDesired){
					enoughResources = true;
				}
				break;
			case TRADE:
				if(clothCount >= levelDesired){
					enoughResources = true;
				}
				break;
		}

		if(levelDesired >= 4){
			return hasNonMetropolisCity && enoughResources;
		} else {
			return hasCity && enoughResources;
		}

	}

	private void setPurchasedViewVisibility(View city_improvement_purchased){
		switch (playerTradeLevel){
			case 0:
				city_improvement_purchased.findViewById(R.id.trade_level0).setVisibility(View.VISIBLE);
				break;
			case 1:
				city_improvement_purchased.findViewById(R.id.trade_level1).setVisibility(View.VISIBLE);
				break;
			case 2:
				city_improvement_purchased.findViewById(R.id.trade_level2).setVisibility(View.VISIBLE);
				break;
			case 3:
				city_improvement_purchased.findViewById(R.id.trade_level3).setVisibility(View.VISIBLE);
				break;
			case 4:
				city_improvement_purchased.findViewById(R.id.trade_level4).setVisibility(View.VISIBLE);
				break;
			case 5:
				city_improvement_purchased.findViewById(R.id.trade_level5).setVisibility(View.VISIBLE);
				break;
		}

		switch (playerScienceLevel){
			case 0:
				city_improvement_purchased.findViewById(R.id.science_level0).setVisibility(View.VISIBLE);
				break;
			case 1:
				city_improvement_purchased.findViewById(R.id.science_level1).setVisibility(View.VISIBLE);
				break;
			case 2:
				city_improvement_purchased.findViewById(R.id.science_level2).setVisibility(View.VISIBLE);
				break;
			case 3:
				city_improvement_purchased.findViewById(R.id.science_level3).setVisibility(View.VISIBLE);
				break;
			case 4:
				city_improvement_purchased.findViewById(R.id.science_level4).setVisibility(View.VISIBLE);
				break;
			case 5:
				city_improvement_purchased.findViewById(R.id.science_level5).setVisibility(View.VISIBLE);
				break;
		}

		switch (playerPoliticsLevel){
			case 0:
				city_improvement_purchased.findViewById(R.id.politics_level0).setVisibility(View.VISIBLE);
				break;
			case 1:
				city_improvement_purchased.findViewById(R.id.politics_level1).setVisibility(View.VISIBLE);
				break;
			case 2:
				city_improvement_purchased.findViewById(R.id.politics_level2).setVisibility(View.VISIBLE);
				break;
			case 3:
				city_improvement_purchased.findViewById(R.id.politics_level3).setVisibility(View.VISIBLE);
				break;
			case 4:
				city_improvement_purchased.findViewById(R.id.politics_level4).setVisibility(View.VISIBLE);
				break;
			case 5:
				city_improvement_purchased.findViewById(R.id.politics_level5).setVisibility(View.VISIBLE);
				break;
		}
	}

	private void setShopViewVisibility(View city_improvement_shop){
		boolean canAffordTradeImprovement = canPurchaseCityImprovement(CityImprovement.CityImprovementType.TRADE, playerTradeLevel+1);
		boolean canAffordScienceImprovement = canPurchaseCityImprovement(CityImprovement.CityImprovementType.SCIENCE, playerScienceLevel+1);
		boolean canAffordPoliticsImprovement = canPurchaseCityImprovement(CityImprovement.CityImprovementType.POLITICS, playerPoliticsLevel+1);

		View tradeView;
		Button tradeButton;
		View scienceView;
		Button scienceButton;
		View politicsView;
		Button politicsButton;
		
		switch (playerTradeLevel){
			case 0:
				tradeView = city_improvement_shop.findViewById(R.id.trade_level0);
				tradeButton = (Button) city_improvement_shop.findViewById(R.id.trade_level0).findViewById(R.id.buyTrade1);
				break;
			case 1:
				tradeView = city_improvement_shop.findViewById(R.id.trade_level1);
				tradeButton = (Button) city_improvement_shop.findViewById(R.id.trade_level1).findViewById(R.id.buyTrade2);
				break;
			case 2:
				tradeView = city_improvement_shop.findViewById(R.id.trade_level2);
				tradeButton = (Button) city_improvement_shop.findViewById(R.id.trade_level2).findViewById(R.id.buyTrade3);
				break;
			case 3:
				tradeView = city_improvement_shop.findViewById(R.id.trade_level3);
				tradeButton = (Button) city_improvement_shop.findViewById(R.id.trade_level3).findViewById(R.id.buyTrade4);
				break;
			case 4:
				tradeView = city_improvement_shop.findViewById(R.id.trade_level4);
				tradeButton = (Button) city_improvement_shop.findViewById(R.id.trade_level4).findViewById(R.id.buyTrade5);
				break;
			case 5:
				tradeView = city_improvement_shop.findViewById(R.id.trade_level5);
				tradeButton = null;
				break;
			default:
				tradeView = null;
				tradeButton = null;
				break;
		}

		switch (playerScienceLevel){
			case 0:
				scienceView = city_improvement_shop.findViewById(R.id.science_level0);
				scienceButton = (Button) city_improvement_shop.findViewById(R.id.science_level0).findViewById(R.id.buyScience1);
				break;
			case 1:
				scienceView = city_improvement_shop.findViewById(R.id.science_level1);
				scienceButton = (Button) city_improvement_shop.findViewById(R.id.science_level1).findViewById(R.id.buyScience2);
				break;
			case 2:
				scienceView = city_improvement_shop.findViewById(R.id.science_level2);
				scienceButton = (Button) city_improvement_shop.findViewById(R.id.science_level2).findViewById(R.id.buyScience3);
				break;
			case 3:
				scienceView = city_improvement_shop.findViewById(R.id.science_level3);
				scienceButton = (Button) city_improvement_shop.findViewById(R.id.science_level3).findViewById(R.id.buyScience4);
				break;
			case 4:
				scienceView = city_improvement_shop.findViewById(R.id.science_level4);
				scienceButton = (Button) city_improvement_shop.findViewById(R.id.science_level4).findViewById(R.id.buyScience5);
				break;
			case 5:
				scienceView = city_improvement_shop.findViewById(R.id.science_level5);
				scienceButton = null;
				break;
			default:
				scienceView = null;
				scienceButton = null;
				break;
		}

		switch (playerPoliticsLevel){
			case 0:
				politicsView = city_improvement_shop.findViewById(R.id.politics_level0);
				politicsButton = (Button) city_improvement_shop.findViewById(R.id.politics_level0).findViewById(R.id.buyPolitics1);
				break;
			case 1:
				politicsView = city_improvement_shop.findViewById(R.id.politics_level1);
				politicsButton = (Button) city_improvement_shop.findViewById(R.id.politics_level1).findViewById(R.id.buyPolitics2);
				break;
			case 2:
				politicsView = city_improvement_shop.findViewById(R.id.politics_level2);
				politicsButton = (Button) city_improvement_shop.findViewById(R.id.politics_level2).findViewById(R.id.buyPolitics3);
				break;
			case 3:
				politicsView = city_improvement_shop.findViewById(R.id.politics_level3);
				politicsButton = (Button) city_improvement_shop.findViewById(R.id.politics_level3).findViewById(R.id.buyPolitics4);
				break;
			case 4:
				politicsView = city_improvement_shop.findViewById(R.id.politics_level4);
				politicsButton = (Button) city_improvement_shop.findViewById(R.id.politics_level4).findViewById(R.id.buyPolitics5);
				break;
			case 5:
				politicsView = city_improvement_shop.findViewById(R.id.politics_level5);
				politicsButton = null;
				break;
			default:
				politicsView = null;
				politicsButton = null;
				break;
		}

		//Set the views as visible
		if(tradeView != null){
			tradeView.setVisibility(View.VISIBLE);
		}
		if(scienceView != null){
			scienceView.setVisibility(View.VISIBLE);
		}
		if(politicsView != null){
			politicsView.setVisibility(View.VISIBLE);
		}

		//set the button as visible
		if(tradeButton != null){
			tradeButton.setEnabled(canAffordTradeImprovement);
		}
		if(scienceButton != null){
			scienceButton.setEnabled(canAffordScienceImprovement);
		}
		if(politicsButton != null){
			politicsButton.setEnabled(canAffordPoliticsImprovement);
		}

		//set the onclick listeners
		if(tradeButton != null) {
			tradeButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
				// Perform buy on click because only pressable if they can afford it
					currentPlayer.appendAction(R.string.player_tradeImp, Integer.toString((playerTradeLevel + 1)));

				// increase their discipline level
				playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)] = playerTradeLevel + 1;

				// Remove the resources from the player
					currentPlayer.useResources(Resource.ResourceType.CLOTH,
				playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)]);

				// if level just bought is 3, we need to give special priviledge
				//now commodities harbours!
					if (playerTradeLevel == 2) {
					currentPlayer.setTradeValue(Resource.ResourceType.CLOTH);
					currentPlayer.setTradeValue(Resource.ResourceType.PAPER);
					currentPlayer.setTradeValue(Resource.ResourceType.COIN);
				}

				//if we bought level 4 or 5 they now own the metropolis of that color
				//if steal if from someone else, we need to change thiers back to city
				//prompt the user to chose which city becomes metropolis
				//change that city to the metropolis
				boolean buildNewMetropolis = false;
					if(playerTradeLevel == 3 || playerTradeLevel == 4){

					int currentTradeMetropolisOwner = board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)];
					if(playerTradeLevel == 4 && currentTradeMetropolisOwner != -1
							&& currentTradeMetropolisOwner != currentPlayer.getPlayerNumber()) {
						boolean tradeMetropolisIsStealable = (board.getPlayerById(currentTradeMetropolisOwner).getCityImprovementLevels()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)] == 4);
						if (tradeMetropolisIsStealable) {
							//remove the metropolis from the current user
							for (Vertex vertex : board.getVertices()) {
								switch (vertex.getCurUnitType()) {
									case Vertex.TRADE_METROPOLIS:
										vertex.setCurUnitType(Vertex.CITY);
										break;
									case Vertex.WALLED_TRADE_METROPOLIS:
										vertex.setCurUnitType(Vertex.WALLED_CITY);
										break;
									default:
										break;
								}
							}
							//reset the current owner
							board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)] = currentPlayer.getPlayerNumber();
							buildNewMetropolis = true;
                            currentPlayer.appendAction(R.string.player_stole_trade, board.getPlayerById(currentTradeMetropolisOwner).getPlayerName());
						} else {
							toast("Could not steal the metropolis!");
						}
					} else if (currentTradeMetropolisOwner == -1){
						//we are the first ones to build it so just give it to us
						board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.TRADE)] = currentPlayer.getPlayerNumber();
						buildNewMetropolis = true;
					} else if (currentTradeMetropolisOwner == currentPlayer.getPlayerNumber()){
						//we already own it and should keep it,
						//the level is already changed at this point
					}
				}

				toast(getString(R.string.player_tradeImp, Integer.toString((playerTradeLevel + 1))));
				getFragmentManager().popBackStack();

				//ask the user to select the city to build the metropolis on if needed
					if(buildNewMetropolis){
					currentPlayer.metropolisTypeToBuild = Vertex.TRADE_METROPOLIS;
					board.setPhase(Board.Phase.BUILD_METROPOLIS);
				}
			}
			});
		}

		if(scienceButton != null) {
			scienceButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// Perform buy on click because only pressable if they can afford it
					currentPlayer.appendAction(R.string.player_scienceImp, Integer.toString((playerScienceLevel + 1)));
					// increase their discipline level
					playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)] = playerScienceLevel + 1;
					// Remove the resources from the player
					currentPlayer.useResources(Resource.ResourceType.PAPER,
							playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)]);

					// if level just bought is 3, we need to give special priviledge


					//if we bought level 4 or 5 they now own the metropolis of that color
					//if steal if from someone else, we need to change thiers back to city
					//prompt the user to chose which city becomes metropolis
					//change that city to the metropolis
					boolean buildNewMetropolis = false;
					if(playerScienceLevel == 3 || playerScienceLevel == 4){

						int currentScienceMetropolisOwner = board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)];
						if(playerTradeLevel == 4 && currentScienceMetropolisOwner != -1
								&& currentScienceMetropolisOwner != currentPlayer.getPlayerNumber()) {
							boolean scienceMetropolisIsStealable = (board.getPlayerById(currentScienceMetropolisOwner).getCityImprovementLevels()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)] == 4);
							if (scienceMetropolisIsStealable) {
								//remove the metropolis from the current user
								for (Vertex vertex : board.getVertices()) {
									switch (vertex.getCurUnitType()) {
										case Vertex.SCIENCE_METROPOLIS:
											vertex.setCurUnitType(Vertex.CITY);
											break;
										case Vertex.WALLED_SCIENCE_METROPOLIS:
											vertex.setCurUnitType(Vertex.WALLED_CITY);
											break;
										default:
											break;
									}
								}
								//reset the current owner
								board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)] = currentPlayer.getPlayerNumber();
								buildNewMetropolis = true;
                                currentPlayer.appendAction(R.string.player_stole_science, board.getPlayerById(currentScienceMetropolisOwner).getPlayerName());
							} else {
								toast("Could not steal the science metropolis!");
							}
						} else if (currentScienceMetropolisOwner == -1){
							//we are the first ones to build it so just give it to us
							board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)] = currentPlayer.getPlayerNumber();
							buildNewMetropolis = true;
						} else if (currentScienceMetropolisOwner == currentPlayer.getPlayerNumber()){
							//we already own it and should keep it,
							//the level is already changed at this point
						}
					}

					toast(getString(R.string.player_scienceImp, Integer.toString((playerScienceLevel + 1))));
					getFragmentManager().popBackStack();

					//ask the user to select the city to build the metropolis on if needed
					if(buildNewMetropolis){
						currentPlayer.metropolisTypeToBuild = Vertex.SCIENCE_METROPOLIS;
						board.setPhase(Board.Phase.BUILD_METROPOLIS);
					}

				}
			});
		}

		if(politicsButton != null) {
			politicsButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// Perform buy on click because only pressable if they can afford it
					currentPlayer.appendAction(R.string.player_politicsImp, Integer.toString((playerTradeLevel + 1)));
					// increase their discipline level
					playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)] = playerPoliticsLevel + 1;
					// Remove the resources from the player
					currentPlayer.useResources(Resource.ResourceType.COIN,
							playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)]);

					// if level just bought is 3, we need to give special priviledge
					//Offers the opportunity to promote knights to mighty knights
					//can check in knight functionality, the level of politics

					//if we bought level 4 they now own the metropolis of that color
					//if steal if from someone else, we need to change thiers back to city
					//prompt the user to chose which city becomes metropolis
					//change that city to the metropolis
					//if we bought level5 and someone else if level4 and owns metropolis we steal from them
					//if steal if from someone else, we need to change thiers back to city
					//prompt the user to chose which city becomes metropolis
					//change that city to the metropolis
					boolean buildNewMetropolis = false;
					if(playerPoliticsLevel == 3 || playerPoliticsLevel == 4){

						int currentPoliticsMetropolisOwner = board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)];
						if(playerTradeLevel == 4 && currentPoliticsMetropolisOwner != -1
								&& currentPoliticsMetropolisOwner != currentPlayer.getPlayerNumber()) {
							boolean politicsMetropolisIsStealable = (board.getPlayerById(currentPoliticsMetropolisOwner).getCityImprovementLevels()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)] == 4);
							if (politicsMetropolisIsStealable) {
								//remove the metropolis from the current user
								for (Vertex vertex : board.getVertices()) {
									switch (vertex.getCurUnitType()) {
										case Vertex.POLITICS_METROPOLIS:
											vertex.setCurUnitType(Vertex.CITY);
											break;
										case Vertex.WALLED_POLITICS_METROPOLIS:
											vertex.setCurUnitType(Vertex.WALLED_CITY);
											break;
										default:
											break;
									}
								}
								//reset the current owner
								board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)] = currentPlayer.getPlayerNumber();
								buildNewMetropolis = true;
                                currentPlayer.appendAction(R.string.player_stole_politics, board.getPlayerById(currentPoliticsMetropolisOwner).getPlayerName());
							} else {
								toast("Could not steal the politics metropolis!");
							}
						} else if (currentPoliticsMetropolisOwner == -1){
							//we are the first ones to build it so just give it to us
							board.getMetropolisOwners()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.POLITICS)] = currentPlayer.getPlayerNumber();
							buildNewMetropolis = true;
						} else if (currentPoliticsMetropolisOwner == currentPlayer.getPlayerNumber()){
							//we already own it and should keep it,
							//the level is already changed at this point
						}
					}

					toast(getString(R.string.player_politicsImp, Integer.toString((playerPoliticsLevel + 1))));
					getFragmentManager().popBackStack();

					//ask the user to select the city to build the metropolis on if needed
					if(buildNewMetropolis){
						currentPlayer.metropolisTypeToBuild = Vertex.POLITICS_METROPOLIS;
						board.setPhase(Board.Phase.BUILD_METROPOLIS);
					}

				}
			});
		}
	}

	private void toast(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}
}
