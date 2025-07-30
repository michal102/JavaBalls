//import javax.sound.sampled.*;
//import java.io.File;
//import java.util.*;
//
//public class oldSoundManager {
//    private static final int POOL_SIZE = 10;
//    private static final int MAX_SOUNDS_PER_FRAME = 1000;
//    private static int soundsPlayedThisFrame = 0;
//
//    private static final Random random = new Random();
//
//    private static final Map<String, List<Clip>> clipPools = new HashMap<>();
//
//    static {
//        loadAndPool("click", "click1.wav", "click2.wav");
//        loadAndPool("tap", "tap1.wav", "tap2.wav");
//        loadAndPool("thump", "thump1.wav", "thump2.wav", "thump3.wav");
//        loadAndPool("rumble", "rumble.wav");
//    }
//
//    private static void loadAndPool(String key, String... filenames) {
//        List<Clip> originalClips = new ArrayList<>();
//        for (String filename : filenames) {
//            try {
//                File file = new File("sfx/" + filename);
//                if (!file.exists()) {
//                    System.err.println("File not found: " + file.getAbsolutePath());
//                    continue;
//                }
//
//                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
//                AudioFormat format = stream.getFormat();
//
//                if (!AudioSystem.isLineSupported(new DataLine.Info(Clip.class, format))) {
//                    System.err.println("Format not supported: " + format);
//                    continue;
//                }
//
//                Clip clip = AudioSystem.getClip();
//                clip.open(stream);
//                originalClips.add(clip);
//            } catch (Exception e) {
//                System.err.println("Failed to load " + filename);
//                e.printStackTrace();
//            }
//        }
//
//        List<Clip> pool = new ArrayList<>();
//        for (int i = 0; i < POOL_SIZE && !originalClips.isEmpty(); i++) {
//            Clip original = originalClips.get(i % originalClips.size());
//            try {
//                AudioInputStream stream = AudioSystem.getAudioInputStream(new File("sfx/" + filenames[i % filenames.length]));
//                Clip clone = AudioSystem.getClip();
//                clone.open(stream);
//                pool.add(clone);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        clipPools.put(key, pool);
//    }
//
//    public static void resetSoundFrameCounter() {
//        soundsPlayedThisFrame = 0;
//    }
//
//    public static void playCollisionSound(Panel.Kula a, Panel.Kula b) {
//        if (soundsPlayedThisFrame >= MAX_SOUNDS_PER_FRAME) return;
//
//        Panel.Kula dominant = (a.size >= b.size) ? a : b;
//        String soundKey = getSoundKeyForSize(dominant.size);
//
//        List<Clip> pool = clipPools.get(soundKey);
//        if (pool == null || pool.isEmpty()) return;
//
//        Clip clip = getAvailableClip(pool);
//        if (clip == null) return;
//
//        double speed = Math.sqrt(
//                a.xspeed * a.xspeed + a.yspeed * a.yspeed +
//                        b.xspeed * b.xspeed + b.yspeed * b.yspeed
//        );
//        float gain = (float) Math.min(3.0, speed);
//        int avgDensity = (a.getDensity() + b.getDensity()) / 2;
//        int avgCollision = (a.getCollisionCount() + b.getCollisionCount()) / 2;
//
//        applySoundModifiers(clip, gain, avgDensity, avgCollision);
//        clip.setFramePosition(0);
//        clip.start();
//
//        soundsPlayedThisFrame++;
//    }
//
//    public static void playWallSound(Panel.Kula ball) {
//        if (soundsPlayedThisFrame >= MAX_SOUNDS_PER_FRAME) return;
//
//        List<Clip> pool = clipPools.get("rumble");
//        if (pool == null || pool.isEmpty()) return;
//
//        Clip clip = getAvailableClip(pool);
//        if (clip == null) return;
//
//        float gain = (float) Math.min(2.5, Math.abs(ball.xspeed) + Math.abs(ball.yspeed));
//        applySoundModifiers(clip, gain, ball.getDensity(), ball.getCollisionCount());
//
//        clip.setFramePosition(0);
//        clip.start();
//
//        soundsPlayedThisFrame++;
//    }
//
//    private static Clip getAvailableClip(List<Clip> pool) {
//        for (Clip clip : pool) {
//            if (!clip.isRunning()) {
//                return clip;
//            }
//        }
//        return null; // No available clip
//    }
//
//    private static void applySoundModifiers(Clip clip, float gain, int density, int collisionCount) {
//        try {
//            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
//                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
//                float db = Math.max(-20f, -10f + gain); // Safe range
//                volume.setValue(db);
//            }
//            // For muffling, pitch: you'd need an external lib like TinySound/JSyn
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String getSoundKeyForSize(double size) {
//        if (size < 25) return "click";
//        else if (size < 70) return "tap";
//        else return "thump";
//    }
//}
