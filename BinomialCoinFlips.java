import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Binomial Distribution via Coin Flips (Wolfram Demonstrations Project).
 * Mirrors the demonstration: a list of 100 outcomes (1 = heads, 0 = tails) is
 * built from P(heads);
 * each flip picks a random entry, and the flips of a trial are summed to count
 * the heads.
 * Run: java BinomialCoinFlips.java [outputDir]
 * Author: Isha Kulkarni, Class D7B, Roll No. 22
 */
public class BinomialCoinFlips {

    /**
     * Number of heads in each of `trials` trials of `flips` flips; returns counts
     * of 0..flips heads.
     */
    static int[] simulate(int flips, double pHeads, int trials, Random rnd) {
        int ones = (int) Math.round(pHeads * 100);
        int[] list = new int[100];
        for (int i = 0; i < ones; i++)
            list[i] = 1; // 1 = heads, 0 = tails
        int[] counts = new int[flips + 1];
        for (int t = 0; t < trials; t++) {
            int heads = 0;
            for (int f = 0; f < flips; f++)
                heads += list[rnd.nextInt(100)];
            counts[heads]++;
        }
        return counts;
    }

    /**
     * Binomial pmf P(X = k) = C(n,k) p^k (1-p)^(n-k), built with a running product
     * to avoid overflow.
     */
    static double pmf(int n, int k, double p) {
        double c = 1;
        for (int i = 1; i <= k; i++)
            c = c * (n - k + i) / i;
        return c * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    static double mean(int[] counts, int trials) {
        double s = 0;
        for (int k = 0; k < counts.length; k++)
            s += k * counts[k];
        return s / trials;
    }

    static double std(int[] counts, int trials) {
        double m = mean(counts, trials), s = 0;
        for (int k = 0; k < counts.length; k++)
            s += counts[k] * (k - m) * (k - m);
        return Math.sqrt(s / trials);
    }

    // ---------- tiny plotting helper (Java2D) ----------
    static class Panel {
        Graphics2D g;
        int x0, y0, w, h;
        int n;
        double ymax;

        Panel(Graphics2D g, int x0, int y0, int w, int h, int n, double ymax) {
            this.g = g;
            this.x0 = x0;
            this.y0 = y0;
            this.w = w;
            this.h = h;
            this.n = n;
            this.ymax = ymax;
        }

        double px(double k) {
            return x0 + (k + 0.5) / (n + 1) * w;
        }

        double py(double y) {
            return y0 + h - y / ymax * h;
        }

        void draw(String title, int[] counts, int trials, double[] theory, boolean showY) {
            double bw = w / (double) (n + 1);
            g.setColor(new Color(31, 95, 168, 210));
            for (int k = 0; k <= n; k++) {
                double v = counts[k] / (double) trials;
                g.fill(new java.awt.geom.Rectangle2D.Double(px(k) - bw * 0.4, py(v), bw * 0.8, y0 + h - py(v)));
            }
            Color orange = new Color(224, 123, 0);
            g.setColor(orange);
            g.setStroke(new BasicStroke(2.5f));
            Path2D p = new Path2D.Double();
            for (int k = 0; k <= n; k++) {
                if (k == 0)
                    p.moveTo(px(k), py(theory[k]));
                else
                    p.lineTo(px(k), py(theory[k]));
            }
            g.draw(p);
            for (int k = 0; k <= n; k++)
                g.fill(new java.awt.geom.Ellipse2D.Double(px(k) - 4, py(theory[k]) - 4, 8, 8));
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(x0, y0 + h, x0 + w, y0 + h);
            g.drawLine(x0, y0, x0, y0 + h);
            g.setFont(new Font("SansSerif", Font.PLAIN, 20));
            FontMetrics fm = g.getFontMetrics();
            int step = n > 10 ? 5 : 2;
            for (int k = 0; k <= n; k += step) {
                String s = String.valueOf(k);
                g.drawString(s, (int) px(k) - fm.stringWidth(s) / 2, y0 + h + 26);
            }
            for (double t = 0; t <= ymax + 1e-9; t += 0.1) {
                int Y = (int) py(t);
                g.drawLine(x0 - 6, Y, x0, Y);
                if (showY) {
                    String s = String.format("%.1f", t);
                    g.drawString(s, x0 - 12 - fm.stringWidth(s), Y + 7);
                }
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            fm = g.getFontMetrics();
            g.drawString(title, x0 + (w - fm.stringWidth(title)) / 2, y0 - 12);
            g.setFont(new Font("SansSerif", Font.PLAIN, 20));
            fm = g.getFontMetrics();
            String xl = "heads";
            g.drawString(xl, x0 + (w - fm.stringWidth(xl)) / 2, y0 + h + 54);
        }
    }

    static Graphics2D canvas(BufferedImage im) {
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, im.getWidth(), im.getHeight());
        return g;
    }

    static double[] theory(int n, double p) {
        double[] t = new double[n + 1];
        for (int k = 0; k <= n; k++)
            t[k] = pmf(n, k, p);
        return t;
    }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "output");
        out.mkdirs();
        Random rnd = new Random(22); // fixed seed -> reproducible results

        // ---- pmf for n = 5, p = 0.5 ----
        System.out.println("P(X = k) for n = 5, p = 0.5:");
        for (int k = 0; k <= 5; k++)
            System.out.printf("  k = %d  %.4f%n", k, pmf(5, k, 0.5));

        // ---- Figure 3: n = 20, p = 0.5, 10 / 100 / 1000 / 10000 trials ----
        int[] trialList = { 10, 100, 1000, 10000 };
        int[][] data = new int[4][];
        double ymax = 0;
        double[] th20 = theory(20, 0.5);
        for (int i = 0; i < 4; i++) {
            data[i] = simulate(20, 0.5, trialList[i], rnd);
            for (int c : data[i])
                ymax = Math.max(ymax, c / (double) trialList[i]);
        }
        ymax = Math.ceil(ymax * 10) / 10.0;
        BufferedImage im = new BufferedImage(2000, 560, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(im);
        for (int i = 0; i < 4; i++)
            new Panel(g, 90 + i * 490, 70, 400, 380, 20, ymax).draw(String.format("%,d trials", trialList[i]), data[i],
                    trialList[i], th20, true);
        g.dispose();
        ImageIO.write(im, "png", new File(out, "c3.png"));

        // ---- Mean / std dev of the n = 20, p = 0.5 runs (1,000 and 10,000 trials)
        // ----
        try (PrintWriter pw = new PrintWriter(new File(out, "binomial_n20.csv"))) {
            pw.println("trials,theory_mean,sim_mean,theory_std,sim_std");
            for (int i = 2; i < 4; i++)
                pw.printf("%d,%.3f,%.3f,%.3f,%.3f%n", trialList[i], 20 * 0.5, mean(data[i], trialList[i]),
                        Math.sqrt(20 * 0.25), std(data[i], trialList[i]));
        }

        // ---- Figure 4: n = 10, 1000 trials, p = 0.2 / 0.5 / 0.7 ----
        double[] ps = { 0.2, 0.5, 0.7 };
        int[][] d4 = new int[3][];
        ymax = 0;
        for (int i = 0; i < 3; i++) {
            d4[i] = simulate(10, ps[i], 1000, rnd);
            for (int c : d4[i])
                ymax = Math.max(ymax, c / 1000.0);
        }
        ymax = Math.ceil(ymax * 10) / 10.0;
        im = new BufferedImage(2000, 560, BufferedImage.TYPE_INT_RGB);
        g = canvas(im);
        for (int i = 0; i < 3; i++)
            new Panel(g, 90 + i * 650, 70, 540, 380, 10, ymax).draw("P(heads) = " + ps[i], d4[i], 1000,
                    theory(10, ps[i]), true);
        g.dispose();
        ImageIO.write(im, "png", new File(out, "c4.png"));

        // ---- Mean / std dev table: n = 5, 10, 20; fair coin; 1000 trials ----
        try (PrintWriter pw = new PrintWriter(new File(out, "binomial_stats.csv"))) {
            pw.println("n,theory_mean,sim_mean,theory_std,sim_std");
            System.out.println("\n n   theory mean  sim mean  theory sd  sim sd  (1000 trials, p = 0.5)");
            for (int n : new int[] { 5, 10, 20 }) {
                int[] c = simulate(n, 0.5, 1000, new Random(100 + n));
                double tm = n * 0.5, ts = Math.sqrt(n * 0.25), sm = mean(c, 1000), ss = std(c, 1000);
                pw.printf("%d,%.3f,%.3f,%.3f,%.3f%n", n, tm, sm, ts, ss);
                System.out.printf("%2d   %6.3f      %6.3f     %6.3f    %6.3f%n", n, tm, sm, ts, ss);
            }
        }
    }
}