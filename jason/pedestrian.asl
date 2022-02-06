
!registerDF.
+!registerDF <- .df_register("pedestrian");
				.df_subscribe("taxi").

!ensure_init.
+!ensure_init: current_pos(X, Y) <- !travel.
+!ensure_init <- wait(100); !ensure_init.

	
+!travel
	: world_size(SZ)
	<- .random(DX);.random(DY);
		X = math.floor(DX * SZ); Y = math.floor(DY * SZ);
		desire_position(X, Y);
		!travel_to(X, Y);
		!ensure_arrived(X,Y);
		.wait(2000);
		!travel.

+!travel_to(X, Y)                                                
	<-  !clean_previous_cnp;
	    !call_all_taxi(X,Y,TXs);
		!wait_bids(TXs);
		!pick_winner(All,Price,Ag);
		!send_answers(All,Ag).
		
-!travel_to(X,Y)[Reason] 
	<- .print("travel_to failed! Retrying later.",Reason); 
	   .wait(300); 
	   !travel_to(X,Y).

all_received(Len):- .count(propose(_)[source(_)], Len).

+!wait_bids(TXs)
	<- .wait(all_received(.length(TXs)), 300, _).

+!call_all_taxi(DX,DY,TXs)
	: current_pos(X,Y)
	<- .df_search("taxi", TXs);
		.print("Calling for proposal", TXs);
		.send(TXs,tell,cfp(X,Y,DX,DY)).                                                    
		
+!pick_winner(Offs,Price,Ag)
	: .findall(offer(O,A),propose(O)[source(A)],Offs) & Offs \== []
	<- print("Found proposals!", Offs);
		.min(Offs,offer(Price,Ag)).

+!send_answers([],_).
+!send_answers([offer(_,WAg)|T],WAg)
	<- .send(WAg,askOne,accept_proposal(_),accept_proposal(yes));
	   !send_answers(T,WAg).
+!send_answers([offer(_,LAg)|T],WAg)
	<- .send(LAg,tell,reject_proposal);
	   !send_answers(T,WAg).

+!ensure_arrived(X,Y): current_pos(X,Y) <- true.
+!ensure_arrived(X,Y) <- .wait(200); !ensure_arrived(X, Y).

+!clean_previous_cnp 
	<-  !clean_propose;                                  
		!clean_offer.

+!clean_propose: propose(O)[source(_)] <- -propose(O)[source(_)]; !clean_propose.
+!clean_propose <- true.                                                                            

+!clean_offer: offer(O, A)[source(_)] <- -offer(O, A)[source(_)]; !clean_offer.
+!clean_offer <- true.
