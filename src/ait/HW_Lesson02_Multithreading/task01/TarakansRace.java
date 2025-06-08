package ait.HW_Lesson02_Multithreading.task01;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class TarakansRace {
    private static final AtomicInteger winner = new AtomicInteger(-1);
    private static final List<Integer> finishTable = new CopyOnWriteArrayList<>();
    private static final List<Double> finishTimes = new CopyOnWriteArrayList<>();
    private static final AtomicInteger finishedCount = new AtomicInteger(0);
    private static long raceStartTime;

    private static final String[] COLORS = {
            "\u001B[31m", // Красный
            "\u001B[32m", // Зеленый
            "\u001B[33m", // Желтый
            "\u001B[34m", // Синий
            "\u001B[35m", // Фиолетовый
            "\u001B[36m", // Голубой
            "\u001B[37m", // Белый
            "\u001B[91m", // Яркий красный
            "\u001B[92m", // Яркий зеленый
            "\u001B[93m"  // Яркий желтый
    };
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private static int totalDistance;
    private static int numberOfTarakans;
    private static int[] tarakanProgress;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите количество тараканов: ");
        numberOfTarakans = scanner.nextInt();

        System.out.print("Введите расстояние (количество итераций): ");
        totalDistance = scanner.nextInt();

        tarakanProgress = new int[numberOfTarakans];

        System.out.println("\n" + BOLD + "🏁 Гонка тараканов началась! 🏁" + RESET + "\n");

        // Запоминаем время старта гонки
        raceStartTime = System.currentTimeMillis();

        // Создаем и запускаем потоки-тараканы
        Thread[] tarakans = new Thread[numberOfTarakans];
        Thread progressThread = new Thread(new ProgressDisplay(numberOfTarakans));

        for (int i = 0; i < numberOfTarakans; i++) {
            final int tarakanNumber = i + 1;
            tarakans[i] = new Thread(new Tarakan(tarakanNumber, totalDistance, i));
            tarakans[i].start();
        }

        // Запускаем поток отображения прогресса
        progressThread.start();

        // Ждем завершения всех потоков
        for (Thread tarakan : tarakans) {
            try {
                tarakan.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Останавливаем отображение прогресса
        progressThread.interrupt();

        // Финальное отображение результатов
        displayFinalResults();

        scanner.close();
    }

    private static void displayFinalResults() {
        // Очищаем экран для финальных результатов
        System.out.print("\033[2J\033[H");

        System.out.println(BOLD + "🏆 ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ ГОНКИ 🏆" + RESET);
        System.out.println("=".repeat(50));

        // Отображаем финальный прогресс-бар
        for (int i = 0; i < tarakanProgress.length; i++) {
            String color = COLORS[i % COLORS.length];
            System.out.print(color + "Таракан #" + (i + 1) + ": " + RESET);
            displayProgressBar(tarakanProgress[i], totalDistance, color);
        }

        System.out.println("\n" + BOLD + "📋 ФИНИШНАЯ ТАБЛИЦА:" + RESET);
        System.out.println("-".repeat(60));
        System.out.printf("%-19s %-16s %s%n", "МЕСТО", "УЧАСТНИК", "ВРЕМЯ");
        System.out.println("-".repeat(60));

        for (int i = 0; i < finishTable.size(); i++) {
            int tarakanNumber = finishTable.get(i);
            double finishTime = finishTimes.get(i);
            String medal = getMedal(i + 1);
            String color = COLORS[(tarakanNumber - 1) % COLORS.length];

            // Форматируем строки без цветовых кодов для правильного выравнивания
            String place = medal + " " + (i + 1) + " место:";
            String participant = "Таракан #" + tarakanNumber;
            String time = String.format("%.3f сек", finishTime);

            // Выводим с цветом только участника
            System.out.printf("%-18s %s%-15s%s %s%n",
                    place,
                    color + BOLD,
                    participant,
                    RESET,
                    time);
        }

        System.out.println("\n" + BOLD + COLORS[1] +
                "🎉 Congratulations to tarakan #" + winner.get() + " (winner) 🎉" + RESET);
    }

    private static String getMedal(int position) {
        switch (position) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "🏅";
        }
    }

    private static void displayProgressBar(int current, int total, String color) {
        int barLength = 30;
        int filled = (int) ((double) current / total * barLength);

        System.out.print("[");
        System.out.print(color);

        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                System.out.print("█");
            } else {
                System.out.print("░");
            }
        }

        System.out.print(RESET);
        System.out.printf("] %d/%d (%.1f%%)\n", current, total, (double) current / total * 100);
    }

    static class ProgressDisplay implements Runnable {
        private final int numberOfTarakans;

        public ProgressDisplay(int numberOfTarakans) {
            this.numberOfTarakans = numberOfTarakans;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted() && finishedCount.get() < numberOfTarakans) {
                // Очищаем экран
                System.out.print("\033[2J\033[H");

                System.out.println(BOLD + "🏃 ЖИВАЯ ГОНКА ТАРАКАНОВ 🏃" + RESET);
                System.out.println("=".repeat(50));

                for (int i = 0; i < numberOfTarakans; i++) {
                    String color = COLORS[i % COLORS.length];
                    System.out.print(color + "Таракан #" + (i + 1) + ": " + RESET);
                    displayProgressBar(tarakanProgress[i], totalDistance, color);
                }

                System.out.println("\n" + BOLD + "🎯 Цель: " + totalDistance + " итераций" + RESET);

                try {
                    Thread.sleep(1); // Обновляем каждые 100мс
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    static class Tarakan implements Runnable {
        private final int number;
        private final int distance;
        private final int progressIndex;
        private final Random random;

        public Tarakan(int number, int distance, int progressIndex) {
            this.number = number;
            this.distance = distance;
            this.progressIndex = progressIndex;
            this.random = new Random();
        }

        @Override
        public void run() {
            for (int iteration = 1; iteration <= distance; iteration++) {
                // Обновляем прогресс
                tarakanProgress[progressIndex] = iteration;

                // Проверяем, финишировал ли таракан
                if (iteration == distance) {
                    // Вычисляем время финиша
                    double finishTime = (System.currentTimeMillis() - raceStartTime) / 1000.0;

                    // Добавляем в финишную таблицу
                    finishTable.add(number);
                    finishTimes.add(finishTime);

                    // Увеличиваем счетчик финишировавших
                    finishedCount.incrementAndGet();

                    // Пытаемся стать победителем (только первый сможет)
                    if (winner.compareAndSet(-1, number)) {
                        // Первый финишировавший становится победителем
                    }
                    return;
                }

                // Засыпаем на случайное время от 2 до 5 миллисекунд
                try {
                    int sleepTime = 2 + random.nextInt(4); // 2-5 мс
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}