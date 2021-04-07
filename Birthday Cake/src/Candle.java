import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Candle {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine().replaceAll("\\s+", "");
        while (validateInput(input)) {
            System.out.println("Enter a proper number of candles!");
            input = reader.readLine().replaceAll("\\s+", "");
        }
        int size = Integer.parseInt(input);
        while (size <= 0 || size > 1000) {
            System.out.println("Cake must have a proper number of candles!");
            size = Integer.parseInt(reader.readLine().replaceAll("\\s+", ""));
        }
        int[] candles = new int[size];
        String[] strNumbers = reader.readLine().replaceAll("\\s+", " ").split(" ");
        while (strNumbers.length != size) {
            System.out.println(String.format("Your child is turning %s years old, but you placed %s candles!", size, strNumbers.length));
            strNumbers = reader.readLine().replaceAll("\\s+", " ").split(" ");
        }
        for (int i = 0; i < strNumbers.length; i++) {
            while (validateInput(strNumbers[i])) {
                System.out.println("Please specify a proper height of candles!");
                strNumbers = reader.readLine().replaceAll("\\s+", " ").split(" ");
            }
        }
        for (int i = 0; i < size; i++) {
            candles[i] = Integer.parseInt(strNumbers[i]);
        }
        int blownOut = birthdayCakeCandles(candles);
        System.out.println(blownOut);
    }

    private static boolean validateInput(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) >= '0' && input.charAt(i) <= '9') {
                return false;
            }
            else {
                return true;
            }
        }
        return false;
    }

    private static int birthdayCakeCandles(int[] arr) {
        int numberOfHighest = 0;
        int max = arr[0];
        for (int num : arr) {
            if (num > max) {
                max = num;
            }
        }
        for (int num : arr) {
            if (max == num) {
                numberOfHighest++;
            }
        }
        return numberOfHighest;
    }
}
