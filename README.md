# SmartAlert - Emergency Notification App

SmartAlert is an Android application developed as part of the PMS "Advanced IT Systems - Software 
Development and Artificial Intelligence" course in 2023. 
The application aims to provide immediate notifications to registered users in high-risk emergency 
situations. 
It allows users to submit and receive alerts related to various high-risk incidents, such as floods, 
fires, earthquakes, 
and other natural disasters.

## Features

- User roles: Registered User, Civil Protection Officer
- User notifications for high-risk incidents
- Backend system for incident ranking and verification
- Multi-language support (English + 1 additional language)
- Authentication system for user login
- Recording and aggregation of high-risk incidents
- Statistics display for users to track incident activations
- Optional voice commands via Speech Recognition

## Getting Started

### Prerequisites

- Android Studio (latest version)
- Android SDK
- Firebase account (for cloud storage, optional)

### Installation

1. Clone this repository to your local machine.
2. Open the project in Android Studio.
3. Set up necessary dependencies and configurations.
4. Run the application on an emulator or physical device.

## Usage

- Users can first report an incident which happens nearby by adding comments on the incident and a photo describing the incident.
- Civil Protection Officers are responsible for evaluating the event and creating an alert which is sent to users near the incident.

## Backend Implementation

- For the backend i have implemented Firebase Authentication and Realtime Database. 

## Localization

The application supports English and Greek language based on system preferences on user's device.

## Recording Incidents

The users can report incidents which are later evaluated by the Civil Protection Officer.

## Statistics

The user can view statistics based on the number of incidents reported in the app.

## Screenshots Overview

### Log In Screen
<img width="144" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/a3eec092-4226-4b9d-83f5-bee51c1c6404">  <img width="144" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/01b1d6e4-88bd-46f3-b30e-ebdd6c18bdb4">



### Main Activity Screen
<img width="131" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/ea69ab0d-2da7-4d26-a135-6c96f37dc348">  <img width="132" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/55a2488b-3055-4c34-bb01-6964915cf0e6">

### Reporting an Incident
<img width="157" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/6a25685d-6bea-4d4f-9efe-014356f3fc13">  <img width="158" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/21e390cb-0869-4917-98df-12c0d100507c">

### Inbox Activity for Civilian Workers
<img width="137" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/dd324e54-fbb8-492b-998d-c17a6b79c986">  <img width="139" alt="image" src="https://github.com/AJevangelou/smart-alert/assets/35777043/713a98a2-1287-4272-bcf1-70a9373dfe7f">

## License

Copyright [2023] John Alexander Evangelou
All rights reserved. This software is not open for public use.

