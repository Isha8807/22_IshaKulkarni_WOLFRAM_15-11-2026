# Wolfram Demonstrations: Normal Distribution and Binomial Coin Flips

An exploration of probability using two demonstrations from the Mathematics topic of the [Wolfram Demonstrations Project](https://demonstrations.wolfram.com/topics/mathematics), recreated with simulations in **Java**.

## Area of a Normal Distribution

This section studies how any normal distribution N(μ, σ) relates to the standard normal distribution N(0, 1) through the **z-score**, z = (x − μ) / σ.

The report works through the formula, a worked example (x = 115 under N(100, 15) gives z = 1 and a left-tail area of 0.8413), a table of six slider settings with left and right tail areas, and the 68–95–99.7 empirical rule.

Source: [Area of a Normal Distribution](https://demonstrations.wolfram.com/AreaOfANormalDistribution/) by Eric Schulz.

## Binomial Distribution via Coin Flips

This section explores how the number of flips per trial, the number of trials and the probability of heads shape the distribution of the number of heads.

The report includes the binomial probability mass function, the mean n·p and variance n·p·(1 − p), simulations from 10 to 10,000 trials compared with theory, and distributions for p = 0.2, 0.5 and 0.7.

Source: [Binomial Distribution via Coin Flips](https://demonstrations.wolfram.com/BinomialDistributionViaCoinFlips/) by Adam J. Johnston and Rachael L. Baumann.

## Java Implementation

Both programs use only the standard JDK (no external libraries) and draw their charts with Java2D.

| File | What it does |
| ---- | ------------ |
| `NormalDistributionArea.java` | Computes normal areas with Simpson's rule, shows P(X ≤ x) = P(Z ≤ z), and draws the curve and empirical rule charts |
| `BinomialCoinFlips.java` | Simulates coin flips using a list of 100 outcomes (as in the demonstration), compares with the exact binomial pmf, and draws the distribution charts |

Run each file directly (JDK 11 or later):

```
java NormalDistributionArea.java output
java BinomialCoinFlips.java output
```

Each run writes its charts (`c1.png` to `c4.png`) and CSV tables into the `output` folder. The random seed is fixed (`22`), so results are reproducible.

## Key Takeaways

- Any normal probability can be found from the standard normal distribution by standardising with the z-score.
- Left-tail and right-tail areas are complements and add up to 1.
- The binomial distribution models the number of successes in n independent trials with the same success probability.
- When heads and tails are about equally likely, the distribution is nearly symmetric; when p is near 0 or 1, outcomes cluster at one end.
- With many trials and a fair coin, the binomial distribution resembles a normal curve.

## Document Details

|                 |                                           |
| --------------- | ----------------------------------------- |
| **Author**      | Isha Kulkarni                             |
| **Class**       | D7B                                       |
| **Roll number** | 22                                        |
| **Topic**       | Mathematics (Wolfram Demonstrations)      |
| **Format**      | PDF                                       |
| **Length**      | 8 pages                                   |
| **Date**        | 15 November 2026                          |

## Files

- `22_IshaKulkarni_WOLFRAM_15-11-2026.pdf`: the full report
- `NormalDistributionArea.java`: Java program for demonstration 1
- `BinomialCoinFlips.java`: Java program for demonstration 2
- `README.md`: this file
