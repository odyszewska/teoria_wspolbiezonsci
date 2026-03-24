This repository contains solutions and reports for three home assignments completed for **Teoria Współbieżności** course at **AGH University of Krakow**.

The assignments focus on different aspects of concurrency, synchronization, dependency analysis, and parallel algorithm design.

## Contents

### Assignment 1 (lab3) Bounded Buffer / Producer-Consumer Problem
Implementation of the bounded buffer problem using **Java** and the `wait()` / `notifyAll()` synchronization mechanism.

This part includes:
- the 1 producer / 1 consumer case,
- the n producers / m consumers case,
- experiments with `sleep()` to observe system behavior,
- a pipeline processing variant with multiple intermediate stages.

### Assignment 2 (lab5) Trace Theory
Implementation of a program in **Python** that:
- computes the **dependency relation**,
- computes the **independency relation**,
- finds the **Foata Normal Form (FNF)** of a trace,
- generates the **minimal dependency graph** for a given word.

The graph is generated with **Graphviz**.

### Assignment 3 (lab7) Concurrent Gauss-Jordan Elimination
Implementation of a concurrent **Gauss-Jordan elimination** algorithm in **C** with **OpenMP**.

This part includes:
- identification of atomic operations in the trace-theory model,
- construction of dependency relations,
- generation of the dependency graph,
- Foata normal form analysis,
- a parallel implementation with synchronization barriers reflecting dependency layers.

## Technologies
- Java
- Python
- Graphviz
- C
- OpenMP
