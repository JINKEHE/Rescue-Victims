/* counters */
x(0).
y(0).
numOfSteps(0).
delay(500).

/* the scout does not have any initial beliefs, but the doctor will tell it */
+task(finished) <- 
	.print("nice! doctor asked me to stop working."); 
	?delay(Delay); 
	.wait(Delay).

+!init(robot) <- .print("Run on robot"); start(socket); !start.
+!init(simulation) <- .print("Run in simulation"); start(simulation); !start.

/* add walls */
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
	-+x(0);
	-+y(0);
	+addWall(finished).

/* start the mission */
+!start <- 
	.print("Doctor told me to get started."); 
	!init(wall);
	add(allPossiblePositions);
	!scan(around);  
	.print("Where am I? I started to do localization.").

/* localization */
+!do(localization, Action) <-  
	moveBefore(Action); 
	!scan(around).

+!scan(around) <- 
	get(color);
	get(occupiedInfo);
	remove(impossible);
	!check(localization).

+!check(localization): .count(pos(_,_,_),X) & X=1 <- 
	.print("Localization finished.");
	+determined(location); 
	.send(doctor, tell, determined(location)).
+!check(localization) <- .send(doctor, achieve, plan(localization)).

/* analyze the color of the grid */
+!analyze(color) <- 
	?pos(X,Y,Z)
	get(color);
	?delay(Delay);
	.wait(Delay);
	?color(C);
	!found(C,X,Y);
	updateModel(C,X,Y);
	.wait(Delay);
	.send(doctor,achieve,after(analysis)).
	
/* move to a grid next to the robot according to the relative direction */
+!moveTo(X) <- 
	moveAfter(X); 
	?delay(Delay); 
	.wait(Delay);
	?numOfSteps(Steps);
	-+numOfSteps(Steps+1);
	.print("Number of steps taken: ", (Steps+1));
	.send(doctor,achieve,after(move)).

/* when color papers are found */
+!found(blue,X,Y) <- 
	.send(doctor,tell,blue(X,Y)); 
	.print("Serious victim found.").

+!found(red,X,Y) <-
	.send(doctor,tell,red(X,Y)); 
	.print("Critical victim found").

+!found(green,X,Y) <-
	.send(doctor,tell,green(X,Y)); 
	.print("Minor victim found").

+!found(white,X,Y) <-
	.send(doctor,tell,white(X,Y)); 
	.print("No victim here.",X,",",Y).

