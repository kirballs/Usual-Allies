# Usual Allies – Required Assets

This document lists every asset file the mod expects at runtime, grouped by type, together with the folder each file must be placed in.

All paths are relative to `src/main/resources/` in the repository (or the root of a resource pack if you are supplying assets externally).

---

## Textures

### Kirb – Body (64×64 PNG, opaque)

Folder: `assets/usualallies/textures/entity/kirb/body/`

- `body_default.png` – Default pink body (used when no dye colour is set)
- `body_white.png` – White dye colour
- `body_orange.png` – Orange dye colour
- `body_magenta.png` – Magenta dye colour
- `body_light_blue.png` – Light-blue dye colour
- `body_yellow.png` – Yellow dye colour
- `body_lime.png` – Lime dye colour
- `body_pink.png` – Pink dye colour
- `body_gray.png` – Gray dye colour
- `body_light_gray.png` – Light-gray dye colour
- `body_cyan.png` – Cyan dye colour
- `body_purple.png` – Purple dye colour
- `body_blue.png` – Blue dye colour
- `body_brown.png` – Brown dye colour
- `body_green.png` – Green dye colour
- `body_red.png` – Red dye colour
- `body_black.png` – Black dye colour

### Kirb – Face Overlay (64×64 PNG, transparent outside face-bone UV area ≈ [34,48]–[49,64])

Folder: `assets/usualallies/textures/entity/kirb/face/`

- `face_o.png` – Default / unfriended face; also shown at medium health
- `face_happy.png` – Befriended Kirb at full / high health
- `face_mouthful.png` – Entity captured in Kirb's mouth
- `face_open.png` – Inhaling or expelling a star projectile
- `face_lowhp.png` – Low-health / danger state

### Projectile Entity Textures (PNG, cutout-transparent)

Folder: `assets/usualallies/textures/entity/`

- `air_bullet.png` – Air-bullet projectile (used by GeckoLib `AirBulletModel`)
- `friend_heart.png` – Friend-Heart projectile (billboard quad renderer)
- `star_projectile.png` – Star projectile (spinning billboard quad renderer)

### Item Textures (16×16 PNG)

Folder: `assets/usualallies/textures/item/`

- `friend_heart.png` – Friend Heart item icon
- `one_up.png` – 1-Up item icon

### Particle Textures (PNG)

Folder: `assets/usualallies/textures/particle/`

- `air_big_0.png` – Large air-puff, particle type `usualallies:air_big` (inhale suction, big variant)
- `air_medium_0.png` – Medium air-puff, particle type `usualallies:air_medium` (inhale suction, medium variant)
- `air_small_0.png` – Small air-puff, particle type `usualallies:air_small` (inhale suction, small variant)

---

## Sounds

All sound files must be **OGG Vorbis** format (`.ogg`).

### Kirb Sounds

Folder: `assets/usualallies/sounds/kirb/`

- `step.ogg` – Footstep (plays every step) — event `usualallies:kirb.step`
- `jump.ogg` – Jump — event `usualallies:kirb.jump`
- `walk.ogg` – Walk/waddle loop (plays each animation cycle) — event `usualallies:kirb.walk`
- `flap.ogg` – Wing flap during flight — event `usualallies:kirb.flap`
- `exhale.ogg` – Exhale / air-bullet release — event `usualallies:kirb.exhale`
- `inhale1.ogg` – Inhale charge-up (plays once at start) — event `usualallies:kirb.inhale1`
- `inhale2.ogg` – Inhale loop (streams; plays continuously after inhale1) — event `usualallies:kirb.inhale2`
- `mouthful.ogg` – Entity enters Kirb's mouth — event `usualallies:kirb.mouthful`
- `spit.ogg` – Spit / star-launch — event `usualallies:kirb.spit`
- `swallow.ogg` – Kirb swallows captured entity — event `usualallies:kirb.swallow`
- `tamed.ogg` – Befriending / taming jingle — event `usualallies:kirb.tamed`
- `one_up.ogg` – 1-Up item given to Kirb — event `usualallies:kirb.one_up`
- `hurt1.ogg` – Hurt sound variant 1 (randomly chosen) — event `usualallies:kirb.hurt1`
- `hurt2.ogg` – Hurt sound variant 2 — event `usualallies:kirb.hurt2`
- `hurt3.ogg` – Hurt sound variant 3 — event `usualallies:kirb.hurt3`

### Ally Sounds

Folder: `assets/usualallies/sounds/ally/`

- `lowhp.ogg` – Alert played when any ally drops into low-health state — event `usualallies:ally.lowhp`

### Friend Heart Sounds

Folder: `assets/usualallies/sounds/friend_heart/`

- `throw.ogg` – Friend Heart thrown — event `usualallies:friend_heart.throw`
- `hit.ogg` – Friend Heart successfully converts a mob — event `usualallies:friend_heart.hit`

---

## Animations

All Kirb animations are stored in a single file that is already committed to the repository.

File: `assets/usualallies/animations/kirb.animation.json`

- `animation.kirb.idle` – Gentle up-and-down bob while standing still (loops)
- `animation.kirb.walk` – Leg/arm swing for slow walking (loops)
- `animation.kirb.run` – Faster leg/arm swing with a body bounce for running (loops)
- `animation.kirb.jump` – Arms spread wide on the way up (plays once)
- `animation.kirb.fly` – Arms flap up and down while puffed up in the air (loops)
- `animation.kirb.inhale` – Head pulses slightly larger while inhaling (loops)
- `animation.kirb.mouthful` – Head stays slightly enlarged while an entity is held in the mouth (loops)
- `animation.kirb.swallow` – Head squash-and-stretch as Kirb swallows (plays once)
- `animation.kirb.spit` – Head recoil as Kirb spits (plays once)
- `animation.kirb.air_bullet` – Head compress then release when firing an air bullet (plays once)
- `animation.kirb.held` – Tilted-back pose while Kirb is being carried by the player (loops)
- `animation.kirb.pushback` – Brief knockback slide when Kirb is hit (plays once)

The air-bullet projectile animation is in a separate file that is also already committed:

File: `assets/usualallies/animations/air_bullet.animation.json`

---

## GeckoLib JSON Files (already in repo)

These data-driven files are already committed and do **not** need to be supplied separately.

- `assets/usualallies/geo/kirb.geo.json` – Kirb entity geometry
- `assets/usualallies/geo/air_bullet.geo.json` – Air-bullet projectile geometry
- `assets/usualallies/animations/kirb.animation.json` – All Kirb animations (see above)
- `assets/usualallies/animations/air_bullet.animation.json` – Air-bullet animation

---

## Summary

- Body textures (Kirb colour variants): 17
- Face overlay textures: 5
- Projectile / entity textures: 3
- Item textures: 2
- Particle textures: 3
- Sound files (OGG): 18
- Animations (in kirb.animation.json): 12
- **Total assets to supply: 48**
