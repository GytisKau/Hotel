# Hotel Registration Console App

Manages guest registration, checkout, room availability, occupancy history, and revenue reports. Data is stored in Java collections and saved to a binary file `hotel-data.bin` between sessions.

## Implementation
- **Guest** - Stores guest details as an immutable record
- **Room** - Manages occupancy, guest history, and revenue
- **BusinessRoom** - Extends Room with business-class pricing
- **Hotel** - Assigns rooms and provides reports
- **HotelFileStorage** - Saves and loads hotel data
- **HotelApp** - Runs the console menu
