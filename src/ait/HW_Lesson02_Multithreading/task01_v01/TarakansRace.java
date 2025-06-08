package ait.HW_Lesson02_Multithreading.task01_v01;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TarakansRace {
    private static volatile boolean raceOver = false;
    private static int winnerId = -1;

    // Хранение времени каждого таракана
    private static final Map<Integer, Long> finishTimes = new ConcurrentHashMap<>();
    private static final AtomicInteger finishOrderCounter = new AtomicInteger(1); // счётчик порядка финиша
    private static final Map<Integer, Integer> finishPlaces = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите количество тараканов: ");
        int numTarakans = scanner.nextInt();

        System.out.print("Введите расстояние (итерации): ");
        int distance = scanner.nextInt();

        Thread[] threads = new Thread[numTarakans];
        long startTime = System.nanoTime();

        for (int i = 0; i < numTarakans; i++) {
            TarakanRunner runner = new TarakanRunner(i + 1, distance, startTime);
            threads[i] = new Thread(runner);
            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Сортировка по времени
        List<Map.Entry<Integer, Long>> results = new ArrayList<>(finishTimes.entrySet());
        results.sort(Map.Entry.comparingByValue());

        System.out.println("\n ФИНИШНАЯ ТАБЛИЦА:");
        System.out.println("------------------------------------------------------------");
        System.out.println("МЕСТО               УЧАСТНИК         ВРЕМЯ");
        System.out.println("------------------------------------------------------------");

        String[] places = {" 1 место:", " 2 место:", " 3 место:"};
        int rank = 1;
        for (Map.Entry<Integer, Long> entry : results) {
            String place = (rank <= 3) ? places[rank - 1] : String.format(" %d место:", rank);
            double seconds = entry.getValue() / 1_000_000_000.0;
            System.out.printf("%-18s %-15s %8.6f сек\n", place, "Таракан #" + entry.getKey(), seconds);

            rank++;
        }

        System.out.printf("\n Congratulations to tarakan #%d (winner) \n", winnerId);
    }

    static class TarakanRunner implements Runnable {
        private final int id;
        private final int distance;
        private final long startTime;
        private final Random rand = new Random();

        public TarakanRunner(int id, int distance, long startTime) {
            this.id = id;
            this.distance = distance;
            this.startTime = startTime;
        }

        @Override
        public void run() {
            for (int i = 0; i < distance; i++) {
                    System.out.println("Tarakan #" + id + " ran step " + (i + 1));
                try {
                    Thread.sleep(2 + rand.nextInt(4)); // 2–5 ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long finishTime = System.nanoTime() - startTime;
            finishTimes.put(id, finishTime);

            int place = finishOrderCounter.getAndIncrement();
            finishPlaces.put(id, place);

            double seconds = finishTime / 1_000_000_000.0;
            System.out.printf("Tarakan #%d закончил гонку за %.6f сек с %d-м результатом\n", id, seconds, place);

            synchronized (TarakansRace.class) {
                if (!raceOver) {
                    raceOver = true;
                    winnerId = id;
                }
            }
        }
    }
}


