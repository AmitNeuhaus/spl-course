CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -lpthread

all: MainClient
	g++ -o bin/BGSclient bin/connectionHandler.o bin/MainClient.o  bin/encoderDecoder.o $(LDFLAGS)

MainClient: bin/connectionHandler.o bin/encoderDecoder.o bin/MainClient.o

bin/MainClient.o: src/MainClient.cpp
	g++ $(CFLAGS) -o bin/MainClient.o src/MainClient.cpp

bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp


bin/encoderDecoder.o: src/encoderDecoder.cpp
	g++ $(CFLAGS) -o bin/encoderDecoder.o src/encoderDecoder.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
