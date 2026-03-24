CC=gcc
CFLAGS=-O2 -Wall -Wextra -std=c11 -fopenmp
TARGET=program

all: $(TARGET)

$(TARGET): program.c
	$(CC) $(CFLAGS) program.c -o $(TARGET)

clean:
	rm -f $(TARGET)
