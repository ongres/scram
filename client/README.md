# SCRAM Client API

[![Javadocs](http://javadoc.io/badge/com.ongres.scram/client.svg?label=client)](http://javadoc.io/doc/com.ongres.scram/client)

For general description, please refer to [the main README.md](https://gitlab.com/ongresinc/scram).


## How to use the client API

1. Add Maven (or equivalent) dependencies :
```xml
<dependency>
    <groupId>com.ongres.scram</groupId>
    <artifactId>client</artifactId>
    <version>VERSION</version>
</dependency>
```

2. Get a ```ScramClient```. A ```ScramClient``` can be configured with several parameters,
 and matches a given server. From the client, several ```ScramSession```s can be created,
 for potentially different users. Under certain conditions (read Javadoc) it is thread safe.

 A simple example could be:
```java
ScramClient scramClient = ScramClient
    .channelBinding(ChannelBinding.NO)
    .stringPreparation(NO_PREPARATION)
    .serverMechanisms("SCRAM-SHA-1")
    .setup();
```

 More configuration methods and options are available, as shown below:
```java
ScramClient scramClient = ScramClient
    .channelBinding(ChannelBinding.YES)
    .stringPreparation(NO_PREPARATION)
    .serverMechanisms("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS")
    .nonceSupplier(() -> generateNonce())
    .secureRandomAlgorithmProvider("algorithm", "provider")
    .setup();
```
 
3. For each authentication round and/or user, create a ```ScramSession``` and get the client-first-message:
```java
ScramSession scramSession = scramClient.scramSession("user");
scramSession.clientFirstMessage()
```

4. Receive the server-first-message:
```java
ScramSession.ServerFirstProcessor serverFirst = scramSession.receiveServerFirstMessage(message);
// Read the salt and iterations:
serverFirst.getSalt()
serverFirst.getIteration()
```

5. Generate the client-last-message:
```java
ScramSession.ClientFinalProcessor clientFinal = serverFirst.finalMessagesHandler("password");
clientFinal.clientFinalMessage()
```

6. Receive the server-last-message, check if is valid or error, etc:
```java
ScramSession.ServerFinalProcessor serverFinal = clientFinal.receiveServerFinalMessage(message);
// Methods to check if it is error, get error, verify signature
serverFinal.isError()
serverFinal.getErrorMessage()
serverFinal.verifyServerSignature()
```

## Example

The following example runs a full SCRAM session, using the RFC's data as an example.

```java
ScramClient scramClient = ScramClient
        .channelBinding(ScramClient.ChannelBinding.NO)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1")
        .nonceSupplier(() -> "fyko+d2lbbFgONRv9qkxdawL")
        .setup();

ScramSession scramSession = scramClient.scramSession("user");
System.out.println(scramSession.clientFirstMessage());

ScramSession.ServerFirstProcessor serverFirstProcessor = scramSession.receiveServerFirstMessage(
        "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j,s=QSXCR+Q6sek8bf92,i=4096"
);
System.out.println("Salt: " + serverFirstProcessor.getSalt() + ", i: " + serverFirstProcessor.getIteration());

ScramSession.ClientFinalProcessor clientFinalProcessor
        = serverFirstProcessor.clientFinalProcessor("pencil");
System.out.println(clientFinalProcessor.clientFinalMessage());

ScramSession.ServerFinalProcessor serverFinalProcessor
        = clientFinalProcessor.receiveServerFinalMessage("v=rmF9pqV8S7suAoZWja4dJRkFsKQ=");
if(serverFinalProcessor.isError()) {
        // throw authentication exception
}

System.out.println(serverFinalProcessor.verifyServerSignature());
```
