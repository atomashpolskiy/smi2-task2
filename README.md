### Example

```
Hit <Enter> to run stress test or type a word to begin manual input
cat
Input weight for the above word:
13
Type next word or hit <Enter> to finish input
dog
Input weight for the above word:
13
Type next word or hit <Enter> to finish input
fish
Input weight for the above word:
34
Type next word or hit <Enter> to finish input
bird
Input weight for the above word:
39
Type next word or hit <Enter> to finish input
plant
Input weight for the above word:
1
Type next word or hit <Enter> to finish input

Gathering stats, please wait for 3 seconds...
# of samples: 25 587 273 (117 ns/sample)
# of 'plant':    255 262 (1,00%)
# of '  cat':  3 323 624 (12,99%)
# of ' fish':  8 701 459 (34,01%)
# of ' bird':  9 978 733 (39,00%)
# of '  dog':  3 328 195 (13,01%)
```

### Stress Test

When stress test option is selected, the application generates a number of items with different random weights.

```
Hit <Enter> to run stress test or type a word to begin manual input

Gathering stats, please wait for 3 seconds...
# of samples: 2 719 576 (1103 ns/sample)
# of items: 1 000 000
# of buckets (distinct weights): 999 493
```

