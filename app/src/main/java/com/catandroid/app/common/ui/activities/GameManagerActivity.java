package com.catandroid.app.common.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.pm.ActivityInfo;

import com.catandroid.app.CatAndroidApp;
import com.catandroid.app.common.components.BoardGeometry;
import com.catandroid.app.common.logistics.AppSettings;
import com.catandroid.app.common.logistics.multiplayer.CatandroidTurn;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;
import com.catandroid.app.R;
import com.catandroid.app.common.components.Board;

//***************
//SkeletonActivity
//***************
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.gson.Gson;

import java.util.ArrayList;

public class GameManagerActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener,
		View.OnClickListener, ActiveGameFragment.Listener {

	private String[] names;
	private boolean[] types;
	private boolean auto_discard = false;

	private static final String AUTO_KEY = "auto_discard";

	private static final String[] HUMAN_KEYS = { "player_human1",
			"player_human2", "player_human3", "player_human4" };

	public static final boolean[] DEFAULT_HUMANS = { true, false, false, false };

	private ActiveGameFragment activeGameFragment;

	/******************
	 * SkeletonActivity
	 *******************/

	public static final String TAG = "SkeletonActivity";

	// Client used to interact with Google APIs
	private GoogleApiClient mGoogleApiClient;

	// Are we currently resolving a connection failure?
	private boolean mResolvingConnectionFailure = false;

	// Has the user clicked the sign-in button?
	private boolean mSignInClicked = false;

	// Automatically start the sign-in flow when the Activity starts
	private boolean mAutoStartSignInFlow = true;

	// Current turn-based match
	private TurnBasedMatch mTurnBasedMatch;


	private AlertDialog mAlertDialog;

	// For our intents
	private static final int RC_SIGN_IN = 9001;
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_LOOK_AT_MATCHES = 10001;

	// Should I be showing the turn API?
	public boolean isDoingTurn = false;

	// This is the current match we're in; null if not loaded
	public TurnBasedMatch mMatch;

	// This is the current match data after being unpersisted.
	// Do not retain references to match data once you have
	// taken an action on the match, such as takeTurn()
	public CatandroidTurn catandroidTurn;

	//This is the id of the current match that the user has loaded and is actively playing
	String currentMatchId;

	public static void setup(AppSettings appSettings) {
		for (int i = 0; i < 4; i++)
		{
			appSettings.set(HUMAN_KEYS[i], DEFAULT_HUMANS[i]);
		}
	}


	private void populate() {

		CheckBox discardCheck = (CheckBox) findViewById(R.id.auto_discard);
		discardCheck.setChecked(auto_discard);
	}

	private void load(AppSettings appSettings) {

		auto_discard = appSettings.getBool(AUTO_KEY);
	}

	private void save(AppSettings appSettings) {

		appSettings.set(AUTO_KEY, auto_discard);
	}

	@Override
	public void onCreate(Bundle state) {
		//when GameManagerActivity launches, make login mandatory.
		//we create the mGoogleApiClient that is used to intereact with playservices
		//at this time.

		super.onCreate(state);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.game_manager);

		// Create the Google API Client with access to Plus and Games
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Games.API).addScope(Games.SCOPE_GAMES)
				.build();

		// Setup signin and signout buttons
		findViewById(R.id.sign_out_button).setOnClickListener(this);
		findViewById(R.id.sign_in_button).setOnClickListener(this);

	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart(): Connecting to Google APIs");
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop(): Disconnecting from Google APIs");
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onBackPressed() {
		Log.d("myTag", "DETECTED BACK");
		if(getSupportFragmentManager().findFragmentByTag("DISCARD") != null && getSupportFragmentManager().findFragmentByTag("DISCARD").isVisible()) {
			//DO NOTHING, AVOID USER EXITING DISCARD REQUEST
		} else {
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				Log.d("myTag", "THERE WAS SOMETHING IN THE FRAGSTACK");
				getSupportFragmentManager().popBackStack();
			} else {
				catandroidTurn.currentBoard = null;
				ActionBar actionBar = getActionBar();
				actionBar.setDisplayHomeAsUpEnabled(false);
				actionBar.setDisplayShowCustomEnabled(false);
				setTitle("CatAndroid");
				View item = findViewById(R.id.reference);
				if(item != null){
					item.setVisibility(View.GONE);
				}


				setContentView(R.layout.game_manager);
				setViewVisibility();
				// Setup signin and signout buttons
				findViewById(R.id.sign_out_button).setOnClickListener(this);
				findViewById(R.id.sign_in_button).setOnClickListener(this);
			}
		}
	}

	/******************
	 * SkeletonActivity
	 *******************/

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected(): Connection successful");

		// Retrieve the TurnBasedMatch from the connectionHint
		if (connectionHint != null) {
			mTurnBasedMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

			if (mTurnBasedMatch != null) {
				if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
					Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
				}

				//updateMatch(mTurnBasedMatch);
				return;
			}
		}
		if(catandroidTurn.currentBoard == null) {
			setViewVisibility();
		}

		// As a demonstration, we are registering this activity as a handler for
		// invitation and match events.

		// This is *NOT* required; if you do not register a handler for
		// invitation events, you will get standard notifications instead.
		// Standard notifications may be preferable behavior in many cases.
		Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

		// Likewise, we are registering the optional MatchUpdateListener, which
		// will replace notifications you would get otherwise. You do *NOT* have
		// to register a MatchUpdateListener.
		Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "onConnectionSuspended():  Trying to reconnect.");
		mGoogleApiClient.connect();
		setViewVisibility();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed(): attempting to resolve");
		if (mResolvingConnectionFailure) {
			// Already resolving
			Log.d(TAG, "onConnectionFailed(): ignoring connection failure, already resolving.");
			return;
		}

		// Launch the sign-in flow if the button was clicked or if auto sign-in is enabled
		if (mSignInClicked || mAutoStartSignInFlow) {
			mAutoStartSignInFlow = false;
			mSignInClicked = false;

			mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this,
					mGoogleApiClient, connectionResult, RC_SIGN_IN,
					getString(R.string.signin_other_error));
		}

		setViewVisibility();
	}

	// Displays your inbox. You will get back onActivityResult where
	// you will need to figure out what you clicked on.
	public void onCheckGamesClicked(View view) {
		setContentView(R.layout.game_setup_options);
		showSpinner();
		findViewById(R.id.setup).setVisibility(View.GONE);
		Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
		startActivityForResult(intent, RC_LOOK_AT_MATCHES);

	}

	// Open the create-game UI. You will get back an onActivityResult
	// and figure out what to do.
	public void onStartMatchCreationClicked(View view) {

		//********************
		//GAMESETUP LOGIC SCREEN INIT
		//********************

		setContentView(R.layout.game_setup_options);

		load(((CatAndroidApp) getApplicationContext()).getAppSettingsInstance());

		//Set the checkboxes, spinners to default values
		populate();

		//SET THE NUMBER OF VICTORY POINTS
		Spinner pointSpinner = (Spinner) findViewById(R.id.option_max_points);
		ArrayAdapter<CharSequence> pointChoices = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		pointChoices
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		for (int i = 5; i <= 15; i++) {
			String choice = i + " " + getString(R.string.victory_points_to_win);

			if (i == 5)
			{
				choice += " " + getString(R.string.short_game);
			}
			else if (i == 10)
			{
				choice += " " + getString(R.string.standard_game);
			}
			else if (i == 15)
			{
				choice += " " + getString(R.string.long_game);
			}

			pointChoices.add(choice);
		}

		pointSpinner.setAdapter(pointChoices);
		pointSpinner.setSelection(5);

		//SET THE NUMBER OF PLAYERS
		Spinner playerSpinner = (Spinner) findViewById(R.id.option_num_players);
		ArrayAdapter<CharSequence> playerChoices = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		playerChoices
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		playerChoices.add("3 Players");
		playerChoices.add("4 Players");

		playerSpinner.setAdapter(playerChoices);
		playerSpinner.setSelection(0);

		//SET THE BOARD SIZE
		Spinner boardSizeSpinner = (Spinner) findViewById(R.id.option_board_size);
		ArrayAdapter<CharSequence> boardSizeChoice = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		boardSizeChoice
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		boardSizeChoice.add("Regular Board");
		boardSizeChoice.add("Large Board");

		boardSizeSpinner.setAdapter(boardSizeChoice);
		boardSizeSpinner.setSelection(0);

		final Button start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				//get the number of players from the spinner
				Spinner playerSpinner = (Spinner) findViewById(R.id.option_num_players);
				int numberPlayersToInvite = playerSpinner.getSelectedItemPosition() + 2;
				int numberPlayersTotal = numberPlayersToInvite + 1;
				names = new String[numberPlayersTotal];
				types = new boolean[numberPlayersTotal];

				showSpinner();

				//LAUNCH THE MULTIPLAYER SELECTION AND SUBSEQUENTLY LAUNCH GAME THHROUGH onActivityResult
				Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,
						numberPlayersToInvite, numberPlayersToInvite, true);
				startActivityForResult(intent, RC_SELECT_PLAYERS);

			}
		});

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	// Sign-in, Sign out behavior

	// Update the visibility based on what state we're in.
	public void setViewVisibility() {
		boolean isSignedIn = (mGoogleApiClient != null) && (mGoogleApiClient.isConnected());

		if (!isSignedIn) {
			findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			findViewById(R.id.matchup_layout).setVisibility(View.GONE);

			if (mAlertDialog != null) {
				mAlertDialog.dismiss();
			}
			return;
		}

		((TextView) findViewById(R.id.name_field)).setText(Games.Players.getCurrentPlayer(
				mGoogleApiClient).getDisplayName());
		findViewById(R.id.login_layout).setVisibility(View.GONE);

		if (isDoingTurn) {
			findViewById(R.id.matchup_layout).setVisibility(View.GONE);
		} else {
			findViewById(R.id.matchup_layout).setVisibility(View.VISIBLE);
			setTitle("CatAndroid");
		}
	}

	// Switch to gameplay view.
	public void setGameplayUI() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		dismissSpinner();

		//Start the fragment
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_container,activeGameFragment)
				.commit();

	}

	// Helpful dialogs

	public void showSpinner() {
		findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
	}

	public void dismissSpinner() {
		findViewById(R.id.progressLayout).setVisibility(View.GONE);
	}

	// Generic warning/info dialog
	public void showWarning(String title, String message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle(title).setMessage(message);

		// set dialog message
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
					}
				});

		// create alert dialog
		mAlertDialog = alertDialogBuilder.create();

		// show it
		mAlertDialog.show();
	}

	// Rematch dialog
	public void askForRematch() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setMessage("Do you want a rematch?");

		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Sure, rematch!",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								rematch();
							}
						})
				.setNegativeButton("No.",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						});

		alertDialogBuilder.show();
	}

	// This function is what gets called when you return from either the Play
	// Games built-in inbox, or else the create game built-in interface.
	@Override
	public void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		if (request == RC_SIGN_IN) {
			mSignInClicked = false;
			mResolvingConnectionFailure = false;
			if (response == Activity.RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				BaseGameUtils.showActivityResultError(this, request, response, R.string.signin_other_error);
			}
		} else if (request == RC_LOOK_AT_MATCHES) {
			// Returning from the 'Select Match' dialog
			setContentView(R.layout.game_manager);
			setViewVisibility();

			if (response != Activity.RESULT_OK) {
				// user canceled
				return;
			}

			TurnBasedMatch match = data
					.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

			if (match != null) {
				updateMatch(match);
			}

			Log.d(TAG, "Match = " + match);
		} else if (request == RC_SELECT_PLAYERS) {
			// Returned from 'Select players to Invite' dialog

			if (response != Activity.RESULT_OK) {
				// user canceled
				return;
			}

			// get the invitee list
			final ArrayList<String> invitees = data
					.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

			// get automatch criteria
			Bundle autoMatchCriteria = null;

			int minAutoMatchPlayers = data.getIntExtra(
					Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
			int maxAutoMatchPlayers = data.getIntExtra(
					Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

			if (minAutoMatchPlayers > 0) {
				autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
						minAutoMatchPlayers, maxAutoMatchPlayers, 0);
			} else {
				autoMatchCriteria = null;
			}

			TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
					.addInvitedPlayers(invitees)
					.setAutoMatchCriteria(autoMatchCriteria).build();


			Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(
					new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
						@Override
						public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
							processResult(result);
						}
					});

		}
	}

	// startMatch() happens in response to the createTurnBasedMatch()
	// above. This is only called on success, so we should have a
	// valid match object. We're taking this opportunity to setup the
	// game, saving our initial state.
	public void startMatch(TurnBasedMatch match) {


		//fetch multiplayer parameters
		catandroidTurn = new CatandroidTurn();

		mMatch = match;

		String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
		String myParticipantId = mMatch.getParticipantId(playerId);
		ArrayList<String> gameParticipantIds = mMatch.getParticipantIds();

		//Start the match based on old game logic - only single player

		boolean blankName = false;
		Participant thisPlayer;
		String name;
		boolean isHuman = true;

		for (int i = 0; i < gameParticipantIds.size(); i++) {
			thisPlayer = mMatch.getParticipant(gameParticipantIds.get(i));
			name = thisPlayer.getDisplayName();

			names[i] = name;

			boolean ai = true;

			if(gameParticipantIds.size() == 3 && ai) {
				if (ai && (i == 1 || i == 2)) {
					types[i] = !isHuman;
				} else{
					types[i] = isHuman;
				}
			} else if(gameParticipantIds.size() == 4 && ai){
				if (ai && (i == 1 || i == 2 || i == 3)) {
					types[i] = !isHuman;
				} else {
					types[i] = isHuman;
				}
			} else {
				types[i] = isHuman;
			}

		}

		Spinner boardSizeSpinner = (Spinner) findViewById(R.id.option_board_size);
		int boardSelected = boardSizeSpinner.getSelectedItemPosition();

		save(((CatAndroidApp) getApplicationContext()).getAppSettingsInstance());

		Spinner pointSpinner = (Spinner) findViewById(R.id.option_max_points);
		int maxPoints = pointSpinner.getSelectedItemPosition() + 5;

		CheckBox discardCheck = (CheckBox) findViewById(R.id.auto_discard);
		boolean autoDiscard = discardCheck.isChecked();


		BoardGeometry boardGeometry = new BoardGeometry(boardSelected);

		activeGameFragment = new ActiveGameFragment();
		activeGameFragment.setmListener(this);
		activeGameFragment.setMyParticipantId(myParticipantId);

		//@TODO
		//add the list of playerIds for the game and the number of players based on view

		Board board = new Board(gameParticipantIds, names, types, maxPoints, boardGeometry,
				autoDiscard, activeGameFragment);
		activeGameFragment.setBoard(board);

		Gson gson = new Gson();
		String serializedBoard = gson.toJson(board);

		catandroidTurn.currentBoard = board;

		Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
				catandroidTurn.persist(), gameParticipantIds.get(0));

		currentMatchId = match.getMatchId();

		setGameplayUI();

	}

	// If you choose to rematch, then call it and wait for a response.
	public void rematch() {
		showSpinner();
		Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
				new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
					@Override
					public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
						processResult(result);
					}
				});
		mMatch = null;
		isDoingTurn = false;
	}


	//Listener that we use to call endTurn from within the gameFragment.
	@Override
	public void endTurn(String nextParticipantId, boolean isWinner) {
		if(isWinner){
			//set winner list
			int winnerIndex = mMatch.getParticipantIds().indexOf(nextParticipantId);
			int numberPlayers = mMatch.getParticipantIds().size();
			ArrayList<ParticipantResult> results = new ArrayList<>();
			for(int i = 0; i < numberPlayers; i++){
				if(i == winnerIndex){
					results.add(new ParticipantResult(mMatch.getParticipantIds().get(i), ParticipantResult.MATCH_RESULT_LOSS, ParticipantResult.PLACING_UNINITIALIZED));
				} else {
					results.add(new ParticipantResult(mMatch.getParticipantIds().get(i), ParticipantResult.MATCH_RESULT_WIN, ParticipantResult.PLACING_UNINITIALIZED));
				}
			}
			Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId(),catandroidTurn.persist(),results);

		} else {
			Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
					catandroidTurn.persist(), nextParticipantId);
		}
	}

	// This is the main function that gets called when players choose a match
	// from the inbox, or else create a match and want to start it.
	public void updateMatch(TurnBasedMatch match) {
		mMatch = match;
		catandroidTurn = new CatandroidTurn();

		String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
		String myParticipantId = mMatch.getParticipantId(playerId);

		activeGameFragment = new ActiveGameFragment();
		activeGameFragment.setmListener(this);
		activeGameFragment.setMyParticipantId(myParticipantId);

		int status = match.getStatus();
		int turnStatus = match.getTurnStatus();

		switch (status) {
			case TurnBasedMatch.MATCH_STATUS_CANCELED:
				showWarning("Canceled!", "This game was canceled!");
				return;
			case TurnBasedMatch.MATCH_STATUS_EXPIRED:
				showWarning("Expired!", "This game is expired.  So sad!");
				return;
			case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
				showWarning("Waiting for auto-match...",
						"We're still waiting for an automatch partner.");
				return;
			case TurnBasedMatch.MATCH_STATUS_COMPLETE:
				if (turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) {
					showWarning(
							"Complete!",
							"This game is over; someone finished it, and so did you!  There is nothing to be done.");
					break;
				}

				// Note that in this state, you must still call "Finish" yourself,
				// so we allow this to continue.
				showWarning("Complete!",
						"This game is over; someone finished it!  You can only finish it now.");
		}

		// OK, it's active. Check on turn pager_title_strip.
		switch (turnStatus) {
			case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
				//fetch the board state from unpersist and set board
				Board board = CatandroidTurn.unpersist(mMatch.getData());
				catandroidTurn.currentBoard = board;
				activeGameFragment.setBoard(board);
				board.reinitBoardOnDependents();
				board.setActiveGameFragment(activeGameFragment);
				currentMatchId = match.getMatchId();
				setGameplayUI();
				break;
			case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
				showWarning("Catandroid",
						"Loaded ongoing game");
				break;
		}

	}

	private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
		dismissSpinner();

		if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
			return;
		}

		isDoingTurn = false;

		showWarning("Match",
				"This match is canceled.  All other players will have their game ended.");
	}

	private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
		TurnBasedMatch match = result.getMatch();

		if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
			return;
		}

		if (match.getData() != null) {
			// This is a game that has already started, so I'll just start
			updateMatch(match);
			return;
		}

		startMatch(match);
	}


	private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
		TurnBasedMatch match = result.getMatch();
		dismissSpinner();
		if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
			return;
		}
		isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
		showWarning("Left", "You've left this match.");
	}


	public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
		TurnBasedMatch match = result.getMatch();

		if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
			return;
		}
		if (match.canRematch()) {
			askForRematch();
		}

		isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

		if (isDoingTurn) {
			updateMatch(match);
			return;
		}

		setViewVisibility();
	}

	// Handle notification events.
	@Override
	public void onInvitationReceived(Invitation invitation) {
		Toast.makeText(
				this,
				"An invitation has arrived from "
						+ invitation.getInviter().getDisplayName(), Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onInvitationRemoved(String invitationId) {
		Toast.makeText(this, "An invitation was removed.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTurnBasedMatchReceived(TurnBasedMatch match) {
		Toast.makeText(this, "A match was updated.", Toast.LENGTH_SHORT).show();
		//when we are in a game and an update is made to it, we call upda
		if(match.getMatchId().equals(currentMatchId)){
			String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
			String myParticipantId = mMatch.getParticipantId(playerId);
			catandroidTurn.unpersist(match.getData());
			activeGameFragment.setBoard(catandroidTurn.currentBoard);
			activeGameFragment.setmListener(this);
			activeGameFragment.setMyParticipantId(myParticipantId);
			activeGameFragment.updateBoardState();
		}

	}

	@Override
	public void onTurnBasedMatchRemoved(String matchId) {
		Toast.makeText(this, "A match was removed.", Toast.LENGTH_SHORT).show();

	}

	public void showErrorMessage(TurnBasedMatch match, int statusCode,
								 int stringId) {

		showWarning("Warning", getResources().getString(stringId));
	}

	// Returns false if something went wrong, probably. This should handle
	// more cases, and probably report more accurate results.
	private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
		switch (statusCode) {
			case GamesStatusCodes.STATUS_OK:
				return true;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
				// This is OK; the action is stored by Google Play Services and will
				// be dealt with later.
				Toast.makeText(
						this,
						"Stored action for later.  (Please remove this toast before release.)",
						Toast.LENGTH_SHORT).show();
				// NOTE: This toast is for informative reasons only; please remove
				// it from your final application.
				return true;
			case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
				showErrorMessage(match, statusCode,
						R.string.status_multiplayer_error_not_trusted_tester);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
				showErrorMessage(match, statusCode,
						R.string.match_error_already_rematched);
				break;
			case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
				showErrorMessage(match, statusCode,
						R.string.network_error_operation_failed);
				break;
			case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
				showErrorMessage(match, statusCode,
						R.string.client_reconnect_required);
				break;
			case GamesStatusCodes.STATUS_INTERNAL_ERROR:
				showErrorMessage(match, statusCode, R.string.internal_error);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
				showErrorMessage(match, statusCode,
						R.string.match_error_inactive_match);
				break;
			case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
				showErrorMessage(match, statusCode,
						R.string.match_error_locally_modified);
				break;
			default:
				showErrorMessage(match, statusCode, R.string.unexpected_status);
				Log.d(TAG, "Did not have warning or string to deal with: "
						+ statusCode);
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.sign_in_button:
				// Check to see the developer who's running this sample code read the instructions :-)
				// NOTE: this check is here only because this is a sample! Don't include this
				// check in your actual production app.
				if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
					Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
				}

				mSignInClicked = true;
				mTurnBasedMatch = null;
				findViewById(R.id.sign_in_button).setVisibility(View.GONE);
				mGoogleApiClient.connect();
				break;
			case R.id.sign_out_button:
				mSignInClicked = false;
				Games.signOut(mGoogleApiClient);
				if (mGoogleApiClient.isConnected()) {
					mGoogleApiClient.disconnect();
				}
				setViewVisibility();
				break;
		}
	}
}
