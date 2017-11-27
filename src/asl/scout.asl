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

x(0).
y(0).

/* start of initial beliefs here */

/* end of initial beliefs here */

// Communication


+task(finished): .print("nice! doctor asked me to stop working.").

+!start : true 
	<- 
	.print("Doctor told me to get started."); 
	.print("try to add all posible positiosn to the belief base");
	add(all);
	!scan(around);  
	.print("Where am I? I started to do localization.");
	!do(localization).

+!scan(around): true
	<- detect(env);
	remove(impossible);
	!check(localization).


+!check(localization): .count(pos(_,_,_),X) & .print(X) & X=1 <- +determined(location).
+!check(localization).

+!do(localization) : determined(location) 
	<- .print("Now I know where I am. Localization finished."); 
	plan(path);
	.send(doctor, tell, determined(location)); 
	!do(task).

+!do(localization) : not determined(location) 
	<- ?bestAction(X); 
	execute(X); 
	!scan(around); 
	!do(localization).



+!do(task): pos(X,Y,Z)
	<-.send(doctor,askOne,potentialVictim(X,Y),Reply);
	if (not Reply=false){
		detect(env); 
		.print("started to test color");
		?color(C);
		.print("finished to test color");
		!found(C,X,Y); 
		updateModel(C,X,Y);
	} 
	!check(mission).
	
+!check(mission): task(finished) <- !check(mission).
+!check(mission): not task(finished) <- .send(doctor,askOne,bestMove(X),bestMove(X)); move(X); -bestMove(X); !do(task).

+!found(blue,X,Y): true 
	<- 
	.send(doctor,tell,blue(X,Y)); 
	.print("Serious victim found.").

+!found(red,X,Y): true 
	<-.send(doctor,tell,red(X,Y)); 
	.print("Criticial victim found").

+!found(green,X,Y): true 
	<-.send(doctor,tell,green(X,Y)); 
	.print("Minor victim found").

+!found(white,X,Y): true 
	<-.send(doctor,tell,white(X,Y)); 
	.print("No victim here.",X,",",Y).

/* 
+!reschedule(plan): task(finished)
	<- !reschedule(plan).

+!reschedule(plan): true 
	<- ?bestMove(X); 
	move(X); 
	!do(task).
*/

