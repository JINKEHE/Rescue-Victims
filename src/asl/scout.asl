// Agent sample_agent in project optmistor

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* internal actions */


/* list of Plans */

/*
 *         name                 functinality
 * 
 *         start                get everything started
 * 
 *        
 */
 
/* list of beliefs */ 

/*
 *         name                 functinality
 *	       
 *         determined(location)	   
 *  
 *         color(x) 
 * 
 * 		   pos(x,y,heading)
 * 
 *   
 *		   occupied()
 * 
 *         bestAction()
 * 
 *         nextMove()	   
 *        
 */



+!start : true <- add(all); detect(env); remove(impossible); !do(localization).

+!do(localization) : not determined(location) <- ?bestAction(X); execute(X); detect(env); remove(impossible); !do(localization).

+!do(localization) : determined(location) <- .print("localization finished.") ; plan(path); !do(task).


+!do(task): pos(X,Y,Z) & potentialVictim(X,Y) <- detect(env); ?color(Color); process(Color); !reschedule(plan).

+!do(task): pos(X,Y,Z) & not potentialVictim(X,Y) <- ?bestMove(M); move(M); !do(task).

+!reschedule(plan): not finished(task) <- ?bestMove(X); move(X); !do(task).

+!reschedule(plan): finished(task) <- stop(everything).





/*  rubbish bin */

//+!start : true <- add(all); get(info); !goTo(next).
//+!goTo(next) : true <- go(next); get(info); !goTo(next).

//+!localize(scout) : heading(X) & not occupied(X) <- ?heading(X); move(X); get(percepts); !localize(scout).
//+!localize(scout) : heading(X) & occupied(X) <- turn(90); get(percepts); !localize(scout).

/*  rubbish bin */
 
//+!localize(scout) : determined(loc) <- !goTo(next).
//+!localize(scout) : not occupied(down) <- move(down); get(percepts); !localize(scout).
//+!localize(scout) : not occupied(right) <- move(right); get(percepts); !localize(scout).
//+!localize(scout) : not occupied(left) <- move(left); get(percepts); !localize(scout).
//+!localize(scout) : not occupied(up) <- move(up); get(percepts); !localize(scout).

// +!start : true <- !goTo(end).
// +!goTo(end) : true <- go(next); get(percepts); ?pos(scout, X, Y); .print("X=",X,",Y=",Y); !goTo(end).

/* rubbish bin */




