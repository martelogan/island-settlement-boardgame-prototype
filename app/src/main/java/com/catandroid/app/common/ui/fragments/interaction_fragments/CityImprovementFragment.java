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

		
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.reference);
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
		boolean hasCity = currentPlayer.getNumCities() > 0;

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

		return hasCity && enoughResources;
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

					//if we bought level 4 they now own the metropolis of that color
					//if steal if from someone else, we need to change thiers back to city
					//prompt the user to chose which city becomes metropolis
					//change that city to the metropolis
					//if we bought level5 and someone else if level4 and owns metropolis we steal from them
					//if steal if from someone else, we need to change thiers back to city
					//prompt the user to chose which city becomes metropolis
					//change that city to the metropolis
					toast(getString(R.string.player_tradeImp, Integer.toString((playerTradeLevel + 1))));
					getFragmentManager().popBackStack();
				}
			});
		}

		if(scienceButton != null) {
			scienceButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// Perform buy on click because only pressable if they can afford it
					currentPlayer.appendAction(R.string.player_scienceImp, Integer.toString((playerTradeLevel + 1)));
					// increase their discipline level
					playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)] = playerScienceLevel + 1;
					// Remove the resources from the player
					currentPlayer.useResources(Resource.ResourceType.PAPER,
							playerCityImprovementLevels[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)]);

					// if level just bought is 3, we need to give special priviledge

					//if we bought level 4 they now own the metropolis of that color
					//if steal if from someone else, we need to change thiers back to city
					//prompt the user to chose which city becomes metropolis
					//change that city to the metropolis
					//if we bought level5 and someone else if level4 and owns metropolis we steal from them
					//if steal if from someone else, we need to change thiers back to city
					//prompt the user to chose which city becomes metropolis
					//change that city to the metropolis
					toast(getString(R.string.player_scienceImp, Integer.toString((playerScienceLevel + 1))));
					getFragmentManager().popBackStack();

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
					toast(getString(R.string.player_politicsImp, Integer.toString((playerPoliticsLevel + 1))));
					getFragmentManager().popBackStack();
				}
			});
		}
	}

	private void toast(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}
}
