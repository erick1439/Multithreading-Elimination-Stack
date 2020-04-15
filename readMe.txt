- How to run:
  + From a command prompt go to the directory where the files are stored
  + Compile the program as "javac EliminationBackOffStack.java"            
  + Run the program by typing "java EliminationBackOffStack"
  + The number of operation, size of stack, and execution time will be printed into the prompt console


- Atomic Variables:
    In part 2 we are also using another atomic variable to store the size of
    the stack.

- Description (Same description as part 1):
    In this stack implementation the pop and push methods will try to compute their operations; if they fail they will surrender their
    turn and try later. The reason they give up their turn is to reduce the number of conflicts between threads at the top of the stack.
    To achieve linearization this program will make the use of the method compare and set from the AtomicReference class in java where
    the tread will atomically update the head of the stack if current value of the head is equal to the expected value that the thread has.

- Additional Notes:
    1) In part 2 every time the size method is called it will count towards the number of operations.
    2) Instead of choosing between pop() and push(), the thread will have the option to choose between
       push(), pop(), and size() for part 2.

- RESOURCES:
    1) The art of multiprocessor programming.
    2) https://docs.oracle.com/javase/7/docs/api/
