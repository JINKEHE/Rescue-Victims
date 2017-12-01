// Agent doctor in project optmistor

/* Initial beliefs and rules */

width(7).
height(8).

man(tom).
x(0).
y(0).

/* Initial goals */
obstacle(1,1).
obstacle(2,1).
obstacle(3,1).
obstacle(1,2).
potentialVictim(2,2).
potentialVictim(3,2).
potentialVictim(4,1).
potentialVictim(5,5).
potentialVictim(2,4).

objectValue(wall,128).
objectValue(obstacle,16).
objectValue(potentialVictim,32).

!start.

 
+wall(X,Y) <- ?objectValue(wall,WALL).
+obstacle(X,Y) <- ?objectValue(obstacle,OBSTACLE).
+potentialVictim(X,Y) <- ?objectValue(potentialVictim,POTENTIAL_VICTIM).

/*
+wall(X,Y) <- ?objectValue(wall,WALL).
+obstacle(X,Y) <- ?objectValue(obstacle,OBSTACLE).
+potentialVictim(X,Y) <- ?objectValue(potentialVictim,POTENTIAL_VICTIM).
*/
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


/* Plans */

+!start : true <-
	//test(com);
	!init(wall);
	!tell(scout,env);
	.wait(2000);
	!build(model);
	.print("I started to work."); 
	.print("I told scout to start working.");
	.send(scout,achieve,start).

+!build(model) <-
	?width(Width);
	?height(Height);
	.findall(potentialVictim(X,Y),potentialVictim(X,Y),VictimList);
	.findall(obstacle(A,B),obstacle(A,B),ObstacleList);
	buildModel(Width,Height,VictimList,ObstacleList);
	.findall(wall(W,E),wall(W,E),WallList);
	for (.member(wall(I,G),WallList)) { 
		?objectValue(wall,WALL); 
		addObject(WALL,I,G);
	};
	for (.member(obstacle(Q,G),ObstacleList)) { 
		?objectValue(obstacle,OBSTACLE) ; 
		addObject(OBSTACLE,Q,G);
	};
	for (.member(potentialVictim(K,T),VictimList)) {
		?objectValue(potentialVictim,POTENTIAL_VICTIM) ; 
		addObject(POTENTIAL_VICTIM,K,T)
	}.

+!tell(Who,env) <- 
	?width(X);
    ?height(Y);
    .send(Who,tell,width(X));
    .send(Who,tell,height(Y));
	.findall(obstacle(A,B),obstacle(A,B),ListOfObstacle);
	.send(Who,tell,ListOfObstacle);
	.send(Who,tell,initEnv(finished)).

+determined(location)[source(scout)] <- !do(plan); .wait(500); !do(mission).				          

+!do(mission) : pos(X,Y,Z) & potentialVictim(X,Y) <- .send(scout,achieve,analyze(color)).
+!do(mission) <- !after(analysis).
+!after(analysis) : not task(finished) <- get(nextMove); .wait(1000); ?bestMove(X); .send(scout,achieve,moveTo(X)).
+!after(move) : not task(finished) <- !do(mission).	
+!after(analysis) : task(finished) <- !after(analysis).
+!after(move) : task(finished) <- !after(move).	

+task(finished) <- .wait(50000); .print("task finished."); stop(everything).

+red(X,Y) <- -potentialVictim(X,Y); !check(mission).
+green(X,Y) <- -potentialVictim(X,Y); !check(mission).
+blue(X,Y) <- -potentialVictim(X,Y);  !check(mission).
+white(X,Y) <- -potentialVictim(X,Y); !check(mission).

+!check(mission): red(_,_) & blue(_,_) & green(_,_) <- 
	!remove(restPotentialVictims);
	.wait(500);
	+task(finished);
	//.send(scout,tell,task(finished));
	.wait(500).

+!remove(restPotentialVictims)<-
	.findall(potentialVictim(X,Y),potentialVictim(X,Y),List);
	for(.member(potentialVictim(A,B),List)) {
		-potentialVictim(A,B);
		updateModel(white,A,B);
	}.
	
+!check(mission) <- 
	!do(plan).

+!do(plan) <- 
	.findall(potentialVictim(X,Y),potentialVictim(X,Y),List); 
	plan(List).

//+red(X,Y)[source(scout)] <- +red(X,Y); -potentialVictim(X,Y).
//+green(X,Y)[source(scout)] <- +green(X,Y).
//+blue(X,Y)[source(scout)] <- +blue(X,Y).

