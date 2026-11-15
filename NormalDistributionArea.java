import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * Area of a Normal Distribution (Wolfram Demonstrations Project).
 * Standardises x to z = (x - mu) / sigma and shows that P(X <= x) = P(Z <= z).
 * Run: java NormalDistributionArea.java [outputDir]
 * Author: Isha Kulkarni, Class D7B, Roll No. 22
 */
public class NormalDistributionArea {

    static double pdf(double x, double mu, double s) {
        double z = (x - mu) / s;
        return Math.exp(-0.5 * z * z) / (s * Math.sqrt(2 * Math.PI));
    }

    /**
     * P(X <= x) by Simpson's rule integration of the density from mu - 10 sigma to
     * x.
     */
    static double cdf(double x, double mu, double s) {
        double a = mu - 10 * s;
        if (x <= a)
            return 0;
        int n = 20000; // even number of sub-intervals
        double h = (x - a) / n, sum = pdf(a, mu, s) + pdf(x, mu, s);
        for (int i = 1; i < n; i++)
            sum += pdf(a + i * h, mu, s) * (i % 2 == 0 ? 2 : 4);
        return Math.min(1.0, sum * h / 3);
    }

    // ---------- tiny plotting helper (Java2D) ----------
    static class Plot {
        Graphics2D g;
        int x0, y0, w, h;
        double xa, xb, ya, yb;

        Plot(Graphics2D g, int x0, int y0, int w, int h, double xa, double xb, double ya, double yb) {
            this.g = g;
            this.x0 = x0;
            this.y0 = y0;
            this.w = w;
            this.h = h;
            this.xa = xa;
            this.xb = xb;
            this.ya = ya;
            this.yb = yb;
        }

        double px(double x) {
            return x0 + (x - xa) / (xb - xa) * w;
        }

        double py(double y) {
            return y0 + h - (y - ya) / (yb - ya) * h;
        }

        static String fmt(double v) {
            return Math.abs(v - Math.rint(v)) < 1e-9 ? String.valueOf((long) Math.rint(v)) : String.format("%.2f", v);
        }

        void axes(String title, String xlabel, double xstep, double ystep) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(x0, y0 + h, x0 + w, y0 + h);
            g.drawLine(x0, y0, x0, y0 + h);
            g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            FontMetrics fm = g.getFontMetrics();
            for (double t = Math.ceil(xa / xstep) * xstep; t <= xb + 1e-9; t += xstep) {
                int X = (int) px(t);
                g.drawLine(X, y0 + h, X, y0 + h + 6);
                String s = fmt(t);
                g.drawString(s, X - fm.stringWidth(s) / 2, y0 + h + 28);
            }
            for (double t = 0; t <= yb + 1e-9; t += ystep) {
                int Y = (int) py(t);
                g.drawLine(x0 - 6, Y, x0, Y);
                String s = fmt(t);
                g.drawString(s, x0 - 12 - fm.stringWidth(s), Y + 7);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            fm = g.getFontMetrics();
            g.drawString(title, x0 + (w - fm.stringWidth(title)) / 2, y0 - 14);
            g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            fm = g.getFontMetrics();
            g.drawString(xlabel, x0 + (w - fm.stringWidth(xlabel)) / 2, y0 + h + 58);
        }

        void curve(java.util.function.DoubleUnaryOperator f, double from, double to, Color c) {
            Path2D p = new Path2D.Double();
            boolean first = true;
            for (int i = 0; i <= 400; i++) {
                double x = from + (to - from) * i / 400.0, y = f.applyAsDouble(x);
                if (first) {
                    p.moveTo(px(x), py(y));
                    first = false;
                } else
                    p.lineTo(px(x), py(y));
            }
            g.setColor(c);
            g.setStroke(new BasicStroke(3f));
            g.draw(p);
        }

        void fill(java.util.function.DoubleUnaryOperator f, double from, double to, Color c) {
            Path2D p = new Path2D.Double();
            p.moveTo(px(from), py(0));
            for (int i = 0; i <= 400; i++) {
                double x = from + (to - from) * i / 400.0;
                p.lineTo(px(x), py(f.applyAsDouble(x)));
            }
            p.lineTo(px(to), py(0));
            p.closePath();
            g.setColor(c);
            g.fill(p);
        }

        void vline(double x) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 10f, 8f },
                    0f));
            g.drawLine((int) px(x), (int) py(0), (int) px(x), (int) py(yb));
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

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "output");
        out.mkdirs();
        Color blue = new Color(31, 95, 168), orange = new Color(224, 123, 0);
        Color blueFill = new Color(31, 95, 168, 100), orangeFill = new Color(224, 123, 0, 100);

        // ---- Worked example: X ~ N(100, 15), x = 115 ----
        double mu = 100, sd = 15, x = 115, z = (x - mu) / sd;
        double area = cdf(z, 0, 1);
        System.out.printf("X ~ N(%.0f, %.0f), x = %.0f -> z = %.2f%n", mu, sd, x, z);
        System.out.printf("P(X <= x) = %.4f (via N(mu,sigma))   P(Z <= z) = %.4f (via N(0,1))%n", cdf(x, mu, sd), area);

        // ---- Figure 1: two curves, equal shaded areas ----
        BufferedImage im = new BufferedImage(1800, 640, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(im);
        Plot a = new Plot(g, 110, 80, 700, 420, 55, 145, 0, 0.03);
        a.fill(t -> pdf(t, mu, sd), 55, x, blueFill);
        a.curve(t -> pdf(t, mu, sd), 55, 145, blue);
        a.vline(x);
        a.axes("N(100, 15): area left of x = 115", "x", 15, 0.01);
        Plot b = new Plot(g, 1000, 80, 700, 420, -3.5, 3.5, 0, 0.45);
        b.fill(t -> pdf(t, 0, 1), -3.5, z, orangeFill);
        b.curve(t -> pdf(t, 0, 1), -3.5, 3.5, orange);
        b.vline(z);
        b.axes("N(0, 1): area left of z = 1.00", "z", 1, 0.1);
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("SansSerif", Font.ITALIC, 24));
        String cap = String.format("Both shaded areas equal %.4f", area);
        g.drawString(cap, (1800 - g.getFontMetrics().stringWidth(cap)) / 2, 625);
        g.dispose();
        ImageIO.write(im, "png", new File(out, "c1.png"));

        // ---- Figure 2: empirical rule ----
        im = new BufferedImage(1400, 640, BufferedImage.TYPE_INT_RGB);
        g = canvas(im);
        Plot e = new Plot(g, 110, 80, 1200, 420, -4, 4, 0, 0.45);
        e.fill(t -> pdf(t, 0, 1), -3, 3, new Color(158, 202, 225));
        e.fill(t -> pdf(t, 0, 1), -2, 2, new Color(107, 174, 214));
        e.fill(t -> pdf(t, 0, 1), -1, 1, new Color(33, 113, 181));
        e.curve(t -> pdf(t, 0, 1), -4, 4, Color.BLACK);
        e.axes(String.format("Empirical rule: %.2f%% / %.2f%% / %.2f%%",
                100 * (cdf(1, 0, 1) - cdf(-1, 0, 1)), 100 * (cdf(2, 0, 1) - cdf(-2, 0, 1)),
                100 * (cdf(3, 0, 1) - cdf(-3, 0, 1))), "z", 1, 0.1);
        g.dispose();
        ImageIO.write(im, "png", new File(out, "c2.png"));

        // ---- Table of slider settings -> CSV used by the report ----
        // first three rows = the settings used in the Wolfram demonstration (z = 0, 1,
        // 1.5); the rest are extra checks
        double[][] cases = { { 100, 15, 100 }, { 100, 15, 115 }, { 100, 15, 122.5 }, { 100, 15, 85 }, { 50, 10, 60 },
                { 0, 1, 1.96 } };
        try (PrintWriter pw = new PrintWriter(new File(out, "normal_table.csv"))) {
            pw.println("mu,sigma,x,z,left,right");
            System.out.println("\n mu  sigma   x     z    P(X<=x)  P(X>=x)");
            for (double[] c : cases) {
                double zz = (c[2] - c[0]) / c[1], l = cdf(c[2], c[0], c[1]);
                pw.printf("%s,%s,%s,%.2f,%.4f,%.4f%n", Plot.fmt(c[0]), Plot.fmt(c[1]), Plot.fmt(c[2]), zz, l, 1 - l);
                System.out.printf("%4.0f %5.0f %6.2f %6.2f  %.4f   %.4f%n", c[0], c[1], c[2], zz, l, 1 - l);
            }
        }
    }
}