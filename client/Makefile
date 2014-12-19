CC=gcc
CXXFLAGS=-Wall -O2 
CPP=g++


trade_library: trade_library.c trade.h
	$(CC) $(CXXFLAGS) -c -o trade_library.o trade_library.c

trade_example_client: trade_library trade_example_client.cpp
	$(CPP) $(CXXFLAGS) -o trade_example_client.exe trade_example_client.cpp trade_library.o

trade_simple: trade_library trade_simple.cpp
	$(CPP) $(CXXFLAGS) -o trade_simple.exe trade_simple.cpp trade_library.o

trade_stupid_simple: trade_library trade_stupid_simple.cpp
	$(CPP) $(CXXFLAGS) -o trade_stupid_simple.exe trade_stupid_simple.cpp trade_library.o

trade_smarter: trade_library trade_smarter.cpp
	$(CPP) $(CXXFLAGS) -o trade_smarter.exe trade_smarter.cpp trade_library.o

trade_luke: trade_library trade_luke.cpp
	$(CPP) $(CXXFLAGS) -o trade_luke.exe trade_luke.cpp trade_library.o

clean: 
	rm *.o *.exe

all: trade_library trade_example_client trade_luke trade_simple trade_stupid_simple trade_smarter

