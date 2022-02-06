
available.
!registerDF.
+!registerDF <- .df_register("taxi").
		
+!transfer_client(Ag,SX,SY,DX,DY)
	: true
	<-
		.print("Serving client ", Ag, "Source: ", SX, ",", SY, "; Dest: ", DX, ",", DY);
		.print("Heading to ", Ag, " ", SX, ", ", SY);
		!move_to(SX, SY);
		!pick_up(Ag);
		.print("Picked up ", Ag);
		.print("Going to ", DX, " ", DY);
		!move_to(DX, DY);
		!drop(Ag);
		.print("Delivered ", Ag);
		+available.

dist(SX,SY,SX,SY,0).
dist(SX,SY,DX,DY,Price)
	:- dir(SX,DX,DirX) &
	   dir(SY,DY,DirY) &
	   NX = SX + DirX &
	   NY = SY + DirY &
	   dist(NX,NY,DX,DY,P) &
	   Price = P + 1.
		
		
+cfp(SX,SY,DX,DY)[source(Ag)]
   :  available & dist(SX,SY,DX,DY,Dist) & .random(PPD) & Price = PPD * Dist
   <- !clean_ag_proposals(Ag);
   	  +proposal(Ag,SX,SY,DX,DY,Price);
   	  -cfp(SX,SY,DX,DY)[source(Ag)];
      .send(Ag,tell,propose(Price)).
+cfp(SX,SY,DX,DY)[source(Ag)]
	<- -cfp(SX,SY,DX,DY)[source(Ag)].

+!clean_ag_proposals(Ag): proposal(Ag,_,_,_,_,_) <- -proposal(Ag,_,_,_,_,_); !clean_ag_proposals(Ag).
+!clean_ag_proposals(_) <- true.
	
+?accept_proposal(yes)[source(Ag)]
   :  available & proposal(Ag,SX,SY,DX,DY,Price) 
   <- -available; 
   		-proposal(Ag,SX,SY,DX,DY,Price); 
		.print("Accepted p ", Ag,", ", Price);
		!transfer_client(Ag,SX,SY,DX,DY);.

+?accept_proposal(no)[source(Ag)]
   <- -proposal(Ag,_,_,_,_,_).
 
+reject_proposal[source(Ag)]
   <- -proposal(Ag,_,_,_,_,_).
	  
+!pick_up(Ag): true <- carry(Ag); +carrying(Ag).
+!drop(Ag): carrying(Ag) <- drop; -carrying(Ag).


dir(P, Q, R):- P > Q & R = -1.
dir(P, Q, R):- P < Q & R = 1.
dir(P, Q, R):- P == Q & R = 0.
	
+!move_to(X, Y): current_pos(X,Y) <- true.
+!move_to(X, Y)
	: current_pos(CX, CY) 
	  & (CX \== X | CY \== Y)
	  & dir(CX, X, DX)
	  & dir(CY, Y, DY)
	<- NX = CX + (1 * DX); 
	   NY = CY + (1 * DY); 
	   -current_pos(CX,CY); 
	   +current_pos(NX, NY);
	   move_at(NX, NY);
	   .wait(200);
	   !move_to(X, Y).
	
	
	
	
