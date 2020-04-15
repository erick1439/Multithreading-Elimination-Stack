// This is the Stack from hw2 with some modifications to fix
// some errors after grading.

import java.util.concurrent.atomic.*;
import java.util.Random;
import java.util.ArrayList;


// This class will be used on the Stack class. It helps to reduce conflict
// between threads if one of them fails to do push or pop operations.
class Surrender
{
	final int minDelay, maxDelay;
	int limit;
	final Random random;

	public Surrender(int min, int max)
	{
		this.minDelay = min;
		this.maxDelay = min;
		this.limit = minDelay;
		this.random = new Random();
	}

	public void giveUp() throws InterruptedException
  {
		int delay = random.nextInt(limit);
		limit = Math.min(maxDelay, 2 * limit);
		Thread.sleep(delay);
	}
}

// Class with the implementation of the concurrent stack.
public class Stack<T>
{
  static final int MIN_DELAY = 1;
  static final int MAX_DELAY = 2;
  Surrender surrender = new Surrender(MIN_DELAY, MAX_DELAY);

	private AtomicInteger size = new AtomicInteger(0);
	private AtomicInteger numOps = new AtomicInteger(0);
	AtomicReference<Node<T>> head = new AtomicReference<>(null);

	public boolean tryPush(Node<T> newNode)
	{
		Node<T> previous = head.get();
		newNode.next = previous;
		
		if (previous != null)
				newNode.size = previous.size + 1;

		 return (head.compareAndSet(previous, newNode));
	}

	public void push(Node<T> newNode)
  {
		while (true)
    {
			if (tryPush(newNode))
			{
				numOps.incrementAndGet();
				return;
			}

			else
      {
				try {
					surrender.giveUp();
				} catch(Exception e) { e.printStackTrace();  }
			}
		}
	}

	public Node<T> popHelper() throws Exception
  {
		Node<T> previous = head.get();

		if (previous == null)
		{
				System.out.println("The array is empty. Pre-allocate more items into the array before running again!");
				System.exit(1);
		}

		Node<T> top = previous.next;

		if (head.compareAndSet(previous, top))
    {
			numOps.incrementAndGet();
			return previous;
		}

		else
			return null;
	}

	public T pop() throws Exception
  {
		while (true)
    {
			Node<T> node = popHelper();

			if (node != null)
				return node.value;
			
			else
				surrender.giveUp();
		}
	}

	public void print()
	{
		Node<T> temp = head.get();

		while (temp != null)
		{
			System.out.print(temp.value + " ");
			temp = temp.next;
		}
	}

	public int getNumOps()
	{
		return numOps.get();
	}

	public int size()
	{
		numOps.incrementAndGet();
		return head.get().size;
	}

}
