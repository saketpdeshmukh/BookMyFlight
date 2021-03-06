package com.saket.flightreservation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saket.flightreservation.controllers.ReservationController;
import com.saket.flightreservation.dto.ReservationRequest;
import com.saket.flightreservation.entities.Flight;
import com.saket.flightreservation.entities.Passenger;
import com.saket.flightreservation.entities.Reservation;
import com.saket.flightreservation.repositories.FlightRepository;
import com.saket.flightreservation.repositories.PassengerRepository;
import com.saket.flightreservation.repositories.ReservationRepository;
import com.saket.flightreservation.utilities.EmailUtil;
import com.saket.flightreservation.utilities.PDFGenerator;

@Service
public class ReservationServiceImpl implements ReservationService {

	private String ITINERARY_DIR = "S:/Saket/Documents/Flights";

	private static final Logger LOGGER = LoggerFactory.getLogger(ReservationServiceImpl.class);
	
	@Autowired
	private FlightRepository flightRepo;
	
	@Autowired
	private PassengerRepository passengerRepo;
	
	@Autowired
	private ReservationRepository reservationRepo;
	
	@Autowired
	private PDFGenerator pdfGenerator;
	
	@Autowired
	private EmailUtil emailUtil;
	
	@Override
	@Transactional
	public Reservation bookFlight(ReservationRequest request) {
		
		LOGGER.info("inside bookFlight(): request: "+request);
		
		Long flightId = request.getFlightId();
		Flight flight =  flightRepo.findById(flightId).get();
		
		Passenger passenger = new Passenger();
		passenger.setFirstName(request.getTravellerFirstName());
		passenger.setLastName(request.getTravellerLastName());
		passenger.setPhone(request.getTravellerPhone());
		passenger.setEmail(request.getTravellerEmail());
		
		Passenger savedTraveller = passengerRepo.save(passenger);
		
		Reservation reservation = new Reservation();
		reservation.setFlight(flight);
		reservation.setPassenger(savedTraveller);
		reservation.setCheckedIn(false);
		
		Reservation savedReservation = reservationRepo.save(reservation);
		
		String filePath = ITINERARY_DIR+savedReservation.getId()+".pdf";
		pdfGenerator.generateItinerary(savedReservation, filePath);
		
		emailUtil.sendItinerary(passenger.getEmail(), filePath);
		
		return savedReservation;
	}

}
