# jeeconf2017

http://jeeconf.com/ 2017

Sample code accompanying the talk 

## Instructions
Do the usual Maven compile and build. As the example uses Spring Boot, you should run Maven at least as far
as the _package_ phase.

### Start some servers
Start at least one server process using,

```
java -jar server/target/server.jar
```

Two or more will enable Jet to spread the workload, but you'll need to look at the logs coming from
each to infer how the workload is being split equally across the JVMs.

The example in `hazelcast.xml` uses localhost discovery, on 127.0.0.1. If you run on multiple hosts,
this is better as each machine can be maxed, but you'll need to update the list of hosts with whatever
IP addresses you have. Make the same change to `hazelcast-client.xml` so the launcher client can
find the servers to run the Jet job.

If you start two, you should expect to see a message like this from each showing it has found and
connected to the other.

```
Members [2] {
	Member [127.0.0.1]:5701 - f1c3d9f5-cf58-43db-a1ad-9ebd204c1978
	Member [127.0.0.1]:5702 - 31f8fc60-3995-4cb6-a32e-e5fc4afd14c0 this
}
```

### Start a client
Start a client process using

```
java -jar client/target/client.jar
```

If this starts correctly, the client should output which server(s) it has found in its logs.
Then navigate to [http://localhost:8080](http://localhost:8080) to use the amazing web
interface.

### Load some Exchange Rate info

On the client GUI, select the *Load Currencies* page then hit the *Load* button.

This will connect to [http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml](The European Central Bank) to load the last 90 days of exchange rates for a selection of 31 currency prices compared to the Euro.
The XML is parsed, and stored in a `com.hazelcast.core.IMap` named "_HistoricCurrency_"

What this URL gives us is the end of day, or closing, price for each currency over 90 calendar days. As
some days are weekends and holidays, typically the last 90 calendar days contains about 65 days of prices.

Once the currency history is loaded, you can use the *List Currencies* page to see them all, and to select any to see the detail. 


### Run the Jet analytics

On the client GUI, select the *Calculate Averages* page in the *Averages* section, then hit the *Run* button.

This will run the Jet DAG `neil.demo.jeeconf2017.jet.MaDAG` (a DAG for *M*oving *A*verages).

This DAG reads from the `com.hazelcast.core.IMap` holding the stored historical currency information loaded in the previous step. The simple moving averages are written to a `com.hazelcast.core.IMap` named "_sma_" and the exponential moving average are written to another `com.hazelcast.core.IMap` named "_ema_".

The run time is returned to the screen, as this DAG is one that runs, produces results, and ends quickly. Other DAGs can run forever or for hours, so measuring the elapsed time isn't a generally applicable pattern.

#### `neil.demo.jeeconf2017.jet.MaDAG`

This is a pictorial view of the **M**oving **Averages** **DAG** (_MaDAG_), viewed from top to bottom.

```
                             /========================\
                             | Historic Currency IMap |
                             \========================/
                                         |
                               (from, to, date, price)
                                         |
                               +--------------------+          
                               | Last 'n' Processor | 
                               +--------------------+          
                                         |
                                    (to, price[])                                        
                                         |
                   +---------------------+---------------------+
                   |                                           |
          +----------------+                           +----------------+
          | SMA Calculator |                           | EMA Calculator |
          +----------------+                           +----------------+
                   |                                           |
              (to, price)                                 (to, price)
                   |                                           |
           /================\                          /================\
           | SMA Price IMap |                          | EMA Price IMap |
           \================/                          \================/
```

The starting point here is an `com.hazelcast.core.IMap`, so saved input rather than a genuine stream.
Can't rely on an internet connection during the talk!

The `com.hazelcast.core.IMap` is essentially unordered, so connect this as input to
`neil.demo.jeeconf2017.jet.LastNProcessor` which will accumulate and then output the
last 10 prices for each currency (Euro v US Dollar, Euro v Canadian Dollar, etc etc).

These last 10 prices per currency are fed into both the `neil.demo.jeeconf2017.jet.SmaProcessor` and
`neil.demo.jeeconf2017.jet.EmaProcessor`. These take the last 10 prices, one calculates the simple
moving average and one does the exponential moving average.

Finally, each of these processing vertices sends its output to another `com.hazelcast.core.IMap`
for storage.

#### Logging

In this example, the last stages of the DAG produce some logging.

The DAG runs on every server in your Jet cluster, so you should check the server logs for each.

The `neil.demo.jeeconf2017.jet.EmaProcessor` will log at it's completion stage. The completion stage is when it is done calculating and is sending the results through to an `com.hazelcast.core.IMap` for storage.

For instance, running two server instances on a machine with two processors could give output like this from one of the servers:

```
09:20:15.295 [hz._hzInstance_1_dev.jet.cooperative.thread-2] INFO  neil.demo.jeeconf2017.jet.EmaProcessor - complete -> [THB, USD, MXN]
09:20:15.296 [hz._hzInstance_1_dev.jet.cooperative.thread-0] INFO  neil.demo.jeeconf2017.jet.EmaProcessor - complete -> [TRY, HUF, NZD]
09:20:15.300 [hz._hzInstance_1_dev.jet.cooperative.thread-3] INFO  neil.demo.jeeconf2017.jet.EmaProcessor - complete -> [PHP, JPY]
09:20:15.296 [hz._hzInstance_1_dev.jet.cooperative.thread-1] INFO  neil.demo.jeeconf2017.jet.EmaProcessor - complete -> [DKK, HRK, CAD]
```

What this means is there are four instances of the `neil.demo.jeeconf2017.jet.EmaProcessor` running in a thread each in this server JVM, and there will be another four instances in the other server JVM on this machine. Each of the eight instances looks after a subset of the available currencies.

This is how Jet scales. By default Jet will configure a number of `neil.demo.jeeconf2017.jet.EmaProcessor` instances per server JVM based on the number of processors (`java.lang.Runtime.getRuntime().availableProcessors()`) and spread the currency processing work across these. If you want higher throughput you just need more instances... so more hosts or more processors.

(The `neil.demo.jeeconf2017.jet.SmaProcessor` does the same thing its logging.)

### Inspect the results

On the client GUI, use the *Exponential Moving Averages* page from the front menu to see the calculated
exponential moving averages. And obviously the *Simple Moving Averages* page for the calculated simple
moving averages.

## Todo
The European Central Bank doesn't provide the *UAH* currency. If you can find it somewhere, feel free to add it.

## Background
The Hazelcast Jet website, [http://jet.hazelcast.org/](http://jet.hazelcast.org/)

The Hazelcast IMD website, [http://imdg.hazelcast.org/](http://imdg.hazelcast.org/)