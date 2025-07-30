// === Modified SoundManager.java ===
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

import java.io.*;
import java.util.*;

public class SoundManager {
    private static final int MAX_SOUNDS_PER_FRAME = 5;
    private static final int MAX_WALL_SOUNDS_PER_FRAME = 100;
    private static final int MAX_SAME_TYPE_PER_FRAME = 2;
    private static final int COOLDOWN_MS = 200;

    private static final Map<String, List<Sound>> soundMap = new HashMap<>();
    private static final Map<String, Integer> soundUseCounterThisFrame = new HashMap<>();
    private static final Map<String, SoundTypeConfig> soundTypeConfig = new HashMap<>();
    private static final Random random = new Random();

    public static float getVolume() {
        return volume;
    }

    public static boolean isMuted() {
        return muted;
    }

    public static float volume = 0.35f;
    public static boolean muted = false;

    private static int soundsPlayedThisFrame = 0;
    private static int wallSoundsPlayedThisFrame = 0;

    public static class SoundTypeConfig {
        public float volumeMultiplier = 1.0f;
        public double sizeThreshold = 0.0;

        public SoundTypeConfig(float volumeMultiplier, double sizeThreshold) {
            this.volumeMultiplier = volumeMultiplier;
            this.sizeThreshold = sizeThreshold;
        }

    }

    public static Map<String, SoundTypeConfig> getSoundTypeConfigs() {
        return new HashMap<>(soundTypeConfig); // return a copy to avoid external modification
    }

    public static SoundTypeConfig getSoundTypeConfig(String name) {
        return soundTypeConfig.get(name);
    }

    public static void setSoundTypeConfig(String name, SoundTypeConfig config) {
        if (soundTypeConfig.containsKey(name)) {
            soundTypeConfig.put(name, config);
        }
    }

    static {
        TinySound.init();
        loadSound("click", "click1.wav", "click2.wav");
        loadSound("tap", "tap1.wav", "tap2.wav");
        loadSound("thump", "thump1.wav", "thump2.wav", "thump3.wav");
        loadSound("rumble", "rumble.wav");

        soundTypeConfig.put("click", new SoundTypeConfig(0.7f, 25));
        soundTypeConfig.put("tap", new SoundTypeConfig(0.5f, 70));
        soundTypeConfig.put("thump", new SoundTypeConfig(0.25f, 100));
        soundTypeConfig.put("rumble", new SoundTypeConfig(1f, 0));
    }

    private static void loadSound(String key, String... filenames) {
        List<Sound> sounds = new ArrayList<>();
        for (String filename : filenames) {
            try (InputStream is = SoundManager.class.getResourceAsStream("/sfx/" + filename)) {
                if (is == null) {
                    System.err.println("Sound resource not found: " + filename);
                    continue;
                }

                // Ensure file ends up fully written
                File tempFile = File.createTempFile("sound_", "_" + filename);
                tempFile.deleteOnExit();

                try (BufferedInputStream bis = new BufferedInputStream(is);
                     BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {

                    byte[] buffer = new byte[16384];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                }

                Sound s = TinySound.loadSound(tempFile);
                if (s != null) {
                    sounds.add(s);
                } else {
                    System.err.println("Failed to load sound from file: " + filename);
                }

            } catch (IOException e) {
                System.err.println("Error loading sound: " + filename);
                e.printStackTrace();
            }
        }
        soundMap.put(key, sounds);
    }

    private static void oldloadSound(String key, String... filenames) {
        List<Sound> sounds = new ArrayList<>();
        for (String filename : filenames) {
            File file = new File("src/sfx/" + filename);
            Sound s = TinySound.loadSound(file);
            if (s != null) {
                sounds.add(s);
            }
        }
        soundMap.put(key, sounds);
    }

    public static void resetSoundFrameCounter() {
        soundsPlayedThisFrame = 0;
        wallSoundsPlayedThisFrame = 0;
        soundUseCounterThisFrame.clear();
    }

    public static void playCollisionSound(Panel.Kula a, Panel.Kula b, int panelWidth) {
        if (muted || soundsPlayedThisFrame >= MAX_SOUNDS_PER_FRAME) return;

        long now = System.currentTimeMillis();
        if (now - a.lastSoundTime < COOLDOWN_MS && now - b.lastSoundTime < COOLDOWN_MS) return;
        a.lastSoundTime = now;
        b.lastSoundTime = now;

        Panel.Kula dominant = (a.size >= b.size) ? a : b;
        String soundKey = getSoundKeyForSize(dominant.size);

        int count = soundUseCounterThisFrame.getOrDefault(soundKey, 0);
        int maxPerType = Math.max(1, MAX_SAME_TYPE_PER_FRAME - (soundsPlayedThisFrame / 5));
        if (count >= maxPerType) return;
        soundUseCounterThisFrame.put(soundKey, count + 1);

        List<Sound> sounds = soundMap.get(soundKey);
        if (sounds == null || sounds.isEmpty()) return;

        Sound sound = sounds.get(random.nextInt(sounds.size()));

        double speed = (a.getSpeed() + b.getSpeed()) / 2.0;
        int avgDensity = (a.getDensity() + b.getDensity()) / 2;
        int avgCollisions = (a.getCollisionCount() + b.getCollisionCount()) / 2;

        float gain = calculateGain(speed, avgDensity, avgCollisions);
        float pan = calculatePan((a.x + b.x) / 2.0, panelWidth);
        float typeVolume = soundTypeConfig.getOrDefault(soundKey, new SoundTypeConfig(1.0f, 0)).volumeMultiplier;

        sound.play(gain * typeVolume * getGlobalLoudnessFactor(), pan);
        soundsPlayedThisFrame++;
    }

    public static void playWallSound(Panel.Kula ball, int panelWidth) {
        if (muted || wallSoundsPlayedThisFrame >= MAX_WALL_SOUNDS_PER_FRAME) return;
        if (System.currentTimeMillis() - ball.lastSoundTime < COOLDOWN_MS) return;
        if (ball.getSpeed() < 0.2) return;

        ball.lastSoundTime = System.currentTimeMillis();

        String soundKey = "rumble";
        int count = soundUseCounterThisFrame.getOrDefault(soundKey, 0);
        soundUseCounterThisFrame.put(soundKey, count + 1);

        List<Sound> sounds = soundMap.get(soundKey);
        if (sounds == null || sounds.isEmpty()) return;

        Sound sound = sounds.get(random.nextInt(sounds.size()));

        float sizeFactor = clamp((float) (ball.size / 100f), 0.9f, 1.0f);
        float gain = calculateGain(ball.getSpeed(), ball.getDensity(), ball.getCollisionCount()) * sizeFactor;
        float pan = calculatePan(ball.x, panelWidth);
        float typeVolume = soundTypeConfig.getOrDefault(soundKey, new SoundTypeConfig(1.0f, 0)).volumeMultiplier;

        sound.play(gain * typeVolume * getGlobalLoudnessFactor(), pan);
        wallSoundsPlayedThisFrame++;
    }

    private static String getSoundKeyForSize(double size) {
        if (size < soundTypeConfig.get("click").sizeThreshold) return "click";
        else if (size < soundTypeConfig.get("tap").sizeThreshold) return "tap";
        else return "thump";
    }

    private static float calculateGain(double speed, int density, int collisions) {
        float gain = (float) Math.min(0.6, speed / 4.0);
        gain -= Math.min(1f, density * 0.008f + collisions * 0.1f);
        gain += (random.nextFloat() - 0.5f) * 0.1f;
        return clamp(gain, 0.05f, 1.0f);
    }

    private static float calculatePitch(double size, int density, int collisions) {
        float base;
        if (size < 25) base = 1.3f;
        else if (size < 70) base = 1.0f;
        else base = 0.85f;

        float pitch = base - density * 0.002f - collisions * 0.001f;
        pitch += (random.nextFloat() - 0.5f) * 0.05f;
        return clamp(pitch, 0.6f, 1.4f);
    }

    private static float calculatePan(double xPosition, int panelWidth) {
        if (panelWidth <= 0) return 0f;
        float relX = (float) xPosition / panelWidth;
        return clamp(relX * 2f - 1f, -1f, 1f);
    }

    private static float getGlobalLoudnessFactor() {
        float factor = 1.0f - ((float) soundsPlayedThisFrame / MAX_SOUNDS_PER_FRAME);
        return clamp(factor, 0.1f, 1.0f) * volume;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Map<String, SoundTypeConfig> getSoundTypeConfig() {
        return soundTypeConfig;
    }

    public static void setVolume(float newVolume) {
        volume = clamp(newVolume, 0f, 1f);
    }

    public static void setMuted(boolean mute) {
        muted = mute;
    }
}
