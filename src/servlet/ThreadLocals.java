package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/threadlocals")
public class ThreadLocals extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException
   {
      response.setContentType("text/plain");
      PrintWriter out = response.getWriter();
      try
      {
         out.println(threadLocals());
      }
      catch (ReflectiveOperationException ex)
      {
         ex.printStackTrace();
      }
   }

   // http://blog.igorminar.com/2009/03/identifying-threadlocal-memory-leaks-in.html
   public static String threadLocals() throws ReflectiveOperationException
   {
      Thread thread = Thread.currentThread();
      Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
      threadLocalsField.setAccessible(true);
      Class<?> threadLocalMapKlazz = Class
            .forName("java.lang.ThreadLocal$ThreadLocalMap");
      Field tableField = threadLocalMapKlazz.getDeclaredField("table");
      tableField.setAccessible(true);

      Object table = tableField.get(threadLocalsField.get(thread));

      int threadLocalCount = Array.getLength(table);
      StringBuilder classSb = new StringBuilder();

      for (int i = 0; i < threadLocalCount; i++)
      {
         Object entry = Array.get(table, i);
         if (entry != null)
         {
            Field valueField = entry.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            Object value = valueField.get(entry);
            /*
            if (value != null)
            {
               classSb.append(value.getClass().getName()).append(", ");
            }
            else
            {
               classSb.append("null, ");
            }
            */
            classSb.append(new ObjectAnalyzer().toString(value)).append("\n");
         }
      }
      return classSb.toString();
   }

}

// Core Java vol 1 ch 5
class ObjectAnalyzer
{
   private ArrayList<Object> visited = new ArrayList<>();

   /**
    * Converts an object to a string representation that lists all fields.
    * @param obj an object
    * @return a string with the object's class name and all field names and values
    */
   public String toString(Object obj)
         throws ReflectiveOperationException
   {
      if (obj == null) return "null";
      if (visited.contains(obj)) return "...";
      visited.add(obj);
      Class<?> cl = obj.getClass();
      if (cl == String.class) return (String) obj;
      if (cl.isArray())
      {
         String r = cl.getComponentType() + "[]{";
         for (int i = 0; i < Array.getLength(obj); i++)
         {
            if (i > 0) r += ",";
            Object val = Array.get(obj, i);
            if (cl.getComponentType().isPrimitive()) r += val;
            else r += toString(val);
         }
         return r + "}";
      }

      String r = cl.getName();
      // inspect the fields of this class and all superclasses
      do
      {
         r += "[";
         Field[] fields = cl.getDeclaredFields();
         try {
            AccessibleObject.setAccessible(fields, true);
            // get the names and values of all fields
            for (Field f : fields)
            {
               if (!Modifier.isStatic(f.getModifiers()))
               {
                  if (!r.endsWith("[")) r += ",";
                  r += f.getName() + "=";
                  Class<?> t = f.getType();
                  Object val = f.get(obj);
                  if (t.isPrimitive()) r += val;
                  else r += toString(val);
               }
            }
         }
         catch (InaccessibleObjectException ex) {
         }
         r += "]";
         cl = cl.getSuperclass();
      }
      while (cl != null);

      return r;
   }
}

