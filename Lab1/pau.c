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

#define MAXLINE 1024
#define SA struct sockaddr

#define MAX 1024

struct token_message{
    char *id;
    char *message;
}mes;

struct arg_struct {
    int sendtofd;
    int recvfromfd;
    struct sockaddr_in sendtoservaddr2;
    struct sockaddr_in recvfromservaddr2;
    char* id;
    struct token_message me;
}argument;

struct token_message new_token_message;


void *sendto_thread_function(void *arguments)
{
    printf("Jestem w wątkowej funkcji sendto_thread_function\n");

    char buffer2[MAXLINE];

    struct arg_struct *args = arguments;

    int sockfd2=args -> sendtofd;
    struct sockaddr_in	 servaddr2 = args -> sendtoservaddr2;
    char* hello2 = args->id;


    sleep(10);

    int n2, len2;

    sleep(5);

    fcntl(sockfd2,F_SETFL,O_NONBLOCK);

    struct token_message m = args->me;
    //printf("Nowa wiadomość: %s", m.message);

    while(1){
        n2 = sendto(sockfd2, &m, sizeof(struct token_message),
                    MSG_CONFIRM, (const struct sockaddr *) &servaddr2,
                    sizeof(servaddr2));
        printf("Hello message sent.\n");
        printf("1 wysłał : %i bitow\n", n2);

        sleep(7);
    }
    //return NULL;

}



void *recvfrom_thread_function(void *arguments){
    printf("Jestem w wątkowej funkcji recvfrom_thread_function\n");

    struct arg_struct *args = arguments;

    int sockfd=args -> recvfromfd;
    struct sockaddr_in	 cliaddr = args -> recvfromservaddr2;
    char* id = args -> id;

    int sockfd2=args -> sendtofd;
    struct sockaddr_in	 servaddr2 = args -> sendtoservaddr2;
    char* hello2 = args->id;

    fcntl(sockfd,F_SETFL,O_NONBLOCK);


    int len, n;
    struct token_message m;

    while(1){
        sleep(3);
        n = recvfrom(sockfd, &m, sizeof(struct token_message), MSG_WAITALL, ( struct sockaddr *) &cliaddr, &len);

        if(n!=-1){
            printf("1 odebrał : %i bitow\n", n);
            printf("1 odebrał wiadomość : %s\n", m.message);
            printf("1 odebrał wiadomość : %s\n", m.id);


            if(m.id != NULL && strcmp(m.id, id)==0){
                printf("Wiadomość do mnie!\n");

            }
            else{
                printf("Wiadomość nie do mnie :(");
            }

            int n2;

            n2 = sendto(sockfd2, &m, sizeof(struct token_message),
                        MSG_CONFIRM, (const struct sockaddr *) &servaddr2,
                        sizeof(servaddr2));
            printf("Hello message sent.\n");
            printf("1 wysłał : %i bitow\n", n2);
            sleep(5);
        }


        sleep(5);
    } // koniec whilea

    //return NULL;



    // MULTICAST IMPLEMENTATION

    char* group = "239.255.255.250";
    int multicast_port = 8989;

    const int delay_secs = 1;
    const char *multicast_message = "Hello, World!";



    int multicast_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (multicast_fd < 0) {
        perror("socket");
        exit(EXIT_FAILURE);
    }


    struct sockaddr_in multicast_addr;
    memset(&multicast_addr, 0, sizeof(multicast_addr));
    multicast_addr.sin_family = AF_INET;
    multicast_addr.sin_addr.s_addr = inet_addr(group);
    multicast_addr.sin_port = htons(multicast_port);

    char ch = 0;
    int nbytes = sendto(multicast_fd,multicast_message,strlen(multicast_message),0,(struct sockaddr*) &multicast_addr, sizeof(multicast_addr)
    );
    if (nbytes < 0) {
        perror("sendto");
        exit(EXIT_FAILURE);
    }


    sleep(delay_secs); // Unix sleep is seconds

    //return NULL;

}

void handle_udp(int recvfrom_port_number, int sendto_port_number, char *ID, int token){
// SOCKFD dla recvfrom
    int sockfd;
    struct sockaddr_in servaddr, cliaddr;

    if ( (sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0 ) {
        perror("socket creation failed");
        exit(EXIT_FAILURE);
    }

    memset(&servaddr, 0, sizeof(servaddr));
    memset(&cliaddr, 0, sizeof(cliaddr));

    servaddr.sin_family = AF_INET; // IPv4
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port = htons(recvfrom_port_number);

    if ( bind(sockfd, (const struct sockaddr *)&servaddr,
              sizeof(servaddr)) < 0 )
    {
        perror("bind failed");
        exit(EXIT_FAILURE);
    }

// SOCKFD2 dla sendto
    int sockfd2;
    struct sockaddr_in	 servaddr2;

    if ( (sockfd2 = socket(AF_INET, SOCK_DGRAM, 0)) < 0 ) {
        perror("socket creation failed");
        exit(EXIT_FAILURE);
    }

    memset(&servaddr2, 0, sizeof(servaddr2));

    servaddr2.sin_family = AF_INET;
    servaddr2.sin_port = htons(sendto_port_number);
    servaddr2.sin_addr.s_addr = INADDR_ANY;


    pthread_t sendto_thread;
    pthread_t recvfrom_thread;

    struct arg_struct s;
    s.sendtofd = sockfd2;
    s.recvfromfd=sockfd;
    s.sendtoservaddr2 = servaddr2;
    s.recvfromservaddr2 = servaddr;
    s.id=ID;

    struct token_message m;
    m.id="id";
    m.message="Olcia";

    s.me=m;



    if(token==1){
        if(pthread_create(&sendto_thread, NULL, sendto_thread_function,(void *)&s)) {

            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }

        if(pthread_create(&recvfrom_thread, NULL, recvfrom_thread_function, (void *)&s)) {

            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }

        if(pthread_join(sendto_thread, NULL)) {

            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);
        }

        if(pthread_join(recvfrom_thread, NULL)) {

            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);

        }
    }

    sleep(10);


    if(token==0){
        if(pthread_create(&recvfrom_thread, NULL, recvfrom_thread_function, (void *)&s)) {

            fprintf(stderr, "Error creating thread\n");
            exit(EXIT_FAILURE);
        }

        if(pthread_join(recvfrom_thread, NULL)) {

            fprintf(stderr, "Error joining thread\n");
            exit(EXIT_FAILURE);

        }
    }
}


int connfd;

void *connect_thread_function(void *arguments)
{

    int len;//, connfd;
    struct arg_struct *args = arguments;


    int recvfomsockfd=args -> fd;
    struct sockaddr_in	 cli = args -> recvfromservaddr2;
    sleep(5);

    // Now server is ready to listen and verification
    if ((listen(sockfd, 5)) != 0) {
        printf("Listen failed...\n");
        exit(0);
    }
    else
        printf("Server listening..\n");
    len = sizeof(cli);

    // Accept the data packet from client and verification
    connfd = accept(sockfd, (SA*)&cli, &len);
    if (connfd < 0) {
        printf("server acccept failed...\n");
        exit(0);
    }
    else
        printf("server acccept the client...\n");



    return NULL;
}

void *read_thread_function(void *arguments)
{

    printf("Jestem w wątkowej funkcji recvfrom_thread_function\n");

    struct arg_struct *args = arguments;
    //printf("%d\n", args -> fd);

    int sockfd=args -> recvfromfd;
    //struct sockaddr_in	 cli = args -> servaddrARG;
    sleep(5);

//fcntl(sockfd,F_SETFL,O_NONBLOCK);
    char buff[MAX];
    int n;

    //bzero(buff, MAX);
    sleep(5);
    while(1){
        // read the message from client and copy it in buffer
        int a = read(connfd, buff, sizeof(buff));
        printf("Przeczytano: %i\n", a);
        // print buffer which contains the client contents
        printf("Wiadomość: %s\n ", buff);
        bzero(buff, MAX);
        n = 0;
        sleep(5);
    }

    // the function must return something - NULL will do
    return NULL;

//}
}


void *write_thread_function(void *arguments)
{
    printf("Jestem w wątkowej funkcji sendto_thread_function\n");


    struct arg_struct *args = arguments;
    //printf("%d\n", args -> fd);

    int sockfd2=args -> fd;
    //struct sockaddr_in	 servaddr2 = args -> servaddrARG;

    sleep(2);
    //fcntl(sockfd2,F_SETFL,O_NONBLOCK);

    char buff[MAX];
    for(int i=0; i<10;i++)
        buff[i]=i;

    int n;

    while(1){
        //bzero(buff, sizeof(buff));
        //buff[0]="P";
        int a = write(sockfd2, buff, sizeof(buff));
        bzero(buff, sizeof(buff));
        printf("Wyslano: %i\n" , a);
        //read(sockfd2, buff, sizeof(buff));
        //printf("From Server : %s", buff);
        if ((strncmp(buff, "exit", 4)) == 0) {
            printf("Client Exit...\n");
        }

        sleep(5);
    }

    // the function must return something - NULL will do
    return NULL;

}


void handle_tcp(int read_port_number, int write_port_number, int token){
// SOCKFD dla read
    int sockfd, connfd, len;
    struct sockaddr_in servaddr, cli;


    // socket create and verification
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd == -1) {
        printf("socket creation failed...\n");
        exit(0);
    }
    else
        printf("Socket successfully created..\n");
    bzero(&servaddr, sizeof(servaddr));

    // assign IP, PORT
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(read_port_number);

    // Binding newly created socket to given IP and verification
    if ((bind(sockfd, (SA*)&servaddr, sizeof(servaddr))) != 0) {
        printf("socket bind failed...\n");
        exit(0);
    }
    else
        printf("Socket successfully binded..\n");


    pthread_t connect_thread;

    struct arg_struct connect_args;
    connect_args.recvfrom = sockfd;
    connect_args.recvfromservaddr2 = cli;

    if(pthread_create(&connect_thread, NULL, connect_thread_function, (void *)&connect_args)) {

        fprintf(stderr, "Error creating thread\n");
        exit(EXIT_FAILURE);
    }


    sleep(10);

// SOCKFD2 dla write

    int sockfd2, connfd2;
    struct sockaddr_in servaddr2, cli2;

    // socket create and varification
    sockfd2 = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd2 == -1) {
        printf("socket creation failed...\n");
        exit(0);
    }
    else
        printf("Socket successfully created..\n");
    bzero(&servaddr2, sizeof(servaddr2));

    // assign IP, PORT
    servaddr2.sin_family = AF_INET;
    servaddr2.sin_addr.s_addr = inet_addr("127.0.0.1");
    servaddr2.sin_port = htons(write_port_number);

    // connect the client socket to server socket
    if (connect(sockfd2, (SA*)&servaddr2, sizeof(servaddr2)) != 0) {
        printf("connection with the server failed...\n");
        exit(0);
    }
    else
        printf("connected to the server..\n");




    pthread_t write_thread;
    pthread_t read_thread;


    struct arg_struct s;
    s.recvfromfd = sockfd;
    s.sendtofd=sockfd2;
    s.sendtoservaddr2=servaddr2;
    s.recvfromservaddr2=cli;
    s.id=id;

    //recvfrom_args.servaddrARG = cli;

    if(pthread_create(&read_thread, NULL, read_thread_function, (void *)&s)) {

        fprintf(stderr, "Error creating thread\n");
        exit(EXIT_FAILURE);
    }



    if(pthread_create(&write_thread, NULL, write_thread_function,(void *)&s)) {

        fprintf(stderr, "Error creating thread\n");
        exit(EXIT_FAILURE);
    }

    if(pthread_join(read_thread, NULL)) {

        fprintf(stderr, "Error joining thread\n");
        exit(EXIT_FAILURE);

    }


    // wait for the second thread to finish
    if(pthread_join(write_thread, NULL)) {

        fprintf(stderr, "Error joining thread\n");
        exit(EXIT_FAILURE);
    }

    sleep(2);

}


int main(int argc, char *argv[] ) {

    // odczytanie argumentów przekazanych przy wywołaniu programu
    char *ID=argv[1];
    int recv_port_number = atoi(argv[2]);
    char* IPaddres = argv[3];
    int send_port_number = atoi(argv[4]);
    int token = atoi(argv[5]);  // 1 - posiada, 0 - nie posiada
    int protocol =  atoi(argv[6]); // 0 - tcp, 1 - udp
    printf("ID: %s\n", ID);
    printf("recvfrom_port_number : %i\n", recv_port_number);
    printf("IPaddres: %s\n", IPaddres);
    printf("sendto_port_number: %i\n", send_port_number);
    printf("token: %i\n", token);
    printf("protocol: %i\n", protocol);
    printf("\n");

    if(protocol==1){
        handle_tcp(recv_port_number, send_port_number, token);
    }
    else if (protocol==0){
        handle_udp(recv_port_number, send_port_number, ID, token);
        // jesli przy wywolaniu posiada token tzn. zaczyna nadawać
    }else{
        printf("Wybrano nieodpowiedni protokół!");
        exit(EXIT_FAILURE);
    }


    return 0;
}
