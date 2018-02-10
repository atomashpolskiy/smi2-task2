import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        ItemRandomizer.Builder<String> builder = ItemRandomizer.builder();

        String word;
        float weight;

        ItemRandomizer<String> randomizer;

        boolean isStressTest = false;

        try {
            Scanner scanner = new Scanner(in);

            out.println(String.format("Input probability resolution (default is %f; hit <Enter> to leave the default):",
                    ItemRandomizer.PROB_RESOLUTION));
            String s = scanner.nextLine();
            if (!s.isEmpty()) {
                float resolution = Float.parseFloat(s);
                builder.resolution(resolution);
                out.println("Probability resolution set to " + resolution);
            }

            out.println("Hit <Enter> to run stress test or type a word to begin manual input");

            int k = 0;
            for (;;) {
                word = scanner.nextLine().trim();
                if (word.isEmpty()) {
                    if (k == 0) {
                        generateStressTest(builder);
                        isStressTest = true;
                    }
                    break;
                } else {
                    k++;
                }
                out.println("Input probability for the above word. Correct values are (0; 1.0]");
                weight = Float.parseFloat(scanner.nextLine());
                builder.add(word, weight);
                out.println("Type next word or hit <Enter> to finish input");
            }

            randomizer = builder.build();
        } catch (Exception e) {
            out.println("*** ERROR: " + e.getMessage() + " ***");
            return;
        }

        if (builder.capacity() < 1) {
            out.println(String.format("Individual items weights do not sum to 1; %.2f%% of queries will return null",
                    (1 - builder.capacity()) * 100));
        }

        Duration maxRunningTime = Duration.ofSeconds(3);
        Duration runningTime;
        out.println("Gathering stats, please wait for " + maxRunningTime.getSeconds() + " seconds...");

        long t0 = System.nanoTime();
        int i = 0;
        Map<String, Long> counts = new HashMap<>();
        long nullsCount = 0;

        for (;;) {
            String nextWord = randomizer.nextItem();
            if (nextWord == null) {
                nullsCount++;
            } else {
                counts.merge(nextWord, 1L, Math::addExact);
            }

            if (i % 1_000_000 == 0) {
                runningTime = Duration.ofNanos(System.nanoTime() - t0);
                if (runningTime.compareTo(maxRunningTime) > 0) {
                    break;
                }
            }
        }

        long total = counts.values().stream().reduce(Math::addExact).get() + nullsCount;
        out.println(String.format("# of samples: %,d (%.0f ns/sample)", total, ((double) runningTime.toNanos() / total)));
        if (isStressTest) {
            out.println("# of items: " + randomizer.getItemCount());
            out.println("# of buckets (different weights): " + randomizer.getBucketCount());
        } else {
            counts.forEach((w, count) -> {
                out.println("% of '" + w + "': " + (count.doubleValue() / total * 100));
            });
            out.println("% of nulls: " + (Long.valueOf(nullsCount).doubleValue() / total * 100));
        }
    }

    private static void generateStressTest(ItemRandomizer.Builder<String> builder) {
        float r = 0.000_000_1f;
        builder.resolution(r);

        Random random = new Random();
        int i = 0;
        float w = 0;
        while (i < 1_000_000_000) {
            w += r * (random.nextInt(2) + 1);
            if (w <= (1 - builder.capacity())) {
                builder.add(randomString(random), w);
            } else {
                break;
            }
            i++;
        }
    }

    private static String randomString(Random r) {
        char[] chars = new char[r.nextInt(10) + 10];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('A' + r.nextInt(26));
        }
        return new String(chars);
    }
}
