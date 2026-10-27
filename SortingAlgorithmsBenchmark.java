import java.util.Arrays;
import java.util.Random;

/**
 * Sorting Algorithms Benchmark
 * Tests five sorting algorithms on four input patterns.
 *
 * Run:
 *   javac SortingAlgorithmsBenchmark.java
 *   java SortingAlgorithmsBenchmark
 *
 * Optional array-size argument:
 *   java SortingAlgorithmsBenchmark 10000
 */
public class SortingAlgorithmsBenchmark {
    private static final int DEFAULT_SIZE = 100_000;
    private static final long RANDOM_SEED = 2026L;

    @FunctionalInterface
    interface Sorter {
        void sort(int[] a);
    }

    static class Algorithm {
        final String name;
        final Sorter sorter;

        Algorithm(String name, Sorter sorter) {
            this.name = name;
            this.sorter = sorter;
        }
    }

    public static void main(String[] args) {
        int n = DEFAULT_SIZE;
        if (args.length > 0) {
            try {
                n = Integer.parseInt(args[0]);
                if (n < 1) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                System.err.println("Array size must be a positive integer.");
                System.exit(1);
            }
        }

        Algorithm[] algorithms = {
            new Algorithm("Selection Sort", SortingAlgorithmsBenchmark::selectionSort),
            new Algorithm("Insertion Sort", SortingAlgorithmsBenchmark::insertionSort),
            new Algorithm("Bubble Sort", SortingAlgorithmsBenchmark::bubbleSort),
            new Algorithm("Merge Sort", SortingAlgorithmsBenchmark::mergeSort),
            new Algorithm("Quick Sort", SortingAlgorithmsBenchmark::quickSort)
        };

        String[] cases = {
            "Random",
            "Reversely Sorted",
            "Partially Sorted (80%)",
            "Fully Sorted"
        };

        int[][] originals = {
            makeRandom(n, RANDOM_SEED),
            makeReverseSorted(n),
            makePartiallySorted(n, RANDOM_SEED + 1),
            makeSorted(n)
        };

        long[][] times = new long[algorithms.length][cases.length];

        System.out.println("Array size N = " + n);
        System.out.println("Times are measured in microseconds (us).");
        System.out.println("Note: O(N^2) algorithms can take a very long time at N = 100,000.");
        System.out.println();

        for (int i = 0; i < algorithms.length; i++) {
            for (int j = 0; j < cases.length; j++) {
                // Every algorithm receives a fresh copy; previous runs cannot alter the input.
                int[] testArray = Arrays.copyOf(originals[j], originals[j].length);

                long startTime = System.nanoTime();
                algorithms[i].sorter.sort(testArray);
                long endTime = System.nanoTime();

                times[i][j] = (endTime - startTime) / 1_000L;

                if (!isSorted(testArray)) {
                    throw new IllegalStateException(
                        algorithms[i].name + " failed on " + cases[j]
                    );
                }
            }
        }

        printTable(algorithms, cases, times);
        writeCsv(algorithms, cases, times);
        System.out.println("\\nSaved raw measurements to benchmark_results.csv");
    }

    private static void writeCsv(Algorithm[] algorithms, String[] cases, long[][] times) {
        try (java.io.PrintWriter out = new java.io.PrintWriter("benchmark_results.csv")) {
            out.print("Algorithm");
            for (String testCase : cases) out.print("," + testCase);
            out.println();
            for (int i = 0; i < algorithms.length; i++) {
                out.print(algorithms[i].name);
                for (int j = 0; j < cases.length; j++) out.print("," + times[i][j]);
                out.println();
            }
        } catch (java.io.IOException ex) {
            System.err.println("Could not write benchmark_results.csv: " + ex.getMessage());
        }
    }

    private static int[] makeRandom(int n, long seed) {
        int[] a = new int[n];
        Random random = new Random(seed);
        for (int i = 0; i < n; i++) a[i] = random.nextInt();
        return a;
    }

    private static int[] makeReverseSorted(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = n - i;
        return a;
    }

    private static int[] makeSorted(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }

    /**
     * The first 80% of the array is ascending. The remaining 20% contains
     * random integers. This definition is documented so the test is reproducible.
     */
    private static int[] makePartiallySorted(int n, long seed) {
        int[] a = new int[n];
        int sortedLength = (int) (n * 0.80);
        for (int i = 0; i < sortedLength; i++) a[i] = i;
        Random random = new Random(seed);
        for (int i = sortedLength; i < n; i++) a[i] = random.nextInt();
        return a;
    }

    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }

    private static void printTable(Algorithm[] algorithms, String[] cases, long[][] times) {
        System.out.printf("%-22s | %15s | %19s | %24s | %15s%n",
            "Algorithm", cases[0], cases[1], cases[2], cases[3]);
        System.out.println("-".repeat(108));
        for (int i = 0; i < algorithms.length; i++) {
            System.out.printf("%-22s | %15d | %19d | %24d | %15d%n",
                algorithms[i].name, times[i][0], times[i][1],
                times[i][2], times[i][3]);
        }
    }

    // O(N^2): repeatedly select the minimum remaining element.
    public static void selectionSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[j] < a[minIndex]) minIndex = j;
            }
            int temp = a[i];
            a[i] = a[minIndex];
            a[minIndex] = temp;
        }
    }

    // O(N^2) worst case; efficient on already/nearly sorted input.
    public static void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    // O(N^2) worst case, with early termination when no swaps occur.
    public static void bubbleSort(int[] a) {
        for (int end = a.length - 1; end > 0; end--) {
            boolean swapped = false;
            for (int j = 0; j < end; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    // O(N log N): divide-and-conquer merge sort.
    public static void mergeSort(int[] a) {
        if (a.length < 2) return;
        int[] temp = new int[a.length];
        mergeSort(a, temp, 0, a.length - 1);
    }

    private static void mergeSort(int[] a, int[] temp, int left, int right) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSort(a, temp, left, mid);
        mergeSort(a, temp, mid + 1, right);
        if (a[mid] <= a[mid + 1]) return;
        merge(a, temp, left, mid, right);
    }

    private static void merge(int[] a, int[] temp, int left, int mid, int right) {
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            if (a[i] <= a[j]) temp[k++] = a[i++];
            else temp[k++] = a[j++];
        }
        while (i <= mid) temp[k++] = a[i++];
        while (j <= right) temp[k++] = a[j++];
        System.arraycopy(temp, left, a, left, right - left + 1);
    }

    // Expected O(N log N). Randomized pivot helps avoid common worst-case inputs.
    public static void quickSort(int[] a) {
        quickSort(a, 0, a.length - 1, new Random(RANDOM_SEED + 2));
    }

    private static void quickSort(int[] a, int low, int high, Random random) {
        while (low < high) {
            int pivotIndex = low + random.nextInt(high - low + 1);
            int pivot = a[pivotIndex];
            int i = low, j = high;
            while (i <= j) {
                while (a[i] < pivot) i++;
                while (a[j] > pivot) j--;
                if (i <= j) {
                    int temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                    i++;
                    j--;
                }
            }
            // Recurse into the smaller partition; loop over the larger one to limit stack depth.
            if (j - low < high - i) {
                if (low < j) quickSort(a, low, j, random);
                low = i;
            } else {
                if (i < high) quickSort(a, i, high, random);
                high = j;
            }
        }
    }
}
