/* START OF INITIAL BELIEFS FOR DEMO */

// basic info about the arena
width(7).
height(8).

// the locations of obstacles
obstacle(1,2).
obstacle(3,2).
obstacle(4,3).
obstacle(2,4).
obstacle(3,5).
obstacle(2,6).

// the locations of potential victims
potentialVictim(4,2).
potentialVictim(2,5).
potentialVictim(4,6).
potentialVictim(2,2).
potentialVictim(3,4).

// robot or simulation
run(robot).

/* END OF INITIAL BELIEFS FOR DEMO */

/* SOME INITIAL BELIEFS */

// counters
x(0).
y(0).

// object values in the model
objectValue(wall,128).
objectValue(obstacle,16).
objectValue(potentialVictim,32).

// the time of delay
delay(500).

!start.

/* PLANS */

/* INITIALIZE THE MODEL AND ENVIRONMENT */
+!init(wall) <- 
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

/* GET STARTED */
+!start <-
	!init(wall);
	!tell(scout,env);
	?delay(Delay);
	.wait(Delay);
	!build(model);
	.print("I started to work."); 
	.print("I told scout to start working.");
	?run(What);
	.send(scout,achieve,init(What)).

/* LOCALIZATION PART */	
+!plan(localization) <-
	get(nextMoveToLocalize);
	?delay(Delay);
	.wait(Delay);
	?nextMoveToLocalize(X);
	.send(scout,achieve,do(localization,X)).
	
+determined(location)[source(scout)] <- 
	!do(plan); 
	.wait(500); 
	!do(mission).	

/* MISSION: VISIT AND RESCUE THE VICTIMS */
				         
+!do(mission) : pos(X,Y,Z) & potentialVictim(X,Y) <- .send(scout,achieve,analyze(color)).
+!do(mission) <- !after(analysis).
+!after(analysis) : not task(finished) <- get(nextMove); .wait(1000); ?bestMove(X); .send(scout,achieve,moveTo(X)).
+!after(move) : not task(finished) <- !do(mission).	
+!after(analysis) : task(finished) <- !after(analysis).
+!after(move) : task(finished) <- !after(move).	

+task(finished) <- .print("task finished."); stop(everything); .wait(5000);.

/* when color papers are found */
+red(X,Y) <- -potentialVictim(X,Y); !check(mission).
+green(X,Y) <- -potentialVictim(X,Y); !check(mission).
+blue(X,Y) <- -potentialVictim(X,Y);  !check(mission).
+white(X,Y) <- -potentialVictim(X,Y); !check(mission).

/* check whether the mission can be ended now */
+!check(mission): red(_,_) & blue(_,_) & green(_,_) <- 
	!remove(restPotentialVictims);
	+task(finished).

+!check(mission) <- 
	!do(plan).

+!do(plan) <- 
	.findall(potentialVictim(X,Y),potentialVictim(X,Y),List); 
	plan(List).

+!remove(restPotentialVictims)<-
	.findall(potentialVictim(X,Y),potentialVictim(X,Y),List);
	for(.member(potentialVictim(A,B),List)) {
		-potentialVictim(A,B);
		updateModel(white,A,B);
	}.