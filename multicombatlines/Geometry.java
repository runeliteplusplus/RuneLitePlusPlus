package net.runelite.client.plugins.multicombatlines;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

// COPIED FROM net.runelite.api.geometry.Geometry

public class Geometry
{
    public static Point2D.Float lineIntersectionPoint(
            float x1, float y1, float x2, float y2,
            float x3, float y3, float x4, float y4)
    {
        // https://stackoverflow.com/a/1968345

        float p1x = x2 - x1;
        float p1y = y2 - y1;
        float p2x = x4 - x3;
        float p2y = y4 - y3;

        float s = (-p1y * (x1 - x3) + p1x * (y1 - y3)) / (-p2x * p1y + p1x * p2y);
        float t = ( p2x * (y1 - y3) - p2y * (x1 - x3)) / (-p2x * p1y + p1x * p2y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
        {
            float x = x1 + (t * p1x);
            float y = y1 + (t * p1y);
            return new Point2D.Float(x, y);
        }

        // No intersection
        return null;
    }

    public static List<Point2D.Float> intersectionPoints(Shape shape, float x1, float y1, float x2, float y2)
    {
        List<Point2D.Float> intersections = new LinkedList<>();

        PathIterator it = shape.getPathIterator(new AffineTransform());
        float[] coords = new float[2];
        float[] prevCoords = new float[2];
        float[] start = new float[2];
        while (!it.isDone())
        {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO)
            {
                start[0] = coords[0];
                start[1] = coords[1];
                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
            }
            else if (type == PathIterator.SEG_LINETO)
            {
                Point2D.Float intersection = lineIntersectionPoint(
                        prevCoords[0], prevCoords[1], coords[0], coords[1], x1, y1, x2, y2);
                if (intersection != null)
                {
                    intersections.add(intersection);
                }
                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
            }
            else if (type == PathIterator.SEG_CLOSE)
            {
                Point2D.Float intersection = lineIntersectionPoint(
                        coords[0], coords[1], start[0], start[1], x1, y1, x2, y2);
                if (intersection != null)
                {
                    intersections.add(intersection);
                }
            }
            it.next();
        }

        return intersections;
    }

    public static GeneralPath transformPath(PathIterator it, Consumer<float[]> method)
    {
        GeneralPath path = new GeneralPath();
        float[] coords = new float[2];
        while (!it.isDone())
        {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO)
            {
                method.accept(coords);
                path.moveTo(coords[0], coords[1]);
            }
            else if (type == PathIterator.SEG_LINETO)
            {
                method.accept(coords);
                path.lineTo(coords[0], coords[1]);
            }
            else if (type == PathIterator.SEG_CLOSE)
            {
                path.closePath();
            }
            it.next();
        }

        return path;
    }

    public static GeneralPath transformPath(GeneralPath path, Consumer<float[]> method)
    {
        return transformPath(path.getPathIterator(new AffineTransform()), method);
    }

    private static void appendUnitLines(GeneralPath path, float unitSize,
                                        float x1, float y1, float x2, float y2)
    {
        float x = x1;
        float y = y1;
        float angle = (float)Math.atan2(y2 - y1, x2 - x1);
        float dx = (float)Math.cos(angle) * unitSize;
        float dy = (float)Math.sin(angle) * unitSize;
        float length = (float)Math.hypot(x2 - x1, y2 - y1);
        int steps = (int)((length - 1e-4) / unitSize);
        for (int i = 0; i < steps; i++)
        {
            x += dx;
            y += dy;
            path.lineTo(Math.round(x), Math.round(y));
        }
    }

    public static GeneralPath filterPath(PathIterator it, BiFunction<float[], float[], Boolean> method)
    {
        GeneralPath newPath = new GeneralPath();
        float[] prevCoords = new float[2];
        float[] coords = new float[2];
        float[] start = null;
        boolean shouldMoveNext = false;
        while (!it.isDone())
        {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO)
            {
                if (start == null)
                {
                    start = new float[2];
                    start[0] = coords[0];
                    start[1] = coords[1];
                }
                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
                shouldMoveNext = true;
            }
            else if (type == PathIterator.SEG_LINETO)
            {
                if (method.apply(prevCoords, coords))
                {
                    if (shouldMoveNext)
                    {
                        newPath.moveTo(prevCoords[0], prevCoords[1]);
                        shouldMoveNext = false;
                    }
                    newPath.lineTo(coords[0], coords[1]);
                }
                else
                {
                    shouldMoveNext = true;
                }
                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
            }
            else if (type == PathIterator.SEG_CLOSE)
            {
                if (shouldMoveNext)
                {
                    newPath.moveTo(prevCoords[0], prevCoords[1]);
                    shouldMoveNext = false;
                }
                if (method.apply(prevCoords, start))
                {
                    newPath.lineTo(start[0], start[1]);
                }
                start = null;
                shouldMoveNext = false;
            }
            it.next();
        }

        return newPath;
    }

    public static GeneralPath filterPath(GeneralPath path, BiFunction<float[], float[], Boolean> method)
    {
        return filterPath(path.getPathIterator(new AffineTransform()), method);
    }

    public static GeneralPath clipPath(PathIterator it, Shape shape)
    {
        GeneralPath newPath = new GeneralPath();
        float[] prevCoords = new float[2];
        float[] coords = new float[2];
        float[] start = new float[2];
        float[] nextMove = new float[2];
        boolean shouldMove = false;
        boolean wasInside = false;
        while (!it.isDone())
        {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO)
            {
                start[0] = coords[0];
                start[1] = coords[1];
                wasInside = shape.contains(coords[0], coords[1]);
                if (wasInside)
                {
                    nextMove[0] = coords[0];
                    nextMove[1] = coords[1];
                    shouldMove = true;
                }
                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
            }
            else if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_CLOSE)
            {
                if (type == PathIterator.SEG_CLOSE)
                {
                    coords[0] = start[0];
                    coords[1] = start[1];
                }

                List<Point2D.Float> intersections = intersectionPoints(shape, prevCoords[0], prevCoords[1], coords[0], coords[1]);
                intersections.sort((a, b) ->
                {
                    double diff = a.distance(prevCoords[0], prevCoords[1]) - b.distance(prevCoords[0], prevCoords[1]);
                    if (diff < 0)
                    {
                        return -1;
                    }
                    if (diff > 0)
                    {
                        return 1;
                    }
                    return 0;
                });

                for (Point2D.Float intersection : intersections)
                {
                    if (wasInside)
                    {
                        if (shouldMove)
                        {
                            newPath.moveTo(nextMove[0], nextMove[1]);
                            shouldMove = false;
                        }
                        newPath.lineTo(intersection.getX(), intersection.getY());
                    }
                    else
                    {
                        nextMove[0] = intersection.x;
                        nextMove[1] = intersection.y;
                        shouldMove = true;
                    }
                    wasInside = !wasInside;
                    prevCoords[0] = intersection.x;
                    prevCoords[1] = intersection.y;
                }

                wasInside = shape.contains(coords[0], coords[1]);
                if (wasInside)
                {
                    if (shouldMove)
                    {
                        newPath.moveTo(nextMove[0], nextMove[1]);
                        shouldMove = false;
                    }
                    newPath.lineTo(coords[0], coords[1]);
                }
                else
                {
                    nextMove[0] = coords[0];
                    nextMove[1] = coords[1];
                    shouldMove = true;
                }

                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
            }
            it.next();
        }
        return newPath;
    }

    public static GeneralPath clipPath(GeneralPath path, Shape shape)
    {
        return clipPath(path.getPathIterator(new AffineTransform()), shape);
    }

    // NEW METHODS
    public static boolean rectangleContainsPoint(
            float minX, float minY, float maxX, float maxY, float testX, float testY)
    {
        return testX >= minX && testX <= maxX && testY >= minY && testY <= maxY;
    }

    public static GeneralPath unitifyPath(PathIterator it, float unitSize)
    {
        GeneralPath newPath = new GeneralPath();
        float[] prevCoords = new float[2];
        float[] coords = new float[2];
        float[] startCoords = null;
        while (!it.isDone())
        {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO)
            {
                if (startCoords == null)
                {
                    startCoords = new float[2];
                    startCoords[0] = coords[0];
                    startCoords[1] = coords[1];
                }
                newPath.moveTo(coords[0], coords[1]);
                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
            }
            else if (type == PathIterator.SEG_LINETO)
            {
                appendUnitLines(newPath, unitSize, prevCoords[0], prevCoords[1], coords[0], coords[1]);
                newPath.lineTo(coords[0], coords[1]);
                prevCoords[0] = coords[0];
                prevCoords[1] = coords[1];
            }
            else if (type == PathIterator.SEG_CLOSE)
            {
                appendUnitLines(newPath, unitSize, coords[0], coords[1], startCoords[0], startCoords[1]);
                newPath.closePath();
                startCoords = null;
            }
            it.next();
        }

        return newPath;
    }

    public static GeneralPath unitifyPath(GeneralPath path, float unitSize)
    {
        return unitifyPath(path.getPathIterator(new AffineTransform()), unitSize);
    }
}