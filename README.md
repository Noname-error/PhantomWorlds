# PhantomWorlds - Minigame Plugin für Bukkit/PaperMC

## 🎮 Features

### 🏗 Instance Management
- **Dynamic World Creation**: Erstelle eigene Welten für Minigames
- **Template System**: Kopiere und modifiziere Welt-Templates
- **Auto-Cleanup**: Automatische Löschung von Instanzen

### 🎯 Minigame System
- **Event-Driven Architecture**: Flexible Events für alle Spiel-Typen
- **Region-Based Logic**: Spieler-Verhalten je nach Region
- **Configurable Games**: YAML-basierte Spiel-Konfiguration
- **Multiple Game Types**: PVP, Skill, Fun, Strategy

### 🎮 Implementierte Minigames
- **TEST_SURVIVAL**: Survival Test mit Timer und Win-Conditions
- **CAPTURE_THE_FLAG**: Klassen-basierter Capture the Flag
- **LAST_MAN_STANDING**: Battle Royale Style
- **SPLEEF**: Block-Abbau Spiel
- **KING_OF_THE_HILL**: Hill-Control Spiel
- **TNT_RUN**: Rennspiel mit TNT
- **HOT_POTATO**: Weitergabe-Spiel
- **LUCKY_BLOCK**: Glücksblock-Mechanik
- **MINI_BEDWARS**: Mini BedWars

### 🎨 User Interface
- **GUI System**: Intuitive Menüs für Minigame-Auswahl
- **Command System**: Vollständige Befehls-Struktur
- **Party System**: Spieler-Gruppen-Verwaltung

## 🚀 Installation

### Voraussetzungen
- **Minecraft**: Java 17+ kompatibel
- **Server**: Bukkit/PaperMC 1.19+
- **Memory**: Mindestens 2GB RAM

### Installation
1. Lade die `phantomworlds-1.0.0.jar`
2. Kopiere die Datei in den `plugins/` Ordner
3. Starte den Server neu
4. Führe `/minigame gui` aus zum Testen

## 📖 Commands

### Instance Management
```bash
/instance create <name> <template>    # Neue Instanz erstellen
/instance delete <name>              # Instanz löschen
/instance list                       # Alle Instanzen auflisten
/instance tp <name>                  # Zu Instanz teleportieren
/instance leave                      # Instanz verlassen
```

### Minigame Commands
```bash
/minigame start <instance> <game>   # Minigame starten
/minigame join <instance>             # Minigame beitreten
/minigame leave                     # Minigame verlassen
/minigame end <instance>              # Minigame beenden
/minigame status                     # Status anzeigen
/minigame gui                        # GUI öffnen
```

### Party Commands
```bash
/party create <name>               # Party erstellen
/party join <name>                 # Party beitreten
/party leave                        # Party verlassen
/party invite <player>              # Spieler einladen
/party list                        # Party auflisten
```

## 🧪 Konfiguration

### Minigame Templates
Alle Minigames sind über YAML-Dateien konfigurierbar:
```
plugins/PhantomWorlds/minigames/
├── test_survival.yml      # Survival Test Spiel
├── capture_the_flag.yml    # Capture the Flag
├── last_man_standing.yml   # Battle Royale
└── spleef.yml             # Spleef Spiel
```

### Regions
Jedes Minigame benötigt folgende Regionen:
- **LOBBY**: Wartebereich
- **GAME**: Hauptspielbereich
- **TEAM_SPAWN**: Team-Spawnpunkte
- **SPECTATOR**: Zuschauerbereich
- **OBJECTIVE**: Spezielle Ziele

## 🔧 Entwicklung

### Build
```bash
# Plugin kompilieren
mvn clean compile package

# Tests ausführen
mvn test
```

### Projekt-Struktur
```
src/main/java/com/phantomworlds/
├── commands/           # Alle Command-Klassen
├── gui/               # GUI System
├── listeners/          # Event Listener
├── managers/           # Core Manager
├── minigames/          # Minigame System
│   ├── gametypes/     # Spiel-Typen
│   ├── events/        # Minigame Events
│   └── regions/       # Region System
└── PhantomWorlds.java   # Main Plugin Klasse
```

## 📄 Lizenz

Dieses Projekt steht unter der MIT Lizenz.

## 🤝 Mitwirkung

Contributions sind willkommen! Bitte erstelle ein Issue oder Pull Request.

## 📞 Support

Bei Problemen:
1. Erstelle ein Issue auf GitHub
2. Beschreibe das Problem detailliert
3. Füge Logs hinzu (falls vorhanden)

---

## 🎯 Quick Start

### 1. Server vorbereiten
```bash
# Plugin installieren
cp phantomworlds-1.0.0.jar plugins/
# Server neustarten
```

### 2. Test-Instanz erstellen
```bash
/instance create testworld default
```

### 3. Minigame starten
```bash
/minigame start testworld TEST_SURVIVAL
```

### 4. Spiel beitreten
```bash
/minigame join testworld
```

---

**PhantomWorlds - Das ultimative Minigame-System für Minecraft Server!** 🎮
