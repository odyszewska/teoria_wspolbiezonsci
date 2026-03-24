public class PKmon {
    private static final int POISON = Integer.MIN_VALUE; // wartość nieprodukowana przez producentów

    public static void main(String[] args) {
        int nProd   = 1;    // liczba producentów (n1)
        int nCons   = 1;    // liczba konsumentów (n2)
        int capacity = 10;  // pojemność bufora
        int pSleepMs = 1;   // pauza producenta (ms)
        int cSleepMs = 1;   // pauza konsumenta (ms)
        int durationSeconds = 2; // czas pracy producentów (s)

        Buffer buf = new Buffer(capacity);

        // Dla czytelnego wypisu relacji n1 z n2
        String rel = nProd == nCons ? "=" : (nProd > nCons ? ">" : "<");

        long endTime = System.currentTimeMillis() + durationSeconds * 1000L;

        Producer[] producers = new Producer[nProd];
        Consumer[] consumers = new Consumer[nCons];

        // Tworzymy wątki
        for (int i = 0; i < nProd; i++) {
            producers[i] = new Producer(buf, pSleepMs, endTime, "Producer-" + i);
        }
        for (int i = 0; i < nCons; i++) {
            consumers[i] = new Consumer(buf, cSleepMs, POISON, "Consumer-" + i);
        }

        long t0 = System.currentTimeMillis();
        for (Thread t : producers) t.start();
        for (Thread t : consumers) t.start();

        // Czekamy, aż wszyscy producenci zakończą
        try {
            for (Thread t : producers) t.join();
        } catch (InterruptedException ignored) {}

        // Wstawiamy „trucizny”
        for (int i = 0; i < nCons; i++) {
            buf.put(POISON);
        }

        // Czekamy, aż konsumenci skończą po zjedzeniu „trucizny”
        try {
            for (Thread t : consumers) t.join();
        } catch (InterruptedException ignored) {}

        long t1 = System.currentTimeMillis();

        long totalProduced = 0, totalConsumed = 0;

        System.out.printf(
                "Wariant: %dP %s %dK  | Bufor=%d | pSleep=%dms | cSleep=%dms | czas=%ds%n",
                nProd, rel, nCons, capacity, pSleepMs, cSleepMs, durationSeconds
        );

        System.out.println("\n--- STATYSTYKI ---");
        for (Producer p : producers) {
            System.out.printf("%s: produced=%d%n", p.getName(), p.getProduced());
            totalProduced += p.getProduced();
        }
        for (Consumer c : consumers) {
            System.out.printf("%s: consumed=%d%n", c.getName(), c.getConsumed());
            totalConsumed += c.getConsumed();
        }

        System.out.printf("SUMA: produced=%d, consumed=%d%n", totalProduced, totalConsumed);
    }
}
