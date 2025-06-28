import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

class SettingsWindow {

    public SettingsWindow(Panel panel) {
        // Settings frame
        JFrame settingsFrame = new JFrame("Settings Window");
        settingsFrame.setResizable(false);

        settingsFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panel.setSettingsOpened(false);
                settingsFrame.setVisible(false);
                settingsFrame.dispose();
            }
        });

        SettingsPanel settingsPanel = new SettingsPanel(panel);
        panel.settingsFrame = settingsFrame;

        // Outer panel with padding
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 1)); // top, left, bottom, right
        outerPanel.setBackground(Color.BLACK); // Match the SettingsPanel background
        outerPanel.add(settingsPanel, BorderLayout.CENTER);

        // Scroll pane setup
        JScrollPane scrollPane = new JScrollPane(outerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(6); // smoother scrolling

        // Apply custom minimalist scrollbar UI
        JScrollBar vScrollBar = scrollPane.getVerticalScrollBar();
        vScrollBar.setPreferredSize(new Dimension(4, Integer.MAX_VALUE)); // force scrollbar width

        vScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(255, 255, 255, 255); // light gray, slightly transparent
                this.trackColor = new Color(0, 0, 0, 255);    // dark background
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                button.setVisible(false);
                return button;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 5);

                g2.dispose();
            }
        });

        settingsFrame.getContentPane().add(scrollPane);

        settingsFrame.setPreferredSize(new Dimension(600, 500)); // Window size
        settingsFrame.pack();
        settingsFrame.setVisible(true);

        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }
}

class SettingsPanel extends JPanel
{
    private boolean resizing = false; // Flag for resizing status
    private final Panel mainPanel;

    private JSlider speedSlider;
    private JSlider quantitySlider;
    private RangeSliderPanel rangeSlider;
    private JCheckBox sweepAndPruneCheckBox; // Checkbox for toggle
    private JCheckBox aliasingCheckBox;
    private JCheckBox collisionCheckBox;
    private JCheckBox roundIntCheckBox;
    private JCheckBox overlapCollisionCheckBox;


    public SettingsPanel(Panel mainPanel)
    {
        this.mainPanel = mainPanel;

        //this.setLayout(new FlowLayout());
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        setBackground(Color.BLACK);

        addMouseListener(new SettingsPanel.Event());
        addKeyListener(new SettingsPanel.Event());

        setFocusable(true);
        requestFocusInWindow();

        // Add a ComponentListener to handle resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Handle resizing logic here
                repaint(); // Repaint the panel after resizing
            }
        });

        // Speed Slider
        speedSlider = new JSlider(2, 22, mainPanel.getMAX_SPEED());
        speedSlider.setFocusable(false);

        speedSlider.setMajorTickSpacing(4);
        speedSlider.setMinorTickSpacing(1);

        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        // Customize the slider thumb and track
        speedSlider.setPreferredSize(new Dimension(400, 80));


        // Customizing the slider background and foreground
        speedSlider.setBackground(Color.BLACK);
        speedSlider.setForeground(Color.WHITE);

        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int speedValue = speedSlider.getValue();
                mainPanel.setMAX_SPEED(speedValue);  // Update speed in main panel
            }
        });
        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(Color.WHITE);
        add(speedLabel);
        add(speedSlider);


        // ball quantity Slider
        quantitySlider = new JSlider(0, 1000, mainPanel.getHowMuchBall());
        quantitySlider.setFocusable(false);

        quantitySlider.setMajorTickSpacing(100);
        quantitySlider.setMinorTickSpacing(10);

        quantitySlider.setSnapToTicks(true);


        quantitySlider.setPaintTicks(true);
        quantitySlider.setPaintLabels(true);

        // Customize the slider thumb and track
        quantitySlider.setPreferredSize(new Dimension(400, 80));


        // Customizing the slider background and foreground
        quantitySlider.setBackground(Color.BLACK);
        quantitySlider.setForeground(Color.WHITE);

        quantitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int quantitySliderValue = Math.max(quantitySlider.getValue(),1);
                mainPanel.setHowMuchBall(quantitySliderValue); // Update quantity in main panel
            }
        });
        JLabel quantityLabel = new JLabel("How much Ball?! :");
        quantityLabel.setForeground(Color.WHITE);
        add(quantityLabel);
        add(quantitySlider);


        // size range slider
        rangeSlider = new RangeSliderPanel(mainPanel.MIN_SIZE, mainPanel.MAX_SIZE);
        rangeSlider.addRangeSliderChangeListener(new RangeSliderPanel.RangeSliderChangeListener() {
            @Override
            public void rangeChanged(int lowerValue, int upperValue) {
                mainPanel.MAX_SIZE = upperValue;
                mainPanel.MIN_SIZE = lowerValue;
            }
        });
        speedSlider.setFocusable(false);

        add(rangeSlider);


        // Checkbox for sweepAndPrune
        sweepAndPruneCheckBox = new JCheckBox("Optimised collision detection (Sweep and Prune)");
        sweepAndPruneCheckBox.setBackground(Color.BLACK);
        sweepAndPruneCheckBox.setForeground(Color.WHITE);
        sweepAndPruneCheckBox.setFocusable(false);
        sweepAndPruneCheckBox.setSelected(true);

        sweepAndPruneCheckBox.addActionListener(e -> {
            boolean selected = sweepAndPruneCheckBox.isSelected();
            mainPanel.setSweepAndPrune(selected);
            //System.out.println("Sweep and Prune mode: " + selected);
        });

        add(sweepAndPruneCheckBox);


        // Checkbox for aliasing
        aliasingCheckBox = new JCheckBox("Aliasing");
        aliasingCheckBox.setBackground(Color.BLACK);
        aliasingCheckBox.setForeground(Color.WHITE);
        aliasingCheckBox.setFocusable(false);
        aliasingCheckBox.setSelected(true);

        aliasingCheckBox.addActionListener(e -> {
            boolean selected = aliasingCheckBox.isSelected();
            mainPanel.setAliasing(selected);
        });

        add(aliasingCheckBox);


        // Checkbox for collision
        collisionCheckBox = new JCheckBox("Elastic Collision");
        collisionCheckBox.setBackground(Color.BLACK);
        collisionCheckBox.setForeground(Color.WHITE);
        collisionCheckBox.setFocusable(false);
        collisionCheckBox.setSelected(true);

        collisionCheckBox.addActionListener(e -> {
            boolean selected = collisionCheckBox.isSelected();
            mainPanel.setElasticCollisions(selected);
            overlapCollisionCheckBox.setVisible(selected); // Show/hide overlap checkbox
        });

        add(collisionCheckBox);

        // Checkbox for overlap Collisions (initially hidden)
        overlapCollisionCheckBox = new JCheckBox("Overlap Collisions");
        overlapCollisionCheckBox.setBackground(Color.BLACK);
        overlapCollisionCheckBox.setForeground(Color.WHITE);
        overlapCollisionCheckBox.setFocusable(false);
        overlapCollisionCheckBox.setSelected(false);
        overlapCollisionCheckBox.setVisible(true); // hidden by default

        overlapCollisionCheckBox.addActionListener(e -> {
            boolean selected = overlapCollisionCheckBox.isSelected();
            mainPanel.setOverlapCollisions(selected);
        });

        add(overlapCollisionCheckBox);

        // Checkbox for round to int
        roundIntCheckBox = new JCheckBox("Round Speed Spawn to Int");
        roundIntCheckBox.setBackground(Color.BLACK);
        roundIntCheckBox.setForeground(Color.WHITE);
        roundIntCheckBox.setFocusable(false);
        roundIntCheckBox.setSelected(false);

        roundIntCheckBox.addActionListener(e -> {
            boolean selected = roundIntCheckBox.isSelected();
            mainPanel.setRoundSpeedToInt(selected);
        });

        add(roundIntCheckBox);


        // Add spacing after the previous checkbox group
        add(Box.createVerticalStrut(20));

        // Stroke Width Slider (supports float values using scaled integers)
        JLabel strokeLabel = new JLabel("Stroke Width:");
        strokeLabel.setForeground(Color.WHITE);
        add(strokeLabel);

        // Simulate float values from 0.1 to 10.0 using integers (e.g., 0.1 -> 1, 10.0 -> 100)
        int floatFactor = 10;
        float minStroke = 0.1f;
        float maxStroke = 10.0f;

        int sliderMin = (int) (minStroke * floatFactor);   // 1
        int sliderMax = (int) (maxStroke * floatFactor);   // 100
        int sliderInit = (int) (mainPanel.getStrokeWidth() * floatFactor);

        JSlider strokeSlider = new JSlider(sliderMin, sliderMax, sliderInit);
        strokeSlider.setMajorTickSpacing(10); // Represents 1.0 units
        strokeSlider.setMinorTickSpacing(5);  // Represents 0.5 units
        strokeSlider.setPaintTicks(true);

        // Create custom labels showing clean float values (e.g., 1.0, 2.0, ..., 10.0)
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 10; i <= 100; i += 10) { // exact steps for 1.0 to 10.0
            float labelValue = i / (float) floatFactor;
            JLabel label = new JLabel(String.format("%.1f", labelValue));
            label.setForeground(Color.WHITE);
            labelTable.put(i, label);
        }
        strokeSlider.setLabelTable(labelTable);
        strokeSlider.setPaintLabels(true);

        strokeSlider.setBackground(Color.BLACK);
        strokeSlider.setForeground(Color.WHITE);
        strokeSlider.setPreferredSize(new Dimension(400, 80));
        strokeSlider.setFocusable(false);

        // Update stroke width as float
        strokeSlider.addChangeListener(e -> {
            float strokeValue = strokeSlider.getValue() / (float) floatFactor;
            mainPanel.setStrokeWidth(strokeValue);
            mainPanel.repaint(); // immediate visual feedback
        });

        add(strokeSlider);


    }

//    private void updateStrokeWidth(JTextField field, Panel mainPanel) {
//        try {
//            float val = Float.parseFloat(field.getText());
//            if (val > 0) {
//                mainPanel.setStrokeWidth(val);
//                // Optionally repaint to see immediate effect
//                mainPanel.repaint();
//            } else {
//                // Reset to previous valid value if input invalid
//                field.setText(Float.toString(mainPanel.getStrokeWidth()));
//            }
//
//        }
//        catch (NumberFormatException ex) {
//            // Reset to previous valid value if input invalid
//            field.setText(Float.toString(mainPanel.getStrokeWidth()));
//        }
//    }

    private class Event implements MouseListener, ActionListener, KeyListener {
        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mousePressed(MouseEvent e)
        {
            requestFocusInWindow();
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        }
        @Override
        public void mouseEntered(MouseEvent e) {
        }
        @Override
        public void mouseExited(MouseEvent e) {
        }
        @Override
        public void actionPerformed(ActionEvent e) {

        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e)
        {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_ESCAPE)
            {
                mainPanel.setSettingsOpened(false);
                SwingUtilities.getWindowAncestor(SettingsPanel.this).setVisible(false);
                SwingUtilities.getWindowAncestor(SettingsPanel.this).dispose();
            }
            else if(keyCode == KeyEvent.VK_SPACE)
            {
               SwingUtilities.getWindowAncestor(mainPanel).toFront();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
    }
}