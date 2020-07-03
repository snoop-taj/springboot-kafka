## Pre-Requisites to run this example locally

- install docker-compose [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/)
- run on your local machine or add it to your `bash_profile` file ```export HOST_MACHINE_IP=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | awk '{ print $2 }')
``` to modify the ```KAFKA_ADVERTISED_LISTENERS``` in ```docker-compose.yml``` to match your docker host IP (Note: Do not use localhost or 127.0.0.1 as the host ip if you want to run multiple brokers.)
- if you want to customize any Kafka parameters, simply add them as environment variables in ```docker-compose.yml```, e.g. in order to increase the ```message.max.bytes``` parameter set the environment to ```KAFKA_MESSAGE_MAX_BYTES: 2000000```. To turn off automatic topic creation set ```KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'false'```

## Usage this example locally

### Single Broker

Start a cluster:

- ```make start```

Destroy a cluster:

- ```make stop```

SSH a container:
if you dont add a service by default its kafka
- ```make ssh SERVICE=zookeeper```

### Multiple Broker

Start a cluster:

- ```make start-multiple-broker```

Destroy a cluster:

- ```make stop-multiple-broker```

SSH a container:
if you dont add a service by default its kafka
- ```make ssh-multiple SERVICE=zookeeper```
- ```make ssh-multiple SERVICE=springboot```



## Broker IDs

You can configure the broker id in different ways

explicitly, using ```KAFKA_BROKER_ID``` by default its set to ``-1``

If you don't specify a broker id in your docker-compose file, it will automatically be generated (see [https://issues.apache.org/jira/browse/KAFKA-1070](https://issues.apache.org/jira/browse/KAFKA-1070). This allows scaling up and down. In this case it is recommended to use the ```--no-recreate``` option of docker-compose to ensure that containers are not re-created and thus keep their names and ids.


## Automatically create topics

If you want to have kafka-docker automatically create topics in Kafka during
creation, a ```KAFKA_CREATE_TOPICS``` environment variable can be
added in ```docker-compose.yml```.

Here is an example snippet from ```docker-compose.yml```:

        environment:
          KAFKA_CREATE_TOPICS: "Topic1:1:3,Topic2:1:1:compact"

```Topic 1``` will have 1 partition and 3 replicas, ```Topic 2``` will have 1 partition, 1 replica and a `cleanup.policy` set to `compact`.

## Kafka advertised listener

You can configure the advertised listener in different ways

### For single Broker
1. explicitly, using ```KAFKA_ADVERTISED_LISTENERS```
2. via a command, using ``export HOST_MACHINE_IP=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | awk '{ print $2 }')``

### For multiple Broker
you will need to specify several kafka server properties to run on multiple broker. e.g running two brokers
    
      KAFKA_LISTENERS: LISTENER_1://:9092,LISTENER_2://:9093
      KAFKA_ADVERTISED_LISTENERS: LISTENER_1://:9092,LISTENER_2://${HOST_MACHINE_IP}:9093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: LISTENER_1:PLAINTEXT,LISTENER_2:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: LISTENER_1

`KAFKA_LISTENERS(listeners)` is a comma-separated list of listeners and the host/IP and port to which Kafka binds to for listening. For more complex networking, this might be an IP address associated with a given network interface on a machine. The default is 0.0.0.0, which means listening on all interfaces.


`KAFKA_ADVERTISED_LISTENERS(advertised.listeners)` is a comma-separated list of listeners with their host/IP and port. This is the metadata that’s passed back to clients.

`KAFKA_LISTENER_SECURITY_PROTOCOL_MAP(listener.security.protocol.map)` defines key/value pairs for the security protocol to use per listener name.

Kafka brokers communicate between themselves, usually on the internal network (e.g., Docker network, AWS VPC, etc.). To define which listener to use, specify 

`KAFKA_INTER_BROKER_LISTENER_NAME`(inter.broker.listener.name). The host/IP used must be accessible from the broker machine to others.

Kafka clients may well not be local to the broker’s network, and this is where the additional listeners come in.

## Running Test

#### Single Broker
- Before running the test Run the main springboot application from you IDE `SpringKafkaApplication` to subscribe the application with Kafka 
- You can run integration tests from the IDE as Normal after starting the main application and stopping it.

#### Multiple Broker
- Once the containers start running zookeeper, kafka and springboot. You can ssh in springboot container `make ssh-multiple SERVICE=springboot` and run all tests by running `mvn test`

## List of Commands

#### List Brokers
``broker-list.sh``

#### Create Topic
``kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 3 --topic "Topic1" --config cleanup.policy="compact"``

#### List Topics
``kafka-topics.sh --bootstrap-server :9092 --list``

#### Describe Topic Group
``kafka-consumer-groups.sh --bootstrap-server :9092 --describe --group string-group``

#### Console Consume
``kafka-console-consumer.sh --bootstrap-server :9092 --topic string-topic-target --from-beginning``

#### Running Single Test
``mvn -Dtest=StringProducerConsumerTest#whenMultipleStringTopicProducedItConsumes test``