#include <stdlib.h>
#include <printf.h>
#include <zconf.h>
#include <stdio.h>
#include <sys/socket.h>
#include <string.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <signal.h>
#include <errno.h>
#include <stdbool.h>

char *id;
int port;

char *neighbourIP;
int neigbourPort;

bool hasToken;

