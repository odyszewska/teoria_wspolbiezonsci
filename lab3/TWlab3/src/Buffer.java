class Buffer {
    private final int[] data;   // tablica przechowująca dane
    private int head = 0;       // indeks odczytu
    private int tail = 0;       // indeks zapisu
    private int count = 0;      // aktualna liczba elementów w buforze

    public Buffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.data = new int[capacity];
    }

    // Wkłada element do bufora, jeśli jest pełny to czeka.
    public synchronized void put(int i) {
        while (count == data.length) {
            try { wait(); } catch (InterruptedException ignored) {}
        }
        data[tail] = i;
        tail = (tail + 1) % data.length;
        count++;
        notifyAll();
    }

    // Pobiera element z bufora, jeśli jest pusty to czeka.
    public synchronized int get() {
        while (count == 0) {
            try { wait(); } catch (InterruptedException ignored) {}
        }
        int i = data[head];
        head = (head + 1) % data.length;
        count--;
        notifyAll();
        return i;
    }

    public int capacity() { return data.length; }
}
