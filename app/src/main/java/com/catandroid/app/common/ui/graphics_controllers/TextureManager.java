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
import com.catandroid.app.common.components.board_pieces.Knight;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Harbor;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.hashing_utils.HashingUtils;
import com.catandroid.app.common.ui.resources.Square;
import com.catandroid.app.common.ui.resources.UIButton;
import com.catandroid.app.R;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.players.Player;

public class TextureManager {

    /* IMPORTANT: here, the ordering of these constants designates the depth
    * at which they are drawn (ie. increasing depth left -> right => layered
    * progressively more towards the top). In particular, the ordering from
    * NONE -> CITY must be preserved for proper behaviour. Afterwards, we render
    * button just on top (layer CITY + 1) and all remaining png's at the city layer
    */
	private enum TextureType {
		NONE, HEX_COAST, HEX_TERRAIN, HEX_ROBBER, HEX_ACTIVE,
		HARBOR, RESOURCE, NUMBER_TOKEN, ROAD, SETTLEMENT, CITY, CITY_WALL, 
		TRADE_METROPOLIS, SCIENCE_METROPOLIS, POLITICS_METROPOLIS, 
		WALLED_TRADE_METROPOLIS, WALLED_SCIENCE_METROPOLIS, WALLED_POLITICS_METROPOLIS, 
		SHIP, BASIC_KNIGHT_INACTIVE, STRONG_KNIGHT_INACTIVE, MIGHTY_KNIGHT_INACTIVE,
        BASIC_KNIGHT_ACTIVE, STRONG_KNIGHT_ACTIVE, MIGHTY_KNIGHT_ACTIVE, BUTTON_BG, BUTTON
	}

	private Hashtable<Long, Bitmap> bitmap;
	private Hashtable<Long, Integer> resource;
	private Hashtable<Long, Square> square;

    public void initGL(GL10 gl) {
        for (Long key : bitmap.keySet()) {
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
		bitmap = new Hashtable<Long, Bitmap>();
		resource = new Hashtable<Long, Integer>();
		square = new Hashtable<Long, Square>();

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
        add(TextureType.SETTLEMENT, Player.Color.SELECTING.ordinal(),
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
        add(TextureType.CITY, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.CITY, Player.Color.RED.ordinal(), R.drawable.city_r, res);
        add(TextureType.CITY, Player.Color.BLUE.ordinal(), R.drawable.city_b, res);
        add(TextureType.CITY, Player.Color.GREEN.ordinal(), R.drawable.city_grn, res);
        add(TextureType.CITY, Player.Color.YELLOW.ordinal(), R.drawable.city_y,
                res);

        //load large city wall textures
        //@TODO ADD PNG RESOURCES FOR BUILD_CITY_WALL
        add(TextureType.CITY_WALL, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.CITY_WALL, Player.Color.RED.ordinal(), R.drawable.walled_city_r, res);
        add(TextureType.CITY_WALL, Player.Color.BLUE.ordinal(), R.drawable.walled_city_b, res);
        add(TextureType.CITY_WALL, Player.Color.GREEN.ordinal(), R.drawable.walled_city_grn, res);
        add(TextureType.CITY_WALL, Player.Color.YELLOW.ordinal(), R.drawable.walled_city_y,
                res);

        //load large inactive basic knight textures
        //@TODO ADD CORRECT PNG RESOURCES FOR INACTIVE BASIC KNIGHT
        add(TextureType.BASIC_KNIGHT_INACTIVE, Player.Color.SELECTING.ordinal(), R.drawable.knight_basic_grey,
                res);
        add(TextureType.BASIC_KNIGHT_INACTIVE, Player.Color.RED.ordinal(), R.drawable.knight_basic_r_inactive, res);
        add(TextureType.BASIC_KNIGHT_INACTIVE, Player.Color.BLUE.ordinal(), R.drawable.knight_basic_b_inactive, res);
        add(TextureType.BASIC_KNIGHT_INACTIVE, Player.Color.GREEN.ordinal(), R.drawable.knight_basic_grn_inactive, res);
        add(TextureType.BASIC_KNIGHT_INACTIVE, Player.Color.YELLOW.ordinal(), R.drawable.knight_basic_y_inactive,
                res);

        //load large inactive strong knight textures
        //@TODO ADD CORRECT PNG RESOURCES FOR INACTIVE STRONG KNIGHT
        add(TextureType.STRONG_KNIGHT_INACTIVE, Player.Color.SELECTING.ordinal(), R.drawable.knight_strong_grey,
                res);
        add(TextureType.STRONG_KNIGHT_INACTIVE, Player.Color.RED.ordinal(), R.drawable.knight_strong_r_inactive, res);
        add(TextureType.STRONG_KNIGHT_INACTIVE, Player.Color.BLUE.ordinal(), R.drawable.knight_strong_b_inactive, res);
        add(TextureType.STRONG_KNIGHT_INACTIVE, Player.Color.GREEN.ordinal(), R.drawable.knight_strong_grn_inactive, res);
        add(TextureType.STRONG_KNIGHT_INACTIVE, Player.Color.YELLOW.ordinal(), R.drawable.knight_strong_y_inactive,
                res);

        //load large inactive mighty knight textures
        //@TODO ADD CORRECT PNG RESOURCES FOR INACTIVE MIGHTY KNIGHT
        add(TextureType.MIGHTY_KNIGHT_INACTIVE, Player.Color.SELECTING.ordinal(), R.drawable.knight_mighty_grey,
                res);
        add(TextureType.MIGHTY_KNIGHT_INACTIVE, Player.Color.RED.ordinal(), R.drawable.knight_mighty_r_inactive, res);
        add(TextureType.MIGHTY_KNIGHT_INACTIVE, Player.Color.BLUE.ordinal(), R.drawable.knight_mighty_b_inactive, res);
        add(TextureType.MIGHTY_KNIGHT_INACTIVE, Player.Color.GREEN.ordinal(), R.drawable.knight_mighty_grn_inactive, res);
        add(TextureType.MIGHTY_KNIGHT_INACTIVE, Player.Color.YELLOW.ordinal(), R.drawable.knight_mighty_y_inactive,
                res);

        //load large active basic knight textures
        //@TODO ADD CORRECT PNG RESOURCES FOR ACTIVE BASIC KNIGHT
        add(TextureType.BASIC_KNIGHT_ACTIVE, Player.Color.SELECTING.ordinal(), R.drawable.knight_basic_grey,
                res);
        add(TextureType.BASIC_KNIGHT_ACTIVE, Player.Color.RED.ordinal(), R.drawable.knight_basic_r_active, res);
        add(TextureType.BASIC_KNIGHT_ACTIVE, Player.Color.BLUE.ordinal(), R.drawable.knight_basic_b_active, res);
        add(TextureType.BASIC_KNIGHT_ACTIVE, Player.Color.GREEN.ordinal(), R.drawable.knight_basic_grn_active, res);
        add(TextureType.BASIC_KNIGHT_ACTIVE, Player.Color.YELLOW.ordinal(), R.drawable.knight_basic_y_active,
                res);

        //load large active strong knight textures
        //@TODO ADD CORRECT PNG RESOURCES FOR ACTIVE STRONG KNIGHT
        add(TextureType.STRONG_KNIGHT_ACTIVE, Player.Color.SELECTING.ordinal(), R.drawable.knight_strong_grey,
                res);
        add(TextureType.STRONG_KNIGHT_ACTIVE, Player.Color.RED.ordinal(), R.drawable.knight_strong_r_active, res);
        add(TextureType.STRONG_KNIGHT_ACTIVE, Player.Color.BLUE.ordinal(), R.drawable.knight_strong_b_active, res);
        add(TextureType.STRONG_KNIGHT_ACTIVE, Player.Color.GREEN.ordinal(), R.drawable.knight_strong_grn_active, res);
        add(TextureType.STRONG_KNIGHT_ACTIVE, Player.Color.YELLOW.ordinal(), R.drawable.knight_strong_y_active,
                res);

        //load large active mighty knight textures
        //@TODO ADD CORRECT PNG RESOURCES FOR ACTIKVE MIGHTY KNIGHT
        add(TextureType.MIGHTY_KNIGHT_ACTIVE, Player.Color.SELECTING.ordinal(), R.drawable.knight_mighty_grey,
                res);
        add(TextureType.MIGHTY_KNIGHT_ACTIVE, Player.Color.RED.ordinal(), R.drawable.knight_mighty_r_active, res);
        add(TextureType.MIGHTY_KNIGHT_ACTIVE, Player.Color.BLUE.ordinal(), R.drawable.knight_mighty_b_active, res);
        add(TextureType.MIGHTY_KNIGHT_ACTIVE, Player.Color.GREEN.ordinal(), R.drawable.knight_mighty_grn_active, res);
        add(TextureType.MIGHTY_KNIGHT_ACTIVE, Player.Color.YELLOW.ordinal(), R.drawable.knight_mighty_y_active,
                res);

        //load large TRADEmetropolis textures
        //@TODO ADD RESOURCES FOR BUILD_CITY_WALL
        add(TextureType.TRADE_METROPOLIS, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.TRADE_METROPOLIS, Player.Color.RED.ordinal(), R.drawable.trade_metropolis_r, res);
        add(TextureType.TRADE_METROPOLIS, Player.Color.BLUE.ordinal(), R.drawable.trade_metropolis_b, res);
        add(TextureType.TRADE_METROPOLIS, Player.Color.GREEN.ordinal(), R.drawable.trade_metropolis_grn, res);
        add(TextureType.TRADE_METROPOLIS, Player.Color.YELLOW.ordinal(), R.drawable.trade_metropolis_y,
                res);

        //load large SCIENCEmetropolis textures
        //@TODO ADD RESOURCES FOR BUILD_CITY_WALL
        add(TextureType.SCIENCE_METROPOLIS, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.SCIENCE_METROPOLIS, Player.Color.RED.ordinal(), R.drawable.science_metropolis_r, res);
        add(TextureType.SCIENCE_METROPOLIS, Player.Color.BLUE.ordinal(), R.drawable.science_metropolis_b, res);
        add(TextureType.SCIENCE_METROPOLIS, Player.Color.GREEN.ordinal(), R.drawable.science_metropolis_grn, res);
        add(TextureType.SCIENCE_METROPOLIS, Player.Color.YELLOW.ordinal(), R.drawable.science_metropolis_y,
                res);

        //load large POLITICSmetropolis textures
        //@TODO ADD RESOURCES FOR BUILD_CITY_WALL
        add(TextureType.POLITICS_METROPOLIS, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.POLITICS_METROPOLIS, Player.Color.RED.ordinal(), R.drawable.politics_metropolis_r, res);
        add(TextureType.POLITICS_METROPOLIS, Player.Color.BLUE.ordinal(), R.drawable.politics_metropolis_b, res);
        add(TextureType.POLITICS_METROPOLIS, Player.Color.GREEN.ordinal(), R.drawable.politics_metropolis_grn, res);
        add(TextureType.POLITICS_METROPOLIS, Player.Color.YELLOW.ordinal(), R.drawable.politics_metropolis_y,
                res);

        //load large WALLED_TRADEmetropolis textures
        //@TODO ADD RESOURCES FOR BUILD_CITY_WALL
        add(TextureType.WALLED_TRADE_METROPOLIS, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.WALLED_TRADE_METROPOLIS, Player.Color.RED.ordinal(), R.drawable.walled_trade_metropolis_r, res);
        add(TextureType.WALLED_TRADE_METROPOLIS, Player.Color.BLUE.ordinal(), R.drawable.walled_trade_metropolis_b, res);
        add(TextureType.WALLED_TRADE_METROPOLIS, Player.Color.GREEN.ordinal(), R.drawable.walled_trade_metropolis_grn, res);
        add(TextureType.WALLED_TRADE_METROPOLIS, Player.Color.YELLOW.ordinal(), R.drawable.walled_trade_metropolis_y,
                res);

        //load large WALLED_SCIENCEmetropolis textures
        //@TODO ADD RESOURCES FOR BUILD_CITY_WALL
        add(TextureType.WALLED_SCIENCE_METROPOLIS, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.WALLED_SCIENCE_METROPOLIS, Player.Color.RED.ordinal(), R.drawable.walled_science_metropolis_r, res);
        add(TextureType.WALLED_SCIENCE_METROPOLIS, Player.Color.BLUE.ordinal(), R.drawable.walled_science_metropolis_b, res);
        add(TextureType.WALLED_SCIENCE_METROPOLIS, Player.Color.GREEN.ordinal(), R.drawable.walled_science_metropolis_grn, res);
        add(TextureType.WALLED_SCIENCE_METROPOLIS, Player.Color.YELLOW.ordinal(), R.drawable.walled_science_metropolis_y,
                res);

        //load large WALLED_POLITICSmetropolis textures
        //@TODO ADD RESOURCES FOR BUILD_CITY_WALL
        add(TextureType.WALLED_POLITICS_METROPOLIS, Player.Color.SELECTING.ordinal(), R.drawable.city_grey,
                res);
        add(TextureType.WALLED_POLITICS_METROPOLIS, Player.Color.RED.ordinal(), R.drawable.walled_politics_metropolis_r, res);
        add(TextureType.WALLED_POLITICS_METROPOLIS, Player.Color.BLUE.ordinal(), R.drawable.walled_politics_metropolis_b, res);
        add(TextureType.WALLED_POLITICS_METROPOLIS, Player.Color.GREEN.ordinal(), R.drawable.walled_politics_metropolis_grn, res);
        add(TextureType.WALLED_POLITICS_METROPOLIS, Player.Color.YELLOW.ordinal(), R.drawable.walled_politics_metropolis_y,
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
        add(TextureType.ROAD, 0, R.drawable.edge_unit_road, res);

        //TODO: adapt graphics to incorporate ship
        // load ship texture
        add(TextureType.SHIP, 0, R.drawable.edge_unit_ship, res);

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
        add(TextureType.BUTTON, UIButton.ButtonType.VIEW_BARBARIANS.ordinal(),
                R.drawable.button_barbarian_progress, res);
		add(TextureType.BUTTON, UIButton.ButtonType.DICE_ROLL.ordinal(), R.drawable.button_roll_dice,
				res);
		add(TextureType.BUTTON, UIButton.ButtonType.BUILD_ROAD.ordinal(), R.drawable.button_build_road,
				res);
        add(TextureType.BUTTON, UIButton.ButtonType.BUILD_SHIP.ordinal(), R.drawable.button_build_ship,
                res);
        add(TextureType.BUTTON, UIButton.ButtonType.MOVE_SHIP.ordinal(), R.drawable.button_move_ship,
                res);
		add(TextureType.BUTTON, UIButton.ButtonType.BUILD_SETTLEMENT.ordinal(),
				R.drawable.button_build_settlement, res);
		add(TextureType.BUTTON, UIButton.ButtonType.BUILD_CITY.ordinal(), R.drawable.button_build_city,
				res);
		add(TextureType.BUTTON, UIButton.ButtonType.PLAY_PROGRESS_CARD.ordinal(),
				R.drawable.button_progress_cards, res);
		add(TextureType.BUTTON, UIButton.ButtonType.TRADE.ordinal(),
				R.drawable.button_init_trade, res);
		add(TextureType.BUTTON, UIButton.ButtonType.END_TURN.ordinal(),
				R.drawable.button_end_turn, res);
		add(TextureType.BUTTON, UIButton.ButtonType.CANCEL_ACTION.ordinal(),
				R.drawable.button_cancel_action, res);
        add(TextureType.BUTTON, UIButton.ButtonType.BUILD_CITY_WALL.ordinal(),
                R.drawable.button_build_wall, res);
        add(TextureType.BUTTON, UIButton.ButtonType.PURCHASE_CITY_IMPROVEMENT.ordinal(),
                R.drawable.button_city_improvement, res);
        add(TextureType.BUTTON, UIButton.ButtonType.HIRE_KNIGHT.ordinal(),
                R.drawable.button_hire_knight, res);
        add(TextureType.BUTTON, UIButton.ButtonType.ACTIVATE_KNIGHT.ordinal(),
                R.drawable.button_activate_knight, res);
        add(TextureType.BUTTON, UIButton.ButtonType.PROMOTE_KNIGHT.ordinal(),
                R.drawable.button_promote_knight, res);
        add(TextureType.BUTTON, UIButton.ButtonType.CHASE_ROBBER.ordinal(),
                R.drawable.button_chase_robber, res);
        add(TextureType.BUTTON, UIButton.ButtonType.CHASE_PIRATE.ordinal(),
                R.drawable.button_chase_pirate, res);
        add(TextureType.BUTTON, UIButton.ButtonType.MOVE_KNIGHT.ordinal(), R.drawable.button_move_knight,
                res);
	}

    public void drawButton(UIButton button, GL10 gl) {
        float factor = 2 * BoardGeometry.HEX_PNG_SCALE / BoardGeometry.BUTTON_PNG_SCALE;

        gl.glPushMatrix();
        gl.glTranslatef(button.getX(), button.getY(), 10);
        gl.glScalef(button.getWidth() * factor, button.getHeight() * factor, 1);

        square.get(hash(TextureType.BUTTON_BG, UIButton.ButtonBackground.DEFAULT.ordinal())).render(gl);

        if (button.isPressed())
        {
            square.get(hash(TextureType.BUTTON_BG, UIButton.ButtonBackground.PRESSED.ordinal())).render(gl);
        }

        Square sqObj = square.get(hash(TextureType.BUTTON, button.getButtonType().ordinal()));
        sqObj.render(gl);

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

        if (!hexagon.hasRobber() && lastRoll != 0 && lastRoll != 7 && numToken == lastRoll)
        {
            square.get(hash(TextureType.HEX_ACTIVE, 0)).render(gl);
        }

        gl.glPopMatrix();
    }

    public void drawRobber(Hexagon hexagon, GL10 gl, BoardGeometry boardGeometry) {
        gl.glPushMatrix();

        int id = hexagon.getId();
        gl.glTranslatef(boardGeometry.getHexagonX(id), boardGeometry.getHexagonY(id), 0);

        if (hexagon.hasRobber() || hexagon.hasPirate())
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

    public void drawBuildableVertexUnit(Vertex vertex, boolean selectableSettlement, boolean selectableCity,
                                        boolean selectableCityWall, boolean selectableMetropolis, 
                                        GL10 gl, BoardGeometry boardGeometry) {

        TextureType textureType = TextureType.NONE;
        if (vertex.getCurUnitType() == Vertex.CITY || selectableCity || selectableCityWall || selectableMetropolis)
        {
            textureType = TextureType.CITY;
        }
        else if (vertex.getCurUnitType() == Vertex.SETTLEMENT || selectableSettlement)
        {
            textureType = TextureType.SETTLEMENT;
        }
        else if (vertex.getCurUnitType() == Vertex.CITY_WALL)
        {
            textureType = TextureType.CITY_WALL;
        }

        Player.Color color;
        Player owner = vertex.getOwnerPlayer();
        if (selectableSettlement || selectableCity || selectableCityWall || selectableMetropolis)
        {
            color = Player.Color.SELECTING;
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
            gl.glTranslatef(boardGeometry.getVertexX(id), boardGeometry.getVertexY(id),
                    TextureType.SETTLEMENT.ordinal());
            object.render(gl);
            gl.glPopMatrix();
        }
    }

    public TextureType getKnightTextureType(Knight knight) {
        if (knight.isActive()) {
            switch(knight.getKnightRank()) {
                case BASIC_KNIGHT:
                    return TextureType.BASIC_KNIGHT_ACTIVE;
                case STRONG_KNIGHT:
                    return TextureType.STRONG_KNIGHT_ACTIVE;
                case MIGHTY_KNIGHT:
                    return TextureType.MIGHTY_KNIGHT_ACTIVE;
            }
        } else {
            switch(knight.getKnightRank()) {
                case BASIC_KNIGHT:
                   return TextureType.BASIC_KNIGHT_INACTIVE;
                case STRONG_KNIGHT:
                    return TextureType.STRONG_KNIGHT_INACTIVE;
                case MIGHTY_KNIGHT:
                    return TextureType.MIGHTY_KNIGHT_INACTIVE;
            }
        }
        return TextureType.NONE;
    }

    public void drawKnight(Vertex vertex, Knight selectableKnight,
                           GL10 gl, BoardGeometry boardGeometry) {

        boolean isSelectable = selectableKnight != null;
        TextureType textureType = TextureType.NONE;
        if (isSelectable) { // selectability takes precedence
            textureType = getKnightTextureType(selectableKnight);
        }
        else if (vertex.getCurUnitType() == Vertex.KNIGHT)
        {
            textureType = getKnightTextureType(vertex.getPlacedKnight());
        }
        else if (vertex.getCurUnitType() == Vertex.TRADE_METROPOLIS)
        {
            textureType = TextureType.TRADE_METROPOLIS;
        }
        else if (vertex.getCurUnitType() == Vertex.SCIENCE_METROPOLIS)
        {
            textureType = TextureType.SCIENCE_METROPOLIS;
        }
        else if (vertex.getCurUnitType() == Vertex.POLITICS_METROPOLIS)
        {
            textureType = TextureType.POLITICS_METROPOLIS;
        }
        else if (vertex.getCurUnitType() == Vertex.WALLED_TRADE_METROPOLIS)
        {
            textureType = TextureType.WALLED_TRADE_METROPOLIS;
        }
        else if (vertex.getCurUnitType() == Vertex.WALLED_SCIENCE_METROPOLIS)
        {
            textureType = TextureType.WALLED_SCIENCE_METROPOLIS;
        }
        else if (vertex.getCurUnitType() == Vertex.WALLED_POLITICS_METROPOLIS)
        {
            textureType = TextureType.WALLED_POLITICS_METROPOLIS;
        }

        Player.Color color;
        Player owner = vertex.getOwnerPlayer();
        if (isSelectable)
        {
            color = Player.Color.SELECTING;
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
            gl.glTranslatef(boardGeometry.getVertexX(id), boardGeometry.getVertexY(id),
                    TextureType.SETTLEMENT.ordinal());
            object.render(gl);
            gl.glPopMatrix();
        }
    }


    //TODO: fix this to differentiate between roads and ships
    private void renderEdge(Edge edge, GL10 gl, BoardGeometry boardGeometry, TextureType texture, float dx, float dy) {
        gl.glTranslatef(boardGeometry.getEdgeX(edge.getId()), boardGeometry.getEdgeY(edge.getId()), texture.ordinal());
        gl.glRotatef((float) (180 / Math.PI * Math.atan(dy / dx)), 0, 0, 1);

        square.get(hash(texture, 0)).render(gl);
    }

    public void drawEdge(Edge edge, int edgeUnitType, boolean isSelectable,
                         GL10 gl, BoardGeometry boardGeometry) {
        float[] x = new float[2];
        float[] y = new float[2];
        x[0] = boardGeometry.getVertexX(edge.getV0Clockwise().getId());
        x[1] = boardGeometry.getVertexX(edge.getV1Clockwise().getId());
        y[0] = boardGeometry.getVertexY(edge.getV0Clockwise().getId());
        y[1] = boardGeometry.getVertexY(edge.getV1Clockwise().getId());

        Player owner = edge.getOwnerPlayer();
        float[] color;
        if (owner != null && !isSelectable)
        {
            color = getColorArray(getColor(owner.getColor()));
        }
        else
        {
            color = getColorArray(getColor(Player.Color.SELECTING));
        }

        float dx = x[1] - x[0];
        float dy = y[1] - y[0];

        gl.glColor4f(color[0], color[1], color[2], color[3]);

        gl.glPushMatrix();

        // OLD METHOD (everything looks like a road)
        gl.glTranslatef(boardGeometry.getEdgeX(edge.getId()), boardGeometry.getEdgeY(edge.getId()), TextureType.ROAD.ordinal());
        gl.glRotatef((float) (180 / Math.PI * Math.atan(dy / dx)), 0, 0, 1);

        square.get(hash(TextureType.ROAD, 0)).render(gl);

// FIXME: below attempt at rendering ships vs. roads caused strange OpenGL issues (noted below)
// NEW ATTEMPT AT EDGE RENDERING
//        int curEdgeUnitType = edge.getCurUnitType();
//        if (curEdgeUnitType != Edge.NONE) { // has a type already
//            switch(curEdgeUnitType) {
//                case Edge.ROAD:
//                    renderEdge(edge, gl, boardGeometry, TextureType.ROAD, dx, dy);
//                    break;
//                case Edge.SHIP:
//                    renderEdge(edge, gl, boardGeometry, TextureType.SHIP, dx, dy);
//                    break;
//            }
//        }
//        else { // render based on requested type (ie. during piece selection)
//            switch(edgeUnitType) {
//                case Edge.NONE: // ambiguous
//                    if (edge.isAvailableForShip()) {
//                        if(edge.isBorderingSea()) { // choice of road or ship
//                            // just render it as a ship for now
//                            //FIXME: strange OpenGL issues when road and ship rendered in same execution
//                            renderEdge(edge, gl, boardGeometry, TextureType.SHIP, dx, dy);
//                        }
//                        else { // can only build a ship here
//                            renderEdge(edge, gl, boardGeometry, TextureType.SHIP, dx, dy);
//                        }
//                    }
//                    else { // can only build a road here
//                        renderEdge(edge, gl, boardGeometry, TextureType.ROAD, dx, dy);
//                    }
//                    break;
//                case Edge.ROAD:
//                    renderEdge(edge, gl, boardGeometry, TextureType.ROAD, dx, dy);
//                    break;
//                case Edge.SHIP:
//                    renderEdge(edge, gl, boardGeometry, TextureType.SHIP, dx, dy);
//                    break;
//            }
//        }

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

    private static Long hash(TextureType textureType, int variant) {
        return HashingUtils.perfectHash(textureType.ordinal(), variant);
    }

    private Bitmap get(TextureType textureType, int variant) {
        return bitmap.get(hash(textureType, variant));
    }

    private void add(TextureType textureType, int variant, int id, Resources res) {
        Long key = hash(textureType, variant);
        Bitmap bitmap = BitmapFactory.decodeResource(res, id, new Options());
        this.bitmap.put(key, bitmap);
        this.resource.put(key, id);
        int textureDepth = 0;
        if(textureType == TextureType.BUTTON || textureType == TextureType.BUTTON_BG) {
            // buttons should always be rendered at the topmost layer
            textureDepth = TextureType.CITY.ordinal() + 1;
        }
        else if (textureType.ordinal() > TextureType.CITY.ordinal()) {
            // render most textures at the depth of a city
            textureDepth = TextureType.CITY.ordinal();
        }
        else {
            // the ordinal number of a texture type designates its layering
            textureDepth = textureType.ordinal();
        }
        this.square.put(key, new Square(id, 0, 0, textureDepth,
                (float) bitmap.getWidth() / (float) BoardGeometry.HEX_PNG_SCALE,
                (float) bitmap.getHeight() / (float) BoardGeometry.HEX_PNG_SCALE));
    }
}
