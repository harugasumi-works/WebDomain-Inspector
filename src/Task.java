import java.util.concurrent.Callable;

public interface Task extends Callable<Boolean> { // Changed from Runnable
   int getPriority();
}