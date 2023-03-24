package ru.netology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static final int TEXT_COUNT = 10_000;
    public static final int TEXT_LENGTH = 100_000;
    public static final String LETTERS = "abc";
    public static BlockingQueue<String> QUEUE_FOR_A = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> QUEUE_FOR_B = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> QUEUE_FOR_C = new ArrayBlockingQueue<>(100);


    public static void main(String[] args) throws InterruptedException {

        AtomicInteger maxA = new AtomicInteger();
        AtomicInteger maxB = new AtomicInteger();
        AtomicInteger maxC = new AtomicInteger();


        List<Thread> threads = new ArrayList<>();


        threads.add(new Thread(() -> {
            for (int i = 0; i < TEXT_COUNT; i++) {
                String genText = generateText(LETTERS, TEXT_LENGTH);
                for (BlockingQueue<String> strings : Arrays.asList(QUEUE_FOR_A, QUEUE_FOR_B, QUEUE_FOR_C)) {
                    try {
                        strings.put(genText);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }));
        threads.add(new Thread(new MyRunnable('a', QUEUE_FOR_A, maxA)));
        threads.add(new Thread(new MyRunnable('b', QUEUE_FOR_B, maxB)));
        threads.add(new Thread(new MyRunnable('c', QUEUE_FOR_C, maxC)));

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("max 'a' in generated texts: " + maxA);
        System.out.println("max 'b' in generated texts: " + maxB);
        System.out.println("max 'c' in generated texts: " + maxC);
    }

    public static class MyRunnable implements Runnable {

        private char ch;
        private BlockingQueue<String> queue;
        private AtomicInteger max;

        public MyRunnable(char ch, BlockingQueue<String> queue, AtomicInteger max) {
            this.ch = ch;
            this.queue = queue;
            this.max = max;
        }

        @Override
        public void run() {
            for (int i = 0; i < TEXT_COUNT; i++) {
                try {
                    String inputText = queue.take();
                    int currentCount = (int) inputText.chars().filter(c -> c == ch).count();
                    if (max.get() < currentCount) {
                        max.set(currentCount);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

}