public class Producer extends Thread {
    private final Buffer buf;       // bufor
    private final int sleepMs;      // pauza między produkcjami (do obserwacji)
    private final long endTimeMs;    // znacznik czasu zakończenia pracy
    private long produced = 0;       // statystyka ile elementów wyprodukowano

    public Producer(Buffer buf, int sleepMs, long endTimeMs, String name) {
        super(name);
        this.buf = buf;
        this.sleepMs = sleepMs;
        this.endTimeMs = endTimeMs;
    }

    public long getProduced() { return produced; }

    @Override public void run() {
        int x = 0;
        while (System.currentTimeMillis() < endTimeMs) {
            buf.put(x++);
            produced++;
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
            }
        }
    }
}
