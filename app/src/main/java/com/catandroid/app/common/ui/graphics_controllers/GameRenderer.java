package com.catandroid.app.common.ui.graphics_controllers;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.util.Log;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.BoardGeometry;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.views.GameView;
import com.catandroid.app.common.ui.resources.Square;

public class GameRenderer implements Renderer {

	public enum Action {
		NONE, SETTLEMENT, CITY, ROAD, ROBBER
	}

	private TextureManager texture;
	private Board board;
	private Player player;
	private int lastRoll;
	private Action action;

	private int width, height;

	private static BoardGeometry boardGeometry = null;
	private int HEX_COUNT;
	private int EDGE_COUNT;
	private int VERTEX_COUNT;
	private int HARBOR_COUNT;

	private static final float[] backgroundColors = { 0, 0.227f, 0.521f, 1,
			0.262f, 0.698f, 0.878f, 1, 0, 0.384f, 0.600f, 1, 0.471f, 0.875f,
			1f, 1 };

	private Square background;
	private GameView view;

	public GameRenderer(GameView gameView, BoardGeometry boardGeometry) {
		view = gameView;

		if (boardGeometry != null) {
			this.boardGeometry = boardGeometry;
			this.HEX_COUNT = boardGeometry.getHexCount();
			this.VERTEX_COUNT = boardGeometry.getVertexCount();
			this.EDGE_COUNT = boardGeometry.getEdgeCount();
			this.HARBOR_COUNT = boardGeometry.getHarborCount();
		}

		action = Action.NONE;
	}

	public void setState(Board board, Player player, TextureManager texture, int lastRoll) {
		this.texture = texture;
		this.board = board;
		this.player = player;
		this.lastRoll = lastRoll;
	}

	public void setSize(DisplayMetrics screen, int width, int height) {
		boardGeometry.setCurFocus(width, height);
		this.width = width;
		this.height = height;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}
	
	public BoardGeometry getGeometry() {
		return boardGeometry;
	}

	public boolean cancel() {
		// TODO: cancel intermediate interactions

		return ((board.isProduction() || board.isBuild()) && action != Action.NONE);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_DST_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		texture.initGL(gl);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;

		boardGeometry.setCurFocus(width, height);

		float aspect = (float) width / (float) height;
		if (width > height)
			background = new Square(backgroundColors, 0, 0, 0, 2 * aspect, 2);
		else
			background = new Square(backgroundColors, 0, 0, 0, 2, 2 / aspect);

		gl.glViewport(0, 0, width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		try {
			gl.glColor4f(1, 1, 1, 1);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			float aspect = (float) width / (float) height;
			if (width > height)
			{
				gl.glOrthof(-aspect, aspect, -1, 1, 0.1f, 40f);
			}
            else
			{
				gl.glOrthof(-1, 1, -1 / aspect, 1 / aspect, 0.1f, 40f);
			}

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();

			gl.glTranslatef(0, 0, -30);

			// draw background without transformation
			background.render(gl);

			gl.glTranslatef(-boardGeometry.getTranslateX(), -boardGeometry.getTranslateY(), 0);
			gl.glScalef(boardGeometry.getZoom(), boardGeometry.getZoom(), 1);

			// draw the hexangons with backdrop
			for (int i = 0; i < HEX_COUNT; i++)
			{
				texture.drawHexTerrain(board.getHexagonById(i), gl, boardGeometry);
			}

			// draw the executeDiceRoll numbers, robber, and highlighting
			for (int i = 0; i < HEX_COUNT; i++)
			{
				texture.drawRobber(board.getHexagonById(i), gl, boardGeometry);
			}
			for (int i = 0; i < HEX_COUNT; i++)
			{
				texture.drawActiveHex(board.getHexagonById(i), gl, boardGeometry, lastRoll);
			}
			for (int i = 0; i < HEX_COUNT; i++)
			{
				texture.drawNumTokenOnHex(board.getHexagonById(i), gl, boardGeometry);
			}

			// draw harbors
			for (int i = 0; i < HARBOR_COUNT; i++)
			{
				texture.drawHarbor(board.getHarborById(i), gl, boardGeometry);
			}

			// draw edges
			for (int i = 0; i < EDGE_COUNT; i++) {
                Edge edge = board.getEdgeById(i);
                boolean build = action == Action.ROAD && player != null
                        && player.canBuild(edge);

                if (build || edge.getOwnerPlayer() != null)
				{
					texture.drawEdge(edge, build, gl, boardGeometry);
				}
            }

			// draw vertices
			for (int i = 0; i < VERTEX_COUNT; i++) {
                Vertex vertex = board.getVertexById(i);
                boolean settlement = player != null && action == Action.SETTLEMENT
                        && player.canBuild(vertex, Vertex.SETTLEMENT);
                boolean city = player != null && action == Action.CITY
                        && player.canBuild(vertex, Vertex.CITY);

                texture.drawVertex(vertex, settlement, city, gl, boardGeometry);
            }

			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glOrthof(0, width, 0, height, 0.1f, 40f);
			gl.glTranslatef(0, 0, -30);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();

			// draw the buttons
			view.placeButtons(width, height);
			view.drawButtons(texture, gl);
		} catch (Exception e) {
			Log.e("Renderer Exception", e.toString());
		}
	}
}
