> Дан набор элементов с соответствующими весами. Реализовать функцию distribution, которая будет возвращать один элемент из заданного набора с вероятностью, которая соответствует весу данного элемента.
Например, у каждого баннера есть вес, и нужно показывать баннеры пропорционально их весу.

### Probability resolution

In the first case `alice` and `bob` are equally likely to be selected, despite the fact that their weights are different: 0.003 and 0.004. This is because the precision of their weights _exceeds_ the default probability resolution of 0.01, so they both end up in a single bucket.

```
Input probability resolution (default is 0,010000; hit <Enter> to leave the default):

Hit <Enter> to run stress test or type a word to begin manual input
alice
Input probability for the above word. Correct values are (0; 1.0]
0.003
Type next word or hit <Enter> to finish input
bob
Input probability for the above word. Correct values are (0; 1.0]
0.004
Type next word or hit <Enter> to finish input

Individual items weights do not sum to 1; 99,30% of queries will return null
Gathering stats, please wait for 3 seconds...
# of samples: 59 591 330 (50 ns/sample)
% of 'bob': 0.34998547607512703
% of 'alice': 0.3497790702103813
% of nulls: 99.30023545371449
```

In order to fix this, one should pick a more precise resolution value: 

```
Input probability resolution (default is 0,010000; hit <Enter> to leave the default):
0.001
Probability resolution set to 0.001
Hit <Enter> to run stress test or type a word to begin manual input
alice
Input probability for the above word. Correct values are (0; 1.0]
0.003
Type next word or hit <Enter> to finish input
bob
Input probability for the above word. Correct values are (0; 1.0]
0.004
Type next word or hit <Enter> to finish input

Individual items weights do not sum to 1; 99,30% of queries will return null
Gathering stats, please wait for 3 seconds...
# of samples: 57 313 252 (52 ns/sample)
% of 'bob': 0.39914154583306494
% of 'alice': 0.30023771814588357
% of nulls: 99.30062073602105
```

### Nulls

When the weights for individual items do not sum up to 1, the remaining portion of `ItemRandomizer#nextItem()` invocations will return `null`, so that the probabilities of retrieving each item stay intact.

### Another example

```
Input probability resolution (default is 0,010000; hit <Enter> to leave the default):

Hit <Enter> to run stress test or type a word to begin manual input
cat
Input probability for the above word. Correct values are (0; 1.0]
0.05
Type next word or hit <Enter> to finish input
dog
Input probability for the above word. Correct values are (0; 1.0]
0.04
Type next word or hit <Enter> to finish input
bird
Input probability for the above word. Correct values are (0; 1.0]
0.2
Type next word or hit <Enter> to finish input
fish
Input probability for the above word. Correct values are (0; 1.0]
0.33
Type next word or hit <Enter> to finish input
plant
Input probability for the above word. Correct values are (0; 1.0]
0.21
Type next word or hit <Enter> to finish input

Individual items weights do not sum to 1; 17,00% of queries will return null
Gathering stats, please wait for 3 seconds...
# of samples: 35 158 086 (85 ns/sample)
% of 'cat': 5.0047604980544165
% of 'fish': 33.00388707166824
% of 'bird': 20.4869315127109
% of 'plant': 20.492128041327394
% of 'dog': 4.002285562416566
% of nulls: 17.010007313822488
```

Note how `bird` and `plant` again end up in a single bucket and have the same sampling fraction of 20.5% due to insufficient prob. resolution. Increasing the resolution is the correct way to sample `bird` and `plant` in 20% and 21% of cases respectively.

### Stress Test

When stress test option is selected, the application generates a number of items with different random weights.

```
Input probability resolution (default is 0,010000; hit <Enter> to leave the default):

Hit <Enter> to run stress test or type a word to begin manual input

Individual items weights do not sum to 1; 0,05% of queries will return null
Gathering stats, please wait for 3 seconds...
# of samples: 11 135 247 (269 ns/sample)
# of items: 3624
# of buckets (different weights): 3602
```

