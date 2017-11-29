// Agent doctor in project optmistor

/* Initial beliefs and rules */

width(9).
height(8).

man(tom).
x(0).
y(0).

/* Initial goals */
obstacle(2,1).
obstacle(2,3).
obstacle(3,3).
obstacle(3,4).
potentialVictim(2,2).
potentialVictim(5,6).
potentialVictim(4,4).
potentialVictim(2,5).
potentialVictim(1,4).

objectValue(wall,128).
objectValue(obstacle,16).
objectValue(potentialVictim,32).

!start.

+wall(X,Y) <- ?objectValue(wall,WALL) ; addObject(WALL,X,Y).
+obstacle(X,Y) <- ?objectValue(obstacle,OBSTACLE) ; addObject(16,X,Y).
+potentialVictim(X,Y) <- ?objectValue(potentialVictim,POTENTIAL_VICTIM) ; addObject(POTENTIAL_VICTIM,X,Y).

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
	+addWall(finished).

+bestMove(X) <- .send(scout,tell,bestMove(X)).

/* Plans */

+!start : true <- 
	//test(com);
	!init(wall);
	!tell(scout,env);
	.print("I started to work."); 
	.print("I told scout to start working.");
	.send(scout,achieve,start).

+!tell(Who,env): true
	<- 
	?width(X);
    ?height(Y);
    .send(Who,tell,width(X));
    .send(Who,tell,height(Y));
	//.findall(wall(I,O),wall(I,O),ListOfWall);
	//.send(Who,tell,ListOfWall);
	.findall(obstacle(A,B),obstacle(A,B),ListOfObstacle);
	.send(Who,tell,ListOfObstacle);
	.send(Who,tell,initEnv(finished)).

				          

+red(X,Y) <- -potentialVictim(X,Y); !check(task).
+green(X,Y) <- -potentialVictim(X,Y); !check(task).
+blue(X,Y) <- -potentialVictim(X,Y); !check(task).
+white(X,Y) <- -potentialVictim(X,Y).

+!check(task): red(_,_) & blue(_,_) & green(_,_) 
	<- +task(finished);
	-potentialVictim(_,_);
	.send(scout,tell,task(finished));.wait(1000); stop(everything).
+!check(task).

//+red(X,Y)[source(scout)] <- +red(X,Y); -potentialVictim(X,Y).
//+green(X,Y)[source(scout)] <- +green(X,Y).
//+blue(X,Y)[source(scout)] <- +blue(X,Y).

