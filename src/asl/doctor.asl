// Agent doctor in project optmistor

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true <- 
	.print("I started to work."); 
	.print("I told scout to start working.");
	.send(scout,achieve,start).

+X[source(scout)] <- +X.
