# FleckSearch
Author: James Fleckenstein

_This project was completed individually._ 

This repository serves as my submission for the CS 1660 term project, Option 2.

Required Video Code and Application Walkthrough:
https://pitt-my.sharepoint.com/:v:/g/personal/jpf47_pitt_edu/EdoGE0Z_kNtEgv5DMDr3OxABzqimjjPtawR4drEUtgRYbA?e=m2eua8

## Compiling the jars for the Cluster
To perform this step, you must have a running Dataproc cluster on the Google cloud, and must also have the required 
Hadoop Jar Files (the ones located in the module on Canvas) in some directory accessible to the current working directory. The path to this directory will be 
referred to in the following instructions as `[JAR_PATH]`.

### Compiling InvertedIndex
This jar is responsible for constructing the Inverted Indices on the cluster
To compile and package:

1. `cd InvertedIndecies`
1. `javac --release 8 -cp "[JAR_PATH]" InvertedIndex.java`
1. `jar cf ii.jar InvertedIndex*.class`

The jar `ii.jar` must then be uploaded to the GCP cluster, and will be referenced in a job request by FleckSearch

### Compiling TopN
This jar is responsible for determine the Top N most common words, where N is a number specified by the user
To compile and package:

1. `cd TopN`
1. `javac --release 8 -cp "[JAR_PATH]" TopN.java`
1. `jar cf tn.jar TopN*.class`

The jar `tn.jar` must then be uploaded to the GCP cluster, and will be referenced in a job request by FleckSearch

## Running the GUI Docker Container

### Authentication
In order to use this application on your cluster, there are a few variables that must be modified before you can begin.
One resides in the Dockerfile located in the FleckSearch Directory, while the other two lie in the source code located at:
`\FleckSearch\src\main\java\work\flecksearch`

1. Dockerfile
    * Modify the Dockerfile's line 12 to include the name of you GCP key JSON file as `[key-file]`, which should be placed in the same
     FleckSearch directory as the Dockerfile
    * `ENV GOOGLE_APPLICATION_CREDENTIALS="/[key-file].json"`
    
1. FrontEnd.java
    * This file must be modified on lines 114 to 120 (located in the `submitJob()` method) as follows:
    ```
        String projectId = "[project-id]"; //change to your project id for your cluster
        String region = "[region]"; //change to the region where your cluster resides 
        String clusterName = "[cluster-name]"; //change to the name of your cluster you wish to run this proccess on
        String hadoopIndexMain = "InvertedIndex"; //keep the same
        String hadoopIndexJar = "[jar-path]/ii.jar"; //change to the path where the jar is located on the cluster
        String hadoopIndexArgs = "[input-path]/* [output-path]"; //change to specify the input path where the input directories reside, and specify the output path you would like to use
        String hadoopcatQuery = "-cat [output-path]/*"; //change to include the same output path as above
    ``` 
1. DisplayTopN.java
* This file must be modified on lines 123 to 129 (located in the `submitJob()` method) as follows:
    ```
        String projectId = "[project-id]"; //change to your project id for your cluster
        String region = "[region]"; //change to the region where your cluster resides
        String clusterName = "[cluster-name]"; //change to the name of your cluster you wish to run this proccess on
        String hadoopTopNMain = "TopN"; //keep the same
        String hadoopTopNJar = "[jar-path]/tn.jar"; //change to the path where the jar is located on the cluster
        String hadoopTopNArgs = "-D nValue="+n+" [index-input-path] [output-path]/Top"+n; //change the input path to the output of the prevoiusly ran InvertedIndex, and specify the output path you would like to use
        String hadoopcatQuery = "-cat [output-path]/Top"+n+"/*"; //specify the same path as above
    ``` 
  
### Starting the Application
To the run the Docker container, make sure to have Xming running on your system by following the instructions linked here:
https://docs.microsoft.com/en-us/archive/blogs/jamiedalton/windows-10-docker-gui
Then, run the following commands to build and run the GUI application in Docker:

1. `cd FleckSearch`
1. `docker build -t flecksearch .`
1. `docker run -it --privileged -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix flecksearch`


## References Cited:
The following section compromises the array of sources consulted for this project. They include things like official
documentation, tutorials, and publicly answered questions.

The overall Java API for Hadoop was instrumental for implementing both jar programs: 
* https://hadoop.apache.org/docs/current/api/index.html

The official Hadoop MapReduce tutorial also formed the basis for both jar files, in terms of implementing the Mapper and Reducer classes:
* https://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html

The official Java API documentation for Java 8 was referenced in the construction of all three applications, particularly
its documentation on Arrays, Regex Patterns, and BufferedReader:
* https://docs.oracle.com/javase/8/docs/api/

The following chapter shared in class on Inverted Indices provided the general high-level structure of my implementation:
* http://www.dcs.bbk.ac.uk/~dell/teaching/cc/book/ditp/ditp_ch4.pdf

The following tutorial also formed the basis for my TopN algorithm:
* https://www.geeksforgeeks.org/how-to-find-top-n-records-using-mapreduce/

The following tutorial by Antoine Amend also assisted me in implementing the Tool interface, which would allow for command-line specified 
arguments, specifically the size of N. The above guide omitted this requirement:
* https://hadoopi.wordpress.com/2013/06/05/hadoop-implementing-the-tool-interface-for-mapreduce-driver/

The following guide from Netbeans assisted me heavily in leveraging Netbean's built-in GUI constructor to create the required GUI: 
* https://netbeans.org/kb/docs/java/gui-functionality.html

This StackOverflow answer from user Paul Samsotha assisted with constructing a Multi-JFrame application:
* https://stackoverflow.com/a/20988058

This example code, provided by Google, formed the basis for executing both of my Hadoop Jobs on the cluster:
* https://github.com/googleapis/java-dataproc/blob/master/samples/snippets/src/main/java/SubmitHadoopFsJob.java

The official Apache Maven documentation, specifically its tutorial on using Maven and constructing a pom file, was used
to quickly package my gui application into a runnable jar:
* https://maven.apache.org/guides/getting-started/index.html
* https://maven.apache.org/pom.html

This StackOverflow answer from Olivier Refalo helped fix an issue with Maven's packaging of jars:
* https://stackoverflow.com/a/9689877

This StackOverflow answer from user cactuschibre helped me included the required GCP dependencies into my jar file 
* https://stackoverflow.com/a/43309812

Docker's Official documentation on building Dockerfiles and constructing multi-staged builds was invaluable to construing 
a Dockerfile that would allow me to run my GUI within a container:
* https://docs.docker.com/engine/reference/builder/
* https://docs.docker.com/develop/develop-images/multistage-build/

I utilized the official Maven Docker image, which can be found on Docker Hub:
* https://hub.docker.com/_/maven?tab=description

When attempting to use the basic Maven image to compile and package my FleckSearch GUI application, I ran into an issue stemming from
that image not containing the needed libex6 library to render the GUI. The following blog post from Jessica Kerr demonstrated
a similar issue on a different system, and found that using an image which contained a linux distribution (In this case,
an image of OpenJDK which contained Debain) would allow me to download and install the required library package before running: 
* https://jessitron.com/2020/04/17/run-alloy-on-windows-in-docker/

I also used the following fix from Xming's website to stop Xming from randomly crashing:
* http://www.straightrunning.com/XmingNotes/trouble.php#head-14