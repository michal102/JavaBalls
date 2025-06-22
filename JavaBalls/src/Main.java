import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;

class Okno {
    public static void main(String[] args) {

        JFrame frame = new JFrame("BALLS!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("*Windows*".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e) {}

        /*
        frame.getContentPane().add(new Panel());
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setVisible(true);

        frame.setFocusable(true); // Make the JPanel focusable
        frame.requestFocus();
        frame.addKeyListener(new KeyAdapter() {});
        */

        Panel panel = new Panel();
        frame.getContentPane().add(panel);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setVisible(true);


        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }
}

class Panel extends JPanel {
    private ArrayList<Kula> listaKul;
    private int size = 40;
    private Timer timer;
    private long lastTime; // To store the last update time
    private double deltatime; // Time between updates in seconds
    private final int DELAY = 1000 / 144; // Target for 60 FPS
    //dla 30fps -> 1s/30 = 0,033s
    int MAX_SIZE = 20;
    int MIN_SIZE = 1;
    private int MAX_SPEED = 4;
    private int howMuchBall = 500;
    private boolean resizing = false; // Flag for resizing status
    private boolean aliasing = true;
    private boolean sweepAndPrune = true;

    public boolean isAliasing() {
        return aliasing;
    }

    public void setAliasing(boolean aliasing) {
        this.aliasing = aliasing;
    }

    private boolean simpleCollisions = true;

    public boolean isSweepAndPrune() {
        return sweepAndPrune;
    }

    public void setSweepAndPrune(boolean sweepAndPrune) {
        this.sweepAndPrune = sweepAndPrune;
    }

    boolean settingsOpened = false;
    JFrame settingsFrame;


    public Panel() {
        listaKul = new ArrayList<>();
        setBackground(Color.BLACK);
        addMouseListener(new Event());
        addKeyListener(new Event());
        timer = new Timer(DELAY, new Event());
        lastTime = System.nanoTime(); // Initialize the last update time
        timer.start();

        // Add a ComponentListener to handle resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Handle resizing logic here
                repaint(); // Repaint the panel after resizing
            }
        });
    }


    public boolean isSettingsOpened() {
        return settingsOpened;
    }

    public void setSettingsOpened(boolean settingsOpened) {
        this.settingsOpened = settingsOpened;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw each ball on the panel, dynamically adjusting based on the panel size

        Graphics2D g2d = (Graphics2D) g;

        if(aliasing) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Kula k : listaKul) {
            g.setColor(k.color);
            g.drawOval(k.x, k.y, k.size, k.size);
        }
        g.setColor(Color.YELLOW);
        g.drawString(Integer.toString(listaKul.size()), 40, 40);
    }

    /*
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Kula k : listaKul) {
            g.setColor(k.color);
            g.drawOval(k.x, k.y, k.size, k.size);
        }
        g.setColor(Color.YELLOW);
        g.drawString(Integer.toString(listaKul.size()),40,40);
    }*/

    public void sweepAndPruneColl() {
        // Step 1: Sort balls by their minimum x position (x)
        listaKul.sort(Comparator.comparingInt(k -> k.x));

        // Step 2: Sweep through the list and compare only overlapping intervals
        for (int i = 0; i < listaKul.size(); i++) {
            Kula a = listaKul.get(i);
            int aMinX = a.x;
            int aMaxX = a.x + a.size;

            for (int j = i + 1; j < listaKul.size(); j++) {
                Kula b = listaKul.get(j);
                int bMinX = b.x;

                // If b starts after a ends, no collision possible, break
                if (bMinX > aMaxX) break;

                // Otherwise, their x-ranges overlap, check full collision
                if (a != b) {
                    // Check circular collision (same as in handleCollision)
                    int axCenter = a.x + a.size / 2;
                    int ayCenter = a.y + a.size / 2;
                    int bxCenter = b.x + b.size / 2;
                    int byCenter = b.y + b.size / 2;

                    double dx = axCenter - bxCenter;
                    double dy = ayCenter - byCenter;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    double rsum = a.size / 2.0 + b.size / 2.0;

                    if (distance <= rsum) {
                        // Either use a.handleCollision(...) or inline your logic
                        // To avoid infinite recursion, inline a lightweight version here

                        double overlap = rsum - distance;
                        Vector2 offset = new Vector2(dx, dy);
                        offset.normalise();
                        offset.scale(overlap);

                        a.x += (int)(offset.x / 2);
                        a.y += (int)(offset.y / 2);
                        b.x -= (int)(offset.x / 2);
                        b.y -= (int)(offset.y / 2);

                        // Simple collision: swap velocities
                        if (simpleCollisions) {
                            int tempX = a.xspeed, tempY = a.yspeed;
                            a.xspeed = b.xspeed;
                            a.yspeed = b.yspeed;
                            b.xspeed = tempX;
                            b.yspeed = tempY;
                        } else {
                            // Elastic collision
                            int aSize = a.size, bSize = b.size;
                            int aXSpeed = a.xspeed, aYSpeed = a.yspeed;
                            int bXSpeed = b.xspeed, bYSpeed = b.yspeed;

                            a.xspeed = (int)(((aSize - bSize) * aXSpeed + 2 * bSize * bXSpeed) / (aSize + bSize));
                            a.yspeed = (int)(((aSize - bSize) * aYSpeed + 2 * bSize * bYSpeed) / (aSize + bSize));

                            b.xspeed = (int)(((bSize - aSize) * bXSpeed + 2 * aSize * aXSpeed) / (aSize + bSize));
                            b.yspeed = (int)(((bSize - aSize) * bYSpeed + 2 * aSize * aYSpeed) / (aSize + bSize));
                        }

                        a.collisionCount++;
                        b.collisionCount++;
                    }
                }
            }
        }
    }

    private class Event implements MouseListener, ActionListener, KeyListener
    {
        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mousePressed(MouseEvent e) {
            for(int i = 0; i < howMuchBall; i++){
                listaKul.add(new Kula(e.getX(), e.getY(), (int)(Math.random() * (MAX_SIZE - MIN_SIZE + 1)) + MIN_SIZE,MAX_SPEED));
                repaint();
            }
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
            long currentTime = System.nanoTime();
            deltatime = (currentTime - lastTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
            lastTime = currentTime;

            if(sweepAndPrune) sweepAndPruneColl();
            for (int i = 0; i < listaKul.size(); i++) {
                listaKul.get(i).update(i, deltatime); // Pass delta time to the update method
            }
            repaint();
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e)
        {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_R) {
                System.out.println("Resetting the balls!");
                listaKul.clear();
                repaint();
            }
            else if ((keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_SPACE) && !isSettingsOpened())
            {
                new SettingsWindow(Panel.this);
                settingsOpened = true;
            }
            else if (keyCode == KeyEvent.VK_ESCAPE && isSettingsOpened())
            {
                settingsFrame.setVisible(false);
                settingsFrame.dispose();
                settingsOpened = false;
            }
            else if (keyCode == KeyEvent.VK_SPACE && isSettingsOpened())
            {
                settingsFrame.toFront();
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {}
    }

    public int getBallSize() {
        return size;
    }

    public void setBallSize(int size) {
        this.size = size;
    }

    public int getMAX_SIZE() {
        return MAX_SIZE;
    }

    public int getMIN_SIZE() {
        return MIN_SIZE;
    }

    public int getMAX_SPEED() {
        return MAX_SPEED;
    }

    public void setMAX_SPEED(int MAX_SPEED) {
        this.MAX_SPEED = MAX_SPEED;
    }

    public int getHowMuchBall() {
        return howMuchBall;
    }

    public void setHowMuchBall(int howMuchBall) {
        this.howMuchBall = howMuchBall;
    }

    private class Kula {
        public int x, y, size, xspeed = 0, yspeed = 0;
        public Color color;
        private int MAX_SPEED = 4;

        private int collisionCount = 0; // Track the number of collisions
        private final int MAX_COLLISIONS = 20; // Max number of collisions to fully turn red
        private final double DECAY_RATE = 1; // How quickly collision count decreases
        private final int MAX_DENSITY = 100; // Max density to shift hue

        private final float MIN_BRIGHTNESS = 0.5f; // Minimum brightness (to avoid full black)
        private final float MIN_HUE = 0.1f; // Minimum hue (to avoid no color)

        private double gravity = 9.81;

        public Kula(int x, int y, int size, int maxSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.MAX_SPEED = maxSpeed;
            //color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            color = Color.getHSBColor(240, 53, 43); // Initial color with medium hue (green) and brightness 50%
            while(xspeed == 0) xspeed = (int) (Math.random() * MAX_SPEED * 2 - MAX_SPEED);
            while(yspeed == 0) yspeed = (int) (Math.random() * MAX_SPEED * 2 - MAX_SPEED);
        }

        public void bruteForceColl(int indeks)
        {

            for(int i = indeks+1; i < listaKul.size() ; i++)
            {
                int currx = (x + size/2), curry = (y + size/2);
                int otherx = (listaKul.get(i).x + listaKul.get(i).size/2), othery = (listaKul.get(i).y + listaKul.get(i).size/2);

                double distance = Math.sqrt(Math.pow((currx - otherx),2) + Math.pow(curry - othery,2)),
                        rsum =  ((size/2.0) + (listaKul.get(i).size/2.0));

                if(distance <= rsum)
                {
                    Kula kula = listaKul.get(i);
                    double overlap = (size / 2.0 + kula.size / 2.0) - distance;
                    Vector2 offsetVector = new Vector2();

                    //calculate offset
                    offsetVector.x = kula.x - x;
                    offsetVector.y = kula.y - y;

                    offsetVector.normalise();
                    offsetVector.scale(overlap);

                    //apply offset
                    x -= (int)offsetVector.x;
                    y -= (int)offsetVector.y;

                    int pomXspeed = xspeed, pomYspeed = yspeed;

                    if(simpleCollisions)
                    {
                        //tranfer vectors
                        xspeed = listaKul.get(i).xspeed; yspeed = listaKul.get(i).yspeed;
                        listaKul.get(i).xspeed = pomXspeed; listaKul.get(i).yspeed = pomYspeed;
                    }
                    else
                    {
                        // Calculate new velocities after elastic collision
                        double newXSpeed = ((size - kula.size) * xspeed + 2 * kula.size * kula.xspeed) / (size + kula.size);
                        double newYSpeed = ((size - kula.size) * yspeed + 2 * kula.size * kula.yspeed) / (size + kula.size);

                        kula.xspeed = ((kula.size - size) * kula.xspeed + 2 * size * pomXspeed) / (size + kula.size);
                        kula.yspeed = ((kula.size - size) * kula.yspeed + 2 * size * pomYspeed) / (size + kula.size);

                        // Apply a threshold to avoid stopping due to small velocities
                        final double VELOCITY_THRESHOLD = 0.01;  // Small value to detect near-zero velocities

                        // If velocities are too small, set them to zero
                        if (Math.abs(newXSpeed) < VELOCITY_THRESHOLD) {
                            newXSpeed = 0;
                        }
                        if (Math.abs(newYSpeed) < VELOCITY_THRESHOLD) {
                            newYSpeed = 0;
                        }

                        if (Math.abs(kula.xspeed) < VELOCITY_THRESHOLD) {
                            kula.xspeed = 0;
                        }
                        if (Math.abs(kula.yspeed) < VELOCITY_THRESHOLD) {
                            kula.yspeed = 0;
                        }

                        // Now apply the calculated speeds
                        xspeed = (int)newXSpeed;
                        yspeed = (int)newYSpeed;
                    }


                    this.collisionCount++;
                    kula.collisionCount++;
                    //distance = Math.sqrt(Math.pow((x - listaKul.get(i).x),2) + Math.pow(y - listaKul.get(i).y,2));
                    /*
                    while(distance < rsum){
                        x -= xspeed; y -= yspeed;
                        distance = Math.sqrt(Math.pow((x - listaKul.get(i).x),2) + Math.pow(y - listaKul.get(i).y,2));
                    }
                    */

                    /*if(distance > rsum){
                        x -= xspeed; y-= yspeed;
                    }*/

                }
            }
        }

        public void update(int indeks, double deltatime)
        {
            x += (int)(xspeed * deltatime * 100);
            y += (int)(yspeed * deltatime * 100);
            //y += (int)(gravity * deltatime * 100);

            if (x <= 0 || x + size >= getWidth()) {
                if (x <= 0) x = 0;
                else x = getWidth() - size;
                xspeed = -xspeed;

                collisionCount++;
            }
            if (y <= 0 || y + size >= getHeight()) {
                if(y <= 0) y = 0;
                else y = getHeight() - size;
                yspeed = -yspeed;

                collisionCount++;
            }

            if(!sweepAndPrune) bruteForceColl(indeks);


            collisionCount -= DECAY_RATE;
            if (collisionCount < 0) collisionCount = 0;
            else if(collisionCount > MAX_COLLISIONS) collisionCount = MAX_COLLISIONS;

            // Calculate density (how many balls are within a certain distance)
            int density = 0;
            for (Kula kula : listaKul) {
                if (kula != this) {
                    double distance = Math.sqrt(Math.pow(this.x - kula.x, 2) + Math.pow(this.y - kula.y, 2));
                    if (distance < 100) { // Consider balls within 100 pixels as part of the "density"
                        density++;
                    }
                }
            }

            updateColor(density);
        }

        private void updateColor(int density) {
            // Calculate brightness based on collision count (scaled to 0-1 range)
            float brightness = Math.max(MIN_BRIGHTNESS, Math.min(1.0f, collisionCount / (float) MAX_COLLISIONS));

            // Calculate hue based on density (scaled to 0-1 range)
            float hue = Math.max(MIN_HUE, Math.min(1.0f, density / (float) MAX_DENSITY));

            // Convert to HSB color model and adjust brightness and hue
            color = Color.getHSBColor(hue, 1.0f, brightness);
        }
    }
}



class Vector2
{
    public double x,y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2() {}

    void normalise()
    {
        double magnitude = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        x /= magnitude;
        y /= magnitude;
    }

    void scale(double s)
    {
        x *= s;
        y *= s;
    }
}