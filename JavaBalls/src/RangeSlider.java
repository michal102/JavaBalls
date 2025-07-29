import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class RangeSliderPanel extends JPanel {

    private String leftLabel = null;
    private String rightLabel = null;
    private Color leftColor = null;
    private Color middleColor = null;
    private Color rightColor = null;


    private int minValue = 1;
    private int maxValue = 100;
    private int lowerValue = 1;
    private int upperValue = 20;

    private final int sliderHeight = 5;
    private final int thumbRadius = 8;

    private boolean draggingLowerThumb = false;
    private boolean draggingUpperThumb = false;

    // Listeners
    private java.util.List<RangeSliderChangeListener> listeners = new java.util.ArrayList<>();

    public RangeSliderPanel(int savedLowerValue, int savedUpperValue)
    {
        setPreferredSize(new Dimension(400, 80));
        setBackground(Color.BLACK);

        this.lowerValue = savedLowerValue;
        this.upperValue = savedUpperValue;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingLowerThumb = false;
                draggingUpperThumb = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        });
    }

    public void setZoneLabels(String left, String right) {
        this.leftLabel = left;
        this.rightLabel = right;
    }

    public void setZoneColors(Color left, Color middle, Color right) {
        this.leftColor = left;
        this.middleColor = middle;
        this.rightColor = right;
    }

    public void setSliderWidth(int width) {
        Dimension newSize = new Dimension(width, getPreferredSize().height);
        setPreferredSize(newSize);
        setMaximumSize(newSize);
        setMinimumSize(newSize);
        revalidate(); // Notify parent layout manager
        repaint();
    }

    public void addRangeSliderChangeListener(RangeSliderChangeListener listener) {
        listeners.add(listener);
    }

    public void removeRangeSliderChangeListener(RangeSliderChangeListener listener) {
        listeners.remove(listener);
    }

    private void handleMousePress(MouseEvent e) {
        if (isNearThumb(e, lowerValue)) {
            draggingLowerThumb = true;
        } else if (isNearThumb(e, upperValue)) {
            draggingUpperThumb = true;
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        int newValue = getValueFromMouse(e.getX());

        if (draggingLowerThumb) {
            lowerValue = Math.max(minValue, Math.min(newValue, maxValue));

            // Check if thumbs are equal
            if (lowerValue == upperValue) {
                upperValue = Math.min(maxValue, lowerValue - 1); // Nudge upper thumb
            }
        } else if (draggingUpperThumb) {
            upperValue = Math.max(minValue, Math.min(newValue, maxValue));

            // Check if thumbs are equal
            if (upperValue == lowerValue) {
                lowerValue = Math.max(minValue, upperValue + 1); // Nudge lower thumb
            }
        }

        if (lowerValue > upperValue) {
            int temp = lowerValue;
            lowerValue = upperValue;
            upperValue = temp;

            // Swap the dragging thumb
            draggingLowerThumb = !draggingLowerThumb;
            draggingUpperThumb = !draggingUpperThumb;
        }

        // Notify listeners about the change
        notifyRangeChanged();

        repaint();
    }

    private void notifyRangeChanged() {
        for (RangeSliderChangeListener listener : listeners) {
            listener.rangeChanged(lowerValue, upperValue);
        }
    }

    private boolean isNearThumb(MouseEvent e, int value) {
        int xPos = mapValueToX(value);
        return Math.abs(e.getX() - xPos) < thumbRadius;
    }

    private int getValueFromMouse(int x) {
        int value = (int) ((x - thumbRadius) / (double) (getWidth() - 2 * thumbRadius) * (maxValue - minValue) + minValue);
        return Math.max(minValue, Math.min(value, maxValue));
    }

    private int mapValueToX(int value) {
        return (int) ((value - minValue) / (double) (maxValue - minValue) * (getWidth() - 2 * thumbRadius)) + thumbRadius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw the slider bar
        g2d.setColor(Color.GRAY);
        g2d.fillRect(thumbRadius, getHeight() / 2 - sliderHeight / 2, getWidth() - 2 * thumbRadius, sliderHeight);

        int y = getHeight() / 2 - sliderHeight / 2;
        int leftX = thumbRadius;
        int lowerX = mapValueToX(lowerValue);
        int upperX = mapValueToX(upperValue);
        int rightX = getWidth() - thumbRadius;

// Left zone
        if (leftColor != null && lowerX > leftX) {
            g2d.setColor(leftColor);
            g2d.fillRect(leftX, y, lowerX - leftX, sliderHeight);
        }

// Middle zone
        Color midColor = middleColor != null ? middleColor : new Color(255, 255, 255, 200);
        if (upperX > lowerX) {
            g2d.setColor(midColor);
            g2d.fillRect(lowerX, y, upperX - lowerX, sliderHeight);
        }

// Right zone
        if (rightColor != null && rightX > upperX) {
            g2d.setColor(rightColor);
            g2d.fillRect(upperX, y, rightX - upperX, sliderHeight);
        }


        // Draw the lower and upper thumbs
        g2d.setColor(Color.BLUE);
        g2d.fillOval(mapValueToX(lowerValue) - thumbRadius, getHeight() / 2 - thumbRadius, 2 * thumbRadius, 2 * thumbRadius);
        g2d.setColor(Color.RED);
        g2d.fillOval(mapValueToX(upperValue) - thumbRadius, getHeight() / 2 - thumbRadius, 2 * thumbRadius, 2 * thumbRadius);

        // Optionally, display the values
        g2d.setColor(Color.WHITE);
        g2d.drawString("Min Size: " + lowerValue, 10, 20);
        g2d.drawString("Max Size: " + upperValue, getWidth() - 70, 20);


        //labels

        if (leftLabel != null) {
            g2d.setColor(Color.WHITE);
            g2d.drawString(leftLabel, lowerX - 15, getHeight() / 2 + 25);
        }
        if (rightLabel != null) {
            g2d.setColor(Color.WHITE);
            g2d.drawString(rightLabel, upperX - 10, getHeight() / 2 + 25);
        }

    }

    public int getLowerValue() {
        return lowerValue;
    }

    public int getUpperValue() {
        return upperValue;
    }

    interface RangeSliderChangeListener {
        void rangeChanged(int lowerValue, int upperValue);
    }

}



