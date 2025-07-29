import kuusisto.tinysound.TinySound;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;

class Okno {
    public static void main(String[] args) {

        JFrame frame = new JFrame("BALLS!");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                TinySound.shutdown(); // Stop audio engine
                frame.dispose();
                System.exit(0);
            }
        });

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
        frame.setPreferredSize(new Dimension(1200, 800));
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
    int MAX_SIZE = 35;
    int MIN_SIZE = 20;
    private int MAX_SPEED = 4;
    private int howMuchBall = 500;
    private boolean resizing = false; // Flag for resizing status
    private boolean aliasing = true;
    private boolean sweepAndPrune = true;
    private boolean elasticCollisions = true;
    float strokeWidth = 2f;
    boolean roundSpeedToInt = false;
    boolean overlapCollisions = false;

    private boolean sweepDirectionLeftToRight = true;


    public boolean isOverlapCollisions() {
        return overlapCollisions;
    }

    public void setOverlapCollisions(boolean overlapCollisions) {
        this.overlapCollisions = overlapCollisions;
    }

    public boolean isRoundSpeedToInt() {
        return roundSpeedToInt;
    }

    public void setRoundSpeedToInt(boolean roundSpeedToInt) {
        this.roundSpeedToInt = roundSpeedToInt;
    }


    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public boolean isElasticCollisions() {return elasticCollisions;}

    public void setElasticCollisions(boolean elasticCollisions) {this.elasticCollisions = elasticCollisions;}


    public boolean isAliasing() {
        return aliasing;
    }

    public void setAliasing(boolean aliasing) {
        this.aliasing = aliasing;
    }

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

        Graphics2D g2d = (Graphics2D) g;

        if (aliasing) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // Set the stroke (line thickness)
         // Change this to your desired thickness
        g2d.setStroke(new BasicStroke(strokeWidth));

        for (Kula k : listaKul) {
            g2d.setColor(k.color);
            g2d.drawOval((int) k.x, (int) k.y, (int) k.size, (int) k.size);
        }

        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(1.0f)); // Reset to default for text
        g2d.drawString(Integer.toString(listaKul.size()), 40, 40);
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

    private void ensureMinimumVelocity(Kula k) {
        double minSpeed = 0.5f; // tune this to fit visual feedback
        if (Math.abs(k.xspeed) < minSpeed) {
            k.xspeed = (k.xspeed >= 0 ? 1 : -1) * minSpeed;
        }
        if (Math.abs(k.yspeed) < minSpeed) {
            k.yspeed = (k.yspeed >= 0 ? 1 : -1) * minSpeed;
        }
    }

    private void resolveElasticCollision(Kula a, Kula b) {
//        // Calculate mass based on size (assumed proportional)
//        double massA = a.size;
//        double massB = b.size;
//
//        double vAx = a.xspeed;
//        double vAy = a.yspeed;
//        double vBx = b.xspeed;
//        double vBy = b.yspeed;
//
//        // Elastic collision formulas
//        double newAx = (vAx * (massA - massB) + 2 * massB * vBx) / (massA + massB);
//        double newAy = (vAy * (massA - massB) + 2 * massB * vBy) / (massA + massB);
//        double newBx = (vBx * (massB - massA) + 2 * massA * vAx) / (massA + massB);
//        double newBy = (vBy * (massB - massA) + 2 * massA * vAy) / (massA + massB);
//
//        // Set updated velocities
//        a.xspeed = newAx;
//        a.yspeed = newAy;
//        b.xspeed = newBx;
//        b.yspeed = newBy;

        // Get mass (we'll use size as a proxy for mass)
        double m1 = a.size;
        double m2 = b.size;

        // Position vectors
        double x1 = a.x + a.size / 2, y1 = a.y + a.size / 2;
        double x2 = b.x + b.size / 2, y2 = b.y + b.size / 2;

        // Velocity vectors
        double vx1 = a.xspeed, vy1 = a.yspeed;
        double vx2 = b.xspeed, vy2 = b.yspeed;

        // Normal vector
        double nx = x2 - x1;
        double ny = y2 - y1;
        double dist = Math.sqrt(nx * nx + ny * ny);
        if (dist == 0) return; // Avoid divide by zero

        // Normalize
        nx /= dist;
        ny /= dist;


        if(!overlapCollisions)
        {
            // Relative velocity in normal direction
            double relVel = (vx2 - vx1) * nx + (vy2 - vy1) * ny;

            // Only resolve if moving toward each other
            if (relVel > 0) return;
        }


        // Relative velocity
        double tx = -ny; // Tangent vector
        double ty = nx;

        // Dot product tangential
        double dpTan1 = vx1 * tx + vy1 * ty;
        double dpTan2 = vx2 * tx + vy2 * ty;

        // Dot product normal
        double dpNorm1 = vx1 * nx + vy1 * ny;
        double dpNorm2 = vx2 * nx + vy2 * ny;

        // Conservation of momentum in 1D for the normal direction
        double mTotal = m1 + m2;
        double newNorm1 = (dpNorm1 * (m1 - m2) + 2 * m2 * dpNorm2) / mTotal;
        double newNorm2 = (dpNorm2 * (m2 - m1) + 2 * m1 * dpNorm1) / mTotal;

//        ensureMinimumVelocity(a);
//        ensureMinimumVelocity(b);

        // Final velocity components
        a.xspeed = tx * dpTan1 + nx * newNorm1;
        a.yspeed = ty * dpTan1 + ny * newNorm1;
        b.xspeed = tx * dpTan2 + nx * newNorm2;
        b.yspeed = ty * dpTan2 + ny * newNorm2;
    }

    public void sweepAndPruneColl() {
        // Step 1: Sort balls by their minimum x position (x)
        listaKul.sort(Comparator.comparingDouble(k -> k.x));

        // Step 2: Sweep in alternating directions
        if (sweepDirectionLeftToRight) {
            for (int i = 0; i < listaKul.size(); i++) {
                Kula a = listaKul.get(i);
                double aMaxX = a.x + a.size;

                for (int j = i + 1; j < listaKul.size(); j++) {
                    Kula b = listaKul.get(j);
                    if (b.x > aMaxX) break;

                    handlePotentialCollision(a, b);
                }
            }
        } else {
            for (int i = listaKul.size() - 1; i >= 0; i--) {
                Kula a = listaKul.get(i);
                double aMaxX = a.x + a.size;

                for (int j = i - 1; j >= 0; j--) {
                    Kula b = listaKul.get(j);
                    if (a.x > b.x + b.size) break;

                    handlePotentialCollision(a, b);
                }
            }
        }

        // Toggle direction for next frame
        sweepDirectionLeftToRight = !sweepDirectionLeftToRight;
    }


    // Helper method to handle actual collision check and resolution
    private void handlePotentialCollision(Kula a, Kula b) {
        if (a == b) return;

        double axCenter = a.x + a.size / 2;
        double ayCenter = a.y + a.size / 2;
        double bxCenter = b.x + b.size / 2;
        double byCenter = b.y + b.size / 2;

        double dx = axCenter - bxCenter;
        double dy = ayCenter - byCenter;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double rsum = a.size / 2.0 + b.size / 2.0;

        if (distance <= rsum && distance > 0.00001) {
            if (distance <= rsum) {
                double overlap = rsum - distance;
                Vector2 offset = new Vector2(dx, dy);
                offset.normalise();
                offset.scale(overlap);

                a.x += offset.x / 2.0;
                a.y += offset.y / 2.0;
                b.x -= offset.x / 2.0;
                b.y -= offset.y / 2.0;

                if (!elasticCollisions) {
                    double tempX = a.xspeed, tempY = a.yspeed;
                    a.xspeed = b.xspeed;
                    a.yspeed = b.yspeed;
                    b.xspeed = tempX;
                    b.yspeed = tempY;
                } else {
                    resolveElasticCollision(a, b);
                }

                a.collisionCount++;
                b.collisionCount++;

                //oldSoundManager.playCollisionSound(a, b);
                SoundManager.playCollisionSound(a, b, getWidth());
            }
        }
        else if (distance == 0) {
            // Balls are exactly on top of each other: separate arbitrarily
            double overlap = rsum;
            Vector2 offset = new Vector2(Math.random() - 0.5, Math.random() - 0.5);
            offset.normalise();
            offset.scale(overlap);

            a.x += offset.x / 2.0;
            a.y += offset.y / 2.0;
            b.x -= offset.x / 2.0;
            b.y -= offset.y / 2.0;
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

            SoundManager.resetSoundFrameCounter();
            repaint();
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e)
        {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_R) {
                //System.out.println("Resetting the balls!");
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

    public class Kula {
        public double x, y, size, xspeed = 0, yspeed = 0;
        public Color color;
        private double MAX_SPEED = 4;

        public long lastSoundTime = 0;


        public int getCollisionCount() {
            return collisionCount;
        }

        private int collisionCount = 0; // Track the number of collisions
        private final int MAX_COLLISIONS = 20; // Max number of collisions to fully turn red
        private final double DECAY_RATE = 1; // How quickly collision count decreases
        private final int MAX_DENSITY = 100; // Max density to shift hue

        private final float MIN_BRIGHTNESS = 0.5f; // Minimum brightness (to avoid full black)
        private final float MIN_HUE = 0.1f; // Minimum hue (to avoid no color)

        private double gravity = 9.81;

        public int getDensity() {
            return density;
        }

        private int density = 0;

        public double getSpeed() { return Math.sqrt(xspeed * xspeed + yspeed * yspeed); }


        public Kula(int x, int y, int size, int maxSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.MAX_SPEED = maxSpeed;
            //color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            color = Color.getHSBColor(240, 53, 43); // Initial color with medium hue (green) and brightness 50%

            if(roundSpeedToInt)
            {
                while(xspeed == 0) xspeed = (int) (Math.random() * MAX_SPEED * 2 - MAX_SPEED);
                while(yspeed == 0) yspeed = (int) (Math.random() * MAX_SPEED * 2 - MAX_SPEED);
            }
            else
            {
                while(xspeed == 0) xspeed = (Math.random() * MAX_SPEED * 2 - MAX_SPEED);
                while(yspeed == 0) yspeed = (Math.random() * MAX_SPEED * 2 - MAX_SPEED);
            }

        }

        public void bruteForceColl(int indeks)
        {

            for(int i = indeks+1; i < listaKul.size() ; i++)
            {
                double currx = (x + size/2), curry = (y + size/2);
                double otherx = (listaKul.get(i).x + listaKul.get(i).size/2), othery = (listaKul.get(i).y + listaKul.get(i).size/2);

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

                    double pomXspeed = xspeed, pomYspeed = yspeed;

                    if(!elasticCollisions)
                    {
                        //tranfer vectors
                        xspeed = listaKul.get(i).xspeed; yspeed = listaKul.get(i).yspeed;
                        listaKul.get(i).xspeed = pomXspeed; listaKul.get(i).yspeed = pomYspeed;

                        SoundManager.playCollisionSound(this, listaKul.get(i), getWidth());
                    }
                    else
                    {
//                        // Calculate new velocities after elastic collision
//                        double newXSpeed = ((size - kula.size) * xspeed + 2 * kula.size * kula.xspeed) / (size + kula.size);
//                        double newYSpeed = ((size - kula.size) * yspeed + 2 * kula.size * kula.yspeed) / (size + kula.size);
//
//                        kula.xspeed = ((kula.size - size) * kula.xspeed + 2 * size * pomXspeed) / (size + kula.size);
//                        kula.yspeed = ((kula.size - size) * kula.yspeed + 2 * size * pomYspeed) / (size + kula.size);
//
//                        // Apply a threshold to avoid stopping due to small velocities
//                        final double VELOCITY_THRESHOLD = 0.01;  // Small value to detect near-zero velocities
//
//                        // If velocities are too small, set them to zero
//                        if (Math.abs(newXSpeed) < VELOCITY_THRESHOLD) {
//                            newXSpeed = 0;
//                        }
//                        if (Math.abs(newYSpeed) < VELOCITY_THRESHOLD) {
//                            newYSpeed = 0;
//                        }
//
//                        if (Math.abs(kula.xspeed) < VELOCITY_THRESHOLD) {
//                            kula.xspeed = 0;
//                        }
//                        if (Math.abs(kula.yspeed) < VELOCITY_THRESHOLD) {
//                            kula.yspeed = 0;
//                        }
//
//                        // Now apply the calculated speeds
//                        xspeed = (int)newXSpeed;
//                        yspeed = (int)newYSpeed;
                        resolveElasticCollision(this,listaKul.get(i));
                        SoundManager.playCollisionSound(this, listaKul.get(i), getWidth());
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
            x += (xspeed * deltatime * 100);
            y += (yspeed * deltatime * 100);
            //y += (int)(gravity * deltatime * 100);

            if (x <= 0 || x + size >= getWidth()) {
                if (x <= 0) x = 0;
                else x = getWidth() - size;
                xspeed = -xspeed;

                collisionCount++;

                //oldSoundManager.playWallSound(this);
                SoundManager.playWallSound(this, getWidth());
            }
            if (y <= 0 || y + size >= getHeight()) {
                if(y <= 0) y = 0;
                else y = getHeight() - size;
                yspeed = -yspeed;

                collisionCount++;

                //oldSoundManager.playWallSound(this);
                SoundManager.playWallSound(this, getWidth());
            }

            if(!sweepAndPrune) bruteForceColl(indeks);

//            ensureMinimumVelocity(listaKul.get(indeks));

            collisionCount -= DECAY_RATE;
            if (collisionCount < 0) collisionCount = 0;
            else if(collisionCount > MAX_COLLISIONS) collisionCount = MAX_COLLISIONS;

            // Calculate density (how many balls are within a certain distance)
            density = 0;
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