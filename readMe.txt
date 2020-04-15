- How to run:
  + From a command prompt go to the directory where the files are stored
  + Compile the program as "javac EliminationBackOffStack.java"            
  + Run the program by typing "java EliminationBackOffStack"
  + The number of operation, size of stack, and execution time will be printed into the prompt console

- Correctness 
  + For this stack implementation, I am using as a template the pseudo code from the book on chapter 11. This implementation will be 
    linearizable because we are inheriting all the essential operations from the stack on hw2. The difference is that we will add the 
    elimination array to cancel out push() and pop() operations at the point in which they exchanged values. The linearization points 
    of both the push() and the pop() methods is the successful compareAndSet(), or the throwing of the exception in case of a 
    pop() on an empty stack.

- Comparison:
  + It appears that as we increase the number of threads the elimination stack will perform better than the stack from hw2. From the 
    data comparison I was able to see that the stack from hw2 has a similar or slightly better performance with a small number of threads, but 
    as we increase the number of threads the stack with the elimination array will yield better results. This is probably because as we increase the number 
    of threads the stack from hw2 will have a bigger contention causing a sequential bottle neck between the threads. In the other side, this current stack will 
    cancel pop() and push() operations as they happen back to back.
 
- RESOURCES:
    1) The art of multiprocessor programming. (Chapter 11)
    2) https://docs.oracle.com/javase/8/docs/api/
