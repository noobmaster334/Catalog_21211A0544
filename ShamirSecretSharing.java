import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {

    // Decode a value from the given base to an integer
    private static BigInteger decodeBase(String value, int base) {
        return new BigInteger(value, base);
    }

    // Perform Lagrange interpolation to find the constant term
    private static BigInteger lagrangeInterpolation(List<Integer> xVals, List<BigInteger> yVals) {
        int n = xVals.size();
        BigInteger constantTerm = BigInteger.ZERO;

        for (int i = 0; i < n; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    numerator = numerator.multiply(BigInteger.valueOf(-xVals.get(j)));
                    denominator = denominator.multiply(BigInteger.valueOf(xVals.get(i) - xVals.get(j)));
                }
            }

            // Calculate the Lagrange basis polynomial L_i(0) * y_i
            BigInteger term = yVals.get(i).multiply(numerator).divide(denominator);
            constantTerm = constantTerm.add(term);
        }

        return constantTerm;
    }

    // Find the constant term (secret) from the input JSON file
    private static BigInteger findSecret(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject data = new JSONObject(content);

        JSONObject keys = data.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");

        List<Integer> xVals = new ArrayList<>();
        List<BigInteger> yVals = new ArrayList<>();

        for (String key : data.keySet()) {
            if (key.equals("keys")) continue;

            JSONObject root = data.getJSONObject(key);
            int x = Integer.parseInt(key);
            int base = root.getInt("base");
            String value = root.getString("value");

            BigInteger y = decodeBase(value, base);
            xVals.add(x);
            yVals.add(y);
        }

        // Use the first k roots to calculate the constant term
        xVals = xVals.subList(0, k);
        yVals = yVals.subList(0, k);

        // Calculate the constant term (c)
        return lagrangeInterpolation(xVals, yVals);
    }

    public static void main(String[] args) throws IOException {
        // Test case file paths
        String testCase1 = "test_case_1.json";
        String testCase2 = "test_case_2.json";

        BigInteger secret1 = findSecret(testCase1);
        BigInteger secret2 = findSecret(testCase2);

        System.out.println("Secret for Test Case 1: " + secret1);
        System.out.println("Secret for Test Case 2: " + secret2);
    }
}
