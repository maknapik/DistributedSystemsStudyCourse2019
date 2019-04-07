#include<stdio.h>
#include<stdlib.h>
#include <sys/socket.h>

#define DATA_MAX_SIZE 1024
#define SRC_SIZE 128
#define DST_SIZE 128 //sprawdzić size 128 dlaczego

typedef int bool;
#define true 1
#define false 0

typedef struct {

    char data[DATA_MAX_SIZE];
    //type
    char src[SRC_SIZE];
    char dst[DST_SIZE];

} Token;


typedef struct {

    char *id;
    int port;
    char *neigbourIP;
    int neigbourPort;
    bool hasToken;

} Client;

bool parseArguments(char **argv, Client *client){

    //obsluga zly parametrów - dodać później
    client->id = argv[1];
    client->port = atoi(argv[2]);
    client->neigbourIP = argv[3];
    client->neigbourPort = atoi(argv[4]);
    client->hasToken = atoi(argv[5]);
    return true;
}

void showClient(Client client){
    printf("%s %d %s %d %d\n", client.id, client.port, client.neigbourIP, client.neigbourPort, client.hasToken);
}

int main(int argc, char **argv){

    if(argc < 6){
        printf("%s\n", "WRONG ARGS");
        return -1;
    }

    Client client;
    parseArguments(argv, &client);
    // if(client.hasToken == true) {
    //     showClient(client);
    // }

    showClient(client);

    int socketId;

    socketId = socket(AF_INET, SOCK_STREAM, 0);

    if(socketId == -1){
        printf("%s\n", "ERROR IN SOCKET");
        exit(EXIT_FAILURE);
    }






    return 0;
}