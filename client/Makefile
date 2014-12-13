CC=gcc
CXXFLAGS=-Wall -O2 
CPP=g++


ttd_library: ttd_library.c ttd.h
	$(CC) $(CXXFLAGS) -c -o ttd_library.o ttd_library.c

ttd_example_client: ttd_library ttd_example_client.cpp
	$(CPP) $(CXXFLAGS) -o ttd_example_client.exe ttd_example_client.cpp ttd_library.o

ttd_luke: ttd_library ttd_luke.cpp
	$(CPP) $(CXXFLAGS) -o ttd_luke.exe ttd_luke.cpp ttd_library.o

clean: 
	rm *.o *.exe

all: ttd_library ttd_example_client ttd_luke

