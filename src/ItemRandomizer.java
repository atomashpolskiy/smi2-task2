import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Random;

public class ItemRandomizer<T> {

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final int itemCount;

    private final Bucket[] buckets;
    private final Random random;

    private PrimitiveIterator.OfLong randomLongs;

    private ItemRandomizer(List<WeightedItem<T>> items) {
        Collections.sort(items, WeightedItemComparator.ascending);

        Iterator<WeightedItem<T>> iter = items.iterator();
        WeightedItem<T> item = null;

        List<Bucket> buckets = new ArrayList<>();

        List<T> bucket;
        long weight;
        long weightTotal = 0;

        do {
            bucket = new ArrayList<>();
            if (item == null) {
                item = iter.next();
            }
            weight = item.getWeight();
            weightTotal += item.getWeight();
            bucket.add(item.getItem());
            item = null;

            while (iter.hasNext()) {
                item = iter.next();
                if (item.getWeight() == weight) {
                    bucket.add(item.getItem());
                    weightTotal += item.getWeight();
                    item = null;
                } else {
                    break;
                }
            }

            buckets.add(new Bucket(weightTotal, bucket));
        } while (iter.hasNext());

        if (item != null) {
            weightTotal += item.getWeight();
            buckets.add(new Bucket(weightTotal, Collections.singletonList(item.getItem())));
        }

        this.itemCount = items.size();
        this.buckets = buckets.toArray(new Bucket[buckets.size()]);
        this.random = new Random();
    }

    private void initRandomLongs() {
        long maxWeight = buckets[buckets.length - 1].getWeight();
        this.randomLongs = random.longs(Long.MAX_VALUE, 1, maxWeight + 1).iterator();
    }

    private long getRandomLong() {
        if (randomLongs == null || !randomLongs.hasNext()) {
            initRandomLongs();
        }
        return randomLongs.nextLong();
    }

    public T nextItem() {
        long r = getRandomLong();

        int from = 0, to = (buckets.length - 1);
        int mid;
        double weight;

        while (from < to) {
            mid = from + (to - from) / 2;
            weight = buckets[mid].getWeight();

            if (r == weight) {
                return getRandomItem(buckets[mid]);
            } else if (r > weight) {
                from = mid + 1;
            } else {
                to = mid - 1;
            }
        }

        do {
            weight = buckets[from].getWeight();
            if (r <= weight) {
                return getRandomItem(buckets[from]);
            }
        } while (++from < buckets.length);

        // should not happen as long as the above algorithm works correctly
        throw new IllegalStateException("Failed to select random item");
    }

    @SuppressWarnings("unchecked")
    private T getRandomItem(Bucket bucket) {
        if (bucket.getItems().size() == 1) {
            return (T) bucket.getItems().get(0);
        }
        int i = random.nextInt(bucket.getItems().size());
        return (T) bucket.getItems().get(i);
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getBucketCount() {
        return buckets.length;
    }

    public static class Builder<T> {
        private List<WeightedItem<T>> items;

        private Builder() {
            this.items = new ArrayList<>();
        }

        public Builder<T> add(T item, double weight) {
            Objects.requireNonNull(item);
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be positive");
            }
            items.add(new WeightedItem<>(item, weight));
            return this;
        }

        public ItemRandomizer<T> build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("No items");
            }
            normalize(items);
            checkTotalWeight(items);
            return new ItemRandomizer<>(items);
        }

        // possible scale values are [0; 324]
        // e.g., see https://docs.oracle.com/cd/E19957-01/806-3568/ncg_math.html
        // (TABLE 2-5   Bit Patterns in Double-Storage Format and their IEEE Values)
        private void normalize(List<WeightedItem<T>> items) {
            int maxScale = 0;
            for (WeightedItem<?> item : items) {
                double weight = item.getUnscaledWeight();
                int scale = BigDecimal.valueOf(weight).scale();
                if (scale > maxScale) {
                    maxScale = scale;
                }
            }

            for (WeightedItem<?> item : items) {
                item.setScale(maxScale);
            }
        }

        private void checkTotalWeight(List<WeightedItem<T>> items) {
            long capacity = Long.MAX_VALUE;

            for (WeightedItem<?> item : items) {
                capacity -= item.getWeight();
                if (capacity < 0) {
                    throw new IllegalStateException("Insufficient capacity");
                }
            }
        }
    }

    private static class WeightedItemComparator implements Comparator<Weighted> {
        static final WeightedItemComparator ascending = new WeightedItemComparator();

        @Override
        public int compare(Weighted o1, Weighted o2) {
            return (int) Math.signum(o1.getWeight() - o2.getWeight());
        }
    }
}

interface Weighted {
    long getWeight();
}

class WeightedItem<T> implements Weighted {
    private T item;
    private double weight;
    private int scale;

    private long normalizedWeight;

    WeightedItem(T item, double weight) {
        this.item = item;
        this.weight = weight;
        this.scale = 0;
        this.normalizedWeight = normalize(weight, scale);
    }

    private long normalize(double weight, int scale) {
        return (long) (weight * Math.pow(10, scale));
    }

    double getUnscaledWeight() {
        return weight;
    }

    void setScale(int scale) {
        if (this.scale != scale) {
            this.scale = scale;
            this.normalizedWeight = normalize(weight, scale);
        }
    }

    public long getWeight() {
        return normalizedWeight;
    }

    T getItem() {
        return item;
    }
}

class Bucket {
    private long weight;
    private List<?> items;

    Bucket(long weight, List<?> items) {
        this.weight = weight;
        this.items = items;
    }

    long getWeight() {
        return weight;
    }

    List<?> getItems() {
        return items;
    }
}