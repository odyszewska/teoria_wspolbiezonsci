#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <omp.h>

// Wypisanie komunikatu błędu i zakończenie program
static void die(const char *msg) {
    fprintf(stderr, "%s\n", msg);
    exit(1);
}

// Alokacja macierzy
static double **alloc_matrix(int rows, int cols) {
    double **M = (double **)malloc((size_t)rows * sizeof(double *));
    if (!M) die("alloc_matrix: malloc failed");

    for (int i = 0; i < rows; ++i) {
        M[i] = (double *)malloc((size_t)cols * sizeof(double));
        if (!M[i]) die("alloc_matrix: malloc row failed");
    }
    return M;
}

// Zwalnianie macierzy zaalokowanej przez alloc_matrix
static void free_matrix(double **M, int rows) {
    if (!M) return;
    for (int i = 0; i < rows; ++i) free(M[i]);
    free(M);
}

// Wczytanie danych i budowa macierzy rozszerzonej Aug = [A|b]
static void read_input(FILE *in, int *N_out, double ***Aug_out) {
    int N;
    if (fscanf(in, "%d", &N) != 1) die("Blad: brak N na wejsciu.");
    if (N <= 0) die("Blad: N <= 0.");

    // tworzymy macierz rozszerzoną
    double **Aug = alloc_matrix(N, N + 1);

    // Wczytujemy A: N*N liczb do kolumn 0..N-1
    for (int i = 0; i < N; ++i)
        for (int j = 0; j < N; ++j)
            if (fscanf(in, "%lf", &Aug[i][j]) != 1)
                die("Blad wczytywania macierzy A.");

    // Wczytujemy b: N liczb do ostatniej kolumny (indeks N)
    for (int i = 0; i < N; ++i)
        if (fscanf(in, "%lf", &Aug[i][N]) != 1)
            die("Blad wczytywania wektora b.");

    // wyniki
    *N_out = N;
    *Aug_out = Aug;
}

// Gauss-Jordan z pivotingiem i OpenMP
static void gauss_jordan(int N, double **Aug) {

    const double EPS_PIVOT = 1e-12; //próg dla "zbyt małego" pivota; jeśli |pivot| < EPS_PIVOT, traktujemy pivot jako 0

    double *m = (double *)malloc((size_t)N * sizeof(double)); // mnożniki
    if (!m) die("malloc failed for multipliers");

    // Iterujemy po kolejnych kolumnach / pivotach i = 0..N-1
    for (int i = 0; i < N; ++i) {

        int p = i;
        double best = fabs(Aug[i][i]);
        // Szukamy najlepszego pivota w kolumnie i
        for (int r = i + 1; r < N; ++r) {
            double v = fabs(Aug[r][i]);
            if (v > best) { best = v; p = r; }
        }
        if (best < EPS_PIVOT) die("Macierz osobliwa / brak poprawnego pivota.");

        // Zamiana wierszy przez zamianę wskaźników
        if (p != i) {
            double *tmp = Aug[i];
            Aug[i] = Aug[p];
            Aug[p] = tmp;
        }

        double pivot = Aug[i][i];

        // 3 fazy (Foata) z barierami
        #pragma omp parallel
        {
            // F1: normalizacja wiersza pivota
            #pragma omp for schedule(static)
            for (int j = 0; j < N + 1; ++j) {
                Aug[i][j] /= pivot;
            }

            #pragma omp barrier

            // F2: obliczenie mnożników m[k]
            #pragma omp for schedule(static)
            for (int k = 0; k < N; ++k) {
                m[k] = (k == i) ? 0.0 : Aug[k][i];
            }

            #pragma omp barrier

            // F3: eliminacja wierszy
            #pragma omp for schedule(static)
            for (int k = 0; k < N; ++k) {
                if (k == i) continue;

                double factor = m[k];

                // Jeśli mnożnik jest prawie zerowy, to nic nie zmieniamy
                if (fabs(factor) < EPS_PIVOT) {
                    Aug[k][i] = 0.0;
                    continue;
                }

                // Odejmujemy mnożnik * wiersz pivota od wiersza k
                for (int j = 0; j < N + 1; ++j) {
                    Aug[k][j] -= factor * Aug[i][j];
                }
            }
        }
    }
    // Sprzątanie
    free(m);
}

// Kosmetyka - zamiana -0.0 na 0.0
static double fix_neg_zero(double x) {
    if (x == -0.0) return 0.0;
    return x;
}

// Zapis wyjścia w formacie zgodnym z poleceniem
static void write_output(FILE *out, int N, double **Aug) {
    fprintf(out, "%d\n", N);

    for (int i = 0; i < N; ++i) {
        for (int j = 0; j < N; ++j) {
            if (j) fputc(' ', out);
            fprintf(out, "%.1f", fix_neg_zero(Aug[i][j]));
        }
        fputc('\n', out);
    }

    for (int i = 0; i < N; ++i) {
        if (i) fputc(' ', out);
        fprintf(out, "%.10f", fix_neg_zero(Aug[i][N]));
    }
    fputc('\n', out);
}


int main(int argc, char **argv) {
    if (argc != 3) {
        fprintf(stderr, "Uzycie: %s input.txt output.txt\n", argv[0]);
        return 1;
    }

    // Otwórz plik wejściowy
    FILE *in = fopen(argv[1], "r");
    if (!in) die("Nie mozna otworzyc pliku wejsciowego.");

    // Wczytaj dane
    int N = 0;
    double **Aug = NULL;
    read_input(in, &N, &Aug);
    fclose(in);

    // Wykonaj eliminację współbieżną
    gauss_jordan(N, Aug);

    // Zapisz wynik
    FILE *out = fopen(argv[2], "w");
    if (!out) die("Nie mozna otworzyc pliku wyjsciowego.");
    write_output(out, N, Aug);
    fclose(out);

    // Sprzątanie
    free_matrix(Aug, N);
    return 0;
}
