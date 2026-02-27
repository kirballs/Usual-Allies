# Usual Allies – Required Assets

This document lists every asset file the mod expects at runtime, grouped by type, together with the folder each file must be placed in.

All paths are relative to `src/main/resources/` in the repository (or the root of a resource pack if you are supplying assets externally).

---

## Textures

### Kirb – Body (64×64 PNG, opaque)

Folder: `assets/usualallies/textures/entity/kirb/body/`

| File | Description |
|------|-------------|
| `body_default.png` | Default pink body (used when no dye colour is set) |
| `body_white.png` | White dye colour |
| `body_orange.png` | Orange dye colour |
| `body_magenta.png` | Magenta dye colour |
| `body_light_blue.png` | Light-blue dye colour |
| `body_yellow.png` | Yellow dye colour |
| `body_lime.png` | Lime dye colour |
| `body_pink.png` | Pink dye colour |
| `body_gray.png` | Gray dye colour |
| `body_light_gray.png` | Light-gray dye colour |
| `body_cyan.png` | Cyan dye colour |
| `body_purple.png` | Purple dye colour |
| `body_blue.png` | Blue dye colour |
| `body_brown.png` | Brown dye colour |
| `body_green.png` | Green dye colour |
| `body_red.png` | Red dye colour |
| `body_black.png` | Black dye colour |

### Kirb – Face Overlay (64×64 PNG, transparent outside face-bone UV area ≈ [34,48]–[49,64])

Folder: `assets/usualallies/textures/entity/kirb/face/`

| File | Description |
|------|-------------|
| `face_o.png` | Default / unfriended face; also shown at medium health |
| `face_happy.png` | Befriended Kirb at full / high health |
| `face_mouthful.png` | Entity captured in Kirb's mouth |
| `face_open.png` | Inhaling or expelling a star projectile |
| `face_lowhp.png` | Low-health / danger state |

### Projectile Entity Textures (PNG, cutout-transparent)

Folder: `assets/usualallies/textures/entity/`

| File | Description |
|------|-------------|
| `air_bullet.png` | Air-bullet projectile (used by GeckoLib `AirBulletModel`) |
| `friend_heart.png` | Friend-Heart projectile (billboard quad renderer) |
| `star_projectile.png` | Star projectile (spinning billboard quad renderer) |

### Item Textures (16×16 PNG)

Folder: `assets/usualallies/textures/item/`

| File | Description |
|------|-------------|
| `friend_heart.png` | Friend Heart item icon |
| `one_up.png` | 1-Up item icon |

### Particle Textures (PNG)

Folder: `assets/usualallies/textures/particle/`

| File | Particle type | Description |
|------|---------------|-------------|
| `air_big_0.png` | `usualallies:air_big` | Large air-puff – inhale suction (big variant) |
| `air_medium_0.png` | `usualallies:air_medium` | Medium air-puff – inhale suction (medium variant) |
| `air_small_0.png` | `usualallies:air_small` | Small air-puff – inhale suction (small variant) |

---

## Sounds

All sound files must be **OGG Vorbis** format (`.ogg`).

### Kirb Sounds

Folder: `assets/usualallies/sounds/kirb/`

| File | Sound event | Description |
|------|-------------|-------------|
| `step.ogg` | `usualallies:kirb.step` | Footstep (plays every step) |
| `jump.ogg` | `usualallies:kirb.jump` | Jump |
| `walk.ogg` | `usualallies:kirb.walk` | Walk/waddle loop (plays each animation cycle) |
| `flap.ogg` | `usualallies:kirb.flap` | Wing flap during flight |
| `exhale.ogg` | `usualallies:kirb.exhale` | Exhale / air-bullet release |
| `inhale1.ogg` | `usualallies:kirb.inhale1` | Inhale charge-up (plays once at start) |
| `inhale2.ogg` | `usualallies:kirb.inhale2` | Inhale loop (streams; plays continuously after inhale1) |
| `mouthful.ogg` | `usualallies:kirb.mouthful` | Entity enters Kirb's mouth |
| `spit.ogg` | `usualallies:kirb.spit` | Spit / star-launch |
| `swallow.ogg` | `usualallies:kirb.swallow` | Kirb swallows captured entity |
| `tamed.ogg` | `usualallies:kirb.tamed` | Befriending / taming jingle |
| `one_up.ogg` | `usualallies:kirb.one_up` | 1-Up item given to Kirb |
| `hurt1.ogg` | `usualallies:kirb.hurt1` | Hurt sound variant 1 (randomly chosen) |
| `hurt2.ogg` | `usualallies:kirb.hurt2` | Hurt sound variant 2 |
| `hurt3.ogg` | `usualallies:kirb.hurt3` | Hurt sound variant 3 |

### Ally Sounds

Folder: `assets/usualallies/sounds/ally/`

| File | Sound event | Description |
|------|-------------|-------------|
| `lowhp.ogg` | `usualallies:ally.lowhp` | Alert played when any ally drops into low-health state |

### Friend Heart Sounds

Folder: `assets/usualallies/sounds/friend_heart/`

| File | Sound event | Description |
|------|-------------|-------------|
| `throw.ogg` | `usualallies:friend_heart.throw` | Friend Heart thrown |
| `hit.ogg` | `usualallies:friend_heart.hit` | Friend Heart successfully converts a mob |

---

## GeckoLib JSON Files (already in repo)

These data-driven files are already committed and do **not** need to be supplied separately.

| File | Location |
|------|----------|
| Kirb geometry | `assets/usualallies/geo/kirb.geo.json` |
| Air-bullet geometry | `assets/usualallies/geo/air_bullet.geo.json` |
| Kirb animations | `assets/usualallies/animations/kirb.animation.json` |
| Air-bullet animations | `assets/usualallies/animations/air_bullet.animation.json` |

---

## Summary

| Asset type | Count |
|------------|-------|
| Body textures (Kirb colour variants) | 17 |
| Face overlay textures | 5 |
| Projectile / entity textures | 3 |
| Item textures | 2 |
| Particle textures | 3 |
| Sound files (OGG) | 18 |
| **Total** | **48** |
