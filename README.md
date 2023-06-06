# sajr-java
It is java part of SAJR - the package to analyse alternative splicing bsed on bulk RNA-seq. This repository contains java code for read counter. Please go to https://github.com/iaaka/sajr if you with to perform alternative splicing analyses, this repo is for developmental purposes only.

## Compilation
Sajr uses ant as compilation manager, under linux it can be installed by `sudo apt install ant`.
<pre>
git clone https://github.com/iaaka/sajr-java.git
cd sajr-java
ant jar
</pre>
 
## Testing
The repository contains toy dataset for testing. Run the following code from  the project folder to try it
<pre>
java -jar sajr.jar count_reads
</pre>
It will use parameters, input, and output files specified in `sajr.config` that points to data in `example`. It should produce `.gene`, `.seg`, and `.intron` files for each of two input files (six in total).
For more information and manual please refer to https://github.com/iaaka/sajr.
