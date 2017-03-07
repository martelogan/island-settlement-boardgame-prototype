package com.catandroid.app.common.ui.graphics_controllers;

import java.util.Hashtable;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLUtils;

import com.catandroid.app.common.components.BoardGeometry;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Harbor;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.ui.resources.Square;
import com.catandroid.app.common.ui.resources.UIButton;
import com.catandroid.app.R;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.players.Player;

public class TextureManager {

	private enum TextureType {
		NONE, HEX_COAST, HEX_TERRAIN, HEX_ROBBER, HEX_ACTIVE,
		HARBOR, RESOURCE, NUMBER_TOKEN, ROAD, SETTLEMENT, CITY, BUTTON_BG, BUTTON
	}

	private Hashtable<Integer, Bitmap> bitmap;
	private Hashtable<Integer, Integer> resource;
	private Hashtable<Integer, Square> square;

    public void initGL(GL10 gl) {
        for (Integer key : bitmap.keySet()) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, resource.get(key));

            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                    GL10.GL_LINEAR);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                    GL10.GL_LINEAR);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                    GL10.GL_REPEAT);
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                    GL10.GL_REPEAT);
            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                    GL10.GL_MODULATE);

            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap.get(key), 0);
        }
    }

	public TextureManager(Resources res) {
		// initialize hash table
		bitmap = new Hashtable<Integer, Bitmap>();
		resource = new Hashtable<Integer, Integer>();
		square = new Hashtable<Integer, Square>();

		// load hex terrain textures
		add(TextureType.HEX_COAST, 0, R.drawable.hex_coast, res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.DESERT.ordinal(), R.drawable.hex_desert,
				res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.PASTURE.ordinal(), R.drawable.hex_wool, res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.FIELDS.ordinal(), R.drawable.hex_grain, res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.FOREST.ordinal(), R.drawable.hex_lumber,
				res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.HILLS.ordinal(), R.drawable.hex_brick, res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.MOUNTAINS.ordinal(), R.drawable.hex_ore, res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.SEA.ordinal(), R.drawable.hex_sea, res);
		add(TextureType.HEX_TERRAIN, Hexagon.TerrainType.GOLD_FIELD.ordinal(), R.drawable.hex_gold, res);
		add(TextureType.HEX_ACTIVE, 0, R.drawable.hex_active, res);

        // load large resource icons
        add(TextureType.RESOURCE, Resource.ResourceType.LUMBER.ordinal(),
                R.drawable.resource_lumber, res);
        add(TextureType.RESOURCE, Resource.ResourceType.WOOL.ordinal(), R.drawable.resource_wool,
                res);
        add(TextureType.RESOURCE, Resource.ResourceType.GRAIN.ordinal(), R.drawable.resource_grain,
                res);
        add(TextureType.RESOURCE, Resource.ResourceType.BRICK.ordinal(), R.drawable.resource_brick,
                res);
        add(TextureType.RESOURCE, Resource.ResourceType.ORE.ordinal(), R.drawable.resource_ore, res);
        add(TextureType.RESOURCE, Resource.ResourceType.ANY.ordinal(), R.drawable.harbor_3to1,
                res);

        // load large settlement textures
        add(TextureType.SETTLEMENT, Player.Color.SELECT.ordinal(),
                R.drawable.settlement_grey, res);
        add(TextureType.SETTLEMENT, Player.Color.RED.ordinal(), R.drawable.settlement_r,
                res);
        add(TextureType.SETTLEMENT, Player.Color.BLUE.ordinal(),
                R.drawable.settlement_b, res);
        add(TextureType.SETTLEMENT, Player.Color.GREEN.ordinal(),
                R.drawable.settlement_grn, res);
        add(TextureType.SETTLEMENT, Player.Color.YELLOW.ordinal(),
                R.drawable.settlement_y, res);

        // load large city textures
        add(TextureType.CITY, Player.Color.SELECT.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.CITY, Player.Color.RED.ordinal(), R.drawable.city_r, res);
        add(TextureType.CITY, Player.Color.BLUE.ordinal(), R.drawable.city_b, res);
        add(TextureType.CITY, Player.Color.GREEN.ordinal(), R.drawable.city_grn, res);
        add(TextureType.CITY, Player.Color.YELLOW.ordinal(), R.drawable.city_y,
                res);

        // load robber texture
        add(TextureType.HEX_ROBBER, 0, R.drawable.hex_robber, res);

        // load harbor textures
        add(TextureType.HARBOR, Harbor.Position.NORTH.ordinal(),
                R.drawable.harbor_north, res);
        add(TextureType.HARBOR, Harbor.Position.SOUTH.ordinal(),
                R.drawable.harbor_south, res);
        add(TextureType.HARBOR, Harbor.Position.NORTHEAST.ordinal(),
                R.drawable.harbor_northeast, res);
        add(TextureType.HARBOR, Harbor.Position.NORTHWEST.ordinal(),
                R.drawable.harbor_northwest, res);
        add(TextureType.HARBOR, Harbor.Position.SOUTHEAST.ordinal(),
                R.drawable.harbor_southeast, res);
        add(TextureType.HARBOR, Harbor.Position.SOUTHWEST.ordinal(),
                R.drawable.harbor_southwest, res);

        // load road texture
        add(TextureType.ROAD, 0, R.drawable.edge_unit, res);

		// load number token textures
		add(TextureType.NUMBER_TOKEN, 2, R.drawable.num_2, res);
		add(TextureType.NUMBER_TOKEN, 3, R.drawable.num_3, res);
		add(TextureType.NUMBER_TOKEN, 4, R.drawable.num_4, res);
		add(TextureType.NUMBER_TOKEN, 5, R.drawable.num_5, res);
		add(TextureType.NUMBER_TOKEN, 6, R.drawable.num_6, res);
		add(TextureType.NUMBER_TOKEN, 8, R.drawable.num_8, res);
		add(TextureType.NUMBER_TOKEN, 9, R.drawable.num_9, res);
		add(TextureType.NUMBER_TOKEN, 10, R.drawable.num_10, res);
		add(TextureType.NUMBER_TOKEN, 11, R.drawable.num_11, res);
		add(TextureType.NUMBER_TOKEN, 12, R.drawable.num_12, res);

		// load button textures
		add(TextureType.BUTTON_BG, UIButton.ButtonBackground.DEFAULT.ordinal(),
				R.drawable.button_background, res);
		add(TextureType.BUTTON_BG, UIButton.ButtonBackground.PRESSED.ordinal(),
				R.drawable.button_pressed, res);
		add(TextureType.BUTTON, UIButton.ButtonType.PLAYER_STATUS.ordinal(),
				R.drawable.button_player_stats, res);
		add(TextureType.BUTTON, UIButton.ButtonType.DICE_ROLL.ordinal(), R.drawable.button_roll_dice,
				res);
		add(TextureType.BUTTON, UIButton.ButtonType.BUILD_ROAD.ordinal(), R.drawable.button_build_road,
				res);
		add(TextureType.BUTTON, UIButton.ButtonType.BUILD_SETTLEMENT.ordinal(),
				R.drawable.button_build_settlement, res);
		add(TextureType.BUTTON, UIButton.ButtonType.BUILD_CITY.ordinal(), R.drawable.button_build_city,
				res);
		add(TextureType.BUTTON, UIButton.ButtonType.PROGRESS_CARD.ordinal(),
				R.drawable.button_progress_cards, res);
		add(TextureType.BUTTON, UIButton.ButtonType.TRADE.ordinal(),
				R.drawable.button_init_trade, res);
		add(TextureType.BUTTON, UIButton.ButtonType.END_TURN.ordinal(),
				R.drawable.button_end_turn, res);
		add(TextureType.BUTTON, UIButton.ButtonType.CANCEL.ordinal(),
				R.drawable.button_cancel_action, res);
	}

    public void drawButton(UIButton button, GL10 gl) {
        float factor = 2 * BoardGeometry.HEX_PNG_SCALE / BoardGeometry.BUTTON_PNG_SCALE;

        gl.glPushMatrix();
        gl.glTranslatef(button.getX(), button.getY(), 10);
        gl.glScalef(button.getWidth() * factor, button.getHeight() * factor, 1);

        square.get(hash(TextureType.BUTTON_BG, UIButton.ButtonBackground.DEFAULT.ordinal())).render(gl);

        if (button.isPressed())
            square.get(hash(TextureType.BUTTON_BG, UIButton.ButtonBackground.PRESSED.ordinal())).render(gl);

        square.get(hash(TextureType.BUTTON, button.getButtonType().ordinal())).render(gl);

//		if (!button.isEnabled())
//			square.get(hash(ResourceType.BUTTON_BG, UIButton.ButtonBackground.ACTIVATED.ordinal())).render(gl);

        gl.glPopMatrix();
    }


    public void drawHexTerrain(Hexagon hexagon, GL10 gl, BoardGeometry boardGeometry) {
        gl.glPushMatrix();

        int id = hexagon.getId();
        gl.glTranslatef(boardGeometry.getHexagonX(id), boardGeometry.getHexagonY(id), 0);

        square.get(hash(TextureType.HEX_COAST, 0)).render(gl);
        square.get(hash(TextureType.HEX_TERRAIN, hexagon.getTerrainType().ordinal())).render(gl);

        gl.glPopMatrix();
    }

    public void drawNumTokenOnHex(Hexagon hexagon, GL10 gl, BoardGeometry boardGeometry) {
        gl.glPushMatrix();

        int id = hexagon.getId();
        gl.glTranslatef(boardGeometry.getHexagonX(id), boardGeometry.getHexagonY(id), 0);

        int numToken = hexagon.getNumberTokenAsInt();

        if (numToken != 0 && numToken != 7) {
            gl.glScalef(1.5f, 1.5f, 1);
            square.get(hash(TextureType.NUMBER_TOKEN, numToken)).render(gl);
        }

        gl.glPopMatrix();
    }

    public void drawActiveHex(Hexagon hexagon, GL10 gl, BoardGeometry boardGeometry, int lastRoll) {
        gl.glPushMatrix();

        int id = hexagon.getId();
        gl.glTranslatef(boardGeometry.getHexagonX(id), boardGeometry.getHexagonY(id), 0);

        int numToken = hexagon.getNumberTokenAsInt();

        if (!hexagon.hasRobber() && lastRoll != 0 && numToken == lastRoll)
        {
            square.get(hash(TextureType.HEX_ACTIVE, 0)).render(gl);
        }

        gl.glPopMatrix();
    }

    public void drawRobber(Hexagon hexagon, GL10 gl, BoardGeometry boardGeometry) {
        gl.glPushMatrix();

        int id = hexagon.getId();
        gl.glTranslatef(boardGeometry.getHexagonX(id), boardGeometry.getHexagonY(id), 0);

        if (hexagon.hasRobber())
        {
            square.get(hash(TextureType.HEX_ROBBER, 0)).render(gl);
        }

        gl.glPopMatrix();
    }

    public void drawHarbor(Harbor harbor, GL10 gl, BoardGeometry boardGeometry) {
        int id = harbor.getId();

        // draw shore access notches
        gl.glPushMatrix();
        gl.glTranslatef(boardGeometry.getHarborX(id), boardGeometry.getHarborY(id), 0);
        square.get(hash(TextureType.HARBOR, harbor.getPosition().ordinal()))
                .render(gl);
        gl.glPopMatrix();

        // draw type harbor icon
        gl.glPushMatrix();
        gl.glTranslatef(boardGeometry.getHarborIconX(id, harbor.getEdge()),
                boardGeometry.getHarborIconY(id, harbor.getEdge()), 0);
        square.get(hash(TextureType.RESOURCE, harbor.getResourceType().ordinal())).render(gl);
        gl.glPopMatrix();
    }

    public void drawVertex(Vertex vertex, boolean buildsettlement, boolean buildCity,
                           GL10 gl, BoardGeometry boardGeometry) {

        TextureType textureType = TextureType.NONE;
        if (vertex.getBuilding() == Vertex.CITY || buildCity)
        {
            textureType = TextureType.CITY;
        }
        else if (vertex.getBuilding() == Vertex.SETTLEMENT || buildsettlement)
        {
            textureType = TextureType.SETTLEMENT;
        }

        Player.Color color;
        Player owner = vertex.getOwner();
        if (buildsettlement || buildCity)
        {
            color = Player.Color.SELECT;
        }
        else if (owner != null)
        {
            color = owner.getColor();
        }
        else
        {
            color = Player.Color.NONE;
        }

        Square object = square.get(hash(textureType, color.ordinal()));
        if (object != null) {
            gl.glPushMatrix();
            int id = vertex.getId();
            gl.glTranslatef(boardGeometry.getVertexX(id), boardGeometry.getVertexY(id), TextureType.SETTLEMENT.ordinal());
            object.render(gl);
            gl.glPopMatrix();
        }
    }

    public void drawEdge(Edge edge, boolean build, GL10 gl, BoardGeometry boardGeometry) {
        float[] x = new float[2];
        float[] y = new float[2];
        x[0] = boardGeometry.getVertexX(edge.getV0Clockwise().getId());
        x[1] = boardGeometry.getVertexX(edge.getV1Clockwise().getId());
        y[0] = boardGeometry.getVertexY(edge.getV0Clockwise().getId());
        y[1] = boardGeometry.getVertexY(edge.getV1Clockwise().getId());

        Player owner = edge.getOwnerPlayer();
        float[] color;
        if (owner != null)
        {
            color = getColorArray(getColor(owner.getColor()));
        }
        else
        {
            color = getColorArray(getColor(Player.Color.SELECT));
        }

        float dx = x[1] - x[0];
        float dy = y[1] - y[0];

        gl.glColor4f(color[0], color[1], color[2], color[3]);

        gl.glPushMatrix();

        gl.glTranslatef(boardGeometry.getEdgeX(edge.getId()), boardGeometry.getEdgeY(edge.getId()), TextureType.ROAD.ordinal());
        gl.glRotatef((float) (180 / Math.PI * Math.atan(dy / dx)), 0, 0, 1);

        square.get(hash(TextureType.ROAD, 0)).render(gl);

        gl.glPopMatrix();

        gl.glColor4f(1, 1, 1, 1);
    }

    public Bitmap get(UIButton.ButtonType buttonType) {
        return get(TextureType.BUTTON, buttonType.ordinal());
    }

    public Bitmap get(Resource.ResourceType resourceType) {
        return get(TextureType.RESOURCE, resourceType.ordinal());
    }

	public static int getColor(Player.Color color) {
		switch (color) {
            case RED:
                return Color.rgb(0xBE, 0x28, 0x20);
            case BLUE:
                return Color.rgb(0x37, 0x57, 0xB3);
            case GREEN:
                return Color.rgb(0x13, 0xA6, 0x19);
            case YELLOW:
                return Color.rgb(0xE9, 0xD3, 0x03);
            default:
                return Color.rgb(0x87, 0x87, 0x87);
            }
	}

    public static void setPaintColor(Paint paint, Player.Color color) {
        paint.setColor(getColor(color));
    }

    public static int darken(int color, double factor) {
        return Color.argb(Color.alpha(color),
                (int) (Color.red(color) * factor),
                (int) (Color.green(color) * factor),
                (int) (Color.blue(color) * factor));
    }

	
	public static float[] getColorArray(int color) {
		float[] array = new float[4];
		array[0] = (float) Color.red(color) / 255.0f;
		array[1] = (float) Color.green(color) / 255.0f;
		array[2] = (float) Color.blue(color) / 255.0f;
		array[3] = 1f;
		return array;
	}

    private static int hash(TextureType textureType, int variant) {
        return variant << 6 | textureType.ordinal();
    }

    private Bitmap get(TextureType textureType, int variant) {
        return bitmap.get(hash(textureType, variant));
    }

    private void add(TextureType textureType, int variant, int id, Resources res) {
        int key = hash(textureType, variant);
        Bitmap bitmap = BitmapFactory.decodeResource(res, id, new Options());
        this.bitmap.put(key, bitmap);
        this.resource.put(key, id);
        this.square.put(key, new Square(id, 0, 0, textureType.ordinal(),
                (float) bitmap.getWidth() / (float) BoardGeometry.HEX_PNG_SCALE,
                (float) bitmap.getHeight() / (float) BoardGeometry.HEX_PNG_SCALE));
    }
}
