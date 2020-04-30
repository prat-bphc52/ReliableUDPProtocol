# ReliableUDP Protocol
Computer Networks CS F303 Assignment 3
mysock.java contains the implementation of a reliable protocol that uses UDP sockets to transfer data over the network
The protocol ensures that there is no packet loss and also takes care of packet corruption.

The two java files - cclient and cserver, can be used to test the protocol by transferring one file from client to the server

## Requirements
- Java (Java 8 recommended)

## Installation
- Clone this repository in your preferred directory

```
git clone https://github.com/prat-bphc52/ReliableUDPProtocol
```
- Or you can also download the source code as a zip file

## Execution
Compile the java code using the below command

```
javac cserver.java cclient.java mysock.java
```

Start the server and pass the following arguments **hostIP** **hostPort** **outputFileName**. For ex:
```
java CServer 127.0.0.1 6000 output.txt
```

Start the client and pass the following arguments **hostIP** **hostPort** **destinationIP** **destinationPort** **outputFileName**. For ex:
```
java CClient 127.0.0.1 6001 127.0.0.1 6000 input.txt
```

## Team Members
- [Prateek Agarwal](https://github.com/prat-bphc52/ "Prateek Agarwal") 2017A7PS0075H
- Rashi Jain - 2017A7PS0082H
- Shreeya Bharat Nelekar 2017A7PS0193H
- Ankit Bansal - 2017A7PS0159H
- [Utkarsh Grover](https://github.com/utkgrover/ "Utkarsh Grover") - 2017A7PS1428H
