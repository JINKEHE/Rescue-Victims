// Agent sample_agent in project optmistor

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* internal actions */


/* Plans */

+!start : true <- !goTo(end).

+!goTo(end) : true <- go(next); !goTo(end).
