public class Consumer extends Thread {
    private final Buffer buf;      // bufor
    private final int sleepMs;     // pauza między konsumpcjami
    private final int poison;      // specjalna wartość sygnalizująca koniec
    private long consumed = 0;     // statystyka ile elementów skonsumowano

    public Consumer(Buffer buf, int sleepMs, int poison, String name) {
        super(name);
        this.buf = buf;
        this.sleepMs = sleepMs;
        this.poison = poison;
    }

    public long getConsumed() { return consumed; }

    @Override public void run() {
        while (true) {
            int v = buf.get();
            if (v == poison) break; // „trucizna” kończy pracę konsumenta
            consumed++;
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
            }
        }
    }
}
