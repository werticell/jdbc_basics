CREATE TABLE aircrafts (
    aircraft_code CHAR(3) NOT NULL,
    model text NOT NULL,
    range INTEGER NOT NULL,
    PRIMARY KEY(aircraft_code)
);

CREATE TABLE airports (
    airport_code CHAR(3) NOT NULL,
    airport_name text NOT NULL,
    city text NOT NULL,
    coordinates text NOT NULL,
    timezone text NOT NULL,
    PRIMARY KEY (airport_code)
);

CREATE TABLE boarding_passes (
     ticket_no CHAR(13) NOT NULL,
     flight_id INTEGER NOT NULL,
     boarding_no INTEGER NOT NULL,
     seat_no VARCHAR(4) NOT NULL,
     PRIMARY KEY(ticket_no, flight_id)
);

CREATE TABLE bookings (
    book_ref CHAR(6) NOT NULL,
    book_date TIMESTAMP WITH TIME ZONE NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL,
    PRIMARY KEY(book_ref)
);

CREATE TABLE flights (
    flight_id serial NOT NULL,
    flight_no CHAR(6) NOT NULL,
    scheduled_departure TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_arrival TIMESTAMP WITH TIME ZONE NOT NULL,
    departure_airport CHAR(3) NOT NULL,
    arrival_airport CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    aircraft_code CHAR(3) NOT NULL,
    actual_departure TIMESTAMP WITH TIME ZONE,
    actual_arrival TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY(flight_id)
);

CREATE TABLE seats (
    aircraft_code CHAR(3) NOT NULL,
    seat_no VARCHAR(4) NOT NULL,
    fare_conditions VARCHAR(10) NOT NULL,
    PRIMARY KEY(aircraft_code, seat_no)
);

CREATE TABLE ticket_flights (
    ticket_no CHAR(13) NOT NULL,
    flight_id INTEGER NOT NULL,
    fare_conditions VARCHAR(10) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY(ticket_no, flight_id)
);

CREATE TABLE tickets (
     ticket_no CHAR(13) NOT NULL,
     book_ref CHAR(6) NOT NULL,
     passenger_id VARCHAR(20) NOT NULL,
     passenger_name text NOT NULL,
     contact_data text,
     PRIMARY KEY(ticket_no)
);
