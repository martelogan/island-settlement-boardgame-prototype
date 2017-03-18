package com.catandroid.app.common.ui.fragments;

import com.catandroid.app.common.logistics.multiplayer.TradeProposal;
import com.catandroid.app.common.components.board_pieces.ProgressCard;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.ui.fragments.interaction_fragments.CityImprovementFragment;
import com.catandroid.app.common.ui.fragments.interaction_fragments.DiscardResourcesFragment;
import com.catandroid.app.R;;
import com.catandroid.app.common.components.Board;
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

import java.util.Vector;

public class ActiveGameFragment extends Fragment {

	private static final int MIN_BOT_DELAY = 1000;
	private static final int DEFAULT_TURN_DELAY = 750;

	private static final int UPDATE_MESSAGE = 1, LOG_MESSAGE = 2, DISCARD_MESSAGE = 3;

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

	private static final String[] ROLL_STRINGS = { "", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅" };
	private static final String[] EVENT_ROLL_STRINGS = { "", "☠", "☠", "☠", "Trade", "Science", "Politics" };

	public Listener mListener = null;

	public String myParticipantId;

	public ActiveGameFragment(){ }

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

				if (board.hasPlayersYetToDiscard()) {
					//show popup if we are the ones that should discard
					if(board.checkNextPlayerToDiscard().getGooglePlayParticipantId().equals(myParticipantId)){
						Message discard = new Message();
						discard.what = DISCARD_MESSAGE;
						turnHandler.sendMessage(discard);
					}
				} else if (board.getCurrentPlayer().isBot()) {
					board.runAITurn();
					Message change = new Message();
					change.what = UPDATE_MESSAGE;
					turnHandler.sendMessage(change);

					if (board.getCurrentPlayer().isHuman()) {
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

					Player toDiscard = board.getPlayerToDiscard();
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
			case R.id.reference:
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
				select(action, board.getHexagonById(id));
				break;

			case BUILD_SETTLEMENT:
			case BUILD_CITY:
			case BUILD_CITY_WALL:
			case HIRE_KNIGHT:
			case ACTIVATE_KNIGHT:
			case PROMOTE_KNIGHT:
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
			vertexUnitType = Vertex.CITY_WALL;
		}
		else if (action == Action.HIRE_KNIGHT || action == Action.ACTIVATE_KNIGHT
				|| action == Action.PROMOTE_KNIGHT) {
			vertexUnitType = Vertex.KNIGHT;
		}

		Player player = board.getCurrentPlayer();
		switch(vertexUnitType) {
			case Vertex.SETTLEMENT:
			case Vertex.CITY:
			case Vertex.CITY_WALL: // selecting buildable vertex unit
				if (player.buildVertexUnit(vertex, vertexUnitType)) {
					if (board.isSetupSettlement() || board.isSetupCity())
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

        Player player = board.getCurrentPlayer();
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
				else {
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
			//TODO: test move ship
			if (player.removeShipFrom(edge)) {
				renderer.setAction(Action.MOVE_SHIP_2);
				showState(false);
			}
		}
		else if (action == Action.MOVE_SHIP_2) {
            //TODO: test move ship
            if (player.moveShipTo(edge)) {
				board.nextPhase();
                renderer.setAction(Action.NONE);
                showState(true);
            }
        }
	}


	public boolean buttonPress(UIButton.ButtonType button) {
		boolean canAct = false;
		Player player = board.getCurrentPlayer();
		
		switch (button) {
			case PLAYER_STATUS:
				//PLAYER_STATUS IS THE BUTTON THAT IS ALWAYS VISIBLE IN TOP LEFT CORNER
				Log.d("myTag", "about to launch PLAYER PLAYER_STATUS");

				PlayerStatsFragment playerStatsFragment = new PlayerStatsFragment();
				playerStatsFragment.setBoard(board);
				playerStatsFragment.setMyPlayerId(myParticipantId);

				FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
				FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.fragment_container, playerStatsFragment, playerStatsFragment.getClass().getSimpleName());
				fragmentTransaction.addToBackStack(playerStatsFragment.getClass().getSimpleName());
				fragmentTransaction.commit();
				break;

			case DICE_ROLL:
				// enter build phase
				board.nextPhase();

				int roll1 = (int) (Math.random() * 6) + 1;
				int roll2 = (int) (Math.random() * 6) + 1;
				int event = (int) (Math.random() * 6) + 1;
				int roll = roll1 + roll2;
				board.getCurrentPlayer().rollDice(roll1, roll2 , event);
				if(!board.isMyPseudoTurn()){
					mListener.endTurn(board.getCurrentPlayer().getGooglePlayParticipantId(), false);
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
				for (Edge edge : board.getEdges()) {
					if (edge.canBuildRoad(player))
					{
						canAct = true;
					}
				}

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
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_road));
				break;

			case  BUILD_SHIP:
				for (Edge edge : board.getEdges()) {
					if (edge.canBuildShip(player))
					{
						canAct = true;
					}
				}

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
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_ship));

				break;

			case  MOVE_SHIP:
				boolean hasEdgeToRemove = false, hasSomewhereToMove = false;
				for (Edge edge : board.getEdges()) {
					if (edge.canRemoveShipFromHere(player))
					{
						hasEdgeToRemove = true;
					}
					if (edge.canMoveShipToHere(player)) {
						hasSomewhereToMove = true;
					}
					if (hasEdgeToRemove && hasSomewhereToMove) {
						canAct = true;
						break;
					}
				}

				if (!canAct) {
					cantAct(Action.MOVE_SHIP_1);
					break;
				}

				renderer.setAction(Action.MOVE_SHIP_1);
				setButtons(Action.MOVE_SHIP_1);
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_move_ship));
				break;

			case BUILD_SETTLEMENT:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canBuild(player, Vertex.SETTLEMENT, false))
						canAct = true;
				}

				if (!canAct) {
					cantAct(Action.BUILD_SETTLEMENT);
					break;
				}

				if (board.getCurrentPlayer().getNumOwnedSettlements() >= Player.MAX_SETTLEMENTS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_settlements_max));
					break;
				}

				renderer.setAction(Action.BUILD_SETTLEMENT);
				setButtons(Action.BUILD_SETTLEMENT);
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_settlement));
				break;

			case BUILD_CITY:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canBuild(player, Vertex.CITY, false))
						canAct = true;
				}

				if (!canAct) {
					cantAct(Action.BUILD_CITY);
					break;
				}

				if (board.getCurrentPlayer().getNumOwnedCities() >= Player.MAX_CITIES) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_city_max));
					break;
				}

				renderer.setAction(Action.BUILD_CITY);
				setButtons(Action.BUILD_CITY);
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						 + getActivity().getString(R.string.game_build_city));
				break;

			case PLAY_PROGRESS_CARD:
				progressCardDialog();

				break;

			case BUILD_CITY_WALL:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canBuild(player, Vertex.CITY_WALL, false))
					{
						canAct = true;
					}
				}

				if (!canAct) {
					cantAct(Action.BUILD_CITY_WALL);
					break;
				}

				if (board.getCurrentPlayer().getNumOwnedCityWalls() >= Player.MAX_CITY_WALLS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_wall_max));
					break;
				}

				renderer.setAction(Action.BUILD_CITY_WALL);
				setButtons(Action.BUILD_CITY_WALL);
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_build_wall));
				break;

			case HIRE_KNIGHT:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canPlaceKnightHere(player)) {
						canAct = true;
					}
				}

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
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_hire_knight));
				break;

			case ACTIVATE_KNIGHT:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canActivateKnightHere(player)) {
						canAct = true;
					}
				}

				if (!canAct) {
					cantAct(Action.ACTIVATE_KNIGHT);
					break;
				}

				renderer.setAction(Action.ACTIVATE_KNIGHT);
				setButtons(Action.ACTIVATE_KNIGHT);
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_activate_knight));
				break;

			case PROMOTE_KNIGHT:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canPromoteKnightHere(player)) {
						canAct = true;
					}
				}

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
				getActivity().setTitle(board.getCurrentPlayer().getPlayerName() + ": "
						+ getActivity().getString(R.string.game_promote_knight));
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

			case END_TURN:
				board.nextPhase();
				showState(true);
				break;

			case CANCEL_ACTION:
				// return false if there is nothing to cancel
				boolean result = renderer.cancel();

				if (renderer.getAction() == Action.MOVE_SHIP_2) {
					board.cancelMovingShipPhase();
				}

				//TODO: could this be moved?
				// sets renderer action to cancel
				showState(false);
				return result;
			}

		return true;
	}

	public boolean clickResource(int index) {
		if (!board.getCurrentPlayer().isHuman() || !board.isPlayerTurnPhase())
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
		Player player = board.getCurrentPlayer();

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

		renderer.setAction(action);
		setButtons(action);

		getActivity().setTitleColor(Color.WHITE);

		int color = TextureManager.getColor(board.getCurrentPlayer().getColor());
		color = TextureManager.darken(color, 0.35);
		ActionBar actionBar = getActivity().getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(color));

		//Add check if its actually our turn and
		int resourceId = board.getPhaseResource();
		if (resourceId != 0)
			if((board.itsMyTurn(myParticipantId) && !board.isMyPseudoTurn())
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
//            } else if(board.isTradeRespondedPhase() && board.getTradeProposal().getTradeCreatorPlayerId() == board.getCurrentPlayer().getPlayerNumber()){
//				resultsTrade();
//			}
        } else if(board.isTradeRespondedPhase()
				&& board.getPlayerById(board.getTradeProposal().getTradeCreatorPlayerId()).getGooglePlayParticipantId().equals(myParticipantId)){
			resultsTrade();
		}

		// TODO: remove/replace all references to resources
//		resources.setValues(player);

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

		Player player = board.getCurrentPlayer();
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
		} else if (action != Action.NONE) {
			// cancel the action
			view.addButton(ButtonType.CANCEL_ACTION);
		} else if (board.isProduction()) {
			view.addButton(ButtonType.DICE_ROLL);
            //TODO: add button to play progress cards
//			if (player.canUseCard())
//            {
//                view.addButton(ButtonType.PLAY_PROGRESS_CARD);
//            }
		} else if (board.isPlayerTurnPhase()) {
			view.addButton(ButtonType.TRADE);
			view.addButton(UIButton.ButtonType.END_TURN);

            if (!player.getHand().isEmpty())
            {
                view.addButton(UIButton.ButtonType.PLAY_PROGRESS_CARD);
            }

			if (player.canAffordToBuildRoad())
            {
                view.addButton(UIButton.ButtonType.BUILD_ROAD);
            }

            if (player.canAffordToBuildShip())
            {
				view.addButton(UIButton.ButtonType.BUILD_SHIP);
			}

			if(!player.movedShipThisTurn()) {
				view.addButton(UIButton.ButtonType.MOVE_SHIP);
			}

			if (player.canAffordToBuildSettlement())
            {
                view.addButton(UIButton.ButtonType.BUILD_SETTLEMENT);
            }

			if (player.canAffordToBuildCity())
            {
                view.addButton(UIButton.ButtonType.BUILD_CITY);
            }
            if (player.canAffordToBuildCityWall())
            {
				view.addButton(ButtonType.BUILD_CITY_WALL);
			}
			if(player.canAffordToHireKnight()) {
				view.addButton(ButtonType.HIRE_KNIGHT);
			}
			if(player.canAffordToActivateKnight()) {
				view.addButton(ButtonType.ACTIVATE_KNIGHT);
			}
			if(player.canAffordToPromoteKnight()) {
				view.addButton(ButtonType.PROMOTE_KNIGHT);
			}
			//@TODO ADD THESE BUTTONS WHEN THEY ARE RELEVANT
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
		int offset = board.getCurrentPlayer().getPlayerNumber() + 1;
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
		Player current = board.getCurrentPlayer();

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
		//TODO: why was this here?
//		builder.setTitle(getString(R.string.to_remove_str));
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
		Player current = board.getCurrentPlayer();
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
				Resource.ResourceType resourceType = board.getCurrentPlayer().steal(player);

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
				showState(false);
				return;
			}

			index++;
		}

	}

	private void cantAct(Action action) {
		Player player = board.getCurrentPlayer();

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

			default:
				return;
		}

		popup(getString(R.string.game_cant_act_str), message);

		showState(false);
	}

	private void progressCardDialog() {
		final Player me = board.getPlayerFromParticipantId(myParticipantId);
		final Vector<ProgressCard.ProgressCardType> cards = me.getHand();

		CharSequence[] list = new CharSequence[cards.size()+1];
		int index = 0;

		for (int i = 0; i < cards.size(); i++) {
			ProgressCard.ProgressCardType type = cards.get(i);

			list[index++] = getString(ProgressCard.getCardStringResource(type));
		}

		list[index++] = getString(R.string.game_cancel_str);

		CharSequence[] items = new CharSequence[index];
		for (int i = 0; i < index; i++)
			items[i] = list[i];

		final int cancel = index-1;

		//create the popup asking which card to use
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.status_progress_cards));
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
											//play the card
											switch (card) {
												case MERCHANT:
													playMerchant();
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
//				Player player = board.getCurrentPlayer();
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
//				Player player = board.getCurrentPlayer();
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
