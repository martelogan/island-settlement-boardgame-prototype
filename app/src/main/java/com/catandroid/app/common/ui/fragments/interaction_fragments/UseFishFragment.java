package com.catandroid.app.common.ui.fragments.interaction_fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.catandroid.app.R;
import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.CityImprovement;
import com.catandroid.app.common.components.board_pieces.ProgressCard;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;

public class UseFishFragment extends Fragment {


	private Board board;

	private ActiveGameFragment activeGameFragment;

	Player currentPlayer;

	int numFishOWned;

	public UseFishFragment(){

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

		numFishOWned = currentPlayer.getNumOwnedFish();


		getActivity().setTitle("Fish");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

		View view = inflater.inflate(R.layout.fish_owned_actions, null, false);

		setShopViewVisibility(view);

		if(board.playerNumBootOwner == currentPlayer.getPlayerNumber()){
			Button boot = (Button) view.findViewById(R.id.giveBoot);
			boot.setEnabled(true);
			boot.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//TODO add check if noone has resources
					boolean someoneToGiveBoot = false;
					//choose player to steal from
					CharSequence[] items = new CharSequence[(board.getNumPlayers()-1)];
					final int[] toGiveTo = new int[board.getNumPlayers()-1];
					int current = 0;
					for(int i = 0; i < board.getNumPlayers(); i++){
						if(i != currentPlayer.getPlayerNumber()){
							items[current] = board.getPlayerById(i).getPlayerName();
							toGiveTo[current] = i;
							current++;
							if(board.getPlayerById(i).getVictoryPoints() >= currentPlayer.getVictoryPoints()) someoneToGiveBoot = true;
						}
					}

					if(!someoneToGiveBoot){
						toast("Nobody to give boot to!");
						return;
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Choose Player to give the boot");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if(board.getPlayerById(toGiveTo[item]).getVictoryPoints() >= currentPlayer.getVictoryPoints()){
								board.playerNumBootOwner = board.getPlayerById(toGiveTo[item]).getPlayerNumber();
								getFragmentManager().popBackStack();
								toast("Gave boot to: "
										+ board.getPlayerById(toGiveTo[item]).getPlayerName());
								currentPlayer.appendAction(R.string.player_gave_boot,board.getPlayerById(toGiveTo[item]).getPlayerName() );
							} else{
								toast("This player has less VP than you, choose other player!");
							}

						}
					});

					AlertDialog chooseFreeResourceDialog = builder.create();
					chooseFreeResourceDialog.setCancelable(true);
					chooseFreeResourceDialog.show();
				}
			});

		}

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

	private void setShopViewVisibility(View fish_owned_actions){

		Button owned2 = null;
		Button owned3 = null;
		Button owned4 = null;
		Button owned5 = null;
		Button owned7 = null;

		if(numFishOWned >= 2){
			owned2 = (Button) fish_owned_actions.findViewById(R.id.buyFish2);
			owned2.setEnabled(true);
		}
		if(numFishOWned >= 3){
			owned3 = (Button) fish_owned_actions.findViewById(R.id.buyFish3);
			owned3.setEnabled(true);
		}
		if(numFishOWned >= 4){
			owned4 = (Button) fish_owned_actions.findViewById(R.id.buyFish4);
			owned4.setEnabled(true);
		}
		if(numFishOWned >= 5){
			owned5 = (Button) fish_owned_actions.findViewById(R.id.buyFish5);
			owned5.setEnabled(true);
		}
		if(numFishOWned >= 7){
			owned7 = (Button) fish_owned_actions.findViewById(R.id.buyFish7);
			owned7.setEnabled(true);
		}


		//set the onclick listeners
		if(owned2 != null) {
			owned2.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//choose player to steal from
					CharSequence[] items = new CharSequence[2];
					items[0] = "Pirate";
					items[1] = "Robber";

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Disable Pirate or Robber?");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if(item == 0){
								//pirate
								board.setPirateDisabled(true);
								getFragmentManager().popBackStack();
								toast("Removed the Pirate");
								currentPlayer.appendAction(R.string.player_removedPirate);
								currentPlayer.setNumOwnedFish(numFishOWned-2);
							} else{
								//robber
								board.setRobberDisabled(true);
								getFragmentManager().popBackStack();
								toast("Removed the Robber");
								currentPlayer.appendAction(R.string.player_removed_robber);
								currentPlayer.setNumOwnedFish(numFishOWned-2);
							}
						}
					});

					AlertDialog chooseFreeResourceDialog = builder.create();
					chooseFreeResourceDialog.setCancelable(true);
					chooseFreeResourceDialog.show();
				}
			});
		}
		if(owned3 != null) {
			owned3.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//TODO add check if noone has resources
					boolean someoneToStealFrom = false;
					//choose player to steal from
					CharSequence[] items = new CharSequence[(board.getNumPlayers()-1)];
					//items[0] = "Fred";
					final int[] toStealFrom = new int[board.getNumPlayers()-1];
					int current = 0;
					for(int i = 0; i < board.getNumPlayers(); i++){
						if(i != currentPlayer.getPlayerNumber()){
							items[current] = board.getPlayerById(i).getPlayerName();
							toStealFrom[current] = i;
							current++;
							if(board.getPlayerById(i).getResourceCount() > 0) someoneToStealFrom = true;
						}
					}

					if(!someoneToStealFrom){
						toast("Nobody to steal from!");
						return;
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Choose Player to Steal From");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if(board.getPlayerById(toStealFrom[item]).getResourceCount() > 0){
								Resource.ResourceType stolen = currentPlayer.steal(board.getPlayerById(toStealFrom[item]));
								getFragmentManager().popBackStack();
								currentPlayer.setNumOwnedFish(numFishOWned-3);
								toast(getString(R.string.game_stole_str) + " "
										+ getActivity().getString(Resource.toRString(stolen))
										+ " " + getString(R.string.game_from_str) + " "
										+ board.getPlayerById(toStealFrom[item]).getPlayerName());
								currentPlayer.appendAction(R.string.player_stole_from,board.getPlayerById(toStealFrom[item]).getPlayerName() );
							} else{
								toast("This player has no resource! Choose another Player.");
							}

						}
					});

					AlertDialog chooseFreeResourceDialog = builder.create();
					chooseFreeResourceDialog.setCancelable(true);
					chooseFreeResourceDialog.show();

				}
			});
		}
		if(owned4 != null) {
			owned4.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//choose free resource
					CharSequence[] items = new CharSequence[Resource.numBaseCatanResourceTypes];
					for(int i = 0; i < items.length; i++){
						items[i] = getString(Resource.toRString(Resource.RESOURCE_TYPES[i]));
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Choose Free Resource");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							currentPlayer.addResources(Resource.RESOURCE_TYPES[item],1, false);
							getFragmentManager().popBackStack();
							currentPlayer.setNumOwnedFish(numFishOWned-4);
							currentPlayer.appendAction(R.string.player_received_resource, Resource.toRString(Resource.RESOURCE_TYPES[item]));
							toast("Received " + getString(Resource.toRString(Resource.RESOURCE_TYPES[item])));
						}
					});

					AlertDialog chooseFreeResourceDialog = builder.create();
					chooseFreeResourceDialog.setCancelable(true);
					chooseFreeResourceDialog.show();

				}
			});
		}
		if(owned5 != null) {
			owned5.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//build free road or ship
					CharSequence[] items = new CharSequence[2];
					items[0] = "Build Ship";
					items[1] = "Build Road";

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Build road or ship?");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							currentPlayer.setFreeBuild(true);
							if(item == 0){
								if(currentPlayer.canBuildSomeShip()){
									currentPlayer.setFreeBuildUnit(0);
									getFragmentManager().popBackStack();
									currentPlayer.appendAction(R.string.player_ship);
									currentPlayer.setNumOwnedFish(numFishOWned-5);

								} else {
									toast("No place to build ship");
									currentPlayer.setFreeBuild(false);
								}
							} else{
								if(currentPlayer.canBuildSomeRoad()){
									currentPlayer.setFreeBuildUnit(1);
									getFragmentManager().popBackStack();
									currentPlayer.appendAction(R.string.player_road);
									currentPlayer.setNumOwnedFish(numFishOWned-5);
								} else {
									toast("No places to build road!");
									currentPlayer.setFreeBuild(false);
								}

							}

						}
					});

					AlertDialog chooseFreeResourceDialog = builder.create();
					chooseFreeResourceDialog.setCancelable(true);
					chooseFreeResourceDialog.show();
				}
			});
		}
		if(owned7 != null) {
			owned7.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//choose free progress card
					CharSequence[] items = new CharSequence[CityImprovement.CITY_IMPROVEMENT_TYPES.length];

					for(int i = 0; i < items.length; i++){
						items[i] = getString(CityImprovement.toRString(CityImprovement.CITY_IMPROVEMENT_TYPES[i]));
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Choose Free Progress Card");
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if(!board.progressCardStackEmpty(CityImprovement.CITY_IMPROVEMENT_TYPES[item])){
								ProgressCard.ProgressCardType gained = currentPlayer.gainProgressCard(CityImprovement.CITY_IMPROVEMENT_TYPES[item]);
								getFragmentManager().popBackStack();
								currentPlayer.appendAction(R.string.player_received_card);
								if(gained != (ProgressCard.ProgressCardType.CONSTITUTION) && gained != ProgressCard.ProgressCardType.PRINTER) {
									toast("Received: " + getString(ProgressCard.getCardStringResource(gained)));
								}
								currentPlayer.setNumOwnedFish(numFishOWned-7);

							} else{
								toast("No more cards available in that deck!");
							}
						}
					});

					AlertDialog chooseFreeResourceDialog = builder.create();
					chooseFreeResourceDialog.setCancelable(true);
					chooseFreeResourceDialog.show();

				}
			});
		}

	}

	private void toast(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}
}
