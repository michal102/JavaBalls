import javax.sound.sampled.Line;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

class SettingsWindow {

    public SettingsWindow(Panel panel) {
        // Settings frame
        JFrame settingsFrame = new JFrame("Settings Window");
        settingsFrame.setResizable(false);

        settingsFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                panel.setSettingsOpened(false);
                // handle closing the window
                settingsFrame.setVisible(false);
                settingsFrame.dispose();
            }
        });

        SettingsPanel settingsPanel = new SettingsPanel(panel);
        panel.settingsFrame = settingsFrame;

        settingsFrame.getContentPane().add(settingsPanel);
        settingsFrame.setPreferredSize(new Dimension(550, 400));
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

    public SettingsPanel(Panel mainPanel)
    {
        this.mainPanel = mainPanel;

        this.setLayout(new FlowLayout());

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
        speedSlider = new JSlider(2, 50, mainPanel.getMAX_SPEED());
        speedSlider.setFocusable(false);

        speedSlider.setMajorTickSpacing(6);
        speedSlider.setMinorTickSpacing(2);

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

    }

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