// Agent sample_agent in project optmistor

/* Initial beliefs and rules */

/* Initial goals */



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

/* start of initial beliefs here */

man(tom).

/* end of initial beliefs here */

!start.

+!start : true 
	<- add(all); 
	detect(env); 
	remove(impossible); 
	!do(localization).

+!do(localization) : not determined(location) 
	<- ?bestAction(X); 
	execute(X); 
	detect(env); 
	remove(impossible); 
	!do(localization).

+!do(localization) : determined(location) 
	<- .print("localization finished."); 
	plan(path); !do(task).

+!do(task): pos(X,Y,Z) & potentialVictim(X,Y) 
	<- detect(env); 
	?color(Color); 
	process(Color); 
	!reschedule(plan).

+!do(task): pos(X,Y,Z) & not potentialVictim(X,Y) 
	<- ?bestMove(M); 
	move(M); 
	!do(task).

+!reschedule(plan): not finished(task) 
	<- ?bestMove(X); 
	move(X); 
	!do(task).

+!reschedule(plan): finished(task) 
	<- stop(everything).



