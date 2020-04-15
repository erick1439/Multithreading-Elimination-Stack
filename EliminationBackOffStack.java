import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

// Class that represents each node in the stack
class Node<T>
{
	int size;
	public T value;
	public Node<T> next;

	public Node(T value)
  {
		this.value = value;
		this.next = null;
	}
}

// Class that contains the action of each thread.
class Process extends Thread
{
  int id;
  EliminationBackOffStack<Integer> stack;
	private ArrayList<Node<Integer>> list;

  public Process(EliminationBackOffStack<Integer> stack, ArrayList<Node<Integer>> list, int id)
  {
    this.id = id;
		this.list = list;
    this.stack = stack;
  }

  public void run()
  {
    int i = 0;

    while (i < 100000)
    {
			int operation = (int)(Math.random() * 3);

      if (operation == 0)
        stack.push(this.list.get(i));


			else if (operation == 1)
			{
				try{
          stack.pop();
  			} catch (Exception e){ e.printStackTrace(); }
			}

			else
				stack.size();

      i++;
    }
  }
}

// Class that extends the original stack from hw2 which will make use of the elimination array.
public class EliminationBackOffStack<T> extends Stack<T> 
{
  private static class RangePolicy 
  {
		int maxRange;
		int currentRange = 1;

    RangePolicy(int maxRange) 
    {
			this.maxRange = maxRange;
		}

    public void recordEliminationSuccess() 
    {
			if (currentRange < maxRange)
				currentRange++;
		}

    public void recordEliminationTimeout()
    {
			if (currentRange > 1)
				currentRange--;
		}

    public int getRange() 
    {
			return currentRange;
		}
	}

	private boolean blocking = false;
	private EliminationArray<T> eliminationArray;
	private static ThreadLocal<RangePolicy> policy;

  public EliminationBackOffStack(final int exchangerCapacity, int exchangerWaitDuration, boolean blocking) 
  {
		//super(blocking);
		this.blocking = blocking;
		eliminationArray = new EliminationArray<T>(exchangerCapacity, exchangerWaitDuration);
		policy = new ThreadLocal<RangePolicy>() {
			protected synchronized RangePolicy initialValue() {
				return new RangePolicy(exchangerCapacity);
			}
		};
	}

  public boolean push(T value) 
  {
		RangePolicy rangePolicy = policy.get();
		Node<T> node = new Node<>(value);

    while (true) 
    {
			if (this.tryPush(node))
        return true;
        
      else 
      {
				try {

					T otherValue = eliminationArray.visit(value, rangePolicy.getRange());
              
          if (otherValue == null) 
          {
						rangePolicy.recordEliminationSuccess();
						return true;
          }
          
				} catch (TimeoutException e) {
					rangePolicy.recordEliminationTimeout();
				}
			}
		}
	}

  public T pop() throws Exception
  {
		RangePolicy rangePolicy = policy.get();

    while (true) 
    {
			Node<T> returnNode = popHelper();
			if (returnNode != null)
        return returnNode.value;
        
      else 
      {
				try {

          T otherValue = eliminationArray.visit(null, rangePolicy.getRange());
          
          if (otherValue != null) 
          {
						rangePolicy.recordEliminationSuccess();
						return otherValue;
          } 
          
          else if(!blocking)
						return null;
					
				} catch (TimeoutException e) { rangePolicy.recordEliminationTimeout(); }
				
				if(!blocking)
					return null;
			}
		}
  }
  
  public static void main(String [] args)
  {

    EliminationBackOffStack<Integer> stack = new EliminationBackOffStack<Integer>(2, 10, true);

		ArrayList<Node<Integer>> list1 = new ArrayList<>();
		ArrayList<Node<Integer>> list2 = new ArrayList<>();
		ArrayList<Node<Integer>> list3 = new ArrayList<>();
		ArrayList<Node<Integer>> list4 = new ArrayList<>();
		ArrayList<Node<Integer>> list5 = new ArrayList<>();

		// Pre-allocate nodes for each Thread.
		for (int i = 0; i < 100000; i++)
		{
			list1.add(new Node<>(i));
			list2.add(new Node<>(i));
			list3.add(new Node<>(i));
			list4.add(new Node<>(i));
			list5.add(new Node<>(i));
		}

		// Pre allocating the stack with 50,000 nodes.
		stack.push(new Node<>(-1));
		stack.head.get().size = 1;
		
		for (int i = 1; i < 50000; i++)
			stack.push(new Node<>(i));

		long startTime = System.currentTimeMillis();

		Process t1 = new Process(stack, list1, 1);
		Process t2 = new Process(stack, list2, 2);
		Process t3 = new Process(stack, list3, 3);
		Process t4 = new Process(stack, list4, 4);
		Process t5 = new Process(stack, list5, 5);

		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();

		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();

		} catch(Exception e) { e.printStackTrace(); }

		long endTime = System.currentTimeMillis();

    // The output of the program.
		System.out.println("Number of operations: " + stack.getNumOps());
		System.out.println("Size of the Stack: " + stack.size());
		System.out.println("Time execution: " + (endTime - startTime) + " milliseconds");
  }
}

class EliminationArray<T> 
{
	private int duration = 10;
	private LockFreeExchanger<T>[] exchanger;
	private Random random;

	@SuppressWarnings("unchecked")
  public EliminationArray(int capacity, int duration) 
  {
		exchanger = (LockFreeExchanger<T>[]) new LockFreeExchanger[capacity];
		this.duration = duration;
    
		for (int i = 0; i < capacity; i++)
			exchanger[i] = new LockFreeExchanger<T>();
		
		random = new Random();
	}

  public T visit(T value, int range) throws TimeoutException
  {
		int slot = random.nextInt(range);
		return (exchanger[slot].exchange(value, duration, TimeUnit.MILLISECONDS));
	}
}

class LockFreeExchanger<T> 
{
	static final int EMPTY = 0, WAITING = 1, BUSY = 2;
	AtomicStampedReference<T> slot = new AtomicStampedReference<T>(null, 0);

	public T exchange(T myItem, long timeout, TimeUnit unit) throws TimeoutException 
	{
		long nanos = unit.toNanos(timeout);
		long timeBound = System.nanoTime() + nanos;
		int [] stampHolder = {EMPTY};

		while (true) 
		{
			if (System.nanoTime() > timeBound)
				throw new TimeoutException();

			T yrItem = slot.get(stampHolder);
			int stamp = stampHolder[0];

			switch (stamp) 
			{
				case EMPTY:
					if (slot.compareAndSet(yrItem, myItem, EMPTY, WAITING)) 
					{
						while (System.nanoTime() < timeBound) 
						{
							yrItem = slot.get(stampHolder);

							if (stampHolder[0] == BUSY) 
							{
								slot.set(null, EMPTY);
								return yrItem;
							}
						}
						if (slot.compareAndSet(myItem, null, WAITING, EMPTY))
							throw new TimeoutException();

						else 
						{
							yrItem = slot.get(stampHolder);
							slot.set(null, EMPTY);
							return yrItem;
						}
					}
					break;

				case WAITING:
					if (slot.compareAndSet(yrItem, myItem, WAITING, BUSY))
						return yrItem;
					break;

				case BUSY:
					break;

				default:
					System.out.println("Impossible");
			}
		}
	}
}
