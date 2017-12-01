// counters
x(0).
y(0).

delay(500).

// the scout does not have any initial beliefs, but the doctor will tell it
+task(finished) <- 
	.print("nice! doctor asked me to stop working."); 
	?delay(Delay); 
	.wait(Delay).

// add walls
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

// start the mission
+!start : true <- 
	.print("Doctor told me to get started."); 
	!init(wall);
	// add all the posible positions to the belief base
	add(all);
	run(simulation);
	!remove(impossible);
	!scan(around);  
	.print("Where am I? I started to do localization.");
	!do(localization).

/* do localization */

+!do(localization) : determined(location) 
	<- .print("Localization finished."); 
	.send(doctor, tell, determined(location)).	

+!do(localization) : not determined(location) 
	<- ?bestAction(X); 
	execute(X); 
	!scan(around); 
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
	<- get(color);
	get(occupied);
	!remove(impossible);
	remove(impossible);
	!check(localization).

+!remove(impossible)
<-
.findall(pos(X,Y,Heading),pos(X,Y,Heading),ListOfPos);
//.print(ListOfPos);
for (.member(pos(X,Y,Heading),ListOfPos)) {
	
}.
+!check(localization): .count(pos(_,_,_),X) & X=1 <- +determined(location).
//+!check(localization): .count(pos(_,_,_),X) & .print(X) & X=1 <- +determined(location).
+!check(localization).



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
	move(X); 
	?delay(Delay); 
	.wait(Delay); 
	.send(doctor,achieve,after(move)).

/* when color papers are found */
+!found(blue,X,Y) <- 
	.send(doctor,tell,blue(X,Y)); 
	.print("Serious victim found.").

+!found(red,X,Y) <-
	.send(doctor,tell,red(X,Y)); 
	.print("Criticial victim found").

+!found(green,X,Y) <-
	.send(doctor,tell,green(X,Y)); 
	.print("Minor victim found").

+!found(white,X,Y) <-
	.send(doctor,tell,white(X,Y)); 
	.print("No victim here.",X,",",Y).

