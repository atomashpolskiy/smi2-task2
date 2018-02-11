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
    private final int minWeight;
    private final int maxWeight;

    private final Random random;

    private ItemRandomizer(List<WeightedItem<T>> items) {
        Collections.sort(items, WeightedItemComparator.ascending);

        Iterator<WeightedItem<T>> iter = items.iterator();
        WeightedItem<T> item = null;

        List<Bucket> buckets = new ArrayList<>();

        List<T> bucket;
        int weight;
        int weightTotal = 0;

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
        this.minWeight = buckets.get(0).getWeight();
        this.maxWeight = buckets.get(buckets.size() - 1).getWeight();
        this.random = new Random();
    }

    public T nextItem() {
        long r = random.nextInt(maxWeight - minWeight + 1) + minWeight;

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
        private long capacity = Integer.MAX_VALUE;

        private Builder() {
            this.items = new ArrayList<>();
        }

        public Builder<T> add(T item, int weight) {
            Objects.requireNonNull(item);
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be positive");
            } else if (capacity - weight < 0) {
                throw new IllegalStateException("Insufficient capacity: the sum of items' weights exceeds maximum allowed value");
            }
            capacity -= weight;
            items.add(new WeightedItem<>(item, weight));
            return this;
        }

        public ItemRandomizer<T> build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("No items");
            }
            return new ItemRandomizer<>(items);
        }
    }

    private static class WeightedItemComparator implements Comparator<Weighted> {
        static final WeightedItemComparator ascending = new WeightedItemComparator();

        @Override
        public int compare(Weighted o1, Weighted o2) {
            return o1.getWeight() - o2.getWeight();
        }
    }
}

interface Weighted {
    int getWeight();
}

class WeightedItem<T> implements Weighted {
    private T item;
    private int weight;

    WeightedItem(T item, int weight) {
        this.item = item;
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    T getItem() {
        return item;
    }
}

class Bucket {
    private int weight;
    private List<?> items;

    public Bucket(int weight, List<?> items) {
        this.weight = weight;
        this.items = items;
    }

    public int getWeight() {
        return weight;
    }

    public List<?> getItems() {
        return items;
    }
}