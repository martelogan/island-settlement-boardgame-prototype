package com.catandroid.app.common.ui.fragments;

import com.catandroid.app.common.logistics.multiplayer.TradeProposal;
import com.catandroid.app.common.components.board_pieces.CityImprovement;
import com.catandroid.app.common.components.board_pieces.ProgressCard;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.AutomatedPlayer;
import com.catandroid.app.common.ui.fragments.interaction_fragments.CityImprovementFragment;
import com.catandroid.app.common.ui.fragments.interaction_fragments.DiscardResourcesFragment;
import com.catandroid.app.R;;
import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.ui.fragments.interaction_fragments.UseFishFragment;
import com.catandroid.app.common.ui.fragments.interaction_fragments.trade.TradeProposedFragment;
import com.catandroid.app.common.ui.fragments.interaction_fragments.trade.TradeRequestFragment;
import com.catandroid.app.common.ui.graphics_controllers.GameRenderer;
import com.catandroid.app.common.ui.graphics_controllers.GameRenderer.Action;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.ui.resources.UIButton;
import com.catandroid.app.common.ui.views.GameView;
import com.catandroid.app.common.ui.fragments.static_fragments.CostsReferenceFragment;
import com.catandroid.app.common.ui.views.ResourceView;
import com.catandroid.app.common.ui.fragments.static_fragments.PlayerStatsFragment;
import com.catandroid.app.common.ui.resources.UIButton.ButtonType;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.graphics_controllers.TextureManager;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ActiveGameFragment extends Fragment {

	private static final int MIN_BOT_DELAY = 5000;
	private static final int DEFAULT_TURN_DELAY = 750;

	private static final int UPDATE_MESSAGE = 1, LOG_MESSAGE = 2, DISCARD_MESSAGE = 3,
			PICK_PROGRESS_CARD_MESSAGE = 4;

	private RelativeLayout rl;
	private FragmentActivity fa;

	private GameView view;
	private Board board;
	private ResourceView resources;
	private TextureManager texture;

	private UpdateHandler turnHandler;
	private TurnThread turnThread;
	
	private GameRenderer renderer;

	private boolean isActive;
	private boolean showedPlayerGameCreationStats = false;

	private static final String[] ROLL_STRINGS = { "", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅" };
	private static final String[] EVENT_ROLL_STRINGS = { "", "☠", "☠", "☠", "Trade", "Science", "Politics" };

	public Listener mListener = null;

	public String myParticipantId;

	public ActiveGameFragment(){ }

	//Dice Rolls for Alchemist
	private boolean playedIsAlchemist = false;
	private int roll1;
	private int roll2;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(getActivity().findViewById(R.id.setup) != null){
			getActivity().findViewById(R.id.setup).setVisibility(View.GONE);
		}
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
	}

	public void setBoard(Board board){
		this.board = board;
	}

	public interface Listener {
		void endTurn(String nextParticipantId, boolean isWinner);
	}

	public void setmListener(Listener mListener){
		this.mListener = mListener;
	}

	public void setMyParticipantId(String myParticipantId){
		this.myParticipantId = myParticipantId;
	}

	class TurnThread implements Runnable {
		private boolean done;

		@Override
		public void run() {
			done = false;

			while (!done) {
				if (board.getWinner() != null)
				{
					return;
				}

				if (board.hasPlayersYetToAct()) {
					//show popup if we are the ones that should discard
					if(board.checkNextPlayerToAct().getGooglePlayParticipantId().equals(myParticipantId)
							&& (board.getPhase() == Board.Phase.CHOOSE_ROBBER_PIRATE || board.isRobberPhase())){
						Message discard = new Message();
						discard.what = DISCARD_MESSAGE;
						turnHandler.sendMessage(discard);
					}

					if(board.checkNextPlayerToAct().getGooglePlayParticipantId().equals(myParticipantId)
						&& board.getPhase() == Board.Phase.PROGRESS_CARD_STEP_2) {
						//TODO
						//fill up a method through which player picks progress card.

					}

					//show card choose if defended catan
					Player p = board.checkNextPlayerToAct();
					if(p.getGooglePlayParticipantId().equals(myParticipantId)
							&& board.getPhase() == Board.Phase.DEFENDER_OF_CATAN){
						Message pickCard = new Message();
						pickCard.what = PICK_PROGRESS_CARD_MESSAGE;
						turnHandler.sendMessage(pickCard);
					}
				} else if (board.getPlayerOfCurrentGameTurn().isBot()) {
					board.runAITurn();
					Message change = new Message();
					change.what = UPDATE_MESSAGE;
					turnHandler.sendMessage(change);

					if (board.getPlayerOfCurrentGameTurn().isHuman()) {
						Message turn = new Message();
						turn.what = LOG_MESSAGE;
						turnHandler.sendMessage(turn);
					}

					int delay = DEFAULT_TURN_DELAY;
					if (delay > 0) {
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
							return;
						}
					}

					continue;
				}

				try {
					Thread.sleep(MIN_BOT_DELAY);
				} catch (InterruptedException e) {
					return;
				}
			}
		}

		public void end() {
			done = true;
		}
	}

	@SuppressLint("HandlerLeak")
	class UpdateHandler extends Handler {
		ActiveGameFragment activeGameFragment;
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case UPDATE_MESSAGE:
					showState(false);
					break;

				case LOG_MESSAGE:
					notifyTurn();
					break;

				case DISCARD_MESSAGE:
					if (board == null)
					{
						return;
					}

					Player toDiscard = board.getPlayerToAct();
					int cards = toDiscard.getResourceCount();
					int extra = cards > 7 ? cards / 2 : 0;

					if (extra == 0)
					{
						break;
					}


					Bundle bundle = new Bundle();
					bundle.putInt(DiscardResourcesFragment.PLAYER_KEY, toDiscard.getPlayerNumber());
					bundle.putInt(DiscardResourcesFragment.QUANTITY_KEY, extra);
					DiscardResourcesFragment discardResourcesFragment = new DiscardResourcesFragment();
					discardResourcesFragment.setArguments(bundle);
					discardResourcesFragment.setBoard(board);
					discardResourcesFragment.setActiveGameFragment(activeGameFragment);

					FragmentTransaction discardFragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
					discardFragmentTransaction.replace(R.id.fragment_container, discardResourcesFragment,"DISCARD");
					discardFragmentTransaction.addToBackStack("DISCARD");
					discardFragmentTransaction.commit();


					break;

				case PICK_PROGRESS_CARD_MESSAGE:
					//show popup for pick card
					Player toPick = board.getPlayerToAct();
					CharSequence[] items = new CharSequence[3];
					items[0] = getString(R.string.tradeImprovement);
					items[1] = getString(R.string.scienceImprovement);
					items[2] = getString(R.string.politicsImprovement);

					Builder builder = new Builder(getActivity());
					builder.setTitle(getString(R.string.game_defended_catan));
					builder.setItems(items, new OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							Player currentPlayer = board.getPlayerFromParticipantId(myParticipantId);
							if (item == 0) {
								currentPlayer.getHand().add(board.pickNewProgressCard(CityImprovement.CityImprovementType.TRADE));
							} else if(item == 1){
								currentPlayer.getHand().add(board.pickNewProgressCard(CityImprovement.CityImprovementType.SCIENCE));
							} else if(item == 2){
								currentPlayer.getHand().add(board.pickNewProgressCard(CityImprovement.CityImprovementType.POLITICS));
							}

							//pass turn
							board.nextPhase();
							showState(true);
						}
					});

					AlertDialog choseProgressCardDialog = builder.create();
					choseProgressCardDialog.setCancelable(false);
					choseProgressCardDialog.show();
				}

				super.handleMessage(msg);
			}
			public void setActiveGameFragment(ActiveGameFragment activeGameFragment){
				this.activeGameFragment = activeGameFragment;
			}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fa = (FragmentActivity) super.getActivity();


		if (texture == null) {
			texture = new TextureManager(getActivity().getResources());
		}

		//changed constructor
		view = new GameView(this, getActivity(), myParticipantId, board);
		view.setBoard(board);
		renderer = new GameRenderer(view, board.getBoardGeometry());
		view.setRenderer(renderer);
		view.requestFocus();
		view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1));



		turnHandler = new UpdateHandler();
		turnHandler.setActiveGameFragment(this);

		return view;
	}

	@Override
	public void onResume() {

		super.onResume();

		turnThread = new TurnThread();
		new Thread(turnThread).start();

		Log.d("myTag", "Created Thread");
		isActive = true;
		showState(false);
	}

	@Override
	public void onPause() {
		isActive = false;
		turnThread.end();
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//http://stackoverflow.com/questions/15653737/oncreateoptionsmenu-inside-fragments
		inflater.inflate(R.menu.active_game_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				//finish();
				// must ask the activity to close this StartScreenActivity Fragment
				getActivity().getSupportFragmentManager().popBackStack();
				return true;
			case R.id.costs_reference:
				CostsReferenceFragment costsReferenceFragment = new CostsReferenceFragment();

				Log.d("myTag", "about to launch costs fragment");
				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.fragment_container, costsReferenceFragment);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();

				return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	public void select(Action action, int id) {
		switch (action) {
			case MOVE_ROBBER:
			case MOVE_PIRATE:
            case PLACE_MERCHANT:
				select(action, board.getHexagonById(id));
				break;

			case BUILD_SETTLEMENT:
			case BUILD_CITY:
			case BUILD_CITY_WALL:
			case BUILD_METROPOLIS:
			case HIRE_KNIGHT:
			case ACTIVATE_KNIGHT:
			case PROMOTE_KNIGHT:
            case CHASE_ROBBER:
            case CHASE_PIRATE:
			case MOVE_KNIGHT_1:
			case MOVE_KNIGHT_2:
			case MOVE_DISPLACED_KNIGHT:
				select(action, board.getVertexById(id));
				break;

			case BUILD_EDGE_UNIT:
			case BUILD_ROAD:
			case BUILD_SHIP:
			case MOVE_SHIP_1:
			case MOVE_SHIP_2:
				select(action, board.getEdgeById(id));
				break;

			default:
				// REMOVED
				//Log.e(getClass().getName(), "invalid selection type");
				break;
			}
	}

	private void select(Action action, Hexagon hexagon) {
		if (action == Action.MOVE_ROBBER) {
			if (hexagon == board.getPrevRobberHex()) {
				popup(getString(R.string.game_cant_move_robber),
						getString(R.string.game_robber_same));
			}
			else if (hexagon.getTerrainType() == Hexagon.TerrainType.SEA) {
				popup(getString(R.string.game_cant_move_robber),
						getString(R.string.game_robber_sea));
			}
			else {
				board.setCurRobberHex(hexagon);
				showState(false);
			}
		}
		else if (action == Action.MOVE_PIRATE) {
			if (hexagon == board.getPrevPirateHex()) {
				popup(getString(R.string.game_cant_move_pirate),
						getString(R.string.game_pirate_same));
			}
			else if (hexagon.getTerrainType() != Hexagon.TerrainType.SEA) {
				popup(getString(R.string.game_cant_move_pirate),
						getString(R.string.game_pirate_land));
			}
			else {
				board.setCurPirateHex(hexagon);
				showState(false);
			}
		}
		else if(action == Action.PLACE_MERCHANT) {
            boolean ownsBuildableOnHex = hexagon.getPlayers().contains(board.getActiveFragmentPlayer());
            if (hexagon.getTerrainType() == Hexagon.TerrainType.SEA) {
                popup(getString(R.string.game_cant_move_merchant),
                        getString(R.string.game_merchant_sea));
            }
            else if(!ownsBuildableOnHex){
                popup("Can't Place Merchant",
                        "You don't own a City/Settlement on that Hexagon");
            }
            else {
                board.setCurMerchantHex(hexagon);
                board.setMerchantOwner(board.getActiveFragmentPlayer().getPlayerNumber());
				board.nextPhase();
                showState(false);
            }
        }
	}

	private void select(Action action, Vertex vertex) {
		int vertexUnitType = Vertex.NONE;
		if (action == Action.BUILD_SETTLEMENT)
		{
			vertexUnitType = Vertex.SETTLEMENT;
		}
		else if (action == Action.BUILD_CITY)
		{
			vertexUnitType = Vertex.CITY;
		}
		else if (action == Action.BUILD_CITY_WALL)
		{
			vertexUnitType = Vertex.WALLED_CITY;
		}
		else if(action == Action.BUILD_METROPOLIS)
		{
			vertexUnitType = board.getPlayerOfCurrentGameTurn().metropolisTypeToBuild;
		}
		else if (action == Action.HIRE_KNIGHT || action == Action.ACTIVATE_KNIGHT
				|| action == Action.PROMOTE_KNIGHT || action == Action.CHASE_ROBBER
                || action == Action.CHASE_PIRATE || action == Action.MOVE_KNIGHT_1
				|| action == Action.MOVE_KNIGHT_2 || action == Action.MOVE_DISPLACED_KNIGHT) {
			vertexUnitType = Vertex.KNIGHT;
		}

		boolean executingPsuedoTurnAction =
				(board.isMyPseudoTurn() && renderer.isPsuedoTurnAction());
		Player player;
		if (executingPsuedoTurnAction) {
			player = view.getActivePlayer();
		} else {
			player = board.getPlayerOfCurrentGameTurn();
		}
		switch(vertexUnitType) {
			case Vertex.SETTLEMENT:
			case Vertex.CITY:
			case Vertex.WALLED_CITY:
			    // selecting buildable vertex unit
				if (player.buildVertexUnit(vertex, vertexUnitType)) {
					if (board.isSetupSettlement() || board.isSetupCity() || board.isBuildMetropolisPhase())
					{
						board.nextPhase();
					}

					showState(false);
				}
				break;

			case Vertex.KNIGHT: // selecting knight unit
				switch(action) {
					case HIRE_KNIGHT:
						if (player.hireKnightTo(vertex)) {
							showState(false);
						}
						break;
					case ACTIVATE_KNIGHT:
						if (player.activateKnightAt(vertex)) {
							showState(false);
						}
						break;
					case PROMOTE_KNIGHT:
						if (player.promoteKnightAt(vertex)) {
							showState(false);
						}
						break;
                    case CHASE_ROBBER:
                        if (player.chaseRobberFrom(vertex)) {
                            showState(false);
                        }
                        break;
                    case CHASE_PIRATE:
                        if (player.chasePirateFrom(vertex)) {
                            showState(false);
                        }
                        break;
					case MOVE_KNIGHT_1:
						if (player.removeKnightFrom(vertex)) {
							renderer.setAction(Action.MOVE_KNIGHT_2);
							showState(true);
						}
						break;
					case MOVE_KNIGHT_2:
						if(vertex.getCurUnitType() == Vertex.KNIGHT
								&& vertex.getOwnerPlayer() != player
								   && player.canDisplaceKnightAt(vertex)) {

								confirmDisplaceKnightDialog(vertex);
						}
						else if (player.moveKnightPeacefullyTo(vertex)) {
							board.nextPhase();
							renderer.setAction(Action.NONE);
							showState(true);
						}
						break;
					case MOVE_DISPLACED_KNIGHT:
						if(player.displaceKnightTo(vertex)) {
							// pass turn back to attacker
							board.nextPhase();
							showState(true);
						}
						break;
					case PLAY_INTRIGUE:
						if(vertex.getCurUnitType() == Vertex.KNIGHT
								&& vertex.getOwnerPlayer() != player
								&& player.canDisplaceKnightAt(vertex)) {

						}
				}
		}
	}

	private void select(Action action, Edge edge) {
        if (action == Action.BUILD_EDGE_UNIT ) {
            if (edge.isAvailableForShip() && !edge.isBlockedByPirate()) {
				if(edge.isBorderingSea()) { // choice of road or ship
					CharSequence[] items = new CharSequence[2];
					items[0] = getString(R.string.game_choose_road);
					items[1] = getString(R.string.game_choose_ship);

					Builder builder = new Builder(getActivity());
					builder.setTitle(getString(R.string.game_road_ship_title));
					final Edge edgeRef = edge;
					builder.setItems(items, new OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if (item == 0) {
								select(Action.BUILD_ROAD, edgeRef);
							} else {
								select(Action.BUILD_SHIP, edgeRef);
							}
						}
					});

					AlertDialog chooseRoadShipDialog = builder.create();
					chooseRoadShipDialog.setCancelable(false);
					chooseRoadShipDialog.show();
				}
				else { // can only build a ship here
					select(Action.BUILD_SHIP, edge);
				}
            }
            else { // can only build a road here
                select(Action.BUILD_ROAD, edge);
            }
        }

        //proceed to build road or edge

        Player player = board.getPlayerOfCurrentGameTurn();
		if (action == Action.BUILD_ROAD) {
			if (player.buildRoad(edge)) {
				renderer.setAction(Action.NONE);

				if (board.isSetupRoadOrShip()) {
					board.nextPhase();
					showState(true);
				}
				//TODO: special progress card shit?
//				else if (board.isProgressPhase()) {
//					board.nextPhase();
//
//					boolean canBuild = false;
//					for (Edge other : board.getEdges()) {
//						if (other.canBuildRoad(player))
//						{
//							canBuild = true;
//						}
//					}
//
//					if (!canBuild) {
//						board.nextPhase();
//						cantAct(Action.BUILD_ROAD);
//					}
//
//					showState(false);
//				}
				else {toast("Played the Saboteur");
					showState(false);
				}
			}
		}
		else if (action == Action.BUILD_SHIP) {
			if (player.buildShip(edge)) {
				renderer.setAction(Action.NONE);

				if (board.isSetupRoadOrShip()) {
					board.nextPhase();
					showState(true);
				}
				//TODO: special progress card shit?
//				else if (board.isProgressPhase()) {
//					board.nextPhase();
//
//					boolean canBuild = false;
//					for (Edge other : board.getEdges()) {
//						if (other.canBuildShip(player))
//						{
//							canBuild = true;
//						}
//					}
//
//					if (!canBuild) {
//						board.nextPhase();
//						cantAct(Action.BUILD_SHIP);
//					}
//
//					showState(false);
//				}
				else {
					showState(false);
				}
			}
		}
		else if (action == Action.MOVE_SHIP_1) {
			if (player.removeShipFrom(edge)) {
				renderer.setAction(Action.MOVE_SHIP_2);
				showState(true);
			}
		}
		else if (action == Action.MOVE_SHIP_2) {
            if (player.moveShipTo(edge)) {
				board.nextPhase();
                renderer.setAction(Action.NONE);
                showState(true);
            }
        }
	}


	public boolean buttonPress(UIButton.ButtonType button) {
		boolean canAct = false;
		Player player = board.getPlayerOfCurrentGameTurn();
		
		switch (button) {
			case PLAYER_STATUS:
				//PLAYER_STATUS IS THE BUTTON THAT IS ALWAYS VISIBLE IN TOP LEFT CORNER
				Log.d("playerStatus", "about to launch PLAYER PLAYER_STATUS");

				PlayerStatsFragment playerStatsFragment = new PlayerStatsFragment();
				playerStatsFragment.setBoard(board);
				playerStatsFragment.setMyPlayerId(myParticipantId);

				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.fragment_container, playerStatsFragment, playerStatsFragment.getClass().getSimpleName());
				fragmentTransaction.addToBackStack(playerStatsFragment.getClass().getSimpleName());
				fragmentTransaction.commit();
				break;

			case VIEW_BARBARIANS:
				AlertDialog.Builder alertadd = new AlertDialog.Builder(getActivity());
				LayoutInflater factory = LayoutInflater.from(getActivity());
				final View view = factory.inflate(R.layout.barbarian_map, null);
				switch(board.getBarbarianPosition()){
					case 0:
						view.findViewById(R.id.barbarian0).setVisibility(View.VISIBLE);
						break;
					case 1:
						view.findViewById(R.id.barbarian1).setVisibility(View.VISIBLE);
						break;
					case 2:
						view.findViewById(R.id.barbarian2).setVisibility(View.VISIBLE);
						break;
					case 3:
						view.findViewById(R.id.barbarian3).setVisibility(View.VISIBLE);
						break;
					case 4:
						view.findViewById(R.id.barbarian4).setVisibility(View.VISIBLE);
						break;
					case 5:
						view.findViewById(R.id.barbarian5).setVisibility(View.VISIBLE);
						break;
					case 6:
						view.findViewById(R.id.barbarian6).setVisibility(View.VISIBLE);
						break;
				}
				alertadd.setView(view);

				alertadd.show();
				break;

			case DICE_ROLL:

				// enter build phase
				board.nextPhase();

				if(!playedIsAlchemist) {
					roll1 = (int) (Math.random() * 6) + 1;
					roll2 = (int) (Math.random() * 6) + 1;
				} else {
					playedIsAlchemist = false;
				}
				int event = (int) (
						Math.random() * 6) + 1;
				int roll = roll1 + roll2;
				board.getPlayerOfCurrentGameTurn().rollDice(roll1, roll2 , event);
				if(!board.isMyPseudoTurn()){
					mListener.endTurn(board.getPlayerOfCurrentGameTurn().getGooglePlayParticipantId(), false);
				}

				//give free resource if they have AQUEDUCT and didnt get any resources and not 7
				if(player.getCityImprovementLevels()[CityImprovement.toCityImprovementIndex(CityImprovement.CityImprovementType.SCIENCE)] >= 3
						&& player.gotResourcesSinceLastTurn == false && roll != 7){

					CharSequence[] items = new CharSequence[5];
					items[0] = "Lumber";
					items[1] = "Wool";
					items[2] = "Grain";
					items[3] = "Brick";
					items[4] = "Ore";

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Aqueduct: Choose your free resource!");
					builder.setItems(items,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							board.getPlayerOfCurrentGameTurn().addResources(Resource.RESOURCE_TYPES[item], 1, false);
						}
					});

					AlertDialog chooseFreeResourceDialog = builder.create();
					chooseFreeResourceDialog.setCancelable(false);
					chooseFreeResourceDialog.show();
				}

				if (roll == 7) {
					if(!board.isMyPseudoTurn()){
						toast(getString(R.string.game_rolled_str) + " 7 " + ROLL_STRINGS[roll1]
								+ ROLL_STRINGS[roll2] + " "
								+ getString(R.string.game_chooseRobberPirate_title));
						showState(true);
						break;
					} else{
						getActivity().setTitle("Pending Players to Discard");
					}
				} else {
					toast(getString(R.string.game_rolled_str) + " " + roll + " "
							+ ROLL_STRINGS[roll1] + " " + ROLL_STRINGS[roll2] + " " + EVENT_ROLL_STRINGS[event]);
				}

				showState(false);
				break;

			case  BUILD_ROAD:
				canAct = player.canBuildSomeRoad();

				if (!canAct) {
					cantAct(Action.BUILD_ROAD);
					break;
				}

				if (player.getNumRoads() >= Player.MAX_ROADS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_road_max));
					break;
				}

				renderer.setAction(Action.BUILD_ROAD);
				setButtons(Action.BUILD_ROAD);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_road));
				break;

			case  BUILD_SHIP:
				canAct = player.canBuildSomeShip();

				if (!canAct) {
					cantAct(Action.BUILD_SHIP);
					break;
				}

				if (player.getNumShips() >= Player.MAX_SHIPS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_ship_max));
					break;
				}

				renderer.setAction(Action.BUILD_SHIP);
				setButtons(Action.BUILD_SHIP);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_ship));

				break;

			case  MOVE_SHIP:
				canAct = player.canMoveSomeShip();

				if (!canAct) {
					cantAct(Action.MOVE_SHIP_1);
					break;
				}

				renderer.setAction(Action.MOVE_SHIP_1);
				setButtons(Action.MOVE_SHIP_1);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_move_ship));
				break;

			case BUILD_SETTLEMENT:
				canAct = player.canBuildSomeSettlement();

				if (!canAct) {
					cantAct(Action.BUILD_SETTLEMENT);
					break;
				}

				if (board.getPlayerOfCurrentGameTurn().getNumOwnedSettlements() >= Player.MAX_SETTLEMENTS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_settlements_max));
					break;
				}

				renderer.setAction(Action.BUILD_SETTLEMENT);
				setButtons(Action.BUILD_SETTLEMENT);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_settlement));
				break;

			case BUILD_CITY:
				canAct = player.canBuildSomeCity();

				if (!canAct) {
					cantAct(Action.BUILD_CITY);
					break;
				}

				if (board.getPlayerOfCurrentGameTurn().getNumOwnedCities() >= Player.MAX_CITIES) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_city_max));
					break;
				}

				renderer.setAction(Action.BUILD_CITY);
				setButtons(Action.BUILD_CITY);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						 + getActivity().getString(R.string.game_build_city));
				break;

			case PLAY_PROGRESS_CARD:
				if (board.isProduction()) {
					progressCardDialog(true);
				} else {
					progressCardDialog(false);
				}

				break;

			case BUILD_CITY_WALL:
				canAct = player.canBuildSomeCityWall();

				if (!canAct) {
					cantAct(Action.BUILD_CITY_WALL);
					break;
				}

				if (board.getPlayerOfCurrentGameTurn().getNumOwnedCityWalls() >= Player.MAX_CITY_WALLS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_wall_max));
					break;
				}

				renderer.setAction(Action.BUILD_CITY_WALL);
				setButtons(Action.BUILD_CITY_WALL);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_wall));
				break;

			case HIRE_KNIGHT:
				canAct = player.canHireSomeKnight();

				if (!canAct) {
					cantAct(Action.HIRE_KNIGHT);
					break;
				}

				if (player.getNumOwnedBasicKnights() >= Player.MAX_BASIC_KNIGHTS
						|| player.getTotalNumOwnedKnights() >= Player.MAX_TOTAL_KNIGHTS) {
					popup(getString(R.string.game_cant_hire_knight_str),
							getString(R.string.game_basic_knight_max));
					break;
				}

				renderer.setAction(Action.HIRE_KNIGHT);
				setButtons(Action.HIRE_KNIGHT);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_hire_knight));
				break;

			case ACTIVATE_KNIGHT:
				canAct = player.canActivateSomeKnight();

				if (!canAct) {
					cantAct(Action.ACTIVATE_KNIGHT);
					break;
				}

				renderer.setAction(Action.ACTIVATE_KNIGHT);
				setButtons(Action.ACTIVATE_KNIGHT);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_activate_knight));
				break;

			case PROMOTE_KNIGHT:
				canAct = player.canPromoteSomeKnight();

				if (!canAct) {
					cantAct(Action.PROMOTE_KNIGHT);
					break;
				}

				int numOwnedBasicKnights = player.getNumOwnedBasicKnights(),
						numOwnedStrongKnights = player.getNumOwnedStrongKnights(),
						numOwnedMightyKnights = player.getNumOwnedMightyKnights(),
						totalNumOwnedKnights = player.getTotalNumOwnedKnights();
				if (numOwnedBasicKnights > Player.MAX_BASIC_KNIGHTS
						|| numOwnedStrongKnights > Player.MAX_STRONG_KNIGHTS
						|| numOwnedMightyKnights  > Player.MAX_MIGHTY_KNIGHTS
						|| totalNumOwnedKnights >= Player.MAX_TOTAL_KNIGHTS) {

					popup(getString(R.string.game_cant_promote_knight_str),
							getString(R.string.game_total_knights_max));
					break;
				}

				renderer.setAction(Action.PROMOTE_KNIGHT);
				setButtons(Action.PROMOTE_KNIGHT);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_promote_knight));
				break;

			case CHASE_ROBBER:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canChaseRobberFromHere(player)) {
						canAct = true;
						break;
					}
				}

				if (!canAct) {
					cantAct(Action.CHASE_ROBBER);
					break;
				}

                renderer.setAction(Action.CHASE_ROBBER);
                setButtons(Action.CHASE_ROBBER);
                getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
                        + getActivity().getString(R.string.game_chase_robber_title));
//				confirmChaseRobberDialog();
				break;

			case CHASE_PIRATE:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canChasePirateFromHere(player)) {
						canAct = true;
						break;
					}
				}

				if (!canAct) {
					cantAct(Action.CHASE_PIRATE);
					break;
				}
                renderer.setAction(Action.CHASE_PIRATE);
                setButtons(Action.CHASE_PIRATE);
                getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
                        + getActivity().getString(R.string.game_chase_pirate_title));
//				confirmChasePirateDialog();
				break;

			case  MOVE_KNIGHT:
				for (Vertex vertex : board.getVertices()) {
					// ensure that removed knight would have >= 1 target movement
					if (vertex.canRemoveKnightFromHere(player))
					{
						canAct = true;
						break;
					}
				}

				if (!canAct) {
					cantAct(Action.MOVE_KNIGHT_1);
					break;
				}

				renderer.setAction(Action.MOVE_KNIGHT_1);
				setButtons(Action.MOVE_KNIGHT_1);
				getActivity().setTitle(board.getPlayerOfCurrentGameTurn().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_move_knight));
				break;

			case PURCHASE_CITY_IMPROVEMENT:
                FragmentManager cityImprovementFragmentManager = getActivity().getSupportFragmentManager();
                CityImprovementFragment cityImprovementFragment = new CityImprovementFragment();
                cityImprovementFragment.setBoard(board);
                cityImprovementFragment.setActiveGameFragment(this);

                FragmentTransaction cityImprovementFragmentTransaction =  cityImprovementFragmentManager.beginTransaction();
                cityImprovementFragmentTransaction.replace(R.id.fragment_container, cityImprovementFragment,cityImprovementFragment.getClass().getSimpleName());
                cityImprovementFragmentTransaction.addToBackStack(cityImprovementFragment.getClass().getSimpleName());
                cityImprovementFragmentTransaction.commit();

				break;

			case USE_FISH:
				FragmentManager fishFragmentManager = getActivity().getSupportFragmentManager();
				UseFishFragment fishFragment = new UseFishFragment();
				fishFragment.setBoard(board);
				fishFragment.setActiveGameFragment(this);

				FragmentTransaction fishFragmentTransaction =  fishFragmentManager.beginTransaction();
				fishFragmentTransaction.replace(R.id.fragment_container, fishFragment,fishFragment.getClass().getSimpleName());
				fishFragmentTransaction.addToBackStack(fishFragment.getClass().getSimpleName());
				fishFragmentTransaction.commit();

				break;

			case TRADE:

				FragmentManager tradeFragmentManager = getActivity().getSupportFragmentManager();
				TradeRequestFragment tradeFragment = new TradeRequestFragment();
				tradeFragment.setBoard(board);
                tradeFragment.setActiveGameFragment(this);

				FragmentTransaction tradeFragmentTransaction =  tradeFragmentManager.beginTransaction();
				tradeFragmentTransaction.replace(R.id.fragment_container, tradeFragment,tradeFragment.getClass().getSimpleName());
				tradeFragmentTransaction.addToBackStack(tradeFragment.getClass().getSimpleName());
				tradeFragmentTransaction.commit();

				showState(false);

				break;

			case END_TURN:
				if(board.getIsMerchantFleetActive()
						&& player.getIsMerchantFleetActive()) {
					board.setIsMerchantFleetActive(false);
					player.setIsMerchantFleetActive(false);
				}
				player.gotResourcesSinceLastTurn = false;
				board.nextPhase();
				showState(true);
				break;

			case CANCEL_ACTION:
				// return false if there is nothing to cancel
				boolean result = renderer.cancel();

				if (renderer.getAction() == Action.MOVE_SHIP_2) {
					board.cancelMovingShipPhase();
				}
				else if (renderer.getAction() == Action.MOVE_KNIGHT_2) {
					board.cancelMovingKnightPhase();
				}

				//TODO: could this be moved?
				// sets renderer action to cancel
				showState(false);
				return result;
			}

		return true;
	}

	public boolean clickResource(int index) {
		if (!board.getPlayerOfCurrentGameTurn().isHuman() || !board.isPlayerTurnPhase())
			return false;


		Bundle bundle = new Bundle();
		bundle.putInt(TradeRequestFragment.TYPE_KEY, index);
		TradeRequestFragment tradeFragment = new TradeRequestFragment();
		tradeFragment.setBoard(board);
		tradeFragment.setArguments(bundle);

		FragmentTransaction tradeFragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
		tradeFragmentTransaction.replace(R.id.fragment_container, tradeFragment,tradeFragment.getClass().getSimpleName());
		tradeFragmentTransaction.addToBackStack(tradeFragment.getClass().getSimpleName());
		tradeFragmentTransaction.commit();

		return true;
	}

	public void updateBoardState(){
		board.reinitBoardOnDependents();
		view.setBoard(board);
		board.setActiveGameFragment(this);
		showState(true);
	}

	//setup()
	private void showState(boolean setZoom) {
		boolean executingPsuedoTurnAction =
				(board.isMyPseudoTurn() && renderer.isPsuedoTurnAction());
		Player player;
		if (executingPsuedoTurnAction) {
			player = view.getActivePlayer();
		} else {
			player = board.getPlayerOfCurrentGameTurn();
		}

		// WARNING: only sets the active player on renderer if they are playing their game turn
		renderer.setState(board, (player.isHuman() && board.itsMyTurn(myParticipantId) &&
				!board.isMyPseudoTurn())  ? player : null, texture, board.getLastDiceRollNumber());

		if (setZoom)
		{
			renderer.getGeometry().zoomOut();
		}

		if (board.isChooseRobberPiratePhase() && !board.isMyPseudoTurn()) {
			chooseRobberPirate();
		}
		else if (board.isRobberPhase() && board.getCurRobberHex() != null){
            rob();
        }
        else if (board.isPiratePhase() && board.getCurPirateHex() != null) {
			pirate();
		}

		// display winner
		boolean hadWinner = board.getWinner() != null;
		Player winner = board.getWinner();
		if (!hadWinner && winner != null) {
			// declare winner
			final Builder infoDialog = new AlertDialog.Builder(getActivity());
			infoDialog.setTitle(getString(R.string.phase_game_over));
			infoDialog.setIcon(R.drawable.logo);
			infoDialog.setMessage(winner.getPlayerName() + " "
					+ getString(R.string.game_won));
			infoDialog.setNeutralButton(getString(R.string.game_back_to_board),
					null);
			infoDialog.setPositiveButton(getString(R.string.game_return_to_menu),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//ActiveGameFragment.this.finish();
							// must ask the activity to close this StartScreenActivity Fragment
							getActivity().getSupportFragmentManager().popBackStack();
						}
					});
			infoDialog.show();
		}

		Action action = Action.NONE;
		if (board.isSetupSettlement())
		{
			action = Action.BUILD_SETTLEMENT;
			if(!showedPlayerGameCreationStats) {
				showedPlayerGameCreationStats = true;
				popup("Current Game", "Number players: " + board.getNumPlayers() + "\n" +
						"VP to win: " + board.getMaxPoints() + "\n\nTo reject, quit game and reject invite.");
			}

		}
		else if (board.isSetupCity())
		{
			action = Action.BUILD_CITY;
		}
		else if (board.isSetupRoadOrShip() || board.isProgressPhase())
		{ //TODO: does progress phase matter?
			action = Action.BUILD_EDGE_UNIT;
		}
		else if (board.isChooseRobberPiratePhase()) {
			action = Action.CHOOSE_ROBBER_PIRATE;
		}
		else if (board.isRobberPhase() && board.getCurRobberHex() == null)
		{
			action = Action.MOVE_ROBBER;
		}
		else if (board.isPiratePhase() && board.getCurPirateHex() == null)
		{
			action = Action.MOVE_PIRATE;
		}
		else if (board.isMovingShipPhase()) {
			action = Action.MOVE_SHIP_2;
		}
		else if (board.isMovingKnightPhase()) {
			action = Action.MOVE_KNIGHT_2;
		}
		else if (board.isKnightDisplacementPhase()) {
			action = Action.MOVE_DISPLACED_KNIGHT;
		}
		else if(board.isBuildMetropolisPhase()){
			action = Action.BUILD_METROPOLIS;
		}
        else if(board.isPlaceMerchantPhase()){
            action = Action.PLACE_MERCHANT;
        }

		renderer.setAction(action);
		setButtons(action);

		getActivity().setTitleColor(Color.WHITE);

		int color = TextureManager.getColor(player.getColor());
		color = TextureManager.darken(color, 0.35);
		ActionBar actionBar = getActivity().getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(color));

		//Add check if its actually our turn and
		int resourceId = board.getPhaseResource();
		if (resourceId != 0)
			if((board.itsMyTurn(myParticipantId) && !board.isMyPseudoTurn())
					|| (board.isMyPseudoTurn() && renderer.isPsuedoTurnAction())
					|| myTurnInterrupted()) {
				getActivity().setTitle(player.getPlayerName() + ": " + getActivity().getString(resourceId));
			} else{
				getActivity().setTitle(R.string.not_my_turn);
			}
		else
		{
			getActivity().setTitle(player.getPlayerName());
		}

		if(board.getTradeProposal() != null && board.isMyPseudoTurn() && board.isTradeProposedPhase()){
            //We are in a phase of trade somehow
            //check which phase we are in based on current Phase
            // show trade proposal if its my turn to see the proposal
			proposeTrade();
//            String currentPlayerToProposeParticipantId = board.getPlayerById(board.getTradeProposal().getCurrentPlayerToProposeId()).getGooglePlayParticipantId();
//            if (board.isTradeProposedPhase() && currentPlayerToProposeParticipantId.equals(myParticipantId)){
//                proposeTrade();
//            } else if(board.isTradeRespondedPhase() && board.getTradeProposal().getTradeCreatorPlayerId() == board.getPlayerOfCurrentGameTurn().getPlayerNumber()){
//				resultsTrade();
//			}
        } else if(board.isTradeRespondedPhase()
				&& board.getPlayerById(board.getTradeProposal().getTradeCreatorPlayerId()).getGooglePlayParticipantId().equals(myParticipantId)){
			resultsTrade();
		}

		if(!board.isMyPseudoTurn() && player.getFreeBuild()){
			switch(player.getFreeBuildUnit()){
				case 0:
					buttonPress(ButtonType.BUILD_SHIP);
					break;
				case 1:
					buttonPress(ButtonType.BUILD_ROAD);
					break;
				default:

			}
		}

		Log.d("myTag", "end of showState");
	}

	private boolean myTurnInterrupted(){
		if(board.isTradeProposedPhase()){
			return  board.getPlayerById(board.getTradeProposal().getTradeCreatorPlayerId()).getGooglePlayParticipantId().equals(myParticipantId);
		}
		return false;
	}

	private void setButtons(Action action) {
		view.removeButtons();

		view.addButton(ButtonType.PLAYER_STATUS);
		view.addButton(ButtonType.VIEW_BARBARIANS);

		Player player = board.getPlayerOfCurrentGameTurn();
		Player winner = board.getWinner();
		if (winner != null || !player.isHuman() || !board.itsMyTurn(myParticipantId)){
			// anonymous mode
		} else if (board.isSetupPhase()) {
			// no extra buttons in showState phase
		} else if (board.isProgressPhase()) {
			// TODO: add ability to cancel card use
			// consider what happens if there's nowhere to build a road
		} else if (board.isChooseRobberPiratePhase() ||
				board.isRobberPhase() || board.isPiratePhase()) {
			// do nothing
        } else if (board.isPlaceMerchantPhase()) {
            // do nothing
		} else if (action != Action.NONE && action != Action.BUILD_METROPOLIS
				&& action != Action.MOVE_DISPLACED_KNIGHT) {
			// cancel the action
			view.addButton(ButtonType.CANCEL_ACTION);
		} else if (board.isProduction()) {
			view.addButton(ButtonType.DICE_ROLL);

			final Player me = board.getPlayerOfCurrentGameTurn();
			final Vector<ProgressCard.ProgressCardType> cards = me.getHand();
			String cardType = "";
			for (int i = 0; i < cards.size(); i++) {
				ProgressCard.ProgressCardType type = cards.get(i);
				cardType = getString(ProgressCard.getCardStringResource(type));

				if (cardType.equals(getString(R.string.alchemist))) {
					view.addButton(ButtonType.PLAY_PROGRESS_CARD);
					break;
				}
			}
		} else if (board.isPlayerTurnPhase()) {
			view.addButton(ButtonType.TRADE);
			view.addButton(UIButton.ButtonType.END_TURN);

            if (!player.getHand().isEmpty())
            {
                view.addButton(UIButton.ButtonType.PLAY_PROGRESS_CARD);
            }

			if (player.canBuildSomeRoad())
            {
                view.addButton(UIButton.ButtonType.BUILD_ROAD);
            }

            if (player.canBuildSomeShip())
            {
				view.addButton(UIButton.ButtonType.BUILD_SHIP);
			}

			if(player.canMoveSomeShip()) {
				view.addButton(UIButton.ButtonType.MOVE_SHIP);
			}

			if (player.canBuildSomeSettlement())
            {
                view.addButton(UIButton.ButtonType.BUILD_SETTLEMENT);
            }

			if (player.canBuildSomeCity())
            {
                view.addButton(UIButton.ButtonType.BUILD_CITY);
            }
            if (player.canBuildSomeCityWall())
            {
				view.addButton(ButtonType.BUILD_CITY_WALL);
			}
			if(player.canHireSomeKnight()) {
				view.addButton(ButtonType.HIRE_KNIGHT);
			}
			if(player.canActivateSomeKnight()) {
				view.addButton(ButtonType.ACTIVATE_KNIGHT);
			}
			if(player.canPromoteSomeKnight()) {
				view.addButton(ButtonType.PROMOTE_KNIGHT);
			}
			if(player.canChaseRobber()) {
				view.addButton(ButtonType.CHASE_ROBBER);
			}
			if(player.canChasePirate()) {
				view.addButton(ButtonType.CHASE_PIRATE);
			}
			if(player.canMoveSomeKnight()) {
				view.addButton(ButtonType.MOVE_KNIGHT);
			}
			if(player.getNumOwnedFish() > 0){
				view.addButton(ButtonType.USE_FISH);
			}
			view.addButton(ButtonType.PURCHASE_CITY_IMPROVEMENT);
		}
	}

	private void toast(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG)
				.show();
	}

	private void popup(String title, String message) {
		final Builder infoDialog = new AlertDialog.Builder(getActivity());
		infoDialog.setTitle(title);
		infoDialog.setIcon(R.drawable.logo);
		infoDialog.setMessage(message);
		infoDialog.setNeutralButton(getString(R.string.game_ok_str), null);
		infoDialog.show();
	}

	private void notifyTurn() {
		// vibrate if enabled
		Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(400);

		// show turn log
		if (board.isProduction() && isActive)
			turnLog();
	}

	private void turnLog() {
		String message = "";

		// show log of the other players' turns
		int offset = board.getPlayerOfCurrentGameTurn().getPlayerNumber() + 1;
		for (int i = offset; i < offset + board.getNumPlayers()-1; i++) {
			// don't include players after you on your first turn
			if (board.getGameRoundNumber() == 1 && (i % board.getNumPlayers()) >= offset)
			{
				continue;
			}

			Player player = board.getPlayerById(i % board.getNumPlayers());
			String name = player.getPlayerName()
					+ " ("
					+ getActivity().getString(Player
							.getColorStringResource(player.getColor())) + ")";
			String log = player.getActionLog();

			if (message != "")
			{
				message += "\n";
			}

			if (log == null || log == "")
			{
				message += name + " " + getString(R.string.game_did_nothing_str)
						+ "\n";
			}
			else
			{
				message += name + "\n" + log + "\n";
			}
		}

		if (message != "")
		{
			popup(getString(R.string.game_turn_summary), message);
		}
	}

    private void proposeTrade() {
        if (!board.isTradeProposedPhase()) {
            Log.w(getActivity().getClass().getName(),
                    "shouldn't be calling trade() out of trade phase");
            return;
        }

        if (board.getTradeProposal() == null) {
            Log.w(getActivity().getClass().getName(),
                    "shouldn't be calling trade() without trade proposal set");
            return;
        }

        TradeProposedFragment tradeProposedFragment = new TradeProposedFragment();
        tradeProposedFragment.setBoard(board);
		tradeProposedFragment.setActiveGameFragment(this);

        FragmentTransaction tradeFragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
        tradeFragmentTransaction.replace(R.id.fragment_container, tradeProposedFragment,"PROPOSE_TRADE");
        tradeFragmentTransaction.addToBackStack("PROPOSE_TRADE");
        tradeFragmentTransaction.commit();

    }

	private void resultsTrade() {

		final Player tradeCreator = board.getPlayerById(board.getTradeProposal().getTradeCreatorPlayerId());
		final Player tradeProposed = board.getPlayerById(board.getTradeProposal().getCurrentPlayerToProposeId());
		final int[] counterOffer = board.getTradeProposal().getCounterOffer();
		final Resource.ResourceType resourceType = board.getTradeProposal().getTradeResource();


		TradeProposal tradeProposal = board.getTradeProposal();
		if (!board.isTradeRespondedPhase()) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling trade() out of trade phase");
			return;
		}

		if (tradeProposal == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling trade() without trade proposal set");
			return;
		}

		if(tradeProposal.getCounterOffer() == null && !tradeProposal.isTradeReplied()){
			//nobody accepted, show dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.waiting_trade_responded));
			builder.setMessage(R.string.waiting_trade_responded_nobody);

			AlertDialog tradeResultsDialog = builder.create();
			tradeResultsDialog.setCancelable(true);
			tradeResultsDialog.show();

			//reset the board to state before trade
			board.setTradeProposal(null);
			board.nextPhase();
			showState(true);

			return;
		} else if(tradeProposal.getCounterOffer() == null && tradeProposal.isTradeReplied()){
			//somebody accepted, show dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.waiting_trade_responded));
			builder.setMessage(tradeProposed.getPlayerName()
					+ " accepted your trade offer! \n\nGained 1 " + getString(Resource.toRString(tradeProposal.getTradeResource())));

			AlertDialog tradeResultsDialog = builder.create();
			tradeResultsDialog.setCancelable(true);
			tradeResultsDialog.show();

			//reset the board to state before trade
			board.setTradeProposal(null);
			board.nextPhase();
			showState(true);

			return;
		} else if(counterOffer != null && tradeProposal.isTradeReplied()){
			//somebody proposed counter offer
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			String newProposal = tradeProposed.getPlayerName() + " asks for:\n";
			for (int i = 0; i < counterOffer.length; i++) {
				if(counterOffer[i] > 0){
					newProposal += "(" + Integer.toString(counterOffer[i]) + ") " + getString(Resource.toRString(Resource.RESOURCE_TYPES[i])) + "\n";
				}
			}

			//check which message to present based on if enough resources
			if(!tradeCreator.canTradePlayer(counterOffer)){
				alertDialogBuilder.setMessage("A counter offer was created!\nYou ask for: " + getString(Resource.toRString(resourceType)) +"\n" + newProposal + "\nYou don't have enough resources!");

				alertDialogBuilder
						.setCancelable(false)
						.setNegativeButton("Close",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										board.nextPhase();
										showState(true);
									}
								});

			} else {
				alertDialogBuilder.setMessage("A counter offer was created!\nYou ask for: " + getString(Resource.toRString(resourceType)) +"\n" + newProposal);

				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("Accept",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										tradeCreator.trade(tradeProposed, resourceType, counterOffer);
										board.nextPhase();
										showState(true);
									}
								})
						.setNegativeButton("Reject",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										board.nextPhase();
										showState(true);
									}
								});

			}

			alertDialogBuilder.show();


		}


	}

	private void chooseRobberPirate() {
		if (!board.isChooseRobberPiratePhase()) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling chooseRobberPirate() out of choice phase");
			return;
		}
/*
		Hexagon robbing = board.getCurRobberHex();
		if (robbing == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling chooseRobberPirate() without robber location set");
			showState(false);
			return;
		}

		Hexagon pirating = board.getCurPirateHex();
		if (pirating == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling chooseRobberPirate() without pirate location set");
			showState(false);
			return;
		}
*/
		CharSequence[] items = new CharSequence[2];
		items[0] = getString(R.string.game_choose_robber);
		items[1] = getString(R.string.game_choose_pirate);


		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.game_chooseRobberPirate_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				chooseRobberPirate(item);
			}
		});

		AlertDialog chooseRobberPirateDialog = builder.create();
		chooseRobberPirateDialog.setCancelable(false);
		chooseRobberPirateDialog.show();
	}

	private void chooseRobberPirate(int choice) {
		if (!board.isChooseRobberPiratePhase()) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling chooseRobberPirate() out of choice phase");
			return;
		}

		Hexagon robbing = board.getCurRobberHex();
		if (robbing == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling rob() without robber location set");
			return;
		}

		Hexagon pirating = board.getCurPirateHex();
		if (pirating == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling chooseRobberPirate() without pirate location set");
			return;
		}

		if (choice == 0) {// Chose Robber
			board.nextPhase(0);
		}
		else { // Chose Pirate
			board.nextPhase(1);
		}
		showState(false);
	}

	private void rob() {
		if (!board.isRobberPhase()) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling rob() out of robber phase");
			return;
		}

		Hexagon targetHex = board.getCurRobberHex();
		if (targetHex == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling rob() without robber location set");
			showState(false);
			return;
		}

		steal(targetHex);
	}

	private void pirate() {
		if (!board.isPiratePhase()) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling pirate() out of pirate phase");
			return;
		}

		Hexagon targetHex = board.getCurPirateHex();
		if (targetHex == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling pirate() without pirate location set");
			showState(false);
			return;
		}

		steal(targetHex);
	}

	private void steal(Hexagon target) {

		boolean stealFromShipsOnly = board.isPiratePhase();
		Player current = board.getPlayerOfCurrentGameTurn();

		CharSequence[] list = new CharSequence[3];
		int index = 0;

		Player player = null;
		boolean somethingToStealFrom = false;
		for (int i = 0; i < board.getNumPlayers(); i++) {
			somethingToStealFrom = false;
			player = board.getPlayerById(i);

			if (player == current)
			{
				// don't steal from ourselves...
				continue;
			}
			else {
				somethingToStealFrom = stealFromShipsOnly ? target.adjacentToShipOwnedBy(player)
						: target.adjacentToVertexUnitOwnedBy(player);
				if(!somethingToStealFrom) {
					// nothing to steal from
					continue;
				}
			}

			// add to list of players to rob from
			int count = player.getResourceCount();
			list[index++] = getString(R.string.game_steal_from_str) + " "
					+ player.getPlayerName() + " (" + count + " "
					+ getString(R.string.game_resources_str) + ")";
		}

		if (index == 0) {
			// nobody to rob from
			toast(getString(R.string.game_steal_fail_str));
			board.nextPhase();
			mListener.endTurn(board.getPlayerOfCurrentGameTurn().getGooglePlayParticipantId(), false);
			showState(false);
			return;
		} else if (index == 1) {
			// automatically rob if only one player is listed
			steal(0);
			return;
		}

		// create new list that is the right size
		CharSequence[] items = new CharSequence[index];
		for (int i = 0; i < index; i++)
		{
			items[i] = list[i];
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.game_steal_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				steal(item);
			}
		});

		AlertDialog stealDialog = builder.create();
		stealDialog.setCancelable(false);
		stealDialog.show();

	}

	private void steal(int victim) {
		Player current = board.getPlayerOfCurrentGameTurn();
		boolean stealFromShipsOnly = board.isPiratePhase();
		Hexagon target = null;
		if(board.isRobberPhase()) {
			target = board.getCurRobberHex();
		}
		else if (board.isPiratePhase()) {
			target = board.getCurPirateHex();
		}
		else {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling steal() out of pirate or robber phase");
			return;
		}
		int index = 0;
		for (int i = 0; i < board.getNumPlayers(); i++) {
			Player player = board.getPlayerById(i);
			boolean somethingToStealFrom = false;
			if (player == current)
			{
				// don't steal from ourselves...
				continue;
			}
			else {
				somethingToStealFrom = stealFromShipsOnly ? target.adjacentToShipOwnedBy(player)
						: target.adjacentToVertexUnitOwnedBy(player);
				if(!somethingToStealFrom) {
					// nothing to steal from
					continue;
				}
			}

			if (index == victim) {
				Resource.ResourceType resourceType = board.getPlayerOfCurrentGameTurn().steal(player);

				if (resourceType != null)
				{
					toast(getString(R.string.game_stole_str) + " "
							+ getActivity().getString(Resource.toRString(resourceType))
							+ " " + getString(R.string.game_from_str) + " "
							+ player.getPlayerName());
				}
				else
				{
					toast(getString(R.string.game_player_couldnt_steal) + " "
							+ player.getPlayerName());
				}

				board.nextPhase();
				mListener.endTurn(board.getPlayerOfCurrentGameTurn().getGooglePlayParticipantId(), false);
				showState(false);
				return;
			}

			index++;
		}

	}

	private void cantAct(Action action) {
		Player player = board.getPlayerOfCurrentGameTurn();

		String message = "";
		switch (action) {
			case BUILD_EDGE_UNIT:

				if (player.getNumRoads() + player.getNumShips()
						== (Player.MAX_ROADS + Player.MAX_SHIPS))
				{
					message = getActivity().getString(R.string.game_build_edge_unit_max);
				}
				else
				{
					message = getString(R.string.game_nowhere_available_edge_unit_build);
				}

				if (board.isProgressPhase1()) {
					//TODO: is cost affected by progress card?
				} else if (board.isProgressPhase2()) {
					//TODO: is  cost affected by progress card?
				}

				break;
			case BUILD_ROAD:

				if (player.getNumRoads() == Player.MAX_ROADS)
				{
					message = getActivity().getString(R.string.game_build_road_max);
				}
				else
				{
					message = getString(R.string.game_nowhere_available_road_build);
				}

				if (board.isProgressPhase1()) {
					//TODO: is road cost affected by progress card?
				} else if (board.isProgressPhase2()) {
					//TODO: is road cost affected by progress card?
				}

				break;
			case BUILD_SHIP:

				if (player.getNumShips() == Player.MAX_SHIPS)
				{
					message = getActivity().getString(R.string.game_build_ship_max);
				}
				else
				{
					message = getString(R.string.game_nowhere_available_ship_build);
				}

				if (board.isProgressPhase1()) {
					//TODO: is road cost affected by progress card?
				} else if (board.isProgressPhase2()) {
					//TODO: is road cost affected by progress card?
				}

				break;

			case MOVE_SHIP_1:
			case MOVE_SHIP_2:

				message = getString(R.string.game_nowhere_available_ship_move);

				break;

			case BUILD_SETTLEMENT:
				if (player.getNumOwnedSettlements() == Player.MAX_SETTLEMENTS)
				{
					message = getString(R.string.game_build_settlements_max);
				}
				else
				{
					message = getString(R.string.game_nowhere_available_settlement);
				}

				break;

			case BUILD_CITY:
				if (player.getNumOwnedCities() == Player.MAX_CITIES)
					message = getString(R.string.game_build_city_max);
				else
					message = getString(R.string.game_nowhere_available_city);

				break;

			case BUILD_CITY_WALL:
				if (player.getNumOwnedCityWalls() == Player.MAX_CITY_WALLS)
				{
					message = getString(R.string.game_build_wall_max);
				}
				else
				{
					message = getString(R.string.game_nowhere_available_city_wall);
				}
				break;

			case HIRE_KNIGHT:
				if (player.getNumOwnedBasicKnights() == Player.MAX_BASIC_KNIGHTS)
				{
					message = getString(R.string.game_basic_knight_max);
					if (player.getTotalNumOwnedKnights() == Player.MAX_TOTAL_KNIGHTS) {
						message += "\n" + getString(R.string.game_total_knights_max);
					}
				}
				else
				{
					message = getString(R.string.game_nowhere_available_hire_knight);
				}
				break;

			case ACTIVATE_KNIGHT:
					message = getString(R.string.game_nowhere_available_activate_knight);
				break;

			case PROMOTE_KNIGHT:
				int numOwnedBasicKnights = player.getNumOwnedBasicKnights(),
						numOwnedStrongKnights = player.getNumOwnedStrongKnights(),
						numOwnedMightyKnights = player.getNumOwnedMightyKnights(),
						totalNumOwnedKnights = player.getTotalNumOwnedKnights();
				if (numOwnedBasicKnights > Player.MAX_BASIC_KNIGHTS
						|| numOwnedStrongKnights > Player.MAX_STRONG_KNIGHTS
						|| numOwnedMightyKnights  > Player.MAX_MIGHTY_KNIGHTS
						|| totalNumOwnedKnights >= Player.MAX_TOTAL_KNIGHTS) {

					message = getString(R.string.game_total_knights_max);
					break;
				}
				else
				{
					message = getString(R.string.game_nowhere_available_promote_knight);
				}
				break;

			case CHASE_ROBBER:
				message = getString(R.string.game_nowhere_available_chase_robber);
				break;

			case CHASE_PIRATE:
				message = getString(R.string.game_nowhere_available_chase_pirate);
				break;

			case MOVE_KNIGHT_1:
			case MOVE_KNIGHT_2:

				message = getString(R.string.game_nowhere_available_knight_move);

				break;

			default:
				return;
		}

		popup(getString(R.string.game_cant_act_str), message);

		showState(false);
	}

	private void progressCardDialog(boolean hasAlchemist) {
		final Player me = board.getPlayerOfCurrentGameTurn();
		final Vector<ProgressCard.ProgressCardType> cards = me.getHand();

		CharSequence[] list = new CharSequence[cards.size()+1];
		int index = 0;
		if(hasAlchemist) {
			String cardType = "";
			for (int i = 0; i < cards.size(); i++) {
				ProgressCard.ProgressCardType type = cards.get(i);
				cardType = getString(ProgressCard.getCardStringResource(type));
				if(cardType.equals(getString(R.string.alchemist)));
				list[index++] = cardType;
			}
		} else {
			for (int i = 0; i < cards.size(); i++) {
				ProgressCard.ProgressCardType type = cards.get(i);

				list[index++] = getString(ProgressCard.getCardStringResource(type));
			}
		}

		list[index++] = getString(R.string.game_cancel_str);

		CharSequence[] items = new CharSequence[index];
		for (int i = 0; i < index; i++)
		{
			items[i] = list[i];
		}

		final int cancel = index-1;

		//create the popup asking which card to use
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.player_status_progress_cards));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (item == cancel) {
					dialog.dismiss();
				} else {
					dialog.dismiss();
					final ProgressCard.ProgressCardType card = cards.get(item);
					//open confirmation popup
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
					alertDialogBuilder.setMessage("Confirm using card");

					alertDialogBuilder
							.setCancelable(true)
							.setPositiveButton("Use",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int id) {
											//remove that card from the player & return to bottom of deck
											cards.remove(card);
											board.returnProgressCard(card);
											showState(false);
											//play the card
											switch (card) {
												case MERCHANT:
													playMerchant();
													break;
												case ALCHEMIST:
													playAlchemist();
													break;
												case BISHOP:
													playBishop();
													break;
												case INTRIGUE:
													playIntrigue();
													break;
												case SABOTEUR:
													playSaboteur();
													break;
												case SPY:
													playSpy();
													break;
												case COMMERCIAL_HARBOR:
													playCommercialHarbor();
													break;
												case MERCHANT_FLEET:
													playMerchantFleet();
													break;
												default:
													break;
											}
										}
									})
							.setNegativeButton("Close",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					alertDialogBuilder.create().show();
				}
			}
		});

		builder.create().show();
	}

	private void playMerchant(){
		//@TODO
		//add merchant placement logic

		toast("Played the merchant");
        getActivity().setTitle("Place Merchant");
        board.setPhase(Board.Phase.PLACE_MERCHANT);
		showState(true);
	}

	private void confirmDisplaceKnightDialog(Vertex vertex) {

		if (vertex.getCurUnitType() != Vertex.KNIGHT) {
			return;
		}

		final int confirm = 0;
		final int cancel = 1;
		CharSequence[] items = new CharSequence[2];
		items[0] = getString(R.string.game_confirm_displace_knight);
		items[1] = getString(R.string.game_cancel_str);

		final Vertex displacementTarget = vertex;
		//create the popup asking which card to use
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.game_confirm_displace_knight_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (item == cancel) {
					dialog.dismiss();
				} else if (item == confirm) {
					dialog.dismiss();
					if (board.getPlayerOfCurrentGameTurn().displaceKnightAt(displacementTarget)) {
						// finish moving the knight
						renderer.setAction(Action.NONE);
						showState(true);
					}
				}
			}
		});

		builder.create().show();
	}

	private void playAlchemist(){
		//@TODO
		//add merchant placement logic
		playedIsAlchemist = true;
		final CharSequence[] items = new CharSequence[6];
		for (int i = 0; i < 6; i++) {
			items[i] = Integer.toString(i+1);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Choose the number for Dice 1");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
				roll1 = item + 1;

				AlertDialog.Builder secondBuilder = new AlertDialog.Builder(getActivity());
				secondBuilder.setTitle("Choose the number for Dice 2");
				secondBuilder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						roll2 = item + 1;

						buttonPress(ButtonType.DICE_ROLL);

					}
				});
				secondBuilder.setCancelable(false);
				secondBuilder.create().show();
			}
		});

		builder.setCancelable(false);
		builder.create().show();


		toast("Played the Alchemist");
	}

	private void playBishop(){
		//@TODO
		//add bishop placement logic
		board.setReturnPhase(board.getPhase());
		board.startRobberPhase();
		showState(false);
		/*while(board.getCurRobberHex() == null) {

		}
		rob();*/
		showState(false);
		board.nextPhase();
		toast("Played the Bishop");
	}

	private void playIntrigue(){
		//@TODO
		Player player = board.getPlayerOfCurrentGameTurn();
		player.setIsIntrigue(true);
		//add merchant placement logic
		toast("Played the Intrigue");
	}

	@Override
	public String toString() {
		return super.toString();
	}

	private void playSaboteur(){
		//@TODO
		//add Saboteur placement logic
		board.setReturnPhase(board.getPhase());
		board.setPhase(Board.Phase.PROGRESS_CARD_STEP_2);
		Player currentPlayer = board.getPlayerOfCurrentGameTurn();
		Player opponentPlayer;
		int currentPlayerVictoryPoints = currentPlayer.getVictoryPoints();
		for (int i = 0; i < board.getNumPlayers(); i++) {
			opponentPlayer = board.getPlayerById(i);
			if (currentPlayer.getPlayerNumber() == opponentPlayer.getPlayerNumber()) {
				continue;
			} else {
				if (currentPlayerVictoryPoints <= opponentPlayer.getVictoryPoints()) {
					int cards = opponentPlayer.getResourceCount();
					int extra = cards / 2;
					if (board.getAutoDiscad()) {
						for (int j = 0; j < extra; j++) {
							opponentPlayer.discard(null);
						}
					} else if (opponentPlayer.isBot()) {
						// instruct the ai to discard_resources
						AutomatedPlayer bot = (AutomatedPlayer) opponentPlayer;
						bot.discard(extra);
					} else if (opponentPlayer.isHuman()) {
						// queue human players to discard_resources
						board.getPlayerIdsYetToAct().add(opponentPlayer.getPlayerNumber());
					}
				}
			}
		}

		toast("Played the Saboteur");

		// Something to consider about how to pass the turn around.
		if(!board.getPlayerIdsYetToAct().isEmpty()) {
			mListener.endTurn(board.getPlayerById(currentPlayer.getPlayerNumber()).getGooglePlayParticipantId(), false);
		}

		board.nextPhase();

	}

	private void playSpy(){
		//@TODO
		//add merchant placement logic

		final Player current = board.getPlayerOfCurrentGameTurn();
		List<String> playerList = new ArrayList<>();

		final Map<CharSequence, Integer> playerNameToPlayerId = new HashMap<>();


		for (int i = 0; i< board.getNumPlayers(); i++) {
			Player player = board.getPlayerById(i);
			if (player == current) {
				continue;
			}

			Vector<ProgressCard.ProgressCardType> cards = player.getHand();
			if (cards.size() == 0) {
				continue;
			}
			playerList.add(player.getPlayerName());
			playerNameToPlayerId.put(player.getPlayerName(), player.getPlayerNumber());
		}

		final CharSequence[] playerListToShow = new CharSequence[playerList.size()];
		for (int i = 0; i < playerList.size(); i++) {
			playerListToShow[i] = playerList.get(i);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Player");
		builder.setItems(playerListToShow, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();

				final Player opponentPlayer = board.getPlayerById(playerNameToPlayerId.get(playerListToShow[item]));

				final Vector<ProgressCard.ProgressCardType> cards = opponentPlayer.getHand();

				CharSequence[] list = new CharSequence[cards.size()];
				int index = 0;

				for (int i = 0; i < cards.size(); i++) {
					ProgressCard.ProgressCardType type = cards.get(i);

					list[index++] = getString(ProgressCard.getCardStringResource(type));
				}

				CharSequence[] items = new CharSequence[index];
				for (int i = 0; i < index; i++)
				{
					items[i] = list[i];
				}

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				alertDialogBuilder.setTitle("Pick Progress Card");
				alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						final ProgressCard.ProgressCardType card = cards.get(item);
						cards.remove(card);
						board.getPlayerOfCurrentGameTurn().getHand().add(card);

						toast("Got Progress Card " + getString(ProgressCard.getCardStringResource(card)) + " from " + opponentPlayer.getPlayerName());
					}
				});
				alertDialogBuilder.setCancelable(false);
				alertDialogBuilder.create().show();
			}
		});
		builder.setCancelable(true);
		builder.create().show();

		toast("Played the Spy");
	}

	private void playCommercialHarbor(){
		//@TODO
		//add commercial harbor placement logic
		board.setReturnPhase(board.getPhase());
		board.setPhase(Board.Phase.PROGRESS_CARD_STEP_2);

		CharSequence[] playerList = new CharSequence[board.getNumPlayers()];
		boolean somethingToStealFrom = false;

		final Player current = board.getPlayerOfCurrentGameTurn();
		//storing opponent player in this

		int playerIndex = 0;
		int resourceIndex = 0;

		int totalResources = current.getBaseGameResourceCount();

		if (totalResources == 0) {
			toast("No resources to trade, progress card void");
			return;
		}

		final Map<CharSequence,Integer> resourcePointerMap = new HashMap<>();

		resourcePointerMap.put("Lumber", 0);
		resourcePointerMap.put("Wool", 1);
		resourcePointerMap.put("Grain", 2);
		resourcePointerMap.put("Brick", 3);
		resourcePointerMap.put("Ore", 4);

		int totalCommodities = 0;
		int lumberCount = 0;
		int woolCount = 0;
		int grainCount = 0;
		int brickCount = 0;
		int oreCount = 0;

		for (int i= 0; i < board.getNumPlayers(); i++) {
			totalCommodities = 0;
			resourceIndex = 0;

			//ArrayList in which we store the resources that the cur player has
			final List<String> currentlyContainedResources = new ArrayList<>();
			final Player player = board.getPlayerById(i);

			lumberCount = current.getResources(Resource.ResourceType.LUMBER);
			if(lumberCount != 0) {
				currentlyContainedResources.add(resourceIndex++, "Lumber");
			}
			woolCount = current.getResources(Resource.ResourceType.WOOL);
			if(woolCount != 0) {
				currentlyContainedResources.add(resourceIndex++, "Wool");
			}
			grainCount = current.getResources(Resource.ResourceType.GRAIN);
			if(grainCount != 0) {
				currentlyContainedResources.add(resourceIndex++, "Grain");
			}
			brickCount = current.getResources(Resource.ResourceType.BRICK);
			if(brickCount != 0) {
				currentlyContainedResources.add(resourceIndex++, "Brick");
			}
			oreCount = current.getResources(Resource.ResourceType.ORE);
			if(oreCount != 0) {
				currentlyContainedResources.add(resourceIndex, "Ore");
			}

			CharSequence[] items = new CharSequence[resourceIndex];
			items = currentlyContainedResources.toArray(items);

			if (player == current) {
				//just move onto the next player
				continue;
			} else {
				totalCommodities = player.getResources(Resource.ResourceType.PAPER) +
						player.getResources(Resource.ResourceType.COIN) +
						player.getResources(Resource.ResourceType.CLOTH) +
						player.getResources(Resource.ResourceType.GOLD);
				if (totalCommodities > 0) {
					String playerInfo = "Trade Resource with commodity with \n" + player.getPlayerName() + " " + player.getPlayerNumber();

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(playerInfo);
					builder.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							dialog.dismiss();
							Resource.ResourceType resourceType = Resource.RESOURCE_TYPES[resourcePointerMap.get(currentlyContainedResources.get(item))];
							player.addResources(resourceType, 1, false);
							current.decreaseResources(resourceType);
							toast("Gave 1 resource of " + currentlyContainedResources.get(item) + "to " + player.getPlayerName());
							board.getPlayerIdsYetToAct().add(player.getPlayerNumber());
						}
					});
					builder.setCancelable(true);
					builder.create().show();

				}
			}
		}

		mListener.endTurn(board.getPlayerOfCurrentGameTurn().getGooglePlayParticipantId(), false);
		board.nextPhase();
		/*
		CharSequence[] items = new CharSequence[playerIndex];
		for (int i = 0; i < playerIndex; i++) {
			items[i] = playerList[i];
		}
		for (int i = 0; i < playerIndex; i++) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(playerList[i]);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

				}
			});

		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.commercial_harbor_trade_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

			}
		});
		*/

		toast("Played the Commercial Harbor");
	}

	private void playMerchantFleet(){
		//@TODO
		//add merchant placement logic
		board.setIsMerchantFleetActive(true);
		board.getPlayerOfCurrentGameTurn().setIsMerchantFleetActive(true);
		toast("Played the Merchant Fleet");
	}

	private void confirmChaseRobberDialog() {
		final int confirm = 0;
		final int cancel = 1;
		CharSequence[] items = new CharSequence[2];
		items[0] = getString(R.string.game_confirm_chase_robber);
		items[1] = getString(R.string.game_cancel_str);

		//create the popup asking which card to use
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.game_confirm_chase_robber_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (item == cancel) {
					dialog.dismiss();
				} else if (item == confirm){
					dialog.dismiss();
					board.setReturnPhase(board.getPhase());
					board.startRobberPhase();
					showState(false);
				}
			}
		});

		builder.create().show();
	}

	private void confirmChasePirateDialog() {
		final int confirm = 0;
		final int cancel = 1;
		CharSequence[] items = new CharSequence[2];
		items[0] = getString(R.string.game_confirm_chase_pirate);
		items[1] = getString(R.string.game_cancel_str);

		//create the popup asking which card to use
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.game_confirm_chase_pirate_title));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (item == cancel) {
					dialog.dismiss();
				} else if (item == confirm){
					dialog.dismiss();
					board.setReturnPhase(board.getPhase());
					board.startPiratePhase();
					showState(false);
				}
			}
		});

		builder.create().show();
	}

//	private void monopoly() {
//		CharSequence[] items = new CharSequence[Resource.RESOURCE_TYPES.length];
//		for (int i = 0; i < items.length; i++)
//			items[i] = String.format(getString(R.string.game_monopoly_select),
//					getActivity().getString(Resource.toRString(Resource.RESOURCE_TYPES[i])));
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setTitle(getString(R.string.game_monopoly_prompt));
//		builder.setItems(items, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Player player = board.getPlayerOfCurrentGameTurn();
//
//				if (player.useCard(Board.ProgressCardType.MONOPOLY)) {
//					int total = player.monopoly(Resource.RESOURCE_TYPES[which]);
//					toast(String.format(getString(R.string.game_used_monopoly),
//							total));
//					showState(false);
//				} else {
//					toast(getString(R.string.game_card_fail));
//				}
//			}
//		});
//
//		builder.create().show();
//	}

//	private void harvest() {
//		CharSequence[] items = new CharSequence[Resource.RESOURCE_TYPES.length];
//		for (int i = 0; i < items.length; i++)
//			items[i] = String.format(getString(R.string.game_harvest_select),
//					getActivity().getString(Resource.toRString(Resource.RESOURCE_TYPES[i])));
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setTitle(getActivity().getString(R.string.game_harvest_prompt));
//		builder.setItems(items, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Player player = board.getPlayerOfCurrentGameTurn();
//
//				if (player.useCard(Board.ProgressCardType.HARVEST)) {
//					player.harvest(Resource.RESOURCE_TYPES[which], Resource.RESOURCE_TYPES[which]);
//					toast(getString(R.string.game_used_harvest));
//					showState(false);
//				} else {
//					toast(getString(R.string.game_card_fail));
//				}
//			}
//		});
//
//		builder.create().show();
//	}

}
