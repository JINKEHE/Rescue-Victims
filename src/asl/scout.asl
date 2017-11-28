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


+!init(wall): true
	<- 
	while(x(X) & width(W) & X<=W-1) {
		?height(H); 
    	+wall(X,0);
    	+wall(X,H-1);
		-+x(X+1);
	};
	while(y(Y) & height(H) & Y<=H-1){
		?width(W);
    	+wall(0,Y);
    	+wall(W-1,Y);
		-+y(Y+1);
	};
	-+x(0);
	-+y(0);
	+addWall(finished).

+!start : true 
	<- 
	.print("Doctor told me to get started."); 
	.print("try to add all posible positiosn to the belief base");
	!init(wall);
	//.wait(5000);
	//.findall(wall(A,),wall(A,B),L)
	//.count(L,ZZ)
	//.print("wall length",ZZ)
	add(all);
	//.wait({+initEnv(finished)[source(doctor)]});
	//!addAll(possiblePos);
	!remove(impossible);
	!scan(around);  
	.print("Where am I? I started to do localization.");
	!do(localization).

+!addAll(possiblePos) <- 
	while(x(X) & width(W) & not X=W) {
		-+y(0);
		!addY(X);
		-+x(X+1);
	};
	.count(pos(_,_,_),Z);
	.print(Z).

+!addY(X) <-
	while(y(Y) & height(H) & not Y=H) {
		!addPos(X,Y);
		-+y(Y+1);
	}.

+!addPos(X,Y) <-
if (not wall(X,Y) & not obstacle(X,Y)){
	+pos(X,Y,up);
	+pos(X,Y,down);
	+pos(X,Y,left);
	+pos(X,Y,right);	
}.

+!scan(around): true
	<- detect(env);
	!remove(impossible);
	remove(impossible);
	!check(localization).

+!remove(impossible)
<-
.findall(pos(X,Y,Heading),pos(X,Y,Heading),ListOfPos);
.print(ListOfPos);
for (.member(pos(X,Y,Heading),ListOfPos)) {
	
}.

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

