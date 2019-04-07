#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <netdb.h>
#include <fcntl.h>
#include <pthread.h>

#define SA struct sockaddr

struct Message {
	char *id;
	char *message;
};

struct Argument {
    int sendToFd;
	int recvFromFd;
    struct sockaddr_in sendToServerAddress;
	struct sockaddr_in recvFromServerAddress;
	char* id;
	struct Message message;
};

void *sendToMessage(void *object)
{
	struct Argument *argument = object;

	int sockfd = argument->sendToFd;
	struct sockaddr_in servaddr = argument->sendToServerAddress;

	fcntl(sockfd, F_SETFL, O_NONBLOCK);

	struct Message message = argument->message;

    while (1) {
	    sendto(sockfd, &message, sizeof(struct Message), MSG_CONFIRM, (const struct sockaddr*)&servaddr, sizeof(servaddr));

        printf("Sent message: %s, to: %s\n", message.message, message.id);

        sleep(2);
    }
}

void *recvFromMessage(void *object) {
	struct Argument *argument = object;

	int sockfd = argument->recvFromFd;
	struct sockaddr_in clientAddress = argument->recvFromServerAddress;
	char* id = argument->id;

	int sockfd2 = argument->sendToFd;
	struct sockaddr_in serverAddress = argument->sendToServerAddress;

    fcntl(sockfd, F_SETFL, O_NONBLOCK);

	int length, recv;
	struct Message message;

	while (1) {
        recv = recvfrom(sockfd, &message, sizeof(struct Message), MSG_WAITALL, (struct sockaddr*)&clientAddress, &length);

        if (recv != -1) {
            if (message.id != NULL && strcmp(message.id, id) == 0) {
                printf("Received message: %s\n", message.message);
            } else {
                sendto(sockfd2, &message, sizeof(struct Message), MSG_CONFIRM, (const struct sockaddr*)&serverAddress, sizeof(serverAddress));
                printf("Received message to another client. Sending further.\n");
            }
        }
    }
}
void handleUDP(int portNumber, int neighbourPortNumber, char *ID, int token){
    // Receiving
	int sockfd;
	struct sockaddr_in serverAddress, clientAddress;

	if ((sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
		printf("Cannot create socket.\n");
		exit(EXIT_FAILURE);
	}

	memset(&serverAddress, 0, sizeof(serverAddress));
	memset(&clientAddress, 0, sizeof(clientAddress));

	serverAddress.sin_family = AF_INET;
	serverAddress.sin_addr.s_addr = INADDR_ANY;
	serverAddress.sin_port = htons(portNumber);

	if (bind(sockfd, (const struct sockaddr*)&serverAddress, sizeof(serverAddress)) < 0)
	{
        printf("Cannot bind.\n");
		exit(EXIT_FAILURE);
	}

	// Sending
	int sockfd2;
	struct sockaddr_in servaddr2;

	if ((sockfd2 = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        printf("Cannot create socket.\n");
        exit(EXIT_FAILURE);
	}

	memset(&servaddr2, 0, sizeof(servaddr2));

	servaddr2.sin_family = AF_INET;
	servaddr2.sin_port = htons(neighbourPortNumber);
	servaddr2.sin_addr.s_addr = INADDR_ANY;

    pthread_t sendtoThread;
    pthread_t recvFromThread;

    struct Argument argument;
    argument.sendToFd = sockfd2;
    argument.recvFromFd = sockfd;
    argument.sendToServerAddress = servaddr2;
    argument.recvFromServerAddress = serverAddress;
    argument.id = ID;

    struct Message message;
    message.id = "client3";
    message.message = "message to client3";

    argument.message = message;

    if (token == 1) {
        if(pthread_create(&sendtoThread, NULL, sendToMessage, (void *) &argument)) {
            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }
        if (pthread_create(&recvFromThread, NULL, recvFromMessage, (void *) &argument)) {
            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }
        if (pthread_join(sendtoThread, NULL)) {
            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);
        }
        if (pthread_join(recvFromThread, NULL)) {
            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);
        }
    } else {
        if (pthread_create(&recvFromThread, NULL, recvFromMessage, (void *) &argument)) {
            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }
        if(pthread_join(recvFromThread, NULL)) {
            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);
        }
    }
}

/**********************************************************************************************************************/

int connfd;

void *connectTCP(void *object)
{
    int length;
	struct Argument *argument = object;

	int sockfd = argument->recvFromFd;
	struct sockaddr_in client = argument->recvFromServerAddress;

    if ((listen(sockfd, 5)) != 0) {
        printf("Cannot listen.\n");
        exit(0);
    } else {
        printf("Listening...\n");
    }

    length = sizeof(client);

    connfd = accept(sockfd, (SA*)&client, &length);
    if (connfd < 0) {
        printf("Lack of client's acceptance.\n");
        exit(EXIT_FAILURE);
    }
    else {
        printf("Client accepted.\n");
    }
}

void *recvMessage (void *object) {
	struct Argument *argument = object;

	int sockfd = argument->sendToFd;
	char *id = argument->id;
    struct Message message;

    while (1) {
        int a = read(connfd, &message, sizeof(struct Message));
        if (a != -1) {
            if(message.id != NULL && strcmp(message.id, id) == 0) {
                printf("Received message: %s\n", message.message);
            } else {
                write(sockfd, &message, sizeof(struct Message));
                printf("Received message to another client. Sending further.\n");
            }
        }
    }
}

void *sendMessage (void *object) {
    struct Argument *argument = object;
    int sockfd = argument->sendToFd;

    while (1) {
        struct Message message = argument->message;

        write(sockfd, &message, sizeof(struct Message));

        printf("Sent message: %s, to: %s\n", message.message, message.id);

        sleep(2);
    }
}

void handleTcp (int portNumber, int neighbourPortNumber, char *ID, int hasToken) {
    // Receiving
    int sockfd;
    struct sockaddr_in serverAddress, neighbourAddress;

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd == -1) {
        printf("Cannot create socket.\n");
        exit(EXIT_FAILURE);
    } else {
        printf("Socket created.\n");
    }

    bzero(&serverAddress, sizeof(serverAddress));

    serverAddress.sin_family = AF_INET;
    serverAddress.sin_addr.s_addr = htonl(INADDR_ANY);
    serverAddress.sin_port = htons(portNumber);

    if ((bind(sockfd, (SA*)&serverAddress, sizeof(serverAddress))) != 0) {
        printf("Cannot bind.\n");
        exit(EXIT_FAILURE);
    }
    else {
        printf("Binded successfully.\n");
    }

    pthread_t connectThread;

	struct Argument connectArgument;
    connectArgument.recvFromFd = sockfd;
    connectArgument.recvFromServerAddress = neighbourAddress;

    if(pthread_create(&connectThread, NULL, connectTCP, (void *) &connectArgument)) {
        printf("Cannot created connect'argument thread.\n");
        exit(EXIT_FAILURE);
    }

    sleep(2);

    // Sending
	int sockfd2;
    struct sockaddr_in clientAddress;

    sockfd2 = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd2 == -1) {
        printf("Cannot create socket.\n");
        exit(EXIT_FAILURE);
    } else {
        printf("Socket created.\n");
    }

    bzero(&clientAddress, sizeof(clientAddress));

    clientAddress.sin_family = AF_INET;
    clientAddress.sin_addr.s_addr = inet_addr("127.0.0.1");
    clientAddress.sin_port = htons(neighbourPortNumber);

    if (connect(sockfd2, (SA*) &clientAddress, sizeof(clientAddress)) != 0) {
        printf("Cannot connect.\n");
        exit(EXIT_FAILURE);
    } else {
        printf("Connection created.\n");
    }

    // Creating recv and send threads
    pthread_t recvThread;
    pthread_t sendThread;

	struct Argument argument;
    argument.recvFromFd = sockfd;
	argument.sendToFd = sockfd2;
	argument.sendToServerAddress = clientAddress;
	argument.id = ID;

	struct Message message;
	message.id = "client3";
	message.message = "message to client3";

	argument.message = message;

    if (hasToken == 1) {
        if(pthread_create(&recvThread, NULL, recvMessage, (void *) &argument)) {

            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }
        if(pthread_create(&sendThread, NULL, sendMessage, (void *) &argument)) {

            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }
        if(pthread_join(recvThread, NULL)) {

            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);

        }
        if(pthread_join(sendThread, NULL)) {

            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);
        }
    } else {
        if(pthread_create(&recvThread, NULL, recvMessage, (void *) &argument)) {

            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }
        if(pthread_join(recvThread, NULL)) {

            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);
        }
    }
}


int main(int argc, char *argv[] ) {
	char *ID=argv[1];
    int portNumber = atoi(argv[2]);
	char* IPAddress = argv[3];
	int neighbourPortNumber = atoi(argv[4]);
	int hasToken = atoi(argv[5]);
	int protocol =  atoi(argv[6]);

	printf("Client's ID: %s\n", ID);
	printf("Client's port number : %i\n", portNumber);
	printf("Neighbour IP address: %s\n", IPAddress);
	printf("Neighbour port number: %i\n", neighbourPortNumber);

	if(hasToken) {
        printf("Has token: yes\n");
    } else {
        printf("Has token: no\n");
    }

	if (protocol == 0) {
        printf("Protocol: TCP\n");
        handleTcp(portNumber, neighbourPortNumber, ID, hasToken);
	}
	else if (protocol == 1) {
        printf("Protocol: UDP\n");
        handleUDP(portNumber, neighbourPortNumber, ID, hasToken);
	} else {
		printf("Wrong protocol. Choose 0 - TCP or 1 - UDP.");
		exit(EXIT_FAILURE);
	}

	exit(EXIT_SUCCESS);
}
