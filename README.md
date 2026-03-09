# ShiftTracker

ShiftTracker is a mobile application designed to simplify attendance tracking using NFC technology.

The idea behind the project came from noticing that many attendance systems are still slow, inconvenient, or easy to manipulate. ShiftTracker aims to make the process faster and more reliable by allowing users to register their attendance simply by scanning an NFC tag with their phone.

This project was developed as my final project for the **Multiplatform Application Development (DAM)** program.

---

# Motivation

In many organizations, attendance tracking still relies on manual processes like paper sheets, physical punch cards, or complicated digital systems.

I wanted to build something that would be:

* simple to use
* quick for users
* easy to manage
* harder to manipulate or falsify

NFC technology provided a simple and practical way to achieve this.

---

# How it works

The system is based on a simple interaction between three components.

**User**

Each user is associated with an NFC tag.

**Device**

An Android device with NFC detects the tag when it is placed near the phone.

**Attendance record**

The application automatically records the check-in or check-out and stores it in the system.

The whole process takes only a few seconds.

---

# Main Features

### NFC-based attendance tracking

Users can register their attendance by scanning their NFC tag with the device.

### User management

The system allows administrators to create and manage users within the application.

### Attendance history

Attendance records can be reviewed easily to see when users checked in or out.

### Simple interface

The interface was designed to be clear and easy to use, minimizing friction during the check-in process.

---

# Application Screenshots

## Main screen

![Pantalla principal](<img width="309" height="692" alt="image" src="https://github.com/user-attachments/assets/325dbeaf-9791-464a-aa65-49f3e87c00f9" />)

## NFC check-in

![Registro NFC](<img width="237" height="522" alt="image" src="https://github.com/user-attachments/assets/91773891-bfd6-43ad-9f6f-c38b2727dc95" />)

## Attendance history

![Historial](<img width="311" height="688" alt="image" src="https://github.com/user-attachments/assets/839a40e8-b001-4499-9b56-059c974b1858" />)

---

# Technologies Used

This project was built using:

* Android
* Java
* NFC (Near Field Communication)
* SQLite

---

# Possible Future Improvements

Some ideas for future development include:

* Cloud synchronization
* Web dashboard for administrators
* Advanced reporting and analytics
* Multi-device synchronization
* Integration with company management systems

---

# Repository

GitHub repository:
https://github.com/lucasTejel/ShiftTracker

---

# Author

Lucas Tejel

If you are interested in the project or have suggestions for improvements, feel free to open an issue or contact me.

