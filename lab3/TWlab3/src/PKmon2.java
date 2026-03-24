public class PKmon2 {
    private static final int POISON = Integer.MIN_VALUE;

    public static void main(String[] args) {
        int nProd = 1, nCons = 2;
        int capacity = 10;
        int pSleepMs = 1, cSleepMs = 1;
        int durationSeconds = 5;

        // Konfiguracja potoku
        int K = 3; // liczba etapów pośrednich: B,C,D

        int[] stageSleep = new int[] { 2, 0, 3 }; // różne prędkości B,C,D

        // Bufory: A->B, B->C, ..., ostatni->Z
        Buffer[] q = new Buffer[K + 1];
        for (int i = 0; i < q.length; i++) q[i] = new Buffer(capacity);

        long endTime = System.currentTimeMillis() + durationSeconds * 1000L;

        // Producenci (A) -> q[0]
        Producer[] producers = new Producer[nProd];
        for (int i = 0; i < nProd; i++)
            producers[i] = new Producer(q[0], pSleepMs, endTime, "Producer-" + i);

        // Etapy B.. (q[i] -> q[i+1])
        Stage[] stages = new Stage[K];
        for (int i = 0; i < K; i++) {
            stages[i] = new Stage(q[i], q[i+1], POISON, stageSleep[i],
                    "Stage-" + (char)('B'+i), nCons);
        }


        // Konsumenci (Z) czytają z q[K]
        Consumer[] consumers = new Consumer[nCons];
        for (int i = 0; i < nCons; i++)
            consumers[i] = new Consumer(q[K], cSleepMs, POISON, "Consumer-" + i);

        long t0 = System.currentTimeMillis();
        for (Thread t : producers) t.start();
        for (Thread s : stages) s.start();
        for (Thread t : consumers) t.start();

        for (Thread t : producers) try { t.join(); } catch (InterruptedException ignored) {}

        // Wstrzykujemy POISON do pierwszego bufora
        for (int i = 0; i < nCons; i++) q[0].put(POISON);

        for (Thread s : stages) try { s.join(); } catch (InterruptedException ignored) {}
        for (Thread t : consumers) try { t.join(); } catch (InterruptedException ignored) {}
        long t1 = System.currentTimeMillis();

        // STATYSTYKI
        long totalProduced = 0, totalConsumed = 0;

        System.out.printf(
                "POTOK A->...->Z | K=%d | Bufor=%d | pSleep=%dms | cSleep=%dms | czas=%ds%n",
                 K, capacity, pSleepMs, cSleepMs, durationSeconds
        );

        System.out.println("\n--- STATYSTYKI ---");
        for (Producer p : producers) {
            System.out.printf("%s: produced=%d%n", p.getName(), p.getProduced());
            totalProduced += p.getProduced();
        }
        for (Stage s : stages) {
            System.out.printf("%s: processed=%d%n", s.getName(), s.getProcessed());
        }
        for (Consumer c : consumers) {
            System.out.printf("%s: consumed=%d%n", c.getName(), c.getConsumed());
            totalConsumed += c.getConsumed();
        }

        System.out.printf("SUMA: produced=%d, consumed=%d%n", totalProduced, totalConsumed);
    }
}
