# Distributed Computing Assignment 4
by Duncan and Jenny

### Screencast demo uploaded [here](https://www.dropbox.com/s/zxq7an12m2cp1ej/mapreduce_demo.mov?dl=0)

### Description:

Our code allows for any number of worker machines. Each worker exports a single instance of a Mapper and a Reducer to accessible rmi registries (dubbed “managers” as they perform no processing, but only spawn new map/reduce processes locally). The master obtains remote stubs to these manages by looking up hard-coded ip’s for it’s workers (this can be run locally by including only one ip: 127.0.0.1). These stubs are stored in circular queues (once one is accessed, it’s added to the back of the queue) for even load distribution between workers. Even with this however, we have run into issues with too many exported objects (reducers are 1 per unique word, and are likely the culprit), at which point the program fails rather ungracefully.

Mapper tasks utilize the deprecated interface method iMaster.markMapperDone(sender) so that the master can be sure that all lines have been mapped and all words sent to reducers. The master then invokes iReducer.terminate() on all reducers, which provide their counts through iMaster.receiveOutput(key, count).


### To run our code: 

- Make sure all machines are running java 1.8. If not: run the following, being positive when prompted:

```
$ sudo yum install java-1.8.0
$ sudo yum remove java-1.7.0-openjdk   # (machine’s current java version)
$ sudo yum install java-devel
```

- Edit the workerIPs variable in Master.main() (ln 236) to match the ip’s of the desired worker machines (again, can be run purely locally with 127.0.0.1). 

- Also edit the filepath in the wordCountFile(filepath) method call on 252.
Distribute all .java files in the src/ directory to all machines, and compile

- Transfer target file to the master machine in the same directory as the class files. Make sure the file name matches that specified previously.

- On the worker machines start the rmiregistry and the managers with:

```
$ rmiregistry 1099 &
$ java MapReduceManagers
```

- On the master machine start the word count with: 

```
$ java Master
```
- Once the process is complete, a new directory should be created data/ on the master machine with the results inside.
