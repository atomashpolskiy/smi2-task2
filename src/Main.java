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
        double weight;
        int maxLength = 0;

        ItemRandomizer<String> randomizer;

        boolean isStressTest = false;

        try {
            Scanner scanner = new Scanner(in);

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
                    if (word.length() > maxLength) {
                        maxLength = word.length();
                    }
                    k++;
                }
                out.println("Input weight for the above word:");
                weight = Double.parseDouble(scanner.nextLine());
                builder.add(word, weight);
                out.println("Type next word or hit <Enter> to finish input");
            }

            randomizer = builder.build();
        } catch (Exception e) {
            out.println("*** ERROR: " + e.getMessage() + " ***");
            return;
        }

        Duration maxRunningTime = Duration.ofSeconds(3);
        Duration runningTime;
        out.println("Gathering stats, please wait for " + maxRunningTime.getSeconds() + " seconds...");

        long t0 = System.nanoTime();
        int i = 0;
        Map<String, Long> counts = new HashMap<>();

        for (;;) {
            String nextWord = randomizer.nextItem();
            counts.merge(nextWord, 1L, Math::addExact);

            if (i % 1_000_000 == 0) {
                runningTime = Duration.ofNanos(System.nanoTime() - t0);
                if (runningTime.compareTo(maxRunningTime) > 0) {
                    break;
                }
            }
        }

        int maxWordLength = maxLength;
        long total = counts.values().stream().reduce(Math::addExact).get();
        out.println(String.format("# of samples: %,d (%.0f ns/sample)", total, ((double) runningTime.toNanos() / total)));
        if (isStressTest) {
            out.println(String.format("# of items: %,d", randomizer.getItemCount()));
            out.println(String.format("# of buckets (distinct weights): %,d", randomizer.getBucketCount()));
        } else {
            counts.forEach((w, count) -> {
                out.println(String.format("# of '%" + maxWordLength + "s': %,10d (%.2f%%)", w, count, (count.doubleValue() / total * 100)));
            });
        }
    }

    private static void generateStressTest(ItemRandomizer.Builder<String> builder) {
        int streamSize = 1_000_000, bound = 1_000_000_000;

        Random random = new Random();
        random.longs(streamSize, 1, bound)
                .forEach((randomWeight) -> {
                    builder.add(randomString(random), randomWeight);
                });
    }

    private static String randomString(Random r) {
        char[] chars = new char[r.nextInt(10) + 10];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('A' + r.nextInt(26));
        }
        return new String(chars);
    }
}
