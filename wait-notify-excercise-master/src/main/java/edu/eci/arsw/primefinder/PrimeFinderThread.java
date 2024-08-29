package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;

class PrimeFinderThread extends Thread {

    int a, b;
    private List<Integer> primes;
    private static final Object lock = new Object();
    private volatile boolean paused;

    public PrimeFinderThread(int a, int b) {
        super();
        this.primes = new LinkedList<>();
        this.a = a;
        this.b = b;
    }

    @Override
    public void run() {
        for (int i = a; i < b; i++) {
            synchronized (lock) {
                // Verificar si el hilo está en pausa antes de continuar
                while (paused) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                if (isPrime(i)) {
                    primes.add(i);
                    System.out.println(i);
                }
            }
        }
    }

    boolean isPrime(int n) {
        boolean ans;
        if (n > 2) {
            ans = n % 2 != 0;
            for (int i = 3; ans && i * i <= n; i += 2) {
                ans = n % i != 0;
            }
        } else {
            ans = n == 2;
        }
        return ans;
    }

    public List<Integer> getPrimes() {
        return primes;
    }

    public void pauseThread() {
        paused = true;
    }

    public void resumeThread() {
        paused = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}