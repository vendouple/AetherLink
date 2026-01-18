# Build It Yourself

Compile AetherLink from source.

---

## Requirements

- **Java 21** or newer (JDK, not JRE)
- **Git** (to clone the repository)
- **Hytale Server JAR** (for compilation)

---

## Clone the Repository

```bash
git clone https://github.com/vendouple/AetherLink.git
cd AetherLink
```

---

## Add Hytale Server JAR

The project requires the Hytale server JAR for compilation (it's not included in the repo).

1. Obtain `HytaleServer.jar` from your Hytale server installation
2. Create a `libs/` folder in the project root (if it doesn't exist)
3. Copy `HytaleServer.jar` into the `libs/` folder

Your structure should look like:
```
AetherLink/
├── libs/
│   └── HytaleServer.jar
├── src/
├── build.gradle.kts
└── ...
```

---

## Build

### On Linux/macOS

```bash
./gradlew shadowJar
```

### On Windows

```cmd
gradlew.bat shadowJar
```

---

## Find the Output

The compiled JAR will be at:

```
build/libs/AetherLink-1.0.0-beta.1.jar
```

Copy this file to your Hytale server's `plugins/` folder.

---

## Build Options

### Clean Build

Remove previous build artifacts before building:

```bash
./gradlew clean shadowJar
```

### Skip Tests

```bash
./gradlew shadowJar -x test
```

---

## IDE Setup

### IntelliJ IDEA

1. Open IntelliJ IDEA
2. File → Open → Select the `AetherLink` folder
3. Wait for Gradle to sync
4. If prompted, trust the project

### VS Code

1. Install the "Extension Pack for Java" extension
2. Open the `AetherLink` folder
3. Wait for Java projects to load

---

## Troubleshooting

### "Could not find HytaleServer.jar"

Make sure you placed `HytaleServer.jar` in the `libs/` folder at the project root.

### Java version errors

Ensure you have Java 21 installed:

```bash
java -version
```

Should show version 21 or higher. If not, install Java 21 from [Adoptium](https://adoptium.net/).

### Gradle wrapper permission denied (Linux/macOS)

```bash
chmod +x gradlew
./gradlew shadowJar
```

---

## Project Structure

| Path | Description |
|------|-------------|
| `src/main/java/` | Java source code |
| `src/main/resources/` | Plugin manifest and resources |
| `libs/` | Dependencies not in Maven (HytaleServer.jar) |
| `build.gradle.kts` | Build configuration |
| `settings.gradle.kts` | Project settings |

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run `./gradlew build` to verify
5. Submit a pull request

See the [GitHub repository](https://github.com/vendouple/AetherLink) for contribution guidelines.
