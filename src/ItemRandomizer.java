import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ItemRandomizer<T> {
    static final float PROB_RESOLUTION = 0.01f;

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final int itemCount;

    private final float resolution;
    private final Bucket[] buckets;
    private final Random random;

    private ItemRandomizer(List<WeightedItem<T>> items, float resolution) {
        Collections.sort(items, WeightedItemComparator.ascending);

        Iterator<WeightedItem<T>> iter = items.iterator();
        WeightedItem<T> item = null;

        List<Bucket> buckets = new ArrayList<>();

        List<T> bucket;
        float weight;
        float weightTotal = 0;

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
                if (distance(item.getWeight(), weight) < resolution) {
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

        this.resolution = resolution;
        this.buckets = buckets.toArray(new Bucket[buckets.size()]);
        this.random = new Random();
    }

    private static float distance(float x, float y) {
        return Math.abs(x - y);
    }

    public T nextItem() {
        float f = random.nextFloat();

        int from = 0, to = (buckets.length - 1);
        int mid;
        float weight;

        while (from < to) {
            mid = from + (to - from) / 2;
            weight = buckets[mid].getWeight();

            float diff = weight - f;
            if (diff >= 0 && Math.abs(diff) < resolution) {
                return getRandomItem(buckets[mid]);
            }

            if (f > weight) {
                from = mid + 1;
            } else {
                to = mid - 1;
            }
        }

        do {
            weight = buckets[from].getWeight();
            if (f < weight) {
                return getRandomItem(buckets[from]);
            }
        } while (++from < buckets.length);

        return null;
    }

    @SuppressWarnings("unchecked")
    private T getRandomItem(Bucket bucket) {
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
        private float capacity;
        private float resolution = PROB_RESOLUTION;

        private Builder() {
            this.items = new ArrayList<>();
        }

        public Builder<T> resolution(float resolution) {
            if (resolution < 0 || resolution > 0.5) {
                throw new IllegalArgumentException(
                        String.format("Illegal resolution. Expected [0; 0.5], got: %f", resolution));
            }
            this.resolution = resolution;
            return this;
        }

        public Builder<T> add(T item, float weight) {
            Objects.requireNonNull(item);
            if (weight <= 0 || weight > 1) {
                throw new IllegalArgumentException("Illegal weight. Expected (0; 1], got: " + weight);
            } else if (capacity + weight > 1) {
                throw new IllegalArgumentException(
                        String.format("Insufficient capacity (left: %f, item weight: %f)", (1 - capacity), weight));
            }
            items.add(new WeightedItem<>(item, weight));
            capacity += weight;
            return this;
        }

        public float capacity() {
            return capacity;
        }

        public ItemRandomizer<T> build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("No items");
            }
            return new ItemRandomizer<>(items, resolution);
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
    float getWeight();
}

class WeightedItem<T> implements Weighted {
    private T item;
    private float weight;

    WeightedItem(T item, float weight) {
        this.item = item;
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }

    T getItem() {
        return item;
    }
}

class Bucket {
    private float weight;
    private List<?> items;

    public Bucket(float weight, List<?> items) {
        this.weight = weight;
        this.items = items;
    }

    public float getWeight() {
        return weight;
    }

    public List<?> getItems() {
        return items;
    }
}