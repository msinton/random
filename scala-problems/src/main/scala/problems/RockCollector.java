package problems;

public class RockCollector {

    public static void main(String[] args) {
        int[][] input = {
            {0, 1, 2, 2, 1},
            {3, 2, 1, 1, 1},
            {2, 3, 5, 6, 1},
            {0, 0, 0, 2, 1},
            {1, 1, 1, 3, 1}};

        System.out.println(21 == bestRouteTotal(input));
    }

    static int[][] addColumnOfZeros(int[][] input) {
        int columnLength = input[0].length;
        int[][] result = new int[input.length][columnLength + 1];
        int i = 0;
        for(int[] row : input) {
            System.arraycopy(row, 0, result[i], 0, columnLength);
            i++;
        }
        return result;
    }

    static int bestRouteTotal(int[][] in) {

        // enables simpler iteration - no checking if at end of row
        int[][] input = addColumnOfZeros(in);

        int[] previousRowAccumulated = new int[in[0].length];

        for (int i = 0; i < input.length; i++) {
            int[] row = input[i];

            for (int j = row.length - 2; j >= 0; j--) {
                input[i][j] += Math.max(previousRowAccumulated[j], input[i][j+1]);
            }
            previousRowAccumulated = input[i];
        }
        return previousRowAccumulated[0];
    }
}
