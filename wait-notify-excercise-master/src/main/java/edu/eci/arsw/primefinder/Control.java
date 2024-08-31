package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Control extends Thread {

    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 30000000;
    private final static int TMILISECONDS = 5000;

    private final int NDATA = MAXVALUE / NTHREADS;
    private PrimeFinderThread[] pft;
    private static final Object lock = new Object();

    private Control() {
        super();
        this.pft = new PrimeFinderThread[NTHREADS];

        int i;
        for (i = 0; i < NTHREADS - 1; i++) {
            PrimeFinderThread elem = new PrimeFinderThread(i * NDATA, (i + 1) * NDATA);
            pft[i] = elem;
        }
        pft[i] = new PrimeFinderThread(i * NDATA, MAXVALUE + 1);
    }

    public static Control newControl() {
        return new Control();
    }

    @Override
    public void run() {
        for (int i = 0; i < NTHREADS; i++) {
            pft[i].start();
        }

        while (true) {
            try {
                Thread.sleep(TMILISECONDS);

                synchronized (lock) {
                    for (PrimeFinderThread thread : pft) {
                        thread.pauseThread();
                    }

                    // Mostrar el número de primos encontrados hasta el momento
                    int totalPrimes = 0;
                    for (PrimeFinderThread thread : pft) {
                        totalPrimes += thread.getPrimes().size();
                    }
                    System.out.println("Primos encontrados hasta ahora: " + totalPrimes);

                    System.out.println("Presiona ENTER para continuar...");
                    new Scanner(System.in).nextLine();

                    // Reanudar los hilos
                    for (PrimeFinderThread thread : pft) {
                        thread.resumeThread();
                    }

                    lock.notifyAll();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
