# Caching Function
Implement the interface [Cache](src/main/java/scb/cache/Cache.java) for a class that caches the results of a function.

## Constraints
* The implementation will return V from an internal collection if the value is cached otherwise it will call a provided Function<K, V> to get the value.
* The implementation should allow the user of this class to provide a Function<K, V> that is used to obtain the value.
* Important that for any unique instance of K the function is only called once.
* How to handle null K and V is within your prerogative as is, what happens if Function<K, V> throws, however we do need to know your design choices and why in the interview.
* Threading constraints:
  * The function is assumed thread-safe so for different values of K it may be called concurrently. 
  * Point 3 should never be violated so if 2 or more threads have a cache miss on the same key then only 1 may call the function, the other threads must wait efficiently and return the cached value once the winner has called the function and obtained a value.

The above may be implemented with a “Map.computeIfAbsent” however we are interested in how you would implement this.

# Deadline Scheduler
A component is required to schedule timer events with a given deadline. When the deadline is met or exceeded (>=) a provided call-back interface is called with the id of the expired request.  
 
Please read the doc of the [interface](src/main/java/scb/schedule/DeadlineEngine.java) methods for more in depth requirements.