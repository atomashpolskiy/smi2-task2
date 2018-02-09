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
# of samples: 60 565 572
% of 'bob': 0.34892100086167765
% of 'alice': 0.3505737550039154
# of nulls: 99.30050524413441
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
# of samples: 59 212 949
% of 'bob': 0.3983672557838658
% of 'alice': 0.30038530930118
# of nulls: 99.30124743491496
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
# of samples: 35 296 945
% of 'cat': 4.998265430620129
% of 'bird': 20.50542334471156
% of 'plant': 20.501323839782735
% of 'fish': 32.99611906922823
% of 'dog': 3.9979663962419414
# of nulls: 17.000901919415405
```

### Stress Test

When stress test option is selected, the application generates a number of items with different random weights.

```
Input probability resolution (default is 0,010000; hit <Enter> to leave the default):

Hit <Enter> to run stress test or type a word to begin manual input

Individual items weights do not sum to 1; 0,04% of queries will return null
Gathering stats, please wait for 3 seconds...
# of samples: 11 264 209
# of items: 3647
# of buckets (different weights): 3617
```

