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

/* end of initial beliefs here */

// Communication




+!start : true 
	<- 
	.send(doctor,tell,red(fuck));
	.print("Doctor told me to get started."); 
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

+!do(localization) : not determined(location) 
	<- ?bestAction(X); 
	execute(X); 
	!scan(around); 
	!do(localization).

+!do(localization) : determined(location) 
	<- .print("Now I know where I am. Localization finished."); 
	plan(path); 
	!do(task).

+!do(localization) <- .print("GG").

+!do(task): pos(X,Y,Z)
	<-.send(doctor,askOne,potentialVictim(X,Y),Reply);
	.print(Reply);
	if (Reply=potentialVictim(X,Y)[source(doctor)]){
		detect(env); 
		?color(Color); 
		!found(Color); 
		-potentialVictim(X,Y);
		updateModel(Color,X,Y);
	} 
	!reschedule(plan).

+!found(blue): true 
	<- ?pos(X,Y,_); 
	.send(doctor,tell,blue(X,Y)); 
	.print("Serious victim found.").

+!found(red): true 
	<- ?pos(X,Y,_); 
	.send(doctor,tell,red(X,Y)); 
	.print("Criticial victim found").

+!found(green): true 
	<- ?pos(X,Y,_); 
	.send(doctor,tell,green(X,Y)); 
	.print("Minor victim found").

+!found(white): true 
	<- ?pos(X,Y,_); 
	.print("No victim here.").

+!reschedule(plan): red(_,_) & blue(_,_) & green(_,_) 
	<- -potentialVictim(_,_); 
	stop(everything).

+!reschedule(plan): true 
	<- ?bestMove(X); 
	move(X); 
	!do(task).


