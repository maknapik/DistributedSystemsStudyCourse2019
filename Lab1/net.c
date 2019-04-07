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

/* Defines */
#define PORT 57015
#define FRAMESIZE 88
#define MSGSIZE 80
#define SYN 0x16
#define DLE 0x10
#define STX 0x02
#define ETX 0x03

#define ReportError(msg)       {perror(msg); exit(-1);}

/* Globals */
char my_addr;           // This machine's node number (lower case ASCII char)
char *next_node_addr;   // Hardware address of next node in token ring
// This can be either the ip address or host name
int sd_in;              // Incoming socket descriptor used by the server thread
int sd_out;             // Outgoing socket descriptor used by the client thread
int client_connected;   // Indicates whether or not the client has connected
char dest;              // Destination node number
char *outgoing_frame;   // Frame to be sent out
int send_message;       // Specifies if there is a message to be sent


int main(int argc, char **argv)
{
    sd_in = -1;
    sd_out = -1;
    client_connected = 0;
    send_message = 0;

    /* Check for valid user input */
    if((argc > 1) && (argv[1][0] == '-') && (argv[1][1] == 'h')) {
        exit(1);
    }
    if(argc != 3) {
        ReportError("Invalid input parameters. Run with -h option for help.\n");
    }
    my_addr = argv[1][0];
    next_node_addr = argv[2];
    if(( argv[1][1] != '\0') ||
       (((int)my_addr > 122) || ((int)my_addr < 97))) {
        ReportError("Invalid node number!\n   -Node address must be a lower"
                    " case ACII character: a,b,c,d,e,...)\n");
    }

    SpawnThreads();

    signal(SIGTSTP, GenerateNewToken);
    signal(SIGINT, CloseSockets);
    signal(SIGPIPE, CloseSockets);

    StartNetwork();

    return 0;
}

/*
 * The following code will put the standard input and the socket descriptor
 * in the select function to be inspected for reading.  When the select
 * function returns, the descriptors are inspected to find out which one
 * is ready.   This code can be modified to handle files and sockets which
 * become ready for writing.
 *
 * Assume sd is the data channel socket & STDIN_FILENO is the
 * standard input file descriptor (it is 0)
 */
void StartNetwork()
{
    int n;
    fd_set rset;        /* declare an fd_set for read descriptors */

    for (;;) {  /* endless loop, if you want continuous operation */
        FD_ZERO(&rset);     /* clear all bits in rset */
        FD_SET(STDIN_FILENO, &rset);    /* set the standard input bit */
        FD_SET(sd_in, &rset);  /* set the socket descriptor bit */

        n = select((sd_in>STDIN_FILENO? sd_in:STDIN_FILENO)+1, &rset, NULL, NULL, NULL);
        /* select blocks, and n is the number of ready descriptors */
        if ( (n == -1 ) && (errno == EINTR) ) /* interruption */
            continue;
        if (n== -1) {  /* error: you may handle it, if you want */
            /* code to handle errors */
            ReportError("Interruption error!\n");
        }
        /* after this point, handle the ready descriptor(s) */

        /* check for ready data from the keyboard */

        if (FD_ISSET(STDIN_FILENO, &rset)) {
            /* read data from the standard input*/
            GatherUserInput();
            n--;
        }

        /* check for ready data from the communication channel */
        if ((n > 0 ) && (FD_ISSET(sd_in, &rset)) ) {
            /* socket is ready for reading */
            /* read data from socket */
            ReceiveMessage();
        }
    }
}

/*
 * This function is called when there is data to be read by the server. This
 * function is in charge of interpreting the data frame received; determining
 * if the message is destined for this machine and displaying the message to
 * the screen if so. Or if the frame is not destined for me this function
 * forwards the message to the next node in the ring.
 */
void ReceiveMessage()
{
    char buff[FRAMESIZE];
    if((read(sd_in, buff, FRAMESIZE)) < 0 ) {
        ReportError("Failed to receive message!\n");
    }

    /* Check if the frame is destined for me */
    if(buff[4] == my_addr && !(buff[6] == DLE && buff[7] == ETX)) {
        int i = 6;
        char *message = malloc(MSGSIZE);
        while(buff[i] != DLE) {
            message[i-6] = buff[i];
            i++;
        }
        printf("Message Received!\n   -Message from (%c): %s\n", buff[5], message);
        GenerateNewToken();
        bzero(&message, sizeof(message));
        free(message);
        return;
    }
    /* Check if I have a message to send and if this is a token message */
    if(send_message && (buff[6] == DLE && buff[7] == ETX)) {
        if((write(sd_out, outgoing_frame, strlen(outgoing_frame))) < 0) {
            ReportError("Failed to send message!\n");
        }
        send_message = 0;
        bzero(&outgoing_frame, sizeof(outgoing_frame));
        free(outgoing_frame);
        return;
    }

    /* Default case is to pass the frame along if it is not for me and if I
       currently do not have a message to send */
    write(sd_out, buff, strlen(buff));
}

/*
 * This function generates a new token when a message is consumed by the
 * ReceiveMessage function or when the user presses 'CTRL-Z'
 */
void GenerateNewToken()
{
    printf("New token generated.\n");
    char *f = CreateFrame(my_addr, "");
    write(sd_out, f, strlen(f));
}

/*
 * This function is called whenever the user begins to type. It is used to
 * gather the user's input from stdin.
 */
void GatherUserInput()
{
    /* Perform minor error checking on input */
    while(1) {
        int valid = 0;
        dest = fgetc(stdin);

        char temp = fgetc(stdin);
        if (temp != '\n') {
            printf("INVALID: Destination must be a single lower case ASCII"
                   " character!\n   -Re-enter Destination: ");
            char temp;
            /* Clear stdin if the user entered a message that was longer than
               80 characters */
            while( (temp = fgetc( stdin )) && (temp != EOF && temp != '\n') ) {}
        }
        else {
            break;
        }

    }

    printf("Enter message to send to [%c]: ", dest);

    char msg[MSGSIZE];
    fgets(msg, sizeof(msg), stdin);
    /* Check if the user entered a string longer than 80 characters */
    int msg_size = strlen(msg);
    if(msg[msg_size - 1] != '\n') {
        /* Insert newline at end of string and flush stdin buffer */
        msg[msg_size - 1] = '\n';
        int c;
        while( (c = fgetc( stdin )) && c != EOF && c != '\n' ) {}
    }
    outgoing_frame = CreateFrame( dest, msg);
    send_message = 1;
}

/*
 * This function will create a frame that will be sent when this node receives
 * the token. We pass in the destinatioin of the message and the message itself
 */
char *CreateFrame(char d, char *msg)
{
    char *frame = malloc(FRAMESIZE);

    frame[0] = SYN;
    frame[1] = SYN;
    frame[2] = DLE;
    frame[3] = STX;
    frame[4] = d;
    frame[5] = my_addr;

    int x;
    for(x = 6; x < strlen(msg) + 6; x++ ) {
        frame[x] = msg[x - 6];
    }

    frame[x] = DLE;
    frame[x + 1] = ETX;
    return frame;
}

/*
 * This function spawns two threads that are used to establish a connection to
 * the ring. Since the server contains the 'accept' function which blocks until
 * it receives a connection from a client it would be ideal to place the server
 * in its own thread so the program does not block. The client part of the
 * program I have working in the same manner, where it will continue to loop
 * trying to establish a connection with a server that is running on a machine
 * specified by the user upon program startup.
 */
void SpawnThreads()
{
    pthread_t client_thread, server_thread;

    pthread_create( &server_thread, NULL, Server, NULL );
    pthread_create( &client_thread, NULL, Client, NULL );

    pthread_join( server_thread, NULL );
    pthread_join( client_thread, NULL );
}

/*
 * This routine establishes a passive open connection.  That is, it creates
 * a socket, and passively wait for a connection.  Once a connection request
 * has been received, it echoes "connected" on the screen, and return
 * a file descriptor to be used to communicate with the remote machine.
 * Make sure that you change the machine name from "vulcan" to that you
 * will be running your process on. Also, change the port number to
 * a suitable port number as indicated in the project writeup.
 */
void Server()
{
    struct sockaddr_in myaddr, otheraddr;
    struct hostent *myname;

    int sd, otherlength;
    int hostnamelength;

    if ((sd = socket(AF_INET, SOCK_STREAM, 0)) < 0 ) {
        ReportError("Server socket establishment failed!\n");
    }

    bzero(&myaddr, sizeof(myaddr));
    myaddr.sin_family  = AF_INET;
    myaddr.sin_port = htons(PORT);
    myaddr.sin_addr.s_addr = htonl(INADDR_ANY);

    if((bind(sd, (struct sockaddr *)&myaddr, sizeof(myaddr))) < 0) {
        ReportError("Server bind failed!\n"
                    "   -Is another socket already listening on that port?\n");
    }

    if((listen(sd, 5)) < 0) {
        ReportError("Server listen failed!\n")
    }
    otherlength = sizeof(otheraddr);

    printf("Server waiting for connection...\n");
    sd_in = accept(sd, (struct sockaddr *)&otheraddr, &otherlength);

    printf("Server connected!\n");
    if(client_connected == 0){
        fflush(stdout);
        printf("Client still attempting to connect...");
    }
}

/*
 * This routine establishes an active open connection.  That is, it creates
 * a socket, and connects using it to a remote machine. The routine returns
 * a file descriptor to be used to communicate with the remote machine.
 * Make sure that you change the machine name from "vulcan" to that of
 * the remote machine.  Also, change the port number to a suitable port
 * number as indicated in the project writeup. This function will block until
 * it successfully establishes a connection with the specified server.
 */
void Client()
{
    int n, sd;
    u_long ip = inet_addr(next_node_addr);
    struct hostent *otherhost;
    struct sockaddr_in otheraddr;

    bzero(&otheraddr, sizeof(otheraddr));
    otheraddr.sin_family = AF_INET;
    otheraddr.sin_port = htons(PORT);

    if((sd_out = socket(AF_INET, SOCK_STREAM, 0)) < 0 ) {
        ReportError("Client socket establishment failed!\n");
    }

    /* Verify that the next node's IP address or host name exists on network */
    otherhost = gethostbyaddr((const char *) &ip, sizeof(ip), AF_INET);
    if(otherhost == NULL) {
        /* Try to resolve the next node's name if possible */
        if((otherhost = gethostbyname(next_node_addr)) == NULL) {
            ReportError("Cannot resolve next node's ip address or hostname!\n"
                        "   -Verify input and try again.\n");
        }
    }

    bcopy(otherhost->h_addr, &otheraddr.sin_addr, otherhost->h_length);

    printf("Client attempting to connect...");
    /* Block until we successfully establish a connection with the next node */
    while(1) {
        fflush(stdout);
        printf(".");
        sd = connect(sd_out, (struct sockaddr *)&otheraddr, sizeof(otheraddr));
        if(sd >= 0) {
            printf("Client connected!\n");
            client_connected = 1;
            if(sd_in <= 0) {
                fflush(stdout);
                printf("Server still attempting to connect...");
                while(sd_in <= 0) {
                    sleep(1);
                    fflush(stdout);
                    printf(".");
                }
            }
            break;
        }
        sleep(1);
    }
}

/*
 * Closes the socket connections for the client and server.
 */
void CloseSockets()
{
    close(sd_in);
    close(sd_out);
    printf("Shutting down...\n");
    exit(1);
}
