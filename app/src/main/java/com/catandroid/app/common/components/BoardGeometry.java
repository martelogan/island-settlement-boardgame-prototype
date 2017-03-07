package com.catandroid.app.common.components;

import android.util.Log;

import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Harbor;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.components.utilities.hex_grid_utils.AxialHexLocation;
import com.catandroid.app.common.components.utilities.hex_grid_utils.HexGridLayout;
import com.catandroid.app.common.components.utilities.hex_grid_utils.HexGridUtils;
import com.catandroid.app.common.components.utilities.hex_grid_utils.HexPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BoardGeometry {

	// DEFAULT GEOMETRY CONSTANTS (regular board)
    private static HexPoint HEX_POINT_SCALE = HexGridLayout.size_default;
	private int BOARD_SIZE = 0, ZOOM_SCALE = 250, HEX_COUNT = 61,
			EDGE_COUNT = 210, VERTEX_COUNT = 150, HARBOR_COUNT = 9;

	/* NOTE: For a perfectly-centered hexagon
	with centered hexagonal number K, the map
	radius is R = n + 1 where n is the positive
	integer solution of	(3n^2 - 3n + 1) = K.

	In other words, for n >= 1, the n-th centered
	hexagonal number K has map radius n + 1.*/
	private int MAP_RADIUS = 4;

	// ZOOM PARAMETERS
	private int width, height;
	private float cx, cy, zoom;
	private float minZoom, maxZoom, highZoom;

	// GEOMETRIC OVERLAY
	private float[] HEXAGONS_X, HEXAGONS_Y, VERTICES_X, VERTICES_Y,
			EDGES_X, EDGES_Y;
	private int[] HARBOR_EDGE_IDS, HARBOR_HEX_IDS;

	// GRAPHICAL RESOURCE SIZES
	private static final float MAX_PAN = 2.5f;
	public static final int HEX_PNG_SCALE = 256;
	public static final int BUTTON_PNG_SCALE = 128;

	public BoardGeometry(int boardSize) {
		cx = cy = 0;
		width = height = 480;
		zoom = minZoom = maxZoom = highZoom = 1;
		this.BOARD_SIZE = boardSize;
		switch (boardSize) {
			case 0:
				this.ZOOM_SCALE = 450;
				this.HEX_COUNT = 37;
				this.VERTEX_COUNT = 96;
				this.EDGE_COUNT = 132;
				this.MAP_RADIUS = 3;
				break;
			case 1:
				this.ZOOM_SCALE = 500;
				this.HEX_COUNT = 61;
				this.VERTEX_COUNT = 150;
				this.EDGE_COUNT = 210;
				this.MAP_RADIUS = 4;
				break;
		}
		HEXAGONS_X = new float[HEX_COUNT];
		HEXAGONS_Y = new float[HEX_COUNT];
		VERTICES_X = new float[VERTEX_COUNT];
		VERTICES_Y =  new float[VERTEX_COUNT];
		EDGES_X = new float[EDGE_COUNT];
		EDGES_Y = new float[EDGE_COUNT];
		HARBOR_EDGE_IDS = new int[HARBOR_COUNT];
		HARBOR_HEX_IDS = new int[HARBOR_COUNT];
	}

	public void setCurFocus(int w, int h) {
		width = w;
		height = h;

		float aspect = (float) width / (float) height;
		float minZoomX = 0.5f * (float) width / (5.5f * ZOOM_SCALE);
		float minZoomY = 0.5f * (float) width / aspect / (5.1f * ZOOM_SCALE);

		minZoom = min(minZoomX, minZoomY);
		highZoom = 2 * minZoom;
		maxZoom = 3 * minZoom;

		setZoom(minZoom);
	}

	public int getBoardSize() {
		return BOARD_SIZE;
	}

	public int getHexCount() {
		return HEX_COUNT;
	}

	public int getEdgeCount() {
		return EDGE_COUNT;
	}

	public int getVertexCount() {
		return VERTEX_COUNT;
	}

	public int getHarborCount() {
		return HARBOR_COUNT;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private int getMinimalSize() {
		return width < height ? width : height;
	}

	public int getNearestHexagon(int userX, int userY) {
		return getNearest(userX, userY, HEXAGONS_X, HEXAGONS_Y, HEX_COUNT);
	}

	public int getNearestEdge(int userX, int userY) {
		return getNearest(userX, userY, EDGES_X, EDGES_Y, EDGE_COUNT);
	}

	public int getNearestVertex(int userX, int userY) {
		return getNearest(userX, userY, VERTICES_X, VERTICES_Y, VERTEX_COUNT);
	}

	public float getHexagonX(int index) {
		return HEXAGONS_X[index];
	}

	public float getHexagonY(int index) {
		return HEXAGONS_Y[index];
	}

	public float getEdgeX(int index) {
		return EDGES_X[index];
	}

	public float getEdgeY(int index) {
		return EDGES_Y[index];
	}

	public float getVertexX(int index) {
		return VERTICES_X[index];
	}

	public float getVertexY(int index) {
		return VERTICES_Y[index];
	}

	public float getHarborX(int index) {
		return HEXAGONS_X[HARBOR_HEX_IDS[index]];
	}

	public float getHarborY(int index) {
		return HEXAGONS_Y[HARBOR_HEX_IDS[index]];
	}

	public float getHarborIconX(int index, Edge e) {
		float edgeX = EDGES_X[HARBOR_EDGE_IDS[index]];
		int sign = e.isBorderingSea() ? e.getDirectTowardsSea_X() : e.getOriginHexDirectXsign();
		return edgeX + 0.28f * ((float) Math.abs(HEX_POINT_SCALE.x)) * (sign);
	}

	public float getHarborIconY(int index, Edge e) {
		float edgeY = EDGES_Y[HARBOR_EDGE_IDS[index]];
		int sign = e.isBorderingSea() ? e.getDirectTowardsSea_Y() : e.getOriginHexDirectYsign();
		return edgeY + 0.28f * ((float) Math.abs(HEX_POINT_SCALE.y)) * (sign);
	}

	public float getZoom() {
		return zoom;
	}

	public void zoomBy(float z) {
		setZoom(zoom * z);
	}

	public void setZoom(float z) {
		zoom = z;

		if (zoom > maxZoom)
			zoom = maxZoom;
		else if (zoom < minZoom)
			zoom = minZoom;

		translate(0, 0);
	}

	public void zoomTo(int userX, int userY) {
		cx = translateScreenX(userX);
		cy = translateScreenY(userY);
		zoom = highZoom;
		translate(0, 0);
	}

	public void zoomOut() {
		cx = cy = 0;
		zoom = minZoom;
	}

	public void toggleZoom(int userX, int userY) {
		if (zoom > (highZoom - 0.01f))
		{
			zoomOut();
		}
		else
		{
			zoomTo(userX, userY);
		}
	}

	public void translate(float dx, float dy) {
		float halfMin = (float) getMinimalSize() / 2.0f;

		cx += dx / halfMin;
		cy -= dy / halfMin;

		float radius = (float) Math.sqrt(cx * cx + cy * cy);
		float maxRadius = MAX_PAN * (zoom - minZoom) / (maxZoom - minZoom);

		if (radius > maxRadius) {
			cx *= maxRadius / radius;
			cy *= maxRadius / radius;
		}
	}

	public float getTranslateX() {
		return cx;
	}

	public float getTranslateY() {
		return cy;
	}

	private float translateScreenX(int x) {
		float halfMin = (width < height ? width : height) / 2f;
		return ((x - width / 2) / halfMin + cx) / zoom;
	}

	private float translateScreenY(int y) {
		float halfMin = (width < height ? width : height) / 2f;
		return ((height / 2 - y) / halfMin + cy) / zoom;
	}

	private int getNearest(int userX, int userY, float[] rx, float[] ry, int length) {
		float x = translateScreenX(userX);
		float y = translateScreenY(userY);

		int best = -1;
		double dist2 = zoom * zoom / 4;
		for (int i = 0; i < length; i++) {
			double x2 = Math.pow(x - rx[i], 2);
			double y2 = Math.pow(y - ry[i], 2);
			if (x2 + y2 < dist2) {
				dist2 = x2 + y2;
				best = i;
			}
		}
		return best;
	}

	// BOARD POPULATION LOGIC

	void populateBoard(Hexagon[] hexagons, Vertex[] vertices,
					   Edge[] edges, Harbor[] harbors, HashMap<Long, Hexagon> hexMap)  {

		// edges available to neighbour harbors
		HashSet<Edge> portEdges = new HashSet<Edge>();

        // shuffled array of hexagons
		Hexagon[] randomHexes = Arrays.copyOf(hexagons, HEX_COUNT);
        Collections.shuffle(Arrays.asList(randomHexes));

        // check that all hexagons were hashed as expected
		HashSet<Hexagon> successfullyHashed = new HashSet<Hexagon>();

        // variables for hexagon population logic
		int hexagonIndex = 0, edgeIndexOnHex = 0, vertexIndex = 0;
        Long hexLocationHash;
		Hexagon curHex = null, curNeighborHex = null;
        AxialHexLocation curHexLocation = null, neighborLocation = null;
        Boolean hadClockwiseNeighbor = false, hadAntiClockwiseNeighbor = false;
		Edge clockwiseEdge = null, neighborEdge = null;

        // iteration to generate perfectly-centered hex shape
        for (int q = -MAP_RADIUS; q <= MAP_RADIUS; q++) {
            int r1 = max(-MAP_RADIUS, -q - MAP_RADIUS);
            int r2 = min(MAP_RADIUS, -q + MAP_RADIUS);
            for (int r = r1; r <= r2; r++) { // for each (q, r) axial coordinate

                // for each hexagon we seek to place
                curHex = randomHexes[hexagonIndex];
                curHexLocation = new AxialHexLocation(q, r);

                // iterate clockwise from top vertex around hexagon
                Vertex clockwiseV0 = null, clockwiseV1 = null;
                for (int vDirect = 0; vDirect < 6; vDirect++) {
                    hadClockwiseNeighbor = false;
                    // is there a clockwise axialNeighbor w.r.t vDirect?
                    neighborLocation = AxialHexLocation.axialNeighbor(curHexLocation, vDirect);
                    hexLocationHash = HexGridUtils.perfectHash(neighborLocation);
                    curNeighborHex = hexMap.get(hexLocationHash);
                    if (curNeighborHex != null) { // we have a clockwise axialNeighbor
                        hadClockwiseNeighbor = true;
                        resolveNeighborHex(portEdges, curHex, curNeighborHex,
                                vDirect, (vDirect + 1) % 6);
                    } else { // there was no clockwise axialNeighbor
                        // get and increment next available edge
                        clockwiseEdge = edges[edgeIndexOnHex];
						clockwiseEdge.setOriginHex(curHex);
                        edgeIndexOnHex += 1;
                        // new edge is candidate for harbor
                        portEdges.add(clockwiseEdge);
						clockwiseEdge.setPortHex(curHex);
                        // use current hex's next vertex if already placed
                        clockwiseV1 = curHex.getVertex((vDirect + 1) % 6);
                        if (clockwiseV1 == null) {
                            // is there a clockwise axialNeighbor w.r.t vDirect + 1?
                            neighborLocation = AxialHexLocation.axialNeighbor(
                                    curHexLocation, (vDirect + 1) % 6);
                            hexLocationHash = HexGridUtils.perfectHash(neighborLocation);
                            curNeighborHex = hexMap.get(hexLocationHash);
                            if (curNeighborHex != null) { // we have a clockwise axialNeighbor
								neighborEdge = resolveNeighborHex(portEdges, curHex, curNeighborHex,
										(vDirect + 1) % 6, (vDirect + 2) % 6);
                                clockwiseV1 = neighborEdge.getV1Clockwise();
                                curHex.setVertex(clockwiseV0, (vDirect + 1) % 6);
                            } else { // there was no clockwise axialNeighbor
                                // place new vertex at clockwiseV1
                                clockwiseV1 = vertices[vertexIndex];
                                vertexIndex += 1;
                            }
                        }
                    }
                    // is there a counter-clockwise axialNeighbor w.r.t vDirect?
                    neighborLocation =
							AxialHexLocation.axialNeighbor(curHexLocation,
									Math.abs(((((vDirect - 1)  % 6) + 6) % 6)));
                    hexLocationHash = HexGridUtils.perfectHash(neighborLocation);
                    curNeighborHex = hexMap.get(hexLocationHash);
                    if (curNeighborHex != null) { // we have an anti-clockwise axialNeighbor
                        hadAntiClockwiseNeighbor = true;
                        neighborEdge = resolveNeighborHex(portEdges, curHex, curNeighborHex,
                                Math.abs(((((vDirect - 1)  % 6) + 6) % 6)), vDirect);
                        if (!hadClockwiseNeighbor) {
                            // reuse less-clockwise vertex of anti-clockwise axialNeighbor
                            clockwiseV0 = neighborEdge.getV0Clockwise();
                            curHex.setVertex(clockwiseV0, vDirect);

                            placeClockwiseEdge(clockwiseEdge, clockwiseV0,
                                    clockwiseV1, curHex, vDirect);
                        }
                    } else { // we did not have an anti-clockwise axialNeighbor
                        if (!hadClockwiseNeighbor) { // vertex and edge are both new in vDirect
                            // use current hex's vertex if already placed
                            clockwiseV0 = curHex.getVertex(vDirect);
                            if (clockwiseV0 == null) {
                                // get next available vertex
                                clockwiseV0 = vertices[vertexIndex];
                                vertexIndex += 1;
                            }

                            placeClockwiseEdge(clockwiseEdge, clockwiseV0,
                                    clockwiseV1, curHex, vDirect);
                        }
                    }
                }
                // hexagon is ready to place
				curHex.setCoord(curHexLocation);
                hexLocationHash = HexGridUtils.perfectHash(curHexLocation);
                hexMap.put(hexLocationHash, curHex);
                successfullyHashed.add(curHex);
                hexagonIndex += 1;
            }
        }

		if (successfullyHashed.size() != HEX_COUNT) {
			Log.w("BOARD_GEOMETRY_WARNING","Some hexes were not hashed!");
		}

        placeHarbors(hexMap, harbors, portEdges);

 		initCartesianCoordinates(hexagons, vertices, edges);

	}

    // BOARD POPULATION HELPERS

    private Edge resolveNeighborHex(HashSet<Edge> portEdges, Hexagon myHex,
                                    Hexagon myNeighbor, int myV0index, int myV1index) {
        Vertex myClockwiseV0, myClockwiseV1;
        int neighborEdgeDirect = AxialHexLocation.complementAxialDirection(myV0index);
        Edge neighborEdge = myNeighbor.getEdge(neighborEdgeDirect);
        neighborEdge.setNeighborHex(myNeighbor);
        Hexagon.TerrainType myHexTerrain = myHex.getTerrainType(),
                neighborHexTerrain = myNeighbor.getTerrainType();
        boolean myLandNeighborsSea, mySeaNeighborsLand, isPortEdge;
        myLandNeighborsSea = myHexTerrain != Hexagon.TerrainType.SEA
                && neighborHexTerrain == Hexagon.TerrainType.SEA;
        mySeaNeighborsLand = myHexTerrain == Hexagon.TerrainType.SEA
                && neighborHexTerrain != Hexagon.TerrainType.SEA;
        isPortEdge = (myLandNeighborsSea || mySeaNeighborsLand);
        if (isPortEdge) {
            Hexagon portHex = myLandNeighborsSea ? myHex : myNeighbor;
            neighborEdge.setPortHex(portHex);
            neighborEdge.setBorderingSea(true);
        }
        else {
            // forfeit neighborEdge candidacy for harbor
            try {
                // should fail silently if edge not present
                portEdges.remove(neighborEdge);
                neighborEdge.removePortHex();
            } catch (Exception e) {
                // Warn exception (in case an intended removal failed)
                System.out.println("WARNING: failed to remove edge. Exception:\n");
                e.printStackTrace();
            }
        }
        // axialDirection is reversed to that of edge creator
        myClockwiseV0 = neighborEdge.getV1Clockwise();
        myClockwiseV1 = neighborEdge.getV0Clockwise();
        myHex.setEdge(neighborEdge, myV0index);
        myHex.setVertex(myClockwiseV0, myV0index);
        myHex.setVertex(myClockwiseV1, myV1index);

        return neighborEdge;
    }

    private void placeClockwiseEdge(Edge clockwiseEdge, Vertex clockwiseV0,
                                    Vertex clockwiseV1, Hexagon curHex, int vDirect) {
        clockwiseEdge.setVertices(clockwiseV0, clockwiseV1);
        curHex.setEdge(clockwiseEdge, vDirect);
        curHex.setVertex(clockwiseV0, vDirect);
        curHex.setVertex(clockwiseV1, (vDirect + 1) % 6);
    }

    private void placeHarbors(HashMap<Long, Hexagon> hexMap,
                              Harbor[] harbors, HashSet<Edge> portEdges) {

        // shuffled array of edges
        ArrayList<Edge> randomPortEdges = new ArrayList<Edge>(portEdges);
        Collections.shuffle(randomPortEdges);

        // associate vertices with harbors
        Harbor harbor;
        Edge candidatePortEdge = null;
        Hexagon candidatePortHex = null, curNeighborHex = null;
        AxialHexLocation candidatePortHexLocation, neighborLocation;
        int neighborDirect, forbiddenNeighborEdgeDirect, edgeIndexOnHex;
        HashSet<Edge> forbiddenPortEdges = new HashSet<Edge>();
        for (int i = 0, j = 0; i < harbors.length; i++, j++) {
            while (j < randomPortEdges.size()) { // find a candidate port edge
                candidatePortEdge = randomPortEdges.get(j);
                if (forbiddenPortEdges.contains(candidatePortEdge)) { // iterate until valid
                    j++;
                    continue;
                }
                candidatePortHex = candidatePortEdge.getPortHex();
                candidatePortHexLocation = candidatePortHex.getCoord();
                if (candidatePortEdge.isBorderingSea()) { // harbor within sea hex
                    // FORBID OVERLAP OF SEA HEX HARBORS
                    neighborLocation =
                            AxialHexLocation.axialNeighbor(
                                    candidatePortHexLocation, candidatePortEdge.getPortHexDirect());
                    Hexagon seaHex = hexMap.get(HexGridUtils.perfectHash(neighborLocation));
                    Edge curEdge = null;
                    for (int k = 0; k < 6; k++) { // remove candidacy of other edges on sea hex
                        curEdge = seaHex.getEdge(k);
                        if (portEdges.contains(curEdge)
                                && curEdge.isBorderingSea()) {
                            forbiddenPortEdges.add(curEdge);
                        }
                    }
                } else { // harbor at extremes of game board
                    if (candidatePortHex.getTerrainType() == Hexagon.TerrainType.SEA) {
                        // don't allow harbors in the middle of the ocean
                        forbiddenPortEdges.add(candidatePortEdge);
                        continue;
                    }

                    // FORBID OVERLAP OF HARBORS AT EXTREMES OF BOARD

                    // add clockwise forbidden edge
                    neighborDirect = (candidatePortEdge.getOriginHexDirect() + 1) % 6;
                    neighborLocation =
                            AxialHexLocation.axialNeighbor(candidatePortHexLocation, neighborDirect);
                    curNeighborHex = hexMap.get(HexGridUtils.perfectHash(neighborLocation));
                    if (curNeighborHex != null) {
                        forbiddenNeighborEdgeDirect =
                                (AxialHexLocation.complementAxialDirection(neighborDirect) + 1) % 6;
                        forbiddenPortEdges.add(curNeighborHex.getEdge(forbiddenNeighborEdgeDirect));
                    }
                    // add counter-clockwise forbidden edge
                    neighborDirect = (((((candidatePortEdge.getOriginHexDirect() - 1) % 6) + 6) % 6));
                    neighborLocation =
                            AxialHexLocation.axialNeighbor(candidatePortHexLocation, neighborDirect);
                    curNeighborHex = hexMap.get(HexGridUtils.perfectHash(neighborLocation));
                    if (curNeighborHex != null) {
                        forbiddenNeighborEdgeDirect =
                                ((((AxialHexLocation.complementAxialDirection(neighborDirect) - 1)
                                        % 6) + 6) % 6);
                        forbiddenPortEdges.add(curNeighborHex.getEdge(forbiddenNeighborEdgeDirect));
                    }
                    // track current edge
                    forbiddenPortEdges.add(candidatePortEdge);
                }
                break;
            }
            if (j > randomPortEdges.size()) {
                Log.e("BOARD_GEOMETRY_ERROR", "insufficient port edges");
                break;
            }
            harbor = harbors[i];
            edgeIndexOnHex = candidatePortHex.findEdgeDirect(candidatePortEdge);
            harbor.setPosition(Harbor.vdirectToPosition(edgeIndexOnHex));
            candidatePortEdge.setMyHarbor(harbor);
            candidatePortEdge.getV0Clockwise().setHarbor(harbor);
            candidatePortEdge.getV1Clockwise().setHarbor(harbor);
            HARBOR_EDGE_IDS[i] = candidatePortEdge.getId();
            HARBOR_HEX_IDS[i] = candidatePortEdge.getPortHexId();
        }
    }

	private void initCartesianCoordinates(Hexagon[] hexagons, Vertex[] vertices,
                                          Edge[] edges) {
		HexGridLayout layout = new HexGridLayout(HexGridLayout.flat, HexGridLayout.size_default, HexGridLayout.origin_default);
		AxialHexLocation axialCoord;
		float center_X, center_Y;
		HexPoint cartesianCoord, vertexOffset;
		int hexId, edgeId, vertexId, v0_Id, v1_Id, angleDirection;
		for (Hexagon h : hexagons) {
			// set hex cartesian coordinates
			hexId = h.getId();
			axialCoord = h.getCoord();
			cartesianCoord = HexGridLayout.hexToPixel(layout, axialCoord);
			center_X = (float) cartesianCoord.x;
			center_Y = (float) cartesianCoord.y;
			HEXAGONS_X[hexId] = center_X;
			HEXAGONS_Y[hexId] = center_Y;
		}
        Hexagon myHex;
        for (Vertex v: vertices)
        {
            // set vertex cartesian coordinates
            vertexId = v.getId();
            myHex = v.getHexagon(0);
            hexId = myHex.getId();
            angleDirection = -((((myHex.findVdirect(v) - 1)  % 6) + 6) % 6);
            vertexOffset = HexGridLayout.hexCornerOffset(layout, angleDirection);
            VERTICES_X[vertexId] = HEXAGONS_X[hexId] + ((float) vertexOffset.x);
            VERTICES_Y[vertexId] = HEXAGONS_Y[hexId] + ((float) vertexOffset.y);
        }
		for (Edge e : edges) {
			// set edge cartesian coordinates
			edgeId = e.getId();
			v0_Id = e.getV0Clockwise().getId();
			v1_Id = e.getV1Clockwise().getId();
			EDGES_X[edgeId] = (float) ((VERTICES_X[v0_Id] + VERTICES_X[v1_Id])/2.0);
			EDGES_Y[edgeId] = (float) ((VERTICES_Y[v0_Id] + VERTICES_Y[v1_Id])/2.0);
		}
	}
}
