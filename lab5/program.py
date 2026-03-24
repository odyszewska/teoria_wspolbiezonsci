import sys
import re
from collections import defaultdict
import io
from graphviz import Digraph 

# Zwraca lewą i prawą stronę równania
def split(expr):
    expr = expr.strip()
    if ':=' in expr:
        left, right = expr.split(':=', 1)
    elif '=' in expr:
        left, right = expr.split('=', 1)
    else:
        raise ValueError(f"Nie rozpoznano w: {expr}")
    return left.strip(), right.strip()

# Linie z formatu jak w zadaniu ((a) x := x + 1)), 
# zapisuje w postaci ułatwiającej liczenie relacji D i I 
# ('a': {'R': {'x'}, 'W': {'x'}}, R-odczyt. W- zapis.)
def get_transactions(file):
    text = file.read()
    lines = [ln.strip() for ln in text.splitlines() if ln.strip()]
    transactions = {}
    for line in lines:
        m = re.match(r'\(([A-Za-z])\)\s*(.+)$', line)
        if not m:
            continue  # ignorujemy np. linie "A = {...}" albo "w = ..."
        tr, expr = m.group(1), m.group(2)
        left, right = split(expr)
        R = set(re.findall(r'[A-Za-z]+', right))
        W = {left}
        transactions[tr] = {'R': R, 'W': W}
    return transactions

# Parsuje jeden plik zawierający:
    #   - linie transakcji '(a) x := ...'
    #   - linię 'A = {...}'
    #   - linię 'w = ...'
    # Zwraca (transactions, alphabet, word)
def parse_input_file(text):
    # transakcje
    transactions = get_transactions(io.StringIO(text))

    # alfabet
    a = re.search(r'A\s*=\s*\{([^}]*)\}', text)
    if a:
        letters = re.findall(r'[a-z]', a.group(1))
        alphabet = sorted(set(letters))
    else: # jeśli nie ma podanego alfabetu to można go wyciągnąć z transakcji
        alphabet = sorted(transactions.keys())

    # słowo
    w = re.search(r'\bw\s*=\s*([a-z]+)', text)
    if not w:
        raise ValueError("Nie znaleziono linii 'w = ...' w pliku case.")
    word = w.group(1)

    return transactions, alphabet, word

# Wyznacza relację zależności D na alfabecie
def get_dependent_transactions(transactions, alphabet):
    D = set()
    for p in alphabet:
        for q in alphabet:
            Wp, Rp = transactions[p]['W'], transactions[p]['R']
            Wq, Rq = transactions[q]['W'], transactions[q]['R']
            conflict = (
                p == q or
                (Wp & Wq) or
                (Wp & Rq) or
                (Rp & Wq)
            )
            if conflict:
                D.add((p, q))
    return D

# Zwraca słownik: litera -> zbiór liter, od których zależy (bez siebie)
def get_dependencies(dependent, alphabet):
    dep = {a: set() for a in alphabet}
    for a, b in dependent:
        if a != b:
            dep[a].add(b)
    return dep


# Zwraca postać normalną Foaty FNF([w]) śladu [w]
def get_FNF(dependencies, word):
    n = len(word)
    edges = build_edges(word, dependencies)
    edges = reduction(edges)

    # Listy sąsiedztwa: wejściowe (in_adj) i wyjściowe (out_adj)
    out_list = defaultdict(set)
    in_list = defaultdict(set)
    for u, v in edges:
        out_list[u].add(v)
        in_list[v].add(u)

    # Zbiór jeszcze nie "użytych" pozycji w słowie
    remaining = set(range(1, n + 1))

    fnf = []

    # Warstwowe zdejmowanie wierzchołków bez poprzedników
    while remaining:
        # Pozycje bez krawędzi przychodzących z pozostałych wierzchołków
        layer_positions = sorted(
            i for i in remaining
            if not (in_list[i] & remaining)
        )

        # Litery w tej warstwie
        layer_letters = [word[i - 1] for i in layer_positions]
        fnf.append(tuple(sorted(layer_letters)))

        # Usuwamy wierzchołki tej warstwy z grafu
        for i in layer_positions:
            remaining.remove(i)
            for v in out_list[i]:
                in_list[v].discard(i)

    return fnf


# Zwraca krawędzie, i->j jeśli i<j oraz (word[i] zależy od word[j]) lub word[i]==word[j]
def build_edges(word, dependencies):
    edges = set()
    n = len(word)
    for i in range(n - 1):
        for j in range(i + 1, n):
            if word[j] in dependencies[word[i]] or word[i] == word[j]:
                edges.add((i + 1, j + 1))
    return edges

# Usuwa (u,v) jeśli istnieje ścieżka u->v przez co najmniej jeden węzeł
def reduction(edges):
    l = defaultdict(list)
    for u, v in edges:
        l[u].append(v)

    reduced = set(edges)
    for (u, v) in list(edges):
        stack = [x for x in l[u] if x != v] # stos wierzchołków do odwiedzenia
        seen = set([u])                     # zbiór odwiedzonych wierzchołków
        reachable = False                   # czy dotarliśmy do v
        while stack:
            w = stack.pop()
            if w == v:
                reachable = True
                break
            if w in seen:
                continue
            seen.add(w)
            stack.extend(l[w])
        if reachable:
            reduced.discard((u, v))
    return reduced


if __name__ == '__main__':
    if len(sys.argv) == 2:
        filename = sys.argv[1]
        try:
            with open(filename, encoding='utf8') as f:
                text = f.read()
        except IOError:
            print(f"Nie udało się otworzyć pliku: {filename}")
            sys.exit(0)

        transactions, alphabet, word = parse_input_file(text)
    else:
        print("Nieprawidłowa liczba argumentów! \n Prawidłowe uruchomienie: python program.py <plik>")
        sys.exit(0)

    # Wyznaczenie relacji zależności i niezależności
    sigma2 = [(ai, aj) for aj in alphabet for ai in alphabet]
    dependent = get_dependent_transactions(transactions, alphabet)
    independent = sorted(list(set(sigma2) - dependent))    # I = (sigma)^2 \ D
    dependent = sorted(list(dependent))

    print("\nRelacja zależności:\n D = {", end = "")
    for i in range(len(dependent)):
        if i > 0: print(",", end = "")
        print(f"({dependent[i][0]},{dependent[i][1]})", end = "")
    print("}")

    print("\nRelacja niezależności:\nI = {", end = "")
    for i in range(len(independent)):
        if i > 0: print(",", end = "")
        print(f"({independent[i][0]},{independent[i][1]})", end = "")
    print("}")

    # Wyznaczenie FNF
    dependencies = get_dependencies(dependent, alphabet)
    fnf = get_FNF(dependencies, word)
    print("\nFNF:\nFNF([w]) = ", end = "")
    for f in fnf:
        print("(", end = "")
        for transaction in sorted(f):
            print(transaction, end = "")
        print(")", end = "")
    print()

    # Wznaczenie grafu w postaci DOT
    graph_edges = reduction(build_edges(word, dependencies))
    print("\nGraf w postaci dot:\ndigraph g{")
    for edge in sorted(graph_edges, key=lambda edge: (edge[1], edge[0])):
        u, v = edge
        print(f"    {u} -> {v};")
    for i, ch in enumerate(word, start=1):
        print(f'    {i} [label="{ch}"];')
    print("}\n")

    # Rysowanie grafu za pomocą Graphviz
    dot = Digraph("g")
    for i, ch in enumerate(word, start=1):
        dot.node(str(i), label=ch)

    for u, v in sorted(graph_edges, key=lambda e: (e[1], e[0])):
        dot.edge(str(u), str(v))

    try:
        dot.render("graph", format="png", cleanup=True)
        print("Graf zapisano do pliku graph.png")
    except Exception as e:
        print(e)
