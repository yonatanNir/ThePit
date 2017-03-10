package shutterfly.joins.yonatanir.thepit.Controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import shutterfly.joins.yonatanir.thepit.R;

/**
 * Created by yonatan on 3/7/2017.
 */

//The Pit - a ViewGroup that renders an interactive horizontal 2D graph of points, between every point there is a connected edge (linear edge).
public class Pit extends ViewGroup
{
    private final int PREFERED_SIZE = 700;
    private final int DEFAULT_NUMBER_OF_POINTS = 5;
    private final int DEFAULT_POINT_RADIUS = 20;
    private final int DEFAULT_AXIS_WIDTH = 10;
    private final int DEFAULT_EDGE_WIDTH = 5;
    private int initialPointsNum;
    private int pointColor, axisColor, edgeColor;
    private float pointRadius, axisWidth, edgeWidth;
    private int actualHeight, actualWidth;
    private Paint pointsPaint, edgesPaint, axisPaint;
    private float maxXWithPoint, maxYWithPoint, minXWithPoint, minYWithPoint;
    private LinkedList<PitPoint> pointsList;
    private boolean sizeChanged;

    public Pit(Context context)
    {
        this(context, null);
    }

    public Pit(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public Pit(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        pointsList = new LinkedList<>();
        setWillNotDraw(false);
        setAttributeValues(attrs);
        initPaints();
        setOnTouchListener(new PointsOnTouchListener());
    }

    //This method reads the attribute set by the user in the layout's XML for the view
    private void setAttributeValues(AttributeSet attrs)
    {
        if(attrs != null)
        {
            TypedArray mTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Pit);
            try
            {
                initialPointsNum = mTypedArray.getInteger(R.styleable.Pit_initialPointsAmount, DEFAULT_NUMBER_OF_POINTS);
                pointRadius = mTypedArray.getDimension(R.styleable.Pit_pointRadius, DEFAULT_POINT_RADIUS);
                pointColor = mTypedArray.getColor(R.styleable.Pit_pointColor, getResources().getColor(R.color.red));
                axisColor = mTypedArray.getColor(R.styleable.Pit_axisColor, getResources().getColor(R.color.blue));
                axisWidth = mTypedArray.getDimension(R.styleable.Pit_axisWidth, DEFAULT_AXIS_WIDTH);
                edgeColor = mTypedArray.getColor(R.styleable.Pit_edgeColor, getResources().getColor(R.color.black));
                edgeWidth = mTypedArray.getDimension(R.styleable.Pit_edgeWidth, DEFAULT_EDGE_WIDTH);
            }
            finally
            {
                mTypedArray.recycle();
            }
        }
    }

    //This method initializes the paints which will used to draw shapes on the canvas
    private void initPaints()
    {
        pointsPaint = new Paint();
        pointsPaint.setColor(pointColor);
        pointsPaint.setStyle(Paint.Style.FILL);

        edgesPaint = new Paint();
        edgesPaint.setColor(edgeColor);
        edgesPaint.setStrokeWidth(edgeWidth);
        edgesPaint.setStyle(Paint.Style.STROKE);

        axisPaint = new Paint();
        axisPaint.setColor(axisColor);
        axisPaint.setStrokeWidth(axisWidth);
        axisPaint.setStyle(Paint.Style.STROKE);
    }

    //////////// a bunch of setters and getters for attribute values //////////////////

    public int getInitialPointsNum()
    {
        return initialPointsNum;
    }

    public int getPointColor()
    {
        return pointColor;
    }

    public void setPointColor(int pointColor)
    {
        this.pointColor = pointColor;
        invalidateAndRedraw();
    }


    public int getAxisColor()
    {
        return axisColor;
    }

    public void setAxisColor(int axisColor)
    {
        this.axisColor = axisColor;
        invalidateAndRedraw();

    }

    public int getEdgeColor()
    {
        return edgeColor;
    }

    public void setEdgeColor(int edgeColor)
    {
        this.edgeColor = edgeColor;
        invalidateAndRedraw();
    }


    public float getPointWidth()
    {
        return pointRadius;
    }


    public void setPointRadius(float pointRadius)
    {
        this.pointRadius = pointRadius;
        invalidateAndRedraw();
    }


    public float getAxisWidth()
    {
        return axisWidth;
    }

    public void setAxisWidth(float axisWidth)
    {
        this.axisWidth = axisWidth;
        invalidateAndRedraw();
    }


    public float getEdgeWidth()
    {
        return edgeWidth;
    }


    public void setEdgeWidth(float edgeWidth)
    {
        this.edgeWidth = edgeWidth;
        invalidateAndRedraw();
    }

    /////////////////////////////////////////////////////////////


    //This method will set the layout's size based on information from its parent's layout constraints
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int height = resolveSizeAndState(PREFERED_SIZE,heightMeasureSpec,0);
        int width = resolveSizeAndState(PREFERED_SIZE,widthMeasureSpec,0);
        actualWidth = width - getPaddingLeft() - getPaddingRight();
        actualHeight = height - getPaddingTop() - getPaddingBottom();
        initInitialPoints();
        setMeasuredDimension(width, height);
    }

    //Since this view has no children, no need to implement
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {


    }

    //This method does the actual rendering of the view in the canvas
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        addAxisLInes(canvas);
        addPointsToCanvas(canvas);
        addEdgesBetweenPoints(canvas);
    }

    //Transform x position of axis to an x position inside the view
    private float getRelativeX(float x)
    {
        return x + actualWidth / 2;
    }

    //Transform y position of axis to a y position inside the view
    private float getRelativeY(float y)
    {
        return y + actualHeight / 2;
    }

    //Adding initial points. The points will be distributed evenly on the X axis and on 2 different Y points
    private void initInitialPoints()
    {
        if(pointsList.size() < initialPointsNum)
        {
            float spaceBetweenPoints = actualWidth / initialPointsNum;
            float xStartPoint = spaceBetweenPoints / 2; //some "padding" to make the points distribute in the middle
            minXWithPoint = xStartPoint;
            float y1 = actualHeight / 4;     // the upper half
            minYWithPoint = y1; // since the view's (0,0) is in top left than lower on the screen means larger y
            float y2 = actualHeight * 3 / 4; // lower half
            maxYWithPoint = y2;
            for(int i=0; i < initialPointsNum; i++)
            {
                PitPoint p;
                float xPosition = xStartPoint + (i * spaceBetweenPoints);
                if(i % 2 == 0)
                {
                    p = new PitPoint(xPosition, y1);
                }
                else
                {
                    p = new PitPoint(xPosition, y2);
                }
                pointsList.add(p);
            }
            maxXWithPoint = xStartPoint + ((initialPointsNum - 1) * spaceBetweenPoints);
        }
    }

    //Draw the X and Y axises on the canvas
    private void addAxisLInes(Canvas canvas)
    {
        if(canvas == null)
        {
            return;
        }
        canvas.drawLine(0, actualHeight /2, actualWidth, actualHeight/2, axisPaint);
        canvas.drawLine(actualWidth/2, 0, actualWidth / 2, actualHeight, axisPaint);
    }

    //Draw all points on the canvas
    private void addPointsToCanvas(Canvas canvas)
    {
        if(canvas == null)
        {
            return;
        }
        for(PitPoint p : pointsList)
        {
            canvas.drawCircle(p.x, p.y, pointRadius, pointsPaint);
        }
    }

    //Draw all edges between the points on the canvas
    private void addEdgesBetweenPoints(Canvas canvas)
    {
        if((canvas == null) || (pointsList.size() <= 0))
        {
            return;
        }
        int size = pointsList.size();
        for(int i = 0; i < size; i++)
        {
            if(i + 1 < size)
            {
                PitPoint p1 = pointsList.get(i);
                PitPoint p2 = pointsList.get(i + 1);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, edgesPaint);
            }
        }
    }

    //A method for the View's user to add a new point at (0,0)
    public void addNewPointToGraph()
    {
        addNewPointToGraph(0,0);
    }

    //A method for the View's user to add a new point at his chosen coordinates
    public void addNewPointToGraph(float x, float y)
    {
        float newX = getRelativeX(x);
        float newY = getRelativeY(y);
        PitPoint point = new PitPoint(newX, newY);
        pointsList.add(point);
        Collections.sort(pointsList);
        updateMinMaxCoordinatesIfNeeded(newX, newY);
        invalidateAndRedraw();
    }

    //Tell the view it should redraw itself
    private void invalidateAndRedraw()
    {
        invalidate();
        requestLayout();
    }

    //A class implementing the view's touch listener. This class is responsible for detecting
    //when a user touches a point and move it around, redrawing itself and edges
    private class PointsOnTouchListener implements View.OnTouchListener
    {
        private float dx = 0;
        private float dy = 0;
        private float initialX, initialY;
        private PitPoint draggedPoint;
        private float ACCEPTABLE_CLICK_DISTANCE_FROM_POINT = 30 + pointRadius;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    draggedPoint = getPointOfCoordinates(event.getX(), event.getY());
                    if(draggedPoint != null)
                    {
                        dx = draggedPoint.x - event.getRawX();
                        dy = draggedPoint.y - event.getRawY();
                        initialX = event.getX();
                        initialY = event.getY();
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                {
                    if(draggedPoint != null)
                    {
                        float xPos = getCorrectNewXPos(event.getRawX() + dx);
                        float yPos = getCorrectNewYPos(event.getRawY() + dy);
                        draggedPoint.x = xPos;
                        draggedPoint.y = yPos;
                        Collections.sort(pointsList);
                        invalidateAndRedraw();
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                {
                    if(draggedPoint != null)
                    {
                        updateMinMaxCoordinatesIfNeeded(draggedPoint.x, draggedPoint.y);
                        draggedPoint = null;
                    }
                    break;
                }
            }
            return true;
        }

        //A method which prevents the user to drag the point off the views' X limit
        private float getCorrectNewXPos(float xPos)
        {
            if(xPos < 0)
            {
                return 0;
            }
            if(xPos > actualWidth)
            {
                return actualWidth - pointRadius;
            }
            return xPos;
        }

        //A method which prevents the user to drag the point off the views' Y limit
        private float getCorrectNewYPos(float yPos)
        {
            if(yPos < 0)
            {
                return 0;
            }
            if(yPos > actualHeight)
            {
                return actualHeight - pointRadius;
            }
            return yPos;
        }

        //A method which returns the point the user is touching, if he touches close enough to the point
        private PitPoint getPointOfCoordinates(float pressedX, float pressedY)
        {
            if(!isCoordinatesInRangeOfPoints(pressedX, pressedY))
            {
                return null;
            }
            for(PitPoint p : pointsList)
            {
                if(Math.abs(pressedX - p.x) <= ACCEPTABLE_CLICK_DISTANCE_FROM_POINT && Math.abs(pressedY - p.y) <= ACCEPTABLE_CLICK_DISTANCE_FROM_POINT)
                {
                    return p;
                }
            }
            return null;
        }

        //A method to check if the (x,y) the user pressed is in the range of the points in the pit.
        //If return is false, there is no point to even begin iterating all over the points.
        private boolean isCoordinatesInRangeOfPoints(float pressedX, float pressedY)
        {
            if(pressedX > maxXWithPoint + ACCEPTABLE_CLICK_DISTANCE_FROM_POINT)
            {
                return false;
            }
            if(pressedX < minXWithPoint - ACCEPTABLE_CLICK_DISTANCE_FROM_POINT)
            {
                return false;
            }
            if(pressedY > maxYWithPoint + ACCEPTABLE_CLICK_DISTANCE_FROM_POINT)
            {
                return false;
            }
            if(pressedY < minYWithPoint - ACCEPTABLE_CLICK_DISTANCE_FROM_POINT)
            {
                return false;
            }
            return true;
        }
    }

    //This method checks if given (x,y) is larger or smaller than current max/min (x,y).
    // If they are, the min/max will be updated
    private void updateMinMaxCoordinatesIfNeeded(float x, float y)
    {
        if(x > maxXWithPoint)
        {
            maxXWithPoint = x;
        }
        else if(x < minXWithPoint)
        {
            minXWithPoint = x;
        }
        if(y > maxYWithPoint)
        {
            maxYWithPoint = y;
        }
        else if(y < minYWithPoint)
        {
            minYWithPoint = y;
        }
    }

    //A class to represent a single points data. No use of the android.graphics.Point class for finer control
    private class PitPoint implements Comparable<PitPoint>
    {
        public float x;
        public float y;
        public final float DIFFERENCE = 0.0000001f;

        public PitPoint()
        {
            x = 0;
            y = 0;
        }

        public PitPoint(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        //A point is considered "bigger" when its X position is to the right of the other point
        @Override
        public int compareTo(PitPoint other)
        {
            if(other == null)
            {
                return 1;
            }
            return (int) (this.x - other.x);
        }
    }

}
