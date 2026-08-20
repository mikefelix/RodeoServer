This is the server for my home automation setup. I wrote the original server in Javascript with Node.js, 
and this is my Kotlin rewrite, meant to run on a spare Android device (though I'm trying to architect it
such that it would be easy to run on a Raspberry Pi or Linux server). I'm using it as a way to explore
local-first, offline-tolerant concepts:
1. All data access is flow-based and reactive.
2. Every read (e.g. querying the state of the dining room lamp) will simply access a Room table and 
therefore be fast.
3. Every write (e.g. turning on the dining room lamp) will first set the appropriate flag in the Room
table (for immediate UI feedback),
then queue work in a separate table to do the remote call.
4. WorkManager will be used to fire off work when appropriate (immediately, and when the network returns,
or via exponential backoff when calls fail), to drain the queue and execute the remote sync.
5. When remote calls return, they set the correct state in Room, which propagates back to the UI through
the flows.