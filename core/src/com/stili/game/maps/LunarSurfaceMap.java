package com.stili.game.maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class LunarSurfaceMap {
    private final Array<Vector2> points;
    private final Array<LandscapeLine> lines;
    private final ShapeRenderer renderer;
    private final float mapWidth;

    public LunarSurfaceMap (String map) {
        this.points = new Array<>();
        this.lines = new Array<>();
        loadMap(map);
        this.mapWidth = points.get(points.size - 1).x - points.get(0).x;
        this.renderer = new ShapeRenderer();

//        renderer.setAutoShapeType(true);
    }

    private void loadMap(String fileName) {
        FileHandle fileHandle = Gdx.files.internal(fileName);
        Vector2 prevPoint = null;
        Vector2 point;

        if (fileHandle.exists()) {
            JsonValue root = new JsonReader().parse(fileHandle);
            if (root != null && root.isArray()) {
                for (JsonValue value : root) {
                    float x = value.getFloat("x", 0);
                    float y = value.getFloat("y", 0);
                    point = new Vector2(x, y);

                    this.points.add(point);

                    if (prevPoint != null) {
                        this.lines.add(new LandscapeLine(prevPoint, point));
                    }

                    prevPoint = point;

                }
            } else {
                Gdx.app.error("LunarSurfaceService", "Failed to parse map data.");
            }
        } else {
            Gdx.app.error("LunarSurfaceService", "Map file not found: " + fileName);
        }
    }

    //todo: start thinking vertically
    public void render(OrthographicCamera camera) {
        renderer.setProjectionMatrix(camera.combined);

        float halfViewportWidth = (camera.viewportWidth * camera.zoom) / 2;
        float viewLeft = camera.position.x - halfViewportWidth;
        float viewRight = camera.position.x + halfViewportWidth;

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.WHITE);

        renderLandscape(viewLeft, viewRight);

        renderer.end();
    }

    private void renderLandscape(float viewLeft, float viewRight) {
        float offset = 0;

        while (viewLeft - offset > mapWidth) {
            offset += mapWidth;
        }

        while (viewLeft - offset < 0) {
            offset -= mapWidth;
        }

        if (!lines.isEmpty()) {
            int i = 0;
            float p1x, p2x;
            LandscapeLine line = lines.get(i);

            while (true) {
                p1x = line.getP1().x + offset;
                p2x = line.getP2().x + offset;

                if (p1x >= viewRight) {
                    break;
                }

                if (p2x > viewLeft) {
                    renderer.rectLine(p1x, line.getP1().y, p2x, line.getP2().y, 2);
                }

                i++;
                if (i >= lines.size) {
                    i = 0;
                    offset += mapWidth;
                }

                line = lines.get(i);
            }
        }
    }

    public void dispose() {
        renderer.dispose();
    }
}
