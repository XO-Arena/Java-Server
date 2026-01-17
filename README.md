# 🎮 XO Arena – Server Application

XO Arena Server is the robust backend engine for the XO Arena multiplayer ecosystem. Built with Java SE, it manages high-concurrency player connections, orchestrates real-time matches, and ensures seamless communication between clients using a structured TCP protocol.

## 👥 Team Members
- Mohannad El-Sayeh
- Ahmed El-Sayyad
- Esraa Ehab
- Mohamed Ayman

## 📌 Project Overview
The server acts as the central coordinator (the "Brain") of the game. It manages the lifecycle of a player from the moment they log in until they finish a match.

**Key Responsibilities:**
- **Matchmaking:** Connecting players via "Quick Match" or private invitations.
- **State Validation:** Ensuring every move follows Tic-Tac-Toe rules before broadcasting.
- **Real-time Streaming:** Updating spectators and players instantly as moves occur.
- **Concurrency:** Handling dozens of simultaneous games without performance drops.

> **Note on Storage:** To keep the server lightweight and fast, game recordings (replays) are stored locally on the client-side. The server only manages "Live" data.

## ✨ Key Features
### 🔐 Authentication & Security
- Secure User Registration and Login/Logout systems.
- Player status tracking (Online, InGame,Watching, Offline).

### 🔗 Multiplayer & Matchmaking
- **Quick Match:** Automatically pairs waiting players for instant action.
- **Invitation System:** Allows players to challenge specific friends from the online list.
- **Spectator Mode:** Users can join "Live" rooms to watch ongoing matches in real-time.

### ⚡ Technical Performance
- **Multithreaded Architecture:** Every client connection is handled in a separate thread to prevent blocking.
- **JSON Communication:** Uses Gson for structured, lightweight data exchange.
- **MVC Pattern:** Separation of Models (Data), Controllers (Logic), and Services (Actions).

## 🛠️ Technologies Used
- **Language:** Java SE (JDK 8 or higher)
- **Networking:** TCP Sockets (`java.net.ServerSocket`)
- **Data Handling:** Gson (Google JSON library)
- **Design Pattern:** Model-View-Controller (MVC)

## 📁 Project Structure
```text
server/
├─ controllers/ # Processes incoming requests & routes them to services
├─ models/ # Entity classes (Player, GameSession, Move)
├─ services/ # Logic for matchmaking, auth, and session management
├─ dto/ # Data Transfer Objects for JSON serialization
├─ enums/ # Constants (GameState, RequestType, PlayerStatus)
├─ utils/ # Database helpers and network utilities
└─ ServerApp.java # The main entry point and port listener
```


## ⚙️ Requirements & Installation
### Prerequisites
- Java JDK 8 or higher.
- IDE: IntelliJ IDEA (Recommended), NetBeans, or Eclipse.
- Gson Library: Ensure `gson.jar` is added to your project dependencies.

### How to Run
1. **Clone the Repository**
```bash
git clone https://github.com/XO-Arena/Java-Server.git

Open in IDE: Import the project as a Java application.

Launch the Server: Run App.java. The console will indicate that the server is listening (usually on port 4646 or your configured port).

Connect Clients: Point your XO Arena Client applications to the server's IP address.

🔄 System Flow

Connection: Client connects via TCP Socket.

Auth: Client sends a Login DTO; Server validates and returns a Success/Fail response.

Match: Client requests a game; Server moves the player to a GameSession.

Play: Player A moves → Server validates → Server broadcasts move to Player B and Spectators.
