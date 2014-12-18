CC=gcc
CXXFLAGS=-Wall -O2 
CPP=g++


ttd_library: ttd_library.c ttd.h
	$(CC) $(CXXFLAGS) -c -o ttd_library.o ttd_library.c

ttd_example_client: ttd_library ttd_example_client.cpp
	$(CPP) $(CXXFLAGS) -o ttd_example_client.exe ttd_example_client.cpp ttd_library.o

ttd_simple: ttd_library ttd_simple.cpp
	$(CPP) $(CXXFLAGS) -o ttd_simple.exe ttd_simple.cpp ttd_library.o

ttd_stupid_simple: ttd_library ttd_stupid_simple.cpp
	$(CPP) $(CXXFLAGS) -o ttd_stupid_simple.exe ttd_stupid_simple.cpp ttd_library.o

ttd_smarter: ttd_library ttd_smarter.cpp
	$(CPP) $(CXXFLAGS) -o ttd_smarter.exe ttd_smarter.cpp ttd_library.o

ttd_luke: ttd_library ttd_luke.cpp
	$(CPP) $(CXXFLAGS) -o ttd_luke.exe ttd_luke.cpp ttd_library.o

clean: 
	rm *.o *.exe

all: ttd_library ttd_example_client ttd_luke ttd_simple ttd_stupid_simple ttd_smarter

