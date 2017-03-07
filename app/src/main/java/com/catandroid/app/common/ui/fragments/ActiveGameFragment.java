package com.catandroid.app.common.ui.fragments;

import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.ui.fragments.interaction_fragments.DiscardResourcesFragment;
import com.catandroid.app.R;;
import com.catandroid.app.common.components.Board;
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

	private Handler turnHandler;
	private TurnThread turnThread;
	
	private GameRenderer renderer;

	private boolean isActive;

	private static final String[] ROLL_STRINGS = { "", "⚀", "⚁", "⚂", "⚃", "⚄", "⚅" };

	public Listener mListener = null;

	private String myParticipantId;

	public ActiveGameFragment(){ }

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(getActivity().findViewById(R.id.setup) != null){
			getActivity().findViewById(R.id.setup).setVisibility(View.GONE);
		}
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
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

				if (board.checkPlayerToDiscard()) {
					Message discard = new Message();
					discard.what = DISCARD_MESSAGE;
					turnHandler.sendMessage(discard);
				} else if (board.getCurrentPlayer().isBot()) {
					board.runTurn();
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

					FragmentTransaction discardFragmentTransaction =  getActivity().getSupportFragmentManager().beginTransaction();
					discardFragmentTransaction.replace(R.id.fragment_container, discardResourcesFragment,"DISCARD");
					discardFragmentTransaction.addToBackStack("DISCARD");
					discardFragmentTransaction.commit();


					break;
				}

				super.handleMessage(msg);
			}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fa = (FragmentActivity) super.getActivity();

		RelativeLayout frame = new RelativeLayout(getActivity());

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
		frame.addView(view);


		((ViewGroup)view.getParent()).removeView(view);

		turnHandler = new UpdateHandler();

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
			case ROBBER:
				select(action, board.getHexagonById(id));
				break;

			case SETTLEMENT:
			case CITY:
				select(action, board.getVertexById(id));
				break;

			case ROAD:
				select(action, board.getEdgeById(id));
				break;

			default:
				// REMOVED
				//Log.e(getClass().getName(), "invalid selection type");
				break;
			}
	}

	private void select(Action action, Hexagon hexagon) {
		if (action == Action.ROBBER) {
			if (hexagon != board.getPrevRobberHex()) {
				board.setRobber(hexagon.getId());
				showState(false);
			} else {
				popup(getString(R.string.game_cant_move_robber),
						getString(R.string.game_robber_same));
			}
		}
	}

	private void select(Action action, Vertex vertex) {
		int type = Vertex.NONE;
		if (action == Action.SETTLEMENT)
		{
			type = Vertex.SETTLEMENT;
		}
		else if (action == Action.CITY)
		{
			type = Vertex.CITY;
		}

		Player player = board.getCurrentPlayer();
		if (player.build(vertex, type)) {
			if (board.isSetupSettlement() || board.isSetupCity())
			{
				board.nextPhase();
			}

			showState(false);
		}
	}

	private void select(Action action, Edge edge) {
		Player player = board.getCurrentPlayer();
		if (player.build(edge)) {
			renderer.setAction(Action.NONE);

			if (board.isSetupRoad()) {
				board.nextPhase();
				showState(true);
			} else if (board.isProgressPhase()) {
				board.nextPhase();
				
				boolean canBuild = false;
				for (Edge other : board.getEdges()) {
					if (other.canBuild(player))
						canBuild = true;
				}
				
				if (!canBuild) {
					board.nextPhase();
					cantBuild(Action.ROAD);
				}
				
				showState(false);
			} else {
				showState(false);
			}
		}
	}

	public boolean buttonPress(UIButton.ButtonType button) {
		boolean canBuild = false;
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
				int roll = roll1 + roll2;
				board.getCurrentPlayer().roll(roll);
				mListener.endTurn(board.getCurrentPlayer().getGooglePlayParticipantId(), false);

				if (roll == 7) {
					toast(getString(R.string.game_rolled_str) + " 7 " + ROLL_STRINGS[roll1]
							+ ROLL_STRINGS[roll2] + " "
							+ getString(R.string.game_move_robber));
					showState(true);
					break;
				} else {
					toast(getString(R.string.game_rolled_str) + " " + roll + " "
							+ ROLL_STRINGS[roll1] + ROLL_STRINGS[roll2]);
				}

				showState(false);
				break;

			case BUILD_ROAD:
				for (Edge edge : board.getEdges()) {
					if (edge.canBuild(player))
					{
						canBuild = true;
					}
				}

				if (!canBuild) {
					cantBuild(Action.ROAD);
					break;
				}

				if (board.getCurrentPlayer().getNumRoads() >= Player.MAX_ROADS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_road_max));
					break;
				}

				renderer.setAction(Action.ROAD);
				setButtons(Action.ROAD);
				getActivity().setTitle(board.getCurrentPlayer().getName() + ": "
						+ getActivity().getString(R.string.game_build_road));

				break;

			case BUILD_SETTLEMENT:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canBuild(player, Vertex.SETTLEMENT, false))
						canBuild = true;
				}

				if (!canBuild) {
					cantBuild(Action.SETTLEMENT);
					break;
				}

				if (board.getCurrentPlayer().getNumSettlements() >= Player.MAX_SETTLEMENTS) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_settlements_max));
					break;
				}

				renderer.setAction(Action.SETTLEMENT);
				setButtons(Action.SETTLEMENT);
				getActivity().setTitle(board.getCurrentPlayer().getName() + ": "
						+ getActivity().getString(R.string.game_build_settlement));
				break;

			case BUILD_CITY:
				for (Vertex vertex : board.getVertices()) {
					if (vertex.canBuild(player, Vertex.CITY, false))
						canBuild = true;
				}

				if (!canBuild) {
					cantBuild(Action.CITY);
					break;
				}

				if (board.getCurrentPlayer().getNumCities() >= Player.MAX_CITIES) {
					popup(getString(R.string.game_cant_build_str),
							getString(R.string.game_build_city_max));
					break;
				}

				renderer.setAction(Action.CITY);
				setButtons(Action.CITY);
				getActivity().setTitle(board.getCurrentPlayer().getName() + ": "
						 + getActivity().getString(R.string.game_build_city));
				break;

			case PROGRESS_CARD:
	//			development();
				break;

			case TRADE:

				FragmentManager tradeFragmentManager = getActivity().getSupportFragmentManager();
				TradeRequestFragment tradeFragment = new TradeRequestFragment();
				tradeFragment.setBoard(board);
				FragmentTransaction tradeFragmentTransaction =  tradeFragmentManager.beginTransaction();
				tradeFragmentTransaction.replace(R.id.fragment_container, tradeFragment,tradeFragment.getClass().getSimpleName());
				tradeFragmentTransaction.addToBackStack(tradeFragment.getClass().getSimpleName());
				tradeFragmentTransaction.commit();

				showState(false);

				break;

			case END_TURN:
				board.nextPhase();
				showState(true);
				break;

			case CANCEL:
				// return false if there is nothing to cancel
				boolean result = renderer.cancel();

				showState(false);
				return result;
			}

		return true;
	}

	public boolean clickResource(int index) {
		if (!board.getCurrentPlayer().isHuman() || !board.isBuild())
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

	private void showState(boolean setZoom) {
		Player player = board.getCurrentPlayer();

		renderer.setState(board, (player.isHuman() && board.itsMyTurn(myParticipantId))  ? player : null, texture, board.getLastDiceRollNumber());

		if (setZoom)
			renderer.getGeometry().zoomOut();

		// show card stealing dialog
		if (board.isRobberPhase() && board.getCurRobberHex() != null)
			steal();

		// display winner
		boolean hadWinner = board.getWinner() != null;
		Player winner = board.getWinner();
		if (!hadWinner && winner != null) {
			// declare winner
			final Builder infoDialog = new AlertDialog.Builder(getActivity());
			infoDialog.setTitle(getString(R.string.phase_game_over));
			infoDialog.setIcon(R.drawable.logo);
			infoDialog.setMessage(winner.getName() + " "
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
			action = Action.SETTLEMENT;
		else if (board.isSetupCity())
			action = Action.CITY;
		else if (board.isSetupRoad() || board.isProgressPhase())
			action = Action.ROAD;
		else if (board.isRobberPhase() && board.getCurRobberHex() == null)
			action = Action.ROBBER;

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
			if(board.itsMyTurn(myParticipantId)) {
				getActivity().setTitle(player.getName() + ": " + getActivity().getString(resourceId));
			} else{
				getActivity().setTitle(R.string.not_my_turn);
			}
		else
			getActivity().setTitle(player.getName());

		// TODO: remove/replace all references to resources
//		resources.setValues(player);

		Log.d("myTag", "end of showState");
	}


	private void setButtons(Action action) {
		view.removeButtons();

		view.addButton(ButtonType.PLAYER_STATUS);

		Player player = board.getCurrentPlayer();
		Player winner = board.getWinner();
		if (winner != null || !player.isHuman() || !board.itsMyTurn(myParticipantId)){
			// anonymous mode
		} else if (board.isSetupPhase()) {
			// no extra buttons in showState phase
		} else if (board.isProgressPhase()) {
			// TODO: add ability to cancel card use
			// consider what happens if there's nowhere to build a road
		} else if (board.isRobberPhase()) {
			// do nothing
		} else if (action != Action.NONE) {
			// cancel the action
			view.addButton(ButtonType.CANCEL);
		} else if (board.isProduction()) {
			view.addButton(ButtonType.DICE_ROLL);
            //TODO: add button to play progress cards
//			if (player.canUseCard())
//            {
//                view.addButton(ButtonType.PROGRESS_CARD);
//            }
		} else if (board.isBuild()) {
			view.addButton(ButtonType.TRADE);
			view.addButton(UIButton.ButtonType.END_TURN);

            //TODO: add button to play progress cards
//			if (player.affordCard() || player.canUseCard())
//            {
//                view.addButton(UIButton.ButtonType.PROGRESS_CARD);
//            }

			if (player.affordRoad())
            {
                view.addButton(UIButton.ButtonType.BUILD_ROAD);
            }

			if (player.affordSettlement())
            {
                view.addButton(UIButton.ButtonType.BUILD_SETTLEMENT);
            }

			if (player.affordCity())
            {
                view.addButton(UIButton.ButtonType.BUILD_CITY);
            }
		}
	}

	private void toast(String message) {
		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT)
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
			if (board.getTurnNumber() == 1 && (i % board.getNumPlayers()) >= offset)
			{
				continue;
			}

			Player player = board.getPlayer(i % board.getNumPlayers());
			String name = player.getName()
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

	private void steal() {
		if (!board.isRobberPhase()) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling steal() out of robber phase");
			return;
		}

		Hexagon robbing = board.getCurRobberHex();
		if (robbing == null) {
			Log.w(getActivity().getClass().getName(),
					"shouldn't be calling steal() without robber location set");
			showState(false);
			return;
		}

		Player current = board.getCurrentPlayer();

		CharSequence[] list = new CharSequence[3];
		int index = 0;

		Player player = null;
		for (int i = 0; i < board.getNumPlayers(); i++) {
			player = board.getPlayer(i);

			// don't steal from self or players without a settlement/city
			if (player == current || !robbing.adjacentToPlayer(player))
			{
				continue;
			}

			// add to list of players to steal from
			int count = player.getResourceCount();
			list[index++] = getString(R.string.game_steal_from_str) + " "
					+ player.getName() + " (" + count + " "
					+ getString(R.string.game_resources_str) + ")";
		}

		if (index == 0) {
			// nobody to steal from
			toast(getString(R.string.game_steal_fail_str));

			board.nextPhase();
			showState(false);
			return;
		} else if (index == 1) {
			// automatically steal if only one player is listed
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

	private void steal(int select) {
		if (!board.isRobberPhase())
		{
			return;
		}

		Player current = board.getCurrentPlayer();

		Hexagon robbing = board.getCurRobberHex();
		if (robbing == null)
		{
			return;
		}

		int index = 0;
		for (int i = 0; i < board.getNumPlayers(); i++) {
			Player player = board.getPlayer(i);
			if (player == current || !robbing.adjacentToPlayer(player))
			{
				continue;
			}

			if (index == select) {
				Resource.ResourceType resourceType = board.getCurrentPlayer().steal(player);

				if (resourceType != null)
				{
					toast(getString(R.string.game_stole_str) + " "
							+ getActivity().getString(Resource.toRString(resourceType))
							+ " " + getString(R.string.game_from_str) + " "
							+ player.getName());
				}
				else
				{
					toast(getString(R.string.game_player_couldnt_steal) + " "
							+ player.getName());
				}

				board.nextPhase();
				showState(false);
				return;
			}

			index++;
		}
	}

	private void cantBuild(Action action) {
		Player player = board.getCurrentPlayer();

		String message = "";
		switch (action) {
			case ROAD:

				if (player.getNumRoads() == Player.MAX_ROADS)
				{
					message = getActivity().getString(R.string.game_build_road_max);
				}
				else
				{
					message = getString(R.string.game_cant_build_road);
				}

				if (board.isProgressPhase1()) {
					//TODO: is road cost affected by progress card?
				} else if (board.isProgressPhase2()) {
					//TODO: is road cost affected by progress card?
				}

				break;

			case SETTLEMENT:
				if (player.getNumSettlements() == Player.MAX_SETTLEMENTS)
					message = getString(R.string.game_build_settlements_max);
				else
					message = getString(R.string.game_cant_build_settlement);

				break;

			case CITY:
				if (player.getNumCities() == Player.MAX_CITIES)
					message = getString(R.string.game_build_city_max);
				else
					message = getString(R.string.game_cant_build_city);

				break;

			default:
				return;
		}

		popup(getString(R.string.game_cant_build_str), message);

		showState(false);
	}

	//TODO: see how we can use this similar code for progress cards

//	private void development() {
//		Player player = board.getCurrentPlayer();
//		int[] cards = player.getCards();
//
//		CharSequence[] list = new CharSequence[Board.ProgressCardType.values().length + 2];
//		int index = 0;
//
//		if (player.affordCard() && board.isBuild())
//			list[index++] = getString(R.string.to_remove_str);
//
//		for (int i = 0; i < Board.ProgressCardType.values().length; i++) {
//			Board.ProgressCardType type = Board.ProgressCardType.values()[i];
//			if (!player.hasCard(type))
//				continue;
//
//			String quantity = (cards[i] > 1 ? " (" + cards[i] + ")" : "");
//
//			if (type == ProgressCardType.SOLDIER)
//				list[index++] = getString(R.string.to_remove_str) + quantity;
//			else if (type == ProgressCardType.PROGRESS)
//				list[index++] = getString(R.string.to_remove_str)
//						+ quantity;
//			else if (type == ProgressCardType.HARVEST)
//				list[index++] = getString(R.string.to_remove_str) + quantity;
//			else if (type == ProgressCardType.MONOPOLY)
//				list[index++] = getString(R.string.to_remove_str)
//						+ quantity;
//		}
//
//		list[index++] = getString(R.string.game_cancel_str);
//
//		CharSequence[] items = new CharSequence[index];
//		for (int i = 0; i < index; i++)
//			items[i] = list[i];
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setTitle(getString(R.string.to_remove_str));
//		builder.setItems(items, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int item) {
//				Player player = board.getCurrentPlayer();
//
//				if (player.affordCard() && board.isBuild()) {
//					// buy a card
//					if (item == 0) {
//						Board.ProgressCardType card = player.buyCard();
//						if (card != null)
//							toast(getString(R.string.game_purchased_str)
//									+ " "
//									+ getActivity().getString(Board
//											.getCardStringResource(card)) + " "
//									+ getString(R.string.to_remove_str));
//						else
//							toast(getString(R.string.to_remove_str));
//
//						showState(false);
//						return;
//					}
//
//					item--;
//				}
//
//
//				// try to use a card
//				for (int i = 0; i < Board.ProgressCardType.values().length; i++) {
//					Board.ProgressCardType type = Board.ProgressCardType.values()[i];
//					if (item > 0 && player.hasCard(type)) {
//						item--;
//					} else if (item == 0 && player.hasCard(type)) {
//						switch (type) {
//						case HARVEST:
////							harvest();
//							return;
//
//						case MONOPOLY:
////							monopoly();
//							return;
//
//						case SOLDIER:
//							if (player.useCard(type)) {
//								toast(getString(R.string.to_remove_str));
//								showState(true);
//								return;
//							}
//							break;
//
//						case PROGRESS:
//							boolean canBuild = false;
//							for (Edge edge : board.getEdges()) {
//								if (edge.canBuild(player))
//									canBuild = true;
//							}
//
//							if (!canBuild) {
//								cantBuild(Action.BUILD_ROAD);
//								return;
//							} else if (player.useCard(type)) {
//								toast(getString(R.string.to_remove_str));
//								showState(false);
//								return;
//							}
//							break;
//
//						case VICTORY:
//							break;
//						}
//
//						toast(getString(R.string.to_remove_str));
//					}
//				}
//			}
//		});
//
//		builder.create().show();
//	}

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
