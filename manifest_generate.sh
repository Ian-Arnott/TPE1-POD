#!/bin/bash

# Function to generate random uppercase letters
generate_random_letters() {
    cat /dev/urandom | tr -dc 'A-Z' | head -c "$1"
}

# Function to generate random numbers
generate_random_numbers() {
    shuf -i 100-999 -n 1
}

# Function to generate random airline names
generate_random_airline() {
    airlines=("AirCanada" "AmericanAirlines" "Delta" "United" "Lufthansa")
    rand_index=$((RANDOM % ${#airlines[@]}))
    echo "${airlines[$rand_index]}"
}

# Initialize variables
NUM_ENTRIES=50
NUM_UNIQUE_FLIGHTS=8
BOOKINGS_FILE="manifest.csv"
declare -A booked_flights

# Generate CSV header
echo "booking;flight;airline" > "$BOOKINGS_FILE"
echo "ABC123;AC987;AirCanada" >> "$BOOKINGS_FILE"
echo "XYZ234;AA123;AmericanAirlines" >> "$BOOKINGS_FILE"
echo "XYZ235;AA124;AmericanAirlines" >> "$BOOKINGS_FILE"
echo "XYZ236;AA125;AmericanAirlines" >> "$BOOKINGS_FILE"



# Generate unique flights
for ((i=0; i<NUM_UNIQUE_FLIGHTS; i++)); do
    flight="AC$(generate_random_numbers)"
    airline=$(generate_random_airline)
    booked_flights[$flight]="$airline"
done

# Generate random entries with some flight numbers repeated
for ((i=0; i<NUM_ENTRIES; i++)); do
    booking=$(generate_random_letters 6)

    # Randomly select a flight number from the generated unique flights
    flight_numbers=("${!booked_flights[@]}")
    flight=${flight_numbers[$RANDOM % ${#flight_numbers[@]}]}
    airline=${booked_flights[$flight]}

    # Write entry to CSV
    echo "$booking;$flight;$airline" >> "$BOOKINGS_FILE"
done
