// SonarQube/Qodana Training File: intentionally bad code with issues of varying severity.
// Drop into your project as e.g., src/main/java/training/SonarSins.java
// DO NOT use in production.

package training;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class JavaSins {

    // ======== SEVERE / CRITICAL-STYLE ISSUES ========

    // Hardcoded credentials (Security Hotspot / Critical)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/app";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root123"; // TODO remove before prod

    // Insecure random for tokens (Security Hotspot / High)
    public static String generateResetToken(int len) {
        Random r = new Random(); // not cryptographically secure
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // SQL injection (Critical)
    public static boolean login(String username, String password) throws SQLException {
        Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        Statement st = c.createStatement();
        // Vulnerable concatenation
        ResultSet rs = st.executeQuery(
                "SELECT * FROM users WHERE username='" + username + "' AND password='" + password + "'"
        );
        boolean ok = rs.next(); // leak rs, st, c
        return ok;
    }

    // Path traversal + no validation (Critical)
    public static String readUserFile(String userProvidedPath) throws IOException {
        File f = new File("data/" + userProvidedPath); // ../ traversal possible
        BufferedReader br = new BufferedReader(new FileReader(f)); // no try-with-resources
        String line;
        StringBuilder out = new StringBuilder();
        while ((line = br.readLine()) != null) {
            out.append(line).append("\n");
        }
        return out.toString(); // br never closed
    }

    // SSRF / insecure HTTP call + no timeouts (High)
    public static String fetchUrl(String anyUrl) throws IOException {
        URL url = new URL(anyUrl); // user-controlled
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET"); // no timeouts
        try (InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes());
        }
    }

    // Null pointer risk (High)
    public static int lengthOf(String s) {
        return s.length(); // NPE if s == null
    }

    // Array index out of bounds (High)
    public static int getThird(List<Integer> xs) {
        return xs.get(2); // no size check
    }

    // ======== MODERATE ISSUES ========

    // Resource leak + exception swallowing (Moderate)
    public static void exportCsv(List<String[]> rows, String path) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(path);
            for (String[] r : rows) {
                fw.write(String.join(",", r));
                fw.write("\n");
            }
        } catch (Exception e) {
            // swallowed
        } finally {
            // forget to close fw
        }
    }

    // Inefficient string concatenation in loop (Moderate)
    public static String joinWithDash(List<String> parts) {
        String out = "";
        for (String p : parts) {
            out += p + "-"; // use StringBuilder instead
        }
        return out;
    }

    // Unused method parameter + magic numbers (Moderate)
    public static double computePrice(double base, double taxRate, boolean vip) {
        double price = base * 1.2; // magic number
        if (vip) {
            price = price * 0.85; // magic number
        }
        return price; // taxRate unused
    }

    // Overly complex method (Moderate) - cyclomatic complexity
    public static int complexDecision(int a, int b, int c) {
        int r = 0;
        if (a > 0) {
            if (b > 0) {
                if (c > 0) r = 1;
                else if (c == 0) r = 2;
                else r = 3;
            } else if (b == 0) {
                if (c > 0) r = 4;
                else if (c == 0) r = 5;
                else r = 6;
            } else {
                if (c > 0) r = 7;
                else if (c == 0) r = 8;
                else r = 9;
            }
        } else if (a == 0) {
            if (b > 0) r = 10;
            else if (b == 0) r = 11;
            else r = 12;
        } else {
            r = 13;
        }
        return r;
    }

    // ======== LOW / STYLE / SMELLS ========

    // Dead code / unused field (Low)
    private int neverUsed = 42;

    // Non-final public static field (Low smell)
    public static String globalState = "MUTABLE";

    // Method name not following conventions (Low)
    public void DoStuff() {
        System.out.println("doing stuff");
    }

    // Duplicate code blocks (Low/Moderate smell)
    public static int abs1(int x) {
        if (x < 0) return -x;
        return x;
    }
    public static int abs2(int x) {
        if (x < 0) return -x;
        return x;
    }

    // Empty catch block (Low smell)
    public static void ignoreAll() {
        try {
            int x = 1 / 0;
        } catch (Exception e) {
        }
    }

    // System.out in server code (Low)
    public static void log(String msg) {
        System.out.println("[LOG] " + msg);
    }

    // Mutable static with race potential (Low/Moderate)
    private static int counter = 0;
    public static void increment() {
        counter++; // not atomic, no synchronization
    }

    // ======== INPUT VALIDATION / REGEX ========

    // ReDoS potential regex (Moderate/High)
    private static final Pattern BAD_EMAIL_REGEX = Pattern.compile("(.+)+@(.+)+\\.(.+)+");

    public static boolean isEmail(String s) {
        return BAD_EMAIL_REGEX.matcher(s).matches();
    }

    // ======== CONCURRENCY / TIME ========

    // Busy-wait (Low/Moderate)
    public static void waitForFlag(BooleanSupplier flag) {
        while (!flag.getAsBoolean()) {
            // spin-wait
        }
    }

    @FunctionalInterface
    public interface BooleanSupplier {
        boolean getAsBoolean();
    }

    // ======== MISC ========

    // Possible integer overflow (Low/Moderate)
    public static int add(int a, int b) {
        return a + b;
    }

    // Division by zero risk (Low/Moderate)
    public static double ratio(int a, int b) {
        return a / b; // integer division + div by zero
    }

    // Use of ThreadLocalRandom wrongly for security (Moderate)
    public static int luckyNumber() {
        return ThreadLocalRandom.current().nextInt(0, 10);
    }

    // Main to keep file executable
    public static void main(String[] args) throws Exception {
        log("token=" + generateResetToken(12));
        System.out.println("third=" + getThird(Arrays.asList(1, 2))); // will crash
        ignoreAll();
    }
}
