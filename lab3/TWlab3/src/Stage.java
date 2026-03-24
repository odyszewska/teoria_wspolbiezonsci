class Stage extends Thread {
    private final Buffer in, out;        // kolejki: wejściowa i wyjściowa
    private final int POISON;            // specjalna wartość sygnalizująca koniec
    private final int sleepMs;           // prędkości etapów
    private final int poisonsToPass;     // ile trucizn trzeba przepuścić dalej
    private long processed = 0;          // licznik elementów

    Stage(Buffer in, Buffer out, int poison, int sleepMs, String name, int poisonsToPass) {
        super(name);
        this.in = in; this.out = out;
        this.POISON = poison; this.sleepMs = sleepMs;
        this.poisonsToPass = poisonsToPass;
    }

    public long getProcessed() { return processed; }

    public void run() {
        int passed = 0; // ile POISON już przepuściliśmy dalej
        while (true) {
            int v = in.get();
            if (v == POISON) {
                out.put(POISON);
                if (++passed == poisonsToPass) // kończymy dopiero po przepchnięciu wszystkich
                    break;
                continue;
            }
            processed++;
            out.put(v);
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
            }
        }
    }
}
