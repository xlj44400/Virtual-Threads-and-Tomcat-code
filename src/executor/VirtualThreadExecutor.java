package executor;

import java.util.concurrent.*;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;

public class VirtualThreadExecutor
   extends org.apache.catalina.core.StandardThreadExecutor {

   private ExecutorService exec = Executors.newThreadPerTaskExecutor(
      Thread.ofVirtual()
         .name("myfactory-", 1)
         .factory());      

   public void execute(Runnable command) {
      exec.submit(command);
   }
   
   public void execute​(Runnable command, long timeout, java.util.concurrent.TimeUnit unit) {
      execute(command);      
   }

   @Override
   public int getMaxThreads() {
      return Integer.MAX_VALUE;
   }

   
   public String getNamePrefix() { return "virtual-" + super.getNamePrefix(); }
   @SuppressWarnings("preview")
   @Override
   protected void initInternal() throws LifecycleException {
      super.initInternal();
   }
   @Override
   protected void startInternal() throws LifecycleException {
      setState(LifecycleState.STARTING);
   }
   @Override
   protected void stopInternal() throws LifecycleException {
      setState(LifecycleState.STOPPING);
   }
   @Override
   protected void destroyInternal() throws LifecycleException {
      exec.close();
      super.destroyInternal();
   }
}
