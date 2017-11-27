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

!start.

+wall(X,Y) <- addWall(X,Y).
+obstacle(X,Y) <- addObstacle(X,Y).


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
	}.

/* Plans */

+!start : true <- 
	!init(wall);
	.print("I started to work."); 
	.print("I told scout to start working.");
	.send(scout,achieve,start).

+red(X,Y) <- -potentialVictim(X,Y); .print("I tried").
+green(X,Y) <- -potentialVictim(X,Y); .print("I tried").
+blue(X,Y) <- -potentialVictim(X,Y); .print("I tried").


//+red(X,Y)[source(scout)] <- +red(X,Y); -potentialVictim(X,Y).
//+green(X,Y)[source(scout)] <- +green(X,Y).
//+blue(X,Y)[source(scout)] <- +blue(X,Y).

