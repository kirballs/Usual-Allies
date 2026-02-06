# Usual Allies - Minecraft Forge Mod
**A companion mod featuring helpful allies for Minecraft 1.20.1**

## Required External Setup

Since Forge mod configuration files follow standardized formats, you'll need to obtain them from the official Forge MDK (Mod Development Kit):

### Step 1: Download Forge MDK
1. Visit: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
2. Download **Forge 47.3.0 MDK** (or later 47.x version)
3. Extract the MDK zip file

### Step 2: Copy Required Files to This Repository

From the extracted MDK, copy these files to your repository root:

**Gradle Build Files:**
- `build.gradle` - Main build configuration
- `gradle.properties` - Project properties  
- `settings.gradle` - Gradle settings
- `gradlew` - Gradle wrapper script (Unix/Mac)
- `gradlew.bat` - Gradle wrapper script (Windows)
- `gradle/` directory - Contains wrapper JAR and properties

**Configuration:**
- `.gitignore` - Git ignore patterns (or merge with existing)

### Step 3: Customize gradle.properties

After copying, edit `gradle.properties` with these values:
```
mod_id=usualallies
mod_name=Usual Allies
mod_version=1.0.0
mod_group_id=com.kirballs.usualallies
mod_author=kirballs
minecraft_version=1.20.1
forge_version=47.3.0
```

### Step 4: Verify Structure

Your repository should have:
```
Usual-Allies/
├── gradle/
│   └── wrapper/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── .github/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

## Building the Mod

Once setup is complete:

```bash
# Generate IDE workspace (first time only)
./gradlew genIntellijRuns  # For IntelliJ IDEA
# OR
./gradlew genEclipseRuns   # For Eclipse

# Build the mod JAR
./gradlew build

# Output will be in: build/libs/usualallies-1.0.0.jar
```

## Development

See `SETUP-INSTRUCTIONS.md` for detailed information about the mod structure and customization.
