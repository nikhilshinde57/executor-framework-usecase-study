# The Executor Framework Use Case Implementation: Java, Spring Boot, SQS, S3, LocalStack, Java Application Performance Tuning(Xms, Xmx and GC Settings)
This project demonstrates how we can achieve more throughput using concurrency and parallelism in a multithreading framework. 
Java provides its own multi-threading framework called the **Executor Framework (EF)**. 
The project implements EF’s  E2E use case implementation. Along with that while working on highly scalable and 
available applications most of the time we come across situations where our application performance gets down. 
In such scenarios the very interesting point comes into the picture that is Application Performance Tuning. 
As a part of this use case implementation we also touch the **Java Application Performance Tuning** factors like **JVM Heap Size(Xms, Xmx)** 
and **Garbage Collector** configuration.

## About Executor Framework
With the increase in the number of the cores available in the processors nowadays, 
coupled with the ever increasing need to achieve more throughput, 
multi-threading APIs are getting quite popular. 
Java provides its own multi-threading framework called the Executor Framework.

The Executor Framework contains a bunch of components that are used to efficiently manage worker threads. 
The Executor API de-couples the execution of task from the actual task to be executed via Executors. 

### Types of Executors
* **SingleThreadExecutor** - This thread pool executor has only a single thread. 
It is used to execute tasks in a **sequential manner**. 
If the thread dies due to an exception while executing a task, 
a new thread is created to replace the old thread and the subsequent tasks are executed in the new one.
```
ExecutorService executorService = Executors.newSingleThreadExecutor()
```
* **FixedThreadPool(n)** - As the name indicates, it is a thread pool of a fixed number of threads. 
The tasks submitted to the executor are executed by the n threads and if there is more task they are stored on a **LinkedBlockingQueue**. 
This number is usually the total number of the threads supported by the underlying processor.
```
ExecutorService executorService = Executors.newFixedThreadPool(4);
```

* **CachedThreadPool** - This thread pool is mostly used where there are lots of **short-lived parallel tasks** to be executed. 
Unlike the fixed thread pool, the number of threads of this executor pool is not bounded. 
If all the threads are busy executing some tasks and a new task comes, the pool will create and add a new thread to the executor. 
As soon as one of the threads becomes free, it will take up the execution of the new tasks. 
If a thread remains idle for sixty seconds, they are terminated and removed from cache. However, if not managed correctly, or the tasks are not short-lived, the thread pool will have lots of live threads. This may lead to resource thrashing and hence performance drop.
```
ExecutorService executorService = Executors.newCachedThreadPool();
```

* **ScheduledExecutor** - This executor is used when we have a task that needs to be run at regular intervals or if we wish to delay a certain task.
The tasks can be scheduled in **ScheduledExecutor** using either of the two methods **scheduleAtFixedRate** or **scheduleWithFixedDelay**.

This project implements **FixedThreadPool(n)** executor type.

## About Use Case 

#### Functional Requirement
* Assume we have a folder directory present on S3 which contains the list of files
* Each file in the directory contains the text data such that 
each line having single word
* All words are ordered in a sorted order
* The task is to read all the files from the input S3 directory and 
prepare single output file from those input files provided that the data 
in output file should be sorted

#### Non-Functional Requirement
* The S3 input directory can have hundreds of files in it
* The minimum file size is like file with one word and maximum file size we are considering here is 200 MB
* As soon as file uploading finishes on S3, processing should get start
* The system should be scalable and highly available
* If anything fails report it as an event or alert
    
## Solution Approach  

### Event Driven:
As the application requirement is the input directory will contain hundreds of files with large data.
Taking that point into consideration S3 is the best storage place to store such large data files.
To make our system highly scalable, available and for real time processing event driven approach will be perfect fit for this.

When the files are uploaded on S3. The uploading system will publish the event on SQS.
The Spring boot application (Consumer Application) having the consumers running those will listen to that queue and once the message published
they will consume that message.
Once the message gets consumed the message processing service will start the processing of that message.
That message will contain the directory which we would like to process. The sample message will look like below.
```
{
"bucket Name":"niks",
"folderName":"input",
"folderPath":"niks/12345/input/",
"tenantId":"12345"
}
```
The consumer application validates and parse the message. After that it will pass that message to message processing service (MPS). 
It will count the number of files present in the directory and calculate the batch size based on thread pool size number. 
We are using **FixedThreadPool(n)** service multithreading pattern. Using that MPS will read multiple files in parallel from the S3 bucket.

The thread pool size is configurable and that can be configurable from environment local. Properties file.
Once all the batches completed their job, next task is to write those batch list into one file in a sorted way.
So here task is to merge K-sorted lists.
For merging K-sorted files minheap is the best DS. That having complexity of O (n * k * log k); Where n is number of array/list and k is number of elements in it.
After merging will get single list of words will upload that on S3 in output directory.
Considering the output file is very large so, that operation also we are doing parallelly by multiple thread.    

## Prerequisites
* **_JDK 8_** - Install JDK 1.8 version from [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* **_Maven_** - Download latest version of Maven from [here](https://maven.apache.org/download.cgi)
* **_Local Stack_** - Download and start local stack container [here](https://github.com/localstack/localstack) 
* **_IntelliJ IDEA Community Edition [Optional]_** - Install IntelliJ IDEA Community Edition from [here](https://www.jetbrains.com/idea/#chooseYourEdition)
* **_Docker_** Install Docker from [here](https://www.docker.com/products/docker-desktop)

## Steps To Run

Start LocalStack Container
```
docker pull localstack/localstack
docker run localstack/localstack
```
Note: Once the localstack container is up kindly check the port numbers for S3 and SQS service are correct or not and matching with below urls.
If not, then you need to change those to the one which your localstack container showing and update the environment-local. properties file
Once the local stack environment is ready run the below commands.

Create bucket on S3: 
```
aws --endpoint-url=http://localhost:4566 s3 mb s3://niks
```
Create SQS queue: 
```
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name niks-merge-files-event
```
Upload data on S3: 
```
aws --endpoint-url=http://localhost:4566 s3 cp ~/executor-framework-usecase-study/src/main/resources/input s3://niks/12345/input --recursive
```
Note: Here 12345/input is a folder path where we are uploading files. The number 12345 represent the client id.
Also, the sac/main/resources/input directory have some test files. So, you need to upload those using above cmd.
The root directory will change by machine to machine so change that accordingly.

Verify the files are uploaded or not.
```
aws --endpoint-url=http://localhost:4566 s3 ls s3://niks --recursive
```
####  Start Application
```
mvn clean install
mvn spring-boot:run
```

####  Test Application
Run all unit test cases. From project root directory run below file.

```$xslt
./test-runner.sh
``` 
To test application, publish the message on niks-merge-files-event queue.
```
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url http://localhost:4566/000000000000/niks-merge-files-event --message-body "{\"bucketName\":\"niks\",\"folderName\":\"input\",\"folderPath\":\"niks\/12345\/input\/\",\"tenantId\":\"12345\"}"
```
Make sure the details are correct and are same as we used while uploading files on S3.

After that kindly have a look on application logs where you will see message processing details.
The end application log will look like this
```$xslt
2020-09-24 14:40:50.664  INFO 74338 --- [lTaskExecutor-2] o.m.s.s3.S3OperationsHelperService       : tenantId: 12345, fileName: output2020-09-24_14:40:50.629.dat, bucketName: niks, s3FilePath: 12345/output/output2020-09-24_14:40:50.629.dat
2020-09-24 14:40:50.705  INFO 74338 --- [lTaskExecutor-2] o.m.s.s3.S3OperationsHelperService       : Initiated S3 multipart upload request
2020-09-24 14:40:50.832  INFO 74338 --- [lTaskExecutor-2] o.m.s.s3.S3OperationsHelperService       : Finished multipart S3 upload
2020-09-24 14:40:50.858  INFO 74338 --- [lTaskExecutor-2] o.m.s.sqs.MergeFilesSQSMessageProcessor  : Message successfully processed: {"bucketName":"niks","folderName":"input","folderPath":"niks\/12345\/input\/","tenantId":"12345"}
2020-09-24 14:40:50.890  INFO 74338 --- [lTaskExecutor-2] o.m.s.sqs.MergeFilesSQSMessageProcessor  : Message successfully deleted: {"bucketName":"niks","folderName":"input","folderPath":"niks\/12345\/input\/","tenantId":"12345"}
```
Now check on S3 the output file is uploaded or not. 
There will be a new folder with name output under the niks/12345/ path on S3 and that will contain the output file named as shown in above log
```$xslt
aws --endpoint-url=http://localhost:4566 s3 ls s3://niks --recursive
```
Download result file
```$xslt
aws --endpoint-url=http://localhost:4566 s3 cp s3://niks/12345/output/{output_file_name_as_per_application_log} out_put.dat
```

Note:
The configurable values like SQS queue url, region, S3 bucket name, S3 file reading thread pool size, S3 file upload thread pool size 
are such configurable properties you can manage by updating them in environment-local.properties file.

## Application Performance Tuning: Xms, Xmx and GC Settings

#### Tuning Xms and Xmx:
The application is reading the hundreds of files and keeping it in application memory for further processing.
To know the default JVM HeapSize configuration Xms(initial Java heap size) and Xmx(maximum Java heap size) run below command from terminal window: 
```
➜java -XX:+PrintFlagsFinal -version | grep HeapSize
    uintx ErgoHeapSizeLimit                         = 0                                   {product}
    uintx HeapSizePerGCThread                       = 87241520                            {product}
    uintx InitialHeapSize                          := 268435456                           {product}
    uintx LargePageHeapSizeThreshold                = 134217728                           {product}
    uintx MaxHeapSize                              := 4294967296                          {product}
    java version "1.8.0_202"
    Java(TM) SE Runtime Environment (build 1.8.0_202-b08)
    Java HotSpot(TM) 64-Bit Server VM (build 25.202-b08, mixed mode)
```
On local machine (PC) the min heap size is 2 GB and max heap size is 4 GB as shown in above output.
So, if we go with these default configurations and when the consumer application loads the more data beyond 4 GB will get the **java.lang.OutOfMemoryError: Java heap space** error.
Based on application requirement (How much memory it require) we need to set(Tune) those Xms and Xmx values. Those we can set using below command:
```
export _JAVA_OPTIONS="-Xms512m -Xmx8024m"
```
#### GC Tuning
JVM has four types of GC implementations:
 
* **Serial Garbage Collector**
* **Parallel Garbage Collector**
* **CMS Garbage Collector**
* **G1 Garbage Collector**
The each above algorithm have some pros and cons. 
So, based on what kind of work application is doing we can select best suitable one out of above listed. 
To know all default GC configuration run below command:
```
➜java -XX:+PrintFlagsFinal|grep Use|grep GC

     bool ParGCUseLocalOverflow                     = false                               {product}
     bool UseAdaptiveGCBoundary                     = false                               {product}
     bool UseAdaptiveSizeDecayMajorGCCost           = true                                {product}
     bool UseAdaptiveSizePolicyWithSystemGC         = false                               {product}
     bool UseAutoGCSelectPolicy                     = false                               {product}
     bool UseConcMarkSweepGC                        = false                               {product}
     bool UseDynamicNumberOfGCThreads               = false                               {product}
     bool UseG1GC                                   = false                               {product}
     bool UseGCLogFileRotation                      = false                               {product}
     bool UseGCOverheadLimit                        = true                                {product}
     bool UseGCTaskAffinity                         = false                               {product}
     bool UseMaximumCompactionOnSystemGC            = true                                {product}
     bool UseParNewGC                               = false                               {product}
     bool UseParallelGC                            := true                                {product}
     bool UseParallelOldGC                          = true                                {product}
     bool UseSerialGC                               = false                               {product}
```
In Java 8 the default one is Parallel Garbage Collector as you can see in above output.
If we want to run our application with G1 as GC, to do that run below command:
```
java -XX:+UseG1GC -jar Application.java
```
